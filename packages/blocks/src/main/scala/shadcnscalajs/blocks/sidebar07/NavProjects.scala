package shadcnscalajs.blocks.sidebar07

import com.raquo.laminar.api.L.*
import shadcnscalajs.ui.*
import shadcnscalajs.blocks.sidebar07.AppSidebar.Project

/** Port of shadcn-svelte `blocks/sidebar-07/components/nav-projects.svelte` — a project list hidden once the sidebar
  * collapses to icons, each row with a hover-revealed action menu.
  */
object NavProjects:

  // `Sidebar.menuAction`'s classes, duplicated for the same reason as `TeamSwitcher.triggerClass`: `DropdownMenu`
  // renders its own trigger `<button>`, so the action button's look is supplied as modifiers rather than by nesting
  // a second, pre-built button inside it. The position/reveal-on-hover classes live on this *wrapper*, not the
  // button, because `DropdownMenu`'s own root is `position: relative` (see `.dropdown-menu` in the generated CSS),
  // which would otherwise become the nearest positioned ancestor and anchor the button to itself instead of to
  // this menu item — collapsing it into the row's inline flow instead of floating it to the top-right corner.
  private val actionWrapperClass =
    "absolute top-1.5 right-1 peer-data-[size=default]/menu-button:top-2 peer-data-[size=lg]/menu-button:top-2.5 peer-data-[size=sm]/menu-button:top-1 group-data-[collapsible=icon]:hidden group-focus-within/menu-item:opacity-100 group-hover/menu-item:opacity-100 peer-data-active/menu-button:text-sidebar-accent-foreground md:opacity-0 data-open:opacity-100"

  private val actionTriggerClass =
    "flex aspect-square w-5 items-center justify-center rounded-md p-0 text-sidebar-foreground outline-hidden ring-sidebar-ring transition-transform after:absolute after:-inset-2 md:after:hidden hover:bg-sidebar-accent hover:text-sidebar-accent-foreground focus-visible:ring-2 [&>svg]:size-4 [&>svg]:shrink-0"

  private def projectItem(project: Project): HtmlElement =
    li(
      dataAttr("slot") := "sidebar-menu-item",
      dataAttr("sidebar") := "menu-item",
      cls := "group/menu-item relative",
      Sidebar.menuButtonLink()(href := project.url, project.icon(), span(project.name)),
      div(
        cls := actionWrapperClass,
        DropdownMenu.itemsWithTrigger(
          Seq(
            dataAttr("slot") := "sidebar-menu-action",
            dataAttr("sidebar") := "menu-action",
            cls := actionTriggerClass
          )
        )(
          Icons.moreHorizontal(),
          span(cls := "sr-only", "More")
        ) { ctx =>
          Seq(
            ctx.item(Icons.folder(svg.cls := "text-muted-foreground"), span("View Project")),
            ctx.item(Icons.forward(svg.cls := "text-muted-foreground"), span("Share Project")),
            ctx.separator(),
            ctx.item(Icons.trash2(svg.cls := "text-muted-foreground"), span("Delete Project"))
          )
        }
      )
    )

  def apply(projects: List[Project]): HtmlElement =
    Sidebar.group(
      cls := "group-data-[collapsible=icon]:hidden",
      Sidebar.groupLabel("Projects"),
      Sidebar.menu(
        projects.map(projectItem),
        Sidebar.menuItem(
          cls := "text-sidebar-foreground/70",
          Icons.moreHorizontal(svg.cls := "text-sidebar-foreground/70"),
          span("More")
        )
      )
    )
