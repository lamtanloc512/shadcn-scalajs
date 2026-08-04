package shadcnscalajs.ui

import com.raquo.laminar.api.L.*

object Combobox:
  def apply(mods: Modifier[HtmlElement]*): HtmlElement = Popover(mods*)
  def trigger(mods: Modifier[HtmlElement]*): HtmlElement = button(
    typ := "button",
    cls := "flex h-9 w-full items-center justify-between rounded-md border border-input bg-background px-3 py-2 text-sm",
    mods
  )
  def content(mods: Modifier[HtmlElement]*): HtmlElement = Popover.content(mods*)
