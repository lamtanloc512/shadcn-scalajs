package shadcnscalajs.webcomponents

import com.raquo.laminar.api.L.*
import org.scalajs.dom
import shadcnscalajs.ui.Combobox

import scala.scalajs.js

/** `<sc-combobox items='[{"value":"next.js","label":"Next.js"}]' placeholder="Select…"></sc-combobox>` — Web Component
  * export of shadcnscalajs.ui.Combobox. Items are a JSON attribute since custom-element attributes are plain strings.
  * Selecting (or deselecting) an item fires a `change` CustomEvent with `detail: <value or null>`.
  */
class ScCombobox extends ScElementBase:

  private val itemsVar = Var(List.empty[Combobox.Item])
  private val selectedVar = Var(Option.empty[String])
  private val placeholderVar = Var("Select option…")
  private val searchPlaceholderVar = Var("Search…")
  private val emptyTextVar = Var("No results found.")

  observeAttribute("items")(v => itemsVar.set(ScCombobox.parseItems(v.orNull)))
  observeAttribute("placeholder")(v => v.foreach(placeholderVar.set))
  observeAttribute("search-placeholder")(v => v.foreach(searchPlaceholderVar.set))
  observeAttribute("empty-text")(v => v.foreach(emptyTextVar.set))
  jsonProperty("items")(v => itemsVar.set(ScCombobox.parseItems(v)))
  stringProperty("placeholder")
  stringProperty("search-placeholder")
  stringProperty("empty-text")

  mount(
    ScCombobox.view(
      itemsVar,
      selectedVar,
      placeholderVar,
      searchPlaceholderVar,
      emptyTextVar,
      value => emit("sc-change", value.orNull)
    )
  )

object ScCombobox:

  def register(): Unit =
    ScElements.define(
      "sc-combobox",
      js.constructorOf[ScCombobox],
      "items",
      "placeholder",
      "search-placeholder",
      "empty-text"
    )

  // Built outside the ScElementBase/HTMLElement subclass: HTMLElement itself
  // declares a `children: HTMLCollection` member, which would otherwise
  // shadow Laminar's `children <-- signal` receiver.
  private def view(
      itemsVar: Var[List[Combobox.Item]],
      selectedVar: Var[Option[String]],
      placeholderVar: Var[String],
      searchPlaceholderVar: Var[String],
      emptyTextVar: Var[String],
      onChange: Option[String] => Unit
  ): HtmlElement =
    div(
      onMountBind { _ => selectedVar.signal --> onChange },
      children <-- itemsVar.signal
        .combineWith(placeholderVar.signal, searchPlaceholderVar.signal, emptyTextVar.signal)
        .map { case (items, placeholder, searchPlaceholder, emptyText) =>
          List(Combobox(selectedVar, items, placeholder, searchPlaceholder, emptyText))
        }
    )

  private def parseItems(value: js.Any): List[Combobox.Item] =
    ScElements.toArray(value).map(_.toList.map { raw =>
      val v = raw.value.asInstanceOf[String]
      Combobox.Item(value = v, label = raw.label.asInstanceOf[js.UndefOr[String]].getOrElse(v))
    }).getOrElse(Nil)
