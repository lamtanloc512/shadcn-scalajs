package shadcnscalajs.site

import com.raquo.laminar.api.L.*

/** One named demo under a component docs page's "Examples" heading, mirroring how upstream lists several focused
  * recipes below the main preview rather than a single "Composition" card.
  *
  * `code` is the Scala.js source shown in the example's Code tab. It is written by hand rather than derived from
  * `preview`, because the demo carries wiring (ids, docs-only parsing helpers) that would only distract from the recipe
  * a reader is here to copy.
  */
final case class DocExample(
    anchor: String,
    title: String,
    description: Option[String],
    preview: HtmlElement,
    code: String
)

object ComponentExamples:

  /** Focused recipes mirror every additional `ComponentPreview` after a page's primary demo in the local shadcn-svelte
    * component markdown.
    */
  def apply(componentName: String): Seq[DocExample] =
    if componentName == "date-picker" then DatePickerExamples.all
    else
      ComponentExamplesA(componentName) ++
        ComponentExamplesB(componentName) ++
        ComponentExamplesC(componentName) ++
        ComponentExamplesD(componentName)
