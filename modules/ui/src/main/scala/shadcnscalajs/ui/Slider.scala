package shadcnscalajs.ui

import com.raquo.laminar.api.L.*

object Slider:
  def apply(mods: Modifier[HtmlElement]*): HtmlElement = Range(mods*)

