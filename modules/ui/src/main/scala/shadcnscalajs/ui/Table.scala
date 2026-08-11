package shadcnscalajs.ui

import com.raquo.laminar.api.L.*

/** shadcn/ui Table primitives.
  *
  * `*Slot` / `*Class` vals are shared with the light-DOM / customized-builtin `sc-table-*` web components.
  */
object Table:
  private val tableTag = htmlTag("table")
  private val theadTag = htmlTag("thead")
  private val tbodyTag = htmlTag("tbody")
  private val tfootTag = htmlTag("tfoot")
  private val trTag = htmlTag("tr")
  private val thTag = htmlTag("th")
  private val tdTag = htmlTag("td")
  private val captionTag = htmlTag("caption")

  val containerSlot = "table-container"
  val tableSlot = "table"
  val headerSlot = "table-header"
  val bodySlot = "table-body"
  val footerSlot = "table-footer"
  val rowSlot = "table-row"
  val headSlot = "table-head"
  val cellSlot = "table-cell"
  val captionSlot = "table-caption"

  val containerClass = "cn-table-container relative w-full overflow-x-auto"
  val tableClass = "cn-table w-full caption-bottom text-sm"
  val headerClass = "cn-table-header [&_tr]:border-b"
  val bodyClass = "cn-table-body [&_tr:last-child]:border-0"
  val footerClass = "cn-table-footer border-t bg-muted/50 font-medium [&>tr]:last:border-b-0"
  val rowClass =
    "cn-table-row border-b transition-colors hover:bg-muted/50 data-[state=selected]:bg-muted"
  val headClass =
    "cn-table-head h-10 px-2 text-left align-middle font-medium text-foreground whitespace-nowrap [&:has([role=checkbox])]:pr-0"
  val cellClass =
    "cn-table-cell p-2 align-middle whitespace-nowrap [&:has([role=checkbox])]:pr-0"
  val captionClass = "cn-table-caption mt-4 text-sm text-muted-foreground"

  def apply(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := containerSlot,
      cls := containerClass,
      tableTag(
        dataAttr("slot") := tableSlot,
        cls := tableClass,
        mods
      )
    )

  def header(mods: Modifier[HtmlElement]*): HtmlElement =
    theadTag(
      dataAttr("slot") := headerSlot,
      cls := headerClass,
      mods
    )

  def body(mods: Modifier[HtmlElement]*): HtmlElement =
    tbodyTag(
      dataAttr("slot") := bodySlot,
      cls := bodyClass,
      mods
    )

  def footer(mods: Modifier[HtmlElement]*): HtmlElement =
    tfootTag(
      dataAttr("slot") := footerSlot,
      cls := footerClass,
      mods
    )

  def row(mods: Modifier[HtmlElement]*): HtmlElement =
    trTag(
      dataAttr("slot") := rowSlot,
      cls := rowClass,
      mods
    )

  def head(mods: Modifier[HtmlElement]*): HtmlElement =
    thTag(
      dataAttr("slot") := headSlot,
      cls := headClass,
      mods
    )

  def cell(mods: Modifier[HtmlElement]*): HtmlElement =
    tdTag(
      dataAttr("slot") := cellSlot,
      cls := cellClass,
      mods
    )

  def caption(mods: Modifier[HtmlElement]*): HtmlElement =
    captionTag(
      dataAttr("slot") := captionSlot,
      cls := captionClass,
      mods
    )
