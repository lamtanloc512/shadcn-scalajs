package shadcnscalajs.blocks.dashboard01

import com.raquo.laminar.api.L.*
import shadcnscalajs.ui.*
import shadcnscalajs.blocks.dashboard01.AppSidebar.UserInfo

/** Port of shadcn-svelte `blocks/dashboard-01/components/nav-user.svelte` — the account menu pinned to the sidebar
  * footer. Structurally the same component as `sidebar07.NavUser`; upstream has no avatar image asset here either, so
  * only the initials fallback is rendered.
  */
object NavUser:

  private val triggerClass =
    "cn-sidebar-menu-button peer/menu-button group/menu-button flex w-full items-center overflow-hidden outline-hidden disabled:pointer-events-none disabled:opacity-50 aria-disabled:pointer-events-none aria-disabled:opacity-50 [&_svg]:size-4 [&_svg]:shrink-0 [&>span:last-child]:truncate cn-sidebar-menu-button-variant-default cn-sidebar-menu-button-size-lg data-[state=open]:bg-sidebar-accent data-[state=open]:text-sidebar-accent-foreground"

  private def identity(user: UserInfo, avatarMods: Modifier[HtmlElement]*): Modifier[HtmlElement] =
    Seq(
      Avatar(Avatar.fallback(cls := "rounded-lg", user.initials), cls := "size-8 rounded-lg", avatarMods),
      div(
        cls := "grid flex-1 text-start text-sm leading-tight",
        span(cls := "truncate font-medium", user.name),
        span(cls := "truncate text-xs text-muted-foreground", user.email)
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
          identity(user, cls := "grayscale"),
          // Upstream uses a vertical three-dot icon here; the closest available concept is the horizontal variant.
          Icons.moreHorizontal(svg.cls := "ms-auto size-4")
        ) { ctx =>
          Seq(
            ctx.label(
              cls := "p-0 font-normal",
              div(cls := "flex items-center gap-2 px-1 py-1.5 text-start text-sm", identity(user))
            ),
            ctx.separator(),
            // Upstream's `UserCircleIcon` (Account) has no equivalent concept registered; `user` is the closest.
            ctx.group(
              ctx.item(Icons.user(), span("Account")),
              ctx.item(Icons.creditCard(), span("Billing")),
              ctx.item(Icons.bell(), span("Notifications"))
            ),
            ctx.separator(),
            ctx.item(Icons.logOut(), span("Log out"))
          )
        }
      )
    )
