package shadcnscalajs.ui

import com.raquo.laminar.api.L.*
import org.scalajs.dom
import shadcnscalajs.core.Tags.slotTag

import scala.scalajs.js

/** shadcn/ui DropdownMenu — a portaled, keyboard-navigable menu on [[Floating]] with the parts shared through [[Menu]].
  *
  * The panel is no longer an absolutely positioned sibling of the trigger: that version could not escape an `overflow`
  * ancestor, so a row-action menu inside a scrolling table was clipped. It also could not flip when short of room
  * below.
  *
  * Two APIs coexist deliberately. [[items]] takes composable children, matching upstream's
  * `DropdownMenu.Item`/`CheckboxItem`/`Sub` composition. The older `apply(trigger*)(Item*)` form stays as a thin
  * adapter over it so existing call sites keep working while they migrate.
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

  /** Trigger base — the `variant="outline"` trigger upstream. Borrowed from [[Button]] rather than copied: a
    * hand-written utility string drifts from the real button and misses the `data-variant`/`cn-button-*` hooks that
    * basecoat and the style packs skin and size buttons through, leaving the trigger a different height from every
    * button beside it.
    */
  val outlineTrigger: Modifier[HtmlElement] = Button.appearance(Button.Variant.Outline)

  /** Borderless square trigger for row/card action menus — the `variant="ghost" size="icon"` trigger upstream. */
  val ghostIconTrigger: Modifier[HtmlElement] = Button.appearance(Button.Variant.Ghost, Button.Size.IconSm)

  private val slotPrefix = "dropdown-menu"

  private def floatingAlign(align: Align): Floating.Align = align match
    case Align.Start => Floating.Align.Start
    case Align.End   => Floating.Align.End

  /** Composable form: `build` receives the menu context and returns its rows.
    *
    * The context is a parameter rather than an import because checkbox and radio rows, submenus, and item selection all
    * need the open state of *this* menu — a nested submenu gets its own context for the same reason.
    */
  def items(trigger: Modifier[HtmlElement]*)(build: Menu.Ctx => Seq[Modifier[HtmlElement]]): HtmlElement =
    render(outlineTrigger, Align.Start, trigger, build)

  /** As [[items]], but with a different trigger look — [[ghostIconTrigger]], say, or any [[Button.appearance]]. The
    * overload is named rather than added to `items` because the styling is itself a `Modifier`, which would make the
    * two forms ambiguous.
    */
  /** `wrapperStyle` reaches the `inline-flex` element wrapping the trigger. That element shrink-wraps by default, so a
    * trigger asked to fill its row (`w-full`) still only fills the shrunken wrapper; pass `cls := "w-full"` here to
    * stretch it.
    */
  def itemsWithTrigger(
      triggerStyle: Modifier[HtmlElement],
      align: Align = Align.Start,
      wrapperStyle: Modifier[HtmlElement] = emptyMod
  )(
      trigger: Modifier[HtmlElement]*
  )(
      build: Menu.Ctx => Seq[Modifier[HtmlElement]]
  ): HtmlElement =
    render(triggerStyle, align, trigger, build, wrapperStyle)

  /** Menu whose trigger is supplied through a named slot. The slot receives behavior directly, avoiding a nested
    * `<button>` when a consumer supplies an already interactive custom element such as `<sc-button>`.
    */
  def slottedItems(slotName: String = "trigger")(
      build: Menu.Ctx => Seq[Modifier[HtmlElement]]
  ): HtmlElement =
    val anchor = Floating.anchor()
    val ctx = Menu.Ctx(anchor, slotPrefix)
    val contentId = s"sc-menu-content-${js.Date.now().toLong}"
    div(
      dataAttr("slot") := "dropdown-menu",
      cls := "dropdown-menu inline-flex",
      slotTag(
        nameAttr := slotName,
        dataAttr("slot") := "dropdown-menu-trigger",
        aria.hasPopup := true,
        dataAttr("state") <-- anchor.isOpen.signal.map(open => if open then "open" else "closed"),
        aria.expanded <-- anchor.isOpen.signal,
        onMountUnmountCallback(
          mount = { mountCtx =>
            val slot = mountCtx.thisNode.ref.asInstanceOf[js.Dynamic]
            val syncTrigger: js.Function0[Unit] = () =>
              slot
                .assignedElements()
                .asInstanceOf[js.Array[dom.html.Element]]
                .headOption
                .foreach { trigger =>
                  Floating.bindTrigger(anchor, trigger)
                  trigger.setAttribute("aria-haspopup", "menu")
                  trigger.setAttribute("aria-controls", contentId)
                  trigger.setAttribute("aria-expanded", anchor.isOpen.now().toString)
                  trigger.setAttribute("data-state", if anchor.isOpen.now() then "open" else "closed")
                }
            val listener: js.Function1[dom.Event, Unit] = _ => syncTrigger()
            slot.__scTriggerListener = listener
            slot.addEventListener("slotchange", listener)
            syncTrigger()
          },
          unmount = mountCtx =>
            val slot = mountCtx.ref.asInstanceOf[js.Dynamic]
            val listener = slot.__scTriggerListener
            if listener != null && !js.isUndefined(listener) then slot.removeEventListener("slotchange", listener)
            slot.__scTriggerListener = null
        ),
        onMountBind { mountCtx =>
          val slot = mountCtx.thisNode.ref.asInstanceOf[js.Dynamic]
          anchor.isOpen.signal --> Observer[Boolean] { open =>
            slot.assignedElements().asInstanceOf[js.Array[dom.html.Element]].headOption.foreach { trigger =>
              trigger.setAttribute("aria-expanded", open.toString)
              trigger.setAttribute("data-state", if open then "open" else "closed")
            }
          }
        },
        Floating.clickToToggle(anchor),
        onKeyDown --> { (ev: dom.KeyboardEvent) =>
          if ev.key == "ArrowDown" || ev.key == "ArrowUp" then
            ev.preventDefault()
            if !anchor.isOpen.now() then anchor.open()
        }
      ),
      Floating.content(anchor, Floating.Placement(side = Floating.Side.Bottom), Menu.contentClass(slotPrefix))(
        dataAttr("slot") := "dropdown-menu-content",
        idAttr := contentId,
        role := "menu",
        tabIndex := -1,
        Menu.keyboardNav(anchor),
        build(ctx)
      )
    )

  def apply(trigger: Modifier[HtmlElement]*)(items: Item*): HtmlElement =
    render(outlineTrigger, Align.Start, trigger, adapt(items))

  /** Menu anchored to the trigger's inline end — the `align="end"` content of the canonical component. */
  def alignEnd(trigger: Modifier[HtmlElement]*)(items: Item*): HtmlElement =
    render(outlineTrigger, Align.End, trigger, adapt(items))

  /** Replaces the trigger's look outright. Appending overrides instead loses to the base classes whenever Tailwind
    * emits the conflicting utility later (e.g. `p-0` never beats `px-4`).
    */
  def withTrigger(triggerStyle: Modifier[HtmlElement], align: Align = Align.Start)(trigger: Modifier[HtmlElement]*)(
      items: Item*
  ): HtmlElement =
    render(triggerStyle, align, trigger, adapt(items))

  /** Bridges the flat `Item` list onto the composable parts, so both APIs render the same markup and share behavior. */
  private def adapt(items: Seq[Item])(ctx: Menu.Ctx): Seq[Modifier[HtmlElement]] =
    items.map { i =>
      i.checked match
        case Some(checked) =>
          ctx.checkboxItem(checked, i.onSelect, disabledMods(i.disabled), i.label)
        case None =>
          ctx.item(i.onSelect, disabledMods(i.disabled), i.label)
    }

  private def disabledMods(disabled: Boolean): Modifier[HtmlElement] =
    if disabled then Seq[Modifier[HtmlElement]](aria.disabled := true, dataAttr("disabled") := "")
    else emptyMod

  private def render(
      triggerStyle: Modifier[HtmlElement],
      align: Align,
      trigger: Seq[Modifier[HtmlElement]],
      build: Menu.Ctx => Seq[Modifier[HtmlElement]],
      wrapperStyle: Modifier[HtmlElement] = emptyMod
  ): HtmlElement =
    val anchor = Floating.anchor()
    val ctx = Menu.Ctx(anchor, slotPrefix)

    div(
      dataAttr("slot") := "dropdown-menu",
      cls := "dropdown-menu inline-flex",
      wrapperStyle,
      button(
        typ := "button",
        dataAttr("slot") := "dropdown-menu-trigger",
        triggerStyle,
        aria.hasPopup := true,
        Floating.triggerBase(anchor),
        Floating.clickToToggle(anchor),
        trigger,
        onKeyDown --> { (ev: dom.KeyboardEvent) =>
          // Arrow keys open the menu; from there the panel's own handler has focus and takes over.
          if ev.key == "ArrowDown" || ev.key == "ArrowUp" then
            ev.preventDefault()
            if !anchor.isOpen.now() then anchor.open()
        }
      ),
      Floating.content(
        anchor,
        Floating.Placement(side = Floating.Side.Bottom, align = floatingAlign(align)),
        Menu.contentClass(slotPrefix)
      )(
        dataAttr("slot") := "dropdown-menu-content",
        role := "menu",
        tabIndex := -1,
        Menu.keyboardNav(anchor),
        build(ctx)
      )
    )

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
      cls := "cn-dropdown-menu-separator -mx-1 my-1 h-px bg-border",
      mods
    )

  def shortcut(mods: Modifier[HtmlElement]*): HtmlElement =
    span(
      dataAttr("slot") := "dropdown-menu-shortcut",
      cls := "cn-dropdown-menu-shortcut ml-auto text-xs tracking-widest text-muted-foreground",
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
