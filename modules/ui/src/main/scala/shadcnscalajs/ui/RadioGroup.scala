package shadcnscalajs.ui

import com.raquo.laminar.api.L.*

object RadioGroup:
  def apply(mods: Modifier[HtmlElement]*): HtmlElement =
    div(role := "radiogroup", dataAttr("slot") := "radio-group", mods)

  def item(name: String = "", mods: Modifier[HtmlElement]*): HtmlElement = Radio(name, mods*)

