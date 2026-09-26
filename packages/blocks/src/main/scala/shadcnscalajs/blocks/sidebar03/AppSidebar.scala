package shadcnscalajs.blocks.sidebar03

import com.raquo.laminar.api.L.*
import shadcnscalajs.ui.*

/** Port of shadcn-svelte `blocks/sidebar-03/components/app-sidebar.svelte` — a sidebar with nested submenus. */
object AppSidebar:

  final case class SubItem(title: String, url: String, isActive: Boolean = false)
  final case class NavItem(title: String, url: String, items: List[SubItem] = Nil)

  private val navMain: List[NavItem] = List(
    NavItem(
      "Getting Started",
      "#",
      List(
        SubItem("Installation", "#"),
        SubItem("Project Structure", "#")
      )
    ),
    NavItem(
      "Build Your Application",
      "#",
      List(
        SubItem("Routing", "#"),
        SubItem("Data Fetching", "#", isActive = true),
        SubItem("Rendering", "#"),
        SubItem("Caching", "#"),
        SubItem("Styling", "#"),
        SubItem("Optimizing", "#"),
        SubItem("Configuring", "#"),
        SubItem("Testing", "#"),
        SubItem("Authentication", "#"),
        SubItem("Deploying", "#"),
        SubItem("Upgrading", "#"),
        SubItem("Examples", "#")
      )
    ),
    NavItem(
      "API Reference",
      "#",
      List(
        SubItem("Components", "#"),
        SubItem("File Conventions", "#"),
        SubItem("Functions", "#"),
        SubItem("next.config.js Options", "#"),
        SubItem("CLI", "#"),
        SubItem("Edge Runtime", "#")
      )
    ),
    NavItem(
      "Architecture",
      "#",
      List(
        SubItem("Accessibility", "#"),
        SubItem("Fast Refresh", "#"),
        SubItem("Next.js Compiler", "#"),
        SubItem("Supported Browsers", "#"),
        SubItem("Turbopack", "#")
      )
    ),
    NavItem(
      "Community",
      "#",
      List(
        SubItem("Contribution Guide", "#")
      )
    )
  )

  // `Sidebar.menuItem` (the back-compat helper) always wraps its whole argument list inside a *second*,
  // default-sized `menuButton`, so it cannot host an already-built button (e.g. the `Lg` brand button) or a
  // button plus a sibling `menuSub`. Build the `li` manually instead, matching the markup `Sidebar.menuItem`
  // itself emits (see `site.Main`'s sidebar preview for precedent).
  private def menuItem(mods: Modifier[HtmlElement]*): HtmlElement =
    li(
      dataAttr("slot") := "sidebar-menu-item",
      dataAttr("sidebar") := "menu-item",
      cls := "group/menu-item relative",
      mods
    )

  def apply(openVar: Var[Boolean])(mods: Modifier[HtmlElement]*): HtmlElement =
    Sidebar.root(collapsible = Sidebar.Collapsible.Offcanvas, openVar = openVar)(
      mods,
      Sidebar.header(
        Sidebar.menu(
          menuItem(
            Sidebar.menuButtonLink(size = Sidebar.MenuButtonSize.Lg)(
              href := "#",
              div(
                cls := "flex aspect-square size-8 items-center justify-center rounded-lg bg-sidebar-primary text-sidebar-primary-foreground",
                Icons.galleryVerticalEnd(svg.cls := "size-4")
              ),
              div(
                cls := "flex flex-col gap-0.5 leading-none",
                span(cls := "font-medium", "Documentation"),
                span("v1.0.0")
              )
            )
          )
        )
      ),
      Sidebar.content(
        Sidebar.group(
          Sidebar.menu(
            navMain.map { item =>
              menuItem(
                Sidebar.menuButtonLink()(cls := "font-medium", href := item.url, item.title),
                Option.when(item.items.nonEmpty)(
                  Sidebar.menuSub(
                    item.items.map { sub =>
                      Sidebar.menuSubItem(
                        Sidebar.menuSubButton(isActive = sub.isActive)(href := sub.url, sub.title)
                      )
                    }
                  )
                )
              )
            }
          )
        )
      )
    )
