package shadcnscalajs.ui

import com.raquo.laminar.api.L.*

object Tabs:
  def apply(mods: Modifier[HtmlElement]*): HtmlElement = div(cls := "tabs cn-tabs flex flex-col gap-2", mods)
  def list(mods: Modifier[HtmlElement]*): HtmlElement =
    div(role := "tablist", cls := "inline-flex h-9 items-center justify-center rounded-lg bg-muted p-1 text-muted-foreground", mods)
  def trigger(mods: Modifier[HtmlElement]*): HtmlElement = button(
    typ := "button",
    role := "tab",
    cls := "inline-flex items-center justify-center rounded-md px-3 py-1 text-sm font-medium transition-all hover:text-foreground focus-visible:ring-[3px] focus-visible:ring-ring/50",
    mods
  )
  def content(mods: Modifier[HtmlElement]*): HtmlElement = div(role := "tabpanel", cls := "flex-1 outline-none", mods)
