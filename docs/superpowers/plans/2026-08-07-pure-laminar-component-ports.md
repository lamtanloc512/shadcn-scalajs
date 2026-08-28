# Pure Laminar Component Ports — Implementation Plan

> **For agentic workers:** Orchestrator uses **composer-2.5-fast** for execution. Read the design at `docs/superpowers/specs/2026-08-07-pure-laminar-component-ports-design.md`. Reference UI at `/Users/elam/Personal/shadcn-svelte/docs/src/lib/registry/ui/`. Do not invent APIs — port from svelte sources. Prefer additive changes.

**Goal:** Full pure-Laminar ports of the listed components in three waves.

**Tech:** Scala 3 / Laminar / Airstream under `modules/ui`. Demos in `modules/site`. Registry sidecars next to Scala sources; regenerate public JSON via existing build-registry when available.

---

## File map

| Area | Primary files |
|------|----------------|
| Field | `modules/ui/.../Field.scala`, `Field.registry.json`, site Field demo |
| InputGroup | `modules/ui/.../InputGroup.scala`, registry, site demo |
| Tabs | `modules/ui/.../Tabs.scala`, registry, Faq card if needed |
| Toggle / ToggleGroup | `Toggle.scala`, `ToggleGroup.scala`, preview-02 cards |
| Spinner | `Spinner.scala` |
| Sidebar | `Sidebar.scala` |
| Calendar family | `Calendar.scala`, new `RangeCalendar.scala`, `DatePicker.scala` |
| NavigationMenu | `NavigationMenu.scala` |
| Sonner | new `Sonner.scala` (+ Toast chrome reuse) |
| DataTable | new `DataTable.scala` + Table composition |
| Typography | site `/components/typography` page only |

---

## Wave 1 tasks

### T1 — Field
- Port `Field.error` to accept `errors: Seq[String]` and/or children; `data-slot="field-error"`.
- Align `Field.label` classes with `field-label.svelte` (checked-state selectors).
- Update `/components/field` demo.

### T2 — InputGroup
- Add `InputGroup.textarea` matching `input-group-textarea.svelte`.
- Fix defaulted-param-before-varargs if present on `addon`/`button`.
- Demo textarea + block addon.

### T3 — Tabs
- `list` variant `default` | `line`; `data-slot` on parts.
- Stateful API accepting list/trigger mods; keyboard Left/Right/Home/End.
- Update demo; optionally simplify Faq if safe.

### T4 — Toggle + ToggleGroup
- Toggle: audit classes/`data-slot`; disabled Signal if missing.
- ToggleGroup: `spacing`, `orientation` (`data-vertical`, `data-spacing`); item mods.
- Update demos + preview-02 width wrappers where possible.

### T5 — Spinner
- Verify pack CSS targets; prefer SVG Loader2 + `aria-label="Loading"` if packs allow.

### T6 — Sidebar
- Add `input`, `groupAction`, `menuAction`, `menuSkeleton`.
- Mobile Sheet path; cookie persistence for open state.
- Expand `/components/sidebar` demo.

**Wave 1 gate:** `sbt compile` + `scalafmtAll` + demo smoke.

---

## Wave 2 tasks

### T7 — Calendar range + disabled
### T8 — RangeCalendar component + registry + demo
### T9 — DatePicker single + withRange + demos

**Wave 2 gate:** compile + calendar/date-picker/range-calendar demos.

---

## Wave 3 tasks

### T10 — NavigationMenu rewrite
### T11 — Sonner toaster
### T12 — Typography docs page
### T13 — DataTable pure Scala state + demo

**Wave 3 gate:** compile, registries.
