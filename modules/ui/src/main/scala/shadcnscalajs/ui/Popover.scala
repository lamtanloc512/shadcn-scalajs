package shadcnscalajs.ui

import com.raquo.laminar.api.L.*

object Popover:
  def apply(mods: Modifier[HtmlElement]*): HtmlElement = detailsTag(cls := "relative", mods)
  def trigger(mods: Modifier[HtmlElement]*): HtmlElement = summaryTag(cls := "cursor-pointer list-none", mods)
  def content(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      cls := "absolute z-50 mt-2 w-72 rounded-md border bg-popover p-4 text-popover-foreground shadow-md outline-none",
      mods
    )
