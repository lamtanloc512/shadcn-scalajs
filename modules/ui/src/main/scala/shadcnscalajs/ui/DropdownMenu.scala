package shadcnscalajs.ui

import com.raquo.laminar.api.L.*
import org.scalajs.dom

import scala.scalajs.js

/** shadcn/ui DropdownMenu — hand-rolled Airstream state machine with Tailwind styling matching the canonical
  * dropdown-menu.tsx. No Radix equivalent exists for Laminar, so behavior is reimplemented via Var/EventBus.
  *
  * The utility classes must stand alone: `modules/ui` is what the CLI copies into consumer projects, which have neither
  * the vendored basecoat CSS nor `shadcn-presets.generated.css`. The `cn-dropdown-menu*` hook classes and `data-slot`
  * attributes are additionally what the style-pack presets target — packs are unlayered, so where a pack defines a rule
  * it overrides the utilities below by design.
  */
object DropdownMenu:

  /** `checked` turns the row into a DropdownMenuCheckboxItem: a trailing check indicator tracks the signal. */
  final case class Item(
      label: String,
      onSelect: () => Unit,
      disabled: Boolean = false,
      checked: Option[Signal[Boolean]] = None
  )

  object Item:
    def checkbox(label: String, checked: Signal[Boolean], onSelect: () => Unit): Item =
      Item(label, onSelect, disabled = false, checked = Some(checked))

  enum Align derives CanEqual:
    case Start, End

  /** Trigger base — same Tailwind classes as Button.outline. */
  val outlineTriggerClasses: String =
    "inline-flex shrink-0 items-center justify-center gap-2 rounded-md text-sm font-medium whitespace-nowrap transition-all outline-none focus-visible:border-ring focus-visible:ring-[3px] focus-visible:ring-ring/50 disabled:pointer-events-none disabled:opacity-50 border bg-background shadow-xs hover:bg-accent hover:text-accent-foreground h-9 px-4 py-2"

  /** Borderless square trigger for row/card action menus — the `variant="ghost" size="icon"` trigger upstream. */
  val ghostIconTriggerClasses: String =
    "inline-flex shrink-0 items-center justify-center gap-2 rounded-md text-sm font-medium whitespace-nowrap transition-all outline-none focus-visible:border-ring focus-visible:ring-[3px] focus-visible:ring-ring/50 disabled:pointer-events-none disabled:opacity-50 hover:bg-accent hover:text-accent-foreground size-8"

  private val contentClasses: String =
    "cn-dropdown-menu-content cn-dropdown-menu-content-logical cn-menu-target cn-menu-translucent z-50 min-w-48 overflow-x-hidden overflow-y-auto rounded-md bg-popover p-1 text-popover-foreground shadow-md outline-none ring-1 ring-foreground/10 duration-100 data-closed:overflow-hidden data-open:animate-in data-open:fade-in-0 data-open:zoom-in-95 data-closed:animate-out data-closed:fade-out-0 data-closed:zoom-out-95"

  private val itemClasses: String =
    "cn-dropdown-menu-item group/dropdown-menu-item relative flex cursor-default items-center gap-2 rounded-sm px-2 py-1.5 text-sm outline-hidden select-none data-[inset]:pl-8 data-disabled:pointer-events-none data-disabled:opacity-50 [&_svg]:pointer-events-none [&_svg]:shrink-0"

  private val checkboxItemClasses: String =
    "cn-dropdown-menu-checkbox-item relative flex cursor-default items-center gap-2 rounded-sm py-1.5 pe-8 ps-2 text-sm outline-hidden select-none data-[disabled]:pointer-events-none data-[disabled]:opacity-50 [&_svg]:pointer-events-none [&_svg]:shrink-0"

  private var idCounter = 0
  private def nextId(): String = { idCounter += 1; s"dropdown-menu-$idCounter" }

  def apply(trigger: Modifier[HtmlElement]*)(items: Item*): HtmlElement =
    render(outlineTriggerClasses, Align.Start, trigger, items)

  /** Menu anchored to the trigger's inline end — the `align="end"` content of the canonical component. */
  def alignEnd(trigger: Modifier[HtmlElement]*)(items: Item*): HtmlElement =
    render(outlineTriggerClasses, Align.End, trigger, items)

  /** Replaces the trigger's class list outright. Appending overrides instead loses to the base classes whenever
    * Tailwind emits the conflicting utility later (e.g. `p-0` never beats `px-4`).
    */
  def withTrigger(triggerClasses: String, align: Align = Align.Start)(trigger: Modifier[HtmlElement]*)(
      items: Item*
  ): HtmlElement =
    render(triggerClasses, align, trigger, items)

  def label(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "dropdown-menu-label",
      cls := "cn-dropdown-menu-label px-2 py-1.5 text-xs font-medium text-muted-foreground data-[inset]:pl-8",
      mods
    )

  def separator(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "dropdown-menu-separator",
      role := "separator",
      cls := "cn-dropdown-menu-separator bg-border -mx-1 my-1 h-px",
      mods
    )

  def shortcut(mods: Modifier[HtmlElement]*): HtmlElement =
    span(
      dataAttr("slot") := "dropdown-menu-shortcut",
      cls := "cn-dropdown-menu-shortcut text-muted-foreground ml-auto text-xs tracking-widest",
      mods
    )

  def groupHeading(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "dropdown-menu-group-heading",
      cls := "px-2 py-1.5 text-sm font-semibold data-[inset]:ps-8",
      mods
    )

  def group(mods: Modifier[HtmlElement]*): HtmlElement =
    div(dataAttr("slot") := "dropdown-menu-group", role := "group", mods)

  private def render(
      triggerClasses: String,
      align: Align,
      trigger: Seq[Modifier[HtmlElement]],
      items: Seq[Item]
  ): HtmlElement =
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
      cls := "dropdown-menu relative inline-flex",
      onMountBind { ctx =>
        documentEvents(_.onMouseDown) --> { (ev: dom.MouseEvent) =>
          if isOpenVar.now() && compPath(ev).indexOf(ctx.thisNode.ref) == -1 then isOpenVar.set(false)
        }
      },
      button(
        typ := "button",
        dataAttr("slot") := "dropdown-menu-trigger",
        cls := triggerClasses,
        aria.hasPopup := true,
        aria.expanded <-- isOpenVar.signal,
        idAttr := menuId,
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
      div(
        dataAttr("slot") := "dropdown-menu-content",
        dataAttr("state") <-- isOpenVar.signal.map(open => if open then "open" else "closed"),
        cls := s"absolute top-full ${if align == Align.End then "end-0" else "start-0"} mt-1 $contentClasses",
        display <-- isOpenVar.signal.map(open => if open then "block" else "none"),
        aria.hidden <-- isOpenVar.signal.map(!_),
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
        div(
          role := "menu",
          children <-- activeIndexVar.signal.map { activeIdx =>
            items.zipWithIndex.map { case (item, idx) =>
              val isCheckbox = item.checked.isDefined
              div(
                role := (if isCheckbox then "menuitemcheckbox" else "menuitem"),
                dataAttr("slot") := (if isCheckbox then "dropdown-menu-checkbox-item" else "dropdown-menu-item"),
                cls := s"${if isCheckbox then checkboxItemClasses else itemClasses} ${
                    if idx == activeIdx then "bg-accent text-accent-foreground" else ""
                  }",
                aria.disabled := item.disabled,
                if item.disabled then dataAttr("disabled") := "" else emptyMod,
                item.checked.map(c => aria.checked <-- c.map(_.toString)).toSeq,
                item.label,
                item.checked.map { c =>
                  span(
                    dataAttr("slot") := "dropdown-menu-checkbox-item-indicator",
                    cls := "cn-dropdown-menu-item-indicator pointer-events-none absolute end-2 flex size-3.5 items-center justify-center",
                    cls("invisible") <-- c.map(!_),
                    Icons.check(svg.cls := "size-4")
                  )
                }.toSeq,
                onClick --> { _ => select(idx) }
              )
            }.toList
          }
        )
      )
    )

  private def firstEnabledIndex(items: Seq[Item], from: Int): Int =
    items.zipWithIndex.collectFirst { case (item, idx) if !item.disabled => idx }.getOrElse(from)
