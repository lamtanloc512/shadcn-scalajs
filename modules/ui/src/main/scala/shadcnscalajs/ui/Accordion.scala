package shadcnscalajs.ui

import com.raquo.laminar.api.L.*
import shadcnscalajs.core.CommonAttrs.openAttr

/** Basecoat-compatible Accordion built from native `<details>` / `<summary>` elements.
  *
  * `apply` provides the default single-item behavior. Use [[multiple]] when more than one item may be expanded.
  */
object Accordion:

  final case class Section(title: Modifier[HtmlElement], content: Modifier[HtmlElement], disabled: Boolean = false)

  def apply(openIndexVar: Var[Option[Int]], sections: Section*): HtmlElement =
    sectionTag(
      cls := "accordion",
      sections.zipWithIndex.map { case (sec, idx) => renderSingleSection(openIndexVar, sec, idx) }.toList
    )

  def multiple(openIndexesVar: Var[Set[Int]], sections: Section*): HtmlElement =
    sectionTag(
      cls := "accordion",
      dataAttr("multiple") := "",
      sections.zipWithIndex.map { case (sec, idx) => renderMultipleSection(openIndexesVar, sec, idx) }.toList
    )

  private def renderSingleSection(openIndexVar: Var[Option[Int]], sec: Section, idx: Int): HtmlElement =
    detailsTag(
      openAttr <-- openIndexVar.signal.map(_.contains(idx)),
      cls := "cn-accordion-item",
      aria.disabled := sec.disabled,
      trigger(
        sec,
        () =>
          openIndexVar.update {
            case Some(`idx`) => None
            case _           => Some(idx)
          }
      ),
      sectionTag(cls := "cn-accordion-content cn-accordion-content-inner", sec.content)
    )

  private def renderMultipleSection(openIndexesVar: Var[Set[Int]], sec: Section, idx: Int): HtmlElement =
    detailsTag(
      openAttr <-- openIndexesVar.signal.map(_.contains(idx)),
      cls := "cn-accordion-item",
      aria.disabled := sec.disabled,
      trigger(
        sec,
        () =>
          openIndexesVar.update { indexes =>
            if indexes.contains(idx) then indexes - idx else indexes + idx
          }
      ),
      sectionTag(cls := "cn-accordion-content cn-accordion-content-inner", sec.content)
    )

  private def trigger(sec: Section, toggle: () => Unit): HtmlElement =
    summaryTag(
      cls := "cn-accordion-trigger",
      aria.disabled := sec.disabled,
      onClick --> { ev =>
        ev.preventDefault()
        if !sec.disabled then toggle()
      },
      sec.title,
      Icons.chevronDown()
    )
