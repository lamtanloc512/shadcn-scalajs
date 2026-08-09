package shadcnscalajs.site.create

import com.raquo.laminar.api.L.*
import org.scalajs.dom
import shadcnscalajs.site.{Main, SiteChrome, ThemeConfig, ThemeTransition}
import shadcnscalajs.ui.*

/** Create-page shell: site header, designer main, preview frame slot, and customizer column. */
object CreateShell:

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
    // No ThemeMenu / style-pack select here: the customizer column owns theme controls on this page.
    SiteChrome.header(
      active = SiteChrome.Active.Create,
      includeSearch = true,
      includeGitHub = true,
      showHome = false,
      bordered = false,
      trailing = Seq(
        Button(
          Button.ButtonApi.variant(Button.Variant.Ghost),
          Button.ButtonApi.size(Button.Size.Icon),
          cls := "hidden sm:inline-flex",
          aria.label := "Toggle dark mode",
          dataAttr("theme-toggle") := "",
          onClick --> { ev => state.toggleDark(ThemeTransition.originOf(ev)) },
          span(cls := "hidden dark:block", Main.rawIcon(Main.iconSun)),
          span(cls := "block dark:hidden", Main.rawIcon(Main.iconMoon))
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
