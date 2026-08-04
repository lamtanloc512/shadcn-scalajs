package shadcnscalajs.ui

import com.raquo.laminar.api.L.*

object NativeSelect:
  def apply(mods: Modifier[HtmlElement]*): HtmlElement =
    select(
      cls := "flex h-9 w-full items-center justify-between rounded-md border border-input bg-transparent px-3 py-2 text-sm shadow-xs outline-none focus-visible:border-ring focus-visible:ring-[3px] focus-visible:ring-ring/50 disabled:cursor-not-allowed disabled:opacity-50",
      mods
    )
