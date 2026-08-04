package shadcnscalajs.ui

import com.raquo.laminar.api.L.*

object Collapsible:
  def apply(mods: Modifier[HtmlElement]*): HtmlElement = detailsTag(cls := "collapsible", mods)
  def trigger(mods: Modifier[HtmlElement]*): HtmlElement = summaryTag(cls := "cursor-pointer list-none", mods)
  def content(mods: Modifier[HtmlElement]*): HtmlElement = div(mods)
