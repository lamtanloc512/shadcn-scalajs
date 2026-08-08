package shadcnscalajs.ui

import com.raquo.laminar.api.L.*

/** shadcn/ui Menubar — a horizontal row of DropdownMenu instances. Each top-level menu IS a DropdownMenu (already a
  * fully working hand-rolled Var/EventBus dropdown); this only supplies the outer menubar chrome. Individual menu
  * triggers keep DropdownMenu's own outline-button styling rather than a fully flattened menubar-item look — a
  * deliberate simplification, since restyling would require DropdownMenu to expose its trigger's class list, which it
  * doesn't yet.
  *
  * The utility classes must stand alone: `modules/ui` is what the CLI copies into consumer projects, which have neither
  * the vendored basecoat CSS nor `shadcn-presets.generated.css`. The `cn-menubar*` hook classes and `data-slot`
  * attributes are additionally what the style-pack presets target — packs are unlayered, so where a pack defines a rule
  * it overrides the utilities below by design.
  */
object Menubar:

  def apply(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "menubar",
      role := "menubar",
      cls := "cn-menubar flex h-9 items-center gap-1 rounded-md border bg-background p-1 shadow-xs",
      mods
    )

  /** A single top-level menu — thin alias over DropdownMenu so call sites read as `Menubar.menu("File")(...)`. */
  def menu(trigger: Modifier[HtmlElement]*)(items: DropdownMenu.Item*): HtmlElement =
    DropdownMenu(trigger*)(items*)

  def label(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "menubar-label",
      cls := "cn-menubar-label text-muted-foreground px-2 py-1.5 text-xs font-medium",
      mods
    )

  def separator(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "menubar-separator",
      role := "separator",
      cls := "cn-menubar-separator bg-border -mx-1 my-1 h-px",
      mods
    )

  def shortcut(mods: Modifier[HtmlElement]*): HtmlElement =
    span(
      dataAttr("slot") := "menubar-shortcut",
      cls := "cn-menubar-shortcut text-muted-foreground ml-auto text-xs tracking-widest",
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
