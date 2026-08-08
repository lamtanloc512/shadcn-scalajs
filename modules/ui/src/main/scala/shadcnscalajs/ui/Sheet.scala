package shadcnscalajs.ui

import com.raquo.laminar.api.L.*

/** shadcn/ui Sheet — a side panel, built on the same native `<dialog>` mechanism as Dialog/Drawer (`Dialog.element`),
  * positioned to a side via Tailwind classes instead of Radix's animated `data-state` slide transitions.
  *
  * The utility classes must stand alone: `modules/ui` is what the CLI copies into consumer projects, which have neither
  * the vendored basecoat CSS nor `shadcn-presets.generated.css`. The `cn-sheet*` hook classes and `data-slot`
  * attributes are additionally what the style-pack presets target — packs are unlayered, so where a pack defines a rule
  * it overrides the utilities below by design.
  */
object Sheet:

  enum Side derives CanEqual:
    case Top, Right, Bottom, Left

  private def sideName(side: Side): String = side match
    case Side.Top    => "top"
    case Side.Right  => "right"
    case Side.Bottom => "bottom"
    case Side.Left   => "left"

  private val sideClasses: Map[Side, String] = Map(
    Side.Right -> "data-[side=right]:inset-y-0 data-[side=right]:right-0 data-[side=right]:left-auto data-[side=right]:h-full data-[side=right]:w-3/4 data-[side=right]:border-l data-[side=right]:sm:max-w-sm",
    Side.Left -> "data-[side=left]:inset-y-0 data-[side=left]:left-0 data-[side=left]:right-auto data-[side=left]:h-full data-[side=left]:w-3/4 data-[side=left]:border-r data-[side=left]:sm:max-w-sm",
    Side.Top -> "data-[side=top]:inset-x-0 data-[side=top]:top-0 data-[side=top]:bottom-auto data-[side=top]:h-auto data-[side=top]:w-full data-[side=top]:border-b",
    Side.Bottom -> "data-[side=bottom]:inset-x-0 data-[side=bottom]:bottom-0 data-[side=bottom]:top-auto data-[side=bottom]:h-auto data-[side=bottom]:w-full data-[side=bottom]:border-t"
  )

  private def contentClass(side: Side): String =
    s"cn-sheet-content bg-popover text-popover-foreground fixed z-50 flex flex-col bg-clip-padding text-sm shadow-xl transition duration-200 ease-in-out data-open:animate-in data-open:fade-in-0 data-closed:animate-out data-closed:fade-out-0 data-[side=bottom]:data-open:slide-in-from-bottom-10 data-[side=left]:data-open:slide-in-from-left-10 data-[side=right]:data-open:slide-in-from-right-10 data-[side=top]:data-open:slide-in-from-top-10 data-[side=bottom]:data-closed:slide-out-to-bottom-10 data-[side=left]:data-closed:slide-out-to-left-10 data-[side=right]:data-closed:slide-out-to-right-10 data-[side=top]:data-closed:slide-out-to-top-10 ${sideClasses(side)}"

  def apply(isOpenVar: Var[Boolean], side: Side = Side.Right)(mods: Modifier[HtmlElement]*): HtmlElement =
    Dialog.element(
      isOpenVar,
      "sheet m-0 max-h-none max-w-none bg-transparent p-0 text-inherit",
      "sheet-content",
      contentClass(side),
      // Sheet's slide uses `transition duration-200`; give the exit phase room to finish.
      Dialog.Options(exitMs = 220)
    )(
      dataAttr("side") := sideName(side),
      mods
    )

  def overlay(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "sheet-overlay",
      cls := "cn-sheet-overlay fixed inset-0 z-50 bg-black/10 supports-backdrop-filter:backdrop-blur-xs data-open:animate-in data-open:fade-in-0 data-closed:animate-out data-closed:fade-out-0",
      mods
    )

  def close(mods: Modifier[HtmlElement]*): HtmlElement =
    Button
      .of(
        _.variant(Button.Variant.Ghost),
        _.size(Button.Size.IconSm),
        _ => dataAttr("slot") := "sheet-close",
        _ => cls := "cn-sheet-close absolute top-4 right-4",
        _ => Icons.x(),
        _ => span(cls := "sr-only", "Close")
      )
      .amend(mods)

  def header(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "sheet-header",
      cls := "cn-sheet-header flex flex-col gap-1.5 p-6",
      mods
    )

  def footer(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "sheet-footer",
      cls := "cn-sheet-footer mt-auto flex flex-col gap-2 p-6",
      mods
    )

  def title(mods: Modifier[HtmlElement]*): HtmlElement =
    h2(
      dataAttr("slot") := "sheet-title",
      cls := "cn-font-heading cn-sheet-title text-foreground font-medium",
      mods
    )

  def description(mods: Modifier[HtmlElement]*): HtmlElement =
    p(
      dataAttr("slot") := "sheet-description",
      cls := "cn-sheet-description text-muted-foreground text-sm",
      mods
    )
