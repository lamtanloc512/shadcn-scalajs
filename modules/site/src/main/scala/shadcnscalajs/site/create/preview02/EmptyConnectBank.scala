package shadcnscalajs.site.create.preview02

import com.raquo.laminar.api.L.*
import shadcnscalajs.ui.*

/** preview-02 mosaic card — port from empty-connect-bank.svelte */
object EmptyConnectBank:
  def apply(): HtmlElement =
    Card(
      dataAttr("card") := "empty-connect-bank",
      Card.content(
        Empty(
          cls := "p-4",
          Empty.media(Empty.MediaVariant.Icon, Icons.creditCard()),
          Empty.header(
            Empty.title("Connect Bank"),
            Empty.description(
              "Link your payout method to receive monthly royalty distributions automatically."
            )
          ),
          Empty.content(
            Button.of(
              _.variant(Button.Variant.Primary),
              _.size(Button.Size.Default),
              _ => "Set Up Payouts"
            )
          )
        )
      )
    )
