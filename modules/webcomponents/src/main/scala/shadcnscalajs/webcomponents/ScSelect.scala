package shadcnscalajs.webcomponents

import com.raquo.laminar.api.L.*
import org.scalajs.dom
import shadcnscalajs.ui.Select

import scala.scalajs.js

/** `<sc-select options='[{"value":"pro","label":"Pro"}]' value="pro" placeholder="Choose a plan"></sc-select>` — Web
  * Component export of shadcnscalajs.ui.Select.
  *
  * Options come in as a JSON attribute because attributes are plain strings, the same approach `sc-dropdown-menu`
  * takes. Light-DOM `<option>` children are not projected: the Laminar component is a listbox of `role="option"` divs,
  * not a native `<select>`, so there is nowhere for them to go. Use `<sc-native-select>` when real `<option>` markup
  * matters. Choosing an option fires a `change` CustomEvent whose `detail` is the new value.
  */
class ScSelect extends ScElementBase:

  private val optionsVar = Var(List.empty[(String, String)])
  private val selectedVar = Var("")
  private val placeholderVar = Var("Select…")

  observeAttribute("options")(v => optionsVar.set(v.flatMap(parseOptions).getOrElse(Nil)))
  observeAttribute("value")(v => selectedVar.set(v.getOrElse("")))
  observeAttribute("placeholder")(v => placeholderVar.set(v.getOrElse("Select…")))

  selectedVar.signal.changes.foreach { value =>
    this.dispatchEvent(
      new dom.CustomEvent("change", js.Dynamic.literal(detail = value).asInstanceOf[dom.CustomEventInit])
    )
  }(unsafeWindowOwner)

  mount(ScSelect.view(optionsVar, selectedVar, placeholderVar))

  private def parseOptions(json: String): Option[List[(String, String)]] =
    try
      val parsed = js.JSON.parse(json).asInstanceOf[js.Array[js.Dynamic]]
      Some(parsed.toList.map { raw =>
        val optValue = raw.value.asInstanceOf[String]
        val optLabel = raw.label.asInstanceOf[js.UndefOr[String]].getOrElse(optValue)
        optValue -> optLabel
      })
    catch case _: Throwable => None

object ScSelect:
  def register(): Unit =
    dom.window.customElements.define("sc-select", js.constructorOf[ScSelect])

  // Built outside the ScElementBase subclass for the same reason as ScDropdownMenu: HTMLElement's own `children` member
  // would shadow Laminar's `children <-- signal` receiver.
  private def view(
      optionsVar: Var[List[(String, String)]],
      selectedVar: Var[String],
      placeholderVar: Var[String]
  ): HtmlElement =
    div(
      children <-- optionsVar.signal
        .combineWith(placeholderVar.signal)
        .map((options, placeholder) =>
          List(Select(selectedVar, placeholder)(ctx => Seq(ctx.group(options.map((v, l) => ctx.item(v, l))))))
        )
    )
