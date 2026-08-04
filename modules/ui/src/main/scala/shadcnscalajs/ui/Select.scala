package shadcnscalajs.ui

import com.raquo.laminar.api.L.*

object Select:
  def apply(mods: Modifier[HtmlElement]*): HtmlElement =
    select(
      cls := "select flex h-9 w-full items-center justify-between rounded-md border border-input bg-background px-3 py-2 text-sm shadow-xs outline-none focus-visible:border-ring focus-visible:ring-[3px] focus-visible:ring-ring/50",
      mods
    )
