package shadcnscalajs.ui

import com.raquo.laminar.api.L.*

/** shadcn/ui Menubar — a horizontal row of DropdownMenu instances. Each top-level menu IS a DropdownMenu (already a
  * fully working hand-rolled Var/EventBus dropdown); this only supplies the outer menubar chrome. Individual menu
  * triggers keep DropdownMenu's own outline-button styling rather than a fully flattened menubar-item look — a
  * deliberate simplification, since restyling would require DropdownMenu to expose its trigger's class list, which it
  * doesn't yet.
  */
object Menubar:

  def apply(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      role := "menubar",
      cls := "flex h-9 items-center gap-1 rounded-md border bg-background p-1 shadow-xs",
      mods
    )

  /** A single top-level menu — thin alias over DropdownMenu so call sites read as `Menubar.menu("File")(...)`. */
  def menu(trigger: Modifier[HtmlElement]*)(items: DropdownMenu.Item*): HtmlElement =
    DropdownMenu(trigger*)(items*)
