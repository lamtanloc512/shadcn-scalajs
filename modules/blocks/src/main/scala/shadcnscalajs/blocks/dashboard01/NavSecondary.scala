package shadcnscalajs.blocks.dashboard01

import com.raquo.laminar.api.L.*
import shadcnscalajs.ui.*
import shadcnscalajs.blocks.dashboard01.AppSidebar.NavItem

/** Port of shadcn-svelte `blocks/dashboard-01/components/nav-secondary.svelte` — a small nav group pinned to the bottom
  * of the sidebar content via the caller's `mt-auto`.
  */
object NavSecondary:

  def apply(items: List[NavItem], mods: Modifier[HtmlElement]*): HtmlElement =
    Sidebar.group(
      mods,
      Sidebar.groupContent(
        Sidebar.menu(
          items.map { item =>
            // `menuButtonLink` renders the anchor itself; nesting it inside `menuItem`'s built-in `menuButton` would
            // put an `<a>` inside a `<button>`, so the `li` is hand-built instead.
            li(
              dataAttr("slot") := "sidebar-menu-item",
              dataAttr("sidebar") := "menu-item",
              cls := "group/menu-item relative",
              Sidebar.menuButtonLink()(href := item.url, item.icon(), span(item.title))
            )
          }
        )
      )
    )
