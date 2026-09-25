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
    case None, Home, Docs, Components, Blocks, Create, WebComponents

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

  /** The same destinations on every page, in the same order. */
  private def entries: Seq[(Active, String, String)] =
    val all = Seq(
      (Active.Home, "/", "Home"),
      (Active.Docs, "/docs/installation", "Docs"),
      (Active.Components, "/components", "Components"),
      (Active.Blocks, "/blocks", "Blocks"),
      (Active.Create, "/create", "Create"),
      (Active.WebComponents, "/web-components", "Web Components")
    )
    if SiteFeatures.webComponents then all else all.filterNot(_._1 == Active.WebComponents)

  def primaryNav(active: Active): HtmlElement =
    navTag(
      cls := "hidden items-center gap-1 md:flex",
      aria.label := "Primary",
      entries.map { (target, href, label) =>
        if active == target then navGhostActive(href, label) else navGhost(href, label)
      },
      navGhost(githubHref, target := "_blank", rel := "noopener", "GitHub")
    )

  /** The same destinations for viewports too narrow for [[primaryNav]], in a sheet behind a hamburger button. */
  def mobileNav(active: Active): HtmlElement =
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
          entries.map(item(_, _, _)),
          item(Active.None, githubHref, "GitHub", target := "_blank", rel := "noopener")
        )
      )
    )

  private def activeOf(route: Router.Route): Active = route match
    case Router.Route.Landing                                                            => Active.Home
    case Router.Route.Installation                                                       => Active.Docs
    case Router.Route.ComponentsIndex | Router.Route.Component(_)                        => Active.Components
    case Router.Route.BlocksIndex | Router.Route.Block(_) | Router.Route.BlockPreview(_) => Active.Blocks
    case Router.Route.Create | Router.Route.CreatePreview                                => Active.Create
    case Router.Route.WebComponents                                                      => Active.WebComponents

  /** One header for the whole session.
    *
    * Rebuilding it per navigation meant rebuilding [[ThemeMenu]] with it, and that constructs a `CreateState` and the
    * full customizer field set every time. Only the parts that genuinely differ by route — the nav links, the search
    * stub, and whether the bar is bordered — are reactive; `trailing` is held as one instance for the session.
    */
  def persistent(route: Signal[Router.Route], trailing: HtmlElement): HtmlElement =
    headerTag(
      cls := "sticky inset-x-0 top-0 z-40 flex shrink-0 items-center gap-2 border-b bg-background/95 backdrop-blur",
      div(
        cls := "flex h-14 w-full items-center justify-between gap-2 px-4",
        div(
          cls := "flex min-w-0 items-center gap-1",
          child <-- route.map(r => mobileNav(activeOf(r))),
          brand(),
          child <-- route.map(r => primaryNav(activeOf(r)))
        ),
        div(
          cls := "ml-auto flex min-w-0 flex-1 items-center justify-end gap-2",
          searchStub,
          trailing
        )
      )
    )

  /** Sticky top bar. `trailing` replaces the default [[ThemeMenu]] when the page already owns theme controls. */
  def header(
      active: Active = Active.None,
      includeSearch: Boolean = true,
      bordered: Boolean = true,
      nav: Option[HtmlElement] = None,
      trailing: Seq[Modifier[HtmlElement]] = Seq(ThemeMenu())
  ): HtmlElement =
    headerTag(
      cls := (
        if bordered then
          "sticky inset-x-0 top-0 z-40 flex shrink-0 items-center gap-2 border-b bg-background/95 backdrop-blur"
        else "bg-background sticky inset-x-0 top-0 isolate z-30 flex shrink-0 items-center gap-2 border-b"
      ),
      div(
        cls := "flex h-14 w-full items-center justify-between gap-2 px-4",
        div(
          cls := "flex min-w-0 items-center gap-1",
          mobileNav(active),
          brand(),
          nav.getOrElse(primaryNav(active))
        ),
        div(
          cls := "ml-auto flex min-w-0 flex-1 items-center justify-end gap-2",
          if includeSearch then searchStub else emptyNode,
          trailing
        )
      )
    )
