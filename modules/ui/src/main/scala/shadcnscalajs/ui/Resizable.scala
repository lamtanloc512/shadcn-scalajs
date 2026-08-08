package shadcnscalajs.ui

import com.raquo.laminar.api.L.*
import org.scalajs.dom

/** shadcn/ui Resizable — a two-pane split with a drag handle, hand-rolled via document-level mouse-move/up listeners
  * (same `documentEvents` pattern DropdownMenu uses for outside-click) rather than wrapping the
  * `react-resizable-panels` npm package shadcn/ui uses, since there's no Scala.js equivalent to wrap. Horizontal split
  * only — vertical is a documented follow-up, not implemented.
  */
object Resizable:

  def paneGroup(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "resizable-pane-group",
      dataAttr("direction") := "horizontal",
      cls := "cn-resizable-panel-group flex h-full w-full data-[direction=vertical]:flex-col",
      mods
    )

  def handle(mods: Modifier[HtmlElement]*): HtmlElement =
    handle(withHandle = false)(mods*)

  def handle(withHandle: Boolean)(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "resizable-handle",
      dataAttr("direction") := "horizontal",
      cls := "cn-resizable-handle relative flex w-px items-center justify-center bg-border after:absolute after:inset-y-0 after:left-1/2 after:w-1 after:-translate-x-1/2 focus-visible:ring-1 focus-visible:ring-ring focus-visible:ring-offset-1 focus-visible:outline-hidden data-[direction=vertical]:h-px data-[direction=vertical]:w-full data-[direction=vertical]:after:left-0 data-[direction=vertical]:after:h-1 data-[direction=vertical]:after:w-full data-[direction=vertical]:after:translate-x-0 data-[direction=vertical]:after:-translate-y-1/2 [&[data-direction=vertical]>div]:rotate-90",
      if withHandle then div(cls := "cn-resizable-handle-icon z-10 flex h-6 w-1 shrink-0 rounded-lg bg-border")
      else emptyNode,
      mods
    )

  /** `splitVar` is the left pane's width as a percentage (0.0-100.0), owned by the caller so it can be read or
    * persisted if desired.
    */
  def horizontal(splitVar: Var[Double])(left: HtmlElement, right: HtmlElement): HtmlElement =
    val draggingVar = Var(false)
    val containerRef = Var(Option.empty[dom.html.Element])

    def updateFromClientX(clientX: Double): Unit =
      containerRef.now().foreach { el =>
        val rect = el.getBoundingClientRect()
        val pct = ((clientX - rect.left) / rect.width) * 100
        splitVar.set(math.max(10.0, math.min(90.0, pct)))
      }

    paneGroup(
      onMountCallback { ctx => containerRef.set(Some(ctx.thisNode.ref)) },
      onMountBind { _ =>
        documentEvents(_.onMouseMove) --> { (ev: dom.MouseEvent) =>
          if draggingVar.now() then updateFromClientX(ev.clientX)
        }
      },
      onMountBind { _ =>
        documentEvents(_.onMouseUp) --> { _ => draggingVar.set(false) }
      },
      div(cls := "overflow-auto", styleAttr <-- splitVar.signal.map(pct => s"width:${pct}%"), left),
      handle(
        cls := "cursor-col-resize hover:bg-ring active:bg-ring",
        onMouseDown --> { ev =>
          ev.preventDefault(); draggingVar.set(true)
        }
      ),
      div(cls := "flex-1 overflow-auto", right)
    )
