package shadcnscalajs.webcomponents

import com.raquo.laminar.api.L.*
import shadcnscalajs.core.Tags.slotTag
import shadcnscalajs.ui.ContextMenu

import scala.scalajs.js

/** Right-click menu with a light-DOM trigger in the named `trigger` slot. */
class ScContextMenu extends ScElementBase:

  private val itemsVar = Var(List.empty[ContextMenu.Item])

  observeAttribute("items")(v => itemsVar.set(parseItems(v.orNull)))
  jsonProperty("items")(v => itemsVar.set(parseItems(v)))

  mount(ScContextMenu.view(itemsVar))

  private def parseItems(value: js.Any): List[ContextMenu.Item] =
    ScElements
      .toArray(value)
      .map(_.toList.zipWithIndex.map { case (raw, idx) =>
        ContextMenu.Item(
          label = raw.label.asInstanceOf[String],
          onSelect = () => emit("sc-select", idx),
          disabled = raw.disabled.asInstanceOf[js.UndefOr[Boolean]].getOrElse(false)
        )
      })
      .getOrElse(Nil)

object ScContextMenu:
  def register(): Unit =
    ScElements.define("sc-context-menu", js.constructorOf[ScContextMenu], "items")

  private def view(itemsVar: Var[List[ContextMenu.Item]]): HtmlElement =
    div(
      children <-- itemsVar.signal.map { items =>
        List(
          ContextMenu.trigger(ctx =>
            items.map { item =>
              val disabled =
                if item.disabled then Seq[Modifier[HtmlElement]](aria.disabled := true, dataAttr("disabled") := "")
                else Seq.empty
              ctx.item(item.onSelect, disabled, item.label)
            }
          )(slotTag(nameAttr := "trigger"))
        )
      }
    )
