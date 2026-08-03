package shadcnscalajs.webcomponents

import com.raquo.laminar.api.L.*
import org.scalajs.dom
import shadcnscalajs.ui.Accordion

import scala.scalajs.js

/** `<sc-accordion sections='[{"title":"Q1","content":"A1"}]'></sc-accordion>` — Web Component export of
  * shadcnscalajs.ui.Accordion. Sections are plain text title/content pairs passed as a JSON attribute, since
  * custom-element attributes are strings; the native Laminar `Accordion` (shadcnscalajs.ui) still accepts arbitrary
  * rich `Modifier` content for direct Scala.js use.
  */
class ScAccordion extends ScElementBase:

  private val sectionsVar = Var(List.empty[Accordion.Section])
  private val openIndexVar = Var(Option.empty[Int])

  observeAttribute("sections")(v => sectionsVar.set(v.flatMap(ScAccordion.parseSections).getOrElse(Nil)))

  mount(ScAccordion.view(sectionsVar, openIndexVar))

object ScAccordion:

  def register(): Unit =
    dom.window.customElements.define("sc-accordion", js.constructorOf[ScAccordion])

  // `children` must be built outside the ScElementBase/HTMLElement subclass:
  // HTMLElement itself declares a `children: HTMLCollection` member, which
  // would otherwise shadow Laminar's `children <-- signal` receiver.
  private def view(sectionsVar: Var[List[Accordion.Section]], openIndexVar: Var[Option[Int]]): HtmlElement =
    div(
      children <-- sectionsVar.signal.map(sections => List(Accordion(openIndexVar, sections*)))
    )

  private def parseSections(json: String): Option[List[Accordion.Section]] =
    try
      val parsed = js.JSON.parse(json).asInstanceOf[js.Array[js.Dynamic]]
      Some(parsed.toList.map { raw =>
        val title = raw.title.asInstanceOf[String]
        val content = raw.content.asInstanceOf[String]
        val disabled = raw.disabled.asInstanceOf[js.UndefOr[Boolean]].getOrElse(false)
        Accordion.Section(title = title, content = content, disabled = disabled)
      })
    catch case _: Throwable => None
