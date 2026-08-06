# Progress — source of truth for task state

## Done

- **v1 → 60-component catalog on Tailwind v4** (detail in `AGENTS.md` History/Status + git log): 5 basecoat-CSS components → wholesale migration to shadcn/ui's own Tailwind v4 classes → all ~60 catalog components (Data Table and AI-chat additions deliberately cut), each with a `.registry.json` sidecar, most with `sc-*` wrappers, all wired into `/components/:name`. Live-browser-verified except `ContextMenu` (browser automation's right-click hits Chrome's native menu; reviewed against the proven DropdownMenu pattern instead). Docs-site cleanup and `AGENTS.md`/`vendor/NOTICE.md` rewrites landed with it; `vendor/NOTICE.md` still flags a **latent** Shadow-DOM `:root`-vs-`:host` bug at `globals.css:66` for whenever `sc-components.css` gets wired.
- **franky tooling repairs**: `.franky/scripts.toml` pointed at root paths commit `64ebe83` deleted, and the root `scripts/*` wrappers it deleted were never restored — `franky verify` failed with `required script missing: scripts/build`. Both fixed: `scripts.toml` → `.franky/scripts/*` (edit those, never the root wrappers), and root `scripts/{setup,build,lint,test,serve,visual}` recreated as thin delegating wrappers. Do **not** repair this with `franky init --refresh-adapters` — its dry-run shows it would re-`create` `.franky/scripts.toml`, the exact regression decisions.log warns about. Also replaced a hardcoded foreign coursier PATH (`/Users/locgorilla/...`) across `.franky/scripts/*` + `AGENTS.md` with `$HOME/...` plus a `cs`→`coursier` fallback; `coursier` was brew-installed here.

- **Blocks pipeline + 4 blocks** (spec `docs/superpowers/specs/2026-08-05-blocks-pipeline-design.md`, plan `docs/superpowers/plans/2026-08-05-blocks-pipeline.md`). New `modules/blocks` sbt module; `build-registry.mjs` now scans two roots (component output verified byte-identical — 0 of 60 changed); `/blocks`, `/blocks/<name>` (Preview/Code tabs, iframe, Refresh, Open in New Tab, install command) and chrome-less `/blocks/<name>/preview`; `Field.group` added. `login-01`/`signup-01`/`calendar-01` ported, `otp-01` authored. Code tab reads generated registry JSON so shown source can't drift. CLI needed **no** changes — `add login-01` writes 7 files via a two-level dep walk (`login-01`→`field`→`label`), now asserted in `scripts/test`.

- **preview-02 fidelity (complete)** (spec `docs/superpowers/specs/2026-08-06-create-theme-customizer-design.md`, plan `docs/superpowers/plans/2026-08-06-preview-02-fidelity.md`). Waves 1–3 done. Create shell + customizer + preset codec/locks/history; 5 icon libraries (Lucide + Tabler/Hugeicons/Phosphor/Remix, 58 concepts each); 33-card mosaic under `modules/site/.../create/preview02/`; routes `/create` → `/create/preview-02`, chrome-less `/preview/preview-02`. Home embeds `Preview02` with Build Your Own → `/create`. Wave 2 browser gate PASS; Wave 3 Task 30 mosaic audit PASS (widths 3000/2400/lyra-mira 2600); Task 31 create-page e2e **71/0**; Task 32 `franky verify` PASS (setup/build/lint/test). Orchestrator fixes: `Chart.HoverVar` init-order NPE; `SavingsTargets` single outer `data-card`; restored root `scripts/*` thin wrappers for franky. Residual deltas vs reference kept below.

## Known residual deltas vs the reference (Task 30)

Intentional / accepted gaps vs shadcn-svelte `/create/preview-02` (LayerChart + bits-ui):

1. **Charts** — hand-rolled SVG `Chart.bar` / `area` / `donut` instead of LayerChart/d3; no tween on ticker swap; donut center labels are HTML overlays; tooltip is Laminar fixed-position, not bits.
2. **Select** — native `<select>` via `Select.stateful`, not Trigger/Content popover selects.
3. **ToggleGroup** — no call-site `mods` / `flex-1` item sizing; RollerShades/KitchenIsland wrap for width.
4. **Checkbox indeterminate** — DOM `indeterminate` set; no `:indeterminate` CSS (may look checked when partial).
5. **ReleaseCatalog filters** — client-side filter added so toggles change row count (reference binds filters but still renders all rows).
6. **Payments** — no calendar in the svelte card (breadcrumb + items only); calendar lives on UpcomingPayments.
7. **3xl mosaic gap** — `3xl:[--gap:--spacing(12)]` did not change computed gap at 1920px in the audit probe (still md `--spacing(10)` / 40px); widths 3000/2400/lyra-mira 2600 verified. Track as CSS breakpoint follow-up, not a card defect.
8. **Switcher 02 mosaic** — still deferred (01 active; 02 “coming soon”).

Structural comparison was against `.svelte` sources (live shadcn-svelte docs server not required for this gate). Full-mosaic screenshot matrix deferred to manual visual QA.

## Next

- **Switcher “02” mosaic** — deferred from preview-02 fidelity; port separate `examples/create/preview/` mosaic when scheduled (01 active; 02 “coming soon”).
- **3xl mosaic gap follow-up** — see residual delta #7 above.
- **`Button.apply` has no default variant/size**, unlike upstream's cva `defaultVariants: {variant: "default", size: "default"}`. A bare `Button("Save")` renders 20px tall, transparent, zero padding — every call site must use `Button.of(_.variant(...), _.size(...))`. Found by screenshot during the block ports — computed-style checks on the *card* missed it. Giving `apply` the defaults would fix every bare call at once but changes 60 previews: do it as its own change with a browser pass.
- **Sidebar rebuild sub-project** — prerequisite for `sidebar-01`; see the blocks spec's non-goals for the 19-vs-726-line gap.
- Generate `Blocks.all`/`Blocks.render` from the `modules/blocks` sidecars; they are hand-maintained and can drift.
- `dashboard-01`, plus variants `-02`…`-05` per block category. Preview-tab viewport width toggles.
- `BlocksLayout` is a **fourth** duplicated header. The docs-site IA redesign spec should unify all four; the block pages also lack the style-pack `<select>` the other three headers have.
- **The other 59 components very likely have Alert's exact bug.** Alert was audited only because it was asked about; the same hybrid (v3-era or incomplete utilities + structure shaped around basecoat selectors) is the default expectation, not the exception. Audit each against `/Users/elam/Projects/ui/apps/v4/registry/new-york-v4/ui/*.tsx` before sub-project 3 deletes basecoat, or components will break silently at deletion time.
- `.cn-alert-action` / `has-data-[slot=alert-action]:pr-18` in `shadcn-presets.generated.css` is unreachable — upstream's `bases/*/ui/alert.tsx` has an `AlertAction` part that `modules/ui` doesn't. Deliberately skipped: new-york-v4 has no `AlertAction`, so there is no utility fallback to write for CLI consumers, and inventing one would be a fidelity guess.
- `ScAlert` couldn't be browser-verified — Shadow DOM still gets no CSS (`ScElementBase.styleSheetText` is `None`), so every `Sc*` wrapper is unstyled until `sc-components.css` is wired. Compile + `fastLinkJS` only.
- 14 new components have no `Sc*`/`ScPrimitives` Web Component wrapper yet (only `modules/ui` + docs page exist so far).
- `sc-components.css` still isn't built/wired for `ScElementBase` — when it is, apply the `:root`→`:host` fix in `vendor/NOTICE.md` *before* shipping, not after rediscovering it's broken.
- `ContextMenu.scala`'s right-click trigger needs a manual (non-automated) browser check.
- CLI has no package-rewriting — "own every line" needs import-rewriting or a real `core` publish.
- `scripts/test` checks CLI-written files exist, not that they compile (full check manual, see `AGENTS.md`). Data Table + newest AI-chat additions remain a deliberate scope cut.

## Blockers

- (none)
