package shadcnscalajs.site.create.preview02

import com.raquo.laminar.api.L.*
import shadcnscalajs.ui.*

/** preview-02 mosaic card — ported from
  * /Users/elam/Personal/shadcn-svelte/docs/src/lib/registry/examples/create/preview-02/cards/receiving-method.svelte
  */
object ReceivingMethod:

  def apply(): HtmlElement =
    val methodVar = Var("bank")

    def methodRadio(method: String): HtmlElement =
      RadioGroup.item(
        "receiving-method",
        idAttr := s"method-$method",
        value := method,
        checked <-- methodVar.signal.map(_ == method),
        onChange.mapToValue --> methodVar.writer
      )

    Card(
      dataAttr("card") := "receiving-method",
      Card.header(
        Card.description("Payout Preferences"),
        Card.title("Receiving Method"),
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
            Field.label("Account Holder Name", forId := "account-holder"),
            Input(idAttr := "account-holder", value := "Synthetic Horizons Music LLC")
          ),
          Field.set(
            Field.legend(Field.LegendVariant.Label, "Receiving Method"),
            RadioGroup(
              cls := "grid grid-cols-1 items-start gap-3 md:grid-cols-2",
              Field.label(
                "",
                forId := "method-bank",
                Field(
                  Field.Orientation.Horizontal,
                  cls := "pb-2.5",
                  methodRadio("bank"),
                  Field.content(
                    Field.description(cls := "font-medium text-foreground", "Bank Transfer"),
                    Field.description("SWIFT / IBAN")
                  )
                )
              ),
              Field.label(
                "",
                forId := "method-paypal",
                Field(
                  Field.Orientation.Horizontal,
                  cls := "pb-2.5",
                  methodRadio("paypal"),
                  Field.content(
                    Field.description(cls := "font-medium text-foreground", "PayPal"),
                    Field.description(cls := "line-clamp-1", "Instant Payout")
                  )
                )
              )
            )
          ),
          Field(
            Field.label("IBAN / Account Number", forId := "iban"),
            Input(idAttr := "iban", placeholder := "DE89 3704 0044 ....")
          )
        )
      ),
      Card.footer(
        Button.of(
          _ => cls := "w-full",
          _ => disabled := true,
          _ => "Save Payout Settings"
        )
      )
    )
