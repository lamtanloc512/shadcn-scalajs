package shadcnscalajs.ui

import com.raquo.laminar.api.L.*

/** shadcn/ui Command — command palette primitives.
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
      mods
    )

  def input(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "command-input-wrapper",
      cls := "cn-command-input-wrapper p-1 pb-0",
      div(
        cls := "cn-command-input-group bg-input/50 flex h-9 items-center gap-2 rounded-md px-3",
        Icons.search(svg.cls := "cn-command-input-icon size-4 shrink-0 opacity-50"),
        Input(
          dataAttr("slot") := "command-input",
          cls := "cn-command-input h-9 w-full rounded-none border-0 bg-transparent px-0 text-sm shadow-none outline-hidden focus-visible:ring-0 disabled:cursor-not-allowed disabled:opacity-50",
          mods
        )
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
