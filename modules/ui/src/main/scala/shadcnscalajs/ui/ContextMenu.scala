package shadcnscalajs.ui

import com.raquo.laminar.api.L.*
import org.scalajs.dom

import scala.scalajs.js

/** shadcn/ui ContextMenu — right-click-triggered menu, positioned at the cursor. Same hand-rolled Var/EventBus approach
  * as DropdownMenu (no Radix equivalent exists for Laminar), triggered by `contextmenu` instead of `click` and
  * positioned via fixed left/top coordinates instead of anchored under a trigger button.
  */
object ContextMenu:

  final case class Item(label: String, onSelect: () => Unit, disabled: Boolean = false)

  private val onContextMenuEvt: EventProp[dom.MouseEvent] = eventProp("contextmenu")

  def apply(items: Item*)(mods: Modifier[HtmlElement]*): HtmlElement =
    val isOpenVar = Var(false)
    val posVar = Var((0.0, 0.0))

    def compPath(ev: dom.Event): js.Array[dom.EventTarget] =
      ev.asInstanceOf[js.Dynamic].composedPath().asInstanceOf[js.Array[dom.EventTarget]]

    div(
      cls := "relative",
      mods,
      onContextMenuEvt --> { (ev: dom.MouseEvent) =>
        ev.preventDefault()
        posVar.set((ev.clientX, ev.clientY))
        isOpenVar.set(true)
      },
      onMountBind { ctx =>
        documentEvents(_.onMouseDown) --> { (ev: dom.MouseEvent) =>
          if isOpenVar.now() && compPath(ev).indexOf(ctx.thisNode.ref) == -1 then isOpenVar.set(false)
        }
      },
      div(
        role := "menu",
        cls := "fixed z-50 min-w-[10rem] overflow-hidden rounded-md border bg-popover p-1 text-popover-foreground shadow-md",
        display <-- isOpenVar.signal.map(open => if open then "block" else "none"),
        styleAttr <-- posVar.signal.map { case (x, y) => s"left:${x}px; top:${y}px" },
        items.zipWithIndex.map { case (item, idx) =>
          div(
            role := "menuitem",
            cls := "relative flex cursor-default items-center gap-2 rounded-sm px-2 py-1.5 text-sm outline-hidden select-none hover:bg-accent hover:text-accent-foreground data-[disabled]:pointer-events-none data-[disabled]:opacity-50",
            aria.disabled := item.disabled,
            item.label,
            onClick --> { _ => if !item.disabled then { item.onSelect(); isOpenVar.set(false) } }
          )
        }
      )
    )
