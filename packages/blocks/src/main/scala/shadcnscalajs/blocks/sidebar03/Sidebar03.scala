package shadcnscalajs.blocks.sidebar03

import com.raquo.laminar.api.L.*
import shadcnscalajs.ui.*

/** Port of shadcn-svelte `blocks/sidebar-03/+page.svelte` — a sidebar with nested submenus. */
object Sidebar03:

  def apply(): HtmlElement =
    val openVar = Var(true)
    Sidebar.provider(openVar)(
      AppSidebar(openVar)(),
      Sidebar.inset(
        headerTag(
          cls := "flex h-16 shrink-0 items-center gap-2 border-b",
          div(
            cls := "flex items-center gap-2 px-3",
            Sidebar.trigger(openVar)(),
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
          cls := "flex flex-1 flex-col gap-4 p-4",
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
