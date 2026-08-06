# Create page: theme customizer + preview-02 fidelity

**Status:** Accepted — implementation source of truth for the Preview-02 Fidelity Plan  
**Date:** 2026-08-06  
**Supersedes:** the prior draft of this document (simplified `/create` + inline `Dashboard01` customizer). That draft’s deferred non-goals (URL presets, shuffle, Initialize Project, rich pickers, five icon libraries, iframe preview, and the 33-card mosaic) are **in scope** here.

## Goal

Ship a pixel-faithful Scala.js / Laminar port of shadcn-svelte’s `/create/preview-02` experience:

- Responsive create shell with customizer, header, iframe preview frame, floating preview switcher, actions, and Initialize Project dialog.
- Exact 33-card preview-02 mosaic (34 site source files: one grid + 33 cards).
- Full preset encode/decode, locks, undo/redo history, randomize/reset, dark-mode shortcut, and five icon libraries.
- Routes: `/create`, `/create/preview-02`, and chrome-less `/preview/preview-02` (iframe target).

Reference sources (do not invent layout or copy):

- Shell: `shadcn-svelte/docs/src/routes/(app)/(layout)/(create)/create/+layout.svelte` and sibling `components/*`
- Mosaic: `shadcn-svelte/docs/src/lib/registry/examples/create/preview-02/preview-02.svelte` + `cards/*`
- Preset/state: `shadcn-svelte` `packages/cli/src/preset/preset.ts` + `docs/src/lib/features/design-system/*`

## Non-goals (only these)

1. **Switcher “02” mosaic** — the reference switcher maps label `"02"` → registry example `preview` (a separate 33-card mosaic under `examples/create/preview/`). Keep the floating **01 / 02** switcher UI with **01** active (`preview-02`). Do **not** port the separate `preview` mosaic in this work. Clicking **02** may no-op, show a short “coming soon” affordance, or route to a stub — but must not claim fidelity for that mosaic.
2. **CLI packaging of preview-02** — preview-02 is a **site-only** registry-example equivalent under `modules/site`, not a `modules/blocks` CLI block and not written by `packages/cli add`.
3. **Changing `dashboard-01`** — leave `modules/blocks/.../dashboard01/` and its registry entry **unchanged**. It remains separately available on Blocks routes. `/create` no longer uses it as the create-page preview.
4. **Unifying duplicated site headers** across the whole docs site (still owned by the docs-site IA redesign). Add Create nav links where the create shell needs them; do not rewrite all four headers as a separate project.
5. **Welcome-dialog marketing copy parity beyond structure** — include the dismissible welcome dialog in the create shell (reference: `welcome-dialog.svelte`); adapt product naming to shadcn-scalajs where needed, but keep layout/behavior.

Everything else required for visual and interaction fidelity of `/create/preview-02` is in scope, including previously deferred customizer features.

---

## Architecture

### Placement

| Concern | Location |
|---|---|
| Theme / preset state | `modules/site/.../ThemeConfig.scala` (evolve in place) |
| Create shell, customizer, pickers, actions, dialogs | `modules/site/.../site/create/` (split from today’s `CreatePage.scala`) |
| Preview-02 mosaic | `modules/site/.../site/create/preview02/` — **34 Scala files** |
| Shared UI primitives gaps | `modules/ui` only as needed by the reference; update `.registry.json` when APIs/deps change |
| Icons (5 libraries) | `modules/ui/Icons.scala` + `Icons.registry.json` |
| Routing / FOUC / fonts / tokens / QR dep | `Main.scala`, `globals.css`, `index.html`, `modules/site/package.json` |
| Existing dashboard block | `modules/blocks/.../dashboard01/` — **untouched** |

### Routing (static multi-page site)

This site has **no client-side router**: each path is a real navigation; `Main.main()` remounts with fresh `Var`s. Persistence must use `localStorage` + URL `?preset=` (and iframe attribute sync), not an in-memory singleton alone.

| Path | Behavior |
|---|---|
| `/create` | Redirect (or immediate remount equivalent) to `/create/preview-02`, preserving `url.search` (reference: `create/+page.server.ts` → 303 to `/create/preview-02${search}`). |
| `/create/preview-02` | Full create chrome: site header + designer main + customizer + iframe pointing at `/preview/preview-02` (+ current preset query) + floating switcher. |
| `/preview/preview-02` | Chrome-less mosaic only (`Preview02`), for the iframe. Support `fromPreview=true` minimize control back to `/create/preview-02` with search preserved (reference: `preview/[item]/+page.svelte`). |

Other `/create/<item>` values are out of scope except the deferred **02** stub if implemented.

### Iframe + theme sync

- Parent create page hosts `<iframe src="/preview/preview-02?...">` with height `h-(--preview-height)` and `title` set to the item name.
- Maximize control (top-right of preview frame) navigates to `/preview/preview-02?...&fromPreview=true`.
- Theme must apply on both parent and iframe documents without FOUC: blocking/pre-paint script in `index.html` plus `ThemeConfig.applyToDocument` on mount; persist preset string and locks so iframe loads match parent.
- Fix any stale `dark` class removal path so light mode clears correctly after dark.

---

## Exact layout dimensions (create shell)

Mirror reference CSS custom properties and structure from `create/+layout.svelte` + `customizer.svelte` + `preview.svelte`:

```
[--customizer-width:--spacing(56)]          /* 14rem */
[--gap:--spacing(4)] md:[--gap:--spacing(6)]
[--preview-height:calc(100svh-var(--header-height)-2rem-150px)]
md:[--preview-height:calc(100svh-var(--header-height)-2rem)]
```

Shell structure:

- Root: `group/layout relative z-10 flex h-svh flex-col overflow-hidden section-soft`
- Main (`data-slot="designer"`): `container-wrapper flex min-h-0 flex-1 flex-col gap-(--gap) p-(--gap) pt-[calc(var(--gap)*0.25)] md:flex-row-reverse`
  - Children = preview frame (flex-1)
  - Customizer column: `md:w-(--customizer-width)`, self-start
- Customizer card: `dark max-h-(--preview-height) ... rounded-2xl bg-card/90 shadow-xl backdrop-blur-xl`, size sm
  - Desktop: vertical field groups + separators; header with Main Menu visible `md:flex`
  - Mobile: horizontal scrolling controls (`flex-row` field groups, `overflow-x-auto` content)
- Preview frame (`data-slot="preview"`): `rounded-2xl border`, inner scroll `max-h-(--preview-height)`, iframe height `--preview-height`
- Floating switcher: `absolute right-3 bottom-3`, `dark ... rounded-xl bg-card/90 p-1 shadow-xl backdrop-blur-xl`, buttons `h-7 min-w-8 ... data-[active=true]:bg-accent`
- Sponsor blocks (`Cta` / `Ethical` equivalents): `hidden md:flex` below customizer on desktop

Do not invent a left-rail `<aside>` customizer; the reference is **desktop reversed** (customizer on the right via `md:flex-row-reverse`).

---

## Exact layout dimensions (preview-02 mosaic)

From `preview-02.svelte`:

Outer: `overflow-x-auto overflow-y-hidden bg-muted contain-[paint]` with gap tokens:

```
[--gap:--spacing(4)]
md:[--gap:--spacing(10)]
3xl:[--gap:--spacing(12)]
style-lyra:md:[--gap:--spacing(6)]
style-mira:md:[--gap:--spacing(6)]
dark:bg-background
```

Capture grid (`data-slot="capture-target"`):

```
grid w-[2400px] grid-cols-7 ... md:w-[3000px]
style-lyra:md:w-[2600px]
style-mira:md:w-[2600px]
gap-(--gap) p-(--gap)
*:[div]:gap-(--gap)
```

Seven columns (left → right), card order **exact**:

| Col | Span / notes | Cards (top → bottom) |
|---|---|---|
| 1 | 1 col; `contain-intrinsic-size:380px_1200px` | ContributionHistory, EmptyDistributeTrack, QrConnect, DividendIncome, IndexInvesting, SyncingState |
| 2 | 1 col | PayoutThreshold, ClaimableBalance, Preferences, SavingsProgress, KitchenIsland |
| 3–4 | `col-span-2`; intrinsic `760px_1200px` | SavingsTargets, RecentTransactions; then nested 2-col: (SidebarNav, Faq) \| (Payments, FrontDoor); then ReleaseCatalog |
| 5 | 1 col | AccountAccess, CardOverview, TransferFunds, CoverArt, LoadingCard |
| 6 | 1 col | ReceivingMethod, PowerUsage, EmptyConnectBank, UpcomingPayments, RollerShades |
| 7 | 1 col | StockPerformance, EmptyExploreCatalog, NewMilestone, SocialLinks, NotificationSettings |

That is **33 cards**. Source file budget under `modules/site/.../create/preview02/`:

1. `Preview02.scala` — grid shell only  
2–34. One Scala file per card (PascalCase matching the reference component names above)

Copy, datasets, class names, style-pack gap/width overrides, and per-card responsive visibility (e.g. DividendIncome `hidden md:block` chart/label) must match the reference files. Prefer `content-visibility` / contain-intrinsic sizing class parity where Tailwind v4 supports the utilities used.

---

## Component boundaries (create shell)

Split today’s monolithic `CreatePage.scala` into focused site modules (names indicative; keep packages under `shadcnscalajs.site.create`):

| Module | Responsibility |
|---|---|
| Create shell / layout | CSS vars, header slot, designer main, wire children |
| Preview frame | iframe, maximize, hosts switcher |
| PreviewSwitcher | 01/02 affordance; 01 → `preview-02` active; 02 deferred |
| Customizer | Card chrome, field groups, separators, footers |
| Style / BaseColor / Theme / ChartColor / Font / IconLibrary / Radius / MenuColor / MenuAccent pickers | Rich picker menus (not bare `<select>`), each with LockButton |
| LockButton | Per-field lock toggle writing persisted locks |
| MainMenu | Navigate…, Shuffle, Light/Dark, Undo, Redo, Reset + shortcut labels |
| CopyPreset / OpenPreset | Copy `--preset <code>`; dialog to paste code or `--preset …` |
| RandomButton / Reset | Shuffle / reset to defaults |
| InitializeProject dialog | Shows init command with current preset; copy command |
| Action / command palette (optional parity) | ⌘/Ctrl+P navigate among create examples if wired; minimum: MainMenu “Navigate…” |
| Welcome dialog | First-visit dismissible dialog, persisted |
| Sponsor / Ethical | Desktop-only below customizer |

Reuse existing `ui` primitives (`Card`, `Button`, `Dialog`, `Field`, `Separator`, `Tooltip`, `DropdownMenu`/`Command` as needed). Site-only picker chrome may live under `create/` without becoming registry components.

---

## State model

### Defaults (Nova / Neutral)

Match `DEFAULT_PRESET_CONFIG` from `shadcn-svelte/packages/cli/src/preset/preset.ts` (first value of each v2 field array):

| Field | Default |
|---|---|
| `style` / stylePack | `nova` |
| `baseColor` | `neutral` |
| `theme` / themeColor | `neutral` |
| `chartColor` | `neutral` |
| `iconLibrary` | `lucide` (first key in reference `iconLibraries`) |
| `font` / bodyFont | `inter` |
| `fontHeading` / headingFont | `inherit` |
| `radius` | `default` |
| `menuAccent` | `subtle` |
| `menuColor` | `default` |
| `darkMode` | `false` (orthogonal to preset code; toggled via D / MainMenu) |

**Replace** the current site defaults (`lyra`, orange theme/chart, `"default"` fonts, menuAccent `"solid"` naming, gray/slate base list). Align value vocabularies with the reference:

- **Styles:** nova, vega, maia, lyra, mira, luma, sera, rhea  
- **Base colors:** neutral, stone, zinc, mauve, olive, mist, taupe  
- **Themes / chart colors:** reference theme key set (neutral + stone/zinc + named hues + mauve/olive/mist/taupe)  
- **Fonts:** full `PRESET_FONTS` list used by the reference pickers (not a 4-option subset); heading allows `inherit`  
- **Icon libraries:** lucide, tabler, hugeicons, phosphor, remixicon  
- **Radius:** default, none, small, medium, large  
- **Menu accent:** `subtle` \| `bold` (not `solid`)  
- **Menu color:** default, inverted, default-translucent, inverted-translucent  

### Preset encode / URL

- Persist an encoded preset string (v2 `"b"` + base62), compatible with reference `encodePreset` / `decodePreset` field order and bit widths so shared codes round-trip.
- Sync `?preset=` on the create URL with `replaceState` semantics (no scroll jump).
- `shareUrl` shape: origin + `/create?preset=<code>` (or `/create/preview-02?preset=<code>` — prefer matching reference `/create?preset=` since `/create` redirects and preserves search).
- Copy Preset copies `` `--preset ${code}` ``; Open Preset accepts raw code or `--preset <code>`, validates with the same alphabet/version rules, rejects invalid input (`aria-invalid`).

### Locks

Persisted lock map (localStorage key e.g. `locks` / namespaced equivalent) for: style, baseColor, theme, chartColor, iconLibrary, font, fontHeading, menuAccent, menuColor, radius (plus unused item/template keys if keeping structural parity). Randomize **skips** locked fields. Unlock/lock via LockButton on each picker.

### History

Undo/redo stack over the encoded preset string (reference: `StateHistory`). Shortcuts: ⌘/Ctrl+Z undo, ⇧⌘/Ctrl+Shift+Z redo. Disable menu items when `canUndo` / `canRedo` is false.

### Randomize / reset

- **R** — randomize (ignore when focus is editable input/textarea/select/contentEditable).  
- **⇧R** — reset to `DEFAULT_PRESET_CONFIG`.  
- Apply reference biases: lyra → mono font + none radius; rhea → no large radius; chart color pairings by theme; heading font ~70% inherit / ~30% contrast font.  
- Style-driven radius coercion (lyra/sera → none; rhea forbids large) must run after style changes, matching `design-system-provider.svelte`.

### Dark mode

- **D** toggles document dark class (same editable-target guard as R).  
- Customizer card itself uses `dark` chrome; preview/iframe content follows document theme.  
- Persist dark preference so navigations and iframe loads stay consistent.

### Document application

`applyToDocument` (and iframe equivalent) must set style-pack / base / theme / chart / fonts / icon library / radius / menu attributes or classes as required by `globals.css` and menu hooks (`.cn-menu-target`, translucent markers). Complete light **and** dark token coverage for every selectable base/theme/chart combination used by the reference. Load all fonts needed by the font pickers from `index.html` (no dead options).

Menu color behavior: inverted variants add `dark` on `.cn-menu-target`; translucent variants toggle `cn-menu-translucent` — observe DOM mutations if menus mount late (reference MutationObserver pattern), adapted to Laminar.

---

## Primitive gaps (`modules/ui`)

Extend **only** what preview-02 and the customizer require. Current stubs are insufficient; bring APIs in line with shadcn new-york-v4 / reference usage:

| Primitive | Required additions |
|---|---|
| Sidebar | Full composition used by SidebarNav (Provider, Root/collapsible, Content, Group, Menu, MenuButton, MenuItem, MenuBadge, etc. as referenced) |
| Item | Variants + parts used by pickers and cards (size/variant, ItemMedia, ItemContent, ItemTitle, ItemDescription, ItemActions, ItemHeader, separators) |
| Field | FieldSet, FieldLegend, FieldSeparator, FieldContent, FieldTitle, orientation variants |
| InputGroup | Addon, Text, Input, Button slots / alignments used by TransferFunds & SocialLinks |
| Empty | Media (icon variant), Content, Title, Description — EmptyConnectBank, EmptyDistributeTrack, EmptyExploreCatalog, SyncingState |
| Slider | Controlled bindings for single/multi values (KitchenIsland, RollerShades, PayoutThreshold) |
| ToggleGroup | Variants, disabled, single selection bindings (KitchenIsland, RollerShades, ReleaseCatalog) |
| Checkbox | Indeterminate state for NotificationSettings “select all” |
| Tabs | Stateful value binding (Faq) |
| Select | Stateful helpers for Preferences, TransferFunds, SavingsTargets, PayoutThreshold |
| Chart | See charts strategy below (may extend existing Chart helpers) |

Update `.registry.json` sidecars whenever public API or dependencies change. Do not block create-page fidelity on Web Component wrappers.

---

## Icons (five libraries)

`Icons.scala` must expose every icon concept used by preview-02 cards **and** create-shell controls (~40 concepts; audit reference `IconPlaceholder` usages and card SVGs during implementation).

For each concept, ship real path data (or equivalent SVG children) for:

1. Lucide  
2. Tabler  
3. Hugeicons  
4. Phosphor  
5. Remix Icon (`remixicon`)

Rules:

- Resolve via `data-icon-library` / active library signal.  
- Return **SVG roots** (or `display:contents`-safe roots) so `> svg` selectors keep working — no block wrapper that breaks accordion/chevron CSS.  
- Register an `icons` registry item so CLI consumers that depend on Icons compile.  
- Customizer Icon Library picker shows the five libraries with logo + preview glyph row (reference `icon-library-picker.svelte`).

---

## Charts / QR / dates strategy

### Charts

Reference cards use LayerChart (`BarChart`, `AreaChart`, `PieChart`) via shadcn Chart containers. This port:

- Implements **Laminar-owned SVG helpers** for bar, area, and donut/pie geometry (no LayerChart dependency).  
- Uses existing `--chart-*` CSS tokens from the active chart color.  
- Matches axes, curves/fills, labels, legends, and **hover tooltips** closely enough that matched screenshots differ only by intentional engine limits (document any residual delta).  
- Cards: ContributionHistory, DividendIncome, CardOverview, PowerUsage, SavingsProgress, SidebarNav (mini charts), StockPerformance (area + tooltip).

### QR

- Add `qrcode` npm dependency in `modules/site`.  
- Thin Scala.js facade calling `QRCode.toDataURL(url, { width: 160, margin: 1 })`.  
- `QrConnect` card: 160×160 image, pulse placeholder until ready, same connect URL and copy as reference.

### Dates

- Use existing `js.Date`-based `Calendar` API (no java.time).  
- UpcomingPayments / Payments calendar interactions bind through that API.

---

## Card interactions (must work)

At minimum, browser-verify the plan’s listed set:

| Card | Interaction |
|---|---|
| Faq | Tabs (General/Billing/Goals) + Accordion open/close |
| KitchenIsland | Switch enable; ToggleGroup scenes update sliders; sliders move |
| RollerShades | Slider drives shade height; ToggleGroup open/half/closed presets |
| NotificationSettings | Per-row checkboxes; indeterminate/select-all master |
| ReleaseCatalog | ToggleGroup filters catalog |
| StockPerformance | Command/combobox ticker search; area chart + tooltip |
| PayoutThreshold | Slider + Select bindings |
| Preferences / TransferFunds / SavingsTargets | Select value changes |
| UpcomingPayments / Payments | Calendar selection; Payments dropdowns |
| RecentTransactions | Dropdown menus |
| CoverArt (and any reference file-picker card) | File picker / upload affordance matching reference behavior |
| QrConnect | QR renders to data URL |

All 33 cards must render with correct copy and structure even if some are mostly static.

---

## Accessibility

- Keyboard shortcuts (R, ⇧R, D, ⌘/Ctrl+Z, ⇧⌘/Ctrl+Shift+Z, ⌘/Ctrl+P if action menu ships) must **not** fire when focus is in input, textarea, select, or contentEditable (match reference guards).  
- Provide visible focus rings on picker triggers and customizer controls.  
- Open Preset: `sr-only` label, `aria-invalid` when input non-empty and invalid.  
- KitchenIsland scene group: `sr-only` “Scenes” (or equivalent) labeling the ToggleGroup.  
- Dialogs (Open Preset, Initialize Project, Welcome): focus trap / restore via existing Dialog behavior; Escape closes.  
- iframe `title` set to the preview item name.  
- Decorative QR may use empty alt only if adjacent text already describes purpose (reference pattern); otherwise provide meaningful alt text describing “scan to connect”.  
- Lock buttons: accessible name (e.g. “Lock style”).  
- Do not rely on color alone for active switcher / lock state — use `data-active` / pressed semantics plus text.

---

## CSS / integration checklist

- Port shell and mosaic utilities into `globals.css` / Tailwind sources so 2400/3000/2600 widths, gaps, `--preview-height`, `--customizer-width`, style-lyra/mira overrides, and capture-target exist.  
- Complete light/dark token blocks for base/theme/chart selections; ensure dark mode does not override base/theme controls into no-ops (known prior bug: unlayered `.dark` after attribute selectors).  
- Font loading for every selectable family used.  
- Pre-paint theme script in `index.html`.  
- Menu accent/color hooks compatible with SidebarNav / `.cn-menu-target`.  
- `qrcode` dependency; Vite can resolve the facade.

---

## Relationship to dashboard-01

- `dashboard-01` stays a normal block: Blocks index, docs, preview routes unchanged.  
- Create page **does not** embed `Dashboard01`.  
- No registry or source edits under `modules/blocks/.../dashboard01/` for this feature.

---

## Implementation phases (aligned with the accepted plan)

1. **Spec** — this document (done when Accepted).  
2. **State + shell** — ThemeConfig/preset/locks/history/randomize; routes; responsive shell; customizer; actions; Initialize dialog; iframe frame; switcher affordance.  
3. **Primitives + icons** — ui gaps + five-library Icons.  
4. **Preview cards** — 34 files, exact grid and interactions.  
5. **Charts + QR + dates** — SVG chart helpers, qrcode facade, Calendar wiring.  
6. **Visual + Franky verify** — screenshots, interaction checks, compile, registry, `franky verify`.

---

## Verification criteria

Work is done only when all of the following pass:

### Functional

- Every customizer field updates preview (parent + iframe) for style, base, theme, chart, fonts, radius, menu color/accent, icon library.  
- Preset URL updates; Copy Preset / Open Preset round-trip; invalid codes rejected.  
- Locks persist and block randomize for locked fields.  
- Undo/redo and Reset restore expected presets.  
- R / ⇧R / D / undo-redo shortcuts honor editable-target guards.  
- Initialize Project dialog shows command including current `--preset` and copies it.  
- `/create` → `/create/preview-02` with search preserved; `/preview/preview-02` is chrome-less; maximize/minimize preserve search.  
- Switcher shows 01 active; 02 remains deferred (no false claim of second mosaic fidelity).  
- Listed interactive cards behave as specified; QR renders.

### Visual

- Matched desktop and mobile screenshots vs live shadcn-svelte reference for default Nova/Neutral, dark mode, representative style packs (including lyra/mira width+gap overrides), chart colors, fonts, radii, menu modes, and all five icon libraries.  
- Full `[data-slot="capture-target"]` mosaic at 3000px (or lyra/mira 2600px) compared; residual differences documented and intentional only.

### Build / repo

- `sbt ui/compile site/compile blocks/compile` (blocks still compiles; dashboard-01 unchanged).  
- `sbt scalafmtAll`  
- Registry generation (`build-registry.mjs` / predev) succeeds; Icons registry present if Icons is a dependency.  
- `./scripts/test` and `franky verify` exit 0; inspect `.franky/verify-report.json`.  
- Update `.franky/memory/PROGRESS.md` with completion status and remaining deferred **02** mosaic work.

### Explicit non-regressions

- `dashboard-01` block source and registry hash/content unchanged aside from unrelated pre-existing dirty work not part of this feature.  
- No CLI `add preview-02` requirement.

---

## Self-review (spec quality)

- **No Draft status** — Accepted.  
- **No contradictory non-goals** — prior “defer presets/shuffle/init/rich pickers/extra icon libs/second preview” removed; only switcher-02 mosaic, CLI packaging of preview-02, dashboard-01 edits, and full header unification remain deferred.  
- **Card count** — 33 cards, 34 Scala sources, ordered columns specified.  
- **Dimensions** — shell CSS vars and mosaic widths/gaps/columns specified from reference.  
- **Ambiguity** — 02 click behavior may be stub/no-op; must not implement the other mosaic. Preset share path may use `/create?preset=` (preferred) with redirect preserving search.  
- **Placeholders** — none intentional; implementation audits icon list and exact primitive part names from reference files at code time.  
- **Scope** — site-only preview-02; ui primitives only as required; no production code changes in Task 1 (spec only).
