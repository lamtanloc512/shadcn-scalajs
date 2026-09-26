import assert from "node:assert/strict";

import { chromium } from "playwright";

const baseUrl = process.env.SC_WC_TEST_URL ?? "http://127.0.0.1:4173";
const browser = await chromium.launch();
const page = await browser.newPage();
await page.addInitScript(() => {
  localStorage.setItem("shadcn-scalajs:theme", JSON.stringify({ stylePack: "rhea", darkMode: true }));
});

let releaseCss;
const cssReleased = new Promise(resolve => {
  releaseCss = resolve;
});
let cssRequested;
const sawCssRequest = new Promise(resolve => {
  cssRequested = resolve;
});
let cssRequestCount = 0;

await page.route("**/sc-components.css", async route => {
  cssRequestCount += 1;
  cssRequested();
  await cssReleased;
  await route.continue();
});

try {
  await page.goto(`${baseUrl}/wc-fouc-fixture.html`, { waitUntil: "domcontentloaded" });
  await sawCssRequest;

  assert.equal(
    await page.evaluate(() => customElements.get("sc-button") !== undefined),
    false,
    "components must not upgrade before their shadow-root stylesheet is ready"
  );
  assert.equal(
    await page.evaluate(() => getComputedStyle(document.querySelector("sc-button")).visibility),
    "hidden",
    "undefined elements must not expose their raw light-DOM content"
  );
  assert.equal(
    await page.evaluate(() => document.documentElement.classList.contains("dark")),
    true,
    "the stored color mode must apply before the component bundle is ready"
  );

  releaseCss();
  await page.waitForFunction(() => customElements.get("sc-button") !== undefined, null, { timeout: 120000 });
  await page.waitForFunction(
    () => {
      const button = document.querySelector("sc-button")?.shadowRoot?.querySelector("button");
      return button && getComputedStyle(button).display === "inline-flex";
    },
    null,
    { timeout: 120000 }
  );
  assert.ok(cssRequestCount >= 1, "stylesheet must be requested");
} finally {
  releaseCss();
  await browser.close();
}

console.log("Web components wait for CSS before upgrading");
