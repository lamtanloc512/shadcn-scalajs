package shadcnscalajs.site.create.preview02

import com.raquo.laminar.api.L.*
import shadcnscalajs.ui.*

/** preview-02 mosaic card — ported from
  * /Users/elam/Personal/shadcn-svelte/docs/src/lib/registry/examples/create/preview-02/cards/contribution-history.svelte
  */
object ContributionHistory:

  private val chartData = List(
    ("Dec", 800.0),
    ("Jan", 1100.0),
    ("Feb", 900.0),
    ("Mar", 1300.0),
    ("Apr", 750.0),
    ("May", 1400.0)
  )

  def apply(): HtmlElement =
    val hover = Chart.hoverVar()

    Card(
      dataAttr("card") := "contribution-history",
      Card.header(
        Card.title("Contribution History"),
        Card.description("Last 6 months of activity"),
        Card.action(
          Badge.of(_.variant(Badge.Variant.Secondary), _ => "+12% vs last month")
        )
      ),
      Card.content(
        Chart(
          cls := "h-[200px] w-full",
          Chart.bar(
            chartData,
            hover,
            Chart.BarStyle(
              color = "var(--chart-2)",
              showLabels = true,
              labelFormat = identity,
              padding = 0.25
            )
          ),
          Chart
            .tooltip(hover, seriesLabel = "Contribution", hideLabel = true)
            .amend(cls := "min-w-40")
        )
      ),
      Card.footer(
        cls := "flex-col gap-4",
        div(
          cls := "grid w-full grid-cols-1 gap-3 md:grid-cols-2",
          Item.of(
            _.variant(Item.Variant.Muted),
            _ => cls := "flex-col items-stretch",
            _ =>
              Item.content(
                cls := "gap-1",
                Item.description(
                  cls := "text-xs font-medium tracking-wider text-muted-foreground uppercase",
                  "Upcoming"
                ),
                span(cls := "cn-font-heading text-lg font-semibold", "May 25, 2024"),
                span(cls := "text-sm text-muted-foreground", "$1,000 scheduled")
              )
          ),
          Item.of(
            _.variant(Item.Variant.Muted),
            _ => cls := "flex-col items-stretch",
            _ =>
              Item.content(
                cls := "gap-1",
                Item.description(
                  cls := "text-xs font-medium tracking-wider text-muted-foreground uppercase",
                  "Auto-Save Plan"
                ),
                span(cls := "cn-font-heading text-lg font-semibold", "Accelerated"),
                span(cls := "text-sm text-muted-foreground", "Recurring weekly")
              )
          )
        ),
        Button.of(
          _.variant(Button.Variant.Primary),
          _.size(Button.Size.Default),
          _ => cls := "w-full",
          _ => "View Full Report"
        )
      )
    )
