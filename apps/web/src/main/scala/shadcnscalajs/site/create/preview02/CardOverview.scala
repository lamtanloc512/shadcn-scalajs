package shadcnscalajs.site.create.preview02

import com.raquo.laminar.api.L.*
import shadcnscalajs.ui.*

/** preview-02 mosaic card — ported from
  * /Users/elam/Personal/shadcn-svelte/docs/src/lib/registry/examples/create/preview-02/cards/card-overview.svelte
  */
object CardOverview:

  private val activityData = List(
    ("Jan", 40.0),
    ("Feb", 55.0),
    ("Mar", 35.0),
    ("Apr", 60.0),
    ("May", 45.0),
    ("Jun", 50.0),
    ("Jul", 65.0),
    ("Aug", 40.0),
    ("Sep", 55.0),
    ("Oct", 70.0),
    ("Nov", 45.0),
    ("Dec", 80.0)
  )

  def apply(): HtmlElement =
    val hover = Chart.hoverVar()

    div(
      dataAttr("card") := "card-overview",
      cls := "grid grid-cols-2 gap-3",
      Card(
        Card.content(
          Card.description("Card Balance"),
          Card.title(cls := "text-2xl tabular-nums", "US$12.94"),
          Card.description(cls := "tabular-nums", "US$11,337.06 Available")
        )
      ),
      Card(
        cls := "flex flex-col justify-between",
        Card.content(
          cls := "flex flex-1 flex-col justify-between",
          div(
            cls := "flex flex-col gap-1",
            Card.description("Payment Due"),
            Card.title(cls := "text-2xl", "1 Apr")
          ),
          Button.of(
            _.variant(Button.Variant.Outline),
            _.size(Button.Size.Sm),
            _ => cls := "mt-3 w-full",
            _ => "Pay Early"
          )
        )
      ),
      Card(
        cls := "col-span-2",
        Card.content(
          cls := "flex flex-col gap-2",
          div(
            cls := "flex items-center justify-between",
            Card.description("Yearly Activity"),
            Badge.of(_.variant(Badge.Variant.Secondary), _ => "+US$0.25 Daily Cash")
          ),
          Chart(
            cls := "h-20 w-full",
            Chart.bar(
              activityData,
              hover,
              Chart.BarStyle(
                color = "var(--chart-2)",
                showLabels = true,
                labelFormat = _.take(1),
                padding = 0.2
              )
            ),
            Chart.tooltip(hover, seriesLabel = "Activity", hideLabel = true)
          )
        )
      )
    )
