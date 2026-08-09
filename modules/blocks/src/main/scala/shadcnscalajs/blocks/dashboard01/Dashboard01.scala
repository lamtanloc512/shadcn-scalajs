package shadcnscalajs.blocks.dashboard01

import com.raquo.laminar.api.L.*
import shadcnscalajs.ui.{Sidebar, *}

/** Port of shadcn-svelte `blocks/dashboard-01/+page.svelte` — the inset-sidebar analytics dashboard: an icon-collapsing
  * sidebar, a sticky site header, four KPI cards, an interactive chart, and (landing in Task 8) a data table.
  *
  * One `openVar` is shared across the provider, root (`collapsible = Icon`), trigger, and rail, matching
  * `sidebar07.Sidebar07`'s precedent for wiring `Sidebar.SidebarContext` through a whole block.
  *
  * Upstream sets `collapsible="offcanvas"` on `app-sidebar.svelte` itself (only `variant="inset"` comes from the page);
  * per this wave's plan (`docs/superpowers/plans/2026-08-09-featured-blocks.md`, Task 6, Step 2) `AppSidebar` uses
  * `Collapsible.Icon` instead, matching the canonical shadcn dashboard's icon-rail collapse behavior.
  */
object Dashboard01:

  def apply(): HtmlElement =
    val openVar = Var(true)
    Sidebar.provider(openVar)(
      // `Sidebar.provider` hardcodes `--sidebar-width: 16rem; --sidebar-width-icon: 3rem;` as an inline `styleAttr`.
      // Laminar's `styleAttr :=` replaces the whole attribute value rather than merging individual properties (unlike
      // `cls :=`, which is additive), so overriding it here has to restate `--sidebar-width-icon` too — dropping it
      // would break the icon-collapsed rail width, which several `cn-sidebar-*` classes read via `var(...)`. This
      // mod is applied after `provider`'s own `styleAttr :=` (it comes later in the `div(...)` argument list), so it
      // wins outright; no change to `modules/ui`'s `Sidebar.provider` signature was needed.
      styleAttr := "--sidebar-width: calc(var(--spacing) * 72); --sidebar-width-icon: 3rem; --header-height: calc(var(--spacing) * 12);",
      AppSidebar(openVar)(),
      Sidebar.inset(
        SiteHeader(openVar),
        div(
          cls := "flex flex-1 flex-col",
          div(
            cls := "@container/main flex flex-1 flex-col gap-2",
            div(
              cls := "flex flex-col gap-4 py-4 md:gap-6 md:py-6",
              SectionCards(),
              div(cls := "px-4 lg:px-6", ChartAreaInteractive()),
              shadcnscalajs.blocks.dashboard01.DataTable()
            )
          )
        )
      )
    )
