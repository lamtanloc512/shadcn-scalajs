package shadcnscalajs.ui

import com.raquo.laminar.api.L.*

object Form:
  private val fieldSetTag = htmlTag("fieldset")
  private val legendTag = htmlTag("legend")

  def apply(mods: Modifier[HtmlElement]*): HtmlElement =
    form(cls := "form grid gap-6", mods)

  def item(mods: Modifier[HtmlElement]*): HtmlElement =
    div(dataAttr("slot") := "form-item", cls := "space-y-2", mods)

  def label(text: String, mods: Modifier[HtmlElement]*): HtmlElement =
    Label(
      dataAttr("slot") := "form-label",
      cls := "data-[fs-error]:text-destructive",
      mods,
      text
    )

  def description(mods: Modifier[HtmlElement]*): HtmlElement =
    p(
      dataAttr("slot") := "form-description",
      cls := "text-sm text-muted-foreground",
      mods
    )

  def message(mods: Modifier[HtmlElement]*): HtmlElement =
    fieldErrors(mods*)

  def fieldset(mods: Modifier[HtmlElement]*): HtmlElement =
    fieldSetTag(cls := "space-y-2", mods)

  def legend(mods: Modifier[HtmlElement]*): HtmlElement =
    legendTag(
      cls := "text-sm leading-none font-medium data-[fs-error]:text-destructive",
      mods
    )

  def fieldErrors(mods: Modifier[HtmlElement]*): HtmlElement =
    div(cls := "text-sm font-medium text-destructive", mods)
