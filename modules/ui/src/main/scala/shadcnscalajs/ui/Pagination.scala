package shadcnscalajs.ui

import com.raquo.laminar.api.L.*

object Pagination:
  def apply(mods: Modifier[HtmlElement]*): HtmlElement =
    navTag(cls := "pagination", aria.label := "pagination", mods)

  def list(mods: Modifier[HtmlElement]*): HtmlElement = ul(cls := "flex items-center gap-1", mods)
  def item(mods: Modifier[HtmlElement]*): HtmlElement = li(mods)

  def link(hrefValue: String, current: Boolean = false, mods: Modifier[HtmlElement]*): HtmlElement =
    a(
      href := hrefValue,
      cls := "btn",
      dataAttr("variant") := (if current then "outline" else "ghost"),
      dataAttr("size") := "icon",
      aria.current := (if current then "page" else "false"),
      mods
    )

