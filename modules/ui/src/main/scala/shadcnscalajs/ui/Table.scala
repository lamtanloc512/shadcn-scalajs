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
  private val captionTag = htmlTag("caption")

  def apply(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "table-container",
      cls := "cn-table-container relative w-full overflow-x-auto",
      tableTag(
        dataAttr("slot") := "table",
        cls := "cn-table w-full caption-bottom text-sm",
        mods
      )
    )

  def header(mods: Modifier[HtmlElement]*): HtmlElement =
    theadTag(
      dataAttr("slot") := "table-header",
      cls := "cn-table-header [&_tr]:border-b",
      mods
    )

  def body(mods: Modifier[HtmlElement]*): HtmlElement =
    tbodyTag(
      dataAttr("slot") := "table-body",
      cls := "cn-table-body [&_tr:last-child]:border-0",
      mods
    )

  def footer(mods: Modifier[HtmlElement]*): HtmlElement =
    tfootTag(
      dataAttr("slot") := "table-footer",
      cls := "cn-table-footer border-t bg-muted/50 font-medium [&>tr]:last:border-b-0",
      mods
    )

  def row(mods: Modifier[HtmlElement]*): HtmlElement =
    trTag(
      dataAttr("slot") := "table-row",
      cls := "cn-table-row border-b transition-colors hover:bg-muted/50 data-[state=selected]:bg-muted",
      mods
    )

  def head(mods: Modifier[HtmlElement]*): HtmlElement =
    thTag(
      dataAttr("slot") := "table-head",
      cls := "cn-table-head h-10 px-2 text-left align-middle font-medium text-foreground whitespace-nowrap [&:has([role=checkbox])]:pr-0",
      mods
    )

  def cell(mods: Modifier[HtmlElement]*): HtmlElement =
    tdTag(
      dataAttr("slot") := "table-cell",
      cls := "cn-table-cell p-2 align-middle whitespace-nowrap [&:has([role=checkbox])]:pr-0",
      mods
    )

  def caption(mods: Modifier[HtmlElement]*): HtmlElement =
    captionTag(
      dataAttr("slot") := "table-caption",
      cls := "cn-table-caption mt-4 text-sm text-muted-foreground",
      mods
    )
