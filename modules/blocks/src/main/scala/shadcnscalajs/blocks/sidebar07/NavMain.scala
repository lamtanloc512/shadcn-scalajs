package shadcnscalajs.blocks.sidebar07

import com.raquo.laminar.api.L.*
import shadcnscalajs.ui.*
import shadcnscalajs.blocks.sidebar07.AppSidebar.NavItem

/** Port of shadcn-svelte `blocks/sidebar-07/components/nav-main.svelte` — the primary nav group, each item expandable
  * into a submenu of sub-items.
  *
  * Upstream merges `Collapsible.Root`'s `data-state`/`group/collapsible` onto the `Sidebar.MenuItem` `li` via a child
  * snippet, and its `Collapsible.Trigger` onto the `Sidebar.MenuButton` the same way, so both end up as attributes on
  * one element each rather than nested wrapper elements. [[shadcnscalajs.ui.Collapsible]] instead always renders its
  * own trigger `<button>`, which would nest a second button inside `Sidebar.menuButton`'s — so the open/closed state
  * and its `data-state`/height-reveal are reproduced directly on the `li` and menu button here instead of reusing that
  * component, matching sidebar-03's `AppSidebar` precedent of hand-building markup when a helper does not fit.
  */
object NavMain:

  private def stateOf(open: Boolean): String = if open then "open" else "closed"

  private def item(navItem: NavItem): HtmlElement =
    val openVar = Var(navItem.isActive)
    li(
      dataAttr("slot") := "sidebar-menu-item",
      dataAttr("sidebar") := "menu-item",
      cls := "group/menu-item group/collapsible relative",
      dataAttr("state") <-- openVar.signal.map(stateOf),
      Sidebar.menuButton()(
        onClick --> { _ => openVar.update(!_) },
        navItem.icon(),
        span(navItem.title),
        Icons.chevronRight(
          svg.cls := "ms-auto transition-transform duration-200 group-data-[state=open]/collapsible:rotate-90"
        )
      ),
      Option.when(navItem.items.nonEmpty)(
        div(
          dataAttr("slot") := "collapsible-content",
          dataAttr("state") <-- openVar.signal.map(stateOf),
          cls := "cn-collapsible-content overflow-hidden transition-[max-height] duration-200 ease-out",
          onMountBind { ctx =>
            val el = ctx.thisNode.ref
            def applyOpen(open: Boolean): Unit =
              if open then
                el.style.maxHeight = s"${el.scrollHeight}px"
                el.removeAttribute("aria-hidden")
              else
                el.style.maxHeight = "0px"
                el.setAttribute("aria-hidden", "true")
            openVar.signal --> { open => applyOpen(open) }
          },
          Sidebar.menuSub(
            navItem.items.map { sub =>
              Sidebar.menuSubItem(
                // `menuSubButton` already renders an `<a>`; setting `href` overrides its `"#"` default rather than
                // nesting a second anchor inside it.
                Sidebar.menuSubButton()(href := sub.url, span(sub.title))
              )
            }
          )
        )
      )
    )

  def apply(items: List[NavItem]): HtmlElement =
    Sidebar.group(
      Sidebar.groupLabel("Platform"),
      Sidebar.menu(items.map(item))
    )
