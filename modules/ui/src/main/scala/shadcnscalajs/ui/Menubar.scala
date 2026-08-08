package shadcnscalajs.ui

import com.raquo.laminar.api.L.*
import org.scalajs.dom

/** shadcn/ui Menubar — a row of [[Menu]] surfaces with application-menubar behavior: opening one menu and then moving
  * the pointer across the bar switches menus without a second click, and only one is ever open.
  *
  * That behavior needs state shared across the row, so [[bar]] hands a [[Bar]] to its children and each menu registers
  * with it. Triggers are flat rather than outline buttons, matching upstream's `MenubarTrigger`; the older [[menu]]
  * form stays for call sites that only need an isolated menu in the bar.
  *
  * The utility classes must stand alone: `modules/ui` is what the CLI copies into consumer projects, which have neither
  * the vendored basecoat CSS nor `shadcn-presets.generated.css`. The `cn-menubar*` hook classes and `data-slot`
  * attributes are additionally what the style-pack presets target — packs are unlayered, so where a pack defines a rule
  * it overrides the utilities below by design.
  */
object Menubar:

  private val slotPrefix = "menubar"

  val triggerClasses: String =
    "cn-menubar-trigger flex items-center rounded-sm px-2 py-1 text-sm font-medium outline-hidden select-none hover:bg-accent hover:text-accent-foreground focus:bg-accent focus:text-accent-foreground data-[state=open]:bg-accent data-[state=open]:text-accent-foreground"

  private val rootClasses: String =
    "cn-menubar flex h-9 items-center gap-1 rounded-md border bg-background p-1 shadow-xs"

  /** The open-menu state shared across one bar. */
  final class Bar private[ui] ():
    private var anchors: List[Floating.Anchor] = Nil

    private def register(a: Floating.Anchor): Unit = anchors ::= a

    private def anyOpen: Boolean = anchors.exists(_.isOpen.now())

    private def closeOthers(keep: Floating.Anchor): Unit =
      anchors.filterNot(_ eq keep).foreach(_.close())

    def menu(trigger: Modifier[HtmlElement]*)(items: DropdownMenu.Item*): HtmlElement =
      menuItems(trigger*)(ctx =>
        items.map { i =>
          i.checked match
            case Some(checked) => ctx.checkboxItem(checked, i.onSelect, itemMods(i.disabled), i.label)
            case None          => ctx.item(i.onSelect, itemMods(i.disabled), i.label)
        }
      )

    def menuItems(trigger: Modifier[HtmlElement]*)(build: Menu.Ctx => Seq[Modifier[HtmlElement]]): HtmlElement =
      val anchor = Floating.anchor()
      register(anchor)
      renderMenu(
        anchor,
        trigger,
        build,
        // Hovering a trigger switches menus, but only once the bar is already "active" — otherwise merely crossing the
        // bar with the pointer would pop menus open unprompted.
        onPointerEnter --> { _ =>
          if anyOpen then
            closeOthers(anchor)
            anchor.open()
        },
        onClick.stopPropagation --> { _ =>
          closeOthers(anchor)
          anchor.toggle()
        }
      )

  private def itemMods(disabled: Boolean): Modifier[HtmlElement] =
    if disabled then Seq[Modifier[HtmlElement]](aria.disabled := true, dataAttr("disabled") := "")
    else emptyMod

  private def renderMenu(
      anchor: Floating.Anchor,
      trigger: Seq[Modifier[HtmlElement]],
      build: Menu.Ctx => Seq[Modifier[HtmlElement]],
      interaction: Modifier[HtmlElement]*
  ): HtmlElement =
    val ctx = Menu.Ctx(anchor, slotPrefix)
    div(
      dataAttr("slot") := "menubar-menu",
      cls := "contents",
      button(
        typ := "button",
        dataAttr("slot") := "menubar-trigger",
        cls := triggerClasses,
        aria.hasPopup := true,
        Floating.triggerBase(anchor),
        interaction,
        trigger,
        onKeyDown --> { (ev: dom.KeyboardEvent) =>
          if ev.key == "ArrowDown" then
            ev.preventDefault()
            anchor.open()
        }
      ),
      Floating.content(
        anchor,
        Floating.Placement(side = Floating.Side.Bottom, align = Floating.Align.Start),
        Menu.contentClass(slotPrefix)
      )(
        dataAttr("slot") := "menubar-content",
        role := "menu",
        tabIndex := -1,
        Menu.keyboardNav(anchor),
        build(ctx)
      )
    )

  /** The bar chrome alone, for call sites composing menus with [[menu]]. */
  def apply(mods: Modifier[HtmlElement]*): HtmlElement =
    div(dataAttr("slot") := "menubar", role := "menubar", cls := rootClasses, mods)

  /** The bar plus shared open state, so hovering across triggers switches menus. */
  def bar(mods: Modifier[HtmlElement]*)(build: Bar => Seq[Modifier[HtmlElement]]): HtmlElement =
    val shared = Bar()
    div(dataAttr("slot") := "menubar", role := "menubar", cls := rootClasses, mods, build(shared))

  /** A single menu with no shared bar state — its trigger still carries the flat menubar look. */
  def menu(trigger: Modifier[HtmlElement]*)(items: DropdownMenu.Item*): HtmlElement =
    Bar().menu(trigger*)(items*)

  def label(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "menubar-label",
      cls := "cn-menubar-label px-2 py-1.5 text-xs font-medium text-muted-foreground",
      mods
    )

  def separator(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "menubar-separator",
      role := "separator",
      cls := "cn-menubar-separator -mx-1 my-1 h-px bg-border",
      mods
    )

  def shortcut(mods: Modifier[HtmlElement]*): HtmlElement =
    span(
      dataAttr("slot") := "menubar-shortcut",
      cls := "cn-menubar-shortcut ml-auto text-xs tracking-widest text-muted-foreground",
      mods
    )

  def groupHeading(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "menubar-group-heading",
      cls := "px-2 py-1.5 text-sm font-medium data-[inset]:ps-8",
      mods
    )

  def group(mods: Modifier[HtmlElement]*): HtmlElement =
    div(dataAttr("slot") := "menubar-group", role := "group", mods)
