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

  observeAttribute("sections")(v => sectionsVar.set(ScAccordion.parseSections(v.orNull)))
  jsonProperty("sections")(v => sectionsVar.set(ScAccordion.parseSections(v)))

  mount(ScAccordion.view(sectionsVar, openIndexVar))

object ScAccordion:

  def register(): Unit =
    ScElements.define("sc-accordion", js.constructorOf[ScAccordion], "sections")

  // `children` must be built outside the ScElementBase/HTMLElement subclass:
  // HTMLElement itself declares a `children: HTMLCollection` member, which
  // would otherwise shadow Laminar's `children <-- signal` receiver.
  private def view(sectionsVar: Var[List[Accordion.Section]], openIndexVar: Var[Option[Int]]): HtmlElement =
    div(
      children <-- sectionsVar.signal.map(sections => List(Accordion(openIndexVar, sections*)))
    )

  private def parseSections(value: js.Any): List[Accordion.Section] =
    ScElements
      .toArray(value)
      .map(_.toList.map { raw =>
        Accordion.Section(
          title = raw.title.asInstanceOf[String],
          content = raw.content.asInstanceOf[String],
          disabled = raw.disabled.asInstanceOf[js.UndefOr[Boolean]].getOrElse(false)
        )
      })
      .getOrElse(Nil)
