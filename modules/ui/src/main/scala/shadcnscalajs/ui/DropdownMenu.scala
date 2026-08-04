package shadcnscalajs.ui

import com.raquo.laminar.api.L.*
import org.scalajs.dom

import scala.scalajs.js

/** shadcn/ui DropdownMenu — hand-rolled Airstream state machine with Tailwind styling matching the canonical
  * dropdown-menu.tsx. No Radix equivalent exists for Laminar, so behavior is reimplemented via Var/EventBus.
  */
object DropdownMenu:

  final case class Item(label: String, onSelect: () => Unit, disabled: Boolean = false)

  private var idCounter = 0
  private def nextId(): String = { idCounter += 1; s"dropdown-menu-$idCounter" }

  def apply(trigger: Modifier[HtmlElement]*)(items: Item*): HtmlElement =
    val isOpenVar = Var(false)
    val activeIndexVar = Var(firstEnabledIndex(items, 0))
    val menuId = nextId()

    def compPath(ev: dom.Event): js.Array[dom.EventTarget] =
      ev.asInstanceOf[js.Dynamic].composedPath().asInstanceOf[js.Array[dom.EventTarget]]

    def enabledIndices: Seq[Int] = items.zipWithIndex.collect { case (item, idx) if !item.disabled => idx }

    def moveActive(delta: Int): Unit =
      val indices = enabledIndices
      if indices.nonEmpty then
        val currentPos = math.max(indices.indexOf(activeIndexVar.now()), 0)
        val nextPos = (((currentPos + delta) % indices.size) + indices.size) % indices.size
        activeIndexVar.set(indices(nextPos))

    def select(idx: Int): Unit =
      items.lift(idx).filterNot(_.disabled).foreach { item =>
        item.onSelect()
        isOpenVar.set(false)
      }

    div(
      cls := "relative inline-flex",
      onMountBind { ctx =>
        documentEvents(_.onMouseDown) --> { (ev: dom.MouseEvent) =>
          if isOpenVar.now() && compPath(ev).indexOf(ctx.thisNode.ref) == -1 then isOpenVar.set(false)
        }
      },
      // Trigger button — same Tailwind base as Button.outline
      button(
        typ := "button",
        cls := "inline-flex shrink-0 items-center justify-center gap-2 rounded-md text-sm font-medium whitespace-nowrap transition-all outline-none focus-visible:border-ring focus-visible:ring-[3px] focus-visible:ring-ring/50 disabled:pointer-events-none disabled:opacity-50 border bg-background shadow-xs hover:bg-accent hover:text-accent-foreground h-9 px-4 py-2",
        aria.hasPopup := true,
        aria.expanded <-- isOpenVar.signal,
        trigger,
        onClick --> { _ => isOpenVar.update(!_) },
        onKeyDown --> { (ev: dom.KeyboardEvent) =>
          ev.key match
            case "ArrowDown" =>
              ev.preventDefault()
              if !isOpenVar.now() then isOpenVar.set(true) else moveActive(1)
            case "ArrowUp" =>
              ev.preventDefault()
              if !isOpenVar.now() then isOpenVar.set(true) else moveActive(-1)
            case "Enter" | " " if isOpenVar.now() =>
              ev.preventDefault()
              select(activeIndexVar.now())
            case "Escape" =>
              isOpenVar.set(false)
            case _ => ()
        }
      ),
      // Popover menu
      div(
        cls := "absolute top-full left-0 z-50 mt-1 min-w-[8rem] overflow-hidden rounded-md border bg-popover p-1 text-popover-foreground shadow-md",
        display <-- isOpenVar.signal.map(open => if open then "block" else "none"),
        aria.hidden <-- isOpenVar.signal.map(!_),
        div(
          role := "menu",
          children <-- activeIndexVar.signal.map { activeIdx =>
            items.zipWithIndex.map { case (item, idx) =>
              div(
                role := "menuitem",
                cls := s"relative flex cursor-default items-center gap-2 rounded-sm px-2 py-1.5 text-sm outline-hidden select-none data-[disabled]:pointer-events-none data-[disabled]:opacity-50 ${if idx == activeIdx then "bg-accent text-accent-foreground" else ""}",
                aria.disabled := item.disabled,
                item.label,
                onClick --> { _ => select(idx) }
              )
            }.toList
          }
        )
      )
    )

  private def firstEnabledIndex(items: Seq[Item], from: Int): Int =
    items.zipWithIndex.collectFirst { case (item, idx) if !item.disabled => idx }.getOrElse(from)
