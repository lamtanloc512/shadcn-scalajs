package shadcnscalajs.ui

import com.raquo.laminar.api.L.*
import org.scalajs.dom

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

  /** Trigger base — same Tailwind classes as Button.outline. */
  val outlineTriggerClasses: String =
    "inline-flex shrink-0 items-center justify-center gap-2 rounded-md text-sm font-medium whitespace-nowrap transition-all outline-none focus-visible:border-ring focus-visible:ring-[3px] focus-visible:ring-ring/50 disabled:pointer-events-none disabled:opacity-50 border bg-background shadow-xs hover:bg-accent hover:text-accent-foreground h-9 px-4 py-2"

  /** Borderless square trigger for row/card action menus — the `variant="ghost" size="icon"` trigger upstream. */
  val ghostIconTriggerClasses: String =
    "inline-flex shrink-0 items-center justify-center gap-2 rounded-md text-sm font-medium whitespace-nowrap transition-all outline-none focus-visible:border-ring focus-visible:ring-[3px] focus-visible:ring-ring/50 disabled:pointer-events-none disabled:opacity-50 hover:bg-accent hover:text-accent-foreground size-8"

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
    render(outlineTriggerClasses, Align.Start, trigger, build)

  /** As [[items]], but replacing the trigger's class list — the overload is named rather than added to `items` because
    * a `String` is itself a `Modifier`, which would make the two forms ambiguous.
    */
  def itemsWithTrigger(triggerClasses: String, align: Align = Align.Start)(trigger: Modifier[HtmlElement]*)(
      build: Menu.Ctx => Seq[Modifier[HtmlElement]]
  ): HtmlElement =
    render(triggerClasses, align, trigger, build)

  def apply(trigger: Modifier[HtmlElement]*)(items: Item*): HtmlElement =
    render(outlineTriggerClasses, Align.Start, trigger, adapt(items))

  /** Menu anchored to the trigger's inline end — the `align="end"` content of the canonical component. */
  def alignEnd(trigger: Modifier[HtmlElement]*)(items: Item*): HtmlElement =
    render(outlineTriggerClasses, Align.End, trigger, adapt(items))

  /** Replaces the trigger's class list outright. Appending overrides instead loses to the base classes whenever
    * Tailwind emits the conflicting utility later (e.g. `p-0` never beats `px-4`).
    */
  def withTrigger(triggerClasses: String, align: Align = Align.Start)(trigger: Modifier[HtmlElement]*)(
      items: Item*
  ): HtmlElement =
    render(triggerClasses, align, trigger, adapt(items))

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
      triggerClasses: String,
      align: Align,
      trigger: Seq[Modifier[HtmlElement]],
      build: Menu.Ctx => Seq[Modifier[HtmlElement]]
  ): HtmlElement =
    val anchor = Floating.anchor()
    val ctx = Menu.Ctx(anchor, slotPrefix)

    div(
      dataAttr("slot") := "dropdown-menu",
      cls := "dropdown-menu inline-flex",
      button(
        typ := "button",
        dataAttr("slot") := "dropdown-menu-trigger",
        cls := triggerClasses,
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
