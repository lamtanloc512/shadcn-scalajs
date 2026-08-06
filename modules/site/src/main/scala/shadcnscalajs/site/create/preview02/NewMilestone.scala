package shadcnscalajs.site.create.preview02

import com.raquo.laminar.api.L.*
import shadcnscalajs.ui.*

/** Port of shadcn-svelte preview-02 `cards/new-milestone.svelte`. */
object NewMilestone:
  def apply(): HtmlElement =
    Card(
      dataAttr("card") := "new-milestone",
      Card.header(
        Card.title("Set a new milestone"),
        Card.description("Define your financial target and we'll help you pace your savings.")
      ),
      Card.content(
        Field.group(
          Field(
            Field.label("Goal Name", forId := "goal-name"),
            Input(idAttr := "goal-name", placeholder := "e.g. New Car, Home Downpayment")
          ),
          div(
            cls := "grid grid-cols-2 gap-3",
            Field(
              Field.label("Target Amount", forId := "target-amount"),
              Input(idAttr := "target-amount", value := "$15,000")
            ),
            Field(
              Field.label("Target Date", forId := "target-date"),
              Input(idAttr := "target-date", value := "Dec 2025")
            )
          )
        )
      ),
      Card.footer(
        cls := "flex-col gap-2",
        Button.of(_ => cls := "w-full", _ => "Create Goal"),
        Button.of(_.variant(Button.Variant.Outline), _ => cls := "w-full", _ => "Cancel")
      )
    )
