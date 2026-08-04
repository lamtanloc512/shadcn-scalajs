package shadcnscalajs.ui

import com.raquo.laminar.api.L.*

object InputGroup:
  def apply(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      cls := "input-group cn-input-group flex w-full items-center rounded-md border border-input bg-background shadow-xs focus-within:ring-[3px] focus-within:ring-ring/50",
      mods
    )
  def addon(mods: Modifier[HtmlElement]*): HtmlElement =
    div(cls := "flex items-center justify-center px-3 text-sm text-muted-foreground", mods)
