package shadcnscalajs.blocks.sidebar07

import com.raquo.laminar.api.L.*
import shadcnscalajs.ui.*

/** Port of shadcn-svelte `blocks/sidebar-07/components/app-sidebar.svelte` — a sidebar that collapses to icons. */
object AppSidebar:

  final case class Team(name: String, plan: String, logo: () => SvgElement)
  final case class SubNavItem(title: String, url: String)
  final case class NavItem(
      title: String,
      url: String,
      icon: () => SvgElement,
      isActive: Boolean = false,
      items: List[SubNavItem] = Nil
  )
  final case class Project(name: String, url: String, icon: () => SvgElement)
  final case class UserInfo(name: String, email: String, initials: String)

  private val teams: List[Team] = List(
    Team("Acme Inc", "Enterprise", () => Icons.galleryVerticalEnd(svg.cls := "size-4")),
    Team("Acme Corp.", "Startup", () => Icons.audioLines(svg.cls := "size-4")),
    Team("Evil Corp.", "Free", () => Icons.command(svg.cls := "size-4"))
  )

  private val user: UserInfo = UserInfo("shadcn", "m@example.com", "CN")

  private val navMain: List[NavItem] = List(
    NavItem(
      "Playground",
      "#",
      () => Icons.squareTerminal(svg.cls := "size-4"),
      isActive = true,
      items = List(
        SubNavItem("History", "#"),
        SubNavItem("Starred", "#"),
        SubNavItem("Settings", "#")
      )
    ),
    NavItem(
      "Models",
      "#",
      () => Icons.bot(svg.cls := "size-4"),
      items = List(
        SubNavItem("Genesis", "#"),
        SubNavItem("Explorer", "#"),
        SubNavItem("Quantum", "#")
      )
    ),
    NavItem(
      "Documentation",
      "#",
      () => Icons.bookOpen(svg.cls := "size-4"),
      items = List(
        SubNavItem("Introduction", "#"),
        SubNavItem("Get Started", "#"),
        SubNavItem("Tutorials", "#"),
        SubNavItem("Changelog", "#")
      )
    ),
    NavItem(
      "Settings",
      "#",
      () => Icons.settings2(svg.cls := "size-4"),
      items = List(
        SubNavItem("General", "#"),
        SubNavItem("Team", "#"),
        SubNavItem("Billing", "#"),
        SubNavItem("Limits", "#")
      )
    )
  )

  private val projects: List[Project] = List(
    Project("Design Engineering", "#", () => Icons.frame(svg.cls := "size-4")),
    Project("Sales & Marketing", "#", () => Icons.pieChart(svg.cls := "size-4")),
    Project("Travel", "#", () => Icons.map(svg.cls := "size-4"))
  )

  def apply(openVar: Var[Boolean])(mods: Modifier[HtmlElement]*): HtmlElement =
    Sidebar.root(collapsible = Sidebar.Collapsible.Icon, openVar = openVar)(
      mods,
      Sidebar.header(TeamSwitcher(teams)),
      Sidebar.content(
        NavMain(navMain),
        NavProjects(projects)
      ),
      Sidebar.footer(NavUser(user))
    )
