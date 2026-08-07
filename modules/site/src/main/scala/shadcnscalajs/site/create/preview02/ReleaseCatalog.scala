package shadcnscalajs.site.create.preview02

import com.raquo.laminar.api.L.*
import shadcnscalajs.ui.*

/** preview-02 mosaic card — port from release-catalog.svelte */
object ReleaseCatalog:

  private final case class Holding(
      ticker: String,
      name: String,
      holdingType: String,
      added: String,
      shares: String,
      value: String
  )

  private val holdings: List[Holding] = List(
    Holding("VOO", "Vanguard S&P 500 ETF", "ETF", "Jan 2021", "112", "$48,230.40"),
    Holding("VIG", "Vanguard Dividend Appreciation", "ETF", "Mar 2022", "450", "$26,033.79"),
    Holding("AAPL", "Apple Inc.", "Stock", "Nov 2020", "85", "$18,488.90"),
    Holding("O", "Realty Income Corp", "REIT", "Jun 2023", "320", "$15,136.59")
  )

  private def filterKey(holdingType: String): String = holdingType match
    case "Stock" => "stocks"
    case "ETF"   => "etfs"
    case "REIT"  => "reits"
    case other   => other.toLowerCase

  def apply(): HtmlElement =
    val filters = Var(Set("etfs"))

    val visibleHoldings =
      filters.signal.map { active =>
        if active.isEmpty then Nil
        else holdings.filter(h => active.contains(filterKey(h.holdingType)))
      }

    Card(
      dataAttr("card") := "release-catalog",
      Card.header(
        div(
          cls := "flex items-center justify-between gap-3",
          InputGroup(
            cls := "max-w-sm",
            InputGroup.addon(InputGroup.AddonAlign.InlineStart, Icons.search()),
            InputGroup.input(placeholder := "Search holdings or tickers...")
          ),
          ToggleGroup.multiple(
            filters,
            Toggle.Variant.Outline,
            Toggle.Size.Default,
            1,
            ToggleGroup.Orientation.Horizontal,
            Nil,
            ToggleGroup.Item("stocks", "Stocks"),
            ToggleGroup.Item("etfs", "ETFs"),
            ToggleGroup.Item("reits", "REITs")
          )
        )
      ),
      Card.content(
        Item.group(
          children <-- visibleHoldings.map { rows =>
            rows.map { holding =>
              Item.of(
                _.variant(Item.Variant.Muted),
                _ =>
                  Seq(
                    Item.media(
                      Item.MediaVariant.Default,
                      div(
                        cls := "flex size-12 items-center justify-center rounded-lg border text-sm font-semibold",
                        holding.ticker
                      )
                    ),
                    Item.content(
                      Item.title(holding.name),
                      Item.description(
                        cls := "text-xs tracking-wider uppercase",
                        s"${holding.shares} Shares · ${holding.added}"
                      )
                    ),
                    div(
                      cls := "flex shrink-0 items-center gap-6",
                      Badge.of(_.variant(Badge.Variant.Outline), _ => holding.holdingType),
                      div(
                        cls := "flex flex-col items-end gap-0.5",
                        span(
                          cls := "text-xs tracking-wider text-muted-foreground uppercase",
                          "Value"
                        ),
                        span(cls := "font-medium tabular-nums", holding.value)
                      )
                    )
                  )
              )
            }
          }
        )
      )
    )
