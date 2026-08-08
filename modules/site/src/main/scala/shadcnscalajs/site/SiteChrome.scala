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
    case None, Home, Components, Blocks, Create

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

  def primaryNav(active: Active, includeGitHub: Boolean = false, showHome: Boolean = true): HtmlElement =
    def item(target: Active, href: String, label: String): HtmlElement =
      if active == target then navGhostActive(href, label) else navGhost(href, label)

    navTag(
      cls := "hidden items-center gap-1 sm:flex",
      aria.label := "Primary",
      if showHome then item(Active.Home, "/", "Home") else emptyNode,
      item(Active.Components, "/components", "Components"),
      item(Active.Blocks, "/blocks", "Blocks"),
      item(Active.Create, "/create", "Create"),
      if includeGitHub then
        navGhost(
          "https://github.com/lamtanloc512/shadcn-scalajs",
          target := "_blank",
          rel := "noopener",
          "GitHub"
        )
      else emptyNode
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
      navGhost("/create", "Create")
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
