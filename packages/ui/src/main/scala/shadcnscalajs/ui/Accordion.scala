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
      dataAttr("slot") := "accordion",
      cls := "cn-accordion flex w-full flex-col",
      sections.zipWithIndex.map { case (sec, idx) => renderSingleSection(openIndexVar, sec, idx) }.toList
    )

  def multiple(openIndexesVar: Var[Set[Int]], sections: Section*): HtmlElement =
    sectionTag(
      dataAttr("slot") := "accordion",
      cls := "cn-accordion flex w-full flex-col",
      dataAttr("multiple") := "",
      sections.zipWithIndex.map { case (sec, idx) => renderMultipleSection(openIndexesVar, sec, idx) }.toList
    )

  private def renderSingleSection(openIndexVar: Var[Option[Int]], sec: Section, idx: Int): HtmlElement =
    detailsTag(
      dataAttr("slot") := "accordion-item",
      openAttr <-- openIndexVar.signal.map(_.contains(idx)),
      cls := "cn-accordion-item",
      aria.disabled := sec.disabled,
      trigger(
        sec,
        openIndexVar.signal.map(_.contains(idx)),
        () =>
          openIndexVar.update {
            case Some(`idx`) => None
            case _           => Some(idx)
          }
      ),
      content(sec.content)
    )

  private def renderMultipleSection(openIndexesVar: Var[Set[Int]], sec: Section, idx: Int): HtmlElement =
    detailsTag(
      dataAttr("slot") := "accordion-item",
      openAttr <-- openIndexesVar.signal.map(_.contains(idx)),
      cls := "cn-accordion-item",
      aria.disabled := sec.disabled,
      trigger(
        sec,
        openIndexesVar.signal.map(_.contains(idx)),
        () =>
          openIndexesVar.update { indexes =>
            if indexes.contains(idx) then indexes - idx else indexes + idx
          }
      ),
      content(sec.content)
    )

  private def content(body: Modifier[HtmlElement]): HtmlElement =
    div(
      dataAttr("slot") := "accordion-content",
      cls := "cn-accordion-content overflow-hidden",
      div(
        cls := "cn-accordion-content-inner [&_a]:underline [&_a]:underline-offset-3 [&_a]:hover:text-foreground [&_p:not(:last-child)]:mb-4",
        body
      )
    )

  private def trigger(sec: Section, expanded: Signal[Boolean], toggle: () => Unit): HtmlElement =
    summaryTag(
      dataAttr("slot") := "accordion-trigger",
      cls := "cn-accordion-trigger group/accordion-trigger relative flex flex-1 items-start justify-between border border-transparent transition-all outline-none disabled:pointer-events-none disabled:opacity-50",
      aria.disabled := sec.disabled,
      aria.expanded <-- expanded,
      onClick --> { ev =>
        ev.preventDefault()
        if !sec.disabled then toggle()
      },
      sec.title,
      Icons.chevronDown(
        Icons.svgSlot := "accordion-trigger-icon",
        svg.cls := "cn-accordion-trigger-icon pointer-events-none shrink-0 group-aria-expanded/accordion-trigger:hidden"
      ),
      // Icons has no chevronUp — rotate chevronDown for the expanded affordance.
      Icons.chevronDown(
        Icons.svgSlot := "accordion-trigger-icon",
        svg.cls := "cn-accordion-trigger-icon pointer-events-none hidden shrink-0 rotate-180 group-aria-expanded/accordion-trigger:inline"
      )
    )
