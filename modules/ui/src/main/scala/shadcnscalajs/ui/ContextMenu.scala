package shadcnscalajs.ui

import com.raquo.laminar.api.L.*
import org.scalajs.dom

import scala.scalajs.js

/** shadcn/ui ContextMenu — right-click-triggered menu, positioned at the cursor. Same hand-rolled Var/EventBus approach
  * as DropdownMenu (no Radix equivalent exists for Laminar), triggered by `contextmenu` instead of `click` and
  * positioned via fixed left/top coordinates instead of anchored under a trigger button.
  *
  * The utility classes must stand alone: `modules/ui` is what the CLI copies into consumer projects, which have neither
  * the vendored basecoat CSS nor `shadcn-presets.generated.css`. The `cn-context-menu*` hook classes and `data-slot`
  * attributes are additionally what the style-pack presets target — packs are unlayered, so where a pack defines a rule
  * it overrides the utilities below by design.
  */
object ContextMenu:

  final case class Item(label: String, onSelect: () => Unit, disabled: Boolean = false)

  private val onContextMenuEvt: EventProp[dom.MouseEvent] = eventProp("contextmenu")

  private val contentClasses: String =
    "cn-context-menu-content cn-menu-target cn-menu-translucent fixed z-50 min-w-36 overflow-x-hidden overflow-y-auto rounded-md bg-popover p-1 text-popover-foreground shadow-md outline-none ring-1 ring-foreground/10 duration-100 data-open:animate-in data-open:fade-in-0 data-open:zoom-in-95 data-closed:animate-out data-closed:fade-out-0 data-closed:zoom-out-95"

  private val itemClasses: String =
    "cn-context-menu-item group/context-menu-item relative flex cursor-default items-center gap-2 rounded-sm px-2 py-1.5 text-sm outline-hidden select-none data-disabled:pointer-events-none data-disabled:opacity-50 [&_svg]:pointer-events-none [&_svg]:shrink-0 hover:bg-accent hover:text-accent-foreground focus:bg-accent focus:text-accent-foreground"

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
        dataAttr("slot") := "context-menu-content",
        dataAttr("state") <-- isOpenVar.signal.map(open => if open then "open" else "closed"),
        cls := contentClasses,
        display <-- isOpenVar.signal.map(open => if open then "block" else "none"),
        styleAttr <-- posVar.signal.map { case (x, y) => s"left:${x}px; top:${y}px" },
        onMountBind { ctx =>
          isOpenVar.signal --> { open =>
            val el = ctx.thisNode.ref
            if open then
              el.setAttribute("data-open", "")
              el.removeAttribute("data-closed")
            else
              el.setAttribute("data-closed", "")
              el.removeAttribute("data-open")
          }
        },
        items.map { item =>
          div(
            role := "menuitem",
            dataAttr("slot") := "context-menu-item",
            cls := itemClasses,
            aria.disabled := item.disabled,
            if item.disabled then dataAttr("disabled") := "" else emptyMod,
            item.label,
            onClick --> { _ => if !item.disabled then { item.onSelect(); isOpenVar.set(false) } }
          )
        }
      )
    )

  def label(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "context-menu-label",
      cls := "cn-context-menu-label px-2 py-1.5 text-xs font-medium text-muted-foreground data-inset:pl-8",
      mods
    )

  def separator(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "context-menu-separator",
      role := "separator",
      cls := "cn-context-menu-separator bg-border -mx-1 my-1 h-px",
      mods
    )

  def shortcut(mods: Modifier[HtmlElement]*): HtmlElement =
    span(
      dataAttr("slot") := "context-menu-shortcut",
      cls := "cn-context-menu-shortcut text-muted-foreground ml-auto text-xs tracking-widest",
      mods
    )

  def groupHeading(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "context-menu-group-heading",
      cls := "px-2 py-1.5 text-sm font-medium text-foreground data-inset:ps-8",
      mods
    )

  def group(mods: Modifier[HtmlElement]*): HtmlElement =
    div(dataAttr("slot") := "context-menu-group", role := "group", mods)
