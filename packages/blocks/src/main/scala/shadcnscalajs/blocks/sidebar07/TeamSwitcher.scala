package shadcnscalajs.blocks.sidebar07

import com.raquo.laminar.api.L.*
import shadcnscalajs.ui.*
import shadcnscalajs.blocks.sidebar07.AppSidebar.Team

/** Port of shadcn-svelte `blocks/sidebar-07/components/team-switcher.svelte` — a dropdown menu that switches the
  * sidebar header's active team.
  */
object TeamSwitcher:

  // `Sidebar.menuButtonOf` builds a whole `<button>`, so it cannot supply the trigger for a `DropdownMenu` (which
  // renders its own `<button>`) without nesting two buttons. The `size="lg"` menu-button classes are duplicated here
  // instead, matching the `li` reconstruction sidebar-03's `AppSidebar` already uses for the same reason.
  private val triggerClass =
    "cn-sidebar-menu-button peer/menu-button group/menu-button flex w-full items-center overflow-hidden outline-hidden disabled:pointer-events-none disabled:opacity-50 aria-disabled:pointer-events-none aria-disabled:opacity-50 [&_svg]:size-4 [&_svg]:shrink-0 [&>span:last-child]:truncate cn-sidebar-menu-button-variant-default cn-sidebar-menu-button-size-lg data-open:bg-sidebar-accent data-open:text-sidebar-accent-foreground"

  def apply(teams: List[Team]): HtmlElement =
    val activeTeamVar = Var(teams.head)
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
          wrapperStyle = cls := "w-full"
        )(
          child <-- activeTeamVar.signal.map { team =>
            div(
              cls := "contents",
              div(
                cls := "flex aspect-square size-8 items-center justify-center rounded-lg bg-sidebar-primary text-sidebar-primary-foreground",
                team.logo()
              ),
              div(
                cls := "grid flex-1 text-start text-sm leading-tight",
                span(cls := "truncate font-medium", team.name),
                span(cls := "truncate text-xs", team.plan)
              )
            )
          },
          Icons.chevronsUpDown(svg.cls := "ms-auto")
        ) { ctx =>
          Seq(
            ctx.label(cls := "text-xs text-muted-foreground", "Teams"),
            teams.zipWithIndex.map { case (team, index) =>
              ctx.item(
                () => activeTeamVar.set(team),
                cls := "gap-2 p-2",
                div(cls := "flex size-6 items-center justify-center rounded-md border", team.logo()),
                team.name,
                ctx.shortcut(s"⌘${index + 1}")
              )
            },
            ctx.separator(),
            ctx.item(
              cls := "gap-2 p-2",
              div(
                cls := "flex size-6 items-center justify-center rounded-md border bg-transparent",
                Icons.plus(svg.cls := "size-4")
              ),
              div(cls := "font-medium text-muted-foreground", "Add team")
            )
          )
        }
      )
    )
