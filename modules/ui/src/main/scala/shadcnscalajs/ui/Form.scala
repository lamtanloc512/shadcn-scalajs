package shadcnscalajs.ui

import com.raquo.laminar.api.L.*

object Form:
  def apply(mods: Modifier[HtmlElement]*): HtmlElement = form(cls := "form grid gap-6", mods)
  def item(mods: Modifier[HtmlElement]*): HtmlElement = Field(mods*)
  def label(text: String, mods: Modifier[HtmlElement]*): HtmlElement = Field.label(text, mods*)
  def description(mods: Modifier[HtmlElement]*): HtmlElement = Field.description(mods*)
  def message(mods: Modifier[HtmlElement]*): Node = Field.error(mods*)
