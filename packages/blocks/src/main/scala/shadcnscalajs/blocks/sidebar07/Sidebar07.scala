package shadcnscalajs.blocks.sidebar07

import com.raquo.laminar.api.L.*
import shadcnscalajs.ui.*

/** Port of shadcn-svelte `blocks/sidebar-07/+page.svelte` — a sidebar that collapses to icons.
  *
  * One `openVar` is shared across the provider, root (`collapsible = Icon`), trigger, and rail, matching sidebar-03's
  * `Sidebar03` precedent for wiring `Sidebar.SidebarContext` through a whole block.
  */
object Sidebar07:

  def apply(): HtmlElement =
    val openVar = Var(true)
    Sidebar.provider(openVar)(
      AppSidebar(openVar)(),
      Sidebar.inset(
        headerTag(
          cls := "flex h-16 shrink-0 items-center gap-2 transition-[width,height] ease-linear group-has-data-[collapsible=icon]/sidebar-wrapper:h-12",
          div(
            cls := "flex items-center gap-2 px-4",
            Sidebar.trigger(openVar)(cls := "-ms-1"),
            Separator(Separator.Orientation.Vertical, cls := "me-2 h-4"),
            Breadcrumb(
              Breadcrumb.list(
                Breadcrumb.item(cls := "hidden md:block", Breadcrumb.link("#", "Build Your Application")),
                Breadcrumb.separator(cls := "hidden md:block"),
                Breadcrumb.item(Breadcrumb.page("Data Fetching"))
              )
            )
          )
        ),
        div(
          cls := "flex flex-1 flex-col gap-4 p-4 pt-0",
          div(
            cls := "grid auto-rows-min gap-4 md:grid-cols-3",
            div(cls := "aspect-video rounded-xl bg-muted/50"),
            div(cls := "aspect-video rounded-xl bg-muted/50"),
            div(cls := "aspect-video rounded-xl bg-muted/50")
          ),
          div(cls := "min-h-screen flex-1 rounded-xl bg-muted/50 md:min-h-min")
        )
      )
    )
