package shadcnscalajs.webcomponents

import com.raquo.laminar.api.L.*
import org.scalajs.dom
import shadcnscalajs.ui.DropdownMenu

import scala.scalajs.js

/** `<sc-dropdown-menu items='[{"label":"Profile"},{"label":"Log out"}]'> <button slot="trigger">Open</button>
  * </sc-dropdown-menu>` — Web Component export of shadcnscalajs.ui.DropdownMenu. The trigger is the element's light-DOM
  * children (projected via the default slot); items are a JSON attribute since attributes are plain strings. Selecting
  * an item fires a `select` CustomEvent with `detail: <index>`.
  */
class ScDropdownMenu extends ScElementBase:

  private val itemsVar = Var(List.empty[DropdownMenu.Item])

  observeAttribute("items")(v => itemsVar.set(parseItems(v.orNull)))
  jsonProperty("items")(v => itemsVar.set(parseItems(v)))

  mount(ScDropdownMenu.view(itemsVar))

  private def parseItems(value: js.Any): List[DropdownMenu.Item] =
    ScElements
      .toArray(value)
      .map(_.toList.zipWithIndex.map { case (raw, idx) =>
        DropdownMenu.Item(
          label = raw.label.asInstanceOf[String],
          onSelect = () => emit("sc-select", idx),
          disabled = raw.disabled.asInstanceOf[js.UndefOr[Boolean]].getOrElse(false)
        )
      })
      .getOrElse(Nil)

object ScDropdownMenu:
  def register(): Unit =
    ScElements.define("sc-dropdown-menu", js.constructorOf[ScDropdownMenu], "items")

  // Built outside the ScElementBase/HTMLElement subclass: HTMLElement itself
  // declares a `children: HTMLCollection` member, which would otherwise
  // shadow Laminar's `children <-- signal` receiver.
  private def view(itemsVar: Var[List[DropdownMenu.Item]]): HtmlElement =
    div(
      // The trigger comes in through a named slot, matching the documented
      // `<button slot="trigger">` usage — a default `<slot>` only accepts light-DOM children with no slot attribute.
      children <-- itemsVar.signal.map { items =>
        List(
          DropdownMenu.slottedItems() { ctx =>
            items.map { item =>
              val disabled =
                if item.disabled then Seq[Modifier[HtmlElement]](aria.disabled := true, dataAttr("disabled") := "")
                else Seq.empty
              item.checked match
                case Some(checked) => ctx.checkboxItem(checked, item.onSelect, disabled, item.label)
                case None          => ctx.item(item.onSelect, disabled, item.label)
            }
          }
        )
      }
    )
