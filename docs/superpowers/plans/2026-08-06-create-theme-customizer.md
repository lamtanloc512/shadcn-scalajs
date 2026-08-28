# Create Page: Theme Customizer + Dashboard Preview Block — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a `/create` page with a 9-control theme customizer sidebar (driving a site-wide, localStorage-persisted `ThemeConfig`) and a live, original dashboard preview block, plus a genuinely swappable Lucide/Hugeicons icon system for the components that actually need one.

**Architecture:** A single `ThemeConfig` case class (in `modules/site`) replaces the per-page `darkMode`/`stylePack` `Var`s every page currently creates independently; it's persisted to `localStorage` and applied by setting `data-*` attributes (plus the `dark` class) on `<html>`, so `globals.css` attribute-selector blocks (following the pattern the existing `[data-style-pack="..."]` rules already use) drive every visual change. A parallel `Icons` module (in `modules/ui`) centralizes the small number of icons real components actually hand-roll today, resolving to Lucide or Hugeicons path data based on the same persisted config. The dashboard preview is a new, original `Dashboard01` block under `modules/blocks`, following the existing `login01`/`signup01`/`otp01`/`calendar01` convention.

**Tech Stack:** Scala 3 / Scala.js / Laminar (existing `modules/core`, `modules/ui`, `modules/blocks`, `modules/site`), Tailwind CSS v4 (`modules/site/src/styles/globals.css`), sbt, headless Chrome via `puppeteer-core` for verification (this project has no unit-test framework configured — see Global Constraints).

## Global Constraints

- No existing Scala test framework is configured in this repo (verified: no `src/test` directories, no munit/utest/scalatest in `build.sbt`). Every task's verification is: `sbt <module>/compile` + `sbt scalafmtAll`, then a headless-browser check via `puppeteer-core` (Chrome at `/Applications/Google Chrome.app/Contents/MacOS/Google Chrome`; `npm install --no-save puppeteer-core` from `modules/site` if `node_modules/puppeteer-core` isn't already present; the verification script must live inside `modules/site/` itself, since Node ESM resolves `node_modules` relative to the importing file's own directory, not cwd) — this replaces the "write failing test / implement / run test" steps used in other stacks. Delete every temp verification script and kill the dev server (`lsof -ti:4300 | xargs -r kill`) at the end of each task.
- Start the dev server with `cd modules/site && npm run dev > /tmp/<task-name>-dev.log 2>&1 &`, then poll `curl -s -o /dev/null -w "%{http_code}" http://localhost:4300/` until it returns `200` before running any browser check.
- Run `sbt scalafmtAll` (not just `scalafmtCheckAll`) after every code change, before verifying — this repo enforces scalafmt in CI (`scripts/lint`).
- Follow the `cn-*` class-prefix convention for any new root-level class name that could collide with legacy basecoat CSS still loaded via `globals.css`'s `@import "./basecoat.generated.css"` (see `Combobox.scala`'s `cn-combobox` class and its comment for why — a real, previously-hit bug in this exact codebase).
- New Scala.js icon SVGs must set an explicit size class (e.g. `size-4`) directly on the `<svg>` — do not rely on an ancestor's `[&_svg:not([class*='size-'])]:size-4` Tailwind rule existing, since not every container has one (a real bug hit earlier in `Combobox.scala`'s check icon, which rendered at ~150px before this was fixed).
- Every new/modified component file must pass `sbt ui/compile` (or the relevant module) and `sbt scalafmtAll` before being considered done.

---

### Task 1: `ThemeConfig` data model + persistence

**Files:**
- Create: `modules/site/src/main/scala/shadcnscalajs/site/ThemeConfig.scala`

**Interfaces:**
- Produces: `final case class ThemeConfig(stylePack: String, darkMode: Boolean, baseColor: String, themeColor: String, chartColor: String, headingFont: String, bodyFont: String, iconLibrary: String, radius: String, menuColor: String, menuAccent: String)` with all-defaults `ThemeConfig.default`; `ThemeConfig.load(): ThemeConfig`; `ThemeConfig.store(cfg: ThemeConfig): Unit`; `ThemeConfig.applyToDocument(cfg: ThemeConfig): Unit`.

- [ ] **Step 1: Write `ThemeConfig.scala`**

```scala
package shadcnscalajs.site

import org.scalajs.dom

import scala.scalajs.js
import scala.util.Try

/** Site-wide theme/customizer state, persisted to `localStorage` so it survives real browser navigations — this site
  * has no client-side router, so `Main.main()` re-runs from scratch on every page load with fresh `Var`s. Every
  * page's mount function seeds a `Var[ThemeConfig]` from `load()` and applies it via `applyToDocument`; any control
  * that changes a field calls `store` so the next page load picks it up. See
  * docs/superpowers/specs/2026-08-06-create-theme-customizer-design.md for the full design.
  */
final case class ThemeConfig(
    stylePack: String = "lyra",
    darkMode: Boolean = false,
    baseColor: String = "neutral",
    themeColor: String = "orange",
    chartColor: String = "orange",
    headingFont: String = "default",
    bodyFont: String = "default",
    iconLibrary: String = "lucide",
    radius: String = "default",
    menuColor: String = "default",
    menuAccent: String = "subtle"
)

object ThemeConfig:

  private val storageKey = "shadcn-scalajs:theme"

  val default: ThemeConfig = ThemeConfig()

  /** Reads the persisted config, falling back to `default` on a missing key, a JSON parse error, or a corrupt shape
    * — a user editing localStorage by hand (or an old, differently-shaped value from before a field was added) must
    * never crash page load.
    */
  def load(): ThemeConfig =
    Try {
      val raw = dom.window.localStorage.getItem(storageKey)
      if raw == null then default
      else
        val parsed = js.JSON.parse(raw).asInstanceOf[js.Dynamic]
        def str(field: String, fallback: String): String =
          parsed.selectDynamic(field).asInstanceOf[js.UndefOr[String]].getOrElse(fallback)
        def bool(field: String, fallback: Boolean): Boolean =
          parsed.selectDynamic(field).asInstanceOf[js.UndefOr[Boolean]].getOrElse(fallback)
        ThemeConfig(
          stylePack = str("stylePack", default.stylePack),
          darkMode = bool("darkMode", default.darkMode),
          baseColor = str("baseColor", default.baseColor),
          themeColor = str("themeColor", default.themeColor),
          chartColor = str("chartColor", default.chartColor),
          headingFont = str("headingFont", default.headingFont),
          bodyFont = str("bodyFont", default.bodyFont),
          iconLibrary = str("iconLibrary", default.iconLibrary),
          radius = str("radius", default.radius),
          menuColor = str("menuColor", default.menuColor),
          menuAccent = str("menuAccent", default.menuAccent)
        )
    }.getOrElse(default)

  def store(cfg: ThemeConfig): Unit =
    val literal = js.Dynamic.literal(
      stylePack = cfg.stylePack,
      darkMode = cfg.darkMode,
      baseColor = cfg.baseColor,
      themeColor = cfg.themeColor,
      chartColor = cfg.chartColor,
      headingFont = cfg.headingFont,
      bodyFont = cfg.bodyFont,
      iconLibrary = cfg.iconLibrary,
      radius = cfg.radius,
      menuColor = cfg.menuColor,
      menuAccent = cfg.menuAccent
    )
    dom.window.localStorage.setItem(storageKey, js.JSON.stringify(literal))

  /** Sets every `data-*` attribute `globals.css`'s attribute-selector blocks key off of, plus the `dark` class, on
    * `<html>` itself — not some inner div. `rem`-based Tailwind classes (used throughout this codebase) only ever
    * resolve against `<html>`'s own state, never an ancestor div's; putting `dark` here too (rather than on each
    * page's own root div, as today) means the `&:is(.dark *)` custom variant in globals.css covers the *entire*
    * document unconditionally, which is a strict superset of today's behavior, not a behavior change.
    */
  def applyToDocument(cfg: ThemeConfig): Unit =
    val html = dom.document.documentElement
    html.setAttribute("data-style-pack", cfg.stylePack)
    html.setAttribute("data-base-color", cfg.baseColor)
    html.setAttribute("data-theme-color", cfg.themeColor)
    html.setAttribute("data-chart-color", cfg.chartColor)
    html.setAttribute("data-heading-font", cfg.headingFont)
    html.setAttribute("data-body-font", cfg.bodyFont)
    html.setAttribute("data-icon-library", cfg.iconLibrary)
    html.setAttribute("data-radius", cfg.radius)
    if cfg.darkMode then html.classList.add("dark") else html.classList.remove("dark")
```

- [ ] **Step 2: Compile**

Run: `cd /Users/elam/Projects/shadcn-scalajs && export PATH="$PATH:$HOME/Library/Application Support/Coursier/bin" && sbt -batch "site/compile" scalafmtAll`
Expected: compiles clean, scalafmt makes no further changes on a second run.

- [ ] **Step 3: Verify persistence in a real browser**

From `modules/site`, write a temp script (e.g. `verify-themeconfig.mjs`) that starts the dev server, then uses `page.evaluate` to run (this exercises the compiled Scala.js output indirectly is not possible yet since nothing calls `ThemeConfig` — instead, verify the *mechanism* directly via the browser's own `localStorage`/`document.documentElement` APIs, which is exactly what the Scala code wraps):

```js
await page.goto("http://localhost:4300/");
const result = await page.evaluate(() => {
  localStorage.setItem("shadcn-scalajs:theme", JSON.stringify({ stylePack: "luma", darkMode: true }));
  const raw = localStorage.getItem("shadcn-scalajs:theme");
  return { stored: raw, parsedStylePack: JSON.parse(raw).stylePack };
});
console.log(JSON.stringify(result));
```

Expected: `parsedStylePack` is `"luma"` — confirms the storage key name and JSON shape this task's `ThemeConfig.store`/`load` use round-trip correctly (full end-to-end proof that `Main.main()` actually reads this value arrives in Task 3, once a page calls `load()`). Delete the script, kill the dev server.

- [ ] **Step 4: Commit**

```bash
cd /Users/elam/Projects/shadcn-scalajs
git add modules/site/src/main/scala/shadcnscalajs/site/ThemeConfig.scala
git commit -m "Add ThemeConfig: site-wide, localStorage-persisted customizer state

Co-Authored-By: Claude Sonnet 5 <noreply@anthropic.com>"
```

---

### Task 2: `globals.css` attribute-selector blocks for the new theme fields

**Files:**
- Modify: `modules/site/src/styles/globals.css`

**Interfaces:**
- Consumes: nothing from other tasks (pure CSS, keyed on the `data-*` attribute names `ThemeConfig.applyToDocument` (Task 1) sets — `data-base-color`, `data-theme-color`, `data-chart-color`, `data-heading-font`, `data-body-font`, `data-radius`).
- Produces: the CSS attribute-selector blocks Task 3's pages and Task 8's `CreatePage` rely on for visible effect.

- [ ] **Step 1: Add the base-color blocks**

Find the closing `}` of the existing `[data-style-pack] { ... }` block (search for `--style-control-height: 2.25rem;`) and insert immediately after it (before the first `[data-style-pack="lyra"]` block):

```css
[data-base-color="neutral"] {
  --background: #ffffff;
  --foreground: #0a0a0a;
  --card: #ffffff;
  --card-foreground: #0a0a0a;
  --border: #e5e5e5;
  --input: #e5e5e5;
  --muted: #f5f5f5;
  --muted-foreground: #737373;
}

[data-base-color="gray"] {
  --background: #ffffff;
  --foreground: #101828;
  --card: #ffffff;
  --card-foreground: #101828;
  --border: #e5e7eb;
  --input: #e5e7eb;
  --muted: #f9fafb;
  --muted-foreground: #6a7282;
}

[data-base-color="zinc"] {
  --background: #ffffff;
  --foreground: #18181b;
  --card: #ffffff;
  --card-foreground: #18181b;
  --border: #e4e4e7;
  --input: #e4e4e7;
  --muted: #f4f4f5;
  --muted-foreground: #71717b;
}

[data-base-color="stone"] {
  --background: #ffffff;
  --foreground: #1c1917;
  --card: #ffffff;
  --card-foreground: #1c1917;
  --border: #e7e5e4;
  --input: #e7e5e4;
  --muted: #f5f5f4;
  --muted-foreground: #79716b;
}

[data-base-color="slate"] {
  --background: #ffffff;
  --foreground: #0f172b;
  --card: #ffffff;
  --card-foreground: #0f172b;
  --border: #e2e8f0;
  --input: #e2e8f0;
  --muted: #f8fafc;
  --muted-foreground: #62748e;
}
```

- [ ] **Step 2: Add the theme-color and chart-color blocks**

Immediately after the base-color blocks from Step 1, add one `[data-theme-color="..."]` block per color, each setting `--primary`, `--primary-foreground` (always `#ffffff` — every one of these accent hues is dark enough at its 500 shade for white text to pass contrast), and `--ring` (same hex as `--primary`). Two fully worked examples, then repeat the *exact same 3-property pattern* for the rest using the hex values given — this is a template, not 17 different structures:

```css
[data-theme-color="red"] {
  --primary: #ef4444;
  --primary-foreground: #ffffff;
  --ring: #ef4444;
}

[data-theme-color="orange"] {
  --primary: #f97316;
  --primary-foreground: #ffffff;
  --ring: #f97316;
}
```

Repeat that same block shape for the remaining 15 colors, using these exact hex values: `amber` `#f59e0b`, `yellow` `#eab308`, `lime` `#84cc16`, `green` `#22c55e`, `emerald` `#10b981`, `teal` `#14b8a6`, `cyan` `#06b6d4`, `sky` `#0ea5e9`, `blue` `#3b82f6`, `indigo` `#6366f1`, `violet` `#8b5cf6`, `purple` `#a855f7`, `fuchsia` `#d946ef`, `pink` `#ec4899`, `rose` `#f43f5e`.

Then add the chart-color blocks the same way, but setting `--chart-1` through `--chart-5` to 5 shades of the *same* hue (the exact same hex for `--chart-1`, then progressively 15% lighter for each subsequent one is fine — e.g. use a CSS `color-mix()` so you don't have to hand-compute shades):

```css
[data-chart-color="orange"] {
  --chart-1: #f97316;
  --chart-2: color-mix(in oklab, #f97316 80%, white);
  --chart-3: color-mix(in oklab, #f97316 60%, white);
  --chart-4: color-mix(in oklab, #f97316 40%, white);
  --chart-5: color-mix(in oklab, #f97316 20%, white);
}
```

Repeat that same `color-mix` pattern for all 17 colors using the same hex values from the `--theme-color` step above (`[data-chart-color="red"]` uses `#ef4444` as its base, etc.).

- [ ] **Step 3: Add the heading/body font blocks**

Add after the color blocks:

```css
[data-heading-font="inter"] {
  --font-heading-token: "Inter Variable", "Inter", ui-sans-serif, system-ui, sans-serif;
}
[data-heading-font="geist"] {
  --font-heading-token: "Geist", ui-sans-serif, system-ui, sans-serif;
}
[data-heading-font="dm-sans"] {
  --font-heading-token: "DM Sans", ui-sans-serif, system-ui, sans-serif;
}
[data-body-font="inter"] {
  --font-body: "Inter Variable", "Inter", ui-sans-serif, system-ui, sans-serif;
}
[data-body-font="geist"] {
  --font-body: "Geist", ui-sans-serif, system-ui, sans-serif;
}
[data-body-font="dm-sans"] {
  --font-body: "DM Sans", ui-sans-serif, system-ui, sans-serif;
}
```

Note there is deliberately no `[data-heading-font="default"]`/`[data-body-font="default"]` block — that value must set nothing, so the active style pack's own font (or the site base Inter Variable) shows through untouched, per the spec's precedence rule.

- [ ] **Step 4: Add the radius blocks**

```css
[data-radius="none"] {
  --radius: 0rem;
  --style-card-radius: 0rem;
  --style-control-radius: 0rem;
}
[data-radius="small"] {
  --radius: 0.25rem;
  --style-card-radius: 0.375rem;
  --style-control-radius: 0.25rem;
}
[data-radius="medium"] {
  --radius: 0.5rem;
  --style-card-radius: 0.625rem;
  --style-control-radius: 0.375rem;
}
[data-radius="large"] {
  --radius: 1rem;
  --style-card-radius: 1.25rem;
  --style-control-radius: 0.875rem;
}
```

No `[data-radius="default"]` block, for the same reason as fonts above — it must defer to the active style pack's own radius values.

- [ ] **Step 5: Compile CSS and spot-check in a browser**

From `modules/site`: `npm run dev > /tmp/css-task-dev.log 2>&1 &`, poll until `200`, then write a temp script that navigates to `http://localhost:4300/` and runs:

```js
await page.evaluate(() => {
  document.documentElement.setAttribute("data-theme-color", "violet");
  document.documentElement.setAttribute("data-radius", "none");
});
const primary = await page.evaluate(() => getComputedStyle(document.documentElement).getPropertyValue("--primary").trim());
console.log(JSON.stringify({ primary }));
```

Expected: `primary` is `#8b5cf6` (violet's hex) — confirms the CSS block is actually loaded and matches. Delete the script, kill the dev server.

- [ ] **Step 6: Commit**

```bash
cd /Users/elam/Projects/shadcn-scalajs
git add modules/site/src/styles/globals.css
git commit -m "Add base-color/theme-color/chart-color/font/radius CSS blocks for the theme customizer

Co-Authored-By: Claude Sonnet 5 <noreply@anthropic.com>"
```

---

### Task 3: Migrate the 4 existing header blocks to `ThemeConfig`

**Files:**
- Modify: `modules/site/src/main/scala/shadcnscalajs/site/Main.scala` (3 places: `app()` around line 244-262, `componentsGalleryPage()` around line 1078-1093, `componentDocsPage()` around line 1157/1936-1945 — exact line numbers will have shifted from earlier edits this session; search for `val stylePack = Var(` to find all 3, and `val darkMode = Var(false)` immediately above each)
- Modify: `modules/site/src/main/scala/shadcnscalajs/site/BlocksLayout.scala`

**Interfaces:**
- Consumes: `ThemeConfig`, `ThemeConfig.load()`, `ThemeConfig.store(cfg)`, `ThemeConfig.applyToDocument(cfg)` from Task 1.
- Produces: every page now seeds and applies a shared `themeConfig: Var[ThemeConfig]`, which Task 8's `CreatePage` and Task 9's routing rely on existing everywhere.

- [ ] **Step 1: Replace `Main.scala`'s 3 `darkMode`/`stylePack` pairs**

In each of the 3 places, replace:
```scala
val darkMode = Var(false)
val stylePack = Var("lyra")
```
with:
```scala
val themeConfig = Var(ThemeConfig.load())
```

Then find each place's root `div(...)` that currently has:
```scala
cls <-- darkMode.signal.map(if _ then "dark" else ""),
stylePackA <-- stylePack.signal,
syncHtmlStylePack(stylePack),
```
(the exact 3 lines vary slightly per call site — some have `syncHtmlStylePack` on its own line, some inline; search for `stylePackA <-- stylePack.signal` to find all 3) and replace with a single reactive binding:
```scala
themeConfig.signal --> { cfg => ThemeConfig.applyToDocument(cfg) },
```
Remove the `cls <-- darkMode.signal...` line entirely (dark-mode is now applied to `<html>` by `applyToDocument`, not the page's own root div — see Task 1's comment on why this is a strict superset of the old behavior, not a regression) and remove the now-unused `stylePackA`/`syncHtmlStylePack` definitions from `Main.scala` entirely (search for `val stylePackA = htmlAttr` and `def syncHtmlStylePack` — delete both, they're fully superseded by `ThemeConfig.applyToDocument`).

Then find each place's style-pack `<select>` (search for `aria.label := "Style pack"`, 3 occurrences) and its dark-mode toggle `button` (search for `aria.label := "Toggle dark mode"`, 3 occurrences), updating their handlers:
```scala
select(
  cls := "hidden sm:block h-8 w-28 shrink-0 rounded-md border border-input bg-background px-2 text-sm",
  aria.label := "Style pack",
  value <-- themeConfig.signal.map(_.stylePack),
  onChange --> { ev =>
    val next = themeConfig.now().copy(stylePack = ev.target.asInstanceOf[dom.html.Select].value)
    themeConfig.set(next)
    ThemeConfig.store(next)
  },
  option(value := "vega", "Vega"),
  option(value := "nova", "Nova"),
  option(value := "maia", "Maia"),
  option(value := "lyra", "Lyra"),
  option(value := "mira", "Mira"),
  option(value := "luma", "Luma"),
  option(value := "sera", "Sera"),
  option(value := "rhea", "Rhea")
),
```
```scala
button(
  typ := "button",
  cls := s"$btnIcon hidden sm:inline-flex",
  aria.label := "Toggle dark mode",
  onClick --> { _ =>
    val next = themeConfig.now().copy(darkMode = !themeConfig.now().darkMode)
    themeConfig.set(next)
    ThemeConfig.store(next)
  },
  span(cls := "hidden dark:block", rawIcon(iconSun)),
  span(cls := "block dark:hidden", rawIcon(iconMoon))
),
```
(keep whatever the existing icon-rendering call was for each of the 3 — `rawIcon(iconSun)`/`rawIcon(iconMoon)` is `app()`'s pattern; the other two call sites may use a slightly different existing helper name for the sun/moon icons — preserve whichever one is already there, only the `onClick` handler body changes.)

- [ ] **Step 2: Migrate `BlocksLayout.scala` the same way**

Read the current file first (it has its own `darkMode`/`stylePack` pair added earlier this session, plus its own copy of the style-pack `<select>` and dark-mode toggle `button`, structured identically to `Main.scala`'s 3 copies). Apply the exact same transformation as Step 1: replace the two `Var`s with `val themeConfig = Var(ThemeConfig.load())`, replace `Main.stylePackA <-- stylePack.signal` / `Main.syncHtmlStylePack(stylePack)` with `themeConfig.signal --> { cfg => ThemeConfig.applyToDocument(cfg) },`, remove the `cls <-- darkMode.signal...` line, and update the `<select>`/dark-mode `button` handlers to `themeConfig.update`/`ThemeConfig.store` the same way.

- [ ] **Step 3: Compile**

Run: `cd /Users/elam/Projects/shadcn-scalajs && export PATH="$PATH:$HOME/Library/Application Support/Coursier/bin" && sbt -batch "site/compile" "site/fastLinkJS" scalafmtAll`
Expected: compiles clean. If it fails on a removed `stylePackA`/`syncHtmlStylePack` reference, grep `modules/site/src/main/scala/shadcnscalajs/site/*.scala` for `stylePackA`/`syncHtmlStylePack` and fix every remaining call site (there should be none left after Steps 1-2).

- [ ] **Step 4: Verify site-wide persistence across a real navigation**

From `modules/site`: start the dev server, then a temp script:

```js
await page.goto("http://localhost:4300/", { waitUntil: "networkidle0" });
await page.select('select[aria-label="Style pack"]', "luma");
await new Promise((r) => setTimeout(r, 150));
await page.goto("http://localhost:4300/components", { waitUntil: "networkidle0" }); // a REAL navigation, not an SPA route change
const value = await page.$eval('select[aria-label="Style pack"]', (el) => el.value);
const htmlAttr = await page.evaluate(() => document.documentElement.getAttribute("data-style-pack"));
console.log(JSON.stringify({ value, htmlAttr }));
```

Expected: both `value` and `htmlAttr` are `"luma"` on the `/components` page — proves the choice persisted across an actual full-page navigation, which is the entire point of this task. Also repeat this same check for `/blocks` (verifies `BlocksLayout`'s independent copy reads the same key). Delete the script, kill the dev server.

- [ ] **Step 5: Commit**

```bash
cd /Users/elam/Projects/shadcn-scalajs
git add modules/site/src/main/scala/shadcnscalajs/site/Main.scala modules/site/src/main/scala/shadcnscalajs/site/BlocksLayout.scala
git commit -m "Migrate all 4 header blocks to the shared, persisted ThemeConfig

Co-Authored-By: Claude Sonnet 5 <noreply@anthropic.com>"
```

---

### Task 4: `Icons.scala` module with Lucide backing

**Files:**
- Create: `modules/ui/src/main/scala/shadcnscalajs/ui/Icons.scala`

**Interfaces:**
- Produces: `Icons.activeLibrary: Signal[String]` (reads `document.documentElement`'s `data-icon-library` attribute reactively via a `MutationObserver`, same technique `ScElementBase.observeAttribute` already uses); `Icons.chevronDown(mods: Modifier[SvgElement]*): HtmlElement`, `Icons.chevronsUpDown(mods: Modifier[SvgElement]*): HtmlElement`, `Icons.check(mods: Modifier[SvgElement]*): HtmlElement`, `Icons.x(mods: Modifier[SvgElement]*): HtmlElement` — each renders reactively, swapping its path data when `activeLibrary` changes.

- [ ] **Step 1: Write `Icons.scala`'s structure + Lucide backing**

This centralizes path data that already exists today in `Accordion.scala`/`Combobox.scala` — Task 6 removes it from there once this compiles. Note each icon is defined as a reactive `HtmlElement` wrapper (a `div` sized to `contents` via `display: contents` so it doesn't add an extra box) around a `child <--` binding that swaps the actual `<svg>`, rather than a plain `SvgElement`, because the icon must re-render when the library changes while mounted:

```scala
package shadcnscalajs.ui

import com.raquo.laminar.api.L.*
import com.raquo.laminar.api.L.svg.{path as svgPath, svg as svgTag}
import org.scalajs.dom

import scala.scalajs.js

/** Swappable icon registry backing the `iconLibrary` field of `shadcnscalajs.site.ThemeConfig` — resolves each named
  * icon to Lucide or Hugeicons path data based on `<html data-icon-library="...">`, which `ThemeConfig.applyToDocument`
  * sets. Only covers icons real `modules/ui` components and blocks actually use today (see
  * docs/superpowers/specs/2026-08-06-create-theme-customizer-design.md's Icon abstraction section for what's
  * deliberately out of scope).
  */
object Icons:

  /** Mirrors the `data-icon-library` attribute on `<html>` reactively, the same MutationObserver technique
    * `ScElementBase.observeAttribute` already uses for custom-element attributes.
    */
  val activeLibrary: Signal[String] =
    val initial = Option(dom.document.documentElement.getAttribute("data-icon-library")).getOrElse("lucide")
    val libraryVar = Var(initial)
    val observer = new dom.MutationObserver((records, _) =>
      records.foreach { record =>
        if record.attributeName == "data-icon-library" then
          libraryVar.set(Option(dom.document.documentElement.getAttribute("data-icon-library")).getOrElse("lucide"))
      }
    )
    observer.observe(
      dom.document.documentElement,
      new dom.MutationObserverInit { attributes = true; attributeFilter = js.Array("data-icon-library") }
    )
    libraryVar.signal

  private def base(mods: Modifier[SvgElement]*)(paths: (String => SvgElement)*)(pathData: Seq[String]): SvgElement =
    svgTag(
      svg.viewBox := "0 0 24 24",
      svg.fill := "none",
      svg.stroke := "currentColor",
      svg.strokeWidth := "2",
      svg.strokeLineCap := "round",
      svg.strokeLineJoin := "round",
      svg.cls := "size-4",
      aria.hidden := true,
      pathData.map(d => svgPath(svg.d := d)),
      mods
    )

  private def reactive(lucide: Seq[String], hugeicons: Seq[String])(mods: Modifier[SvgElement]*): HtmlElement =
    div(
      cls := "inline-contents",
      child <-- activeLibrary.map { lib =>
        val data = if lib == "hugeicons" then hugeicons else lucide
        base(mods*)()(data)
      }
    )

  def chevronDown(mods: Modifier[SvgElement]*): HtmlElement =
    reactive(lucide = Seq("m6 9 6 6 6-6"), hugeicons = Seq("m6 9 6 6 6-6"))(mods*)

  def chevronsUpDown(mods: Modifier[SvgElement]*): HtmlElement =
    reactive(lucide = Seq("m7 15 5 5 5-5", "m7 9 5-5 5 5"), hugeicons = Seq("m7 15 5 5 5-5", "m7 9 5-5 5 5"))(mods*)

  def check(mods: Modifier[SvgElement]*): HtmlElement =
    reactive(lucide = Seq("M20 6 9 17l-5-5"), hugeicons = Seq("M20 6 9 17l-5-5"))(mods*)

  def x(mods: Modifier[SvgElement]*): HtmlElement =
    reactive(lucide = Seq("M18 6 6 18", "m6 6 12 12"), hugeicons = Seq("M18 6 6 18", "m6 6 12 12"))(mods*)
```

Note the `hugeicons` path data here is a **placeholder duplicate of the Lucide data** — Task 5 replaces every `hugeicons = Seq(...)` argument with real, sourced Hugeicons path data. This task's job is only to get the reactive-swap *mechanism* compiling and correctly wired to `data-icon-library`; do not skip Task 5.

Also add this one-line utility class to `globals.css` (search for `@utility no-scrollbar` and add a sibling `@utility` block right after it), needed because `div`s don't support `display: contents` via a plain Tailwind class name in this version:

```css
@utility inline-contents {
  display: contents;
}
```

- [ ] **Step 2: Compile**

Run: `cd /Users/elam/Projects/shadcn-scalajs && export PATH="$PATH:$HOME/Library/Application Support/Coursier/bin" && sbt -batch "ui/compile" scalafmtAll`
Expected: compiles clean.

- [ ] **Step 3: Verify the reactive swap mechanism in a browser**

`Icons.scala` isn't called from anywhere yet (Task 6 wires it up), so this step verifies the mechanism using the components module's own build output isn't possible in isolation — defer full verification to Task 6's Step 3, which exercises this through `Combobox`. Skip a standalone browser check here; proceed to commit.

- [ ] **Step 4: Commit**

```bash
cd /Users/elam/Projects/shadcn-scalajs
git add modules/ui/src/main/scala/shadcnscalajs/ui/Icons.scala modules/site/src/styles/globals.css
git commit -m "Add Icons module: swappable icon registry (Lucide backing, mechanism only)

Co-Authored-By: Claude Sonnet 5 <noreply@anthropic.com>"
```

---

### Task 5: Source real Hugeicons path data

**Files:**
- Modify: `modules/ui/src/main/scala/shadcnscalajs/ui/Icons.scala`

**Interfaces:**
- Consumes: `Icons.scala`'s structure from Task 4 — only the 4 `hugeicons = Seq(...)` arguments change; every other signature stays identical.

- [ ] **Step 1: Fetch the real Hugeicons free icon set**

From a scratch directory (e.g. `/tmp/hugeicons-audit`, not inside this repo): `npm init -y && npm install @hugeicons/core-free-icons`. This is Hugeicons' official free, MIT-licensed npm package. Find the 4 needed icons' source files — they're plain JS/TS files exporting an array of `["path", { d: "..." }]`-shaped tuples per path, one file per icon, named after the icon (e.g. search `node_modules/@hugeicons/core-free-icons` for files matching `*ArrowDown01*` or similar for a chevron-down equivalent, `*ArrowUpDown*` for chevrons-up-down, `*Tick*`/`*Check*` for a checkmark, `*Cancel01*`/`*Close*` for an X). Hugeicons' free set uses its own icon-naming scheme, not Lucide's — read each candidate file's exported path data directly rather than guessing from the filename alone, and pick whichever named icon's actual rendered shape (viewBox + path geometry) most closely matches the Lucide icon it's replacing (rounded stroke style, similar visual weight).

- [ ] **Step 2: Transcribe the 4 icons' exact path data into `Icons.scala`**

Hugeicons' free set ships at a `24x24` viewBox like Lucide, but check each file's actual `d` attribute string(s) and viewBox before assuming they match Lucide's exactly — Hugeicons icons often use multiple `<path>` elements with `fill` rather than `stroke` for some shapes; if a fetched icon uses `fill="currentColor"` instead of a stroked outline, note that `Icons.base`'s current implementation hard-codes `svg.fill := "none"` / `svg.stroke := "currentColor"` for ALL icons uniformly (Task 4's implementation) — if the sourced Hugeicons data genuinely needs `fill` instead of `stroke` to render correctly, extend `base`/`reactive` to accept a per-icon `usesFill: Boolean` flag (default `false`) rather than forcing every Hugeicons icon into a stroke-only rendering model it wasn't designed for. Replace each of the 4 `hugeicons = Seq(...)` placeholder arguments in `Icons.scala` with the real, sourced path data (and adjust `usesFill` per icon if needed).

- [ ] **Step 3: Compile**

Run: `cd /Users/elam/Projects/shadcn-scalajs && export PATH="$PATH:$HOME/Library/Application Support/Coursier/bin" && sbt -batch "ui/compile" scalafmtAll`
Expected: compiles clean.

- [ ] **Step 4: Visual verification deferred to Task 6**

Same reasoning as Task 4 Step 3 — nothing calls `Icons.xxx()` yet. Task 6's Step 3 verifies both Lucide and Hugeicons render correctly (and look visually distinct) via `Combobox`'s check icon.

- [ ] **Step 5: Commit**

```bash
cd /Users/elam/Projects/shadcn-scalajs
git add modules/ui/src/main/scala/shadcnscalajs/ui/Icons.scala
git commit -m "Source real Hugeicons path data for the icon registry

Co-Authored-By: Claude Sonnet 5 <noreply@anthropic.com>"
```

---

### Task 6: Migrate `Accordion.scala` + `Combobox.scala` to `Icons.xxx()`

**Files:**
- Modify: `modules/ui/src/main/scala/shadcnscalajs/ui/Accordion.scala`
- Modify: `modules/ui/src/main/scala/shadcnscalajs/ui/Combobox.scala`

**Interfaces:**
- Consumes: `Icons.chevronDown`, `Icons.chevronsUpDown`, `Icons.check`, `Icons.x` from Tasks 4-5.

- [ ] **Step 1: `Accordion.scala`**

Find the private `chevronDown(): SvgElement` function (currently near the bottom of the file, called once from `trigger`'s body) and its call site `chevronDown()`. Delete the private function entirely and change the call site to `Icons.chevronDown()`. Since `Icons.chevronDown` returns `HtmlElement` now (not `SvgElement`), and it's used as a child alongside other `Modifier[HtmlElement]` content in `summaryTag(...)`, no signature adjustment is needed at the call site beyond the rename — `HtmlElement` is a valid child anywhere `SvgElement` was.

- [ ] **Step 2: `Combobox.scala`**

Find and delete these 3 private functions entirely: `chevronsUpDown(): SvgElement`, `checkIcon(mods: Modifier[SvgElement]*): SvgElement`, `removeIcon(): SvgElement`. Update their call sites:
- `chevronsUpDown()` → `Icons.chevronsUpDown()` (in the `trigger`/`triggerMultiple` functions).
- `checkIcon(svg.cls := ...)` → this one is trickier: the existing call site passes `svg.cls := (if isSelected then "size-4 ml-auto opacity-100" else "size-4 ml-auto opacity-0")` as a `Modifier[SvgElement]`, which `Icons.check`'s signature still accepts (`mods: Modifier[SvgElement]*`) — no change needed to the call site's arguments, just rename `checkIcon` to `Icons.check`.
- `removeIcon()` → `Icons.x()` (used in the chip's remove button in `triggerMultiple`).

- [ ] **Step 3: Compile and verify both icon libraries render correctly**

Run: `cd /Users/elam/Projects/shadcn-scalajs && export PATH="$PATH:$HOME/Library/Application Support/Coursier/bin" && sbt -batch "ui/compile" "ui/fastLinkJS" "site/compile" "site/fastLinkJS" scalafmtAll`
Expected: compiles clean.

From `modules/site`: start the dev server, then a temp script that navigates to `http://localhost:4300/components/combobox`, opens the multi-select combobox, selects one item, and checks the check-icon's rendered size and path:

```js
await page.goto("http://localhost:4300/components/combobox", { waitUntil: "networkidle0" });
const triggers = await page.$$(".cn-combobox-trigger");
await triggers[1].click();
await new Promise((r) => setTimeout(r, 150));
await page.evaluate(() => {
  const multi = document.querySelectorAll(".cn-combobox")[1];
  Array.from(multi.querySelectorAll('[role="option"]')).find((el) => el.textContent.includes("Astro")).click();
});
await new Promise((r) => setTimeout(r, 100));
const lucideRect = await page.evaluate(() => {
  const multi = document.querySelectorAll(".cn-combobox")[1];
  const svg = multi.querySelector('[role="option"][aria-selected="true"] svg');
  const r = svg.getBoundingClientRect();
  return { w: r.width, h: r.height };
});
await page.evaluate(() => document.documentElement.setAttribute("data-icon-library", "hugeicons"));
await new Promise((r) => setTimeout(r, 100));
const hugeiconsPath = await page.evaluate(() => {
  const multi = document.querySelectorAll(".cn-combobox")[1];
  return multi.querySelector('[role="option"][aria-selected="true"] svg path')?.getAttribute("d");
});
console.log(JSON.stringify({ lucideRect, hugeiconsPath }));
```

Expected: `lucideRect` is `{ w: 16, h: 16 }` (confirms the sizing-bug lesson from earlier this session hasn't regressed) and `hugeiconsPath` is a real `d` attribute string (not `undefined`, not identical to Lucide's `"M20 6 9 17l-5-5"` unless Task 5's sourcing genuinely happened to match — the point is it changed and rendered, not that it must differ character-for-character). Delete the script, kill the dev server.

- [ ] **Step 4: Commit**

```bash
cd /Users/elam/Projects/shadcn-scalajs
git add modules/ui/src/main/scala/shadcnscalajs/ui/Accordion.scala modules/ui/src/main/scala/shadcnscalajs/ui/Combobox.scala
git commit -m "Migrate Accordion and Combobox icons to the swappable Icons registry

Co-Authored-By: Claude Sonnet 5 <noreply@anthropic.com>"
```

---

### Task 7: `Dashboard01` block

**Files:**
- Create: `modules/blocks/src/main/scala/shadcnscalajs/blocks/dashboard01/Dashboard01.scala`
- Create: `modules/blocks/src/main/scala/shadcnscalajs/blocks/dashboard01/dashboard-01.registry.json`
- Modify: `modules/site/src/main/scala/shadcnscalajs/site/Blocks.scala`

**Interfaces:**
- Consumes: `Card`, `Table`, `Progress`, `Chart`, `Badge`, `Field`, `Button`, `Input`, `Icons` (all `modules/ui`, all pre-existing except `Icons`).
- Produces: `Dashboard01.apply(): HtmlElement`, registered in `Blocks.all`/`Blocks.render` as `"dashboard-01"`.

- [ ] **Step 1: Write `Dashboard01.scala`**

An original (not a copy of any specific reference layout's cards/copy/dataset) analytics dashboard: a sidebar nav (what `menuColor`/`menuAccent` style — implemented as plain Tailwind classes driven by `data-menu-color`/`data-menu-accent` attributes Task 9 wires up, not yet in this task) plus 3 content cards exercising color/radius/font/icon knobs.

```scala
package shadcnscalajs.blocks.dashboard01

import com.raquo.laminar.api.L.*
import shadcnscalajs.ui.*

/** An original analytics-dashboard composition — not a copy of any specific reference layout's cards, copy, or
  * dataset — built to exercise every `shadcnscalajs.site.ThemeConfig` knob (colors, radius, fonts, icon library) as
  * the live preview on `/create`. See docs/superpowers/specs/2026-08-06-create-theme-customizer-design.md.
  */
object Dashboard01:

  private final case class NavItem(label: String, active: Boolean)
  private val navItems = List(
    NavItem("Overview", active = true),
    NavItem("Transactions", active = false),
    NavItem("Reports", active = false),
    NavItem("Settings", active = false)
  )

  private final case class Transaction(name: String, category: String, amount: String, status: String)
  private val transactions = List(
    Transaction("Coffee Roasters Co.", "Food & Drink", "-$4.50", "Completed"),
    Transaction("Cloud Hosting Inc.", "Software", "-$29.00", "Completed"),
    Transaction("Client Payment", "Income", "+$1,200.00", "Completed"),
    Transaction("Design Subscription", "Software", "-$12.00", "Pending")
  )

  def apply(): HtmlElement =
    div(
      cls := "cn-dashboard-01 flex min-h-svh w-full bg-muted/30",
      aside(
        cls := "hidden w-56 shrink-0 border-r bg-card md:block",
        div(cls := "flex h-14 items-center gap-2 border-b px-4 text-sm font-semibold", "Acme Analytics"),
        navTag(
          cls := "flex flex-col gap-1 p-3",
          navItems.map { item =>
            a(
              href := "#",
              cls := s"flex items-center gap-2 rounded-md px-3 py-2 text-sm ${
                  if item.active then "bg-accent text-accent-foreground" else "text-muted-foreground hover:bg-accent hover:text-accent-foreground"
                }",
              item.label
            )
          }
        )
      ),
      main(
        cls := "flex-1 p-6",
        div(
          cls := "mb-6 flex items-center justify-between",
          h1(cls := "font-heading text-2xl font-semibold", "Overview"),
          Button.of(_.variant(Button.Variant.Outline), _.size(Button.Size.Sm), _ => "Export")
        ),
        div(
          cls := "grid gap-4 md:grid-cols-3",
          Card(
            cls := "md:col-span-2",
            Card.header(Card.title("Weekly Activity"), Card.description("Sessions over the last 7 days")),
            Card.content(
              Chart(
                div(
                  cls := "flex h-full w-full items-end gap-3 px-2",
                  List(38, 62, 45, 80, 55, 90, 70).map { pct =>
                    div(cls := "flex-1 rounded-t bg-primary", height := s"$pct%")
                  }
                )
              )
            )
          ),
          Card(
            Card.header(Card.title("Monthly Goal")),
            Card.content(
              div(cls := "mb-2 flex items-center justify-between text-sm", span("Revenue"), span(cls := "font-medium", "68%")),
              Progress(68, cls := "mb-4"),
              div(cls := "mb-2 flex items-center justify-between text-sm", span("New Customers"), span(cls := "font-medium", "42%")),
              Progress(42)
            )
          )
        ),
        div(
          cls := "mt-4 grid gap-4 md:grid-cols-3",
          Card(
            cls := "md:col-span-2",
            Card.header(Card.title("Recent Transactions")),
            Card.content(
              Table(
                Table.header(Table.row(Table.head("Name"), Table.head("Category"), Table.head("Amount"), Table.head("Status"))),
                Table.body(
                  transactions.map { t =>
                    Table.row(
                      Table.cell(t.name),
                      Table.cell(cls := "text-muted-foreground", t.category),
                      Table.cell(t.amount),
                      Table.cell(
                        Badge.of(
                          _.variant(if t.status == "Completed" then Badge.Variant.Secondary else Badge.Variant.Outline),
                          _ => t.status
                        )
                      )
                    )
                  }
                )
              )
            )
          ),
          Card(
            Card.header(Card.title("Quick Update")),
            Card.content(
              Field(Field.label("Display name"), Input(placeholder := "Ada Lovelace")),
              Field(cls := "mt-3", Field.label("Support email"), Input(placeholder := "team@example.com"))
            ),
            Card.footer(Button(cls := "w-full", "Save changes"))
          )
        )
      )
    )
```

- [ ] **Step 2: Write the registry sidecar**

```json
{
  "name": "dashboard-01",
  "title": "Dashboard 01",
  "type": "scala:block",
  "description": "An analytics dashboard with activity chart, goal progress, and a transactions table.",
  "categories": ["dashboard", "analytics"],
  "registryDependencies": ["card", "table", "progress", "chart", "badge", "field", "button", "input"],
  "scalaDependencies": [],
  "files": [
    { "path": "dashboard01/Dashboard01.scala", "type": "scala:page" }
  ]
}
```

- [ ] **Step 3: Register in `Blocks.scala`**

Add the import: `import shadcnscalajs.blocks.dashboard01.Dashboard01`. Add to `Blocks.all`'s list: `Meta("dashboard-01", "Dashboard 01", "An analytics dashboard with activity chart, goal progress, and a transactions table.", List("dashboard", "analytics"))`. Add to `Blocks.render`'s match: `case "dashboard-01" => Some(Dashboard01())`.

- [ ] **Step 4: Compile and verify visually**

Run: `cd /Users/elam/Projects/shadcn-scalajs && export PATH="$PATH:$HOME/Library/Application Support/Coursier/bin" && sbt -batch "core/compile" "ui/compile" "blocks/compile" "site/compile" "site/fastLinkJS" scalafmtAll`
Expected: compiles clean. If `Card`/`Table`/`Progress`/`Chart`/`Badge`/`Field`/`Button`/`Input`'s actual signatures differ from what's used above (this plan's authoring session confirmed their `def` signatures via direct grep, but re-verify against the current file contents before assuming — a signature could have shifted since this plan was written), fix the call sites to match the real signatures, not the other way around.

From `modules/site`: start the dev server, run `node scripts/build-registry.mjs`, navigate to `http://localhost:4300/blocks/dashboard-01`, and screenshot it (`page.screenshot({ path: ... })`) to confirm it renders a real layout with a sidebar, activity bars, progress bars, and a populated table — not a blank page or a Scala.js exception (check `page.on("pageerror", ...)` logs too). View the screenshot with the Read tool before calling this step done.

- [ ] **Step 5: Commit**

```bash
cd /Users/elam/Projects/shadcn-scalajs
node modules/site/scripts/build-registry.mjs 2>/dev/null || (cd modules/site && node scripts/build-registry.mjs)
git add modules/blocks/src/main/scala/shadcnscalajs/blocks/dashboard01/ modules/site/src/main/scala/shadcnscalajs/site/Blocks.scala modules/site/public/registry/
git commit -m "Add Dashboard 01 block: original analytics dashboard preview

Co-Authored-By: Claude Sonnet 5 <noreply@anthropic.com>"
```

---

### Task 8: `CreatePage.scala` — customizer sidebar + inline preview

**Files:**
- Create: `modules/site/src/main/scala/shadcnscalajs/site/CreatePage.scala`
- Modify: `modules/site/src/styles/globals.css` (menu-color/menu-accent blocks, scoped to `.cn-dashboard-01`)

**Interfaces:**
- Consumes: `ThemeConfig` (Task 1), `Dashboard01` (Task 7).
- Produces: `CreatePage.apply(): HtmlElement`, wired into routing in Task 9.

- [ ] **Step 1: Add the menu-color/menu-accent CSS, scoped to the dashboard block only**

Per the spec, these two fields only ever affect `Dashboard01`'s own sidebar — add, after the radius blocks from Task 2:

```css
.cn-dashboard-01[data-menu-color="inverted"] aside {
  background-color: var(--foreground);
  color: var(--background);
}
.cn-dashboard-01[data-menu-accent="solid"] aside nav a[class*="bg-accent"] {
  background-color: var(--primary);
  color: var(--primary-foreground);
}
```

- [ ] **Step 2: Write `CreatePage.scala`**

```scala
package shadcnscalajs.site

import com.raquo.laminar.api.L.*
import org.scalajs.dom
import shadcnscalajs.blocks.dashboard01.Dashboard01
import shadcnscalajs.ui.*

/** `/create` — the theme customizer: a sidebar of controls writing into a shared, persisted `ThemeConfig`, and
  * `Dashboard01` rendered live next to it (inline, not iframed, so it shares this page's own `Var` directly). See
  * docs/superpowers/specs/2026-08-06-create-theme-customizer-design.md.
  */
object CreatePage:

  private def selectField(
      label: String,
      options: List[(String, String)],
      currentValue: ThemeConfig => String,
      update: (ThemeConfig, String) => ThemeConfig,
      themeConfig: Var[ThemeConfig]
  ): HtmlElement =
    Field(
      Field.label(label),
      select(
        cls := "h-8 w-full rounded-md border border-input bg-background px-2 text-sm",
        value <-- themeConfig.signal.map(currentValue),
        onChange --> { ev =>
          val next = update(themeConfig.now(), ev.target.asInstanceOf[dom.html.Select].value)
          themeConfig.set(next)
          ThemeConfig.store(next)
        },
        options.map { case (v, label) => option(value := v, label) }
      )
    )

  private val stylePackOptions =
    List("vega" -> "Vega", "nova" -> "Nova", "maia" -> "Maia", "lyra" -> "Lyra", "mira" -> "Mira", "luma" -> "Luma", "sera" -> "Sera", "rhea" -> "Rhea")
  private val baseColorOptions = List("neutral" -> "Neutral", "gray" -> "Gray", "zinc" -> "Zinc", "stone" -> "Stone", "slate" -> "Slate")
  private val themeColorOptions = List(
    "red" -> "Red", "orange" -> "Orange", "amber" -> "Amber", "yellow" -> "Yellow", "lime" -> "Lime", "green" -> "Green",
    "emerald" -> "Emerald", "teal" -> "Teal", "cyan" -> "Cyan", "sky" -> "Sky", "blue" -> "Blue", "indigo" -> "Indigo",
    "violet" -> "Violet", "purple" -> "Purple", "fuchsia" -> "Fuchsia", "pink" -> "Pink", "rose" -> "Rose"
  )
  private val fontOptions = List("default" -> "Default", "inter" -> "Inter", "geist" -> "Geist", "dm-sans" -> "DM Sans")
  private val iconLibraryOptions = List("lucide" -> "Lucide", "hugeicons" -> "Hugeicons")
  private val radiusOptions = List("default" -> "Default", "none" -> "None", "small" -> "Small", "medium" -> "Medium", "large" -> "Large")
  private val menuColorOptions = List("default" -> "Default", "inverted" -> "Inverted")
  private val menuAccentOptions = List("subtle" -> "Subtle", "solid" -> "Solid")

  def apply(): HtmlElement =
    val themeConfig = Var(ThemeConfig.load())

    div(
      cls := "min-h-dvh bg-background text-foreground antialiased",
      themeConfig.signal --> { cfg => ThemeConfig.applyToDocument(cfg) },
      div(
        cls := "flex min-h-dvh",
        aside(
          cls := "w-72 shrink-0 border-r bg-card p-4",
          h2(cls := "mb-4 text-sm font-semibold", "Customize"),
          div(
            cls := "flex flex-col gap-3",
            selectField("Style", stylePackOptions, _.stylePack, (c, v) => c.copy(stylePack = v), themeConfig),
            selectField("Base Color", baseColorOptions, _.baseColor, (c, v) => c.copy(baseColor = v), themeConfig),
            selectField("Theme", themeColorOptions, _.themeColor, (c, v) => c.copy(themeColor = v), themeConfig),
            selectField("Chart Color", themeColorOptions, _.chartColor, (c, v) => c.copy(chartColor = v), themeConfig),
            selectField("Heading Font", fontOptions, _.headingFont, (c, v) => c.copy(headingFont = v), themeConfig),
            selectField("Font", fontOptions, _.bodyFont, (c, v) => c.copy(bodyFont = v), themeConfig),
            selectField("Icon Library", iconLibraryOptions, _.iconLibrary, (c, v) => c.copy(iconLibrary = v), themeConfig),
            selectField("Radius", radiusOptions, _.radius, (c, v) => c.copy(radius = v), themeConfig),
            selectField("Menu Color", menuColorOptions, _.menuColor, (c, v) => c.copy(menuColor = v), themeConfig),
            selectField("Menu Accent", menuAccentOptions, _.menuAccent, (c, v) => c.copy(menuAccent = v), themeConfig)
          )
        ),
        div(
          cls := "flex-1 overflow-auto",
          div(
            cls := "cn-dashboard-01-wrapper",
            dataAttr("menu-color") <-- themeConfig.signal.map(_.menuColor),
            dataAttr("menu-accent") <-- themeConfig.signal.map(_.menuAccent),
            Dashboard01()
          )
        )
      )
    )
```

Note: `Dashboard01()`'s own root `div` already carries the `cn-dashboard-01` class (Task 7); the `data-menu-color`/`data-menu-accent` attributes need to land on *that* element, not a wrapper around it, for Step 1's CSS selectors (`.cn-dashboard-01[data-menu-color="..."]`) to match. Adjust: either change `Dashboard01.apply()`'s signature to accept `mods: Modifier[HtmlElement]*` (matching this codebase's established pattern — see `Combobox.apply`'s trailing `mods` parameter) and pass `dataAttr("menu-color") <-- ...` directly into that, or restructure the CSS in Step 1 to target `.cn-dashboard-01-wrapper[data-menu-color="..."] .cn-dashboard-01`. Prefer the first option (add `mods: Modifier[HtmlElement]*` to `Dashboard01.apply` and thread it into its root `div`) since it matches how every other component/block in this codebase accepts caller-supplied modifiers — this requires revisiting Task 7's `Dashboard01.scala` to add that parameter; do so as part of this step, not as a separate task.

- [ ] **Step 3: Compile**

Run: `cd /Users/elam/Projects/shadcn-scalajs && export PATH="$PATH:$HOME/Library/Application Support/Coursier/bin" && sbt -batch "core/compile" "ui/compile" "blocks/compile" "site/compile" "site/fastLinkJS" scalafmtAll`
Expected: compiles clean.

- [ ] **Step 4: Verify visually (routing isn't wired yet — Task 9 does that)**

`CreatePage` has no route yet, so it can't be loaded via URL. Skip browser verification for this task in isolation; Task 9's Step 3 verifies the fully-wired page end to end (this mirrors Tasks 4-5's deferred-verification pattern for the same reason).

- [ ] **Step 5: Commit**

```bash
cd /Users/elam/Projects/shadcn-scalajs
git add modules/site/src/main/scala/shadcnscalajs/site/CreatePage.scala modules/site/src/styles/globals.css modules/blocks/src/main/scala/shadcnscalajs/blocks/dashboard01/Dashboard01.scala
git commit -m "Add CreatePage: customizer sidebar + inline Dashboard01 preview

Co-Authored-By: Claude Sonnet 5 <noreply@anthropic.com>"
```

---

### Task 9: Routing, nav link, and end-to-end verification

**Files:**
- Modify: `modules/site/src/main/scala/shadcnscalajs/site/Main.scala` (routing dispatch + 3 header nav blocks)
- Modify: `modules/site/src/main/scala/shadcnscalajs/site/BlocksLayout.scala` (4th header nav block)

**Interfaces:**
- Consumes: `CreatePage.apply()` from Task 8.

- [ ] **Step 1: Add the route**

In `Main.main()`'s dispatch chain (search for `else app()`), add a new branch before the final `else app()`:
```scala
else if pathname == "/create" || pathname == "/create/" then CreatePage()
```

- [ ] **Step 2: Add the nav link to all 4 header blocks**

In each of the 3 `Main.scala` header blocks and `BlocksLayout.scala`, find the existing `a(cls := btnGhost, href := "/blocks", "Blocks")` (or `Main.btnGhost`/`"Blocks"` in `BlocksLayout.scala`'s case) and add immediately after it: `a(cls := btnGhost, href := "/create", "Create")` (using whichever `btnGhost`/`Main.btnGhost` reference each file already uses for its neighboring nav links).

- [ ] **Step 3: Compile and verify the whole feature end-to-end**

Run: `cd /Users/elam/Projects/shadcn-scalajs && export PATH="$PATH:$HOME/Library/Application Support/Coursier/bin" && sbt -batch "core/compile" "ui/compile" "webcomponents/compile" "blocks/compile" "site/compile" "site/fastLinkJS" scalafmtCheckAll`
Expected: every module compiles clean, scalafmt reports no issues.

From `modules/site`: start the dev server, then a comprehensive temp verification script:

```js
await page.goto("http://localhost:4300/create", { waitUntil: "networkidle0" });
const initial = await page.evaluate(() => ({
  hasSelects: document.querySelectorAll("aside select").length,
  primaryBefore: getComputedStyle(document.documentElement).getPropertyValue("--primary").trim(),
}));
await page.select('aside select', "violet"); // Theme select — first <select> in the sidebar per CreatePage's field order
await new Promise((r) => setTimeout(r, 150));
const after = await page.evaluate(() => ({
  primaryAfter: getComputedStyle(document.documentElement).getPropertyValue("--primary").trim(),
  htmlAttr: document.documentElement.getAttribute("data-theme-color"),
}));
console.log(JSON.stringify({ initial, after }));
```

Expected: `hasSelects` is `10`, `htmlAttr` is `"violet"`, and `primaryAfter` differs from `primaryBefore` and equals violet's hex (`#8b5cf6`). Also screenshot the page (view with the Read tool) to visually confirm the sidebar and dashboard preview both render, and manually navigate to `/`, `/components`, `/blocks` in the same script to confirm the new "Create" nav link is present on all 4 (`page.$('a[href="/create"]')` should be non-null on each). Delete the script, kill the dev server.

- [ ] **Step 4: Commit**

```bash
cd /Users/elam/Projects/shadcn-scalajs
git add modules/site/src/main/scala/shadcnscalajs/site/Main.scala modules/site/src/main/scala/shadcnscalajs/site/BlocksLayout.scala
git commit -m "Wire up /create routing and add the Create nav link site-wide

Co-Authored-By: Claude Sonnet 5 <noreply@anthropic.com>"
```

---

### Task 10: Final full-workspace verification

**Files:** none (verification only).

- [ ] **Step 1: Full compile + format check**

Run: `cd /Users/elam/Projects/shadcn-scalajs && export PATH="$PATH:$HOME/Library/Application Support/Coursier/bin" && sbt -batch "core/compile" "ui/compile" "webcomponents/compile" "blocks/compile" "site/compile" scalafmtCheckAll`
Expected: every module compiles clean, scalafmt reports no issues.

- [ ] **Step 2: Regenerate the registry and confirm `dashboard-01` is in it**

Run: `cd modules/site && node scripts/build-registry.mjs`
Expected: output mentions the new item count including `dashboard-01`; `git diff --stat public/registry/` shows a new `dashboard-01.json` and an updated `index.json`.

- [ ] **Step 3: Spot-check no regression on a page NOT touched by this feature**

From `modules/site`: start the dev server, navigate to `/components/combobox`, and re-run the exact multi-select verification sequence from earlier this session (open, select 2 items, confirm chips render, confirm popover stays open, deselect via chip remove button, confirm popover still doesn't close) — this specific component was the one most recently touched before this feature and is the most likely place for an `Icons` migration regression to surface. Delete the script, kill the dev server.

- [ ] **Step 4: Commit any remaining uncommitted changes**

Run `git status --short` — every file from Tasks 1-9 should already be committed individually; if anything is still unstaged (e.g. a stray `public/registry/` regeneration from Step 2), commit it:
```bash
git add modules/site/public/registry/
git commit -m "Regenerate registry with dashboard-01

Co-Authored-By: Claude Sonnet 5 <noreply@anthropic.com>"
```
