package shadcnscalajs.ui

import com.raquo.laminar.api.L.*

object Radio:
  def apply(name: String = "", mods: Modifier[HtmlElement]*): HtmlElement =
    input(
      typ := "radio",
      nameAttr := name,
      cls := "input aspect-square size-4 rounded-full border border-primary text-primary shadow-xs outline-none focus-visible:ring-[3px] focus-visible:ring-ring/50 disabled:cursor-not-allowed disabled:opacity-50",
      mods
    )
