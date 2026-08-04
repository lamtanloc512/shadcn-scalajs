package shadcnscalajs.ui

import com.raquo.laminar.api.L.*

object Scrollbar:
  def apply(mods: Modifier[HtmlElement]*): HtmlElement =
    div(cls := "relative overflow-auto", mods)
