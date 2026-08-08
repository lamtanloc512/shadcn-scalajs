package shadcnscalajs.ui

import com.raquo.laminar.api.L.*

/** shadcn/ui Breadcrumb — every upstream part, with upstream's `data-slot` values so the style packs' breadcrumb rules
  * apply. Utilities are kept standalone for consumer projects that copy `modules/ui` without the pack CSS.
  */
object Breadcrumb:

  def apply(mods: Modifier[HtmlElement]*): HtmlElement =
    navTag(dataAttr("slot") := "breadcrumb", aria.label := "breadcrumb", cls := "breadcrumb cn-breadcrumb", mods)

  def list(mods: Modifier[HtmlElement]*): HtmlElement =
    ol(
      dataAttr("slot") := "breadcrumb-list",
      cls := "cn-breadcrumb-list flex flex-wrap items-center gap-1.5 text-sm break-words text-muted-foreground sm:gap-2.5",
      mods
    )

  def item(mods: Modifier[HtmlElement]*): HtmlElement =
    li(dataAttr("slot") := "breadcrumb-item", cls := "cn-breadcrumb-item inline-flex items-center gap-1.5", mods)

  def link(hrefValue: String, mods: Modifier[HtmlElement]*): HtmlElement =
    a(
      href := hrefValue,
      dataAttr("slot") := "breadcrumb-link",
      cls := "cn-breadcrumb-link transition-colors hover:text-foreground",
      mods
    )

  /** The current page: rendered as a non-navigable `role="link"`, which is what makes screen readers announce it as the
    * current location rather than as plain text.
    */
  def page(mods: Modifier[HtmlElement]*): HtmlElement =
    span(
      dataAttr("slot") := "breadcrumb-page",
      role := "link",
      aria.disabled := true,
      aria.current := "page",
      cls := "cn-breadcrumb-page font-normal text-foreground",
      mods
    )

  /** Upstream renders a chevron unless children are supplied; with Laminar modifiers there is no way to tell a class
    * modifier from a child, so an empty call gets the chevron and any argument list replaces it.
    */
  def separator(mods: Modifier[HtmlElement]*): HtmlElement =
    li(
      dataAttr("slot") := "breadcrumb-separator",
      role := "presentation",
      aria.hidden := true,
      cls := "cn-breadcrumb-separator [&>svg]:size-3.5",
      if mods.isEmpty then Icons.chevronRight() else mods
    )

  def ellipsis(mods: Modifier[HtmlElement]*): HtmlElement =
    span(
      dataAttr("slot") := "breadcrumb-ellipsis",
      role := "presentation",
      aria.hidden := true,
      cls := "cn-breadcrumb-ellipsis flex size-9 items-center justify-center",
      Icons.moreHorizontal(),
      span(cls := "sr-only", "More"),
      mods
    )
