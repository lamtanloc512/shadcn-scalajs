package shadcnscalajs.site.create.preview02

import com.raquo.laminar.api.L.*
import shadcnscalajs.ui.*

/** Port of shadcn-svelte preview-02 `cards/claimable-balance.svelte`. */
object ClaimableBalance:
  def apply(): HtmlElement =
    Card(
      dataAttr("card") := "claimable-balance",
      Card.header(
        Card.description("Claimable Balance"),
        Card.title(cls := "text-5xl tabular-nums", "$0.00"),
        Badge.of(
          _.variant(Badge.Variant.Outline),
          _ => span(cls := "size-2 rounded-full bg-yellow-500"),
          _ => "Pending Setup"
        )
      ),
      Card.content(
        cls := "flex flex-1 flex-col justify-end",
        Item.of(
          _.variant(Item.Variant.Muted),
          _ => cls := "flex-col items-stretch",
          _ =>
            Item.content(
              cls := "gap-3",
              div(
                cls := "flex items-center justify-between",
                span(cls := "text-sm text-muted-foreground", "Net Royalties"),
                span(cls := "text-sm font-medium tabular-nums", "$0.00")
              ),
              div(
                cls := "flex items-center justify-between",
                span(cls := "text-sm text-muted-foreground", "Processing Fee"),
                span(cls := "text-sm font-medium tabular-nums", "-$0.00")
              ),
              Separator(),
              div(
                cls := "flex items-center justify-between",
                span(cls := "text-sm text-muted-foreground", "Total Ready to Claim"),
                span(cls := "text-sm font-semibold tabular-nums", "$0.00 USD")
              )
            )
        )
      ),
      Card.footer(
        Card.description(
          "Once your bank is connected, balances over $10.00 are automatically eligible for monthly distribution on the 15th of each month."
        )
      )
    )
