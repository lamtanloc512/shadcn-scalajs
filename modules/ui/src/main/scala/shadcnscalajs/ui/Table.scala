package shadcnscalajs.ui

import com.raquo.laminar.api.L.*

object Table:
  private val tableTag = htmlTag("table")
  private val theadTag = htmlTag("thead")
  private val tbodyTag = htmlTag("tbody")
  private val tfootTag = htmlTag("tfoot")
  private val trTag = htmlTag("tr")
  private val thTag = htmlTag("th")
  private val tdTag = htmlTag("td")

  def apply(mods: Modifier[HtmlElement]*): HtmlElement =
    div(cls := "relative w-full overflow-auto", tableTag(cls := "w-full caption-bottom text-sm", mods))
  def header(mods: Modifier[HtmlElement]*): HtmlElement = theadTag(cls := "[&_tr]:border-b", mods)
  def body(mods: Modifier[HtmlElement]*): HtmlElement = tbodyTag(cls := "[&_tr:last-child]:border-0", mods)
  def footer(mods: Modifier[HtmlElement]*): HtmlElement =
    tfootTag(cls := "border-t bg-muted/50 font-medium [&>tr]:last:border-b-0", mods)
  def row(mods: Modifier[HtmlElement]*): HtmlElement =
    trTag(cls := "border-b transition-colors hover:bg-muted/50 data-[state=selected]:bg-muted", mods)
  def head(mods: Modifier[HtmlElement]*): HtmlElement = thTag(
    cls := "h-10 px-2 text-left align-middle font-medium text-muted-foreground [&:has([role=checkbox])]:pr-0",
    mods
  )
  def cell(mods: Modifier[HtmlElement]*): HtmlElement =
    tdTag(cls := "p-2 align-middle [&:has([role=checkbox])]:pr-0", mods)
