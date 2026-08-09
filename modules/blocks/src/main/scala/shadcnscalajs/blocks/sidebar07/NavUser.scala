package shadcnscalajs.blocks.sidebar07

import com.raquo.laminar.api.L.*
import shadcnscalajs.ui.*
import shadcnscalajs.blocks.sidebar07.AppSidebar.UserInfo

/** Port of shadcn-svelte `blocks/sidebar-07/components/nav-user.svelte` — the account menu pinned to the sidebar
  * footer.
  */
object NavUser:

  // Duplicated for the same reason as `TeamSwitcher.triggerClass`.
  private val triggerClass =
    "cn-sidebar-menu-button peer/menu-button group/menu-button flex w-full items-center overflow-hidden outline-hidden disabled:pointer-events-none disabled:opacity-50 aria-disabled:pointer-events-none aria-disabled:opacity-50 [&_svg]:size-4 [&_svg]:shrink-0 [&>span:last-child]:truncate cn-sidebar-menu-button-variant-default cn-sidebar-menu-button-size-lg data-open:bg-sidebar-accent data-open:text-sidebar-accent-foreground"

  private def identity(user: UserInfo): Modifier[HtmlElement] =
    Seq(
      Avatar(Avatar.fallback(cls := "rounded-lg", user.initials), cls := "size-8 rounded-lg"),
      div(
        cls := "grid flex-1 text-start text-sm leading-tight",
        span(cls := "truncate font-medium", user.name),
        span(cls := "truncate text-xs", user.email)
      )
    )

  def apply(user: UserInfo): HtmlElement =
    Sidebar.menu(
      li(
        dataAttr("slot") := "sidebar-menu-item",
        dataAttr("sidebar") := "menu-item",
        cls := "group/menu-item relative",
        DropdownMenu.itemsWithTrigger(
          Seq(
            dataAttr("slot") := "sidebar-menu-button",
            dataAttr("sidebar") := "menu-button",
            dataAttr("variant") := "default",
            dataAttr("size") := "lg",
            cls := triggerClass
          ),
          DropdownMenu.Align.End,
          wrapperStyle = cls := "w-full"
        )(
          identity(user),
          Icons.chevronsUpDown(svg.cls := "ms-auto size-4")
        ) { ctx =>
          Seq(
            ctx.label(
              cls := "p-0 font-normal",
              div(cls := "flex items-center gap-2 px-1 py-1.5 text-start text-sm", identity(user))
            ),
            ctx.separator(),
            ctx.group(ctx.item(Icons.sparkles(), "Upgrade to Pro")),
            ctx.separator(),
            ctx.group(
              ctx.item(Icons.badgeCheck(), "Account"),
              ctx.item(Icons.creditCard(), "Billing"),
              ctx.item(Icons.bell(), "Notifications")
            ),
            ctx.separator(),
            ctx.item(Icons.logOut(), "Log out")
          )
        }
      )
    )
