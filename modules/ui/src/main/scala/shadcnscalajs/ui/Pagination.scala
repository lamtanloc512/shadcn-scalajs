package shadcnscalajs.ui

import com.raquo.laminar.api.L.*

/** shadcn/ui Pagination — markup parts matching upstream, plus a stateful [[Ctx]] that owns the current page and builds
  * the numbered links (with ellipses) the way bits-ui's `Pagination.Root` does via its `pages` snippet.
  *
  * The href-based [[link]] overload stays for static/SSR lists; the button form used by [[Ctx]] is what the interactive
  * demo and any in-app pager need.
  */
object Pagination:

  /** One entry in the page strip: a real page number, or a gap that renders as [[ellipsis]]. */
  enum Entry derives CanEqual:
    case Page(value: Int)
    case Gap

  /** Builds the numbered strip with leading/trailing ellipses. `siblingCount` is how many neighbours of the current
    * page stay visible on each side — bits-ui's default is 1.
    */
  def entries(pageCount: Int, current: Int, siblingCount: Int = 1): List[Entry] =
    if pageCount <= 0 then Nil
    else if pageCount <= siblingCount * 2 + 5 then
      // Short enough that every page fits without gaps.
      (1 to pageCount).toList.map(Entry.Page(_))
    else
      val cur = current.max(1).min(pageCount)
      val left = (cur - siblingCount).max(1)
      val right = (cur + siblingCount).min(pageCount)
      val showLeftEllipsis = left > 2
      val showRightEllipsis = right < pageCount - 1
      val middle = (left to right).toList.map(Entry.Page(_))
      val head =
        if showLeftEllipsis then List(Entry.Page(1), Entry.Gap)
        else (1 until left).toList.map(Entry.Page(_))
      val tail =
        if showRightEllipsis then List(Entry.Gap, Entry.Page(pageCount))
        else ((right + 1) to pageCount).toList.map(Entry.Page(_))
      head ++ middle ++ tail

  /** One pager instance: the current page plus the prev/next/link parts that mutate it. */
  final class Ctx private[ui] (
      val page: Var[Int],
      val pageCount: Int,
      val siblingCount: Int
  ):
    val current: Signal[Int] = page.signal.map(_.max(1).min(pageCount.max(1)))

    val canPrev: Signal[Boolean] = current.map(_ > 1)
    val canNext: Signal[Boolean] = current.map(_ < pageCount)

    def goTo(n: Int): Unit =
      if pageCount > 0 then page.set(n.max(1).min(pageCount))

    def previous(mods: Modifier[HtmlElement]*): HtmlElement =
      Pagination.previous(
        disabled <-- canPrev.map(!_),
        onClick --> { _ => goTo(page.now() - 1) },
        mods
      )

    def next(mods: Modifier[HtmlElement]*): HtmlElement =
      Pagination.next(
        disabled <-- canNext.map(!_),
        onClick --> { _ => goTo(page.now() + 1) },
        mods
      )

    /** A page number button. Active page uses the outline variant, matching upstream's `isActive`. */
    def link(pageNumber: Int, mods: Modifier[HtmlElement]*): HtmlElement =
      val active = current.map(_ == pageNumber)
      button(
        typ := "button",
        dataAttr("slot") := "pagination-link",
        dataAttr("size") := "icon",
        cls <-- active.map { a =>
          val variant = if a then Button.Variant.Outline else Button.Variant.Ghost
          s"btn cn-pagination-link ${Button.classes(variant, Button.Size.Icon)}"
        },
        dataAttr("active") <-- active.map(_.toString),
        dataAttr("variant") <-- active.map(a => if a then "outline" else "ghost"),
        aria.current <-- active.map(a => if a then "page" else "false"),
        onClick --> { _ => goTo(pageNumber) },
        mods,
        pageNumber.toString
      )

    /** Renders the numbered strip for the current page (reactive). */
    def pageItems: Signal[List[HtmlElement]] =
      current.map { cur =>
        entries(pageCount, cur, siblingCount).map {
          case Entry.Gap     => item(ellipsis())
          case Entry.Page(n) => item(link(n))
        }
      }

  def ctx(page: Var[Int], pageCount: Int, siblingCount: Int = 1): Ctx =
    Ctx(page, pageCount, siblingCount)

  /** Full interactive pager: previous, numbered links with ellipses, next. */
  def stateful(page: Var[Int], pageCount: Int, siblingCount: Int = 1, mods: Modifier[HtmlElement]*): HtmlElement =
    val pager = ctx(page, pageCount, siblingCount)
    apply(
      content(
        item(pager.previous()),
        children <-- pager.pageItems,
        item(pager.next())
      ),
      mods
    )

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

  /** Static/SSR page link. Prefer [[Ctx.link]] for an in-app pager bound to a `Var`. */
  def link(hrefValue: String, current: Boolean = false, mods: Modifier[HtmlElement]*): HtmlElement =
    a(
      href := hrefValue,
      dataAttr("slot") := "pagination-link",
      dataAttr("active") := current.toString,
      dataAttr("size") := "icon",
      cls := s"btn cn-pagination-link ${Button.classes(
          if current then Button.Variant.Outline else Button.Variant.Ghost,
          Button.Size.Icon
        )}",
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
    Button
      .of(
        _.variant(Button.Variant.Ghost),
        _.size(Button.Size.Default),
        _ => dataAttr("slot") := "pagination-previous",
        _ => cls := "cn-pagination-previous gap-1 px-2.5 sm:pl-2.5",
        _ => aria.label := "Go to previous page",
        _ => Icons.chevronRight(svg.cls := "rotate-180"),
        _ => span(cls := "cn-pagination-previous-text hidden sm:block", "Previous")
      )
      .amend(mods)

  def next(mods: Modifier[HtmlElement]*): HtmlElement =
    Button
      .of(
        _.variant(Button.Variant.Ghost),
        _.size(Button.Size.Default),
        _ => dataAttr("slot") := "pagination-next",
        _ => cls := "cn-pagination-next gap-1 px-2.5 sm:pr-2.5",
        _ => aria.label := "Go to next page",
        _ => span(cls := "cn-pagination-next-text hidden sm:block", "Next"),
        _ => Icons.chevronRight()
      )
      .amend(mods)

  /** @deprecated Prefer [[previous]]. Kept for call sites that used the old name. */
  def prevButton(mods: Modifier[HtmlElement]*): HtmlElement = previous(mods*)

  /** @deprecated Prefer [[next]]. Kept for call sites that used the old name. */
  def nextButton(mods: Modifier[HtmlElement]*): HtmlElement = next(mods*)
