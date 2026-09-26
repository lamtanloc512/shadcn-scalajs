package shadcnscalajs.site.create.preview02

import com.raquo.laminar.api.L.*
import org.scalajs.dom
import shadcnscalajs.ui.*

/** preview-02 mosaic card — ported from
  * /Users/elam/Personal/shadcn-svelte/docs/src/lib/registry/examples/create/preview-02/cards/savings-targets.svelte
  */
object SavingsTargets:

  private def savingsTargetItem(
      label: String,
      target: String,
      progress: Int,
      achievedLabel: String,
      achievedAmount: String
  ): HtmlElement =
    Item.of(
      _.variant(Item.Variant.Muted),
      _ => cls := "flex-col items-stretch",
      _ =>
        Item.content(
          cls := "gap-3",
          Item.description(
            cls := "cn-font-heading text-xs font-medium tracking-wider text-muted-foreground uppercase",
            label
          ),
          span(cls := "text-3xl font-semibold tabular-nums", target),
          Progress(progress)
        ),
      _ =>
        Item.footer(
          span(cls := "text-sm text-muted-foreground", achievedLabel),
          span(cls := "text-sm font-medium tabular-nums", achievedAmount)
        )
    )

  def apply(): HtmlElement =
    val orderTypeVar = Var("market")

    // One mosaic slot wraps both panels — only the outer grid carries data-card.
    div(
      cls := "grid grid-cols-2 gap-(--gap)",
      dataAttr("card") := "savings-targets",
      Card(
        Card.header(
          Card.title("Savings Targets"),
          Card.description("Active milestones for 2024"),
          Card.action(
            Button.of(
              _.variant(Button.Variant.Outline),
              _.size(Button.Size.Sm),
              _ => "New Goal"
            )
          )
        ),
        Card.content(
          Item.group(
            cls := "gap-3",
            savingsTargetItem("Retirement", "$420,000", 65, "65% achieved", "$273,000"),
            savingsTargetItem("Real Estate", "$85,000", 32, "32% achieved", "$27,200")
          )
        ),
        Card.footer(
          Card.description(cls := "text-center", "You have not met your targets for this year.")
        )
      ),
      Card(
        Card.header(Card.title("Buy Investment")),
        Card.content(
          cls := "flex flex-1 flex-col gap-3",
          Field.group(
            cls := "flex-1",
            Field(
              Field.label("Amount to Invest", forId := "invest-amount"),
              InputGroup(
                InputGroup.addon(
                  InputGroup.AddonAlign.InlineStart,
                  InputGroup.text("$")
                ),
                InputGroup.input(idAttr := "invest-amount", value := "1,000.00")
              )
            ),
            Field(
              Field.label("Order Type", forId := "invest-type"),
              NativeSelect(
                idAttr := "invest-type",
                value <-- orderTypeVar.signal,
                onChange --> { ev =>
                  orderTypeVar.set(ev.target.asInstanceOf[dom.html.Select].value)
                },
                option(value := "market", "Market Order"),
                option(value := "limit", "Limit Order"),
                option(value := "stop", "Stop Order")
              ),
              Field.description("Market orders execute at the current price.")
            ),
            div(
              cls := "flex flex-col gap-2",
              div(
                cls := "flex items-center justify-between",
                span(cls := "text-sm text-muted-foreground", "Estimated Shares"),
                span(cls := "text-sm font-semibold tabular-nums", "1.95")
              ),
              div(
                cls := "flex items-center justify-between",
                span(cls := "text-sm text-muted-foreground", "Buying Power"),
                span(cls := "text-sm font-semibold tabular-nums", "$12,450.00")
              )
            )
          )
        ),
        Card.footer(
          cls := "flex-col gap-3",
          Button.of(_ => cls := "w-full", _ => "Review Order"),
          Card.description(
            cls := "text-center",
            "Trades are typically executed within minutes during market hours."
          )
        )
      )
    )
