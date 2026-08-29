package shadcnscalajs.site

import com.raquo.laminar.api.L.*
import shadcnscalajs.ui.*

/** Shared sticky header used by the landing page, component docs, blocks pages, and `/create`.
  *
  * Keeps the current visual layout — the point is one implementation of nav / brand / search so the four previous
  * copies stop drifting, not a redesign.
  */
object SiteChrome:

  /** Which primary-nav item should look selected. */
  enum Active derives CanEqual:
    case None, Home, Components, Blocks, Create, WebComponents

  def navGhost(hrefValue: String, mods: Modifier[HtmlElement]*): HtmlElement =
    Button.anchor(hrefValue, Button.ButtonApi.variant(Button.Variant.Ghost), mods)

  def navGhostActive(hrefValue: String, mods: Modifier[HtmlElement]*): HtmlElement =
    Button.anchor(
      hrefValue,
      Button.ButtonApi.variant(Button.Variant.Ghost),
      cls := "bg-accent text-accent-foreground",
      mods
    )

  def brand(mods: Modifier[HtmlElement]*): HtmlElement =
    navGhost(
      "/",
      aria.label := "shadcn-scalajs home",
      span(cls := "[&_svg]:size-4", foreignHtmlElement(Main.logoEl)),
      span(cls := "truncate font-semibold", "shadcn-scalajs"),
      Badge.of(
        _.variant(Badge.Variant.Outline),
        _ => cls := "h-5 px-1.5 text-[10px] uppercase tracking-wide text-muted-foreground",
        _ => "Alpha"
      ),
      mods
    )

  /** Read-only search field shown in the landing and create headers. */
  def searchStub: HtmlElement =
    div(
      cls := "hidden sm:block w-full min-w-0 max-w-72 sm:ml-auto",
      InputGroup(
        cls := "h-8",
        InputGroup.input(
          placeholder := "Search...",
          readOnly := true,
          tabIndex := -1,
          aria.label := "Search docs"
        ),
        InputGroup.addon(InputGroup.AddonAlign.InlineEnd, Icons.search()),
        InputGroup.addon(
          InputGroup.AddonAlign.InlineEnd,
          Kbd(cls := "hidden sm:inline-flex", "⌘K")
        )
      )
    )

  private val githubHref = "https://github.com/lamtanloc512/shadcn-scalajs"

  /** The destinations every header shows, in order. `Home` is dropped on pages whose brand link already goes there. */
  private def entries(showHome: Boolean): Seq[(Active, String, String)] =
    val all = Seq(
      (Active.Home, "/", "Home"),
      (Active.Components, "/components", "Components"),
      (Active.Blocks, "/blocks", "Blocks"),
      (Active.Create, "/create", "Create"),
      // Full document load: the demo is a static HTML page outside the SPA, and Router.owns leaves it alone.
      // Never selected by activeOf — leaving the SPA means this header is gone.
      (Active.WebComponents, "/web-components", "Web Components")
    )
    val enabled = if SiteFeatures.webComponents then all else all.filterNot(_._1 == Active.WebComponents)
    if showHome then enabled else enabled.tail

  def primaryNav(active: Active, includeGitHub: Boolean = false, showHome: Boolean = true): HtmlElement =
    navTag(
      cls := "hidden items-center gap-1 md:flex",
      aria.label := "Primary",
      entries(showHome).map { (target, href, label) =>
        if active == target then navGhostActive(href, label) else navGhost(href, label)
      },
      if includeGitHub then navGhost(githubHref, target := "_blank", rel := "noopener", "GitHub")
      else emptyNode
    )

  /** The same destinations for viewports too narrow for [[primaryNav]], in a sheet behind a hamburger button. */
  def mobileNav(active: Active, includeGitHub: Boolean = false, showHome: Boolean = true): HtmlElement =
    val isOpen = Var(false)

    def item(target: Active, href: String, label: String, mods: Modifier[HtmlElement]*): HtmlElement =
      Button.anchor(
        href,
        Button.ButtonApi.variant(if active == target then Button.Variant.Secondary else Button.Variant.Ghost),
        // `justify-start` and the base `justify-center` are the same property, so source order does not decide it.
        cls := "w-full justify-start!",
        onClick --> { _ => isOpen.set(false) },
        mods,
        label
      )

    div(
      cls := "md:hidden",
      Button(
        Button.ButtonApi.variant(Button.Variant.Ghost),
        Button.ButtonApi.size(Button.Size.Icon),
        aria.label := "Open menu",
        aria.expanded <-- isOpen.signal,
        onClick --> { _ => isOpen.set(true) },
        Icons.menu()
      ),
      Sheet(isOpen, Sheet.Side.Left)(
        cls := "w-72 sm:max-w-xs",
        Sheet.close(onClick --> { _ => isOpen.set(false) }),
        Sheet.header(Sheet.title("Menu"), Sheet.description("Jump to another part of the docs.")),
        navTag(
          cls := "flex flex-col gap-1 px-6 pb-6",
          aria.label := "Mobile",
          entries(showHome).map(item(_, _, _)),
          if includeGitHub then item(Active.None, githubHref, "GitHub", target := "_blank", rel := "noopener")
          else emptyNode
        )
      )
    )

  /** Docs page nav keeps the historical "Docs" label for `/components` while highlighting the open component. */
  def docsNav(componentName: String): HtmlElement =
    navTag(
      cls := "hidden items-center gap-1 md:flex",
      aria.label := "Primary",
      navGhost("/", "Home"),
      navGhost("/components", "Docs"),
      navGhostActive(s"/components/$componentName", "Components"),
      navGhost("/blocks", "Blocks"),
      navGhost("/create", "Create"),
      if SiteFeatures.webComponents then navGhost("/web-components", "Web Components") else emptyNode
    )

  private def isLanding(route: Router.Route): Boolean = route == Router.Route.Landing

  private def activeOf(route: Router.Route): Active = route match
    case Router.Route.ComponentsIndex | Router.Route.Component(_)                        => Active.Components
    case Router.Route.BlocksIndex | Router.Route.Block(_) | Router.Route.BlockPreview(_) => Active.Blocks
    case Router.Route.Create | Router.Route.CreatePreview                                => Active.Create
    case Router.Route.WebComponents                                                      => Active.WebComponents
    case Router.Route.Landing                                                            => Active.None

  private def navFor(route: Router.Route): HtmlElement = route match
    case Router.Route.Component(slug) => docsNav(if slug.isEmpty then "drawer" else slug)
    case other => primaryNav(activeOf(other), includeGitHub = isLanding(other), showHome = !isLanding(other))

  /** One header for the whole session.
    *
    * Rebuilding it per navigation meant rebuilding [[ThemeMenu]] with it, and that constructs a `CreateState` and the
    * full customizer field set every time. Only the parts that genuinely differ by route — the nav links, the search
    * stub, and whether the bar is bordered — are reactive; `trailing` is held as one instance for the session.
    */
  def persistent(route: Signal[Router.Route], trailing: HtmlElement): HtmlElement =
    val bordered = route.map(r => !isLanding(r)).distinct
    headerTag(
      cls := "sticky inset-x-0 top-0 flex shrink-0 items-center gap-2",
      cls("z-40", "border-b", "bg-background/95", "backdrop-blur") <-- bordered,
      cls("z-30", "isolate", "bg-background") <-- bordered.map(!_),
      div(
        cls := "flex h-14 w-full items-center justify-between gap-2 px-4",
        div(
          cls := "flex min-w-0 items-center gap-1",
          child <-- route.map(r => mobileNav(activeOf(r), includeGitHub = isLanding(r), showHome = !isLanding(r))),
          brand(),
          child <-- route.map(navFor)
        ),
        div(
          cls := "ml-auto flex min-w-0 flex-1 items-center justify-end gap-2",
          child.maybe <-- route.map(r => Option.when(isLanding(r))(searchStub)),
          trailing
        )
      )
    )

  /** Sticky top bar. `trailing` replaces the default [[ThemeMenu]] when the page already owns theme controls. */
  def header(
      active: Active = Active.None,
      includeSearch: Boolean = false,
      includeGitHub: Boolean = false,
      showHome: Boolean = true,
      bordered: Boolean = true,
      nav: Option[HtmlElement] = None,
      trailing: Seq[Modifier[HtmlElement]] = Seq(ThemeMenu())
  ): HtmlElement =
    headerTag(
      cls := (
        if bordered then
          "sticky inset-x-0 top-0 z-40 flex shrink-0 items-center gap-2 border-b bg-background/95 backdrop-blur"
        else "bg-background sticky inset-x-0 top-0 isolate z-30 flex shrink-0 items-center gap-2"
      ),
      div(
        cls := "flex h-14 w-full items-center justify-between gap-2 px-4",
        div(
          cls := "flex min-w-0 items-center gap-1",
          mobileNav(active, includeGitHub = includeGitHub, showHome = showHome),
          brand(),
          nav.getOrElse(primaryNav(active, includeGitHub = includeGitHub, showHome = showHome))
        ),
        div(
          cls := "ml-auto flex min-w-0 flex-1 items-center justify-end gap-2",
          if includeSearch then searchStub else emptyNode,
          trailing
        )
      )
    )
