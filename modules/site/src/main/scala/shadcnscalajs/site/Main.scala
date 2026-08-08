package shadcnscalajs.site

import com.raquo.laminar.api.L.*
import org.scalajs.dom
import shadcnscalajs.ui.*

import scala.scalajs.js

object Main:

  private lazy val kbdEl = htmlTag("kbd")

  def main(args: Array[String]): Unit =
    val pathname = dom.window.location.pathname
    val page =
      if pathname == "/components" || pathname == "/components/" then componentsGalleryPage()
      else if pathname.startsWith("/components/") then componentDocsPage()
      else if pathname.startsWith("/blocks/") && pathname.endsWith("/preview") then
        BlockPreviewPage(pathname.stripPrefix("/blocks/").stripSuffix("/preview"))
      else if pathname == "/blocks" || pathname == "/blocks/" then BlocksIndexPage()
      else if pathname.startsWith("/blocks/") then BlockDocsPage(pathname.stripPrefix("/blocks/").stripSuffix("/"))
      else if pathname == "/create" || pathname == "/create/" then
        dom.window.location.replace("/create/preview-02" + dom.window.location.search)
        div()
      else if pathname == "/create/preview-02" then shadcnscalajs.site.create.CreatePageEntry()
      else if pathname == "/preview/preview-02" then shadcnscalajs.site.create.PreviewOnlyPage()
      else app()
    render(dom.document.getElementById("root"), page)

  // ── SVG Icons ──
  private def iconSvg(p: String) =
    s"""<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">$p</svg>"""
  private def iconSearch = iconSvg("<circle cx='11' cy='11' r='8'/><path d='m21 21-4.3-4.3'/>")
  def iconSun = iconSvg(
    "<circle cx='12' cy='12' r='4'/><path d='M12 2v2M12 20v2m-7.07-17.07 1.41 1.41m9.32 9.32 1.41 1.41M2 12h2m16 0h2M6.34 17.66l-1.41 1.41M19.07 4.93l-1.41 1.41'/>"
  )
  def iconMoon = iconSvg("<path d='M12 3a6 6 0 0 0 9 9 9 9 0 1 1-9-9Z'/>")
  private def iconInfo = iconSvg("<circle cx='12' cy='12' r='10'/><path d='M12 16v-4M12 8h.01'/>")
  private def iconCircleCheck = iconSvg("<path d='M21.801 10A10 10 0 1 1 17 3.335'/><path d='m9 11 3 3L22 4'/>")
  private def iconCircleAlert = iconSvg(
    "<circle cx='12' cy='12' r='10'/><path d='M12 8v4'/><path d='M12 16h.01'/>"
  )

  private val logoSvg =
    """<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 256 256" aria-hidden="true"><rect width="256" height="256" fill="none"/><line x1="208" y1="128" x2="128" y2="208" fill="none" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="32"/><line x1="192" y1="40" x2="40" y2="192" fill="none" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="32"/></svg>"""

  def rawIcon(html: String): HtmlElement =
    val el = dom.document.createElement("div"); el.innerHTML = html
    span(cls := "[&_svg]:size-4 inline-flex", foreignHtmlElement(el.firstElementChild.asInstanceOf[dom.html.Element]))

  /** `rawIcon` without the wrapping `span`. Needed wherever a component's Tailwind classes target a *direct* `> svg`
    * child — e.g. `Alert`'s `has-[>svg]:grid-cols-*` icon column, which a wrapper element silently defeats.
    */
  private def bareIcon(html: String): HtmlElement =
    val el = dom.document.createElement("div"); el.innerHTML = html
    foreignHtmlElement(el.firstElementChild.asInstanceOf[dom.html.Element])

  lazy val logoEl: dom.html.Element =
    val div = dom.document.createElement("div"); div.innerHTML = logoSvg
    div.firstElementChild.asInstanceOf[dom.html.Element]

  /** Every component with a doc page, in sidebar/prev-next order. Single source of truth for the sidebar nav, prev/next
    * footer links, and (eventually) the components gallery index — add a display name here when porting a new
    * component; `liveExample()`'s `case _` placeholder covers it until a real preview case is added.
    */
  private val componentNavList: List[String] = List(
    "Accordion",
    "Alert",
    "Alert Dialog",
    "Aspect Ratio",
    "Avatar",
    "Badge",
    "Breadcrumb",
    "Button",
    "Button Group",
    "Calendar",
    "Card",
    "Carousel",
    "Chart",
    "Checkbox",
    "Collapsible",
    "Combobox",
    "Command",
    "Context Menu",
    "Data Table",
    "Date Picker",
    "Dialog",
    "Drawer",
    "Dropdown Menu",
    "Empty",
    "Field",
    "Form",
    "Hover Card",
    "Input",
    "Input Group",
    "Input OTP",
    "Item",
    "Kbd",
    "Label",
    "Menubar",
    "Native Select",
    "Navigation Menu",
    "Pagination",
    "Popover",
    "Progress",
    "Radio",
    "Radio Group",
    "Range",
    "Range Calendar",
    "Resizable",
    "Scroll Area",
    "Scrollbar",
    "Select",
    "Separator",
    "Sheet",
    "Sidebar",
    "Skeleton",
    "Slider",
    "Sonner",
    "Spinner",
    "Switch",
    "Table",
    "Tabs",
    "Textarea",
    "Theme Switcher",
    "Toast",
    "Toggle",
    "Toggle Group",
    "Tooltip",
    "Typography"
  )

  private def slugify(name: String): String = name.toLowerCase.replace(' ', '-')

  /** Typography recipe page — utility classes copied from shadcn-svelte typography examples (no registry component). */
  private def typographyDemo(): HtmlElement =
    div(
      cls := "w-full max-w-2xl space-y-8 p-6 text-left",
      p(
        cls := "text-sm text-muted-foreground",
        "No Typography.scala component — apply these Tailwind classes directly in your Laminar elements."
      ),
      div(
        cls := "space-y-6",
        h1(
          cls := "scroll-m-20 text-4xl font-extrabold tracking-tight text-balance",
          "Taxing Laughter: The Joke Tax Chronicles"
        ),
        p(cls := "text-xl leading-7 text-muted-foreground [&:not(:first-child)]:mt-6", "Lead paragraph text."),
        h2(
          cls := "scroll-m-20 border-b pb-2 text-3xl font-semibold tracking-tight transition-colors first:mt-0",
          "The King's Plan"
        ),
        h3(cls := "scroll-m-20 text-2xl font-semibold tracking-tight", "The Joke Tax"),
        h4(cls := "scroll-m-20 text-xl font-semibold tracking-tight", "People stopped telling jokes"),
        p(cls := "leading-7 [&:not(:first-child)]:mt-6", "Body paragraph with standard leading."),
        p(cls := "text-xl text-muted-foreground", "Lead — muted xl text."),
        div(cls := "text-lg font-semibold", "Large semibold text."),
        small(cls := "text-sm leading-none font-medium", "Small label text."),
        p(cls := "text-sm text-muted-foreground", "Muted helper text."),
        blockQuote(cls := "mt-6 border-s-2 ps-6 italic", "A blockquote with a left border."),
        ul(
          cls := "my-6 ms-6 list-disc [&>li]:mt-2",
          li("1st level of puns: 5 gold coins"),
          li("2nd level of jokes: 10 gold coins"),
          li("3rd level of one-liners: 20 gold coins")
        ),
        code(
          cls := "relative rounded bg-muted px-[0.3rem] py-[0.2rem] font-mono text-sm font-semibold",
          "inline code"
        )
      )
    )

  private def formatUsd(amount: Int): String =
    val formatter = js.Dynamic.newInstance(js.Dynamic.global.Intl.NumberFormat)(
      "en-US",
      js.Dynamic.literal(style = "currency", currency = "USD")
    )
    formatter.format(amount).asInstanceOf[String]

  private final case class DemoPayment(id: String, amount: Int, status: String, email: String)

  /** Interactive data-table demo — mirrors shadcn-svelte's data-table-demo.svelte using pure Laminar state. */
  private def dataTableDemo(): HtmlElement =
    val payments = Var(
      Seq(
        DemoPayment("m5gr84i9", 316, "Success", "ken99@yahoo.com"),
        DemoPayment("3u1reuv4", 242, "Success", "Abe45@gmail.com"),
        DemoPayment("derv1ws0", 837, "Processing", "Monserrat44@gmail.com"),
        DemoPayment("5kma53ae", 874, "Success", "Silas22@gmail.com"),
        DemoPayment("bhqecj4p", 721, "Failed", "carmella@hotmail.com")
      )
    )

    // Header-checkbox state, refreshed by the binder on the root element below — column renderers run outside
    // any element scope, so they cannot read the page rows signal directly.
    val pageRowIds = Var(Seq.empty[String])
    val allPageRowsSelected = Var(false)
    val somePageRowsSelected = Var(false)

    lazy val columns: Seq[DataTable.Column[DemoPayment]] = Seq(
      DataTable.Column(
        id = "select",
        header = () =>
          Checkbox(
            allPageRowsSelected,
            somePageRowsSelected.signal,
            role := "checkbox",
            aria.label := "Select all",
            onClick --> { _ => table.toggleAllRows(pageRowIds.now()) }
          ),
        cell = p =>
          Checkbox(
            Var(table.rowSelection.now().contains(p.id)),
            role := "checkbox",
            aria.label := "Select row",
            onClick --> { _ => table.toggleRow(p.id) }
          ),
        accessor = _ => "",
        enableSorting = false,
        enableHiding = false
      ),
      DataTable.Column.text("status", "Status", _.status, p => div(cls := "capitalize", p.status)),
      DataTable.Column(
        id = "email",
        header = () =>
          Button.of(
            _.variant(Button.Variant.Ghost),
            _.size(Button.Size.Sm),
            _ => cls := "-ms-3",
            _ => onClick --> { _ => table.toggleSort("email") },
            // Label and icon must be direct children: the button's own `gap` and `has-[>svg]:px-*`
            // only see direct children, so wrapping both in a span stacks the icon under the text.
            _ => "Email",
            _ => Icons.chevronsUpDown()
          ),
        cell = p => div(cls := "lowercase", p.email),
        accessor = _.email
      ),
      DataTable.Column(
        id = "amount",
        header = () => div(cls := "text-end", "Amount"),
        cell = p => div(cls := "text-end font-medium", formatUsd(p.amount)),
        // Zero-padded so the string sort the table applies orders amounts numerically.
        accessor = p => f"${p.amount}%012d"
      ),
      DataTable.Column(
        id = "actions",
        header = () => span(cls := "sr-only", "Actions"),
        cell = p =>
          DropdownMenu.withTrigger(DropdownMenu.ghostIconTriggerClasses, DropdownMenu.Align.End)(
            span(cls := "sr-only", "Open menu"),
            Icons.moreHorizontal()
          )(
            DropdownMenu.Item(
              "Copy payment ID",
              () => { val _ = js.Dynamic.global.navigator.clipboard.writeText(p.id) }
            ),
            DropdownMenu.Item("View customer", () => ()),
            DropdownMenu.Item("View payment details", () => ())
          ),
        accessor = _ => "",
        enableSorting = false,
        enableHiding = false
      )
    )

    lazy val table: DataTable.TableState[DemoPayment] = DataTable.createTable(
      payments,
      columns,
      initialPageSize = 10,
      rowId = _.id,
      filterFn = (p, q, _) => p.email.toLowerCase.contains(q.toLowerCase)
    )

    div(
      cls := "-mb-8 w-full",
      table.rows.combineWith(table.rowSelection.signal) --> { (state: (Seq[DemoPayment], Set[String])) =>
        val (pageRows, selection) = state
        val ids = pageRows.map(_.id)
        pageRowIds.set(ids)
        val selectedOnPage = ids.count(selection.contains)
        allPageRowsSelected.set(ids.nonEmpty && selectedOnPage == ids.size)
        somePageRowsSelected.set(selectedOnPage > 0 && selectedOnPage < ids.size)
      },
      div(
        cls := "flex items-center py-4",
        Input(
          placeholder := "Filter emails...",
          cls := "max-w-sm",
          controlled(value <-- table.globalFilter.signal, onInput.mapToValue --> table.globalFilter)
        ),
        div(
          cls := "ms-auto",
          DropdownMenu.alignEnd("Columns", Icons.chevronDown(svg.cls := "ms-2 size-4"))(
            table.hideableColumns.map { col =>
              DropdownMenu.Item.checkbox(
                col.id.capitalize,
                table.isColumnVisible(col.id),
                () => table.toggleColumnVisibility(col.id)
              )
            }*
          )
        )
      ),
      table.view(_.id),
      div(
        cls := "flex items-center justify-end space-x-2 pt-4",
        div(
          cls := "flex-1 text-sm text-muted-foreground",
          child.text <-- table.selectedFilteredCount.combineWith(table.filteredCount).map { case (sel, total) =>
            s"$sel of $total row(s) selected."
          }
        ),
        div(
          cls := "space-x-2",
          Button.of(
            _.variant(Button.Variant.Outline),
            _.size(Button.Size.Sm),
            _ => disabled <-- table.canPreviousPage.map(!_),
            _ => onClick --> { _ => table.previousPage() },
            _ => "Previous"
          ),
          Button.of(
            _.variant(Button.Variant.Outline),
            _.size(Button.Size.Sm),
            _ => disabled <-- table.canNextPage.map(!_),
            _ => onClick --> { _ => table.nextPage() },
            _ => "Next"
          )
        )
      )
    )

  // shadcn/ui button classes — repeated inline for readability
  private def btnPrimary =
    "inline-flex shrink-0 items-center justify-center gap-2 rounded-md text-sm font-medium whitespace-nowrap transition-all outline-none focus-visible:border-ring focus-visible:ring-[3px] focus-visible:ring-ring/50 disabled:pointer-events-none disabled:opacity-50 bg-primary text-primary-foreground hover:bg-primary/90 h-9 px-4 py-2"
  private def btnOutline =
    "inline-flex shrink-0 items-center justify-center gap-2 rounded-md text-sm font-medium whitespace-nowrap transition-all outline-none focus-visible:border-ring focus-visible:ring-[3px] focus-visible:ring-ring/50 disabled:pointer-events-none disabled:opacity-50 border bg-background shadow-xs hover:bg-accent hover:text-accent-foreground h-9 px-4 py-2"
  def btnGhost =
    "inline-flex shrink-0 items-center justify-center gap-2 rounded-md text-sm font-medium whitespace-nowrap transition-all outline-none focus-visible:border-ring focus-visible:ring-[3px] focus-visible:ring-ring/50 disabled:pointer-events-none disabled:opacity-50 hover:bg-accent hover:text-accent-foreground h-9 px-4 py-2"
  def btnIcon =
    "inline-flex shrink-0 items-center justify-center gap-2 rounded-md text-sm font-medium whitespace-nowrap transition-all outline-none focus-visible:border-ring focus-visible:ring-[3px] focus-visible:ring-ring/50 disabled:pointer-events-none disabled:opacity-50 hover:bg-accent hover:text-accent-foreground size-9"

  // ── App ──
  private def app(): HtmlElement =
    div(
      cls := "min-h-dvh overflow-x-clip bg-background text-foreground antialiased",

      // ── Header ──
      headerTag(
        cls := "bg-background sticky inset-x-0 top-0 isolate z-30 flex shrink-0 items-center gap-2",
        div(
          cls := "flex h-14 w-full items-center justify-between gap-2 px-4",
          div(
            cls := "flex min-w-0 items-center gap-1",
            a(
              href := "/",
              cls := btnGhost,
              aria.label := "shadcn-scalajs home",
              span(cls := "[&_svg]:size-4", foreignHtmlElement(logoEl)),
              span(cls := "truncate font-semibold", "shadcn-scalajs")
            ),
            navTag(
              cls := "hidden sm:flex items-center gap-1",
              aria.label := "Primary",
              a(cls := btnGhost, href := "/components", "Components"),
              a(cls := btnGhost, href := "/blocks", "Blocks"),
              a(cls := btnGhost, href := "/create", "Create"),
              a(
                cls := btnGhost,
                href := "https://github.com/lamtanloc512/shadcn-scalajs",
                target := "_blank",
                rel := "noopener",
                "GitHub"
              )
            )
          ),
          div(
            cls := "ml-auto flex min-w-0 flex-1 items-center justify-end gap-2",
            div(
              cls := "hidden sm:flex h-8 w-full min-w-0 max-w-72 cursor-text items-center rounded-md border border-input bg-background px-3 text-sm sm:ml-auto",
              role := "button",
              tabIndex := 0,
              aria.label := "Search docs",
              input(
                typ := "text",
                placeholder := "Search...",
                readOnly := true,
                tabIndex := -1,
                cls := "bg-transparent outline-none flex-1"
              ),
              span(aria.hidden := true, rawIcon(iconSearch)),
              span(
                aria.hidden := true,
                kbdEl(
                  cls := "pointer-events-none h-5 select-none items-center gap-1 rounded border bg-muted px-1.5 font-mono text-[10px] font-medium text-muted-foreground hidden sm:flex",
                  "⌘K"
                )
              )
            ),
            ThemeMenu()
          )
        )
      ),
      mainTag(
        cls := "flex flex-1 flex-col",

        // ── Hero ──
        sectionTag(
          cls := "md:[&_.container]:pb-8 lg:[&_.container]:pb-12",
          div(
            cls := "mx-auto w-full px-2 max-w-[1400px]",
            div(
              cls := "mx-auto flex flex-col items-center gap-2 px-6 py-8 text-center md:py-16 lg:py-20 xl:gap-4",
              h1(
                cls := "text-3xl font-semibold tracking-tight text-balance text-primary lg:leading-[1.1] lg:font-semibold xl:text-5xl xl:tracking-tighter max-w-4xl",
                "shadcn/ui, ported to Scala.js"
              ),
              p(
                cls := "max-w-4xl text-base text-balance text-foreground sm:text-lg",
                "Copy-paste Laminar components styled with real shadcn/ui Tailwind classes — every component also compiles to a standalone Web Component for any frontend."
              ),
              div(
                cls := "flex w-full items-center justify-center gap-2 pt-2",
                a(
                  cls := btnPrimary,
                  href := "/create",
                  "Build Your Own ",
                  Icons.arrowRight(svg.cls := "size-4")
                ),
                a(cls := btnOutline, href := "/components", "Get started")
              )
            )
          )
        ),

        // ── Preview mosaic ──
        sectionTag(
          cls := "flex-1 p-0",
          shadcnscalajs.site.create.preview02.Preview02()
        ),

        // ── Footer ──
        footerTag(
          cls := "px-6 py-8 text-center text-sm text-muted-foreground",
          "Built by ",
          a(
            href := "https://x.com/ethandev512",
            target := "_blank",
            rel := "noopener",
            cls := "underline underline-offset-2 decoration-muted-foreground/50 transition-all hover:decoration-foreground/50",
            "Ethan Lam"
          ),
          ". ",
          a(
            href := "https://github.com/lamtanloc512",
            target := "_blank",
            rel := "noopener",
            cls := "underline underline-offset-2 decoration-muted-foreground/50 transition-all hover:decoration-foreground/50",
            "Sponsor me"
          ),
          "."
        )
      ),
      Sonner.Toaster()
    )

  /** Interactive component gallery. Each preview is composed from the same Laminar primitives exported by modules/ui,
    * making this page a useful smoke test as well as documentation.
    */
  private def componentsGalleryPage(): HtmlElement =
    div(
      cls := "min-h-dvh bg-background text-foreground antialiased",
      headerTag(
        cls := "sticky inset-x-0 top-0 z-30 flex h-14 items-center border-b bg-background/95 px-4 backdrop-blur",
        div(
          cls := "mx-auto flex w-full max-w-7xl items-center gap-4",
          a(
            href := "/",
            cls := btnGhost,
            span(cls := "[&_svg]:size-4", foreignHtmlElement(logoEl)),
            span(cls := "font-semibold", "shadcn-scalajs")
          ),
          navTag(
            cls := "hidden items-center gap-1 sm:flex",
            aria.label := "Primary",
            a(cls := btnGhost, href := "/", "Home"),
            a(cls := btnGhost + " bg-accent text-accent-foreground", href := "/components", "Components"),
            a(cls := btnGhost, href := "/blocks", "Blocks"),
            a(cls := btnGhost, href := "/create", "Create")
          ),
          div(
            cls := "ml-auto flex items-center gap-2",
            ThemeMenu()
          )
        )
      ),
      mainTag(
        cls := "mx-auto w-full max-w-7xl px-4 py-10 sm:px-6 lg:px-8",
        div(
          cls := "mb-10 max-w-2xl",
          p(cls := "mb-2 text-sm font-medium text-primary", "Laminar component library"),
          h1(cls := "text-4xl font-semibold tracking-tight", "Components"),
          p(
            cls := "mt-3 text-lg text-muted-foreground",
            "Browse every component in the shadcn-scalajs registry. Each links to its full docs page with a live preview, usage code, and install instructions."
          )
        ),
        div(
          cls := "grid grid-cols-1 gap-x-8 gap-y-3 sm:grid-cols-2 lg:grid-cols-3",
          componentNavList.map { name =>
            a(
              href := s"/components/${slugify(name)}",
              cls := "text-sm text-foreground underline-offset-4 hover:text-primary hover:underline",
              name
            )
          }
        )
      ),
      Sonner.Toaster()
    )

  /** Documentation-style component route. `/components/drawer` is the first full page; other component links use the
    * same shell and live primitive preview so the route structure scales as examples are added.
    */
  private def componentDocsPage(): HtmlElement =
    val themeConfig = Var(ThemeConfig.load())
    val drawerOpen = Var(false)
    val dialogOpen = Var(false)
    val switchOn = Var(true)
    val tabsDefaultSelected = Var("overview")
    val tabsLineSelected = Var("overview")
    val previewTheme = Var(ThemeSwitcher.Theme.System)
    val pathParts = dom.window.location.pathname.stripPrefix("/components").stripPrefix("/").split("/").toList
    val componentName = pathParts.find(_.nonEmpty).getOrElse("drawer")
    val componentTitle = componentName.split("-").map(_.capitalize).mkString(" ")
    val componentDescription = componentName match
      case "accordion" => "A vertically stacked set of interactive headings that each reveal a section of content."
      case "drawer"    => "A mobile-first drawer component for Laminar."
      case "data-table" =>
        "Pure Scala table-state utilities composing Table — sorting, filtering, pagination, and row selection without TanStack."
      case "dialog" => "A modal dialog built with the native HTML dialog element."
      case "button" => "A reusable action button with shadcn/ui variants."
      case "switch" => "A reactive boolean control backed by a Laminar Var."
      case "sonner" => "An opinionated toast component — pure Laminar, no svelte-sonner dependency."
      case "typography" =>
        "Styles for headings, paragraphs, lists, and inline code — utility-class recipes, not a registry component."
      case _ => s"The ${componentTitle.toLowerCase} primitive for shadcn-scalajs."

    dom.document.title = s"$componentTitle – shadcn-scalajs"

    val navIndex = componentNavList.indexWhere(name => slugify(name) == componentName)
    val prevEntry = if navIndex > 0 then Some(componentNavList(navIndex - 1)) else None
    val nextEntry =
      if navIndex >= 0 && navIndex < componentNavList.length - 1 then Some(componentNavList(navIndex + 1)) else None

    def codeBlock(language: String, source: String): HtmlElement =
      div(
        cls := "overflow-hidden rounded-md border bg-muted/30",
        div(
          cls := "flex h-9 items-center justify-between border-b px-3 text-xs text-muted-foreground",
          span(language),
          span("Scala.js")
        ),
        div(
          cls := "overflow-x-auto p-4 font-mono text-xs leading-6 text-foreground",
          styleAttr := "white-space:pre",
          source
        )
      )

    def navLink(name: String): HtmlElement =
      val slug = slugify(name)
      a(
        href := s"/components/$slug",
        cls := s"block rounded-md px-2 py-1.5 text-sm transition-colors hover:bg-accent hover:text-accent-foreground ${if slug == componentName then "bg-accent font-medium text-accent-foreground" else "text-muted-foreground"}",
        name
      )

    def tableOfContents: HtmlElement =
      asideTag(
        cls := "hidden xl:block",
        navTag(
          cls := "sticky top-20 space-y-1 border-l pl-4 text-sm",
          aria.label := "On this page",
          p(cls := "mb-3 text-xs font-medium text-muted-foreground", "ON THIS PAGE"),
          a(href := "#about", cls := "block text-muted-foreground hover:text-foreground", "About"),
          a(href := "#installation", cls := "block text-muted-foreground hover:text-foreground", "Installation"),
          a(href := "#usage", cls := "block text-muted-foreground hover:text-foreground", "Usage"),
          a(href := "#examples", cls := "block text-muted-foreground hover:text-foreground", "Examples"),
          if componentName == "drawer" then
            a(href := "#sides", cls := "block text-muted-foreground hover:text-foreground", "Sides")
          else emptyNode
        )
      )

    /** Wide demos (tables, sidebars, nav) need more than the prose `max-w-2xl` column. */
    val articleWidthCls =
      componentName match
        case "data-table" | "sidebar" | "chart" | "navigation-menu" | "carousel" | "resizable" | "table" =>
          "mx-auto max-w-5xl"
        case _ => "mx-auto max-w-2xl"

    def previewCanvas(content: Modifier[HtmlElement]*): HtmlElement =
      div(cls := "flex min-h-[450px] w-full items-center justify-center gap-3 p-10", content)

    def liveExample(): HtmlElement = componentName match
      case "accordion" =>
        val previewOpen = Var(Option(0))
        previewCanvas(
          div(
            cls := "w-full max-w-sm",
            Accordion(
              previewOpen,
              Accordion.Section(
                "What are your shipping options?",
                "We offer standard (5-7 days), express (2-3 days), and overnight shipping. Free shipping on international orders."
              ),
              Accordion.Section(
                "What is your return policy?",
                "You can return items within 30 days of delivery. Items must be unused and in their original packaging."
              ),
              Accordion.Section(
                "How can I contact customer support?",
                "Email support@example.com or use live chat during business hours."
              )
            )
          )
        )
      // Mirrors shadcn/ui's own new-york-v4 alert-demo.tsx, plus a fourth icon-less alert: the icon is optional, and
      // without one the root falls back to its `grid-cols-[0_1fr]` branch — a layout path nothing else here exercises.
      case "alert" =>
        previewCanvas(
          div(
            cls := "grid w-full max-w-xl items-start gap-4",
            Alert(
              Alert.Variant.Default,
              bareIcon(iconCircleCheck),
              Alert.title("Success! Your changes have been saved"),
              Alert.description("This is an alert with icon, title and description.")
            ),
            Alert(
              Alert.Variant.Default,
              bareIcon(iconInfo),
              Alert.title("This Alert has a title and an icon. No description.")
            ),
            Alert(
              Alert.Variant.Default,
              Alert.title("This Alert has no icon"),
              Alert.description("Title and description line up in the same column either way.")
            ),
            Alert(
              Alert.Variant.Destructive,
              bareIcon(iconCircleAlert),
              Alert.title("Unable to process your payment."),
              Alert.description(
                p("Please verify your billing information and try again."),
                ul(
                  cls := "list-inside list-disc text-sm",
                  li("Check your card details"),
                  li("Ensure sufficient funds"),
                  li("Verify billing address")
                )
              )
            )
          )
        )
      case "alert-dialog" =>
        previewCanvas(
          Button(onClick --> { _ => dialogOpen.set(true) }, "Open alert dialog"),
          AlertDialog(dialogOpen)(
            AlertDialog.title("Delete project?"),
            AlertDialog.description("This action cannot be undone."),
            AlertDialog.footer(Button(onClick --> { _ => dialogOpen.set(false) }, "Cancel"))
          )
        )
      case "avatar" => previewCanvas(Avatar(Avatar.fallback("LS")))
      case "badge" =>
        previewCanvas(
          Badge("New"),
          Badge.of(_.variant(Badge.Variant.Secondary), _ => "Beta"),
          Badge.of(_.variant(Badge.Variant.Outline), _ => "Outline")
        )
      case "breadcrumb" =>
        previewCanvas(
          Breadcrumb(
            Breadcrumb.list(
              Breadcrumb.item(Breadcrumb.link("/", "Home")),
              Breadcrumb.separator(),
              Breadcrumb.item("Components")
            )
          )
        )
      case "button" =>
        previewCanvas(
          Button("Primary"),
          Button.of(_.variant(Button.Variant.Outline), _ => "Outline"),
          Button.of(_.variant(Button.Variant.Destructive), _ => "Delete")
        )
      case "button-group" =>
        previewCanvas(
          ButtonGroup(
            Button.of(_.variant(Button.Variant.Outline), _ => "Back"),
            Button.of(_.variant(Button.Variant.Outline), _ => "Next")
          )
        )
      case "card" =>
        previewCanvas(
          Card(
            cls := "w-full max-w-sm",
            Card.header(Card.title("Project update"), Card.description("A Card composed from Laminar primitives.")),
            Card.content("Your latest deployment is ready.")
          )
        )
      case "chart" => previewCanvas(Chart("Chart preview"))
      case "checkbox" =>
        previewCanvas(Checkbox(idAttr := "terms"), Label(forId := "terms", "Accept terms"))
      case "collapsible" =>
        previewCanvas(
          Collapsible(
            Collapsible.trigger("Show details"),
            Collapsible.content(p(cls := "pt-2 text-sm text-muted-foreground", "This is native details content."))
          )
        )
      case "combobox" =>
        val frameworks = Seq(
          Combobox.Item("next.js", "Next.js"),
          Combobox.Item("sveltekit", "SvelteKit"),
          Combobox.Item("nuxt.js", "Nuxt.js"),
          Combobox.Item("remix", "Remix"),
          Combobox.Item("astro", "Astro")
        )
        previewCanvas(
          div(
            cls := "flex w-full max-w-sm flex-col gap-4",
            Combobox(
              Var(Option.empty[String]),
              frameworks,
              placeholder = "Select framework…",
              searchPlaceholder = "Search framework…",
              emptyText = "No framework found."
            ),
            Combobox.multiple(
              Var(Set.empty[String]),
              frameworks,
              placeholder = "Select frameworks…",
              searchPlaceholder = "Search framework…",
              emptyText = "No framework found."
            )
          )
        )
      case "command" =>
        previewCanvas(
          Command(
            cls := "w-full max-w-sm border",
            Command.input(placeholder := "Search…"),
            Command.list(Command.item("Open settings"), Command.item("Create project"))
          )
        )
      case "dialog" =>
        previewCanvas(
          Button(onClick --> { _ => dialogOpen.set(true) }, "Open dialog"),
          Dialog(dialogOpen)(
            h2(cls := "text-lg font-semibold", "Dialog"),
            p(cls := "text-sm text-muted-foreground", "Native HTML dialog."),
            Button(onClick --> { _ => dialogOpen.set(false) }, "Close")
          )
        )
      case "drawer" =>
        previewCanvas(
          Button(onClick --> { _ => drawerOpen.set(true) }, "Open Drawer"),
          Drawer(drawerOpen)(
            Drawer.header(
              h2(cls := "text-lg font-semibold", "Edit profile"),
              p(cls := "text-sm text-muted-foreground", "Make changes to your public profile.")
            ),
            div(cls := "grid gap-4 px-4", Field.label("Display name"), Input(value := "Laminar Studio")),
            Drawer.footer(
              Button.of(_.variant(Button.Variant.Outline), _ => "Cancel"),
              Button(onClick --> { _ => drawerOpen.set(false) }, "Save changes")
            )
          )
        )
      case "dropdown-menu" =>
        previewCanvas(
          DropdownMenu("Open menu")(DropdownMenu.Item("Profile", () => ()), DropdownMenu.Item("Settings", () => ()))
        )
      case "empty" =>
        previewCanvas(
          Empty(
            cls := "w-full max-w-sm",
            Empty.header(Empty.title("No projects"), Empty.description("Create your first project to get started."))
          )
        )
      case "field" =>
        previewCanvas(
          div(
            cls := "flex w-full max-w-sm flex-col gap-6",
            Field(
              cls := "w-full",
              Field.label("Email"),
              Input(placeholder := "you@example.com"),
              Field.description("We will never share your email.")
            ),
            Field(
              cls := "w-full",
              Field.label("Password"),
              Input(`type` := "password"),
              Field.error(Seq("Password must be at least 8 characters."))
            ),
            Field(
              cls := "w-full",
              Field.label("Username"),
              Input(placeholder := "jane"),
              Field.error(Seq("Username is required.", "Username must be unique."))
            )
          )
        )
      case "form" =>
        previewCanvas(
          Form(
            cls := "w-full max-w-sm",
            Form.item(Form.label("Email"), Input(placeholder := "you@example.com")),
            Button("Submit")
          )
        )
      case "input" => previewCanvas(Input(placeholder := "Type something…", cls := "max-w-sm"))
      case "input-group" =>
        previewCanvas(
          div(
            cls := "flex w-full max-w-sm flex-col gap-4",
            InputGroup(
              InputGroup.addon(InputGroup.AddonAlign.InlineStart, InputGroup.text("https://")),
              InputGroup.input(placeholder := "example.com")
            ),
            InputGroup(
              InputGroup.addon(
                InputGroup.AddonAlign.BlockStart,
                span(cls := "text-sm text-muted-foreground", "Description")
              ),
              InputGroup.textarea(placeholder := "Enter your message…", rows := 3),
              InputGroup.addon(
                InputGroup.AddonAlign.BlockEnd,
                span(cls := "text-xs text-muted-foreground", "Markdown supported")
              )
            )
          )
        )
      case "item" =>
        previewCanvas(
          Item(
            cls := "w-full max-w-sm border",
            Item.content(Item.title("Laminar"), Item.description("Reactive Scala.js UI")),
            Item.actions(Badge("Stable"))
          )
        )
      case "kbd"   => previewCanvas(Kbd("⌘K"), Kbd.group(Kbd("⌘"), Kbd("P")))
      case "label" => previewCanvas(Label("Email address"), Input(placeholder := "you@example.com", cls := "max-w-sm"))
      case "native-select" =>
        previewCanvas(NativeSelect(cls := "max-w-sm", option("Choose a plan"), option("Pro"), option("Team")))
      case "popover" => previewCanvas(Popover(Popover.trigger("Open popover"), Popover.content("Popover content")))
      case "pagination" =>
        previewCanvas(
          Pagination(
            Pagination.list(
              Pagination.item(Pagination.link("#", false, "←")),
              Pagination.item(Pagination.link("#", true, "1")),
              Pagination.item(Pagination.link("#", false, "2")),
              Pagination.item(Pagination.link("#", false, "→"))
            )
          )
        )
      case "progress" => previewCanvas(Progress(68, cls := "w-full max-w-sm"))
      case "radio"    => previewCanvas(Radio("plan", checked := true), Label("Pro"), Radio("plan"), Label("Team"))
      case "radio-group" =>
        previewCanvas(
          RadioGroup(
            Label(RadioGroup.item("plan", checked := true), "Pro"),
            Label(RadioGroup.item("plan"), "Team")
          )
        )
      case "range" => previewCanvas(Range(value := "50", cls := "max-w-sm"))
      case "scrollbar" =>
        previewCanvas(
          Scrollbar(
            cls := "h-32 w-full max-w-sm rounded-md border p-3",
            p("Scrollable content"),
            div(styleAttr := "height:12rem"),
            p("End")
          )
        )
      case "scroll-area" =>
        previewCanvas(
          ScrollArea(
            cls := "h-32 w-full max-w-sm border p-3",
            p("Scrollable content rendered by the Laminar primitive."),
            div(styleAttr := "height:12rem"),
            p("End")
          )
        )
      case "select" => previewCanvas(Select(cls := "max-w-sm", option("Choose a plan"), option("Pro"), option("Team")))
      case "sidebar" =>
        previewCanvas(
          Sidebar(
            cls := "h-48 w-full max-w-sm",
            Sidebar.header(
              Sidebar.input(placeholder := "Search…", cls := "mb-2")
            ),
            Sidebar.content(
              Sidebar.group(
                Sidebar.groupLabel(
                  span(cls := "flex-1", "Navigation"),
                  Sidebar.groupAction(Icons.plus(), aria.label := "Add section")
                ),
                Sidebar.groupContent(
                  Sidebar.menu(
                    li(
                      dataAttr("slot") := "sidebar-menu-item",
                      dataAttr("sidebar") := "menu-item",
                      cls := "group/menu-item relative",
                      Sidebar.menuButton(isActive = true)(Icons.layoutDashboard(), "Overview"),
                      Sidebar.menuAction(showOnHover = true)(Icons.moreHorizontal(), aria.label := "More")
                    ),
                    Sidebar.menuItem(Icons.building2(), "Settings"),
                    Sidebar.menuSkeleton(showIcon = true)()
                  )
                )
              )
            )
          )
        )
      case "skeleton" => previewCanvas(Skeleton(cls := "h-20 w-full max-w-sm"))
      case "slider"   => previewCanvas(Slider(value := "50", cls := "w-full max-w-sm"))
      case "sonner" =>
        previewCanvas(
          div(
            cls := "flex flex-wrap items-center justify-center gap-2",
            Button(onClick --> { _ => Sonner.toast("Event has been created") }, "Toast"),
            Button(onClick --> { _ => Sonner.success("Successfully saved") }, "Success"),
            Button(onClick --> { _ => Sonner.error("Something went wrong") }, "Error"),
            Button(onClick --> { _ => Sonner.info("Did you know?") }, "Info"),
            Button(onClick --> { _ => Sonner.warning("Please review") }, "Warning"),
            Button(onClick --> { _ => Sonner.loading("Loading…") }, "Loading")
          )
        )
      case "spinner" => previewCanvas(Spinner())
      case "switch" =>
        previewCanvas(
          Switch(switchOn),
          span(
            cls := "text-sm text-muted-foreground",
            child.text <-- switchOn.signal.map(if _ then "Enabled" else "Disabled")
          )
        )
      case "table" =>
        previewCanvas(
          Table(
            Table.header(Table.row(Table.head("Component"), Table.head("Status"))),
            Table.body(
              Table.row(Table.cell("Drawer"), Table.cell(Badge("Ready"))),
              Table.row(Table.cell("Dialog"), Table.cell(Badge.of(_.variant(Badge.Variant.Secondary), _ => "Native")))
            )
          )
        )
      case "tabs" =>
        previewCanvas(
          div(
            cls := "flex w-full max-w-md flex-col gap-6",
            div(
              cls := "flex flex-col gap-2",
              p(cls := "text-sm font-medium", "Default"),
              Tabs.stateful(tabsDefaultSelected)(
                ("overview", "Overview", p(cls := "text-sm text-muted-foreground", "Overview panel content.")),
                ("usage", "Usage", p(cls := "text-sm text-muted-foreground", "Usage panel content."))
              )
            ),
            div(
              cls := "flex flex-col gap-2",
              p(cls := "text-sm font-medium", "Line"),
              Tabs.stateful(tabsLineSelected, Tabs.ListVariant.Line)(
                Tabs.Tab("overview", "Overview", p(cls := "text-sm text-muted-foreground", "Overview panel content.")),
                Tabs.Tab("usage", "Usage", p(cls := "text-sm text-muted-foreground", "Usage panel content."))
              )
            )
          )
        )
      case "textarea"       => previewCanvas(Textarea(placeholder := "Write a message…", cls := "max-w-sm"))
      case "theme-switcher" => previewCanvas(ThemeSwitcher(previewTheme))
      case "toast" =>
        previewCanvas(
          Toast(Toast.Variant.Default, Toast.title("Saved"), Toast.description("Everything is up to date."))
        )
      case "tooltip"    => previewCanvas(Tooltip("Helpful context", span("Hover me")))
      case "typography" => previewCanvas(typographyDemo())
      case "aspect-ratio" =>
        previewCanvas(
          div(
            cls := "w-full max-w-sm",
            AspectRatio(
              16.0 / 9.0,
              div(cls := "flex size-full items-center justify-center rounded-md bg-muted", "16:9")
            )
          )
        )
      case "calendar" => previewCanvas(Calendar(Var(Option.empty[js.Date])))
      case "carousel" =>
        previewCanvas(
          div(
            cls := "w-full max-w-sm",
            Carousel(
              div(cls := "flex h-32 items-center justify-center rounded-md border bg-muted", "Slide 1"),
              div(cls := "flex h-32 items-center justify-center rounded-md border bg-muted", "Slide 2"),
              div(cls := "flex h-32 items-center justify-center rounded-md border bg-muted", "Slide 3")
            )
          )
        )
      case "context-menu" =>
        previewCanvas(
          ContextMenu(ContextMenu.Item("Back", () => ()), ContextMenu.Item("Reload", () => ()))(
            div(
              cls := "flex h-32 w-64 items-center justify-center rounded-md border border-dashed text-sm text-muted-foreground",
              "Right click here"
            )
          )
        )
      case "data-table" =>
        div(cls := "w-full min-h-[450px] p-10", dataTableDemo())
      case "date-picker" =>
        previewCanvas(
          div(
            cls := "flex flex-col items-center gap-4",
            DatePicker(Var(Option.empty[js.Date])),
            DatePicker.withRange(Var((Option.empty[js.Date], Option.empty[js.Date])))
          )
        )
      case "range-calendar" =>
        previewCanvas(RangeCalendar(Var((Option.empty[js.Date], Option.empty[js.Date])), cls := "rounded-md border"))
      case "hover-card" =>
        previewCanvas(HoverCard(HoverCard.trigger("Hover me"), HoverCard.content(p("Laminar hover card content."))))
      case "input-otp" => previewCanvas(InputOTP(Var("")))
      case "menubar" =>
        previewCanvas(
          Menubar(
            Menubar.menu("File")(DropdownMenu.Item("New", () => ()), DropdownMenu.Item("Open", () => ())),
            Menubar.menu("Edit")(DropdownMenu.Item("Undo", () => ()), DropdownMenu.Item("Redo", () => ()))
          )
        )
      case "navigation-menu" =>
        previewCanvas(
          NavigationMenu(
            NavigationMenu.list(
              NavigationMenu.item(
                NavigationMenu.trigger("Getting Started"),
                NavigationMenu.content(
                  div(
                    cls := "grid gap-1 p-2 md:w-[400px] lg:w-[500px]",
                    NavigationMenu.link(href := "#", "Introduction"),
                    NavigationMenu.link(href := "#", "Installation"),
                    NavigationMenu.link(href := "#", "Typography")
                  )
                )
              ),
              NavigationMenu.item(
                NavigationMenu.trigger("Components"),
                NavigationMenu.content(
                  div(
                    cls := "grid gap-1 p-2 md:w-[400px] lg:w-[500px]",
                    NavigationMenu.link(href := "#", "Alert"),
                    NavigationMenu.link(href := "#", "Button"),
                    NavigationMenu.link(href := "#", "Card")
                  )
                )
              )
            )
          )
        )
      case "resizable" =>
        previewCanvas(
          div(
            cls := "h-32 w-full max-w-sm",
            Resizable.horizontal(Var(50.0))(
              div(cls := "flex h-full items-center justify-center bg-muted", "Left"),
              div(cls := "flex h-full items-center justify-center bg-muted", "Right")
            )
          )
        )
      case "separator" =>
        previewCanvas(
          div(
            cls := "flex w-full max-w-sm flex-col gap-4",
            p(cls := "text-sm", "Section one"),
            Separator(),
            p(cls := "text-sm", "Section two")
          )
        )
      case "sheet" =>
        previewCanvas(
          Button(onClick --> { _ => drawerOpen.set(true) }, "Open Sheet"),
          Sheet(drawerOpen)(
            Sheet.header(h2(cls := "text-lg font-semibold", "Edit profile")),
            Sheet.footer(Button(onClick --> { _ => drawerOpen.set(false) }, "Save"))
          )
        )
      case "toggle" => previewCanvas(Toggle(Var(false), Toggle.Variant.Default, Toggle.Size.Default, "B"))
      case "toggle-group" =>
        previewCanvas(
          ToggleGroup.single(
            Var(Option("bold")),
            Toggle.Variant.Default,
            Toggle.Size.Default,
            ToggleGroup.Item("bold", "B"),
            ToggleGroup.Item("italic", "I")
          )
        )
      case _ =>
        previewCanvas(
          Alert(
            Alert.Variant.Default,
            Alert.title(componentTitle),
            Alert.description("This component is available in the Laminar registry.")
          )
        )

    // Kept in exact 1:1 correspondence with liveExample()'s cases (same match, same order) so the code shown here is
    // always what actually produced the preview above it — update both together when changing a preview.
    val usageSource = componentName match
      case "accordion" =>
        """val openItem = Var(Option(0))

Accordion(
  openItem,
  Accordion.Section("What are your shipping options?", "We offer standard, express, and overnight shipping."),
  Accordion.Section("What is your return policy?", "Items can be returned within 30 days of delivery.")
)"""
      case "alert" =>
        """// An icon is optional, but must be a *direct* child of Alert — that is what switches
// on the icon grid column (has-[>svg]:grid-cols-*). A wrapper element defeats it.
Alert(
  Alert.Variant.Default,
  circleCheckIcon,
  Alert.title("Success! Your changes have been saved"),
  Alert.description("This is an alert with icon, title and description.")
)

// Title only — the description is optional too.
Alert(
  Alert.Variant.Default,
  infoIcon,
  Alert.title("This Alert has a title and an icon. No description.")
)

// No icon.
Alert(
  Alert.Variant.Default,
  Alert.title("This Alert has no icon"),
  Alert.description("Title and description line up in the same column either way.")
)

// The description is a grid, so block children stack with a gap.
Alert(
  Alert.Variant.Destructive,
  circleAlertIcon,
  Alert.title("Unable to process your payment."),
  Alert.description(
    p("Please verify your billing information and try again."),
    ul(
      cls := "list-inside list-disc text-sm",
      li("Check your card details"),
      li("Ensure sufficient funds"),
      li("Verify billing address")
    )
  )
)"""
      case "alert-dialog" =>
        """val isOpen = Var(false)

Button(onClick --> { _ => isOpen.set(true) }, "Open alert dialog")

AlertDialog(isOpen)(
  AlertDialog.title("Delete project?"),
  AlertDialog.description("This action cannot be undone."),
  AlertDialog.footer(Button(onClick --> { _ => isOpen.set(false) }, "Cancel"))
)"""
      case "avatar" => """Avatar(Avatar.fallback("LS"))"""
      case "badge" =>
        """Badge("New")
Badge.of(_.variant(Badge.Variant.Secondary), _ => "Beta")
Badge.of(_.variant(Badge.Variant.Outline), _ => "Outline")"""
      case "breadcrumb" =>
        """Breadcrumb(
  Breadcrumb.list(
    Breadcrumb.item(Breadcrumb.link("/", "Home")),
    Breadcrumb.separator(),
    Breadcrumb.item("Components")
  )
)"""
      case "button" =>
        """Button("Primary")
Button.of(_.variant(Button.Variant.Outline), _ => "Outline")
Button.of(_.variant(Button.Variant.Destructive), _ => "Delete")"""
      case "button-group" =>
        """ButtonGroup(
  Button.of(_.variant(Button.Variant.Outline), _ => "Back"),
  Button.of(_.variant(Button.Variant.Outline), _ => "Next")
)"""
      case "card" =>
        """Card(
  Card.header(Card.title("Project update"), Card.description("A Card composed from Laminar primitives.")),
  Card.content("Your latest deployment is ready.")
)"""
      case "chart"    => """Chart("Chart preview")"""
      case "checkbox" => """Checkbox(idAttr := "terms")
Label(forId := "terms", "Accept terms")"""
      case "collapsible" =>
        """Collapsible(
  Collapsible.trigger("Show details"),
  Collapsible.content(p("This is native details content."))
)"""
      case "combobox" =>
        """val frameworks = Seq(
  Combobox.Item("next.js", "Next.js"),
  Combobox.Item("sveltekit", "SvelteKit"),
  Combobox.Item("nuxt.js", "Nuxt.js"),
  Combobox.Item("remix", "Remix"),
  Combobox.Item("astro", "Astro")
)

// single-select — value toggles off if you click the same item again
Combobox(
  Var(Option.empty[String]),
  frameworks,
  placeholder = "Select framework…",
  searchPlaceholder = "Search framework…",
  emptyText = "No framework found."
)

// multi-select — picks render as removable chips on the trigger; the popover
// stays open after each pick so you can keep choosing
Combobox.multiple(
  Var(Set.empty[String]),
  frameworks,
  placeholder = "Select frameworks…",
  searchPlaceholder = "Search framework…",
  emptyText = "No framework found."
)"""
      case "command" =>
        """Command(
  Command.input(placeholder := "Search…"),
  Command.list(Command.item("Open settings"), Command.item("Create project"))
)"""
      case "dialog" =>
        """val isOpen = Var(false)

Button(onClick --> { _ => isOpen.set(true) }, "Open dialog")

Dialog(isOpen)(
  h2("Dialog"),
  p("Native HTML dialog."),
  Button(onClick --> { _ => isOpen.set(false) }, "Close")
)"""
      case "drawer" =>
        """val isOpen = Var(false)

Button(onClick --> { _ => isOpen.set(true) }, "Open Drawer")

Drawer(isOpen)(
  Drawer.header(h2("Edit profile"), p("Make changes to your public profile.")),
  Drawer.footer(
    Button.of(_.variant(Button.Variant.Outline), _ => "Cancel"),
    Button(onClick --> { _ => isOpen.set(false) }, "Save changes")
  )
)"""
      case "dropdown-menu" =>
        """DropdownMenu("Open menu")(
  DropdownMenu.Item("Profile", () => ()),
  DropdownMenu.Item("Settings", () => ())
)"""
      case "empty" =>
        """Empty(
  Empty.header(Empty.title("No projects"), Empty.description("Create your first project to get started."))
)"""
      case "field" =>
        """Field(
  Field.label("Email"),
  Input(placeholder := "you@example.com"),
  Field.description("We will never share your email.")
)

Field(
  Field.label("Password"),
  Input(`type` := "password"),
  Field.error(Seq("Password must be at least 8 characters."))
)

Field(
  Field.label("Username"),
  Input(placeholder := "jane"),
  Field.error(Seq("Username is required.", "Username must be unique."))
)"""
      case "form" =>
        """Form(
  Form.item(Form.label("Email"), Input(placeholder := "you@example.com")),
  Button("Submit")
)"""
      case "input" => """Input(placeholder := "Type something…")"""
      case "input-group" =>
        """InputGroup(
  InputGroup.addon(InputGroup.AddonAlign.InlineStart, InputGroup.text("https://")),
  InputGroup.input(placeholder := "example.com")
)

InputGroup(
  InputGroup.addon(InputGroup.AddonAlign.BlockStart, span("Description")),
  InputGroup.textarea(placeholder := "Enter your message…", rows := 3),
  InputGroup.addon(InputGroup.AddonAlign.BlockEnd, span("Markdown supported"))
)"""
      case "item" =>
        """Item(
  Item.content(Item.title("Laminar"), Item.description("Reactive Scala.js UI")),
  Item.actions(Badge("Stable"))
)"""
      case "kbd"           => """Kbd("⌘K")
Kbd.group(Kbd("⌘"), Kbd("P"))"""
      case "label"         => """Label("Email address")
Input(placeholder := "you@example.com")"""
      case "native-select" => """NativeSelect(option("Choose a plan"), option("Pro"), option("Team"))"""
      case "popover"       => """Popover(Popover.trigger("Open popover"), Popover.content("Popover content"))"""
      case "pagination" =>
        """Pagination(
  Pagination.list(
    Pagination.item(Pagination.link("#", false, "←")),
    Pagination.item(Pagination.link("#", true, "1")),
    Pagination.item(Pagination.link("#", false, "2")),
    Pagination.item(Pagination.link("#", false, "→"))
  )
)"""
      case "progress" => """Progress(68)"""
      case "radio"    => """Radio("plan", checked := true)
Label("Pro")
Radio("plan")
Label("Team")"""
      case "radio-group" =>
        """RadioGroup(
  Label(RadioGroup.item("plan", checked := true), "Pro"),
  Label(RadioGroup.item("plan"), "Team")
)"""
      case "range" => """Range(value := "50")"""
      case "scrollbar" =>
        """Scrollbar(
  p("Scrollable content"),
  div(styleAttr := "height:12rem"),
  p("End")
)"""
      case "scroll-area" =>
        """ScrollArea(
  p("Scrollable content rendered by the Laminar primitive."),
  div(styleAttr := "height:12rem"),
  p("End")
)"""
      case "select" => """Select(option("Choose a plan"), option("Pro"), option("Team"))"""
      case "sidebar" =>
        """Sidebar(
  Sidebar.header("Navigation"),
  Sidebar.content(Sidebar.menu(Sidebar.menuItem("Overview"), Sidebar.menuItem("Settings")))
)"""
      case "skeleton" => """Skeleton(cls := "h-20 w-full")"""
      case "slider"   => """Slider(value := "50")"""
      case "sonner" =>
        """// Mount once near your app root
Sonner.Toaster()

// Fire from event handlers anywhere
Sonner.toast("Event has been created")
Sonner.success("Successfully saved")
Sonner.error("Something went wrong")
Sonner.info("Did you know?")
Sonner.warning("Please review")
Sonner.loading("Loading…")"""
      case "spinner" => """Spinner()"""
      case "switch"  => """val enabled = Var(true)
Switch(enabled)"""
      case "table" =>
        """Table(
  Table.header(Table.row(Table.head("Component"), Table.head("Status"))),
  Table.body(
    Table.row(Table.cell("Drawer"), Table.cell(Badge("Ready"))),
    Table.row(Table.cell("Dialog"), Table.cell(Badge.of(_.variant(Badge.Variant.Secondary), _ => "Native")))
  )
)"""
      case "tabs" =>
        """val selected = Var("overview")
Tabs.stateful(selected)(
  ("overview", "Overview", p("Overview panel content.")),
  ("usage", "Usage", p("Usage panel content."))
)

Tabs.stateful(selected, Tabs.ListVariant.Line, cls := "w-full")(
  Tabs.Tab("overview", "Overview", p("Overview content."), Seq(cls := "flex-1")),
  Tabs.Tab("usage", "Usage", p("Usage content."), Seq(cls := "flex-1"))
)"""
      case "textarea"       => """Textarea(placeholder := "Write a message…")"""
      case "theme-switcher" => """val theme = Var(ThemeSwitcher.Theme.System)
ThemeSwitcher(theme)"""
      case "toast" =>
        """Toast(Toast.Variant.Default, Toast.title("Saved"), Toast.description("Everything is up to date."))"""
      case "tooltip" => """Tooltip("Helpful context", span("Hover me"))"""
      case "typography" =>
        """h1(cls := "scroll-m-20 text-4xl font-extrabold tracking-tight lg:text-5xl", "Heading 1")
p(cls := "leading-7 [&:not(:first-child)]:mt-6", "Paragraph")
p(cls := "text-xl text-muted-foreground", "Lead text")
code(cls := "relative rounded bg-muted px-[0.3rem] py-[0.2rem] font-mono text-sm font-semibold", "code")"""
      case "aspect-ratio" =>
        """AspectRatio(16.0 / 9.0, div(cls := "bg-muted flex items-center justify-center", "16:9"))"""
      case "calendar" => """val selected = Var(Option.empty[js.Date])
Calendar(selected)"""
      case "carousel" =>
        """Carousel(
  div("Slide 1"),
  div("Slide 2"),
  div("Slide 3")
)"""
      case "context-menu" =>
        """ContextMenu(ContextMenu.Item("Back", () => ()), ContextMenu.Item("Reload", () => ()))(
  div("Right click here")
)"""
      case "data-table" =>
        """final case class Payment(id: String, amount: Int, status: String, email: String)

val payments = Var(Seq(
  Payment("m5gr84i9", 316, "Success", "ken99@yahoo.com"),
  Payment("3u1reuv4", 242, "Success", "Abe45@gmail.com")
))

val table = DataTable.createTable(
  payments,
  Seq(
    DataTable.Column.text("status", "Status", _.status, p => div(cls := "capitalize", p.status)),
    DataTable.Column.text("email", "Email", _.email, p => div(cls := "lowercase", p.email))
  ),
  rowId = _.id,
  filterFn = (p, q, _) => p.email.toLowerCase.contains(q.toLowerCase)
)

Input(
  placeholder := "Filter emails...",
  controlled(value <-- table.globalFilter.signal, onInput.mapToValue --> table.globalFilter)
)

table.view(_.id)"""
      case "date-picker"    => """val selected = Var(Option.empty[js.Date])
DatePicker(selected)

val range = Var((Option.empty[js.Date], Option.empty[js.Date]))
DatePicker.withRange(range)"""
      case "range-calendar" => """val range = Var((Option.empty[js.Date], Option.empty[js.Date]))
RangeCalendar(range, cls := "rounded-md border")"""
      case "hover-card" =>
        """HoverCard(
  HoverCard.trigger("Hover me"),
  HoverCard.content(p("Laminar hover card content."))
)"""
      case "input-otp" => """val code = Var("")
InputOTP(code)"""
      case "menubar" =>
        """Menubar(
  Menubar.menu("File")(DropdownMenu.Item("New", () => ()), DropdownMenu.Item("Open", () => ())),
  Menubar.menu("Edit")(DropdownMenu.Item("Undo", () => ()), DropdownMenu.Item("Redo", () => ()))
)"""
      case "navigation-menu" =>
        """NavigationMenu(
  NavigationMenu.list(
    NavigationMenu.item(
      NavigationMenu.trigger("Getting Started"),
      NavigationMenu.content(
        div(
          NavigationMenu.link(href := "#", "Introduction"),
          NavigationMenu.link(href := "#", "Installation")
        )
      )
    ),
    NavigationMenu.item(
      NavigationMenu.trigger("Components"),
      NavigationMenu.content(
        div(
          NavigationMenu.link(href := "#", "Alert"),
          NavigationMenu.link(href := "#", "Button")
        )
      )
    )
  )
)"""
      case "resizable" =>
        """val split = Var(50.0)
Resizable.horizontal(split)(div("Left"), div("Right"))"""
      case "separator" => """p("Section one")
Separator()
p("Section two")"""
      case "sheet" =>
        """val isOpen = Var(false)

Button(onClick --> { _ => isOpen.set(true) }, "Open Sheet")

Sheet(isOpen)(
  Sheet.header(h2("Edit profile")),
  Sheet.footer(Button(onClick --> { _ => isOpen.set(false) }, "Save"))
)"""
      case "toggle" => """val pressed = Var(false)
Toggle(pressed, Toggle.Variant.Default, Toggle.Size.Default, "B")"""
      case "toggle-group" =>
        """ToggleGroup.single(
  Var(Option("bold")),
  ToggleGroup.Item("bold", "B"),
  ToggleGroup.Item("italic", "I")
)"""
      case _ => s"""$componentTitle(/* Laminar modifiers */)"""

    div(
      cls := "min-h-dvh bg-background text-foreground antialiased",
      headerTag(
        cls := "sticky inset-x-0 top-0 z-40 border-b bg-background/95 backdrop-blur",
        div(
          cls := "flex h-14 items-center gap-3 px-4",
          a(
            href := "/",
            cls := "flex items-center gap-2 text-sm font-semibold",
            span(cls := "[&_svg]:size-4", foreignHtmlElement(logoEl)),
            "shadcn-scalajs"
          ),
          navTag(
            cls := "hidden items-center gap-1 md:flex",
            a(cls := btnGhost, href := "/", "Home"),
            a(cls := btnGhost, href := "/components", "Docs"),
            a(
              cls := btnGhost + " bg-accent text-accent-foreground",
              href := s"/components/$componentName",
              "Components"
            ),
            a(cls := btnGhost, href := "/blocks", "Blocks"),
            a(cls := btnGhost, href := "/create", "Create")
          ),
          div(
            cls := "ml-auto flex items-center gap-2",
            ThemeMenu()
          )
        )
      ),
      div(
        cls := "mx-auto grid w-full max-w-[1800px] grid-cols-1 lg:grid-cols-[15rem_minmax(0,1fr)] xl:grid-cols-[15rem_minmax(0,1fr)_15rem]",
        asideTag(
          cls := "hidden border-r lg:block",
          navTag(
            cls := "sticky top-14 h-[calc(100svh-3.5rem)] overflow-y-auto px-4 py-8",
            aria.label := "Component navigation",
            p(cls := "mb-2 px-2 text-xs font-medium text-muted-foreground", "SECTIONS"),
            a(
              href := "/",
              cls := "block rounded-md px-2 py-1.5 text-sm text-muted-foreground hover:bg-accent hover:text-foreground",
              "Introduction"
            ),
            a(href := "/components", cls := "block rounded-md bg-accent px-2 py-1.5 text-sm font-medium", "Components"),
            a(
              href := "/blocks",
              cls := "block rounded-md px-2 py-1.5 text-sm text-muted-foreground hover:bg-accent hover:text-foreground",
              "Blocks"
            ),
            p(cls := "mb-2 mt-7 px-2 text-xs font-medium text-muted-foreground", "COMPONENTS"),
            componentNavList.map(navLink)
          )
        ),
        mainTag(
          cls := "min-w-0 px-5 py-10 sm:px-8 lg:px-10",
          articleTag(
            cls := articleWidthCls,
            div(
              cls := "mb-8 flex items-start justify-between gap-4",
              div(
                h1(cls := "text-3xl font-semibold tracking-tight", componentTitle),
                p(cls := "mt-2 text-base text-muted-foreground", componentDescription)
              ),
              button(typ := "button", cls := btnOutline + " hidden shrink-0 sm:inline-flex", "Copy page")
            ),
            div(
              idAttr := "about",
              cls := "scroll-mt-24",
              h2(cls := "text-xl font-semibold", "About"),
              p(
                cls := "mt-3 text-sm leading-6 text-muted-foreground",
                if componentName == "typography" then
                  "We do not ship typography styles by default. This page shows utility-class recipes you can copy into your Laminar elements."
                else s"${componentTitle} is available as a direct Laminar component and through the generated registry."
              )
            ),
            div(cls := "mt-8 rounded-md border bg-card", liveExample()),
            if componentName != "typography" then
              div(
                idAttr := "installation",
                cls := "mt-12 scroll-mt-24",
                h2(cls := "text-xl font-semibold", "Installation"),
                p(cls := "mt-3 text-sm text-muted-foreground", "Add the component through the local registry CLI."),
                codeBlock("shell", s"npx shadcn-scalajs add $componentName")
              )
            else
              div(
                idAttr := "installation",
                cls := "mt-12 scroll-mt-24",
                h2(cls := "text-xl font-semibold", "Installation"),
                p(
                  cls := "mt-3 text-sm text-muted-foreground",
                  "No registry component — upstream shadcn/ui documents typography as Tailwind utility recipes. Copy the class strings from the preview below."
                )
              )
            ,
            div(
              idAttr := "usage",
              cls := "mt-12 scroll-mt-24",
              h2(cls := "text-xl font-semibold", "Usage"),
              p(
                cls := "mt-3 text-sm text-muted-foreground",
                "Import the primitive and compose it with Laminar modifiers."
              ),
              codeBlock("scala", usageSource)
            ),
            div(
              idAttr := "examples",
              cls := "mt-12 scroll-mt-24",
              h2(cls := "text-xl font-semibold", "Examples"),
              h3(
                idAttr := "sides",
                cls := "mt-6 text-base font-semibold",
                if componentName == "drawer" then "Sides" else "Composition"
              ),
              p(
                cls := "mt-2 text-sm leading-6 text-muted-foreground",
                if componentName == "drawer" then
                  "The current native Drawer defaults to a bottom sheet. The same composition API is ready for top, right, bottom, and left variants."
                else "Compose this primitive with Card, Field, Button, and the other Laminar components."
              ),
              div(cls := "mt-4 rounded-md border bg-card", liveExample())
            ),
            div(
              cls := "mt-14 flex items-center justify-between border-t pt-6 text-sm",
              prevEntry match
                case Some(name) =>
                  a(
                    href := s"/components/${slugify(name)}",
                    cls := "text-muted-foreground hover:text-foreground",
                    s"← $name"
                  )
                case None => emptyNode,
              nextEntry match
                case Some(name) =>
                  a(
                    href := s"/components/${slugify(name)}",
                    cls := "text-muted-foreground hover:text-foreground",
                    s"$name →"
                  )
                case None => emptyNode
            )
          )
        ),
        tableOfContents
      ),
      Sonner.Toaster()
    )
