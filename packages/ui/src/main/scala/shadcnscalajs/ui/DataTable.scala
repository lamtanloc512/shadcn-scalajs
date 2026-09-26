package shadcnscalajs.ui

import com.raquo.laminar.api.L.*

/** Pure Scala table-state utilities composing [[Table]] — no TanStack / JS table facades.
  *
  * Demos typically call [[createTable]], wire toolbar controls to the returned [[TableState]] Vars, and either use
  * [[TableState.view]] or compose [[Table]] manually for custom columns (e.g. row-selection checkboxes).
  */
object DataTable:

  enum SortDirection derives CanEqual:
    case Asc, Desc

  /** Column definition: id, header/cell renderers, accessor for sort/filter, optional sorting/hiding flags. */
  final case class Column[T](
      id: String,
      header: () => Node,
      cell: T => Node,
      accessor: T => String = (_: T) => "",
      enableSorting: Boolean = true,
      enableHiding: Boolean = true
  )

  object Column:
    def text[T](
        id: String,
        headerText: String,
        accessor: T => String,
        cell: T => Node,
        enableSorting: Boolean = true,
        enableHiding: Boolean = true
    ): Column[T] =
      Column(id, () => span(headerText), cell, accessor, enableSorting, enableHiding)

  final case class TableState[T](
      columns: Seq[Column[T]],
      data: Signal[Seq[T]],
      sorting: Var[Option[(String, SortDirection)]],
      globalFilter: Var[String],
      pageIndex: Var[Int],
      pageSize: Var[Int],
      rowSelection: Var[Set[String]],
      columnVisibility: Var[Map[String, Boolean]],
      filteredRows: Signal[Seq[T]],
      sortedRows: Signal[Seq[T]],
      rows: Signal[Seq[T]],
      pageCount: Signal[Int],
      filteredCount: Signal[Int],
      selectedFilteredCount: Signal[Int]
  ):
    def visibleColumns: Signal[Seq[Column[T]]] =
      columnVisibility.signal.map { vis =>
        columns.filter(c => vis.getOrElse(c.id, true))
      }

    def isColumnVisible(columnId: String): Signal[Boolean] =
      columnVisibility.signal.map(_.getOrElse(columnId, true))

    def hideableColumns: Seq[Column[T]] =
      columns.filter(_.enableHiding)

    def toggleSort(columnId: String): Unit =
      columns.find(_.id == columnId).filter(_.enableSorting).foreach { _ =>
        sorting.update {
          case Some((id, SortDirection.Asc)) if id == columnId => Some((columnId, SortDirection.Desc))
          case Some((id, _)) if id == columnId                 => None
          case _                                               => Some((columnId, SortDirection.Asc))
        }
        pageIndex.set(0)
      }

    def toggleColumnVisibility(columnId: String): Unit =
      columns.find(_.id == columnId).filter(_.enableHiding).foreach { _ =>
        columnVisibility.update { vis =>
          val current = vis.getOrElse(columnId, true)
          vis.updated(columnId, !current)
        }
      }

    def setColumnVisible(columnId: String, visible: Boolean): Unit =
      columnVisibility.update(_.updated(columnId, visible))

    def toggleRow(rowId: String): Unit =
      rowSelection.update(sel => if sel.contains(rowId) then sel - rowId else sel + rowId)

    def toggleAllRows(rowIds: Seq[String]): Unit =
      val current = rowSelection.now()
      if rowIds.nonEmpty && rowIds.forall(current.contains) then rowSelection.set(current -- rowIds)
      else rowSelection.set(current ++ rowIds)

    def isRowSelected(rowId: String): Signal[Boolean] =
      rowSelection.signal.map(_.contains(rowId))

    def isAllRowsSelected(rowIds: Signal[Seq[String]]): Signal[Boolean] =
      rowIds.combineWith(rowSelection.signal).map { case (ids, sel) =>
        ids.nonEmpty && ids.forall(sel.contains)
      }

    def isSomeRowsSelected(rowIds: Signal[Seq[String]]): Signal[Boolean] =
      rowIds.combineWith(rowSelection.signal).map { case (ids, sel) =>
        val selected = ids.count(sel.contains)
        selected > 0 && selected < ids.size
      }

    def previousPage(): Unit =
      pageIndex.update(i => math.max(0, i - 1))

    def nextPage(): Unit =
      pageIndex.update(_ + 1)

    def canPreviousPage: Signal[Boolean] =
      pageIndex.signal.map(_ > 0)

    def canNextPage: Signal[Boolean] =
      pageIndex.signal.combineWith(pageCount).map { case (idx, count) =>
        count > 0 && idx < count - 1
      }

    /** Renders header + body using [[Table]] primitives and [[visibleColumns]] / [[rows]]. */
    def view(rowId: T => String): HtmlElement =
      div(
        cls := "rounded-md border",
        Table(
          Table.header(
            Table.row(
              children <-- visibleColumns.map { cols =>
                cols.map(col => Table.head(cls := "[&:has([role=checkbox])]:ps-3", col.header())).toList
              }
            )
          ),
          Table.body(
            children <-- rows.combineWith(rowSelection.signal, visibleColumns).map { case (pageRows, selection, cols) =>
              if pageRows.isEmpty then
                List(
                  Table.row(
                    Table.cell(colSpan := cols.size.max(1), cls := "h-24 text-center", "No results.")
                  )
                )
              else
                pageRows.map { row =>
                  val id = rowId(row)
                  val rowMods =
                    if selection.contains(id) then Seq(dataAttr("state") := "selected")
                    else Seq.empty[Modifier[HtmlElement]]
                  Table.row(
                    (rowMods ++ cols.map(col => Table.cell(cls := "[&:has([role=checkbox])]:ps-3", col.cell(row))))*
                  )
                }.toList
            }
          )
        )
      )

  def createTable[T](
      data: Var[Seq[T]],
      columns: Seq[Column[T]],
      initialPageSize: Int = 10,
      rowId: T => String = (_: T) => "",
      filterFn: (T, String, Seq[Column[T]]) => Boolean = defaultFilter
  ): TableState[T] =
    createTableFromSignal(data.signal, columns, initialPageSize, rowId, filterFn)

  def createTable[T](
      data: Signal[Seq[T]],
      columns: Seq[Column[T]],
      initialPageSize: Int,
      rowId: T => String,
      filterFn: (T, String, Seq[Column[T]]) => Boolean
  ): TableState[T] =
    createTableFromSignal(data, columns, initialPageSize, rowId, filterFn)

  private def createTableFromSignal[T](
      data: Signal[Seq[T]],
      columns: Seq[Column[T]],
      initialPageSize: Int,
      rowId: T => String,
      filterFn: (T, String, Seq[Column[T]]) => Boolean
  ): TableState[T] =
    val sorting = Var(Option.empty[(String, SortDirection)])
    val globalFilter = Var("")
    val pageIndex = Var(0)
    val pageSize = Var(initialPageSize)
    val rowSelection = Var(Set.empty[String])
    val columnVisibility = Var(Map.empty[String, Boolean])

    val filteredRows =
      data.combineWith(globalFilter.signal).map { case (rows, filter) =>
        applyFilter(rows, filter, columns, filterFn)
      }

    val sortedRows =
      filteredRows.combineWith(sorting.signal).map { case (rows, sort) =>
        applySort(rows, sort, columns)
      }

    val filteredCount = filteredRows.map(_.size)

    val pageCount =
      filteredCount.combineWith(pageSize.signal).map { case (count, size) =>
        if count == 0 then 0 else (count + size - 1) / size
      }

    val effectivePageIndex =
      pageIndex.signal.combineWith(pageCount).map { case (idx, count) =>
        if count == 0 then 0 else math.min(idx, count - 1)
      }

    val rows =
      sortedRows.combineWith(effectivePageIndex, pageSize.signal).map { case (sorted, idx, size) =>
        val start = idx * size
        sorted.slice(start, start + size)
      }

    val selectedFilteredCount =
      filteredRows.combineWith(rowSelection.signal).map { case (rows, sel) =>
        rows.count(r => sel.contains(rowId(r)))
      }

    TableState(
      columns = columns,
      data = data,
      sorting = sorting,
      globalFilter = globalFilter,
      pageIndex = pageIndex,
      pageSize = pageSize,
      rowSelection = rowSelection,
      columnVisibility = columnVisibility,
      filteredRows = filteredRows,
      sortedRows = sortedRows,
      rows = rows,
      pageCount = pageCount,
      filteredCount = filteredCount,
      selectedFilteredCount = selectedFilteredCount
    )

  private def defaultFilter[T](row: T, query: String, columns: Seq[Column[T]]): Boolean =
    val q = query.trim.toLowerCase
    if q.isEmpty then true
    else columns.exists(c => c.accessor(row).toLowerCase.contains(q))

  private def applyFilter[T](
      rows: Seq[T],
      filter: String,
      columns: Seq[Column[T]],
      filterFn: (T, String, Seq[Column[T]]) => Boolean
  ): Seq[T] =
    if filter.trim.isEmpty then rows else rows.filter(r => filterFn(r, filter, columns))

  private def applySort[T](
      rows: Seq[T],
      sorting: Option[(String, SortDirection)],
      columns: Seq[Column[T]]
  ): Seq[T] =
    sorting match
      case None => rows
      case Some((colId, dir)) =>
        columns.find(_.id == colId).filter(_.enableSorting) match
          case None      => rows
          case Some(col) =>
            // Case-insensitive, matching TanStack's alphanumeric sort: raw string order would put every
            // capitalized value ahead of the lowercase ones.
            val sorted = rows.sortBy(row => col.accessor(row).toLowerCase)
            if dir == SortDirection.Desc then sorted.reverse else sorted
