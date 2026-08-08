package shadcnscalajs.ui

import com.raquo.laminar.api.L.*
import org.scalajs.dom

/** shadcn/ui ContextMenu — the [[Menu]] surface opened by `contextmenu` and anchored to the pointer instead of to an
  * element, via [[Floating.Anchor.openAt]].
  *
  * The previous version positioned itself with `fixed` left/top inside the trigger region, which a `backdrop-blur` or
  * `transform` ancestor turned into a containing block for, placing the menu at the wrong spot; portaling through
  * `Floating` also brings viewport flipping, so a right-click near the bottom edge no longer opens a menu off-screen.
  *
  * The utility classes must stand alone: `modules/ui` is what the CLI copies into consumer projects, which have neither
  * the vendored basecoat CSS nor `shadcn-presets.generated.css`. The `cn-context-menu*` hook classes and `data-slot`
  * attributes are additionally what the style-pack presets target — packs are unlayered, so where a pack defines a rule
  * it overrides the utilities below by design.
  */
object ContextMenu:

  final case class Item(label: String, onSelect: () => Unit, disabled: Boolean = false)

  private val onContextMenuEvt: EventProp[dom.MouseEvent] = eventProp("contextmenu")

  private val slotPrefix = "context-menu"

  def apply(items: Item*)(mods: Modifier[HtmlElement]*): HtmlElement =
    trigger(ctx => items.map(i => ctx.item(i.onSelect, disabledMods(i.disabled), i.label)))(mods*)

  private def disabledMods(disabled: Boolean): Modifier[HtmlElement] =
    if disabled then Seq[Modifier[HtmlElement]](aria.disabled := true, dataAttr("disabled") := "")
    else emptyMod

  /** Composable form: `build` supplies the rows, `mods` the right-clickable region. */
  def trigger(build: Menu.Ctx => Seq[Modifier[HtmlElement]])(mods: Modifier[HtmlElement]*): HtmlElement =
    val anchor = Floating.anchor()
    val ctx = Menu.Ctx(anchor, slotPrefix)

    div(
      dataAttr("slot") := "context-menu-trigger",
      cls := "relative",
      Floating.triggerBase(anchor),
      mods,
      onContextMenuEvt --> { (ev: dom.MouseEvent) =>
        ev.preventDefault()
        anchor.openAt(ev.clientX, ev.clientY)
      },
      Floating.content(
        anchor,
        // Upstream opens down-and-right of the cursor, so the pointer sits at the panel's corner with no offset.
        Floating.Placement(side = Floating.Side.Bottom, align = Floating.Align.Start, sideOffset = 0.0),
        Menu.contentClass(slotPrefix)
      )(
        dataAttr("slot") := "context-menu-content",
        role := "menu",
        tabIndex := -1,
        Menu.keyboardNav(anchor),
        build(ctx)
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
      cls := "cn-context-menu-separator -mx-1 my-1 h-px bg-border",
      mods
    )

  def shortcut(mods: Modifier[HtmlElement]*): HtmlElement =
    span(
      dataAttr("slot") := "context-menu-shortcut",
      cls := "cn-context-menu-shortcut ml-auto text-xs tracking-widest text-muted-foreground",
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
