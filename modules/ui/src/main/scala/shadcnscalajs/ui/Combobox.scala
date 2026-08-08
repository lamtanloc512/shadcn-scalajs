package shadcnscalajs.ui

import com.raquo.laminar.api.L.*

/** shadcn/ui Combobox — a [[Popover]] trigger over a [[Command]] list, matching the canonical combobox demo: searchable
  * list, checkmark on the selected item, trigger label mirrors the selection, closes on select/outside click/Escape.
  *
  * The searching, highlighting, and Arrow/Enter handling all come from `Command` now, so this file only owns selection
  * and the trigger's label. The previous version was a `<details>`/`<summary>` element with its own copy of the filter
  * and keyboard logic, and with an absolutely positioned panel that could not escape an `overflow` ancestor or flip
  * when short of room.
  */
object Combobox:

  final case class Item(value: String, label: String)

  // No height/padding here on purpose: `trigger` (fixed `h-9`) and the multi-select trigger (chips wrap to multiple
  // lines, so `min-h-9`) need different box-model classes, and stuffing both into one shared string risks whichever one
  // isn't actually wanted for a given call site still winning based on generated-CSS order, not source order.
  private val triggerBase =
    "inline-flex w-full items-center justify-between gap-2 rounded-md border bg-background shadow-xs outline-none transition-all hover:bg-accent hover:text-accent-foreground focus-visible:border-ring focus-visible:ring-[3px] focus-visible:ring-ring/50 disabled:pointer-events-none disabled:opacity-50 dark:border-input dark:bg-input/30 dark:hover:bg-input/50 [&_svg]:pointer-events-none [&_svg]:shrink-0 [&_svg:not([class*='size-'])]:size-4"

  private val panelBoxClass = "w-full min-w-48 overflow-hidden p-0!"

  def apply(
      selectedVar: Var[Option[String]],
      items: Seq[Item],
      placeholder: String = "Select option…",
      searchPlaceholder: String = "Search…",
      emptyText: String = "No results found.",
      mods: Modifier[HtmlElement]*
  ): HtmlElement =
    val anchor = Floating.anchor()

    def select(value: String): Unit =
      selectedVar.set(if selectedVar.now().contains(value) then None else Some(value))
      anchor.close()
      anchor.focusTrigger()

    div(
      dataAttr("slot") := "combobox",
      // `cn-` prefix, not `combobox`: legacy basecoat CSS (still loaded site-wide for other components, see
      // vendor/NOTICE.md) scopes several rules under a literal `.combobox` ancestor class — including one that injects a
      // phantom "No results found" via `::before` whenever a `[role=listbox]` descendant has no `[data-value]` children.
      cls := "cn-combobox relative inline-block w-full",
      mods,
      Popover.withAnchor(anchor)(
        Popover.trigger(
          cls := s"cn-combobox-trigger h-9 px-3 py-2 text-sm $triggerBase",
          role := "combobox",
          span(
            cls := "truncate",
            child.text <-- selectedVar.signal.map(sel =>
              sel.flatMap(v => items.find(_.value == v)).map(_.label).getOrElse(placeholder)
            )
          ),
          Icons.chevronsUpDown()
        ),
        Popover.content(
          Floating.Placement(align = Floating.Align.Start, matchTriggerWidth = true),
          panelBoxClass,
          list(items, searchPlaceholder, emptyText, selectedVar.signal.map(_.toSet), select)
        )
      )
    )

  /** Multi-select variant — toggling a value keeps the panel open so several can be picked in one sitting, and the
    * trigger shows each pick as a removable chip. Mirrors `Accordion.apply`/`Accordion.multiple`'s single/multi split.
    */
  def multiple(
      selectedVar: Var[Set[String]],
      items: Seq[Item],
      placeholder: String = "Select options…",
      searchPlaceholder: String = "Search…",
      emptyText: String = "No results found.",
      mods: Modifier[HtmlElement]*
  ): HtmlElement =
    val anchor = Floating.anchor()

    def toggle(value: String): Unit =
      selectedVar.update(picked => if picked.contains(value) then picked - value else picked + value)

    div(
      dataAttr("slot") := "combobox",
      cls := "cn-combobox relative inline-block w-full",
      mods,
      Popover.withAnchor(anchor)(
        Popover.trigger(
          cls := s"cn-combobox-trigger min-h-9 px-3 py-1.5 text-sm $triggerBase",
          role := "combobox",
          div(
            cls := "flex flex-1 flex-wrap items-center gap-1",
            children <-- selectedVar.signal.map { picked =>
              if picked.isEmpty then List(span(cls := "truncate text-muted-foreground", placeholder))
              else items.filter(it => picked.contains(it.value)).map(it => chip(it, () => toggle(it.value))).toList
            }
          ),
          Icons.chevronsUpDown()
        ),
        Popover.content(
          Floating.Placement(align = Floating.Align.Start, matchTriggerWidth = true),
          panelBoxClass,
          list(items, searchPlaceholder, emptyText, selectedVar.signal, toggle, multiSelect = true)
        )
      )
    )

  private def list(
      items: Seq[Item],
      searchPlaceholder: String,
      emptyText: String,
      picked: Signal[Set[String]],
      onSelect: String => Unit,
      multiSelect: Boolean = false
  ): HtmlElement =
    Command(
      Command.input(placeholder := searchPlaceholder),
      Command.list(
        role := "listbox",
        if multiSelect then aria.multiSelectable := true else emptyMod,
        Command.empty(emptyText),
        // Rows are rendered once and `Command` hides the ones that do not match, rather than this rebuilding the list on
        // every keystroke — that is what keeps the highlight and the search state in one place.
        items.map(it => optionItem(it, picked.map(_.contains(it.value)), onSelect))
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
          // Without `stopPropagation` the click would also reach the trigger and toggle the panel.
          onClick --> { ev =>
            ev.preventDefault()
            ev.stopPropagation()
            onRemove()
          },
          Icons.x()
        )
    )

  private def optionItem(it: Item, isSelected: Signal[Boolean], onSelect: String => Unit): HtmlElement =
    Command.item(
      dataAttr("value") := it.label,
      onClick --> { _ => onSelect(it.value) },
      span(cls := "flex-1 truncate", it.label),
      // One binding rather than a static `svg.cls` plus a reactive one: on an SVG element the class attribute is set
      // wholesale, so the second would drop whatever the first wrote.
      Icons.check(svg.className <-- isSelected.map(sel => s"ml-auto ${if sel then "opacity-100" else "opacity-0"}"))
    )
