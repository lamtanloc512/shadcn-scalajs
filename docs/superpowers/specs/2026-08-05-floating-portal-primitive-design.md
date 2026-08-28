# Floating/Portal Primitive + Popover Retrofit — Design Spec

Date: 2026-08-05

## Context

This is sub-project 1 of a larger initiative: porting shadcn/ui to shadcn-scalajs
(Scala.js + Laminar) more faithfully, removing the remaining basecoat CSS dependency,
and rebuilding the docs site to match the real shadcn/ui site. The full initiative was
decomposed (and approved) into, in order:

1. **Floating/portal primitive + Popover retrofit** (this spec).
2. Full re-port of the 26 basecoat-dependent components (and a fidelity pass on the
   other 34) to real shadcn/ui structure/classes, on top of (1).
3. Delete the basecoat generator/vendor once nothing depends on it
   (`modules/site/scripts/build-basecoat-styles.mjs`, `basecoat.generated.css`,
   `vendor/basecoat-source/`, `vendor/basecoat/`), plus fix the "Basecoat" branding
   text and wrong GitHub/Discord links still present in `Main.scala`'s header.
4. Split `Main.scala` (2,175 lines) into focused files.
5. Rebuild the docs site UX to match shadcn/ui's real IA (sidebar grouping,
   Preview/Code tabs, breadcrumbs, etc.), add copy-to-clipboard on code blocks, and
   add "Copy as Markdown" / `llms.txt` support for AI agents.

### Why this exists

An audit of all 60 `modules/ui` components against `modules/site/src/styles/basecoat.generated.css`
found that 26 of them are genuinely, functionally dependent on basecoat's CSS today —
not vestigial. Real shadcn/ui's equivalents of several of these (`Popover`, `Select`,
`Tooltip`, `DropdownMenu`, `ContextMenu`, `HoverCard`, `Menubar`, `NavigationMenu`,
`Combobox`) are built on Radix primitives backed by floating-ui: viewport-aware
positioning, collision/flip, and portal-to-`<body>` rendering. This codebase has none
of that — `Popover.scala` today is a `<details>`/`<summary>` disclosure with static
`position: absolute; margin-top`, no collision detection, and no portal. Building this
primitive once, before re-porting any of those components, avoids re-solving
positioning per component.

## Goals

- A reusable Laminar-friendly floating-position primitive covering the common
  placement/collision/offset needs of the ~10 upcoming components that need it.
- A portal mechanism so floating content renders as a child of `document.body`
  instead of inline in the trigger's DOM subtree (avoids clipping/stacking issues).
- Prove the primitive works end-to-end by retrofitting `Popover` onto it.

## Non-goals (explicitly deferred)

- **Shadow-DOM-safe portaling** for `modules/webcomponents`' `Sc*` wrappers. Deferred
  to whenever the already-tracked `sc-components.css` wiring work happens (see
  `modules/ui/CLAUDE.md`'s wrapper gap list), since it needs its own design (scoped shadow
  root per floating element + token injection) and isn't needed to unblock the
  docs-site re-port work.
- **CLI npm-dependency support.** Not needed — see Decision below.
- **Full re-port of any component other than `Popover`.** That's sub-project 2.
- **Visual/behavioral polish of `Combobox`/`DatePicker`** beyond the minimum needed
  to keep them compiling and functioning against Popover's new API.

## Key decision: hand-rolled, floating-ui-shaped API

Three options were considered:

- **(A) Wrap `@floating-ui/dom` via a Scala.js facade.** Exact parity with real
  shadcn/ui positioning (same engine Radix uses), but adds a real npm/JS runtime
  dependency — in direct tension with this project's constraint of depending only on
  Tailwind CSS + Laminar. Would also require building CLI npm-dependency support
  (a new `registryDependencies`-like concept for JS packages) as a prerequisite,
  since consumers copying this component via the CLI would need that package
  installed too.
- **(B) Hand-roll positioning math in Scala**, designed however fits Laminar best.
  Zero new dependency, consistent with how this codebase already hand-rolls
  `DropdownMenu`/`ContextMenu` instead of reaching for a JS library.
- **(C, chosen) Hand-roll now, but shape the public API to mirror `@floating-ui/dom`**
  (same placement/middleware naming, same attach/cleanup shape). Gets (B)'s
  zero-dependency benefit today; if the hand-rolled math ever proves insufficient for
  some edge case, swapping in the real library later is an internal implementation
  change, not a break for every component already built on top of it.

Because (C) doesn't introduce a JS dependency, sub-project 2 (CLI npm-dependency
support) is dropped from this initiative's scope entirely unless a real npm package
is introduced later.

## Design

### Module placement

New code lives in `modules/core` (`shadcnscalajs.core.Floating`), alongside the
existing `CommonAttrs`/`Tags` shared utilities — every consumer of this primitive is
a `modules/ui` component, matching how `modules/core` is already used.

### API surface

```scala
enum Placement derives CanEqual:
  case Top, TopStart, TopEnd
  case Right, RightStart, RightEnd
  case Bottom, BottomStart, BottomEnd
  case Left, LeftStart, LeftEnd

final case class FloatingOptions(
  placement: Placement = Placement.Bottom,
  offset: Double = 4,
  flip: Boolean = true,
  shift: Boolean = true,
  viewportPadding: Double = 8
)

final case class FloatingPosition(x: Double, y: Double, resolvedPlacement: Placement)

object Floating:
  // Pure function: reference/viewport rect math, no DOM mutation.
  def computePosition(
    reference: dom.Element,
    floating: dom.Element,
    options: FloatingOptions
  ): FloatingPosition

  // Applies position: fixed + computed coordinates to `floating`, wires window
  // scroll(capture)/resize and a ResizeObserver on `reference` to recompute live.
  // Returns a cleanup closure that removes all listeners.
  def attach(
    reference: dom.Element,
    floating: dom.Element,
    options: FloatingOptions
  ): () => Unit

object Portal:
  // Mounts `content` into document.body via Laminar's DetachedRoot, wired to the
  // owning element's mount/unmount lifecycle so it can't leak past its trigger.
  def apply(content: HtmlElement): Modifier[HtmlElement]
```

`Floating` deliberately does not attempt a one-size-fits-all "floating menu"
abstraction — each consuming component keeps its own open-state `Var`/`EventBus`
(matching the existing `DropdownMenu`/`ContextMenu` pattern) and calls `Floating`
only for positioning/portaling.

Collision handling: when `flip` is enabled and the preferred placement would overflow
the viewport (accounting for `viewportPadding`), try the opposite side first, then the
two adjacent sides, falling back to the original placement if all overflow. When
`shift` is enabled, after placement is resolved, clamp the cross-axis coordinate so
the floating element stays within the viewport minus padding, without changing which
side it's on.

### Popover retrofit

Replace the `<details>`/`<summary>` disclosure with:

- A real `Var[Boolean]` open state.
- Click-to-toggle trigger.
- Content mounted via `Portal`, positioned via `Floating.attach` while open.
- Outside-click-to-close, reusing the `composedPath()` helper already proven in
  `DropdownMenu.scala`/`ContextMenu.scala`.
- Escape-key-to-close.

This changes `Popover`'s public shape. Its two existing consumers, `Combobox` and
`DatePicker`, get the minimal updates needed to keep compiling and working against
the new API — no visual/behavioral re-port of those two beyond that.

### Verification

No component test framework exists yet in this project (`scripts/test` only smoke-tests
the CLI's file-writing) — consistent
with that, this gets verified live in a browser, the same way every other component in
this codebase has been:

- Open/close via click, outside-click, and Escape.
- Placement flips correctly near each viewport edge (top, bottom, left, right).
- Repositions live on scroll and window resize.
- No listener leak: open/close a Popover repeatedly and confirm cleanup actually
  removes the scroll/resize/ResizeObserver listeners (e.g. via a manual counter or
  browser devtools event-listener inspection), not just that it visually closes.
- `Combobox` and `DatePicker` still function end-to-end after the ripple update.

## Known limitations (accepted for now)

- The hand-rolled collision/shift math won't cover every edge case real floating-ui
  handles (nested independently-scrolling containers, RTL flipping, arrow-element
  positioning). Acceptable per the Approach C escape hatch — if a real gap is hit
  later, swapping in `@floating-ui/dom` behind the same API is an internal change.
- Shadow-DOM portaling is out of scope here (see Non-goals) — `Sc*` Web Component
  wrappers for any component built on `Floating` will need follow-up work once
  `sc-components.css` is wired.
