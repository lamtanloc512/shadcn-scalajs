package shadcnscalajs.ui

import com.raquo.laminar.api.L.*
import org.scalajs.dom

/** Shared menu surface behind `DropdownMenu`, `ContextMenu`, and `Menubar`.
  *
  * Upstream's three menus are the same component with different triggers and a different `data-slot` prefix, so the
  * class strings and interaction model live here once and each component supplies its prefix.
  *
  * Keyboard navigation reads the item list out of the DOM rather than from a Scala list. With composable children the
  * component cannot know its items up front, and querying `[role^=menuitem]` is also what keeps focus behaving when
  * items are added, removed, or nested in groups.
  */
object Menu:

  def contentClass(prefix: String): String =
    s"cn-$prefix-content cn-$prefix-content-logical cn-menu-target cn-menu-translucent z-50 min-w-48 overflow-x-hidden overflow-y-auto rounded-md bg-popover p-1 text-popover-foreground shadow-md outline-none ring-1 ring-foreground/10 duration-100 data-closed:overflow-hidden data-open:animate-in data-open:fade-in-0 data-open:zoom-in-95 data-closed:animate-out data-closed:fade-out-0 data-closed:zoom-out-95"

  /** Submenu panels get their own hook classes (`sub-content`, `subcontent`) and a narrower minimum width than the root
    * panel, matching upstream's `SubContent`.
    */
  def subContentClass(prefix: String): String =
    s"cn-$prefix-sub-content cn-$prefix-subcontent cn-menu-target cn-menu-translucent z-50 min-w-32 overflow-hidden rounded-md bg-popover p-1 text-popover-foreground shadow-lg outline-none ring-1 ring-foreground/10 duration-100 data-open:animate-in data-open:fade-in-0 data-open:zoom-in-95 data-closed:animate-out data-closed:fade-out-0 data-closed:zoom-out-95"

  def itemClass(prefix: String): String =
    s"cn-$prefix-item group/$prefix-item relative flex cursor-default items-center gap-2 rounded-sm px-2 py-1.5 text-sm outline-hidden select-none focus:bg-accent focus:text-accent-foreground data-[inset]:pl-8 data-disabled:pointer-events-none data-disabled:opacity-50 data-[variant=destructive]:text-destructive data-[variant=destructive]:focus:bg-destructive/10 data-[variant=destructive]:focus:text-destructive dark:data-[variant=destructive]:focus:bg-destructive/20 data-[variant=destructive]:*:[svg]:text-destructive! [&_svg]:pointer-events-none [&_svg]:shrink-0 [&_svg:not([class*='size-'])]:size-4"

  /** Upstream's `variant="destructive"` row. The colors live in [[itemClass]] behind `data-[variant=destructive]`, both
    * so the pack rules that key off the same attribute line up and so the modifier stays a single attribute.
    */
  val destructive: Modifier[HtmlElement] = dataAttr("variant") := "destructive"

  /** Upstream's `inset` row: extra leading padding so a label lines up with rows that have an indicator. */
  val inset: Modifier[HtmlElement] = dataAttr("inset") := ""

  /** Checkbox and radio rows reserve trailing space for the indicator. */
  def indicatorItemClass(prefix: String, part: String): String =
    s"cn-$prefix-$part relative flex cursor-default items-center gap-2 rounded-sm py-1.5 pe-8 ps-2 text-sm outline-hidden select-none focus:bg-accent focus:text-accent-foreground data-disabled:pointer-events-none data-disabled:opacity-50 [&_svg]:pointer-events-none [&_svg]:shrink-0 [&_svg:not([class*='size-'])]:size-4"

  /** One menu instance: the anchor it opens against, plus the `data-slot` prefix its parts carry. */
  final class Ctx private[ui] (private[ui] val anchor: Floating.Anchor, private[ui] val prefix: String):
    def close(): Unit =
      anchor.close()
      anchor.focusTrigger()

    private def itemBase(part: String, itemClasses: String, onSelect: Option[() => Unit], closeOnSelect: Boolean)(
        mods: Seq[Modifier[HtmlElement]]
    ): HtmlElement =
      div(
        dataAttr("slot") := s"$prefix-$part",
        role := (if part == "checkbox-item" then "menuitemcheckbox"
                 else if part == "radio-item" then "menuitemradio"
                 else "menuitem"),
        tabIndex := -1,
        cls := itemClasses,
        // Pointer movement drives focus, which is what the `focus:bg-accent` highlight and upstream's roving
        // tabindex both key off; hovering a row must therefore also move focus to it.
        onPointerEnter --> { (ev: dom.PointerEvent) => ev.currentTarget.asInstanceOf[dom.html.Element].focus() },
        onClick --> { _ =>
          onSelect.foreach(_())
          if closeOnSelect then close()
        },
        mods
      )

    def item(mods: Modifier[HtmlElement]*): HtmlElement =
      itemBase("item", itemClass(prefix), None, closeOnSelect = true)(mods)

    def item(onSelect: () => Unit, mods: Modifier[HtmlElement]*): HtmlElement =
      itemBase("item", itemClass(prefix), Some(onSelect), closeOnSelect = true)(mods)

    def checkboxItem(checked: Var[Boolean], mods: Modifier[HtmlElement]*): HtmlElement =
      itemBase(
        "checkbox-item",
        indicatorItemClass(prefix, "checkbox-item"),
        Some(() => checked.update(!_)),
        closeOnSelect = false
      )(
        Seq[Modifier[HtmlElement]](
          aria.checked <-- checked.signal.map(_.toString),
          indicator(checked.signal),
          mods
        )
      )

    def checkboxItem(checked: Signal[Boolean], onSelect: () => Unit, mods: Modifier[HtmlElement]*): HtmlElement =
      itemBase("checkbox-item", indicatorItemClass(prefix, "checkbox-item"), Some(onSelect), closeOnSelect = false)(
        Seq[Modifier[HtmlElement]](aria.checked <-- checked.map(_.toString), indicator(checked), mods)
      )

    def radioItem(selected: Var[String], itemValue: String, mods: Modifier[HtmlElement]*): HtmlElement =
      itemBase(
        "radio-item",
        indicatorItemClass(prefix, "radio-item"),
        Some(() => selected.set(itemValue)),
        closeOnSelect = true
      )(
        Seq[Modifier[HtmlElement]](
          aria.checked <-- selected.signal.map(v => (v == itemValue).toString),
          indicator(selected.signal.map(_ == itemValue)),
          mods
        )
      )

    private def indicator(checked: Signal[Boolean]): HtmlElement =
      span(
        dataAttr("slot") := s"$prefix-item-indicator",
        cls := s"cn-$prefix-item-indicator pointer-events-none absolute end-2 flex size-3.5 items-center justify-center",
        cls("invisible") <-- checked.map(!_),
        Icons.check(svg.cls := "size-4")
      )

    def label(mods: Modifier[HtmlElement]*): HtmlElement =
      div(
        dataAttr("slot") := s"$prefix-label",
        cls := s"cn-$prefix-label px-2 py-1.5 text-xs font-medium text-muted-foreground data-[inset]:pl-8",
        mods
      )

    def separator(mods: Modifier[HtmlElement]*): HtmlElement =
      div(
        dataAttr("slot") := s"$prefix-separator",
        role := "separator",
        cls := s"cn-$prefix-separator -mx-1 my-1 h-px bg-border",
        mods
      )

    def shortcut(mods: Modifier[HtmlElement]*): HtmlElement =
      span(
        dataAttr("slot") := s"$prefix-shortcut",
        cls := s"cn-$prefix-shortcut ml-auto text-xs tracking-widest text-muted-foreground",
        mods
      )

    def group(mods: Modifier[HtmlElement]*): HtmlElement =
      div(dataAttr("slot") := s"$prefix-group", role := "group", mods)

    def groupHeading(mods: Modifier[HtmlElement]*): HtmlElement =
      div(
        dataAttr("slot") := s"$prefix-group-heading",
        cls := s"cn-$prefix-group-heading px-2 py-1.5 text-sm font-semibold data-[inset]:ps-8",
        mods
      )

    /** A submenu: its trigger is an item that opens a second panel to the inline end on hover or click. Selecting
      * inside the submenu closes the whole stack, which is why the child context closes this one too.
      */
    def sub(triggerMods: Modifier[HtmlElement]*)(build: Ctx => Seq[Modifier[HtmlElement]]): HtmlElement =
      val subAnchor = Floating.anchor()
      val subCtx = Ctx(subAnchor, prefix)
      val parent = this
      anchor.registerNested(subAnchor)
      div(
        cls := "contents",
        div(
          dataAttr("slot") := s"$prefix-sub-trigger",
          role := "menuitem",
          tabIndex := -1,
          aria.hasPopup := true,
          cls := s"cn-$prefix-sub-trigger ${itemClass(prefix)} data-[state=open]:bg-accent data-[state=open]:text-accent-foreground",
          Floating.triggerBase(subAnchor),
          onPointerEnter --> { (ev: dom.PointerEvent) =>
            ev.currentTarget.asInstanceOf[dom.html.Element].focus()
            subAnchor.open()
          },
          onClick.stopPropagation --> { _ => subAnchor.toggle() },
          triggerMods,
          Icons.chevronRight(svg.cls := "ms-auto size-4")
        ),
        Floating.content(
          subAnchor,
          Floating.Placement(side = Floating.Side.Right, align = Floating.Align.Start, sideOffset = -4.0),
          subContentClass(prefix)
        )(
          dataAttr("slot") := s"$prefix-sub-content",
          role := "menu",
          keyboardNav(subAnchor),
          // Leaving the submenu panel closes it, but only it — the parent stays open.
          onPointerLeave --> { _ => subAnchor.close() },
          onClick --> { _ => parent.close() },
          build(subCtx)
        )
      )

  /** Arrow/Home/End/Enter navigation over whatever items are currently in the panel. */
  private[ui] def keyboardNav(a: Floating.Anchor): Modifier[HtmlElement] =
    def items(panel: dom.html.Element): List[dom.html.Element] =
      panel
        .querySelectorAll("[role^='menuitem']:not([data-disabled])")
        .toList
        .collect { case el: dom.html.Element => el }

    def focusAt(panel: dom.html.Element, index: Int): Unit =
      val list = items(panel)
      if list.nonEmpty then
        val bounded = ((index % list.size) + list.size) % list.size
        list(bounded).focus()

    def move(panel: dom.html.Element, delta: Int): Unit =
      val list = items(panel)
      val current = list.indexWhere(_ == dom.document.activeElement)
      focusAt(panel, if current < 0 then (if delta > 0 then 0 else list.size - 1) else current + delta)

    Seq(
      onKeyDown --> { (ev: dom.KeyboardEvent) =>
        a.contentRef.now().foreach { panel =>
          ev.key match
            case "ArrowDown" =>
              ev.preventDefault()
              move(panel, 1)
            case "ArrowUp" =>
              ev.preventDefault()
              move(panel, -1)
            case "Home" =>
              ev.preventDefault()
              focusAt(panel, 0)
            case "End" =>
              ev.preventDefault()
              focusAt(panel, items(panel).size - 1)
            case "Enter" | " " =>
              ev.preventDefault()
              dom.document.activeElement match
                case el: dom.html.Element => el.click()
                case _                    => ()
            case _ => ()
        }
      },
      // Opening with the keyboard should land on the first item, which also moves focus into the portaled panel so the
      // key handler above receives events at all.
      onMountBind { _ =>
        a.isOpen.signal --> { open =>
          if open then
            dom.window.requestAnimationFrame { _ =>
              a.contentRef.now().foreach(panel => items(panel).headOption.fold(panel.focus())(_.focus()))
            }
        }
      }
    )
