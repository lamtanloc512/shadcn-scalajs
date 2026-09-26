package shadcnscalajs.site.create.preview02

import com.raquo.laminar.api.L.*
import shadcnscalajs.ui.*

/** preview-02 mosaic card — ported from
  * /Users/elam/Personal/shadcn-svelte/docs/src/lib/registry/examples/create/preview-02/cards/power-usage.svelte
  */
object PowerUsage:

  private val chartData = List(
    ("6a", 1.2),
    ("8a", 2.8),
    ("10a", 3.1),
    ("12p", 2.4),
    ("2p", 3.4),
    ("4p", 2.9),
    ("6p", 3.8),
    ("8p", 3.2)
  )

  def apply(): HtmlElement =
    val hover = Chart.hoverVar()

    Card(
      dataAttr("card") := "power-usage",
      Card.header(
        Card.title("Power Usage"),
        Card.description("Whole Home")
      ),
      Card.content(
        cls := "flex flex-col gap-4",
        Chart(
          cls := "h-[140px] w-full",
          Chart.bar(
            chartData,
            hover,
            Chart.BarStyle(
              color = "var(--chart-2)",
              showLabels = true,
              labelFormat = identity,
              padding = 0.2
            )
          ),
          Chart.tooltip(hover, seriesLabel = "Usage (kW)", hideLabel = true)
        ),
        Separator(Separator.Orientation.Horizontal),
        div(
          cls := "grid grid-cols-2 gap-4",
          div(
            cls := "flex flex-col gap-0.5",
            span(cls := "text-sm text-muted-foreground", "Currently Using"),
            span(cls := "text-lg font-semibold tabular-nums", "3.4 kW")
          ),
          div(
            cls := "flex flex-col gap-0.5",
            span(cls := "text-sm text-muted-foreground", "Solar Gen"),
            span(cls := "text-lg font-semibold text-chart-1 tabular-nums", "+1.2 kW")
          )
        )
      ),
      Card.footer(
        cls := "flex-col items-start gap-1",
        span(cls := "text-sm text-muted-foreground", "Battery Level"),
        div(
          cls := "flex w-full items-center gap-2",
          Progress(85, cls := "flex-1"),
          span(cls := "text-sm font-medium tabular-nums", "85%")
        )
      )
    )
