package shadcnscalajs.site.create.preview02

import com.raquo.laminar.api.L.*
import shadcnscalajs.ui.*

/** preview-02 mosaic card — ported from
  * /Users/elam/Personal/shadcn-svelte/docs/src/lib/registry/examples/create/preview-02/cards/preferences.svelte
  */
object Preferences:

  private def closeAction: HtmlElement =
    Button.of(
      _.variant(Button.Variant.Ghost),
      _.size(Button.Size.IconSm),
      _ => cls := "bg-muted",
      _ => Icons.x()
    )

  private val currencies = List(
    ("usd", "USD — United States Dollar"),
    ("eur", "EUR — Euro"),
    ("gbp", "GBP — British Pound"),
    ("jpy", "JPY — Japanese Yen")
  )

  private def horizontalSwitch(
      labelText: String,
      inputId: String,
      descriptionText: String,
      checkedVar: Var[Boolean]
  ): HtmlElement =
    Field(
      Field.Orientation.Horizontal,
      Field.content(
        Field.label(labelText, forId := inputId),
        Field.description(descriptionText)
      ),
      Switch(checkedVar, idAttr := inputId)
    )

  def apply(): HtmlElement =
    val currencyVar = Var("usd")
    val publicStatsVar = Var(true)
    val emailNotificationsVar = Var(true)

    Card(
      dataAttr("card") := "preferences",
      Card.header(
        Card.title("Preferences"),
        Card.description("Manage your account settings and notifications."),
        Card.action(closeAction)
      ),
      Card.content(
        Field.group(
          Field(
            Field.label("Default Currency", forId := "default-currency"),
            Select.stateful(
              currencyVar,
              currencies,
              idAttr := "default-currency",
              cls := "w-full"
            )
          ),
          Field.separator(cls := "-my-4")(),
          horizontalSwitch(
            "Public Statistics",
            "public-statistics",
            "Allow others to see your total stream count and listening activity",
            publicStatsVar
          ),
          Field.separator(cls := "-my-4")(),
          horizontalSwitch(
            "Email Notifications",
            "email-notifications",
            "Monthly royalty reports and distribution updates",
            emailNotificationsVar
          )
        )
      ),
      Card.footer(
        Button.of(_.variant(Button.Variant.Outline), _ => "Reset"),
        Button.of(_ => cls := "ml-auto", _ => "Save Preferences")
      )
    )
