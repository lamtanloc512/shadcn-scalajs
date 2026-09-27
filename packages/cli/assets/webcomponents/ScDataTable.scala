package shadcnscalajs.webcomponents

import com.raquo.laminar.api.L.*
import org.scalajs.dom
import shadcnscalajs.ui.{Button, DataTable, Input}

import scala.scalajs.js

/** JSON columns are field names; rows are objects whose field values are displayed as text. */
class ScDataTable extends ScElementBase:

  private val columnsVar = Var(Seq.empty[String])
  private val rowsVar = Var(Seq.empty[js.Dynamic])
  private val pageSizeVar = Var(10)
  private val revision = Var(0)

  observeAttribute("columns")(v => { columnsVar.set(ScDataTable.parseColumns(v.orNull)); revision.update(_ + 1) })
  observeAttribute("rows")(v => rowsVar.set(ScDataTable.parseRows(v.orNull)))
  observeAttribute("page-size")(v => pageSizeVar.set(v.flatMap(_.toIntOption).filter(_ > 0).getOrElse(10)))
  jsonProperty("columns")(v => { columnsVar.set(ScDataTable.parseColumns(v)); revision.update(_ + 1) })
  jsonProperty("rows")(v => rowsVar.set(ScDataTable.parseRows(v)))
  stringProperty("page-size")

  mount(ScDataTable.view(columnsVar, rowsVar, pageSizeVar, revision))

object ScDataTable:
  def register(): Unit =
    ScElements.define("sc-data-table", js.constructorOf[ScDataTable], "columns", "rows", "page-size")

  private def array(value: js.Any): Seq[js.Dynamic] =
    val parsed =
      if value != null && js.typeOf(value) == "string" then
        try js.JSON.parse(value.asInstanceOf[String])
        catch case _: Throwable => null
      else value
    if parsed != null && js.Array.isArray(parsed) then parsed.asInstanceOf[js.Array[js.Dynamic]].toSeq
    else Seq.empty

  private def parseColumns(value: js.Any): Seq[String] =
    array(value).collect { case item if js.typeOf(item) == "string" => item.asInstanceOf[String] }.distinct

  private def parseRows(value: js.Any): Seq[js.Dynamic] =
    array(value).filter(item => item != null && js.typeOf(item) == "object" && !js.Array.isArray(item))

  private def field(row: js.Dynamic, key: String): String =
    val raw = row.selectDynamic(key)
    if raw == null || js.isUndefined(raw) then ""
    else if js.typeOf(raw) == "string" || js.typeOf(raw) == "number" || js.typeOf(raw) == "boolean" then raw.toString
    else ""

  private def view(
      columnsVar: Var[Seq[String]],
      rowsVar: Var[Seq[js.Dynamic]],
      pageSizeVar: Var[Int],
      revision: Var[Int]
  ): HtmlElement =
    div(
      child <-- revision.signal.map { _ =>
        lazy val columns: Seq[DataTable.Column[js.Dynamic]] = columnsVar.now().map { key =>
          DataTable.Column[js.Dynamic](
            id = key,
            header = () =>
              Button(
                typ := "button",
                aria.label := s"Sort by $key",
                onClick --> Observer(_ => state.toggleSort(key)),
                key,
                text <-- state.sorting.signal.map {
                  case Some((id, DataTable.SortDirection.Asc)) if id == key  => " ↑"
                  case Some((id, DataTable.SortDirection.Desc)) if id == key => " ↓"
                  case _                                                     => ""
                }
              ),
            cell = row => span(field(row, key)),
            accessor = row => field(row, key)
          )
        }
        lazy val state: DataTable.TableState[js.Dynamic] =
          DataTable.createTable(rowsVar.signal, columns, pageSizeVar.now(), _ => "", DataTableFilter)
        div(
          div(
            cls := "flex items-center py-4",
            label(
              span(cls := "sr-only", "Filter rows"),
              Input(typ := "search", placeholder := "Filter rows...", onInput.mapToValue --> state.globalFilter.writer)
            )
          ),
          state.view(_ => ""),
          div(
            cls := "flex items-center justify-end gap-2 py-4",
            span(text <-- state.pageIndex.signal.combineWith(state.pageCount).map { case (index, count) =>
              s"Page ${if count == 0 then 0 else index.min(count - 1) + 1} of $count"
            }),
            Button(
              typ := "button",
              disabled <-- state.canPreviousPage.not,
              onClick --> Observer(_ => state.previousPage()),
              "Previous"
            ),
            Button(
              typ := "button",
              disabled <-- state.canNextPage.not,
              onClick --> Observer(_ => state.nextPage()),
              "Next"
            )
          ),
          pageSizeVar.signal --> Observer[Int](size => { state.pageSize.set(size); state.pageIndex.set(0) })
        )
      }
    )

  private val DataTableFilter: (js.Dynamic, String, Seq[DataTable.Column[js.Dynamic]]) => Boolean =
    (row, query, columns) => columns.exists(_.accessor(row).toLowerCase.contains(query.trim.toLowerCase))
