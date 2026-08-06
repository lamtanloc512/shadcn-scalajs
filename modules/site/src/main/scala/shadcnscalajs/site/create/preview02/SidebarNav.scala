package shadcnscalajs.site.create.preview02

import com.raquo.laminar.api.L.*
import shadcnscalajs.ui.*

/** preview-02 mosaic card — port of
  * `/Users/elam/Personal/shadcn-svelte/docs/src/lib/registry/examples/create/preview-02/cards/sidebar-nav.svelte`
  */
object SidebarNav:

  private def menuEntry(label: String, icon: SvgElement, isActive: Boolean = false): HtmlElement =
    li(
      dataAttr("slot") := "sidebar-menu-item",
      dataAttr("sidebar") := "menu-item",
      cls := "group/menu-item relative",
      Sidebar.menuButton(isActive = isActive)(icon, label)
    )

  private def sidebarGroup(label: String, groupCls: String)(entries: (String, SvgElement, Boolean)*): HtmlElement =
    Sidebar.group(
      cls := groupCls,
      Sidebar.groupLabel(label),
      Sidebar.groupContent(
        Sidebar.menu(entries.map { case (entryLabel, icon, active) => menuEntry(entryLabel, icon, active) }.toList)
      )
    )

  private def overviewSidebar(openVar: Var[Boolean]): HtmlElement =
    Card(
      cls := "overflow-hidden py-0",
      Sidebar.provider(openVar)(
        cls := "min-h-0",
        Sidebar.root(collapsible = Sidebar.Collapsible.None, openVar = openVar)(
          cls := "w-full bg-transparent",
          Sidebar.content(
            cls := "gap-0",
            sidebarGroup("Overview", "pb-1")(
              ("Dashboard", Icons.layoutDashboard(), true),
              ("Transactions", Icons.arrowLeftRight(), false),
              ("Investments", Icons.trendingUp(), false),
              ("Accounts", Icons.building2(), false),
              ("Spending", Icons.pieChart(), false)
            ),
            Sidebar.separator(cls := "w-auto!"),
            Sidebar.group(
              cls := "pt-1",
              Sidebar.groupLabel("Planning"),
              Sidebar.groupContent(
                Sidebar.menu(
                  menuEntry("Goals", Icons.target()),
                  menuEntry("Budget", Icons.wallet()),
                  menuEntry("Reports", Icons.fileBarChart()),
                  menuEntry("Documents", Icons.fileText())
                )
              )
            )
          )
        )
      )
    )

  private def accountSidebar(openVar: Var[Boolean]): HtmlElement =
    Card(
      cls := "overflow-hidden py-0",
      Sidebar.provider(openVar)(
        cls := "min-h-0",
        Sidebar.root(collapsible = Sidebar.Collapsible.None, openVar = openVar)(
          cls := "w-full bg-transparent",
          Sidebar.content(
            cls := "gap-0",
            Sidebar.group(
              cls := "pb-1",
              Sidebar.groupLabel("Account"),
              Sidebar.groupContent(
                Sidebar.menu(
                  menuEntry("Profile", Icons.user()),
                  menuEntry("Billing", Icons.creditCard(), isActive = true),
                  menuEntry("Notifications", Icons.bell()),
                  menuEntry("Security", Icons.shield()),
                  menuEntry("Appearance", Icons.paintbrush())
                )
              )
            ),
            Sidebar.separator(cls := "w-auto!"),
            Sidebar.group(
              cls := "pt-1",
              Sidebar.groupLabel("Support"),
              Sidebar.groupContent(
                Sidebar.menu(
                  menuEntry("Help Center", Icons.circleHelp()),
                  menuEntry("Contact Us", Icons.messageSquare()),
                  menuEntry("Documentation", Icons.bookOpen()),
                  menuEntry("Status", Icons.activity())
                )
              )
            )
          )
        )
      )
    )

  def apply(): HtmlElement =
    val overviewOpenVar = Var(true)
    val accountOpenVar = Var(true)
    div(
      dataAttr("card") := "sidebar-nav",
      cls := "grid grid-cols-2 items-start gap-6",
      overviewSidebar(overviewOpenVar),
      accountSidebar(accountOpenVar)
    )
