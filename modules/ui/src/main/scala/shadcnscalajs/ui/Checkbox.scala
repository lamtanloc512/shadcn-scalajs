package shadcnscalajs.ui

import com.raquo.laminar.api.L.*

object Checkbox:
  def apply(mods: Modifier[HtmlElement]*): HtmlElement =
    input(
      typ := "checkbox",
      cls := "input cn-checkbox peer size-4 shrink-0 rounded-[4px] border border-primary shadow-xs outline-none transition-shadow focus-visible:border-ring focus-visible:ring-[3px] focus-visible:ring-ring/50 disabled:cursor-not-allowed disabled:opacity-50 checked:bg-primary checked:text-primary-foreground",
      mods
    )
