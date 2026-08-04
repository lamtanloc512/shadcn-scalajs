package shadcnscalajs.ui

import com.raquo.laminar.api.L.*

object Field:
  private val labelTag = htmlTag("label")

  def apply(mods: Modifier[HtmlElement]*): HtmlElement = div(cls := "field grid gap-2", mods)
  def label(text: String, mods: Modifier[HtmlElement]*): HtmlElement =
    labelTag(cls := "flex items-center gap-2 text-sm leading-none font-medium select-none", mods, text)
  def description(mods: Modifier[HtmlElement]*): HtmlElement = p(cls := "text-sm text-muted-foreground", mods)
  def error(mods: Modifier[HtmlElement]*): HtmlElement = p(cls := "text-sm font-medium text-destructive", mods)
