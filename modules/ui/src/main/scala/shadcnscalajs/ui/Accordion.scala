package shadcnscalajs.ui

import com.raquo.laminar.api.L.*
import org.scalajs.dom
import shadcnscalajs.core.CommonAttrs.openAttr

import scala.scalajs.js

/** shadcn/ui Accordion — native `<details>`/`<summary>` with Tailwind styling, reactive single-open via Laminar.
  */
object Accordion:

  final case class Section(title: Modifier[HtmlElement], content: Modifier[HtmlElement], disabled: Boolean = false)

  private def isOpenAttr(el: dom.Element): Boolean =
    el.asInstanceOf[js.Dynamic].open.asInstanceOf[Boolean]

  def apply(openIndexVar: Var[Option[Int]], sections: Section*): HtmlElement =
    div(
      cls := "flex flex-col",
      children <-- openIndexVar.signal.map { openIndex =>
        sections.zipWithIndex.map { case (sec, idx) =>
          renderSection(openIndexVar, isOpen = openIndex.contains(idx), sec, idx)
        }.toList
      }
    )

  private def renderSection(openIndexVar: Var[Option[Int]], isOpen: Boolean, sec: Section, idx: Int): HtmlElement =
    detailsTag(
      openAttr := isOpen,
      cls := "border-b last:border-b-0",
      aria.disabled := sec.disabled,
      summaryTag(
        cls := "flex cursor-pointer items-center justify-between py-4 font-medium transition-all hover:underline [&::-webkit-details-marker]:hidden",
        sec.title
      ),
      div(
        cls := "pb-4 pt-0 text-muted-foreground",
        sec.content
      ),
      onToggle --> { (ev: dom.Event) =>
        if !sec.disabled then
          val nowOpen = isOpenAttr(ev.target.asInstanceOf[dom.Element])
          openIndexVar.set(if nowOpen then Some(idx) else None)
      }
    )
