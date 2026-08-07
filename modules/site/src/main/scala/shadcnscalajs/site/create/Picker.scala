package shadcnscalajs.site.create

import com.raquo.laminar.api.L.*
import com.raquo.laminar.nodes.RootNode
import org.scalajs.dom

import scala.scalajs.js
import shadcnscalajs.ui.Icons

/** Create-page picker chrome — dropdown menus styled like shadcn-svelte's picker components. Built on the same
  * outside-click / Escape / arrow-key interaction model as `DropdownMenu`, without a floating portal primitive.
  */
object Picker:

  enum TriggerVariant derives CanEqual:
    case Field, Menu

  private final case class MenuEntry(index: Int, isDisabledNow: () => Boolean, selectAction: () => Unit)

  /** Shared open/focus state for one picker instance. Register menu rows via [[registerItem]] while building. */
  final class Root private[Picker] (
      private[Picker] val isOpen: Var[Boolean],
      private[Picker] val activeIndex: Var[Int],
      private val containerRef: Var[Option[dom.html.Element]],
      private[Picker] val triggerRef: Var[Option[dom.html.Element]],
      private[Picker] val menuRef: Var[Option[dom.html.Element]],
      private val entries: scala.collection.mutable.ArrayBuffer[MenuEntry]
  ):
    private var counter = 0

    private[Picker] def registerItem(isDisabledNow: () => Boolean, selectAction: () => Unit): Int =
      val idx = counter
      counter += 1
      entries += MenuEntry(idx, isDisabledNow, selectAction)
      idx

    private[Picker] def enabledIndices: List[Int] =
      entries.toList.collect { case MenuEntry(idx, isDisabledNow, _) if !isDisabledNow() => idx }

    private[Picker] def moveActive(delta: Int): Unit =
      val indices = enabledIndices
      if indices.nonEmpty then
        val currentPos = math.max(indices.indexOf(activeIndex.now()), 0)
        val nextPos = (((currentPos + delta) % indices.size) + indices.size) % indices.size
        activeIndex.set(indices(nextPos))

    private[Picker] def activateIndex(idx: Int, closeOnSelect: Boolean): Unit =
      entries.lift(idx).foreach { entry =>
        if !entry.isDisabledNow() then
          activeIndex.set(idx)
          entry.selectAction()
          if closeOnSelect then isOpen.set(false)
      }

    private[Picker] def close(): Unit = isOpen.set(false)

    private[Picker] def toggle(): Unit = isOpen.update(!_)

  def compPath(ev: dom.Event): js.Array[dom.EventTarget] =
    ev.asInstanceOf[js.Dynamic].composedPath().asInstanceOf[js.Array[dom.EventTarget]]

  def root(mods: Modifier[HtmlElement]*)(build: Root => HtmlElement): HtmlElement =
    val isOpen = Var(false)
    val activeIndex = Var(0)
    val containerRef = Var(Option.empty[dom.html.Element])
    val triggerRef = Var(Option.empty[dom.html.Element])
    val menuRef = Var(Option.empty[dom.html.Element])
    val entries = scala.collection.mutable.ArrayBuffer.empty[MenuEntry]
    val ctx = Root(isOpen, activeIndex, containerRef, triggerRef, menuRef, entries)

    div(
      mods,
      onMountBind { mountCtx =>
        containerRef.set(Some(mountCtx.thisNode.ref))
        documentEvents(_.onMouseDown) --> { (ev: dom.MouseEvent) =>
          // The menu lives in `document.body`, so it is not a descendant of this container and would
          // read as an outside click — closing on mousedown before the item's click ever lands.
          if isOpen.now() then
            val path = compPath(ev)
            val insidePicker = path.indexOf(mountCtx.thisNode.ref) != -1
            val insideMenu = menuRef.now().exists(menu => path.indexOf(menu) != -1)
            if !insidePicker && !insideMenu then isOpen.set(false)
        }
      },
      // Clicking the preview iframe never reaches this document, so the mousedown handler above
      // cannot see it and the menu would hang over the page. Losing window focus covers that case.
      windowEvents(_.onBlur) --> { _ => if isOpen.now() then isOpen.set(false) },
      onKeyDown --> { (ev: dom.KeyboardEvent) =>
        ev.key match
          case "ArrowDown" =>
            ev.preventDefault()
            if !isOpen.now() then isOpen.set(true) else ctx.moveActive(1)
          case "ArrowUp" =>
            ev.preventDefault()
            if !isOpen.now() then isOpen.set(true) else ctx.moveActive(-1)
          case "Enter" | " " if isOpen.now() =>
            ev.preventDefault()
            ctx.activateIndex(activeIndex.now(), closeOnSelect = true)
          case "Escape" =>
            isOpen.set(false)
          case _ => ()
      },
      build(ctx)
    )

  def trigger(
      ctx: Root,
      variant: TriggerVariant = TriggerVariant.Field,
      isTriggerDisabled: Signal[Boolean] = Val(false),
      mods: Modifier[HtmlElement]*
  )(children: Modifier[HtmlElement]*): HtmlElement =
    val variantClasses = variant match
      case TriggerVariant.Field =>
        "relative w-40 shrink-0 touch-manipulation rounded-xl p-3 ring-1 ring-foreground/10 select-none hover:bg-muted focus-visible:ring-foreground/50 focus-visible:outline-none disabled:opacity-50 data-[state=open]:bg-muted md:w-full md:rounded-lg md:px-2.5 md:py-2"
      case TriggerVariant.Menu =>
        // No resting ring: this trigger sits in the customizer's own bordered header, so an outline
        // here reads as a stray box around the label. The focus ring stays for keyboard users.
        "flex w-full items-center justify-between gap-2 rounded-lg px-1.75 focus-visible:ring-1 focus-visible:ring-foreground/50"

    button(
      typ := "button",
      dataAttr("slot") := "dropdown-menu-trigger",
      dataAttr("state") <-- ctx.isOpen.signal.map(open => if open then "open" else "closed"),
      cls := variantClasses,
      aria.hasPopup := true,
      aria.expanded <-- ctx.isOpen.signal,
      disabled <-- isTriggerDisabled,
      onMountCallback { mountCtx => ctx.triggerRef.set(Some(mountCtx.thisNode.ref)) },
      mods,
      children,
      onClick --> { _ => ctx.toggle() }
    )

  /** Places the open menu against the trigger's viewport rect, right edges aligned, flipping above the trigger when
    * there is no room below and clamping to the viewport so it can never run off-screen.
    */
  private def place(ctx: Root): Unit =
    for
      trigger <- ctx.triggerRef.now()
      menu <- ctx.menuRef.now()
    do
      val gap = 8.0
      val margin = 8.0
      val rect = trigger.getBoundingClientRect()
      val viewportWidth = dom.window.innerWidth.toDouble
      val viewportHeight = dom.window.innerHeight.toDouble
      val menuWidth = menu.offsetWidth.toDouble
      val menuHeight = menu.offsetHeight.toDouble
      val left = math.max(margin, math.min(rect.right - menuWidth, viewportWidth - margin - menuWidth))
      val below = rect.bottom + gap
      val above = rect.top - gap - menuHeight
      val top =
        if below + menuHeight <= viewportHeight - margin then below
        else if above >= margin then above
        else math.max(margin, viewportHeight - margin - menuHeight)
      menu.style.left = s"${left}px"
      menu.style.top = s"${top}px"

  /** The menu is rendered into `document.body` instead of inline. Inline it is clipped by three ancestors — the
    * customizer's scroll area plus two `overflow-hidden` wrappers — and the card's `backdrop-blur` makes it a
    * containing block for fixed descendants, so even `position: fixed` resolves against the card and stays clipped. No
    * z-index can lift an element out of an `overflow` ancestor; only leaving that subtree works, after which the menu
    * is positioned by [[place]].
    */
  def content(ctx: Root, mods: Modifier[HtmlElement]*)(children: Modifier[HtmlElement]*): HtmlElement =
    var scrollListener = Option.empty[js.Function1[dom.Event, Unit]]

    val menu = div(
      dataAttr("slot") := "dropdown-menu-content",
      cls := "cn-menu-target fixed top-0 left-0 z-50 max-h-96 min-w-32 w-52 overflow-x-hidden overflow-y-auto rounded-xl border-0 bg-neutral-950 p-1.5 text-neutral-100 shadow-lg ring-1 ring-neutral-950/80 outline-none dark:bg-neutral-800 dark:ring-neutral-700/50",
      display <-- ctx.isOpen.signal.map(open => if open then "block" else "none"),
      aria.hidden <-- ctx.isOpen.signal.map(!_),
      mods,
      div(role := "menu", children),
      // Measure after the browser has applied `display: block`, otherwise the menu is still zero-sized
      // and every flip/clamp decision below is made against the wrong height.
      ctx.isOpen.signal --> { open =>
        if open then dom.window.requestAnimationFrame(_ => place(ctx))
      },
      windowEvents(_.onResize) --> { _ => if ctx.isOpen.now() then place(ctx) },
      onMountUnmountCallback(
        mount = { mountCtx =>
          ctx.menuRef.set(Some(mountCtx.thisNode.ref))
          // Capture phase: the customizer scrolls in its own container and `scroll` does not bubble.
          val listener: js.Function1[dom.Event, Unit] = _ => if ctx.isOpen.now() then place(ctx)
          scrollListener = Some(listener)
          dom.document.addEventListener("scroll", listener, useCapture = true)
        },
        unmount = { _ =>
          scrollListener.foreach(dom.document.removeEventListener("scroll", _, useCapture = true))
          scrollListener = None
          ctx.menuRef.set(None)
        }
      )
    )

    var portal = Option.empty[RootNode]
    div(
      cls := "hidden",
      onMountUnmountCallback(
        mount = _ => portal = Some(render(dom.document.body, menu)),
        unmount = _ =>
          portal.foreach(_.unmount())
          portal = None
      )
    )

  def group(mods: Modifier[HtmlElement]*)(children: Modifier[HtmlElement]*): HtmlElement =
    div(role := "group", cls := "flex flex-col", mods, children)

  def label(labelText: String, mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "dropdown-menu-label",
      cls := "px-2 py-1.5 text-xs font-medium text-neutral-400",
      mods,
      labelText
    )

  def separator(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      role := "separator",
      aria.hidden := true,
      cls := "my-1 h-px bg-neutral-700/80",
      mods
    )

  def shortcut(shortcutText: String, mods: Modifier[HtmlElement]*): HtmlElement =
    span(
      dataAttr("slot") := "dropdown-menu-shortcut",
      cls := "ml-auto text-xs tracking-widest text-neutral-400 group-focus/dropdown-menu-item:text-neutral-100",
      mods,
      shortcutText
    )

  def item(
      ctx: Root,
      selectAction: () => Unit,
      isItemDisabled: Signal[Boolean] = Val(false),
      closeOnSelect: Boolean = true,
      mods: Modifier[HtmlElement]*
  )(children: Modifier[HtmlElement]*): HtmlElement =
    // Mirror into a Var so sync keyboard/click paths can read .now() (Signal.now is protected).
    val disabledVar = Var(false)
    val idx = ctx.registerItem(() => disabledVar.now(), selectAction)
    div(
      role := "menuitem",
      dataAttr("slot") := "dropdown-menu-item",
      onMountBind { _ => isItemDisabled --> disabledVar.writer },
      cls <-- ctx.activeIndex.signal
        .combineWith(isItemDisabled)
        .map { (activeIdx, itemDisabled) =>
          val active = !itemDisabled && activeIdx == idx
          s"group/dropdown-menu-item relative flex cursor-default items-center gap-2 rounded-lg px-2 py-1.5 text-sm font-medium outline-hidden select-none focus:bg-neutral-600 focus:text-neutral-100 dark:focus:bg-neutral-700/80 pointer-coarse:gap-3 pointer-coarse:py-2.5 pointer-coarse:pl-3 pointer-coarse:text-base data-disabled:pointer-events-none data-disabled:opacity-50 ${if active then "bg-neutral-600 text-neutral-100 dark:bg-neutral-700/80" else ""}"
        },
      aria.disabled <-- isItemDisabled,
      mods,
      children,
      onClick --> { _ =>
        if !disabledVar.now() then ctx.activateIndex(idx, closeOnSelect)
      }
    )

  def radioItem(
      ctx: Root,
      itemValue: String,
      selected: Signal[String],
      onSelect: String => Unit,
      isItemDisabled: Signal[Boolean] = Val(false),
      closeOnSelect: Boolean = false,
      hideIndicator: Boolean = false,
      mods: Modifier[HtmlElement]*
  )(children: Modifier[HtmlElement]*): HtmlElement =
    val selectAction = () => onSelect(itemValue)
    val disabledVar = Var(false)
    val idx = ctx.registerItem(() => disabledVar.now(), selectAction)
    div(
      role := "menuitemradio",
      dataAttr("slot") := "dropdown-menu-radio-item",
      onMountBind { _ => isItemDisabled --> disabledVar.writer },
      aria.checked <-- selected.map(sel => if sel == itemValue then "true" else "false"),
      cls <-- ctx.activeIndex.signal
        .combineWith(isItemDisabled)
        .map { (activeIdx, itemDisabled) =>
          val active = !itemDisabled && activeIdx == idx
          s"relative flex cursor-default items-center gap-2 rounded-lg py-1.5 pr-8 pl-2 text-sm font-medium outline-hidden select-none focus:bg-neutral-600 focus:text-neutral-100 dark:focus:bg-neutral-700/80 pointer-coarse:gap-3 pointer-coarse:py-2.5 pointer-coarse:pl-3 pointer-coarse:text-base data-disabled:pointer-events-none data-disabled:opacity-50 ${if active then "bg-neutral-600 text-neutral-100 dark:bg-neutral-700/80" else ""}"
        },
      aria.disabled <-- isItemDisabled,
      mods,
      if !hideIndicator then
        span(
          cls := "pointer-events-none absolute right-2 flex items-center justify-center",
          dataAttr("slot") := "dropdown-menu-radio-item-indicator",
          child.maybe <-- selected.map(sel => if sel == itemValue then Some(Icons.check()) else None)
        )
      else emptyNode,
      children,
      onClick --> { _ =>
        if !disabledVar.now() then ctx.activateIndex(idx, closeOnSelect)
      }
    )
