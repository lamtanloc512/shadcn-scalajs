package shadcnscalajs.webcomponents

import com.raquo.laminar.api.L.*
import org.scalajs.dom
import shadcnscalajs.core.Tags.slotTag
import shadcnscalajs.ui.DropdownMenu

import scala.scalajs.js

/** `<sc-dropdown-menu items='[{"label":"Profile"},{"label":"Log out"}]'>
  *   <button slot="trigger">Open</button>
  * </sc-dropdown-menu>` — Web Component export of shadcnscalajs.ui.DropdownMenu.
  * The trigger is the element's light-DOM children (projected via the default
  * slot); items are a JSON attribute since attributes are plain strings.
  * Selecting an item fires a `select` CustomEvent with `detail: <index>`.
  */
class ScDropdownMenu extends ScElementBase:

  private val itemsVar = Var(List.empty[DropdownMenu.Item])

  observeAttribute("items")(v => itemsVar.set(v.flatMap(parseItems).getOrElse(Nil)))

  mount(ScDropdownMenu.view(itemsVar))

  private def parseItems(json: String): Option[List[DropdownMenu.Item]] =
    try
      val parsed = js.JSON.parse(json).asInstanceOf[js.Array[js.Dynamic]]
      Some(parsed.toList.zipWithIndex.map { case (raw, idx) =>
        val label = raw.label.asInstanceOf[String]
        val disabled = raw.disabled.asInstanceOf[js.UndefOr[Boolean]].getOrElse(false)
        DropdownMenu.Item(
          label = label,
          onSelect = () => this.dispatchEvent(new dom.CustomEvent("select", js.Dynamic.literal(detail = idx).asInstanceOf[dom.CustomEventInit])),
          disabled = disabled
        )
      })
    catch case _: Throwable => None

object ScDropdownMenu:
  def register(): Unit =
    dom.window.customElements.define("sc-dropdown-menu", js.constructorOf[ScDropdownMenu])

  // Built outside the ScElementBase/HTMLElement subclass: HTMLElement itself
  // declares a `children: HTMLCollection` member, which would otherwise
  // shadow Laminar's `children <-- signal` receiver.
  private def view(itemsVar: Var[List[DropdownMenu.Item]]): HtmlElement =
    div(
      children <-- itemsVar.signal.map(items => List(DropdownMenu(slotTag())(items*)))
    )
