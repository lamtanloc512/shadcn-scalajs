package shadcnscalajs.webcomponents

import org.scalajs.dom
import shadcnscalajs.ui.Table

import scala.scalajs.js

/** Light-DOM overflow host for tables. Must wrap a real `<table>` — autonomous custom elements are not table-like to
  * the HTML parser, so putting `<tr>` directly under a shadow/`sc-*` host gets foster-parented out of the table.
  */
class ScTable extends LightPrimitive(Table.containerSlot, Table.containerClass)

/** Customized built-ins for table parts. Author as `<tr is="sc-table-row">` inside a real `<table>`. */
private object TablePart:
  def stamp(el: dom.Element, slotName: String, className: String): Unit =
    el.setAttribute("data-slot", slotName)
    val head = className.split(' ').headOption.getOrElse("")
    if head.nonEmpty && el.classList.contains(head) then ()
    else
      val existing = Option(el.getAttribute("class")).filter(_.nonEmpty)
      el.setAttribute("class", existing.fold(className)(c => s"$className $c"))

class ScTableBody extends dom.HTMLTableSectionElement:
  TablePart.stamp(this, Table.bodySlot, Table.bodyClass)

class ScTableHeader extends dom.HTMLTableSectionElement:
  TablePart.stamp(this, Table.headerSlot, Table.headerClass)

class ScTableFooter extends dom.HTMLTableSectionElement:
  TablePart.stamp(this, Table.footerSlot, Table.footerClass)

class ScTableRow extends dom.HTMLTableRowElement:
  TablePart.stamp(this, Table.rowSlot, Table.rowClass)

class ScTableCell extends dom.HTMLTableCellElement:
  TablePart.stamp(this, Table.cellSlot, Table.cellClass)

class ScTableHead extends dom.HTMLTableCellElement:
  TablePart.stamp(this, Table.headSlot, Table.headClass)

object ScTableParts:
  def register(): Unit =
    ScElements.define("sc-table", js.constructorOf[ScTable])
    ScElements.defineBuiltin("sc-table-body", js.constructorOf[ScTableBody], "tbody")
    ScElements.defineBuiltin("sc-table-header", js.constructorOf[ScTableHeader], "thead")
    ScElements.defineBuiltin("sc-table-footer", js.constructorOf[ScTableFooter], "tfoot")
    ScElements.defineBuiltin("sc-table-row", js.constructorOf[ScTableRow], "tr")
    ScElements.defineBuiltin("sc-table-cell", js.constructorOf[ScTableCell], "td")
    ScElements.defineBuiltin("sc-table-head", js.constructorOf[ScTableHead], "th")
    // Parsed `is=` elements upgrade on define(); also stamp any that landed before the module ran.
    upgradeExisting()

  private def upgradeExisting(): Unit =
    stampAll("tbody[is='sc-table-body']", Table.bodySlot, Table.bodyClass)
    stampAll("thead[is='sc-table-header']", Table.headerSlot, Table.headerClass)
    stampAll("tfoot[is='sc-table-footer']", Table.footerSlot, Table.footerClass)
    stampAll("tr[is='sc-table-row']", Table.rowSlot, Table.rowClass)
    stampAll("td[is='sc-table-cell']", Table.cellSlot, Table.cellClass)
    stampAll("th[is='sc-table-head']", Table.headSlot, Table.headClass)

  private def stampAll(selector: String, slotName: String, className: String): Unit =
    val nodes = dom.document.querySelectorAll(selector)
    var i = 0
    while i < nodes.length do
      nodes.item(i) match
        case el: dom.Element => TablePart.stamp(el, slotName, className)
        case null            => ()
      i += 1
