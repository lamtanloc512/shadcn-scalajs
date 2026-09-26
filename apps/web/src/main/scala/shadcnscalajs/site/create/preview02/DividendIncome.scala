package shadcnscalajs.site.create.preview02

import com.raquo.laminar.api.L.*
import shadcnscalajs.ui.*

/** preview-02 mosaic card — ported from
  * /Users/elam/Personal/shadcn-svelte/docs/src/lib/registry/examples/create/preview-02/cards/dividend-income.svelte
  */
object DividendIncome:

  private final case class Holding(name: String, shares: String, amount: String, data: List[(String, Double)])

  private val holdings = List(
    Holding(
      "Vanguard VIG",
      "450 Shares",
      "$1,842.10",
      List(("Q1", 380.0), ("Q2", 420.0), ("Q3", 390.0), ("Q4", 652.0))
    ),
    Holding(
      "S&P 500 VOO",
      "112 Shares",
      "$928.40",
      List(("Q1", 180.0), ("Q2", 210.0), ("Q3", 320.0), ("Q4", 218.0))
    ),
    Holding(
      "Apple AAPL",
      "85 Shares",
      "$340.00",
      List(("Q1", 60.0), ("Q2", 70.0), ("Q3", 120.0), ("Q4", 90.0))
    ),
    Holding(
      "Realty Income",
      "320 Shares",
      "$1,139.50",
      List(("Q1", 240.0), ("Q2", 260.0), ("Q3", 280.0), ("Q4", 360.0))
    )
  )

  private def holdingRow(holding: Holding): HtmlElement =
    val hover = Chart.hoverVar()

    Item.of(
      _.variant(Item.Variant.Muted),
      _ =>
        Item.content(
          Item.title(holding.name),
          Item.description(holding.shares)
        ),
      _ =>
        Chart(
          cls := "hidden h-8 w-24 md:block [&_[data-slot=chart]]:h-full",
          Chart.bar(
            holding.data,
            hover,
            Chart.BarStyle(
              color = "var(--chart-2)",
              showLabels = false,
              labelFormat = identity,
              padding = 0.2
            )
          ),
          Chart.tooltip(hover, seriesLabel = "Dividend", hideLabel = true)
        ),
      _ => span(cls := "hidden text-sm font-semibold tabular-nums md:block", holding.amount)
    )

  def apply(): HtmlElement =
    Card(
      dataAttr("card") := "dividend-income",
      Card.header(
        Card.title("Q2 Dividend Income"),
        Card.description("Quarterly dividend payouts across your portfolio holdings."),
        Card.action(
          Button.of(
            _.variant(Button.Variant.Ghost),
            _.size(Button.Size.IconSm),
            _ => cls := "bg-muted",
            _ => Icons.x()
          )
        )
      ),
      Card.content(
        Item.group(holdings.map(holdingRow)*)
      )
    )
