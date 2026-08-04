package shadcnscalajs.ui

import com.raquo.laminar.api.L.*

object ScrollArea:
  def apply(mods: Modifier[HtmlElement]*): HtmlElement =
    div(cls := "scroll-area scrollbar relative overflow-auto", mods)

