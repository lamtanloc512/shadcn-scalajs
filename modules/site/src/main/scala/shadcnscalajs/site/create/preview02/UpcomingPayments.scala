package shadcnscalajs.site.create.preview02

import com.raquo.laminar.api.L.*
import shadcnscalajs.ui.*

import scala.scalajs.js

/** preview-02 mosaic card — ported from
  * /Users/elam/Personal/shadcn-svelte/docs/src/lib/registry/examples/create/preview-02/cards/upcoming-payments.svelte
  */
object UpcomingPayments:

  private final case class Payment(title: String, date: String, amount: String)

  private val payments = List(
    Payment("Netflix Subscription", "Apr 15, 2024", "$19.99"),
    Payment("Rent Payment", "Apr 1, 2024", "$2,400.00"),
    Payment("Auto Insurance", "Apr 22, 2024", "$186.00")
  )

  private def paymentRow(payment: Payment): HtmlElement =
    Item.of(
      _.variant(Item.Variant.Muted),
      _ =>
        Item.content(
          Item.title(payment.title),
          Item.description(payment.date)
        ),
      _ => Badge.of(_.variant(Badge.Variant.Secondary), _ => payment.amount)
    )

  def apply(): HtmlElement =
    val selectedDate = Var(Option(new js.Date()))

    Card(
      dataAttr("card") := "upcoming-payments",
      Card.header(
        Card.title("Upcoming Payments"),
        Card.description("Select a date to view scheduled payments.")
      ),
      Card.content(
        cls := "flex flex-col gap-4",
        Item.of(
          _.variant(Item.Variant.Outline),
          _ => cls := "justify-center",
          _ =>
            Calendar(
              selectedDate,
              cls := "w-full [--cell-size:--spacing(8)] md:[--cell-size:--spacing(10)]"
            )
        ),
        Item.group(
          cls := "w-full",
          paymentRow(payments(0)),
          paymentRow(payments(1)),
          paymentRow(payments(2))
        )
      )
    )
