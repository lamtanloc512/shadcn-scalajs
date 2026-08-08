package shadcnscalajs.ui

import com.raquo.laminar.api.L.*

/** Drawer surface using a native dialog; callers can add side-specific classes.
  *
  * The utility classes must stand alone: `modules/ui` is what the CLI copies into consumer projects, which have neither
  * the vendored basecoat CSS nor `shadcn-presets.generated.css`. The `cn-drawer*` hook classes and `data-slot`
  * attributes are additionally what the style-pack presets target — packs are unlayered, so where a pack defines a rule
  * it overrides the utilities below by design.
  */
object Drawer:

  private val contentBase: String =
    "cn-drawer-content group/drawer-content fixed z-50 flex h-auto flex-col bg-popover text-sm text-popover-foreground"

  def apply(isOpenVar: Var[Boolean])(mods: Modifier[HtmlElement]*): HtmlElement =
    Dialog.element(isOpenVar, "drawer", "drawer-content", contentBase)(
      div(
        cls := "cn-drawer-handle mx-auto mt-4 hidden h-1.5 w-[100px] shrink-0 rounded-full bg-muted group-data-[vaul-drawer-direction=bottom]/drawer-content:block"
      ),
      mods
    )

  def overlay(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "drawer-overlay",
      cls := "cn-drawer-overlay fixed inset-0 z-50 bg-black/10 supports-backdrop-filter:backdrop-blur-xs data-open:animate-in data-open:fade-in-0 data-closed:animate-out data-closed:fade-out-0",
      mods
    )

  def close(mods: Modifier[HtmlElement]*): HtmlElement =
    Button
      .of(
        _.variant(Button.Variant.Ghost),
        _.size(Button.Size.IconSm),
        _ => dataAttr("slot") := "drawer-close",
        _ => Icons.x(),
        _ => span(cls := "sr-only", "Close")
      )
      .amend(mods)

  def header(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "drawer-header",
      cls := "cn-drawer-header flex flex-col gap-0.5 p-4 md:gap-1.5 md:text-left",
      mods
    )

  def footer(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "drawer-footer",
      cls := "cn-drawer-footer mt-auto flex flex-col gap-2 p-4",
      mods
    )

  def title(mods: Modifier[HtmlElement]*): HtmlElement =
    h2(
      dataAttr("slot") := "drawer-title",
      cls := "cn-font-heading cn-drawer-title text-foreground font-medium",
      mods
    )

  def description(mods: Modifier[HtmlElement]*): HtmlElement =
    p(
      dataAttr("slot") := "drawer-description",
      cls := "cn-drawer-description text-muted-foreground text-sm",
      mods
    )
