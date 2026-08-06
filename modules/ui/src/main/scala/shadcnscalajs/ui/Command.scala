package shadcnscalajs.ui

import com.raquo.laminar.api.L.*

object Command:
  def apply(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      role := "dialog",
      cls := "command flex h-full w-full flex-col overflow-hidden rounded-md bg-popover text-popover-foreground",
      mods
    )
  def input(mods: Modifier[HtmlElement]*): HtmlElement =
    Input(cls := "h-11 rounded-none border-0 border-b shadow-none focus-visible:ring-0", mods)
  def list(mods: Modifier[HtmlElement]*): HtmlElement =
    div(cls := "max-h-[300px] overflow-y-auto overflow-x-hidden p-1", mods)
  def group(heading: String, mods: Modifier[HtmlElement]*): HtmlElement = div(
    cls := "overflow-hidden p-1 text-foreground",
    h3(cls := "px-2 py-1.5 text-xs font-medium text-muted-foreground", heading),
    mods
  )
  def item(mods: Modifier[HtmlElement]*): HtmlElement = div(
    role := "option",
    cls := "relative flex cursor-default select-none items-center gap-2 rounded-sm px-2 py-1.5 text-sm outline-none hover:bg-accent hover:text-accent-foreground",
    mods
  )
  def empty(mods: Modifier[HtmlElement]*): HtmlElement = div(cls := "py-6 text-center text-sm", mods)
