package shadcnscalajs.blocks.dashboard01

import com.raquo.laminar.api.L.*
import shadcnscalajs.ui.*
import shadcnscalajs.blocks.dashboard01.AppSidebar.DocItem

/** Port of shadcn-svelte `blocks/dashboard-01/components/nav-documents.svelte` — a document list hidden once the
  * sidebar collapses to icons, each row with a hover-revealed action menu.
  *
  * Upstream flips the action menu's `side`/`align` on `sidebar.useSidebar().isMobile`; that per-instance mobile query
  * is not exposed by `shadcnscalajs.ui.Sidebar`, so — matching `sidebar07.NavProjects`'s precedent — the menu just uses
  * `DropdownMenu`'s default placement.
  */
object NavDocuments:

  // Position/reveal-on-hover classes live on this *wrapper*, not the button: `DropdownMenu`'s root is
  // `position: relative`, which would otherwise become the nearest positioned ancestor and pin the action into the
  // row's inline flow instead of floating it to the top-right corner. Same structure as `sidebar07.NavProjects`.
  private val actionWrapperClass =
    "absolute top-1.5 right-1 peer-data-[size=default]/menu-button:top-2 peer-data-[size=lg]/menu-button:top-2.5 peer-data-[size=sm]/menu-button:top-1 group-data-[collapsible=icon]:hidden group-focus-within/menu-item:opacity-100 group-hover/menu-item:opacity-100 peer-data-active/menu-button:text-sidebar-accent-foreground md:opacity-0 data-open:opacity-100"

  private val actionTriggerClass =
    "flex aspect-square w-5 items-center justify-center rounded-md p-0 text-sidebar-foreground outline-hidden ring-sidebar-ring transition-transform after:absolute after:-inset-2 md:after:hidden hover:bg-sidebar-accent hover:text-sidebar-accent-foreground focus-visible:ring-2 [&>svg]:size-4 [&>svg]:shrink-0"

  private def documentItem(item: DocItem): HtmlElement =
    li(
      dataAttr("slot") := "sidebar-menu-item",
      dataAttr("sidebar") := "menu-item",
      cls := "group/menu-item relative",
      Sidebar.menuButtonLink()(href := item.url, item.icon(), span(item.name)),
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
            ctx.item(Icons.folder(svg.cls := "text-muted-foreground"), span("Open")),
            ctx.item(Icons.forward(svg.cls := "text-muted-foreground"), span("Share")),
            ctx.separator(),
            ctx.item(Menu.destructive, Icons.trash2(svg.cls := "text-muted-foreground"), span("Delete"))
          )
        }
      )
    )

  def apply(items: List[DocItem]): HtmlElement =
    Sidebar.group(
      cls := "group-data-[collapsible=icon]:hidden",
      Sidebar.groupLabel("Documents"),
      Sidebar.menu(
        items.map(documentItem),
        Sidebar.menuItem(
          cls := "text-sidebar-foreground/70",
          Icons.moreHorizontal(svg.cls := "text-sidebar-foreground/70"),
          span("More")
        )
      )
    )
