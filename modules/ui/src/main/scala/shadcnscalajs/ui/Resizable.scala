package shadcnscalajs.ui

import com.raquo.laminar.api.L.*
import org.scalajs.dom

/** shadcn/ui Resizable — a two-pane split with a drag handle, hand-rolled via document-level mouse-move/up listeners
  * (same `documentEvents` pattern DropdownMenu uses for outside-click) rather than wrapping the
  * `react-resizable-panels` npm package shadcn/ui uses, since there's no Scala.js equivalent to wrap. Horizontal split
  * only — vertical is a documented follow-up, not implemented.
  */
object Resizable:

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

    div(
      cls := "flex h-full w-full",
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
      div(
        cls := "w-px shrink-0 cursor-col-resize bg-border hover:bg-ring active:bg-ring",
        onMouseDown --> { ev =>
          ev.preventDefault(); draggingVar.set(true)
        }
      ),
      div(cls := "flex-1 overflow-auto", right)
    )
