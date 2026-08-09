package shadcnscalajs.ui

import com.raquo.laminar.api.L.*
import org.scalajs.dom

/** shadcn/ui Command — a command palette that filters its own items as you type.
  *
  * Upstream gets the behavior from cmdk. Here the root runs it by delegation: it listens for `input` and `keydown`
  * events bubbling up from [[input]], then filters, highlights, and activates rows found by querying its own subtree.
  * Doing it that way is what lets every part below stay a plain element — a caller can nest items inside groups,
  * conditionals, or their own wrappers, and the engine still finds them, exactly as cmdk's context-based version does.
  *
  * Rows are matched on their text, or on `data-value` / `data-keywords` when the visible text is not what should be
  * searched. Filtering hides rows through inline `display`, not the `hidden` attribute: `hidden` is a user-agent rule
  * and the row's own `flex` utility, being an author rule, would win.
  *
  * The utility classes must stand alone: `modules/ui` is what the CLI copies into consumer projects, which have neither
  * the vendored basecoat CSS nor `shadcn-presets.generated.css`. The `cn-command*` hook classes and `data-slot`
  * attributes are additionally what the style-pack presets target — packs are unlayered, so where a pack defines a rule
  * it overrides the utilities below by design.
  */
object Command:

  def apply(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "command",
      role := "dialog",
      cls := "cn-command flex size-full flex-col overflow-hidden rounded-md bg-popover p-1 text-popover-foreground",
      onInput --> { (ev: dom.Event) =>
        queryOf(ev).foreach(query => refresh(rootOf(ev), query))
      },
      onKeyDown --> { (ev: dom.KeyboardEvent) => navigate(ev) },
      // Rows present at mount are unfiltered, but the empty state has to start hidden rather than flash into view.
      onMountCallback { ctx => refresh(ctx.thisNode.ref, "") },
      mods
    )

  /** Wraps [[Dialog]] the way upstream's `CommandDialog` does: an edge-to-edge panel, since the palette supplies its
    * own padding and rounding.
    */
  def dialog(isOpenVar: Var[Boolean], mods: Modifier[HtmlElement]*): HtmlElement =
    Dialog(isOpenVar, "overflow-hidden p-0! sm:max-w-lg")(
      Dialog.header(cls := "sr-only", Dialog.title("Command palette"), Dialog.description("Search for a command.")),
      apply(mods*)
    )

  private def rootOf(ev: dom.Event): dom.html.Element =
    ev.currentTarget.asInstanceOf[dom.html.Element]

  private def queryOf(ev: dom.Event): Option[String] =
    ev.target match
      case el: dom.html.Input if el.getAttribute("data-slot") == "command-input" => Some(el.value)
      case _                                                                     => None

  private def elements(root: dom.Element, selector: String): List[dom.html.Element] =
    root.querySelectorAll(selector).toList.collect { case el: dom.html.Element => el }

  private def rows(root: dom.Element): List[dom.html.Element] =
    elements(root, "[data-slot=command-item]:not([data-disabled=true])")

  private def visibleRows(root: dom.Element): List[dom.html.Element] =
    rows(root).filter(_.style.display != "none")

  /** What a row is searched by: `data-value` or `data-keywords` when set, otherwise the text the user can see. */
  private def haystack(row: dom.html.Element): String =
    val explicit = Option(row.getAttribute("data-value")).filter(_.nonEmpty)
    val keywords = Option(row.getAttribute("data-keywords")).getOrElse("")
    s"${explicit.getOrElse(row.textContent)} $keywords".toLowerCase

  private def show(el: dom.html.Element, visible: Boolean): Unit =
    if visible then el.style.removeProperty("display") else el.style.display = "none"

  private def refresh(root: dom.html.Element, query: String): Unit =
    val needle = query.trim.toLowerCase
    rows(root).foreach(row => show(row, needle.isEmpty || haystack(row).contains(needle)))

    // A group whose every row was filtered out would otherwise leave its heading stranded.
    elements(root, "[data-slot=command-group]").foreach { group =>
      show(group, visibleRows(group).nonEmpty)
    }
    elements(root, "[data-slot=command-empty]").foreach { empty =>
      show(empty, visibleRows(root).isEmpty)
    }

    val visible = visibleRows(root)
    val stillValid = visible.exists(_.getAttribute("data-selected") == "true")
    if !stillValid then highlight(root, visible.headOption)

  private def highlight(root: dom.html.Element, row: Option[dom.html.Element]): Unit =
    rows(root).foreach { el =>
      el.removeAttribute("data-selected")
      el.setAttribute("aria-selected", "false")
    }
    row.foreach { el =>
      el.setAttribute("data-selected", "true")
      el.setAttribute("aria-selected", "true")
      revealInList(el)
    }

  /** `scrollIntoView` walks every scrollable ancestor, so highlighting the first row on mount would drag the whole page
    * to the command palette. Scroll the list itself instead.
    */
  private def revealInList(row: dom.html.Element): Unit =
    Option(row.closest("[data-slot=command-list]")).map(_.asInstanceOf[dom.html.Element]).foreach { list =>
      val rowRect = row.getBoundingClientRect()
      val rowTop = rowRect.top - list.getBoundingClientRect().top + list.scrollTop
      val rowBottom = rowTop + rowRect.height
      if rowTop < list.scrollTop then list.scrollTop = rowTop
      else if rowBottom > list.scrollTop + list.clientHeight then list.scrollTop = rowBottom - list.clientHeight
    }

  /** Arrow keys move the highlight and Enter runs it. Focus stays in the search input throughout — that is why the
    * highlight is an attribute rather than DOM focus, unlike the menus.
    */
  private def navigate(ev: dom.KeyboardEvent): Unit =
    val root = rootOf(ev)
    val visible = visibleRows(root)
    if visible.nonEmpty then
      val current = visible.indexWhere(_.getAttribute("data-selected") == "true")
      def moveTo(index: Int): Unit =
        ev.preventDefault()
        highlight(root, Some(visible(((index % visible.size) + visible.size) % visible.size)))
      ev.key match
        case "ArrowDown" => moveTo(if current < 0 then 0 else current + 1)
        case "ArrowUp"   => moveTo(if current < 0 then visible.size - 1 else current - 1)
        case "Home"      => moveTo(0)
        case "End"       => moveTo(visible.size - 1)
        case "Enter" =>
          ev.preventDefault()
          visible.lift(math.max(current, 0)).foreach(_.click())
        case _ => ()

  def input(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "command-input-wrapper",
      // Pack owns `p-1 pb-0` on `.cn-command-input-wrapper` — don't duplicate it in utilities.
      cls := "cn-command-input-wrapper",
      // Upstream builds this from InputGroup so pack rules like `*:data-[slot=input-group-addon]:pl-2!` and
      // `h-8! rounded-lg!` on `.cn-command-input-group` actually match.
      InputGroup(
        cls := "cn-command-input-group",
        InputGroup.input(
          // Filtering engine keys off this slot; overrides InputGroup's `input-group-control`.
          dataAttr("slot") := "command-input",
          cls := "cn-command-input outline-hidden disabled:cursor-not-allowed disabled:opacity-50",
          mods
        ),
        InputGroup.addon(Icons.search(svg.cls := "cn-command-input-icon"))
      )
    )

  def list(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "command-list",
      cls := "cn-command-list no-scrollbar max-h-72 overflow-x-hidden overflow-y-auto scroll-py-1 outline-none",
      mods
    )

  def group(heading: String, mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "command-group",
      cls := "cn-command-group overflow-hidden p-1 text-foreground",
      h3(cls := "px-2 py-1.5 text-xs font-medium text-muted-foreground", heading),
      mods
    )

  def item(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "command-item",
      role := "option",
      cls := "group/command-item relative flex cursor-default items-center gap-2 rounded-sm px-2 py-1.5 text-sm outline-hidden select-none in-data-[slot=dialog-content]:rounded-lg! data-selected:bg-muted data-selected:text-foreground data-[disabled=true]:pointer-events-none data-[disabled=true]:opacity-50 [&_svg]:pointer-events-none [&_svg]:shrink-0 [&_svg:not([class*='size-'])]:size-4 data-selected:*:[svg]:text-foreground hover:bg-accent hover:text-accent-foreground",
      mods
    )

  def empty(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "command-empty",
      cls := "cn-command-empty py-6 text-center text-sm",
      mods
    )

  def separator(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "command-separator",
      role := "separator",
      cls := "cn-command-separator -mx-1 my-1.5 h-px bg-border",
      mods
    )

  def shortcut(mods: Modifier[HtmlElement]*): HtmlElement =
    span(
      dataAttr("slot") := "command-shortcut",
      cls := "cn-command-shortcut ml-auto text-xs tracking-widest text-muted-foreground",
      mods
    )

  /** Upstream `command-loading` is a bits-ui passthrough with no `data-slot`. */
  def loading(mods: Modifier[HtmlElement]*): HtmlElement =
    div(role := "status", aria.live := "polite", mods)
