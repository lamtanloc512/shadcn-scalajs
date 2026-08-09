package shadcnscalajs.blocks.dashboard01

import com.raquo.laminar.api.L.*
import shadcnscalajs.ui.*

/** Port of shadcn-svelte `blocks/dashboard-01/components/app-sidebar.svelte` — the inset sidebar shell for the
  * dashboard: brand header, primary nav, documents list, secondary nav pinned to the bottom, and the account menu.
  *
  * Upstream's `data.navClouds` is defined but never passed to any child component (dead data in the source file
  * itself), so it is not ported here either.
  */
object AppSidebar:

  final case class NavItem(title: String, url: String, icon: () => SvgElement)
  final case class DocItem(name: String, url: String, icon: () => SvgElement)
  final case class UserInfo(name: String, email: String, initials: String)

  // Upstream icons with no equivalent concept registered across all five `Icons` libraries are substituted with the
  // closest available concept (documented in task-6-report.md) rather than hand-adding new per-library SVG path data,
  // which is out of this task's scope (see Task 1, which added exactly one new concept, `gallery-vertical-end`).
  private val user: UserInfo = UserInfo("shadcn", "m@example.com", "CN")

  private val navMain: List[NavItem] = List(
    NavItem("Dashboard", "#", () => Icons.layoutDashboard()),
    NavItem("Lifecycle", "#", () => Icons.fileText()),
    NavItem("Analytics", "#", () => Icons.activity()),
    NavItem("Projects", "#", () => Icons.folder()),
    NavItem("Team", "#", () => Icons.user())
  )

  private val navSecondary: List[NavItem] = List(
    NavItem("Settings", "#", () => Icons.settings2()),
    NavItem("Get Help", "#", () => Icons.circleHelp()),
    NavItem("Search", "#", () => Icons.search())
  )

  private val documents: List[DocItem] = List(
    DocItem("Data Library", "#", () => Icons.cloud()),
    DocItem("Reports", "#", () => Icons.fileBarChart()),
    DocItem("Word Assistant", "#", () => Icons.bookOpen())
  )

  private def brandItem: HtmlElement =
    li(
      dataAttr("slot") := "sidebar-menu-item",
      dataAttr("sidebar") := "menu-item",
      cls := "group/menu-item relative",
      // Upstream spreads the menu-button props onto an `<a>` via a child snippet; `menuButtonLink` reproduces that
      // without nesting an anchor inside `menuButton`'s `<button>`.
      Sidebar.menuButtonLink()(
        cls := "data-[slot=sidebar-menu-button]:!p-1.5",
        href := "#",
        Icons.command(svg.cls := "!size-5"),
        span(cls := "text-base font-semibold", "Acme Inc.")
      )
    )

  def apply(openVar: Var[Boolean])(mods: Modifier[HtmlElement]*): HtmlElement =
    Sidebar.root(variant = Sidebar.Variant.Inset, collapsible = Sidebar.Collapsible.Icon, openVar = openVar)(
      mods,
      Sidebar.header(Sidebar.menu(brandItem)),
      Sidebar.content(
        NavMain(navMain),
        NavDocuments(documents),
        NavSecondary(navSecondary, cls := "mt-auto")
      ),
      Sidebar.footer(NavUser(user))
    )
