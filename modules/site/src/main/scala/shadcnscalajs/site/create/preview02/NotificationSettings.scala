package shadcnscalajs.site.create.preview02

import com.raquo.laminar.api.L.*
import shadcnscalajs.ui.*

/** preview-02 mosaic card — port from notification-settings.svelte */
object NotificationSettings:

  private val defaultChecked: Map[String, Boolean] = Map(
    "transactions" -> true,
    "security" -> true,
    "goals" -> false,
    "market" -> false
  )

  def apply(): HtmlElement =
    val checkedMap: Var[Map[String, Boolean]] = Var(defaultChecked)
    val masterChecked: Var[Boolean] = Var(defaultChecked.values.forall(_ == true))

    def syncedBoolVar(key: String): Var[Boolean] =
      val valueVar: Var[Boolean] = Var(checkedMap.now()(key))
      checkedMap.signal --> { (values: Map[String, Boolean]) => valueVar.set(values(key)) }
      valueVar.signal --> { (checked: Boolean) => checkedMap.update(_.updated(key, checked)) }
      valueVar

    val transactionsChecked = syncedBoolVar("transactions")
    val securityChecked = syncedBoolVar("security")
    val goalsChecked = syncedBoolVar("goals")
    val marketChecked = syncedBoolVar("market")

    val allChecked = checkedMap.signal.map((values: Map[String, Boolean]) => values.values.forall(_ == true))
    val someChecked = checkedMap.signal.map { (values: Map[String, Boolean]) =>
      val checked: List[Boolean] = values.values.toList
      checked.exists(_ == true) && !checked.forall(_ == true)
    }

    allChecked --> masterChecked

    def setAll(value: Boolean): Unit =
      checkedMap.set(defaultChecked.keys.map(_ -> value).toMap)

    Card(
      dataAttr("card") := "notification-settings",
      Card.header(
        Card.title("Notifications"),
        Card.description("Choose what you want to be notified about.")
      ),
      Card.content(
        Field.group(
          Field(
            Field.Orientation.Horizontal,
            Checkbox(
              masterChecked,
              someChecked,
              idAttr := "notify-all",
              onClick --> { _ =>
                val all = checkedMap.now().values.forall(_ == true)
                setAll(!all)
              }
            ),
            Field.content(
              Field.label("Select all", forId := "notify-all")
            )
          ),
          Field(
            Field.Orientation.Horizontal,
            Checkbox(transactionsChecked, idAttr := "notify-transactions"),
            Field.content(
              Field.label("Transaction alerts", forId := "notify-transactions"),
              Field.description("Deposits, withdrawals, and transfers.")
            )
          ),
          Field(
            Field.Orientation.Horizontal,
            Checkbox(securityChecked, idAttr := "notify-security"),
            Field.content(
              Field.label("Security alerts", forId := "notify-security"),
              Field.description("Login attempts and account changes.")
            )
          ),
          Field(
            Field.Orientation.Horizontal,
            Checkbox(goalsChecked, idAttr := "notify-goals"),
            Field.content(
              Field.label("Goal milestones", forId := "notify-goals"),
              Field.description("Updates at 25%, 50%, 75%, and 100%.")
            )
          ),
          Field(
            Field.Orientation.Horizontal,
            Checkbox(marketChecked, idAttr := "notify-market"),
            Field.content(
              Field.label("Market updates", forId := "notify-market"),
              Field.description("Daily portfolio summary and price alerts.")
            )
          )
        )
      ),
      Card.footer(
        Button.of(
          _ => cls := "w-full",
          _ => "Save Preferences"
        )
      )
    )
