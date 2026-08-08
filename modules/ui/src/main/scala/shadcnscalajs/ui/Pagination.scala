package shadcnscalajs.ui

import com.raquo.laminar.api.L.*

object Pagination:

  private val ghostButton: String =
    "inline-flex shrink-0 items-center justify-center gap-2 rounded-md text-sm font-medium whitespace-nowrap transition-all outline-none hover:bg-accent hover:text-accent-foreground focus-visible:border-ring focus-visible:ring-[3px] focus-visible:ring-ring/50 disabled:pointer-events-none disabled:opacity-50 dark:hover:bg-accent/50 [&_svg]:pointer-events-none [&_svg]:shrink-0 [&_svg:not([class*='size-'])]:size-4 h-9 px-4 py-2 has-[>svg]:px-3"

  def apply(mods: Modifier[HtmlElement]*): HtmlElement =
    navTag(
      dataAttr("slot") := "pagination",
      cls := "cn-pagination mx-auto flex w-full justify-center",
      role := "navigation",
      aria.label := "pagination",
      mods
    )

  def content(mods: Modifier[HtmlElement]*): HtmlElement =
    ul(
      dataAttr("slot") := "pagination-content",
      cls := "cn-pagination-content flex items-center gap-1",
      mods
    )

  /** Alias for [[content]] — preserved for existing call sites. */
  def list(mods: Modifier[HtmlElement]*): HtmlElement = content(mods*)

  def item(mods: Modifier[HtmlElement]*): HtmlElement =
    li(dataAttr("slot") := "pagination-item", mods)

  def link(hrefValue: String, current: Boolean = false, mods: Modifier[HtmlElement]*): HtmlElement =
    a(
      href := hrefValue,
      dataAttr("slot") := "pagination-link",
      dataAttr("active") := current.toString,
      dataAttr("size") := "icon",
      cls := "btn cn-pagination-link",
      dataAttr("variant") := (if current then "outline" else "ghost"),
      aria.current := (if current then "page" else "false"),
      mods
    )

  def ellipsis(mods: Modifier[HtmlElement]*): HtmlElement =
    span(
      dataAttr("slot") := "pagination-ellipsis",
      aria.hidden := true,
      cls := "cn-pagination-ellipsis flex size-9 items-center justify-center [&_svg:not([class*='size-'])]:size-4",
      Icons.moreHorizontal(),
      span(cls := "sr-only", "More pages"),
      mods
    )

  def previous(mods: Modifier[HtmlElement]*): HtmlElement =
    button(
      typ := "button",
      aria.label := "Go to previous page",
      cls := s"cn-pagination-previous $ghostButton",
      Icons.chevronRight(svg.cls := "rotate-180"),
      span(cls := "cn-pagination-previous-text hidden sm:block", "Previous"),
      mods
    )

  def next(mods: Modifier[HtmlElement]*): HtmlElement =
    button(
      typ := "button",
      aria.label := "Go to next page",
      cls := s"cn-pagination-next $ghostButton",
      span(cls := "cn-pagination-next-text hidden sm:block", "Next"),
      Icons.chevronRight(),
      mods
    )

  def prevButton(mods: Modifier[HtmlElement]*): HtmlElement =
    button(
      typ := "button",
      aria.label := "Go to previous page",
      cls := s"cn-pagination-previous $ghostButton",
      Icons.chevronRight(svg.cls := "rotate-180 size-4"),
      span("Previous"),
      mods
    )

  def nextButton(mods: Modifier[HtmlElement]*): HtmlElement =
    button(
      typ := "button",
      aria.label := "Go to next page",
      cls := s"cn-pagination-next $ghostButton",
      span("Next"),
      Icons.chevronRight(svg.cls := "size-4"),
      mods
    )
