package shadcnscalajs.blocks.dashboard01

import com.raquo.laminar.api.L.*
import shadcnscalajs.ui.*
import shadcnscalajs.ui.DataTable as UiDataTable

/** Port of shadcn-svelte `blocks/dashboard-01/components/data-table.svelte` — tabs, column visibility, paginated table
  * with row selection and a cell viewer sheet. Column drag-reorder is omitted (no DnD in `ui.DataTable`).
  */
object DataTable:

  private final case class ViewTab(id: String, title: String, badgeCount: Int)

  private val viewTabs: List[ViewTab] = List(
    ViewTab("outline", "Outline", 0),
    ViewTab("past-performance", "Past Performance", 3),
    ViewTab("key-personnel", "Key Personnel", 2),
    ViewTab("focus-documents", "Focus Documents", 0)
  )

  def apply(): HtmlElement =
    apply(Data.rows)

  def apply(rows: Seq[Data.Row]): HtmlElement =
    val tableRows = Var(rows.toSeq)
    val selectedView = Var("outline")
    val pageRowIds = Var(Seq.empty[String])
    val allPageRowsSelected = Var(false)
    val somePageRowsSelected = Var(false)
    val pageSizeSelection = Var("10")

    lazy val columns: Seq[UiDataTable.Column[Data.Row]] = Seq(
      UiDataTable.Column(
        id = "select",
        header = () =>
          Checkbox(
            allPageRowsSelected,
            somePageRowsSelected.signal,
            role := "checkbox",
            aria.label := "Select all",
            onClick --> { _ => table.toggleAllRows(pageRowIds.now()) }
          ),
        cell = row =>
          Checkbox(
            Var(table.rowSelection.now().contains(row.id.toString)),
            role := "checkbox",
            aria.label := "Select row",
            onClick --> { _ => table.toggleRow(row.id.toString) }
          ),
        accessor = _ => "",
        enableSorting = false,
        enableHiding = false
      ),
      UiDataTable.Column(
        id = "header",
        header = () => span("Header"),
        cell = row => DataTableCellViewer(row),
        accessor = _.header,
        enableHiding = false
      ),
      UiDataTable.Column(
        id = "type",
        header = () => span("Section Type"),
        cell = row =>
          div(
            cls := "w-32",
            Badge.of(_.variant(Badge.Variant.Outline), _ => cls := "px-1.5 text-muted-foreground", _ => row.sectionType)
          ),
        accessor = _.sectionType
      ),
      UiDataTable.Column(
        id = "status",
        header = () => span("Status"),
        cell = row =>
          Badge.of(
            _.variant(Badge.Variant.Outline),
            _ => cls := "px-1.5 text-muted-foreground",
            _ =>
              if row.status == "Done" then Icons.badgeCheck(svg.cls := "fill-green-500 dark:fill-green-400")
              else Icons.refreshCw(svg.cls := "size-3"),
            _ => row.status
          ),
        accessor = _.status
      ),
      UiDataTable.Column(
        id = "target",
        header = () => div(cls := "w-full text-end", "Target"),
        cell = row =>
          form(
            cls := "contents",
            onSubmit.preventDefault --> Observer.empty,
            Label(forId := s"${row.id}-target", cls := "sr-only", "Target"),
            Input(
              idAttr := s"${row.id}-target",
              defaultValue := row.target,
              cls := "h-8 w-16 border-transparent bg-transparent text-end shadow-none hover:bg-input/30 focus-visible:border focus-visible:bg-background dark:bg-transparent dark:hover:bg-input/30 dark:focus-visible:bg-input/30"
            )
          ),
        accessor = _.target
      ),
      UiDataTable.Column(
        id = "limit",
        header = () => div(cls := "w-full text-end", "Limit"),
        cell = row =>
          form(
            cls := "contents",
            onSubmit.preventDefault --> Observer.empty,
            Label(forId := s"${row.id}-limit", cls := "sr-only", "Limit"),
            Input(
              idAttr := s"${row.id}-limit",
              defaultValue := row.limit,
              cls := "h-8 w-16 border-transparent bg-transparent text-end shadow-none hover:bg-input/30 focus-visible:border focus-visible:bg-background dark:bg-transparent dark:hover:bg-input/30 dark:focus-visible:bg-input/30"
            )
          ),
        accessor = _.limit
      ),
      UiDataTable.Column(
        id = "reviewer",
        header = () => span("Reviewer"),
        cell = row => reviewerCell(row),
        accessor = _.reviewer
      ),
      UiDataTable.Column(
        id = "actions",
        header = () => span(),
        cell = _ => actionsCell(),
        accessor = _ => "",
        enableSorting = false,
        enableHiding = false
      )
    )

    lazy val table: UiDataTable.TableState[Data.Row] =
      UiDataTable.createTable[Data.Row](
        tableRows,
        columns,
        initialPageSize = 10,
        rowId = (row: Data.Row) => row.id.toString,
        filterFn = (row, query, cols) =>
          val q = query.trim.toLowerCase
          if q.isEmpty then true else cols.exists(c => c.accessor(row).toLowerCase.contains(q))
      )

    pageSizeSelection.signal --> { sizeText =>
      sizeText.toIntOption.foreach { size =>
        table.pageSize.set(size)
        table.pageIndex.set(0)
      }
    }

    val lastPageIndex = Var(0)
    table.pageCount --> { count => lastPageIndex.set(math.max(0, count - 1)) }

    def sortableHead(colId: String, title: String): Node =
      Button.of(
        _.variant(Button.Variant.Ghost),
        _.size(Button.Size.Sm),
        _ => cls := "-ms-3 h-8",
        _ => onClick --> { _ => table.toggleSort(colId) },
        _ => title
      )

    def renderHead(col: UiDataTable.Column[Data.Row]): Node =
      col.id match
        case "select" | "actions" => col.header()
        case "header"             => sortableHead("header", "Header")
        case "type"               => sortableHead("type", "Section Type")
        case "status"             => sortableHead("status", "Status")
        case "target"             => col.header()
        case "limit"              => col.header()
        case "reviewer"           => sortableHead("reviewer", "Reviewer")
        case _                    => col.header()

    val placeholderPanel = div(cls := "aspect-video w-full flex-1 rounded-lg border border-dashed")

    val outlinePanel =
      div(
        cls := "relative flex flex-col gap-4 overflow-auto px-4 lg:px-6",
        table.rows.combineWith(table.rowSelection.signal) --> { case (pageRows, selection) =>
          val ids = pageRows.map(_.id.toString)
          pageRowIds.set(ids)
          val selectedOnPage = ids.count(selection.contains)
          allPageRowsSelected.set(ids.nonEmpty && selectedOnPage == ids.size)
          somePageRowsSelected.set(selectedOnPage > 0 && selectedOnPage < ids.size)
        },
        div(
          cls := "overflow-hidden rounded-lg border",
          Table(
            Table.header(
              cls := "sticky top-0 z-10 bg-muted",
              Table.row(
                children <-- table.visibleColumns.map { cols =>
                  cols.map(col => Table.head(renderHead(col))).toList
                }
              )
            ),
            Table.body(
              cls := "**:data-[slot=table-cell]:first:w-8",
              children <-- table.rows
                .combineWith(table.rowSelection.signal, table.visibleColumns)
                .map { case (pageRows, selection, cols) =>
                  if pageRows.isEmpty then
                    List(
                      Table.row(
                        Table.cell(colSpan := cols.size.max(1), cls := "h-24 text-center", "No results.")
                      )
                    )
                  else
                    pageRows.map { row =>
                      val rowId = row.id.toString
                      val rowMods =
                        if selection.contains(rowId) then Seq(dataAttr("state") := "selected")
                        else Seq.empty[Modifier[HtmlElement]]
                      Table.row(
                        (rowMods ++ cols.map(col => Table.cell(col.cell(row))))*
                      )
                    }.toList
                }
            )
          )
        ),
        div(
          cls := "flex items-center justify-between px-4",
          div(
            cls := "hidden flex-1 text-sm text-muted-foreground lg:flex",
            child.text <-- table.selectedFilteredCount
              .combineWith(table.filteredCount)
              .map { case (selected, total) => s"$selected of $total row(s) selected." }
          ),
          div(
            cls := "flex w-full items-center gap-8 lg:w-fit",
            div(
              cls := "hidden items-center gap-2 lg:flex",
              Label(forId := "rows-per-page", cls := "text-sm font-medium", "Rows per page"),
              Select(pageSizeSelection, size = Select.Size.Sm)(ctx =>
                Seq(
                  ctx.group(
                    ctx.item("10", "10"),
                    ctx.item("20", "20"),
                    ctx.item("30", "30"),
                    ctx.item("40", "40"),
                    ctx.item("50", "50")
                  )
                )
              ).amend(cls := "w-20", idAttr := "rows-per-page")
            ),
            div(
              cls := "flex w-fit items-center justify-center text-sm font-medium",
              child.text <-- table.pageIndex.signal.combineWith(table.pageCount).map { case (idx, count) =>
                s"Page ${idx + 1} of ${count.max(1)}"
              }
            ),
            div(
              cls := "ms-auto flex items-center gap-2 lg:ms-0",
              Button.of(
                _.variant(Button.Variant.Outline),
                _ => cls := "hidden h-8 w-8 p-0 lg:flex",
                _ => disabled <-- table.canPreviousPage.map(!_),
                _ => onClick --> { _ => table.pageIndex.set(0) },
                _ => span(cls := "sr-only", "Go to first page"),
                _ => Icons.chevronRight(svg.cls := "rotate-180")
              ),
              Button.of(
                _.variant(Button.Variant.Outline),
                _.size(Button.Size.Icon),
                _ => disabled <-- table.canPreviousPage.map(!_),
                _ => onClick --> { _ => table.previousPage() },
                _ => span(cls := "sr-only", "Go to previous page"),
                _ => Icons.chevronRight(svg.cls := "rotate-180")
              ),
              Button.of(
                _.variant(Button.Variant.Outline),
                _.size(Button.Size.Icon),
                _ => disabled <-- table.canNextPage.map(!_),
                _ => onClick --> { _ => table.nextPage() },
                _ => span(cls := "sr-only", "Go to next page"),
                _ => Icons.chevronRight()
              ),
              Button.of(
                _.variant(Button.Variant.Outline),
                _.size(Button.Size.Icon),
                _ => cls := "hidden size-8 lg:flex",
                _ => disabled <-- table.canNextPage.map(!_),
                _ => onClick --> { _ => table.pageIndex.set(lastPageIndex.now()) },
                _ => span(cls := "sr-only", "Go to last page"),
                _ => Icons.chevronRight()
              )
            )
          )
        )
      )

    val tabListEl = viewTabs.foldLeft(
      div(
        role := "tablist",
        dataAttr("slot") := "tabs-list",
        dataAttr("variant") := "default",
        cls := "cn-tabs-list group/tabs-list w-fit items-center justify-center text-muted-foreground group-data-[orientation=vertical]/tabs:h-fit group-data-[orientation=vertical]/tabs:flex-col cn-tabs-list-variant-default bg-muted hidden **:data-[slot=badge]:size-5 **:data-[slot=badge]:rounded-full **:data-[slot=badge]:bg-muted-foreground/30 **:data-[slot=badge]:px-1 @4xl/main:flex"
      )
    ) { (el, tab) =>
      el.amend(
        Tabs.trigger(
          dataAttr("value") := tab.id,
          tabIndex := 0,
          aria.selected := (selectedView.now() == tab.id),
          inContext { thisNode =>
            selectedView.signal --> { view =>
              if view == tab.id then thisNode.ref.setAttribute("data-active", "true")
              else thisNode.ref.removeAttribute("data-active")
            }
          },
          onClick --> { _ => selectedView.set(tab.id) },
          tab.title,
          if tab.badgeCount > 0 then
            Badge.of(
              _.variant(Badge.Variant.Secondary),
              _ => dataAttr("slot") := "badge",
              _ => tab.badgeCount.toString
            )
          else emptyNode
        )
      )
    }

    Tabs.apply(
      cls := "w-full flex-col justify-start gap-6",
      div(
        cls := "flex items-center justify-between px-4 lg:px-6",
        Label(forId := "view-selector", cls := "sr-only", "View"),
        Select(selectedView, size = Select.Size.Sm)(ctx =>
          Seq(
            ctx.group(
              viewTabs.map(tab => ctx.item(tab.id, tab.title))*
            )
          )
        ).amend(cls := "flex w-fit @4xl/main:hidden", idAttr := "view-selector"),
        tabListEl,
        div(
          cls := "flex items-center gap-2",
          DropdownMenu.itemsWithTrigger(
            Button.appearance(Button.Variant.Outline, Button.Size.Sm),
            DropdownMenu.Align.End
          )(
            Icons.layoutDashboard(),
            span(cls := "hidden lg:inline", "Customize Columns"),
            span(cls := "lg:hidden", "Columns"),
            Icons.chevronDown()
          ) { ctx =>
            table.hideableColumns.map { col =>
              ctx.checkboxItem(
                table.isColumnVisible(col.id),
                () => table.toggleColumnVisibility(col.id),
                cls := "capitalize",
                col.id
              )
            }
          },
          Button.of(
            _.variant(Button.Variant.Outline),
            _.size(Button.Size.Sm),
            _ => Icons.plus(),
            _ => span(cls := "hidden lg:inline", "Add Section")
          )
        )
      ),
      Tabs.content(
        display <-- selectedView.signal.map(v => if v == "outline" then "flex" else "none"),
        cls := "flex-col",
        outlinePanel
      ),
      Tabs.content(
        display <-- selectedView.signal.map(v => if v == "past-performance" then "flex" else "none"),
        cls := "flex-col px-4 lg:px-6",
        placeholderPanel
      ),
      Tabs.content(
        display <-- selectedView.signal.map(v => if v == "key-personnel" then "flex" else "none"),
        cls := "flex-col px-4 lg:px-6",
        placeholderPanel
      ),
      Tabs.content(
        display <-- selectedView.signal.map(v => if v == "focus-documents" then "flex" else "none"),
        cls := "flex-col px-4 lg:px-6",
        placeholderPanel
      )
    )

  private def reviewerCell(row: Data.Row): HtmlElement =
    if row.reviewer != "Assign reviewer" then span(row.reviewer)
    else
      val reviewerPick = Var("")
      div(
        Label(forId := s"${row.id}-reviewer", cls := "sr-only", "Reviewer"),
        Select(reviewerPick, placeholder = "Assign reviewer", size = Select.Size.Sm)(ctx =>
          Seq(
            ctx.group(
              ctx.item("Eddie Lake", "Eddie Lake"),
              ctx.item("Jamik Tashpulatov", "Jamik Tashpulatov")
            )
          )
        ).amend(
          cls := "w-38 **:data-[slot=select-value]:block **:data-[slot=select-value]:truncate",
          idAttr := s"${row.id}-reviewer"
        )
      )

  private def actionsCell(): HtmlElement =
    DropdownMenu.itemsWithTrigger(
      DropdownMenu.ghostIconTrigger,
      DropdownMenu.Align.End,
      wrapperStyle = cls := "flex size-8 text-muted-foreground data-[state=open]:bg-muted"
    )(
      Icons.moreHorizontal(),
      span(cls := "sr-only", "Open menu")
    ) { ctx =>
      Seq(
        ctx.item(() => (), "Edit"),
        ctx.item(() => (), "Make a copy"),
        ctx.item(() => (), "Favorite"),
        ctx.separator(),
        ctx.item(() => (), Menu.destructive, "Delete")
      )
    }
