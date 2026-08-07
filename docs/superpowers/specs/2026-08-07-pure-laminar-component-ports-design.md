# Pure Laminar Component Ports — Design

**Date:** 2026-08-07  
**Status:** Accepted  
**Repo:** shadcn-scalajs  
**Reference:** `/Users/elam/Personal/shadcn-svelte/docs/src/lib/registry/ui/`

## Goal

Fully port DataTable, DatePicker, Field, InputGroup, NavigationMenu, RangeCalendar, Sidebar, Sonner, Spinner, Tabs, Typography, Toggle, and ToggleGroup to pure Laminar — matching shadcn-svelte API surface and structure — with no bits-ui, TanStack Table, or svelte-sonner.

## Principles

1. **Pure Laminar + Airstream** — `Var`, `documentEvents`, portals; same tier-3 pattern as DropdownMenu / Calendar / create-page Picker.
2. **Additive APIs** — keep existing `apply` working so preview-02 does not break.
3. **Style packs** — preserve `cn-*` hooks and `data-slot`; watch unlayered pack CSS beating utilities (inline `position` when portals fight packs).
4. **Typography** — docs recipes only (no installable component), matching upstream and `modules/ui/CLAUDE.md`.
5. **DataTable** — pure Scala table-state utilities composing `Table`, not a JS facade.
6. **Out of scope** — new `Sc*` wrappers, `sidebar-01` block, preview-02 mosaic redesign (fix cards only if APIs break them).

## Waves

### Wave 1 — Polish existing ports

Field (error list API + label classes), InputGroup (`textarea`), Tabs (`line` variant, mods, keyboard), Toggle audit, ToggleGroup (`spacing` / `orientation` / item mods), Spinner (SVG loader), Sidebar (missing parts + mobile Sheet + cookie).

### Wave 2 — Calendar family

Calendar range mode + disabled dates; new RangeCalendar; DatePicker single + range via Popover.

### Wave 3 — Heavy systems

NavigationMenu rewrite (state machine + viewport); Sonner toaster queue; DataTable state helpers + demo; Typography docs page.

## Done criteria (per component)

Scala API + `.registry.json` + public registry JSON + interactive `/components/:name` demo; `sbt compile` + `scalafmtAll`; browser smoke on the demo page.

## Orchestration

Parent agent plans/reviews; Wave execution dispatched to **composer-2.5-fast** workers in parallel where files do not conflict.
