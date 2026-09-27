package shadcnscalajs.webcomponents

import com.raquo.laminar.api.L.*
import shadcnscalajs.core.Tags.slotTag
import shadcnscalajs.ui.Tabs

import scala.scalajs.js

class ScTabs extends ScElementBase:

  private val valueVar = Var("")
  private val itemsVar = Var(List.empty[(String, String)])
  private val revision = Var(0)

  private def bump(): Unit = revision.update(_ + 1)

  private val echo = EchoGuard[String]()

  observeAttribute("value")(v => { echo.wrote(v.getOrElse("")); valueVar.set(v.getOrElse("")) })
  observeAttribute("items")(v => { itemsVar.set(parseItems(v.orNull)); bump() })
  stringProperty("value")
  jsonProperty("items")(v => { itemsVar.set(parseItems(v)); bump() })

  mount(
    ScTabs
      .view(valueVar, itemsVar, revision)
      .amend(
        valueVar.signal.changes --> Observer[String](value => if !echo.isEcho(value) then emit("sc-change", value))
      )
  )

  private def parseItems(value: js.Any): List[(String, String)] =
    ScElements
      .toArray(value)
      .map(_.toList.map { raw =>
        val itemValue = raw.value.asInstanceOf[String]
        itemValue -> raw.label.asInstanceOf[js.UndefOr[String]].getOrElse(itemValue)
      })
      .getOrElse(Nil)

object ScTabs:
  def register(): Unit =
    ScElements.define("sc-tabs", js.constructorOf[ScTabs], "value", "items")

  private def view(valueVar: Var[String], itemsVar: Var[List[(String, String)]], revision: Var[Int]): HtmlElement =
    div(
      children <-- revision.signal.map { _ =>
        val items = itemsVar.now()
        if items.isEmpty then Nil
        else
          if valueVar.now().isEmpty then valueVar.set(items.head._1)
          val tabs = items.map { case (value, label) =>
            Tabs.Tab(value, label, div(slotTag(nameAttr := s"panel-$value")))
          }
          List(Tabs.stateful(valueVar, Tabs.ListVariant.Default)(tabs*))
      }
    )
