package shadcnscalajs.site.create.preview02

import com.raquo.laminar.api.L.*
import shadcnscalajs.ui.*

/** preview-02 mosaic card — port of
  * `/Users/elam/Personal/shadcn-svelte/docs/src/lib/registry/examples/create/preview-02/cards/recent-transactions.svelte`
  */
object RecentTransactions:

  private final case class Tx(
      name: String,
      category: String,
      date: String,
      amount: String,
      positive: Boolean,
      icon: SvgElement
  )

  private val transactions: List[Tx] = List(
    Tx(
      "Blue Bottle Coffee",
      "Food & Drink",
      "Today, 10:24 AM",
      "-$6.50",
      positive = false,
      Icons.coffee()
    ),
    Tx(
      "Whole Foods Market",
      "Groceries",
      "Yesterday",
      "-$142.30",
      positive = false,
      Icons.shoppingCart()
    ),
    Tx(
      "Stripe Payout",
      "Income",
      "Oct 12",
      "+$4,200.00",
      positive = true,
      Icons.wallet()
    ),
    Tx(
      "Uber Technologies",
      "Transport",
      "Oct 11",
      "-$24.10",
      positive = false,
      Icons.car()
    ),
    Tx(
      "Netflix Subscription",
      "Entertainment",
      "Oct 10",
      "-$19.99",
      positive = false,
      Icons.tv()
    )
  )

  private val ghostIconTrigger: Modifier[HtmlElement] =
    cls := "size-8 border-0 bg-transparent p-0 shadow-none hover:bg-accent hover:text-accent-foreground"

  private def rowMenu: HtmlElement =
    DropdownMenu(ghostIconTrigger, Icons.moreHorizontal())(
      DropdownMenu.Item("View details", () => ()),
      DropdownMenu.Item("Add note", () => ()),
      DropdownMenu.Item("Categorize", () => ()),
      DropdownMenu.Item("Dispute", () => ())
    )

  private def transactionRow(tx: Tx): HtmlElement =
    Table.row(
      Table.cell(
        cls := "w-10",
        div(
          cls := "flex size-10 items-center justify-center rounded-lg bg-muted [&_svg]:size-4 [&_svg]:shrink-0",
          tx.icon
        )
      ),
      Table.cell(
        div(
          cls := "flex flex-col",
          span(cls := "font-medium", tx.name),
          span(cls := "text-sm text-muted-foreground", tx.category)
        )
      ),
      Table.cell(cls := "text-sm text-muted-foreground", tx.date),
      Table.cell(
        cls := "text-right",
        span(
          cls := s"text-sm font-semibold tabular-nums${if tx.positive then " text-emerald-500" else ""}",
          tx.amount
        )
      ),
      Table.cell(cls := "w-8", rowMenu)
    )

  def apply(): HtmlElement =
    Card(
      dataAttr("card") := "recent-transactions",
      Card.header(
        Card.title("Recent Transactions"),
        Card.description("Your latest account activity."),
        Card.action(Button.of(_.variant(Button.Variant.Outline), _.size(Button.Size.Sm), _ => "View All"))
      ),
      Card.content(Table(Table.body(transactions.map(transactionRow).toList)))
    )
