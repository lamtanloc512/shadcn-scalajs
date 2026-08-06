package shadcnscalajs.site.create.preview02

import com.raquo.laminar.api.L.*
import shadcnscalajs.ui.*

/** preview-02 mosaic card — ported from
  * /Users/elam/Personal/shadcn-svelte/docs/src/lib/registry/examples/create/preview-02/cards/transfer-funds.svelte
  */
object TransferFunds:

  private val fromAccounts = List(
    ("checking", "Main Checking (··8402) — $12,450.00"),
    ("business", "Business (··7731) — $8,920.00")
  )

  private val toAccounts = List(
    ("savings", "High Yield Savings (··1192) — $42,100.00"),
    ("investment", "Investment (··3349) — $18,200.00")
  )

  def apply(): HtmlElement =
    val fromAccountVar = Var("checking")
    val toAccountVar = Var("savings")

    Card(
      dataAttr("card") := "transfer-funds",
      Card.header(
        Card.title("Transfer Funds"),
        Card.description("Move money between your connected accounts."),
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
            Field.label("Amount to Transfer", forId := "transfer-amount"),
            InputGroup(
              InputGroup.addon(
                InputGroup.AddonAlign.InlineStart,
                InputGroup.text("$")
              ),
              InputGroup.input(idAttr := "transfer-amount", value := "1,200.00")
            )
          ),
          Field(
            Field.label("From Account", forId := "from-account"),
            Select.stateful(
              fromAccountVar,
              fromAccounts,
              idAttr := "from-account",
              cls := "w-full"
            )
          ),
          Field(
            Field.label("To Account", forId := "to-account"),
            Select.stateful(
              toAccountVar,
              toAccounts,
              idAttr := "to-account",
              cls := "w-full"
            )
          ),
          Item.of(
            _.variant(Item.Variant.Muted),
            _ => cls := "flex-col items-stretch",
            _ =>
              Item.content(
                cls := "gap-3",
                div(
                  cls := "flex items-center justify-between",
                  span(cls := "text-sm text-muted-foreground", "Estimated arrival"),
                  span(cls := "text-sm font-medium", "Today, Apr 14")
                ),
                Separator(),
                div(
                  cls := "flex items-center justify-between",
                  span(cls := "text-sm text-muted-foreground", "Transaction fee"),
                  span(cls := "text-sm font-medium tabular-nums", "$0.00")
                ),
                Separator(),
                div(
                  cls := "flex items-center justify-between",
                  span(cls := "text-sm font-medium", "Total amount"),
                  span(cls := "text-sm font-semibold tabular-nums", "$1,200.00")
                )
              )
          )
        )
      ),
      Card.footer(
        Button.of(
          _ => cls := "w-full",
          _ => "Confirm Transfer"
        )
      )
    )
