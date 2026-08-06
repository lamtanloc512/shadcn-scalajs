package shadcnscalajs.ui

import com.raquo.laminar.api.L.*
import org.scalajs.dom
import shadcnscalajs.core.CommonAttrs.openAttr

import scala.scalajs.js

/** shadcn/ui Combobox — a `Popover` trigger + `Command` list, matching the canonical combobox.tsx demo (searchable
  * list, checkmark on the selected item, trigger label mirrors the selection, closes on select/outside-click/Escape).
  * Built on a native `<details>`/`<summary>` element like `Popover`/`Accordion`, but the native toggle is fully
  * overridden (`preventDefault` + `openAttr <-- openVar.signal`, same trick as `Accordion.scala`) since a plain
  * `<details>` has no way to close itself on selection or on an outside click.
  */
object Combobox:

  final case class Item(value: String, label: String)

  // No height/padding here on purpose: `trigger` (fixed `h-9`) and `triggerMultiple` (chips wrap to multiple lines,
  // so `min-h-9`) need different box-model classes, and stuffing both into one shared string risks whichever one
  // isn't actually wanted for a given call site still winning based on generated-CSS order, not source order.
  private val triggerBase =
    "inline-flex w-full items-center justify-between gap-2 rounded-md border bg-background shadow-xs outline-none transition-all hover:bg-accent hover:text-accent-foreground focus-visible:border-ring focus-visible:ring-[3px] focus-visible:ring-ring/50 disabled:pointer-events-none disabled:opacity-50 dark:border-input dark:bg-input/30 dark:hover:bg-input/50 [&_svg]:pointer-events-none [&_svg]:shrink-0 [&_svg:not([class*='size-'])]:size-4"

  def apply(
      selectedVar: Var[Option[String]],
      items: Seq[Item],
      placeholder: String = "Select option…",
      searchPlaceholder: String = "Search…",
      emptyText: String = "No results found.",
      mods: Modifier[HtmlElement]*
  ): HtmlElement =
    val openVar = Var(false)
    val searchVar = Var("")
    val activeIndexVar = Var(-1)
    val triggerRef = Var(Option.empty[dom.html.Element])

    def visibleItems(search: String): Seq[Item] =
      val q = search.trim.toLowerCase
      if q.isEmpty then items else items.filter(_.label.toLowerCase.contains(q))

    def closePopover(focusTrigger: Boolean): Unit =
      openVar.set(false)
      activeIndexVar.set(-1)
      if focusTrigger then triggerRef.now().foreach(_.focus())

    // Chrome re-asserts focus on the clicked `<summary>` after the click event finishes dispatching (even past a
    // deferred macrotask), which clobbers any attempt to focus the search input from here — verified against a real
    // browser, not just reasoned about. So the search input only gets focus if the user explicitly clicks/tabs into
    // it; opening never tries to steal focus onto it.
    def openPopover(): Unit = openVar.set(true)

    def selectValue(value: String): Unit =
      selectedVar.set(if selectedVar.now().contains(value) then None else Some(value))
      searchVar.set("")
      closePopover(focusTrigger = true)

    detailsTag(
      openAttr <-- openVar.signal,
      // `cn-` prefix, not `combobox`: legacy basecoat CSS (still loaded site-wide for other components, see
      // vendor/NOTICE.md) scopes several rules under a literal `.combobox` ancestor class — including one that
      // injects a phantom "No results found" via `::before` whenever a `[role=listbox]` descendant has no
      // `[data-value]` children, which this markup never sets. Same reason `Accordion`/`Button`/`Input` use `cn-*`.
      cls := "cn-combobox relative inline-block",
      onMountBind { ctx =>
        documentEvents(_.onMouseDown) --> { (ev: dom.MouseEvent) =>
          if openVar.now() && compPath(ev).indexOf(ctx.thisNode.ref) == -1 then closePopover(focusTrigger = false)
        }
      },
      // Bound here rather than on the search input alone: right after opening, focus sits on the `<summary>`
      // trigger (see `trigger`'s onClick), not the input, and keydown bubbles up to this root either way.
      onKeyDown --> { (ev: dom.KeyboardEvent) => if ev.key == "Escape" then closePopover(focusTrigger = true) },
      trigger(
        openVar,
        selectedVar,
        items,
        placeholder,
        triggerRef,
        openPopover,
        () => closePopover(focusTrigger = false)
      ),
      content(searchVar, activeIndexVar, selectedVar, items, searchPlaceholder, emptyText, visibleItems, selectValue),
      mods
    )

  /** Multi-select variant — same `Popover` + `Command` shell as `apply`, but `selectedVar` is a `Set` (toggling a value
    * doesn't close the popover, so several can be picked in one sitting) and the trigger shows each pick as a removable
    * chip, mirroring `Accordion.apply`/`Accordion.multiple`'s single/multi split.
    */
  def multiple(
      selectedVar: Var[Set[String]],
      items: Seq[Item],
      placeholder: String = "Select options…",
      searchPlaceholder: String = "Search…",
      emptyText: String = "No results found.",
      mods: Modifier[HtmlElement]*
  ): HtmlElement =
    val openVar = Var(false)
    val searchVar = Var("")
    val activeIndexVar = Var(-1)
    val triggerRef = Var(Option.empty[dom.html.Element])

    def visibleItems(search: String): Seq[Item] =
      val q = search.trim.toLowerCase
      if q.isEmpty then items else items.filter(_.label.toLowerCase.contains(q))

    def closePopover(focusTrigger: Boolean): Unit =
      openVar.set(false)
      activeIndexVar.set(-1)
      if focusTrigger then triggerRef.now().foreach(_.focus())

    def openPopover(): Unit = openVar.set(true)

    def toggleValue(value: String): Unit =
      selectedVar.update(s => if s.contains(value) then s - value else s + value)
      searchVar.set("")
      activeIndexVar.set(-1)

    def removeValue(value: String): Unit = selectedVar.update(_ - value)

    detailsTag(
      openAttr <-- openVar.signal,
      cls := "cn-combobox relative inline-block",
      onMountBind { ctx =>
        documentEvents(_.onMouseDown) --> { (ev: dom.MouseEvent) =>
          if openVar.now() && compPath(ev).indexOf(ctx.thisNode.ref) == -1 then closePopover(focusTrigger = false)
        }
      },
      onKeyDown --> { (ev: dom.KeyboardEvent) => if ev.key == "Escape" then closePopover(focusTrigger = true) },
      triggerMultiple(
        openVar,
        selectedVar,
        items,
        placeholder,
        triggerRef,
        openPopover,
        () => closePopover(focusTrigger = false),
        removeValue
      ),
      contentMultiple(
        searchVar,
        activeIndexVar,
        selectedVar,
        items,
        searchPlaceholder,
        emptyText,
        visibleItems,
        toggleValue
      ),
      mods
    )

  private def compPath(ev: dom.Event): js.Array[dom.EventTarget] =
    ev.asInstanceOf[js.Dynamic].composedPath().asInstanceOf[js.Array[dom.EventTarget]]

  private def trigger(
      openVar: Var[Boolean],
      selectedVar: Var[Option[String]],
      items: Seq[Item],
      placeholder: String,
      triggerRef: Var[Option[dom.html.Element]],
      openPopover: () => Unit,
      closePopover: () => Unit
  ): HtmlElement =
    summaryTag(
      cls := s"cn-combobox-trigger cursor-pointer list-none h-9 px-3 py-2 text-sm $triggerBase",
      role := "combobox",
      aria.hasPopup := true,
      aria.expanded <-- openVar.signal,
      onMountCallback { ctx => triggerRef.set(Some(ctx.thisNode.ref)) },
      onClick --> { ev =>
        ev.preventDefault()
        if openVar.now() then closePopover() else openPopover()
      },
      span(
        cls := "truncate",
        child.text <-- selectedVar.signal.map(sel =>
          sel.flatMap(v => items.find(_.value == v)).map(_.label).getOrElse(placeholder)
        )
      ),
      Icons.chevronsUpDown()
    )

  private def content(
      searchVar: Var[String],
      activeIndexVar: Var[Int],
      selectedVar: Var[Option[String]],
      items: Seq[Item],
      searchPlaceholder: String,
      emptyText: String,
      visibleItems: String => Seq[Item],
      selectValue: String => Unit
  ): HtmlElement =
    div(
      cls := "popover-content absolute z-50 mt-2 w-full min-w-[12rem] overflow-hidden rounded-md border bg-popover p-0 text-popover-foreground shadow-md outline-none",
      Command(
        Command.input(
          placeholder := searchPlaceholder,
          value <-- searchVar.signal,
          onInput --> { ev =>
            searchVar.set(ev.target.asInstanceOf[dom.html.Input].value)
            activeIndexVar.set(-1)
          },
          onKeyDown --> { (ev: dom.KeyboardEvent) =>
            val visible = visibleItems(searchVar.now())
            ev.key match
              case "ArrowDown" if visible.nonEmpty =>
                ev.preventDefault()
                activeIndexVar.set((activeIndexVar.now() + 1) % visible.size)
              case "ArrowUp" if visible.nonEmpty =>
                ev.preventDefault()
                activeIndexVar.set(((activeIndexVar.now() - 1) % visible.size + visible.size) % visible.size)
              case "Enter" =>
                ev.preventDefault()
                val idx = activeIndexVar.now()
                if idx >= 0 && idx < visible.size then selectValue(visible(idx).value)
                else if visible.size == 1 then selectValue(visible.head.value)
              case _ => ()
          }
        ),
        Command.list(
          role := "listbox",
          children <-- searchVar.signal
            .combineWith(selectedVar.signal, activeIndexVar.signal)
            .map { case (search, selected, activeIndex) =>
              val visible = visibleItems(search)
              if visible.isEmpty then List(Command.empty(emptyText))
              else
                visible.zipWithIndex.map { case (it, idx) =>
                  optionItem(it, selected.contains(it.value), idx == activeIndex, selectValue)
                }.toList
            }
        )
      )
    )

  private def triggerMultiple(
      openVar: Var[Boolean],
      selectedVar: Var[Set[String]],
      items: Seq[Item],
      placeholder: String,
      triggerRef: Var[Option[dom.html.Element]],
      openPopover: () => Unit,
      closePopover: () => Unit,
      removeValue: String => Unit
  ): HtmlElement =
    summaryTag(
      cls := s"cn-combobox-trigger cursor-pointer list-none min-h-9 px-3 py-1.5 text-sm $triggerBase",
      role := "combobox",
      aria.hasPopup := true,
      aria.expanded <-- openVar.signal,
      onMountCallback { ctx => triggerRef.set(Some(ctx.thisNode.ref)) },
      onClick --> { ev =>
        ev.preventDefault()
        if openVar.now() then closePopover() else openPopover()
      },
      div(
        cls := "flex flex-1 flex-wrap items-center gap-1",
        children <-- selectedVar.signal.map { selected =>
          if selected.isEmpty then List(span(cls := "truncate text-muted-foreground", placeholder))
          else items.filter(it => selected.contains(it.value)).map(it => chip(it, () => removeValue(it.value))).toList
        }
      ),
      Icons.chevronsUpDown()
    )

  private def contentMultiple(
      searchVar: Var[String],
      activeIndexVar: Var[Int],
      selectedVar: Var[Set[String]],
      items: Seq[Item],
      searchPlaceholder: String,
      emptyText: String,
      visibleItems: String => Seq[Item],
      toggleValue: String => Unit
  ): HtmlElement =
    div(
      cls := "popover-content absolute z-50 mt-2 w-full min-w-[12rem] overflow-hidden rounded-md border bg-popover p-0 text-popover-foreground shadow-md outline-none",
      Command(
        Command.input(
          placeholder := searchPlaceholder,
          value <-- searchVar.signal,
          onInput --> { ev =>
            searchVar.set(ev.target.asInstanceOf[dom.html.Input].value)
            activeIndexVar.set(-1)
          },
          onKeyDown --> { (ev: dom.KeyboardEvent) =>
            val visible = visibleItems(searchVar.now())
            ev.key match
              case "ArrowDown" if visible.nonEmpty =>
                ev.preventDefault()
                activeIndexVar.set((activeIndexVar.now() + 1) % visible.size)
              case "ArrowUp" if visible.nonEmpty =>
                ev.preventDefault()
                activeIndexVar.set(((activeIndexVar.now() - 1) % visible.size + visible.size) % visible.size)
              case "Enter" =>
                ev.preventDefault()
                val idx = activeIndexVar.now()
                if idx >= 0 && idx < visible.size then toggleValue(visible(idx).value)
                else if visible.size == 1 then toggleValue(visible.head.value)
              case _ => ()
          }
        ),
        Command.list(
          role := "listbox",
          aria.multiSelectable := true,
          children <-- searchVar.signal
            .combineWith(selectedVar.signal, activeIndexVar.signal)
            .map { case (search, selected, activeIndex) =>
              val visible = visibleItems(search)
              if visible.isEmpty then List(Command.empty(emptyText))
              else
                visible.zipWithIndex.map { case (it, idx) =>
                  optionItem(it, selected.contains(it.value), idx == activeIndex, toggleValue)
                }.toList
            }
        )
      )
    )

  private def chip(item: Item, onRemove: () => Unit): HtmlElement =
    Badge.of(
      _.variant(Badge.Variant.Secondary),
      _ => span(cls := "truncate", item.label),
      _ =>
        button(
          typ := "button",
          cls := "inline-flex size-3.5 shrink-0 items-center justify-center rounded-full hover:bg-foreground/10",
          aria.label := s"Remove ${item.label}",
          // `preventDefault` blocks the native details-toggle activation behavior a click inside the `<summary>`
          // would otherwise trigger; `stopPropagation` keeps it from also reaching the trigger's own onClick.
          onClick --> { ev =>
            ev.preventDefault()
            ev.stopPropagation()
            onRemove()
          },
          Icons.x()
        )
    )

  private def optionItem(
      it: Item,
      isSelected: Boolean,
      isActive: Boolean,
      selectValue: String => Unit
  ): HtmlElement =
    Command.item(
      cls := (if isActive then "bg-accent text-accent-foreground" else ""),
      aria.selected := isSelected,
      onClick --> { _ => selectValue(it.value) },
      span(cls := "flex-1 truncate", it.label),
      Icons.check(svg.cls := (if isSelected then "ml-auto opacity-100" else "ml-auto opacity-0"))
    )
