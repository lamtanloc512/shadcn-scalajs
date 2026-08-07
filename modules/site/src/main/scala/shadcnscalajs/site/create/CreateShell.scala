package shadcnscalajs.site.create

import com.raquo.laminar.api.L.*
import org.scalajs.dom
import shadcnscalajs.site.{Main, ThemeConfig}
import shadcnscalajs.ui.*

/** Create-page shell: site header, designer main, preview frame slot, and customizer column. */
object CreateShell:

  private val iconSearchSvg =
    """<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="11" cy="11" r="8"/><path d="m21 21-4.3-4.3"/></svg>"""

  private lazy val kbdEl = htmlTag("kbd")

  def apply(state: CreateState, customizer: HtmlElement): HtmlElement =
    div(
      dataAttr("slot") := "layout",
      cls := "group/layout relative z-10 flex h-svh flex-col overflow-hidden section-soft [--customizer-width:--spacing(56)] [--gap:--spacing(4)] md:[--gap:--spacing(6)] [--preview-height:calc(100svh-var(--header-height)-2rem-150px)] md:[--preview-height:calc(100svh-var(--header-height)-2rem)]",
      onMountCallback { _ => state.installShortcuts() },
      state.config.signal --> { cfg => ThemeConfig.applyToDocument(cfg) },
      siteHeader(state),
      mainTag(
        dataAttr("slot") := "designer",
        cls := "container-wrapper flex min-h-0 flex-1 flex-col gap-(--gap) p-(--gap) pt-[calc(var(--gap)*0.25)] md:flex-row-reverse",
        PreviewFrame(state, "preview-02"),
        customizer,
        WelcomeDialog()
      )
    )

  private def siteHeader(state: CreateState): HtmlElement =
    headerTag(
      cls := "bg-background sticky inset-x-0 top-0 isolate z-30 flex shrink-0 items-center gap-2",
      div(
        cls := "flex h-14 w-full items-center justify-between gap-2 px-4",
        div(
          cls := "flex min-w-0 items-center gap-1",
          a(
            href := "/",
            cls := Main.btnGhost,
            aria.label := "shadcn-scalajs home",
            span(cls := "[&_svg]:size-4", foreignHtmlElement(Main.logoEl)),
            span(cls := "truncate font-semibold", "shadcn-scalajs")
          ),
          navTag(
            cls := "hidden sm:flex items-center gap-1",
            aria.label := "Primary",
            a(cls := Main.btnGhost, href := "/components", "Components"),
            a(cls := Main.btnGhost, href := "/blocks", "Blocks"),
            a(cls := Main.btnGhost, href := "/create", "Create"),
            a(
              cls := Main.btnGhost,
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
            span(aria.hidden := true, Main.rawIcon(iconSearchSvg)),
            span(
              aria.hidden := true,
              kbdEl(
                cls := "pointer-events-none h-5 select-none items-center gap-1 rounded border bg-muted px-1.5 font-mono text-[10px] font-medium text-muted-foreground hidden sm:flex",
                "⌘K"
              )
            )
          ),
          // No style-pack select here: the customizer column owns that control on this page, and a
          // second one would be a competing source of truth for the same preset field.
          button(
            typ := "button",
            cls := s"${Main.btnIcon} hidden sm:inline-flex",
            aria.label := "Toggle dark mode",
            onClick --> { _ => state.toggleDark() },
            span(cls := "hidden dark:block", Main.rawIcon(Main.iconSun)),
            span(cls := "block dark:hidden", Main.rawIcon(Main.iconMoon))
          )
        )
      )
    )

/** `/create/preview-02` entry — full create chrome with the customizer column. */
object CreatePageEntry:

  def apply(): HtmlElement =
    val state = new CreateState()
    CreateShell(
      state,
      Customizer(state)
    )

/** `/preview/preview-02` — chrome-less preview surface for iframe embedding and maximized view. */
object PreviewOnlyPage:

  private val buttonBase =
    "btn cn-button inline-flex shrink-0 items-center justify-center gap-2 rounded-md text-sm font-medium whitespace-nowrap transition-all outline-none focus-visible:border-ring focus-visible:ring-[3px] focus-visible:ring-ring/50 disabled:pointer-events-none disabled:opacity-50 [&_svg]:pointer-events-none [&_svg]:shrink-0 [&_svg:not([class*='size-'])]:size-4"

  def apply(): HtmlElement =
    val state = new CreateState()
    val params = new dom.URLSearchParams(dom.window.location.search)
    val fromPreview = params.get("fromPreview") == "true"

    val returnHref =
      val url = new dom.URL(dom.window.location.href)
      url.searchParams.delete("fromPreview")
      s"/create/preview-02${url.search}"

    div(
      cls := "relative min-h-dvh bg-background text-foreground antialiased",
      state.config.signal --> { cfg => ThemeConfig.applyToDocument(cfg) },
      if fromPreview then
        a(
          href := returnHref,
          cls := s"$buttonBase absolute top-2 right-2 isolate z-10",
          Button.ButtonApi.variant(Button.Variant.Ghost),
          Button.ButtonApi.size(Button.Size.IconSm),
          aria.label := "Minimize preview",
          Icons.minimize()
        )
      else emptyNode,
      preview02.Preview02()
    )
