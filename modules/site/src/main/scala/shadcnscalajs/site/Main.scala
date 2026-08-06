package shadcnscalajs.site

import com.raquo.laminar.api.L.*
import com.raquo.laminar.codecs.StringAsIsCodec
import org.scalajs.dom
import shadcnscalajs.ui.*

import scala.scalajs.js

object Main:

  private val viewBoxA = htmlAttr("viewBox", StringAsIsCodec)
  private val preserveAspectRatioA = htmlAttr("preserveAspectRatio", StringAsIsCodec)
  private val fillA = htmlAttr("fill", StringAsIsCodec)
  private val strokeA = htmlAttr("stroke", StringAsIsCodec)
  private val strokeWidthA = htmlAttr("stroke-width", StringAsIsCodec)
  private val opacityA = htmlAttr("opacity", StringAsIsCodec)
  private val vectorEffectA = htmlAttr("vector-effect", StringAsIsCodec)
  private val dA = htmlAttr("d", StringAsIsCodec)
  private val xA = htmlAttr("x", StringAsIsCodec)
  private val yA = htmlAttr("y", StringAsIsCodec)
  private val wA = htmlAttr("width", StringAsIsCodec)
  private val hA = htmlAttr("height", StringAsIsCodec)

  private lazy val kbdEl = htmlTag("kbd")
  private lazy val svgEl = htmlTag("svg")
  private lazy val pathEl = htmlTag("path")
  private lazy val rectEl = htmlTag("rect")
  private lazy val ulEl = htmlTag[dom.HTMLElement]("ul")
  private lazy val liEl = htmlTag[dom.HTMLElement]("li")
  private lazy val hrEl = htmlTag[dom.HTMLElement]("hr")
  private lazy val olEl = htmlTag[dom.HTMLElement]("ol")

  def main(args: Array[String]): Unit =
    val pathname = dom.window.location.pathname
    val page =
      if pathname == "/components" || pathname == "/components/" then componentsGalleryPage()
      else if pathname.startsWith("/components/") then componentDocsPage()
      else if pathname.startsWith("/blocks/") && pathname.endsWith("/preview") then
        BlockPreviewPage(pathname.stripPrefix("/blocks/").stripSuffix("/preview"))
      else if pathname == "/blocks" || pathname == "/blocks/" then BlocksIndexPage()
      else if pathname.startsWith("/blocks/") then BlockDocsPage(pathname.stripPrefix("/blocks/").stripSuffix("/"))
      else app()
    render(dom.document.getElementById("root"), page)

  // ── SVG Icons ──
  private def iconSvg(p: String) =
    s"""<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">$p</svg>"""
  private def iconArrowRight = iconSvg("<path d='M5 12h14'/><path d='m12 5 7 7-7 7'/>")
  private def iconSearch = iconSvg("<circle cx='11' cy='11' r='8'/><path d='m21 21-4.3-4.3'/>")
  def iconSun = iconSvg(
    "<circle cx='12' cy='12' r='4'/><path d='M12 2v2M12 20v2m-7.07-17.07 1.41 1.41m9.32 9.32 1.41 1.41M2 12h2m16 0h2M6.34 17.66l-1.41 1.41M19.07 4.93l-1.41 1.41'/>"
  )
  def iconMoon = iconSvg("<path d='M12 3a6 6 0 0 0 9 9 9 9 0 1 1-9-9Z'/>")
  private def iconChevronRight = iconSvg("<path d='m9 18 6-6-6-6'/>")
  private def iconChevronUp = iconSvg("<path d='m18 15-6-6-6 6'/>")
  private def iconChart = iconSvg("<path d='M10 3H3v18h18v-7M8 16v-5M12 16V8M16 16v-3'/>")
  private def iconPieChart = iconSvg("<path d='M21.21 15.89A10 10 0 1 1 8 2.83M22 12A10 10 0 0 0 12 2v10Z'/>")
  private def iconTrendingUp = iconSvg(
    "<polyline points='22 7 13.5 15.5 8.5 10.5 2 17'/><polyline points='16 7 22 7 22 13'/>"
  )
  private def iconLandmark = iconSvg(
    "<line x1='3' x2='21' y1='22' y2='22'/><line x1='6' x2='6' y1='18' y2='11'/><line x1='10' x2='10' y1='18' y2='11'/><line x1='14' x2='14' y1='18' y2='11'/><line x1='18' x2='18' y1='18' y2='11'/><polygon points='12 2 20 7 4 7'/>"
  )
  private def iconBanknote = iconSvg(
    "<path d='M12 5v14M18 11v4M6 11v4'/><rect width='20' height='14' x='2' y='5' rx='2'/>"
  )
  private def iconFileText = iconSvg(
    "<path d='M15 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V7ZM14 2v4a2 2 0 0 0 2 2h4M10 9H8M16 13H8M16 17H8'/>"
  )
  private def iconWallet = iconSvg(
    "<path d='M19 7V4a1 1 0 0 0-1-1H5a2 2 0 0 0 0 4h15a1 1 0 0 1 1 1v4h-3a2 2 0 0 0 0 4h3a1 1 0 0 0 1-1v-2a1 1 0 0 0-1-1M3 5v14a2 2 0 0 0 2 2h15a1 1 0 0 0 1-1v-4'/>"
  )
  private def iconTarget = iconSvg(
    "<circle cx='12' cy='12' r='10'/><circle cx='12' cy='12' r='6'/><circle cx='12' cy='12' r='2'/>"
  )
  private def iconCalendar = iconSvg(
    "<rect width='18' height='18' x='3' y='4' rx='2'/><line x1='16' x2='16' y1='2' y2='6'/><line x1='8' x2='8' y1='2' y2='6'/><line x1='3' x2='21' y1='10' y2='10'/><path d='M8 14h.01M12 14h.01M16 14h.01M8 18h.01M12 18h.01M16 18h.01'/>"
  )
  private def iconHelpCircle = iconSvg(
    "<circle cx='12' cy='12' r='10'/><path d='M9.09 9a3 3 0 0 1 5.83 1c0 2-3 3-3 3M12 17h.01'/>"
  )
  private def iconBookOpen = iconSvg(
    "<path d='M2 3h6a4 4 0 0 1 4 4v14a3 3 0 0 0-3-3H2zM22 3h-6a4 4 0 0 0-4 4v14a3 3 0 0 1 3-3h7z'/>"
  )
  private def iconMessageCircle = iconSvg("<path d='M7.9 20A9 9 0 1 0 4 16.1L2 22Z'/>")
  private def iconActivity = iconSvg("<path d='M22 12h-4l-3 9L9 3l-3 9H2'/>")
  private def iconGlobe = iconSvg(
    "<circle cx='12' cy='12' r='10'/><path d='M12 2a14.5 14.5 0 0 0 0 20 14.5 14.5 0 0 0 0-20M2 12h20'/>"
  )
  private def iconUser = iconSvg("<path d='M19 21v-2a4 4 0 0 0-4-4H9a4 4 0 0 0-4 4v2'/><circle cx='12' cy='7' r='4'/>")
  private def iconCreditCard = iconSvg(
    "<rect width='20' height='14' x='2' y='5' rx='2'/><line x1='2' x2='22' y1='10' y2='10'/>"
  )
  private def iconBell = iconSvg("<path d='M6 8a6 6 0 0 1 12 0c0 7 3 9 3 9H3s3-2 3-9M10.3 21a1.94 1.94 0 0 0 3.4 0'/>")
  private def iconShield = iconSvg(
    "<path d='M20 13c0 5-3.5 7.5-7.66 8.95a1 1 0 0 1-.67-.01C7.5 20.5 4 18 4 13V6a1 1 0 0 1 1-1c2 0 4.5-1.2 6.24-2.72a1.06 1.06 0 0 1 1.52 0C14.51 3.81 17 5 19 5a1 1 0 0 1 1 1z'/>"
  )
  private def iconPaintbrush = iconSvg(
    "<path d='M18.37 2.63 14 7l-1.59-1.59a2 2 0 0 0-2.82 0L8 7l9 9 1.59-1.59a2 2 0 0 0 0-2.82L17 10l4.37-4.37a2.12 2.12 0 1 0-3-3ZM9 8c-2 3-4 3.5-7 4l8 10c2-1 6-5 5-7M14.5 17.5 4.5 15'/>"
  )
  private def iconLock = iconSvg(
    "<rect width='18' height='11' x='3' y='11' rx='2'/><path d='M7 11V7a5 5 0 0 1 10 0v4'/>"
  )
  private def iconInfo = iconSvg("<circle cx='12' cy='12' r='10'/><path d='M12 16v-4M12 8h.01'/>")
  private def iconSettings = iconSvg(
    "<path d='M12.22 2h-.44a2 2 0 0 0-2 2v.18a2 2 0 0 1-1 1.73l-.43.25a2 2 0 0 1-2 0l-.15-.08a2 2 0 0 0-2.73.73l-.22.38a2 2 0 0 0 .73 2.73l.15.1a2 2 0 0 1 1 1.72v.51a2 2 0 0 1-1 1.74l-.15.09a2 2 0 0 0-.73 2.73l.22.38a2 2 0 0 0 2.73.73l.15-.08a2 2 0 0 1 2 0l.43.25a2 2 0 0 1 1 1.73V20a2 2 0 0 0 2 2h.44a2 2 0 0 0 2-2v-.18a2 2 0 0 1 1-1.73l.43-.25a2 2 0 0 1 2 0l.15.08a2 2 0 0 0 2.73-.73l.22-.39a2 2 0 0 0-.73-2.73l-.15-.08a2 2 0 0 1-1-1.74v-.5a2 2 0 0 1 1-1.74l.15-.09a2 2 0 0 0 .73-2.73l-.22-.38a2 2 0 0 0-2.73-.73l-.15.08a2 2 0 0 1-2 0l-.43-.25a2 2 0 0 1-1-1.73V4a2 2 0 0 0-2-2z'/><circle cx='12' cy='12' r='3'/>"
  )
  private def iconRefresh = iconSvg(
    "<path d='M3 12a9 9 0 0 1 9-9 9.75 9.75 0 0 1 6.74 2.74L21 8M21 3v5h-5M21 12a9 9 0 0 1-9 9 9.75 9.75 0 0 1-6.74-2.74L3 16M3 21v-5h5'/>"
  )
  private def iconPlus = iconSvg("<path d='M5 12h14M12 5v14'/>")
  private def iconCircleCheck = iconSvg("<path d='M21.801 10A10 10 0 1 1 17 3.335'/><path d='m9 11 3 3L22 4'/>")
  private def iconCircleAlert = iconSvg(
    "<circle cx='12' cy='12' r='10'/><path d='M12 8v4'/><path d='M12 16h.01'/>"
  )
  private def iconEllipsis = iconSvg(
    "<circle cx='12' cy='12' r='1'/><circle cx='19' cy='12' r='1'/><circle cx='5' cy='12' r='1'/>"
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

  private val qrRows = List(
    "111111100101101111111",
    "100000101001001000001",
    "101110101111101011101",
    "101110100100001011101",
    "101110101010101011101",
    "100000100111001000001",
    "111111101010101111111",
    "000000001101000000000",
    "101011111001111010110",
    "010100001110010101001",
    "111010111011101111010",
    "001101000101000010101",
    "110111101111010111011",
    "000000001001010001010",
    "111111101101111101001",
    "100000100010001001111",
    "101110101011101110100",
    "101110100110100010011",
    "101110101000111101110",
    "100000101101000011001",
    "111111101011101101111"
  )

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
    "Resizable",
    "Scroll Area",
    "Scrollbar",
    "Select",
    "Separator",
    "Sheet",
    "Sidebar",
    "Skeleton",
    "Slider",
    "Spinner",
    "Switch",
    "Table",
    "Tabs",
    "Textarea",
    "Theme Switcher",
    "Toast",
    "Toggle",
    "Toggle Group",
    "Tooltip"
  )

  private def slugify(name: String): String = name.toLowerCase.replace(' ', '-')

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
    val themeConfig = Var(ThemeConfig.load())
    val dialogOpen = Var(false)

    div(
      cls := "min-h-dvh overflow-x-clip bg-background text-foreground antialiased",
      themeConfig.signal --> { cfg => ThemeConfig.applyToDocument(cfg) },

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
            select(
              cls := "hidden sm:block h-8 w-28 shrink-0 rounded-md border border-input bg-background px-2 text-sm",
              aria.label := "Style pack",
              value <-- themeConfig.signal.map(_.stylePack),
              onChange --> { ev =>
                val next = themeConfig.now().copy(stylePack = ev.target.asInstanceOf[dom.html.Select].value)
                themeConfig.set(next)
                ThemeConfig.store(next)
              },
              option(value := "vega", "Vega"),
              option(value := "nova", "Nova"),
              option(value := "maia", "Maia"),
              option(value := "lyra", "Lyra"),
              option(value := "mira", "Mira"),
              option(value := "luma", "Luma"),
              option(value := "sera", "Sera"),
              option(value := "rhea", "Rhea")
            ),
            button(
              typ := "button",
              cls := s"$btnIcon hidden sm:inline-flex",
              aria.label := "Toggle dark mode",
              onClick --> { _ =>
                val next = themeConfig.now().copy(darkMode = !themeConfig.now().darkMode)
                themeConfig.set(next)
                ThemeConfig.store(next)
              },
              span(cls := "hidden dark:block", rawIcon(iconSun)),
              span(cls := "block dark:hidden", rawIcon(iconMoon))
            )
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
                a(cls := btnPrimary, href := "/components", "Get started"),
                a(
                  cls := btnOutline,
                  href := "https://github.com/lamtanloc512/shadcn-scalajs",
                  target := "_blank",
                  rel := "noopener",
                  "Source code"
                )
              )
            )
          )
        ),

        // ── Dashboard ──
        sectionTag(
          cls := "flex-1 p-0",
          div(
            cls := "w-full overflow-hidden",
            div(
              cls := "relative flex min-h-[980px] w-full max-w-none flex-col overflow-visible bg-muted p-12 pb-12 lg:p-6 lg:pb-12 xl:p-8 xl:pb-12 min-[1900px]:min-h-[1120px] min-[1900px]:p-12 dark:bg-background",
              styleAttr := "--chart-1: oklch(0.87 0 0); --chart-2: oklch(0.556 0 0);",
              div(
                cls := "relative z-10 mx-auto grid w-full grid-cols-1 gap-6 md:max-w-3xl md:grid-cols-2 lg:max-w-none lg:grid-cols-3 xl:max-w-[1600px] min-[1400px]:grid-cols-4 min-[1900px]:grid-cols-5 lg:gap-8",

                // ═══ COLUMN 1 ═══
                div(
                  cls := "flex min-w-0 flex-col gap-6",
                  div(
                    cls := "rounded-xl border bg-card shadow-sm p-6 flex flex-col gap-6",
                    div(
                      cls := "flex flex-wrap gap-2",
                      button(typ := "button", cls := btnPrimary, "Button ", rawIcon(iconArrowRight)),
                      button(
                        typ := "button",
                        cls := "inline-flex shrink-0 items-center justify-center gap-2 rounded-md text-sm font-medium whitespace-nowrap transition-all outline-none focus-visible:border-ring focus-visible:ring-[3px] focus-visible:ring-ring/50 disabled:pointer-events-none disabled:opacity-50 bg-secondary text-secondary-foreground hover:bg-secondary/80 h-9 px-4 py-2",
                        "Secondary"
                      ),
                      button(typ := "button", cls := btnOutline, "Outline")
                    ),
                    div(
                      cls := "flex items-center rounded-md border border-input bg-background px-3 py-1 text-sm",
                      input(placeholder := "Name", cls := "bg-transparent outline-none flex-1"),
                      rawIcon(iconSearch)
                    ),
                    textArea(
                      cls := "flex min-h-20 w-full rounded-md border border-input bg-background px-3 py-2 text-sm resize-none",
                      placeholder := "Message"
                    ),
                    div(
                      cls := "flex items-center gap-2 flex-wrap",
                      div(
                        cls := "flex gap-2",
                        Badge("Badge"),
                        Badge.of(_.variant(Badge.Variant.Secondary), _ => "Secondary"),
                        div(
                          cls := "hidden min-[1900px]:flex",
                          Badge.of(_.variant(Badge.Variant.Outline), _ => "Outline")
                        )
                      ),
                      div(
                        cls := "ml-auto flex w-fit gap-3",
                        role := "radiogroup",
                        aria.label := "Fruit preference",
                        input(
                          cls := "size-4 rounded-full border border-primary accent-primary",
                          typ := "radio",
                          nameAttr := "fruit",
                          aria.label := "Apple",
                          checked := true
                        ),
                        input(
                          cls := "size-4 rounded-full border border-primary accent-primary",
                          typ := "radio",
                          nameAttr := "fruit",
                          aria.label := "Banana"
                        )
                      ),
                      div(
                        cls := "flex gap-3",
                        input(
                          cls := "size-4 rounded border border-primary accent-primary",
                          typ := "checkbox",
                          aria.label := "Enable email alerts",
                          checked := true
                        ),
                        input(
                          cls := "hidden min-[1900px]:flex size-4 rounded border border-primary accent-primary",
                          typ := "checkbox",
                          aria.label := "Enable push alerts"
                        )
                      ),
                      input(
                        cls := "hidden min-[1900px]:flex h-5 w-9 rounded-full border border-input accent-primary",
                        typ := "checkbox",
                        role := "switch",
                        aria.label := "Enable compact notifications",
                        checked := true
                      )
                    ),
                    div(
                      cls := "flex flex-wrap items-center gap-3",
                      button(
                        typ := "button",
                        cls := btnOutline,
                        onClick --> { _ => dialogOpen.set(true) },
                        "Alert Dialog"
                      ),
                      div(
                        cls := "ml-auto max-w-full",
                        div(
                          cls := "inline-flex rounded-md shadow-xs",
                          button(
                            cls := "inline-flex items-center justify-center rounded-l-md rounded-r-none border border-r-0 bg-background px-4 py-2 text-sm font-medium hover:bg-accent hover:text-accent-foreground min-w-0",
                            typ := "button",
                            span(cls := "truncate", "Button Group")
                          ),
                          button(
                            cls := "inline-flex items-center justify-center rounded-r-md border bg-background p-2 text-sm font-medium hover:bg-accent hover:text-accent-foreground size-9",
                            typ := "button",
                            aria.label := "Open quick actions",
                            rawIcon(iconChevronUp)
                          )
                        )
                      )
                    )
                  ),
                  Dialog(dialogOpen)(
                    div(
                      cls := "flex flex-col gap-4",
                      headerTag(
                        h2(cls := "text-lg font-semibold", "Allow accessory to connect?"),
                        p(
                          cls := "text-sm text-muted-foreground",
                          "Do you want to allow the USB accessory to connect to this device and your data?"
                        )
                      ),
                      footerTag(
                        cls := "flex justify-end gap-2",
                        button(
                          typ := "button",
                          cls := btnOutline,
                          onClick --> { _ => dialogOpen.set(false) },
                          "Don't allow"
                        ),
                        button(
                          typ := "button",
                          cls := btnPrimary,
                          onClick --> { _ => dialogOpen.set(false) },
                          "Allow"
                        )
                      )
                    )
                  ),

                  // Sidebar nav 2×2
                  div(
                    cls := "grid grid-cols-2 gap-4",
                    navSection(
                      "Overview",
                      Some("Analytics"),
                      ("Analytics", iconChart),
                      ("Transactions", iconBanknote),
                      ("Investments", iconTrendingUp),
                      ("Accounts", iconLandmark),
                      ("Spending", iconPieChart)
                    ),
                    navSection(
                      "Account",
                      Some("Billing"),
                      ("Profile", iconUser),
                      ("Billing", iconCreditCard),
                      ("Notifications", iconBell),
                      ("Security", iconShield),
                      ("Appearance", iconPaintbrush)
                    ),
                    navSection(
                      "Planning",
                      None,
                      ("Documents", iconFileText),
                      ("Budget", iconWallet),
                      ("Reports", iconChart),
                      ("Goals", iconTarget),
                      ("Calendar", iconCalendar)
                    ),
                    navSection(
                      "Support",
                      None,
                      ("Help Center", iconHelpCircle),
                      ("Docs", iconBookOpen),
                      ("Contact Us", iconMessageCircle),
                      ("Status", iconActivity),
                      ("Community", iconGlobe)
                    )
                  ),

                  // Savings Targets
                  div(
                    cls := "rounded-xl border bg-card shadow-sm flex flex-col",
                    headerTag(
                      cls := "p-6 pb-3",
                      h2(cls := "text-lg font-semibold", "Savings Targets"),
                      p(
                        cls := "text-sm text-muted-foreground",
                        "Active milestones for 2024 across your portfolio. Monitor how close you are to each savings goal."
                      )
                    ),
                    div(
                      cls := "px-6 flex-1 flex flex-col gap-3",
                      savingsCard("Retirement", "$420,000", "65% achieved", "$273,000", "65%"),
                      savingsCard("Real Estate", "$85,000", "32% achieved", "$27,200", "32%")
                    ),
                    footerTag(
                      cls := "px-6 pb-6",
                      p(
                        cls := "mx-auto text-center text-sm text-muted-foreground",
                        "You have not met your targets for this year."
                      )
                    )
                  )
                ),

                // ═══ COLUMN 2 ═══
                div(
                  cls := "hidden lg:flex min-w-0 flex-col gap-6",
                  div(
                    cls := "rounded-xl border bg-card shadow-sm flex flex-col",
                    headerTag(
                      cls := "p-6 pb-3",
                      h2(cls := "text-lg font-semibold", "Contribution History"),
                      p(cls := "text-sm text-muted-foreground", "Last 6 months of activity")
                    ),
                    div(
                      cls := "px-6 flex-1 flex flex-col gap-4",
                      div(
                        cls := "flex h-[200px] w-full items-end gap-3",
                        role := "img",
                        aria.label := "Last 6 months of contribution activity",
                        chartBar("Dec", 57),
                        chartBar("Jan", 79),
                        chartBar("Feb", 64),
                        chartBar("Mar", 93),
                        chartBar("Apr", 54),
                        chartBar("May", 100)
                      ),
                      div(
                        cls := "grid grid-cols-1 xl:grid-cols-2 gap-3",
                        savingsMini("Upcoming", "May 2024", "Scheduled"),
                        savingsMini("Savings Plan", "Accelerated", "Recurring")
                      )
                    ),
                    footerTag(
                      cls := "px-6 pb-6",
                      button(typ := "button", cls := btnPrimary + " w-full", "View Full Report")
                    )
                  ),
                  div(
                    cls := "rounded-xl border bg-card shadow-sm flex flex-col",
                    headerTag(
                      cls := "p-6 pb-3",
                      p(cls := "text-sm text-muted-foreground", "Claimable Balance"),
                      h2(cls := "text-4xl tabular-nums font-semibold", "$1,211.29"),
                      Badge.of(
                        _.variant(Badge.Variant.Outline),
                        _ => span(cls := "size-2 rounded-full bg-yellow-500 inline-block mr-1 align-middle"),
                        _ => " Pending Setup"
                      )
                    ),
                    div(
                      cls := "px-6 flex-1 flex flex-col justify-end",
                      div(
                        cls := "rounded-md bg-muted/50 p-4",
                        div(
                          cls := "flex justify-between",
                          span(cls := "text-muted-foreground", "Net Royalties"),
                          span(cls := "font-medium tabular-nums", "$1,248.75")
                        ),
                        div(
                          cls := "mt-3 flex justify-between",
                          span(cls := "text-muted-foreground", "Processing Fee"),
                          span(cls := "font-medium tabular-nums", "-$37.46")
                        ),
                        hrEl(cls := "my-3 border-border"),
                        div(
                          cls := "flex justify-between",
                          span(cls := "text-muted-foreground", "Total Ready to Claim"),
                          span(cls := "font-semibold tabular-nums", "$1,211.29 USD")
                        )
                      )
                    ),
                    footerTag(
                      cls := "px-6 pb-6",
                      p(
                        cls := "text-sm text-muted-foreground",
                        "Once your bank is connected, balances over $10.00 are automatically eligible for monthly distribution on the 15th of each month."
                      )
                    )
                  ),
                  div(
                    cls := "rounded-xl border bg-card shadow-sm flex flex-col",
                    headerTag(
                      cls := "p-6 pb-3",
                      h2(cls := "text-lg font-semibold", "Q2 Dividend Income"),
                      p(
                        cls := "text-sm text-muted-foreground",
                        "Quarterly dividend payouts across your portfolio holdings."
                      )
                    ),
                    div(
                      cls := "px-6 flex-1 flex flex-col gap-2",
                      dividendItem("Vanguard", "450 Shares", List(58, 64, 60, 100)),
                      dividendItem("S&P 500 VOO", "112 Shares", List(56, 66, 100, 68)),
                      dividendItem("Apple AAPL", "85 Shares", List(50, 58, 100, 75)),
                      dividendItem("Realty Income", "320 Shares", List(67, 72, 78, 100))
                    )
                  )
                ),

                // ═══ COLUMN 3 ═══
                div(
                  cls := "hidden min-[1900px]:flex min-w-0 flex-col gap-6",
                  div(
                    cls := "rounded-xl border bg-card shadow-sm flex flex-col",
                    headerTag(
                      cls := "p-6 pb-3",
                      h2(cls := "text-lg font-semibold", "Set a new milestone"),
                      p(
                        cls := "text-sm text-muted-foreground",
                        "Define your financial target and we'll help you pace your savings."
                      )
                    ),
                    div(
                      cls := "px-6 flex-1 flex flex-col gap-4",
                      div(
                        label(cls := "text-sm font-medium block mb-1", "Goal Name"),
                        input(
                          cls := "flex h-9 w-full rounded-md border border-input bg-background px-3 py-1 text-sm",
                          placeholder := "e.g. New Car, Home Downpayment"
                        )
                      ),
                      div(
                        cls := "grid grid-cols-2 gap-3",
                        div(
                          label(cls := "text-sm font-medium block mb-1", "Target Amount"),
                          input(
                            cls := "flex h-9 w-full rounded-md border border-input bg-background px-3 py-1 text-sm",
                            value := "$15,000"
                          )
                        ),
                        div(
                          label(cls := "text-sm font-medium block mb-1", "Target Date"),
                          input(
                            cls := "flex h-9 w-full rounded-md border border-input bg-background px-3 py-1 text-sm",
                            value := "Dec 2025"
                          )
                        )
                      )
                    ),
                    footerTag(
                      cls := "px-6 pb-6 flex flex-col gap-2",
                      button(typ := "button", cls := btnPrimary + " w-full", "Create Goal"),
                      button(typ := "button", cls := btnOutline + " w-full", "Cancel")
                    )
                  ),
                  div(
                    cls := "rounded-xl border bg-card shadow-sm flex flex-col",
                    headerTag(
                      cls := "p-6 pb-3",
                      h2(cls := "text-lg font-semibold", "Payout Threshold"),
                      p(
                        cls := "text-sm text-muted-foreground",
                        "Set the minimum balance required before a payout is triggered."
                      )
                    ),
                    div(
                      cls := "px-6 flex-1 flex flex-col gap-4",
                      div(
                        label(cls := "text-sm font-medium block mb-1", "Preferred Currency"),
                        select(
                          cls := "flex h-9 w-full rounded-md border border-input bg-background px-3 py-1 text-sm",
                          option("USD — United States Dollar")
                        )
                      ),
                      div(
                        cls := "flex items-baseline justify-between",
                        label(cls := "text-sm font-medium", "Minimum Payout Amount"),
                        span(cls := "text-2xl font-semibold tabular-nums", "$2500.00")
                      ),
                      div(
                        cls := "flex w-full items-center overflow-hidden h-2 rounded-full bg-secondary",
                        role := "progressbar",
                        span(cls := "h-full bg-primary transition-all rounded-full", styleAttr := "width:25%")
                      ),
                      div(
                        cls := "flex justify-between text-sm text-muted-foreground",
                        span("$50 (MIN)"),
                        span("$10,000 (MAX)")
                      ),
                      div(
                        label(cls := "text-sm font-medium block mb-1", "Notes"),
                        textArea(
                          cls := "flex min-h-[100px] w-full rounded-md border border-input bg-background px-3 py-2 text-sm resize-none",
                          placeholder := "Add any notes for this payout configuration..."
                        )
                      )
                    ),
                    footerTag(
                      cls := "px-6 pb-6",
                      button(typ := "button", cls := btnPrimary + " w-full", "Save Threshold")
                    )
                  ),
                  div(
                    cls := "rounded-xl border bg-card shadow-sm flex flex-col",
                    headerTag(
                      cls := "p-6 pb-3",
                      h2(cls := "text-lg font-semibold", "Account Access"),
                      p(cls := "text-sm text-muted-foreground", "Update your credentials or re-authenticate.")
                    ),
                    div(
                      cls := "px-6 flex-1 flex flex-col gap-4",
                      div(
                        label(cls := "text-sm font-medium block mb-1", "Email Address"),
                        input(
                          cls := "flex h-9 w-full rounded-md border border-input bg-background px-3 py-1 text-sm",
                          typ := "email",
                          placeholder := "artist@studio.inc"
                        )
                      ),
                      div(
                        div(
                          cls := "flex items-center justify-between",
                          label(cls := "text-sm font-medium", "Current Password"),
                          a(
                            cls := "text-xs font-medium tracking-wider text-muted-foreground uppercase hover:text-foreground",
                            href := "#",
                            "Forgot?"
                          )
                        ),
                        input(
                          cls := "flex h-9 w-full rounded-md border border-input bg-background px-3 py-1 text-sm",
                          typ := "password",
                          placeholder := "••••••••••••••••••••••••"
                        )
                      )
                    ),
                    footerTag(
                      cls := "px-6 pb-6 flex flex-col gap-4",
                      button(typ := "button", cls := btnPrimary + " w-full", rawIcon(iconLock), " Update Security"),
                      a(
                        cls := "flex items-center gap-3 rounded-md bg-muted/50 p-4 hover:bg-muted",
                        href := "#",
                        figure(cls := "text-destructive", rawIcon(iconInfo)),
                        sectionTag(
                          cls := "flex flex-1 flex-col leading-snug",
                          h3(cls := "font-medium text-sm", "Danger Zone"),
                          p(cls := "text-sm text-muted-foreground", "Archive account and remove catalog")
                        ),
                        asideTag(cls := "text-muted-foreground [&_svg]:size-4", rawIcon(iconChevronRight))
                      )
                    )
                  )
                ),

                // ═══ COLUMN 4 ═══
                div(
                  cls := "hidden md:flex min-w-0 flex-col gap-6",
                  div(
                    cls := "rounded-xl border bg-card shadow-sm flex flex-col",
                    div(
                      cls := "px-6 pt-6 flex justify-center",
                      div(
                        cls := "rounded-md border border-border bg-white p-4",
                        svgEl(
                          viewBoxA := "0 0 21 21",
                          cls := "size-40 text-black",
                          role := "img",
                          aria.label := "Connect device QR code",
                          rectEl(wA := "21", hA := "21", fillA := "white"),
                          qrRows.zipWithIndex.flatMap { case (row, y) =>
                            row.zipWithIndex.collect {
                              case (cell, x) if cell == '1' =>
                                rectEl(xA := x.toString, yA := y.toString, wA := "1", hA := "1")
                            }
                          }
                        )
                      )
                    ),
                    headerTag(
                      cls := "px-6 pt-4 pb-6 text-center",
                      h2(cls := "text-lg font-semibold", "Scan to connect your mobile device"),
                      p(
                        cls := "text-sm text-muted-foreground text-balance",
                        "Open the Ledger mobile app and scan this code to link your device."
                      )
                    )
                  ),
                  div(
                    cls := "rounded-xl border bg-card shadow-sm flex flex-col",
                    headerTag(
                      cls := "p-6 pb-3",
                      h2(cls := "text-lg font-semibold", "Transfer Funds"),
                      p(cls := "text-sm text-muted-foreground", "Move money between your connected accounts.")
                    ),
                    div(
                      cls := "px-6 flex-1 flex flex-col gap-4",
                      div(
                        label(cls := "text-sm font-medium block mb-1", "Amount to Transfer"),
                        div(
                          cls := "flex items-center rounded-md border border-input bg-background px-3 py-1 text-sm",
                          span("$"),
                          input(cls := "bg-transparent outline-none flex-1", value := "1,200.00")
                        )
                      ),
                      div(
                        label(cls := "text-sm font-medium block mb-1", "From Account"),
                        select(
                          cls := "flex h-9 w-full rounded-md border border-input bg-background px-3 py-1 text-sm",
                          option("Main Checking (··8402) — $12,450.00")
                        )
                      ),
                      div(
                        label(cls := "text-sm font-medium block mb-1", "To Account"),
                        select(
                          cls := "flex h-9 w-full rounded-md border border-input bg-background px-3 py-1 text-sm",
                          option("High Yield Savings (··1192) — $42,100.00")
                        )
                      ),
                      div(
                        cls := "rounded-md bg-muted/50 p-4",
                        div(
                          cls := "flex justify-between",
                          span(cls := "text-muted-foreground", "Estimated arrival"),
                          span(cls := "font-medium", "Today, Apr 14")
                        ),
                        hrEl(cls := "my-3 border-border"),
                        div(
                          cls := "flex justify-between",
                          span(cls := "text-muted-foreground", "Transaction fee"),
                          span(cls := "font-medium tabular-nums", "$0.00")
                        ),
                        hrEl(cls := "my-3 border-border"),
                        div(
                          cls := "flex justify-between",
                          span(cls := "font-medium", "Total amount"),
                          span(cls := "font-semibold tabular-nums", "$1,200.00")
                        )
                      )
                    ),
                    footerTag(
                      cls := "px-6 pb-6",
                      button(typ := "button", cls := btnPrimary + " w-full", "Confirm Transfer")
                    )
                  ),
                  div(
                    cls := "rounded-xl border bg-card shadow-sm flex flex-col",
                    headerTag(
                      cls := "p-6 pb-3",
                      navTag(
                        aria.label := "Breadcrumb",
                        olEl(
                          cls := "flex flex-wrap items-center gap-1.5 text-sm text-muted-foreground",
                          breadcrumbItem("Home"),
                          breadcrumbSeparator,
                          breadcrumbEllipsisMenu,
                          breadcrumbSeparator,
                          liEl(
                            cls := "inline-flex items-center gap-1.5",
                            span(cls := "font-normal text-foreground", "Payments")
                          )
                        )
                      )
                    ),
                    div(
                      cls := "px-6 flex-1 flex flex-col gap-2",
                      itemRow(
                        "Change transfer limit",
                        "Adjust how much you can send from your balance.",
                        iconSettings
                      ),
                      itemRow("Scheduled transfers", "Set up a transfer to send at a later date.", iconCalendar),
                      itemRow("Recurring card payments", "Manage your repeated card transactions.", iconRefresh)
                    )
                  )
                ),

                // ═══ COLUMN 5 ═══
                div(
                  cls := "hidden min-[1400px]:flex min-w-0 flex-col gap-6",
                  div(
                    cls := "rounded-xl border bg-card shadow-sm overflow-hidden flex flex-col items-center gap-4 py-8 px-6",
                    div(
                      cls := "flex size-12 items-center justify-center rounded-full border-2 border-dashed border-muted-foreground/25 text-muted-foreground/50",
                      rawIcon(iconPlus)
                    ),
                    h2(cls := "text-lg font-semibold", "Distribute Track"),
                    p(
                      cls := "text-sm text-muted-foreground text-center text-balance",
                      "Upload your first master to start reaching listeners on Spotify, Apple Music, and more."
                    ),
                    button(typ := "button", cls := btnPrimary, "Create Release")
                  ),
                  div(
                    cls := "rounded-xl border bg-card shadow-sm flex flex-col pb-0",
                    headerTag(
                      cls := "p-6 pb-3 relative",
                      h2(cls := "text-lg font-semibold", "Analytics"),
                      p(
                        cls := "text-sm text-muted-foreground flex items-center gap-2",
                        "418.2K Visitors ",
                        Badge("+10%")
                      ),
                      a(
                        cls := "inline-flex shrink-0 items-center justify-center gap-2 rounded-md text-sm font-medium whitespace-nowrap transition-all outline-none border bg-background shadow-xs hover:bg-accent hover:text-accent-foreground h-8 px-3 py-1 absolute end-4 top-4",
                        href := "/components/chart",
                        "View Analytics"
                      )
                    ),
                    svgEl(
                      viewBoxA := "0 0 100 86",
                      preserveAspectRatioA := "none",
                      cls := "w-full aspect-[1/0.35] text-chart-1",
                      role := "img",
                      aria.label := "Visitor trend",
                      pathEl(
                        dA := "M0 52L18 40L36 46L54 70L72 50L100 49V86H0Z",
                        fillA := "currentColor",
                        opacityA := "0.28"
                      ),
                      pathEl(
                        dA := "M0 52L18 40L36 46L54 70L72 50L100 49",
                        fillA := "none",
                        strokeA := "currentColor",
                        strokeWidthA := "1.5",
                        vectorEffectA := "non-scaling-stroke"
                      )
                    )
                  ),
                  div(
                    cls := "rounded-xl border bg-card shadow-sm flex flex-col",
                    headerTag(
                      cls := "p-6 pb-3",
                      h2(cls := "text-lg font-semibold", "Notifications"),
                      p(
                        cls := "text-sm text-muted-foreground",
                        "Choose which email and push alerts you want to receive."
                      )
                    ),
                    div(
                      cls := "px-6 flex-1 flex flex-col gap-4",
                      notifToggle("Transaction alerts", "Deposits, withdrawals, and transfers.", isChecked = true),
                      notifToggle("Security alerts", "Login attempts and account changes.", isChecked = true),
                      notifToggle("Goal milestones", "Updates at 25%, 50%, 75%, and 100%.", isChecked = false),
                      notifToggle("Market updates", "Daily portfolio summary and price alerts.", isChecked = false)
                    ),
                    footerTag(
                      cls := "px-6 pb-6",
                      button(typ := "button", cls := btnPrimary + " w-full", "Save Preferences")
                    )
                  ),
                  div(
                    cls := "rounded-xl border bg-card shadow-sm flex flex-col",
                    headerTag(
                      cls := "p-6 pb-3",
                      h2(cls := "text-lg font-semibold", "Power Usage"),
                      p(cls := "text-sm text-muted-foreground", "Whole Home")
                    ),
                    div(
                      cls := "px-6 flex-1 flex flex-col gap-4",
                      div(
                        cls := "flex h-[140px] w-full items-end gap-2",
                        role := "img",
                        aria.label := "Power usage by hour",
                        powerBar("6a", 32),
                        powerBar("8a", 74),
                        powerBar("10a", 82),
                        powerBar("12p", 63),
                        powerBar("2p", 89),
                        powerBar("4p", 76),
                        powerBar("6p", 100),
                        powerBar("8p", 84)
                      ),
                      hrEl(cls := "border-border"),
                      div(
                        cls := "grid grid-cols-2 gap-4",
                        div(
                          span(cls := "text-sm text-muted-foreground", "Currently Using"),
                          p(cls := "text-lg font-semibold tabular-nums", "3.4 kW")
                        ),
                        div(
                          span(cls := "text-sm text-muted-foreground", "Solar Gen"),
                          p(cls := "text-lg font-semibold tabular-nums", "+1.2 kW")
                        )
                      )
                    )
                  )
                )
              )
            )
          )
        ),

        // ── Footer ──
        footerTag(
          cls := "px-6 py-8 text-center text-sm text-muted-foreground",
          "Built by ",
          a(
            href := "https://x.com/hunvreus",
            target := "_blank",
            rel := "noopener",
            cls := "underline underline-offset-2 decoration-muted-foreground/50 transition-all hover:decoration-foreground/50",
            "Ronan Berder"
          ),
          ". ",
          a(
            href := "https://github.com/sponsors/hunvreus",
            target := "_blank",
            rel := "noopener",
            cls := "underline underline-offset-2 decoration-muted-foreground/50 transition-all hover:decoration-foreground/50",
            "Sponsor me"
          ),
          "."
        )
      )
    )

  /** Interactive component gallery. Each preview is composed from the same Laminar primitives exported by modules/ui,
    * making this page a useful smoke test as well as documentation.
    */
  private def componentsGalleryPage(): HtmlElement =
    val themeConfig = Var(ThemeConfig.load())

    div(
      cls := "min-h-dvh bg-background text-foreground antialiased",
      themeConfig.signal --> { cfg => ThemeConfig.applyToDocument(cfg) },
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
            a(cls := btnGhost, href := "/blocks", "Blocks")
          ),
          div(
            cls := "ml-auto flex items-center gap-2",
            select(
              cls := "h-8 w-28 rounded-md border border-input bg-background px-2 text-sm",
              aria.label := "Style pack",
              value <-- themeConfig.signal.map(_.stylePack),
              onChange --> { ev =>
                val next = themeConfig.now().copy(stylePack = ev.target.asInstanceOf[dom.html.Select].value)
                themeConfig.set(next)
                ThemeConfig.store(next)
              },
              option(value := "vega", "Vega"),
              option(value := "nova", "Nova"),
              option(value := "maia", "Maia"),
              option(value := "lyra", "Lyra"),
              option(value := "mira", "Mira"),
              option(value := "luma", "Luma"),
              option(value := "sera", "Sera"),
              option(value := "rhea", "Rhea")
            ),
            button(
              typ := "button",
              cls := btnIcon,
              aria.label := "Toggle dark mode",
              onClick --> { _ =>
                val next = themeConfig.now().copy(darkMode = !themeConfig.now().darkMode)
                themeConfig.set(next)
                ThemeConfig.store(next)
              },
              span(cls := "hidden dark:block", rawIcon(iconSun)),
              span(cls := "block dark:hidden", rawIcon(iconMoon))
            )
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
      )
    )

  /** Documentation-style component route. `/components/drawer` is the first full page; other component links use the
    * same shell and live primitive preview so the route structure scales as examples are added.
    */
  private def componentDocsPage(): HtmlElement =
    val themeConfig = Var(ThemeConfig.load())
    val drawerOpen = Var(false)
    val dialogOpen = Var(false)
    val switchOn = Var(true)
    val previewTheme = Var(ThemeSwitcher.Theme.System)
    val pathParts = dom.window.location.pathname.stripPrefix("/components").stripPrefix("/").split("/").toList
    val componentName = pathParts.find(_.nonEmpty).getOrElse("drawer")
    val componentTitle = componentName.split("-").map(_.capitalize).mkString(" ")
    val componentDescription = componentName match
      case "accordion" => "A vertically stacked set of interactive headings that each reveal a section of content."
      case "drawer"    => "A mobile-first drawer component for Laminar."
      case "dialog"    => "A modal dialog built with the native HTML dialog element."
      case "button"    => "A reusable action button with shadcn/ui variants."
      case "switch"    => "A reactive boolean control backed by a Laminar Var."
      case _           => s"The ${componentTitle.toLowerCase} primitive for shadcn-scalajs."

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

    def previewCanvas(content: Modifier[HtmlElement]*): HtmlElement =
      div(cls := "flex min-h-64 w-full items-center justify-center gap-3 p-6", content)

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
      case "chart"    => previewCanvas(Chart("Chart preview"))
      case "checkbox" => previewCanvas(Checkbox(), Label("Accept terms"))
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
          Field(
            cls := "w-full max-w-sm",
            Field.label("Email"),
            Input(placeholder := "you@example.com"),
            Field.description("We will never share your email.")
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
          InputGroup(
            cls := "max-w-sm",
            InputGroup.addon("https://"),
            Input(placeholder := "example.com", cls := "border-0 shadow-none")
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
            Sidebar.header("Navigation"),
            Sidebar.content(Sidebar.menu(Sidebar.menuItem("Overview"), Sidebar.menuItem("Settings")))
          )
        )
      case "skeleton" => previewCanvas(Skeleton(cls := "h-20 w-full max-w-sm"))
      case "slider"   => previewCanvas(Slider(value := "50", cls := "w-full max-w-sm"))
      case "spinner"  => previewCanvas(Spinner())
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
        previewCanvas(Tabs(Tabs.list(Tabs.trigger("Overview"), Tabs.trigger("Usage")), Tabs.content("Tab content")))
      case "textarea"       => previewCanvas(Textarea(placeholder := "Write a message…", cls := "max-w-sm"))
      case "theme-switcher" => previewCanvas(ThemeSwitcher(previewTheme))
      case "toast" =>
        previewCanvas(
          Toast(Toast.Variant.Default, Toast.title("Saved"), Toast.description("Everything is up to date."))
        )
      case "tooltip" => previewCanvas(Tooltip("Helpful context", span("Hover me")))
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
      case "date-picker" => previewCanvas(DatePicker(Var(Option.empty[js.Date])))
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
                NavigationMenu.trigger("Docs"),
                NavigationMenu.content(p(cls := "text-sm", "Getting started guides."))
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
      case "checkbox" => """Checkbox()
Label("Accept terms")"""
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
)"""
      case "form" =>
        """Form(
  Form.item(Form.label("Email"), Input(placeholder := "you@example.com")),
  Button("Submit")
)"""
      case "input" => """Input(placeholder := "Type something…")"""
      case "input-group" =>
        """InputGroup(
  InputGroup.addon("https://"),
  Input(placeholder := "example.com")
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
      case "spinner"  => """Spinner()"""
      case "switch"   => """val enabled = Var(true)
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
        """Tabs(Tabs.list(Tabs.trigger("Overview"), Tabs.trigger("Usage")), Tabs.content("Tab content"))"""
      case "textarea"       => """Textarea(placeholder := "Write a message…")"""
      case "theme-switcher" => """val theme = Var(ThemeSwitcher.Theme.System)
ThemeSwitcher(theme)"""
      case "toast" =>
        """Toast(Toast.Variant.Default, Toast.title("Saved"), Toast.description("Everything is up to date."))"""
      case "tooltip" => """Tooltip("Helpful context", span("Hover me"))"""
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
      case "date-picker" => """val selected = Var(Option.empty[js.Date])
DatePicker(selected)"""
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
      NavigationMenu.trigger("Docs"),
      NavigationMenu.content(p("Getting started guides."))
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
      themeConfig.signal --> { cfg => ThemeConfig.applyToDocument(cfg) },
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
            a(cls := btnGhost, href := "/blocks", "Blocks")
          ),
          div(
            cls := "ml-auto flex items-center gap-2",
            select(
              cls := "h-8 w-28 rounded-md border border-input bg-background px-2 text-sm",
              aria.label := "Style pack",
              value <-- themeConfig.signal.map(_.stylePack),
              onChange --> { ev =>
                val next = themeConfig.now().copy(stylePack = ev.target.asInstanceOf[dom.html.Select].value)
                themeConfig.set(next)
                ThemeConfig.store(next)
              },
              option(value := "vega", "Vega"),
              option(value := "nova", "Nova"),
              option(value := "maia", "Maia"),
              option(value := "lyra", "Lyra"),
              option(value := "mira", "Mira"),
              option(value := "luma", "Luma"),
              option(value := "sera", "Sera"),
              option(value := "rhea", "Rhea")
            ),
            button(
              typ := "button",
              cls := btnIcon,
              aria.label := "Toggle dark mode",
              onClick --> { _ =>
                val next = themeConfig.now().copy(darkMode = !themeConfig.now().darkMode)
                themeConfig.set(next)
                ThemeConfig.store(next)
              },
              span(cls := "hidden dark:block", rawIcon(iconSun)),
              span(cls := "block dark:hidden", rawIcon(iconMoon))
            )
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
            cls := "mx-auto max-w-2xl",
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
                s"${componentTitle} is available as a direct Laminar component and through the generated registry."
              )
            ),
            div(cls := "mt-8 rounded-md border bg-card", liveExample()),
            div(
              idAttr := "installation",
              cls := "mt-12 scroll-mt-24",
              h2(cls := "text-xl font-semibold", "Installation"),
              p(cls := "mt-3 text-sm text-muted-foreground", "Add the component through the local registry CLI."),
              codeBlock("shell", s"npx shadcn-scalajs add $componentName")
            ),
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
      )
    )

  // ── Helpers ──

  private def navSection(label: String, active: Option[String], items: (String, String)*) =
    div(
      cls := "rounded-xl border bg-card shadow-sm p-4",
      h3(cls := "mb-2 text-xs font-medium text-muted-foreground", label),
      ulEl(
        styleAttr := "list-style:none;padding:0;margin:0",
        cls := "flex flex-col gap-1",
        items.map { case (name, icon) =>
          liEl(
            a(
              href := "#",
              cls := s"flex w-full min-w-0 items-center justify-start gap-2 rounded-md px-2 py-1.5 text-sm font-medium transition-colors hover:bg-accent hover:text-accent-foreground ${if active.contains(name) then "bg-muted" else ""}",
              span(cls := "shrink-0 inline-flex", rawIcon(icon)),
              span(cls := "truncate", name)
            )
          )
        }
      )
    )

  private def chartBar(l: String, hp: Int) = div(
    cls := "flex h-full flex-1 flex-col justify-end gap-2",
    div(cls := "rounded-t-md min-h-2 bg-chart-2", styleAttr := s"height:${hp}%"),
    span(cls := "text-center text-xs text-muted-foreground", l)
  )
  private def powerBar(l: String, hp: Int) = div(
    cls := "flex h-full flex-1 flex-col justify-end gap-1.5",
    div(cls := "rounded-t-md min-h-2 bg-chart-2", styleAttr := s"height:${hp}%"),
    span(cls := "text-center text-xs text-muted-foreground", l)
  )

  private def savingsCard(name: String, amount: String, progressText: String, value: String, widthPercent: String) =
    div(
      cls := "rounded-md bg-muted/50 p-4",
      p(cls := "text-xs font-medium tracking-wider text-muted-foreground uppercase", name),
      p(cls := "mt-3 text-3xl font-semibold tabular-nums", amount),
      div(
        cls := "progress mt-3",
        role := "progressbar",
        span(cls := "h-full transition-all", styleAttr := s"width:$widthPercent")
      ),
      div(
        cls := "mt-3 flex justify-between text-sm",
        span(cls := "text-muted-foreground", progressText),
        span(cls := "font-medium tabular-nums", value)
      )
    )

  private def savingsMini(l: String, v: String, s: String) = div(
    cls := "rounded-md bg-muted/50 p-4",
    p(cls := "text-xs font-medium tracking-wider text-muted-foreground uppercase", l),
    p(cls := "text-base font-semibold", v),
    p(cls := "text-sm text-muted-foreground", s)
  )

  private def dividendItem(name: String, shares: String, data: List[Int]) = div(
    cls := "flex items-center gap-3 rounded-md bg-muted/50 p-3",
    div(cls := "mr-auto", p(cls := "text-sm font-medium", name), p(cls := "text-sm text-muted-foreground", shares)),
    div(
      cls := "hidden md:flex h-8 w-24 items-end gap-1",
      data.map(h => span(cls := "rounded-t-md min-h-1 flex-1 bg-chart-2", styleAttr := s"height:${h}%"))
    )
  )

  private def notifToggle(labelStr: String, desc: String, isChecked: Boolean) =
    label(
      cls := "flex items-start gap-3",
      input(
        cls := "size-4 rounded border border-primary accent-primary mt-px",
        typ := "checkbox",
        checked := isChecked
      ),
      sectionTag(
        cls := "flex flex-1 flex-col leading-snug",
        span(cls := "font-medium text-sm", labelStr),
        p(cls := "text-sm text-muted-foreground", desc)
      )
    )

  private def itemRow(title: String, desc: String, iconSvg: String) = a(
    cls := "flex items-center gap-3 rounded-md bg-muted/50 p-4 hover:bg-muted transition-colors",
    href := "#",
    figure(cls := "[&_svg]:size-4", rawIcon(iconSvg)),
    sectionTag(
      cls := "flex flex-1 flex-col",
      h3(cls := "font-medium text-sm", title),
      p(cls := "text-sm text-muted-foreground", desc)
    ),
    asideTag(cls := "text-muted-foreground [&_svg]:size-4", rawIcon(iconChevronRight))
  )

  private def breadcrumbItem(l: String) =
    liEl(cls := "inline-flex items-center gap-1.5", a(href := "#", cls := "hover:text-foreground transition-colors", l))
  private def breadcrumbSeparator =
    liEl(aria.hidden := true, cls := "text-muted-foreground", span(rawIcon(iconChevronRight)))
  private def breadcrumbEllipsisMenu = liEl(
    cls := "inline-flex items-center gap-1.5",
    button(
      typ := "button",
      cls := "flex size-9 h-4 w-4 cursor-pointer items-center justify-center hover:text-foreground",
      aria.label := "Account options",
      rawIcon(iconEllipsis)
    )
  )
