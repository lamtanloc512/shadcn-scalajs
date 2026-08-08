package shadcnscalajs.ui

import com.raquo.laminar.api.L.*

/** shadcn/ui Label primitive for accessible form controls. */
object Label:

  def apply(mods: Modifier[HtmlElement]*): HtmlElement =
    label(
      dataAttr("slot") := "label",
      cls := "label cn-label flex items-center gap-2 text-sm leading-none font-medium select-none peer-disabled:cursor-not-allowed peer-disabled:opacity-50",
      mods
    )
