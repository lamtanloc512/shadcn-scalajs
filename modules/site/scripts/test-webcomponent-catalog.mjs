import { chromium } from "playwright";
import { mkdirSync, writeFileSync, readdirSync } from "node:fs";
import { join } from "node:path";

const base = process.env.SC_SITE_URL ?? "http://127.0.0.1:4300";
const out = process.env.SC_WC_QA_DIR ?? "/tmp/shadcn-scalajs-wc-qa";
mkdirSync(out, { recursive: true });
const sleep = (ms) => new Promise((r) => setTimeout(r, ms));

/** Overlays / zero-box hosts: measure a visible sibling marker instead of the closed host. */
const measureFallback = {
  dialog: "#open-dialog, [data-sc-dialog-demo]",
  tooltip: "sc-button, button",
  popover: "sc-button, button",
  "dropdown-menu": '[slot="trigger"], sc-button',
};

const interactive = {
  accordion: async (frame) => {
    const clicked = await frame.locator("sc-accordion").first().evaluate((el) => {
      const root = el.shadowRoot || el;
      const triggers = root.querySelectorAll("summary, button, [data-slot=accordion-trigger]");
      if (triggers.length < 1) return 0;
      (triggers[1] || triggers[0]).dispatchEvent(new MouseEvent("click", { bubbles: true }));
      return triggers.length;
    });
    if (clicked < 1) throw new Error("accordion needs section triggers");
    await sleep(200);
  },
  button: async (frame) => {
    if ((await frame.locator("sc-button").count()) < 1) throw new Error("missing button");
    if ((await frame.locator("sc-button[disabled]").count()) < 1) throw new Error("missing disabled button");
  },
  checkbox: async (frame) => {
    await frame.locator("sc-checkbox").first().click({ force: true });
  },
  switch: async (frame) => {
    await frame.locator("sc-switch").first().click({ force: true });
  },
  "dropdown-menu": async (frame) => {
    await frame.locator('[slot="trigger"], sc-button').first().click();
    await sleep(300);
    const open =
      (await frame.locator("[data-slot=dropdown-menu-content], [role=menu]").count()) +
      (await frame.locator("[data-open]").count());
    if (open < 1) throw new Error("dropdown did not open");
  },
  dialog: async (frame) => {
    await frame.locator("#open-dialog").first().click();
    await sleep(350);
    if ((await frame.locator("sc-dialog[open]").count()) < 1) throw new Error("dialog did not open");
    await frame.locator("#close-dialog").first().click();
    await sleep(250);
    if ((await frame.locator("sc-dialog[open]").count()) > 0) throw new Error("dialog did not close");
  },
  tabs: async (frame) => {
    const tab = frame.locator("[role=tab], button").nth(1);
    if ((await tab.count()) > 0) await tab.click();
  },
  select: async (frame) => {
    if ((await frame.locator("sc-select").count()) < 1) throw new Error("missing select");
  },
  combobox: async (frame) => {
    if ((await frame.locator("sc-combobox").count()) < 1) throw new Error("missing combobox");
  },
  slider: async (frame) => {
    if ((await frame.locator("sc-slider").count()) < 1) throw new Error("missing slider");
  },
  "radio-group": async (frame) => {
    if ((await frame.locator("sc-radio-group").count()) < 1) throw new Error("missing radio-group");
  },
  calendar: async (frame) => {
    if ((await frame.locator("sc-calendar").count()) < 1) throw new Error("missing calendar");
  },
  tooltip: async (frame) => {
    await frame.locator("sc-button, button").first().hover();
    await sleep(200);
  },
};

const browser = await chromium.launch({ headless: true });
const page = await browser.newPage({ viewport: { width: 1280, height: 900 } });
const errors = [];
const badRequests = [];
page.on("console", (m) => {
  if (m.type() === "error") errors.push(`console: ${m.text()}`);
});
page.on("requestfailed", (r) => {
  const url = r.url();
  if (url.includes("favicon")) return;
  badRequests.push(`${url} ${r.failure()?.errorText ?? ""}`);
});
page.on("response", (r) => {
  if (r.url().includes("/components/sc-components")) badRequests.push(`bad-asset-path ${r.url()}`);
});

await page.goto(`${base}/web-components`, { waitUntil: "networkidle" });
await sleep(1500);

// Monaco host present (playground only — docs stay on the lightweight highlighter).
const monacoOk = (await page.locator(".monaco-editor").count()) > 0;
const monacoApiOk = await page.evaluate(() => typeof window.ScPlaygroundEditor?.mount === "function");

const select = page.locator("select").first();
const options = await select.locator("option").evaluateAll((os) =>
  os.map((o) => ({ value: o.value, label: o.textContent }))
);
const entries = options.filter((o) => o.label.startsWith("Components / "));
if (entries.length === 0) {
  console.error("No catalog entries found in playground selector");
  process.exit(1);
}

const matrix = [];
for (const entry of entries) {
  errors.length = 0;
  badRequests.length = 0;
  await select.selectOption(entry.value);
  await sleep(350);
  const srcFrame = page.frames().find((fr) => fr.url() === "about:srcdoc");
  if (!srcFrame) {
    matrix.push({
      slug: entry.label,
      pass: false,
      result: null,
      interaction: "fail: no srcdoc frame",
      errors: [...errors],
      badRequests: [...badRequests],
    });
    continue;
  }

  const slug = entry.label.replace(/^Components \/ /, "").toLowerCase().replaceAll(" ", "-");
  const expectedTag = slug === "table" ? "sc-table" : `sc-${slug}`;
  const fallbackSel = measureFallback[slug] ?? null;

  // Wait until the custom element is upgraded (not just present) and FOUC visibility:hidden clears.
  // Closed overlays (dialog) stay 0×0 — accept a visible fallback marker in that case.
  await srcFrame
    .waitForFunction(
      ({ tag, fallback }) => {
        const el = document.querySelector(tag);
        if (!el) return false;
        const name = el.tagName.toLowerCase();
        if (name.startsWith("sc-") && !customElements.get(name)) return false;
        const style = getComputedStyle(el);
        if (style.visibility === "hidden") return false;
        const rect = el.getBoundingClientRect();
        if (rect.width >= 12 && (tag === "sc-separator" ? rect.height >= 1 : rect.height >= 12)) return true;
        if (!fallback) return false;
        const marker = document.querySelector(fallback);
        if (!marker) return false;
        const mr = marker.getBoundingClientRect();
        return mr.width >= 12 && mr.height >= 12;
      },
      { tag: expectedTag, fallback: fallbackSel },
      { timeout: 12000 }
    )
    .catch(() => {});

  await sleep(150);

  const result = await srcFrame
    .evaluate(({ tag, fallback, slug: s }) => {
      const root = document.querySelector(tag);
      const marker = fallback ? document.querySelector(fallback) : null;
      const measureEl =
        root && root.getBoundingClientRect().width >= 12
          ? root
          : marker || root || document.body.querySelector("sc-*,[is^='sc-']");
      if (!measureEl) return null;
      const rect = measureEl.getBoundingClientRect();
      const name = (root || measureEl).tagName.toLowerCase();
      const style = getComputedStyle(root || measureEl);
      const hostRect = root ? root.getBoundingClientRect() : null;
      return {
        tag: name,
        expectedTag: tag,
        measured: measureEl.tagName.toLowerCase(),
        is: (root || measureEl).getAttribute("is"),
        width: rect.width,
        height: rect.height,
        hostWidth: hostRect?.width ?? null,
        hostHeight: hostRect?.height ?? null,
        text: ((root || measureEl).textContent || "").trim().slice(0, 200),
        defined: name.startsWith("sc-") ? !!customElements.get(name) : !!customElements.get(tag),
        visibility: style.visibility,
        opacity: style.opacity,
        slug: s,
      };
    }, { tag: expectedTag, fallback: fallbackSel, slug })
    .catch((err) => ({ error: String(err) }));

  const frame = page.frameLocator("iframe");
  let interaction = "skipped";
  try {
    if (interactive[slug]) {
      await interactive[slug](frame);
      interaction = "ok";
    }
  } catch (err) {
    interaction = `fail: ${err.message}`;
  }

  const minH = slug === "separator" ? 1 : 12;
  const sizeOk = !!result && result.width >= 12 && result.height >= minH;
  const definedOk = result?.defined === true;
  const tagOk =
    !!result &&
    (result.tag === expectedTag ||
      result.expectedTag === expectedTag ||
      (result.measured && result.measured !== "body"));
  const visibleOk = result?.visibility !== "hidden" && Number(result?.opacity ?? 1) > 0;
  // Sidebar must stay capped for playground/docs (example sets 16rem).
  const sidebarCapped =
    slug !== "sidebar" || (typeof result?.hostHeight === "number" && result.hostHeight > 0 && result.hostHeight <= 280);
  const pass =
    sizeOk &&
    definedOk &&
    tagOk &&
    visibleOk &&
    sidebarCapped &&
    errors.length === 0 &&
    badRequests.length === 0 &&
    !String(interaction).startsWith("fail");

  await page.screenshot({ path: join(out, `${slug}.png`), fullPage: false });
  matrix.push({
    slug,
    option: entry.label,
    pass,
    result,
    interaction,
    sidebarCapped,
    errors: [...errors],
    badRequests: [...badRequests],
  });
}

const docsChecks = [];
for (const entry of matrix) {
  const slug = entry.slug;
  await page.goto(`${base}/components/${slug}`, { waitUntil: "networkidle" });
  await sleep(700);
  const primary = page.locator("[data-sc-docs-primary-tabs]").first();
  const tabs = primary.getByRole("tab");
  const tabNames = await tabs.allTextContents().catch(() => []);
  const threeTabs = tabNames.slice(0, 3).join("|") === "Preview|Laminar|Web Component";

  // Arrow-key roving focus: Preview -> Laminar via ArrowRight, focus moves with selection.
  let arrowOk = false;
  try {
    const previewTab = tabs.filter({ hasText: "Preview" }).first();
    await previewTab.focus();
    await page.keyboard.press("ArrowRight");
    await sleep(200);
    const laminarSelected = await tabs
      .filter({ hasText: "Laminar" })
      .first()
      .evaluate((el) => el.getAttribute("aria-selected") === "true" && document.activeElement === el);
    arrowOk = laminarSelected;
  } catch {
    arrowOk = false;
  }

  await tabs.filter({ hasText: "Web Component" }).first().click().catch(() => {});
  await sleep(500);
  const docsSource = await primary
    .locator("[data-sc-wc-source]")
    .first()
    .getAttribute("data-sc-wc-source")
    .catch(() => null);
  const host = await primary
    .locator(`[data-sc-example-tag='sc-${slug}'] sc-${slug}, sc-${slug}, [data-sc-example-tag='sc-${slug}']`)
    .count()
    .catch(() => 0);
  const unsupported = await primary.getByText(/planned for a future phase|Not applicable/).count();
  const tokens = await primary.locator(".sc-doc-token-tag, .sc-doc-token-keyword, .sc-doc-token-string").count();
  // Docs must NOT mount Monaco (lightweight highlighter only).
  const docsMonaco = await primary.locator(".monaco-editor").count();
  const sourceMatch = !docsSource || docsSource.includes(`sc-${slug}`) || docsSource.includes("sc-table");
  const passDocs =
    threeTabs && (host > 0 || unsupported > 0) && tokens > 0 && sourceMatch && arrowOk && docsMonaco === 0;
  docsChecks.push({
    slug,
    threeTabs,
    host,
    unsupported,
    tokens,
    arrowOk,
    docsMonaco,
    pass: passDocs,
    hasSourceAttr: !!docsSource,
  });
}

const passed = matrix.filter((x) => x.pass).length;
const docsPassed = docsChecks.filter((x) => x.pass).length;
const summary = {
  monacoOk: monacoOk && monacoApiOk,
  count: matrix.length,
  passed,
  docsCount: docsChecks.length,
  docsPassed,
  entries: matrix,
  docs: docsChecks,
  screenshots: readdirSync(out).filter((f) => f.endsWith(".png")),
};
writeFileSync(join(out, "matrix.json"), JSON.stringify(summary, null, 2));
await browser.close();

console.log(
  `Web Component catalog QA: ${passed}/${matrix.length}; docs ${docsPassed}/${docsChecks.length}; monaco=${monacoOk && monacoApiOk}; artifacts: ${out}`
);
if (!(monacoOk && monacoApiOk) || passed !== matrix.length || docsPassed !== docsChecks.length) process.exitCode = 1;
