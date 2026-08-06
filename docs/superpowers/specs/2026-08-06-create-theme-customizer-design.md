# Create page: theme customizer + dashboard preview block

**Status:** Draft, pending user review
**Scope:** First of several planned specs cloning the shape of shadcn-svelte's `/create/preview-02` page (a live theme customizer with a dashboard preview) into shadcn-scalajs. Explicitly deferred to later specs: a second alternate preview mockup, URL-based preset sharing, a "Random"/shuffle control, and an "Initialize Project" dialog.

## Goals

- A new `/create` page with a customizer sidebar (9 controls) and a live dashboard preview that visibly reacts to every control.
- The customizer's choices are **site-wide**: they drive the same styling state the existing header style-pack selector uses, on every page (Home, Docs, Components gallery, Component doc pages, Blocks pages, and `/create` itself).
- Icon Library becomes a genuinely functional control: every existing shipped component (~30 of them) that currently hand-rolls its own inline SVG icon gets migrated to a shared, swappable icon registry supporting Lucide and Hugeicons.

## Non-goals (explicitly deferred to later specs)

- The reference's second alternate dashboard mockup and its "01/02" preview switcher.
- URL query-param state encoding, "Copy Preset" / "Open Preset" sharing, and the "Random" (shuffle) control.
- The "Initialize Project" dialog / CLI command generation.
- The reference's richer popover/swatch-grid picker widgets — this spec uses plain `<select>` dropdowns for every control, matching the pattern the existing header style-pack selector already uses.
- Migrating every icon usage to the new abstraction is in scope (see Icon Library below), but sourcing additional icon libraries beyond Lucide and Hugeicons is not.

## Architectural context

This site is a static multi-page app with no client-side router: every navigation is a real browser navigation, and `Main.main()` re-runs from scratch on each page load with fresh `Var`s (see `modules/site/src/main/scala/shadcnscalajs/site/Main.scala`'s `main()` dispatch). The existing header style-pack `<select>` already demonstrates this limitation today — its selection resets to "lyra" on every navigation, because there is no persistent in-memory state across page loads.

"Site-wide" therefore requires real persistence, not just a shared `Var`. This design uses `localStorage`.

## Data model

A single case class, `ThemeConfig`, replaces the separate `darkMode`/`stylePack` `Var`s every page currently creates independently:

```scala
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
```

Preset value lists:

| Field | Values |
|---|---|
| `stylePack` | vega, nova, maia, lyra, mira, luma, sera, rhea (existing, unchanged) |
| `baseColor` | neutral, gray, zinc, stone, slate |
| `themeColor` | red, orange, amber, yellow, lime, green, emerald, teal, cyan, sky, blue, indigo, violet, purple, fuchsia, pink, rose — Tailwind's standard named color palette, matching shadcn's own convention rather than a smaller invented list |
| `chartColor` | same palette as `themeColor`, settable independently |
| `headingFont` / `bodyFont` | default, inter, geist, dm-sans |
| `iconLibrary` | lucide, hugeicons |
| `radius` | default, none, small, medium, large — matching shadcn's actual radius preset names (not the `sm`/`lg`/`xl` naming I'd first proposed) |

Two scoping notes:

- **Radius** already exists today, baked into each style pack's own `--radius`/`--style-card-radius`/`--style-control-radius` values (see `globals.css`'s `[data-style-pack="..."]` blocks). Introducing an independent `radius` field means it **overrides** whatever the active pack would otherwise set for these three properties — the pack continues to drive shadow, control height, and font-family, but radius becomes its own knob once a non-`"default"` value is chosen. `"default"` means "defer to the pack's own radius," so existing pages that never touch the new control keep behaving exactly as they do today.
- **`headingFont`/`bodyFont`** follow the same `"default"`-sentinel pattern as radius, for the same reason: the "sera" style pack already has its own special Playfair Display / Noto Sans pairing (see `globals.css`'s `[data-style-pack="sera"]` font block), and defaulting these two fields to a concrete font like `"inter"` would silently override that pairing on every page that never touches the new controls. `"default"` means "defer to whatever the active pack sets (or the site's base Inter Variable if the pack doesn't override it)"; choosing anything else always wins over the pack, matching radius's precedence rule.
- **`menuColor`/`menuAccent`** don't correspond to anything else on the site — the real header nav is a plain bar, not a sidebar. These two fields only ever affect the new dashboard preview block's own internal sidebar nav's CSS. They're still part of the persisted, site-wide `ThemeConfig` for consistency (so the preview block looks the same regardless of which page set them), they just have a narrower blast radius than the other fields.

## Persistence & application

New module: `modules/site/src/main/scala/shadcnscalajs/site/ThemeConfig.scala`.

- `ThemeConfig.default: ThemeConfig` — the all-defaults value shown above.
- `ThemeConfig.load(): ThemeConfig` — reads a single `localStorage` key (e.g. `"shadcn-scalajs:theme"`), parses it as JSON via `js.JSON.parse` + a `js.Dynamic` field-by-field read (same hand-rolled style already used in `ScAccordion.parseSections`/`ScDropdownMenu.parseItems` — no new JSON library dependency), and falls back to `default` on a missing key or any parse error.
- `ThemeConfig.store(cfg: ThemeConfig): Unit` — serializes back to the same key via `js.JSON.stringify` on a `js.Dynamic` literal.
- `ThemeConfig.applyToDocument(cfg: ThemeConfig): Unit` — sets `data-style-pack`, `data-base-color`, `data-theme-color`, `data-chart-color`, `data-heading-font`, `data-body-font`, `data-icon-library`, and `data-radius` attributes on `document.documentElement`, and toggles its `dark` class based on `darkMode`. This generalizes and replaces today's narrower `Main.syncHtmlStylePack` (which only ever handled `data-style-pack`).

Every page's mount function (`app()`, `componentsGalleryPage()`, `componentDocsPage()`, `BlocksLayout.apply()`, and the new `CreatePage()`) is updated to:
1. `val themeConfig = Var(ThemeConfig.load())` instead of separate `darkMode`/`stylePack` `Var`s.
2. Bind `ThemeConfig.applyToDocument` reactively off `themeConfig.signal` (replacing the existing `stylePackA <-- stylePack.signal` / `syncHtmlStylePack(stylePack)` pair).
3. Any control that changes a field does `themeConfig.update(_.copy(...))` followed by `ThemeConfig.store(themeConfig.now())`, so the next page load (a real browser navigation, per the architectural note above) picks up the change.

The existing header style-pack `<select>` (present in 4 duplicated header blocks today) becomes one control among several writing into the same `ThemeConfig`, rather than owning its own narrower `Var`.

## CSS additions (`globals.css`)

New attribute-selector blocks, following the existing `[data-style-pack="..."]` pattern:

- `[data-base-color="..."]` — redefines `--background`/`--foreground`/`--card`/`--border`/etc.'s grayscale OKLCH values per preset.
- `[data-theme-color="..."]` — redefines `--primary`/`--accent`/`--ring` OKLCH values per preset.
- `[data-chart-color="..."]` — redefines `--chart-1` through `--chart-5`.
- `[data-heading-font="..."]` / `[data-body-font="..."]` (for every value except `"default"`) — redefines `--font-heading-token`/`--font-body` directly, taking precedence over whatever the active `[data-style-pack]` block set (declared after the style-pack blocks, same precedence technique as radius below). `data-heading-font="default"`/`data-body-font="default"` set no font properties at all, so the pack's own values (e.g. sera's Playfair/Noto Sans pairing, or the site's base Inter Variable for every other pack) show through untouched.
- `[data-radius="..."]` — redefines `--radius`/`--style-card-radius`/`--style-control-radius` directly, taking precedence over whatever the active `[data-style-pack]` block set for those same three properties (achieved via source order: these rules are declared after the style-pack blocks) — except when `data-radius="default"`, which sets no radius properties at all so the pack's own values show through untouched.

## Icon abstraction

New module: `modules/ui/src/main/scala/shadcnscalajs/ui/Icons.scala`.

- A named registry function per icon actually used anywhere in the component library today (audit needed as the first implementation step — expected set includes at minimum: `chevronDown`, `chevronUp`, `chevronRight`, `chevronsUpDown`, `check`, `x`, `search`, `sun`, `moon`, plus whatever else turns up in the audit).
- Each named function resolves to one of two backing implementations — Lucide (centralizing the path data already hard-coded today, unchanged visually) or Hugeicons (newly sourced from Hugeicons' free, MIT-licensed stroke-rounded set, matching each Lucide icon's meaning as closely as that free set allows) — based on the active `ThemeConfig.iconLibrary`, read the same way `applyToDocument` exposes the other fields (a `data-icon-library` attribute on `<html>`, read via a small reactive signal `Icons.activeLibrary: Signal[String]` that components consume when building their icon elements).
- Every existing component (~30 files, e.g. `Accordion.scala`'s `chevronDown()`, `Combobox.scala`'s `chevronsUpDown()`/`checkIcon()`, `DropdownMenu`, and everything else with a private inline `svgTag(...)` helper) gets refactored to call `Icons.xxx()` instead of its own private icon function.
- This is the largest, most mechanical chunk of work in this spec — see Implementation strategy below.

## UI structure

**`modules/site/src/main/scala/shadcnscalajs/site/CreatePage.scala`** (new): a two-pane layout — a customizer sidebar card with the 9 controls as `<select>` dropdowns (Style, Base Color, Theme, Chart Color, Heading Font, Font, Icon Library, Radius, Menu Color, Menu Accent), each writing into the shared `themeConfig: Var[ThemeConfig]` on change; and the dashboard preview block rendered inline next to it (not iframed, so it shares the page's `ThemeConfig` `Var` directly with no cross-frame messaging needed).

**`modules/blocks/src/main/scala/shadcnscalajs/blocks/dashboard01/Dashboard01.scala`** (new block, following the existing `login01`/`signup01`/`otp01`/`calendar01` naming convention, with its own `.registry.json` sidecar): an original analytics-dashboard composition built from existing `modules/ui` primitives (`Card`, the existing `Chart` component, `Table`, `Progress`, `Field`/`Input`, `Button`, `Badge`) plus a simple internal sidebar nav (the surface `menuColor`/`menuAccent` actually style). Enough surface area to exercise every new knob — not a reproduction of any specific reference layout's card titles, copy, or dataset.

**Routing:** a new `else if pathname == "/create" then CreatePage()` branch in `Main.main()`'s dispatch, and a `"Create"` nav link added consistently across the 4 existing duplicated header blocks (unifying those headers is explicitly out of scope, per `BlocksLayout.scala`'s own existing tech-debt note pointing at the docs-site IA redesign spec).

## Testing / verification

- `sbt compile`/`scalafmtAll` across every touched module (`core`, `ui`, `webcomponents`, `site`, `blocks`) after each implementation phase.
- Headless-browser verification (Chrome via `puppeteer-core`, the established pattern from this project's recent style-pack work, since no other browser automation tool is available in this environment): load `/create`, exercise every control, confirm the dashboard preview visibly updates (background/accent colors, chart colors, corner radius, font family, icon shapes swapping between Lucide and Hugeicons); confirm a choice persists across a simulated navigation to `/components` (same `localStorage` key, header selector reflects the same values); spot-check a handful of the ~30 migrated components' icons still render correctly (not blown up in size — this project has already hit exactly that SVG-sizing bug once this session, in `Combobox.scala`'s check icon).

## Implementation strategy (cost-aware delegation)

Per the user's stated preference, implementation work is delegated to subagents on a cheaper model, with the main thread doing only: writing the plan, establishing the `Icons.scala` pattern with one or two reference migrations, and final verification/review of subagent work before reporting completion. The ~30-component icon migration in particular is highly parallelizable — independent files, identical mechanical transformation — and is the clearest candidate for fanning out across several subagents once the pattern is established.
