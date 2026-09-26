package shadcnscalajs.site.create.preview02

import com.raquo.laminar.api.L.*
import shadcnscalajs.ui.*

/** preview-02 mosaic card — ported from
  * /Users/elam/Personal/shadcn-svelte/docs/src/lib/registry/examples/create/preview-02/cards/payout-threshold.svelte
  */
object PayoutThreshold:

  private val currencies = List(
    ("usd", "USD — United States Dollar"),
    ("eur", "EUR — Euro"),
    ("gbp", "GBP — British Pound"),
    ("jpy", "JPY — Japanese Yen")
  )

  def apply(): HtmlElement =
    val amountVar = Var(List(3000.0))
    val currencyVar = Var("usd")

    Card(
      dataAttr("card") := "payout-threshold",
      Card.header(
        Card.title("Payout Threshold"),
        Card.description("Set the minimum balance required before a payout is triggered."),
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
        Field.group(
          Field(
            Field.label("Preferred Currency", forId := "preferred-currency"),
            Select.stateful(
              currencyVar,
              currencies,
              idAttr := "preferred-currency",
              cls := "w-full"
            )
          ),
          Field(
            div(
              cls := "flex items-baseline justify-between",
              Field.label("Minimum Payout Amount", forId := "min-payout"),
              span(
                cls := "text-2xl font-semibold tabular-nums",
                child.text <-- amountVar.signal.map { amounts =>
                  f"$$${amounts.headOption.getOrElse(0.0)}%.2f"
                }
              )
            ),
            Slider.multiple(
              amountVar,
              min = 50,
              max = 10000,
              step = 50,
              idAttr := "min-payout"
            ),
            div(
              cls := "flex items-center justify-between",
              Field.description("$50 (MIN)"),
              Field.description("$10,000 (MAX)")
            )
          ),
          Field(
            Field.label("Notes", forId := "payout-notes"),
            Textarea(
              idAttr := "payout-notes",
              placeholder := "Add any notes for this payout configuration...",
              cls := "min-h-[100px]"
            )
          )
        )
      ),
      Card.footer(
        Button.of(
          _ => cls := "w-full",
          _ => "Save Threshold"
        )
      )
    )
