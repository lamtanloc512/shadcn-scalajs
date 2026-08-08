package shadcnscalajs.ui

import com.raquo.laminar.api.L.*
import org.scalajs.dom

/** shadcn/ui Collapsible — `data-state`-driven disclosure with an animated height.
  *
  * Replaces a `<details>`/`<summary>` stand-in, which snapped open with no transition and exposed none of the
  * `data-state` hooks the packs and upstream's animation utilities target. Trigger and content are descriptors because
  * they share one open state; [[withOpen]] takes that state when the caller needs to drive or observe it.
  */
object Collapsible:

  final case class Trigger private[Collapsible] (mods: Seq[Modifier[HtmlElement]])
  final case class Content private[Collapsible] (mods: Seq[Modifier[HtmlElement]])

  def trigger(mods: Modifier[HtmlElement]*): Trigger = Trigger(mods)
  def content(mods: Modifier[HtmlElement]*): Content = Content(mods)

  def apply(triggerSpec: Trigger, contentSpec: Content): HtmlElement =
    withOpen(Var(false))(triggerSpec, contentSpec)

  def withOpen(openVar: Var[Boolean])(triggerSpec: Trigger, contentSpec: Content): HtmlElement =
    def stateOf(open: Boolean): String = if open then "open" else "closed"

    div(
      dataAttr("slot") := "collapsible",
      dataAttr("state") <-- openVar.signal.map(stateOf),
      cls := "collapsible cn-collapsible",
      button(
        typ := "button",
        dataAttr("slot") := "collapsible-trigger",
        dataAttr("state") <-- openVar.signal.map(stateOf),
        aria.expanded <-- openVar.signal,
        cls := "cn-collapsible-trigger cursor-pointer text-left outline-none focus-visible:ring-[3px] focus-visible:ring-ring/50",
        onClick --> { _ => openVar.update(!_) },
        triggerSpec.mods
      ),
      div(
        dataAttr("slot") := "collapsible-content",
        dataAttr("state") <-- openVar.signal.map(stateOf),
        cls := "cn-collapsible-content overflow-hidden transition-[max-height] duration-200 ease-out",
        // Height is measured rather than transitioned to `auto`, which does not animate. The closed state also drops
        // the content from the accessibility tree once the transition has had time to finish.
        onMountBind { ctx =>
          val el = ctx.thisNode.ref
          def applyOpen(open: Boolean): Unit =
            if open then
              el.style.maxHeight = s"${el.scrollHeight}px"
              el.removeAttribute("aria-hidden")
            else
              el.style.maxHeight = "0px"
              el.setAttribute("aria-hidden", "true")
          openVar.signal --> { open => applyOpen(open) }
        },
        // Reflowing at a new width changes the content's height, and a stale cap would clip it.
        onMountBind { ctx =>
          val el = ctx.thisNode.ref
          windowEvents(_.onResize) --> { (_: dom.Event) =>
            if openVar.now() then el.style.maxHeight = s"${el.scrollHeight}px"
          }
        },
        contentSpec.mods
      )
    )
