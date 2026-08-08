# shadcn-svelte fidelity for the Laminar component library — design

Status: Accepted
Reference: `/Users/elam/Personal/shadcn-svelte/docs/src/lib/registry/ui`

## Goal

Every component in `modules/ui` renders the same DOM contract and the same visuals as its shadcn-svelte
counterpart, implemented in pure Laminar (no bits-ui, TanStack, svelte-sonner, or native-element stand-ins
for components upstream builds itself).

## Why the site looks different today

Three systemic causes, in order of impact:

1. **Missing `data-slot` attributes.** Upstream sets `data-slot` on all 255 part files. We set it in 13 of 64
   components. The style packs in `shadcn-presets.generated.css` and upstream's own compositional selectors
   (`[&>[data-slot]]:rounded-r-none` for button groups, `*:data-[slot=card-header]`, input-group, field,
   sidebar) match on it, so those rules never apply to our markup.
2. **Missing parts.** `Breadcrumb` has no `page`/`ellipsis` and separates with a literal `/` instead of a
   chevron; `ButtonGroup` has no `text`/`separator`; `Avatar` has no `badge`/`group`/`groupCount` or size
   variants; `Table` has no `caption`; `Alert` has no `action`; `NativeSelect` has no `option`/`optGroup`.
3. **Native-element stand-ins.** `Popover` and `Collapsible` are `<details>`/`<summary>`, `Tooltip` is a
   `title` attribute, `HoverCard` is CSS `group-hover`, `Select` is a native `<select>`, `ScrollArea` is a
   plain overflow div. Upstream anchors, portals, and animates these with `data-state`.

A fourth, narrower gap: `DropdownMenu`, `ContextMenu`, and `Menubar` take an `Item(label, onSelect)` data
model, so upstream's submenus, radio groups, shortcuts, and icon items cannot be expressed at all.

## Conventions

- **`data-slot` is mandatory** on every element upstream marks, using upstream's exact value.
- **Class strings carry standalone utilities plus the `cn-*` hook**, as `Alert.scala` already documents: the
  CLI copies `modules/ui` into projects that have no pack CSS, so utilities must render correctly alone.
  Packs are unlayered and override them where a pack defines the same property — that is by design.
- **Upstream's class list is the source of truth** for ordering and for which utilities exist at all. Where
  upstream omits a utility because a pack owns it, keep a standalone equivalent matching the `vega` pack.
- **No defaulted parameter before a varargs `mods`** — it makes overload resolution ambiguous (the
  `ToggleGroup.Item` and `Calendar.range` regressions). Write explicit overloads.
- **Icon placeholders map to `Icons`**, which is already library-aware, matching upstream `IconPlaceholder`.
- **Public signatures stay source-compatible** where call sites exist in `modules/site`, `modules/blocks`, or
  `modules/webcomponents`; add overloads rather than changing arity.

## Waves

1. **Structural parity.** `data-slot` sweep across all components, plus the missing parts listed above.
   No behavior change, no call-site churn.
2. **Real behavior.** `Popover`, `Collapsible`, `Tooltip`, `HoverCard`, `ScrollArea` become anchored,
   `data-state`-driven components instead of native-element stand-ins.
3. **Composable menus.** A shared floating primitive, then `DropdownMenu` with upstream's parts
   (content/item/checkbox-item/radio-item/sub/label/separator/shortcut/group), then `ContextMenu` and
   `Menubar` on top of it. The existing data-model API stays as a thin compatibility layer so the
   preview-02 cards and `DataTable` keep compiling.
4. **Remaining components.** `Select` as a real listbox, `InputOTP`, `Pagination`, `Carousel` parts,
   `Dialog`/`Sheet`/`Drawer`/`AlertDialog` parts and animations, `Form`, `Command`.
5. **The site itself.** Docs pages composed from our own components instead of hand-rolled class strings,
   and docs chrome/typography matched to the shadcn-svelte docs.

## Done criteria

- Every upstream part exists with upstream's `data-slot` value and class list.
- `sbt compile` green across `ui`, `blocks`, `webcomponents`, `site`; `scalafmtAll` clean.
- Switching style packs visibly changes every component, since pack selectors now match.
- No remaining component implemented as `<details>`, `title=`, or a native `<select>` where upstream builds
  its own.
