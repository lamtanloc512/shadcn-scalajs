package shadcnscalajs.blocks.dashboard01

import com.raquo.laminar.api.L.*
import shadcnscalajs.ui.*
import shadcnscalajs.blocks.dashboard01.AppSidebar.NavItem

/** Port of shadcn-svelte `blocks/dashboard-01/components/nav-main.svelte` — the "Quick Create" action row plus the
  * primary nav list.
  *
  * The first row hosts a `Sidebar.MenuButton` *and* a sibling icon `Button` inside one menu item. `Sidebar.menuItem`
  * unconditionally wraps its whole argument list in a second `menuButton`, so it cannot host that pair; the `li` is
  * hand-built instead, matching `sidebar07.NavProjects`'s precedent.
  */
object NavMain:

  def apply(items: List[NavItem]): HtmlElement =
    Sidebar.group(
      Sidebar.groupContent(
        cls := "flex flex-col gap-2",
        Sidebar.menu(
          li(
            dataAttr("slot") := "sidebar-menu-item",
            dataAttr("sidebar") := "menu-item",
            cls := "group/menu-item relative flex items-center gap-2",
            Sidebar.menuButton()(
              cls := "min-w-8 bg-primary text-primary-foreground duration-200 ease-linear hover:bg-primary/90 hover:text-primary-foreground active:bg-primary/90 active:text-primary-foreground",
              Icons.circlePlus(),
              span("Quick Create")
            ),
            // Upstream's `size="icon"` plus a `class="size-8 ..."` override collapse to `size-8`; `IconSm` already
            // renders that class natively, so no conflicting `size-9`/`size-8` pair ends up in the class list.
            Button.of(
              _.variant(Button.Variant.Outline),
              _.size(Button.Size.IconSm),
              _ => cls := "group-data-[collapsible=icon]:opacity-0",
              _ => Icons.messageSquare(),
              _ => span(cls := "sr-only", "Inbox")
            )
          )
        ),
        // No sibling button here, so `menuItem`'s built-in wrap-in-a-`menuButton` is exactly upstream's shape.
        Sidebar.menu(items.map(item => Sidebar.menuItem(item.icon(), span(item.title))))
      )
    )
