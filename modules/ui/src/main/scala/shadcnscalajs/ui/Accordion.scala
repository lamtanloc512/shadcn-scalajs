package shadcnscalajs.ui

import com.raquo.laminar.api.L.*
import org.scalajs.dom
import shadcnscalajs.core.CommonAttrs.openAttr

import scala.scalajs.js

/** Laminar port of basecoat's Accordion (basecoat/src/css/components/accordion.css +
  * basecoat/src/js/accordion.js). Native-element tier: open/close itself is free
  * from native `<details>`/`<summary>` semantics. basecoat's own JS here only (a)
  * closes sibling sections to enforce single-open, and (b) blocks toggling disabled
  * sections — both reimplemented declaratively below via `openIndexVar` (single
  * source of truth) instead of imperative DOM listening, since Laminar's
  * `children <-- signal` re-render gives us single-open-for-free.
  */
object Accordion:

  final case class Section(title: Modifier[HtmlElement], content: Modifier[HtmlElement], disabled: Boolean = false)

  private def isOpenAttr(el: dom.Element): Boolean =
    el.asInstanceOf[js.Dynamic].open.asInstanceOf[Boolean]

  def apply(openIndexVar: Var[Option[Int]], sections: Section*): HtmlElement =
    sectionTag(
      cls := "accordion",
      children <-- openIndexVar.signal.map { openIndex =>
        sections.zipWithIndex.map { case (sec, idx) =>
          renderSection(openIndexVar, isOpen = openIndex.contains(idx), sec, idx)
        }.toList
      }
    )

  private def renderSection(openIndexVar: Var[Option[Int]], isOpen: Boolean, sec: Section, idx: Int): HtmlElement =
    detailsTag(
      openAttr := isOpen,
      aria.disabled := sec.disabled,
      summaryTag(sec.title),
      sectionTag(sec.content),
      onToggle --> { (ev: dom.Event) =>
        if !sec.disabled then
          val nowOpen = isOpenAttr(ev.target.asInstanceOf[dom.Element])
          openIndexVar.set(if nowOpen then Some(idx) else None)
      }
    )
