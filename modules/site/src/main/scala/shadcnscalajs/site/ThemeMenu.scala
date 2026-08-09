package shadcnscalajs.site

import com.raquo.laminar.api.L.*
import org.scalajs.dom

import scala.scalajs.js
import shadcnscalajs.site.create.{CreateState, Customizer, Picker}
import shadcnscalajs.ui.Icons

/** Site-header theme control — the create page's own customizer in a popover, plus the dark-mode toggle.
  *
  * Docs, gallery, and blocks headers previously offered only a style-pack `<select>`, so theme color, fonts, radius,
  * icon library, and menu options applied on those pages but could only be edited at `/create`.
  */
object ThemeMenu:

  private val triggerClasses =
    "inline-flex h-8 shrink-0 items-center justify-center gap-2 rounded-md border border-input bg-background px-2.5 text-sm font-medium whitespace-nowrap transition-colors outline-none hover:bg-accent hover:text-accent-foreground focus-visible:ring-[3px] focus-visible:ring-ring/50 data-[state=open]:bg-accent"

  private val iconButtonClasses =
    "inline-flex size-8 shrink-0 items-center justify-center rounded-md text-sm transition-colors outline-none hover:bg-accent hover:text-accent-foreground focus-visible:ring-[3px] focus-visible:ring-ring/50"

  /** A click inside a picker's dropdown must not read as an outside click: those menus portal to `document.body`, so
    * they are not descendants of this popover.
    */
  private def isInsidePickerMenu(ev: dom.Event): Boolean =
    val path = Picker.compPath(ev)
    var i = 0
    var found = false
    while i < path.length && !found do
      path(i) match
        case el: dom.html.Element if el.closest("[data-slot='dropdown-menu-content']") != null => found = true
        case _                                                                                 => ()
      i += 1
    found

  def apply(): HtmlElement =
    val state = new CreateState(urlSync = false)
    val isOpen = Var(false)

    div(
      cls := "relative flex items-center gap-2",
      state.config.signal --> { cfg => ThemeConfig.applyToDocument(cfg) },
      onMountBind { ctx =>
        documentEvents(_.onMouseDown) --> { (ev: dom.MouseEvent) =>
          val insideSelf = Picker.compPath(ev).indexOf(ctx.thisNode.ref) != -1
          if isOpen.now() && !insideSelf && !isInsidePickerMenu(ev) then isOpen.set(false)
        }
      },
      onKeyDown --> { (ev: dom.KeyboardEvent) => if ev.key == "Escape" then isOpen.set(false) },
      button(
        typ := "button",
        cls := triggerClasses,
        dataAttr("state") <-- isOpen.signal.map(open => if open then "open" else "closed"),
        aria.hasPopup := true,
        aria.expanded <-- isOpen.signal,
        aria.label := "Customize theme",
        Icons.paintbrush(),
        span(
          cls := "hidden sm:inline",
          child.text <-- state.config.signal.map(_.stylePack.capitalize)
        ),
        Icons.chevronDown(svg.cls := "size-3.5 opacity-60"),
        onClick --> { _ => isOpen.update(!_) }
      ),
      button(
        typ := "button",
        cls := iconButtonClasses,
        aria.label := "Toggle dark mode",
        dataAttr("theme-toggle") := "",
        onClick --> { ev => state.toggleDark(ThemeTransition.originOf(ev)) },
        span(cls := "hidden dark:block", Icons.sun()),
        span(cls := "block dark:hidden", Icons.moon())
      ),
      div(
        // Same slot the create column uses: globals.css exempts customizer chrome from the style-pack card reset,
        // so the panel keeps its own elevation whichever pack is active.
        dataAttr("slot") := "customizer",
        // Opaque, not the create column's translucent card: this panel floats over page copy rather than over the
        // preview canvas, and text showing through it is unreadable.
        cls := "dark absolute end-0 top-full z-50 mt-2 flex max-h-[min(32rem,calc(100dvh-6rem))] w-72 flex-col overflow-hidden rounded-2xl border-0 bg-card text-card-foreground shadow-xl ring-1 ring-foreground/15",
        display <-- isOpen.signal.map(open => if open then "flex" else "none"),
        aria.hidden <-- isOpen.signal.map(!_),
        div(
          cls := "flex items-center justify-between gap-2 border-b px-3 py-2 text-sm font-medium",
          span("Theme"),
          a(
            href := "/create",
            cls := "text-xs text-muted-foreground hover:text-foreground",
            "Open customizer"
          )
        ),
        div(
          cls := "no-scrollbar min-h-0 flex-1 overflow-y-auto",
          Customizer.fields(state)
        ),
        div(
          cls := "border-t p-3",
          button(
            typ := "button",
            cls := "inline-flex h-8 w-full items-center justify-center gap-2 rounded-md border border-foreground/15 text-sm font-medium transition-colors hover:bg-foreground/10",
            onClick --> { _ => state.reset() },
            Icons.undo(svg.cls := "size-3.5"),
            "Reset"
          )
        )
      )
    )
