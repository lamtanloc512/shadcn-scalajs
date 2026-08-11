package shadcnscalajs.webcomponents

import com.raquo.laminar.api.L.*
import shadcnscalajs.ui.{Field, Radio, RadioGroup}

import scala.scalajs.js

class ScRadioGroup extends ScElementBase:

  private val nameVar = Var("sc-radio")
  private val valueVar = Var("")
  private val itemsVar = Var(List.empty[ScRadioGroup.Item])
  private val revision = Var(0)

  private def bump(): Unit = revision.update(_ + 1)

  private val echo = EchoGuard[String]()

  observeAttribute("name")(v => { nameVar.set(v.getOrElse("sc-radio")); bump() })
  observeAttribute("value")(v => { echo.wrote(v.getOrElse("")); valueVar.set(v.getOrElse("")) })
  observeAttribute("items")(v => { itemsVar.set(parseItems(v.orNull)); bump() })
  stringProperty("name")
  stringProperty("value")
  jsonProperty("items")(v => { itemsVar.set(parseItems(v)); bump() })

  valueVar.signal.changes.foreach(value => if !echo.isEcho(value) then emit("sc-change", value))(unsafeWindowOwner)

  mount(ScRadioGroup.view(nameVar, valueVar, itemsVar, revision))

  private def parseItems(value: js.Any): List[ScRadioGroup.Item] =
    ScElements
      .toArray(value)
      .map(_.toList.map { raw =>
        ScRadioGroup.Item(
          value = raw.value.asInstanceOf[String],
          label = raw.label.asInstanceOf[js.UndefOr[String]].getOrElse(raw.value.asInstanceOf[String]),
          description = raw.description.asInstanceOf[js.UndefOr[String]].toOption
        )
      })
      .getOrElse(Nil)

object ScRadioGroup:
  final case class Item(value: String, label: String, description: Option[String])

  def register(): Unit =
    ScElements.define("sc-radio-group", js.constructorOf[ScRadioGroup], "name", "value", "items")

  private def view(
      nameVar: Var[String],
      valueVar: Var[String],
      itemsVar: Var[List[Item]],
      revision: Var[Int]
  ): HtmlElement =
    div(
      children <-- revision.signal.map { _ =>
        val name = nameVar.now()
        val items = itemsVar.now()
        val radios: List[Modifier[HtmlElement]] = (cls := "grid grid-cols-1 items-start gap-3") :: items.map { item =>
          Field.label(
            "",
            forId := s"$name-${item.value}",
            Field(
              Field.Orientation.Horizontal,
              cls := "pb-2.5",
              Radio(
                name,
                idAttr := s"$name-${item.value}",
                value := item.value,
                checked <-- valueVar.signal.map(_ == item.value),
                onChange.mapToValue --> valueVar.writer
              ),
              Field.content(
                Field.description(cls := "font-medium text-foreground", item.label),
                item.description.map(d => Field.description(d)).getOrElse(emptyNode)
              )
            )
          )
        }
        List(RadioGroup(radios*))
      }
    )
