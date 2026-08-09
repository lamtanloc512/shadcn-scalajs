package shadcnscalajs.blocks.dashboard01

import com.raquo.laminar.api.L.*
import shadcnscalajs.ui.*

/** Port of shadcn-svelte `data-table-cell-viewer.svelte` — row header opens a side sheet with a mini chart and form
  * fields. Upstream uses `Drawer`; this block uses [[Sheet]] (same panel role, matches Task 8 plan).
  */
object DataTableCellViewer:

  private final case class ChartPoint(month: String, desktop: Double, mobile: Double)

  private val chartPoints: List[ChartPoint] = List(
    ChartPoint("Jan", 186, 80),
    ChartPoint("Feb", 305, 200),
    ChartPoint("Mar", 237, 120),
    ChartPoint("Apr", 73, 190),
    ChartPoint("May", 209, 130),
    ChartPoint("Jun", 214, 140)
  )

  def apply(row: Data.Row): HtmlElement =
    val sheetOpen = Var(false)
    val typeSelection = Var(row.sectionType)
    val statusSelection = Var(row.status)
    val reviewerSelection = Var(row.reviewer)
    val hover = Chart.HoverVar()

    div(
      Button.of(
        _.variant(Button.Variant.Link),
        _ => cls := "w-fit px-0 text-start text-foreground",
        _ => onClick --> { _ => sheetOpen.set(true) },
        _ => row.header
      ),
      Sheet(sheetOpen, Sheet.Side.Right)(
        Sheet.close(onClick --> { _ => sheetOpen.set(false) }),
        Sheet.header(
          Sheet.title(row.header),
          Sheet.description("Showing total visitors for the last 6 months")
        ),
        div(
          cls := "flex flex-col gap-4 overflow-y-auto px-4 text-sm",
          div(
            cls := "relative aspect-video w-full",
            Chart.area(
              chartPoints.map(p => p.month -> p.desktop),
              hover,
              Chart.AreaStyle(color = "var(--primary)", fillOpacity = 0.4, showLabels = false),
              svg.cls := "size-full"
            ),
            Chart.tooltip(hover, seriesLabel = "Desktop", indicator = Chart.TooltipIndicator.Dot)
          ),
          Separator(),
          div(
            cls := "grid gap-2",
            div(
              cls := "flex gap-2 leading-none font-medium",
              "Trending up by 5.2% this month ",
              Icons.trendingUp()
            ),
            div(
              cls := "text-muted-foreground",
              "Showing total visitors for the last 6 months. This is just some random text to test the layout. It spans multiple lines and should wrap around."
            )
          ),
          Separator(),
          div(
            cls := "flex flex-col gap-4",
            div(
              cls := "flex flex-col gap-3",
              Label(forId := "header", "Header"),
              Input(idAttr := "header", defaultValue := row.header)
            ),
            div(
              cls := "grid grid-cols-2 gap-4",
              div(
                cls := "flex flex-col gap-3",
                Label(forId := "type", "Type"),
                Select(typeSelection, placeholder = "Select a type")(ctx =>
                  Seq(
                    ctx.group(
                      ctx.item("Table of Contents", "Table of Contents"),
                      ctx.item("Executive Summary", "Executive Summary"),
                      ctx.item("Technical Approach", "Technical Approach"),
                      ctx.item("Design", "Design"),
                      ctx.item("Capabilities", "Capabilities"),
                      ctx.item("Focus Documents", "Focus Documents"),
                      ctx.item("Narrative", "Narrative"),
                      ctx.item("Cover Page", "Cover Page")
                    )
                  )
                ).amend(idAttr := "type")
              ),
              div(
                cls := "flex flex-col gap-3",
                Label(forId := "status", "Status"),
                Select(statusSelection, placeholder = "Select a status")(ctx =>
                  Seq(
                    ctx.group(
                      ctx.item("Done", "Done"),
                      ctx.item("In Progress", "In Progress"),
                      ctx.item("Not Started", "Not Started")
                    )
                  )
                ).amend(idAttr := "status")
              )
            ),
            div(
              cls := "grid grid-cols-2 gap-4",
              div(
                cls := "flex flex-col gap-3",
                Label(forId := "target", "Target"),
                Input(idAttr := "target", defaultValue := row.target)
              ),
              div(
                cls := "flex flex-col gap-3",
                Label(forId := "limit", "Limit"),
                Input(idAttr := "limit", defaultValue := row.limit)
              )
            ),
            div(
              cls := "flex flex-col gap-3",
              Label(forId := "reviewer", "Reviewer"),
              Select(reviewerSelection, placeholder = "Select a reviewer")(ctx =>
                Seq(
                  ctx.group(
                    ctx.item("Eddie Lake", "Eddie Lake"),
                    ctx.item("Jamik Tashpulatov", "Jamik Tashpulatov"),
                    ctx.item("Emily Whalen", "Emily Whalen")
                  )
                )
              ).amend(idAttr := "reviewer")
            )
          )
        ),
        Sheet.footer(
          Button.of(_.variant(Button.Variant.Primary), _ => "Submit"),
          Button.of(
            _.variant(Button.Variant.Outline),
            _ => onClick --> { _ => sheetOpen.set(false) },
            _ => "Done"
          )
        )
      )
    )
