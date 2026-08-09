package shadcnscalajs.ui

import com.raquo.laminar.api.L.*

/** Drawer surface using a native dialog. Upstream wraps vaul-svelte for swipe physics; this keeps the same DOM contract
  * (`data-vaul-drawer-direction`, handle, side positioning) so pack rules apply, while open/close and exit animations
  * come from [[Dialog.element]]. Swipe-to-dismiss and snap points are not reproduced.
  *
  * The utility classes must stand alone: `modules/ui` is what the CLI copies into consumer projects, which have neither
  * the vendored basecoat CSS nor `shadcn-presets.generated.css`. The `cn-drawer*` hook classes and `data-slot`
  * attributes are additionally what the style-pack presets target — packs are unlayered, so where a pack defines a rule
  * it overrides the utilities below by design.
  */
object Drawer:

  enum Direction derives CanEqual:
    case Top, Right, Bottom, Left

  private def directionName(direction: Direction): String = direction match
    case Direction.Top    => "top"
    case Direction.Right  => "right"
    case Direction.Bottom => "bottom"
    case Direction.Left   => "left"

  private val directionClasses: Map[Direction, String] = Map(
    Direction.Bottom ->
      "inset-x-0 bottom-0 mt-24 max-h-[80vh] rounded-t-xl border-t data-open:animate-in data-open:fade-in-0 data-open:slide-in-from-bottom-10 data-closed:animate-out data-closed:fade-out-0 data-closed:slide-out-to-bottom-10",
    Direction.Top ->
      "inset-x-0 top-0 mb-24 max-h-[80vh] rounded-b-xl border-b data-open:animate-in data-open:fade-in-0 data-open:slide-in-from-top-10 data-closed:animate-out data-closed:fade-out-0 data-closed:slide-out-to-top-10",
    Direction.Left ->
      "inset-y-0 left-0 w-3/4 border-r sm:max-w-sm data-open:animate-in data-open:fade-in-0 data-open:slide-in-from-left-10 data-closed:animate-out data-closed:fade-out-0 data-closed:slide-out-to-left-10",
    Direction.Right ->
      "inset-y-0 right-0 w-3/4 border-l sm:max-w-sm data-open:animate-in data-open:fade-in-0 data-open:slide-in-from-right-10 data-closed:animate-out data-closed:fade-out-0 data-closed:slide-out-to-right-10"
  )

  private def contentClass(direction: Direction): String =
    s"cn-drawer-content group/drawer-content fixed z-50 flex h-auto flex-col bg-popover text-sm text-popover-foreground duration-200 ${Dialog.exitFillClass} ${directionClasses(direction)}"

  def apply(isOpenVar: Var[Boolean], direction: Direction = Direction.Bottom)(
      mods: Modifier[HtmlElement]*
  ): HtmlElement =
    Dialog.element(
      isOpenVar,
      "drawer m-0 max-h-none max-w-none bg-transparent p-0 text-inherit",
      "drawer-content",
      contentClass(direction),
      Dialog.Options(exitMs = 220)
    )(
      // Pack rules and the handle gate on this attribute, which vaul would set.
      dataAttr("vaul-drawer-direction") := directionName(direction),
      div(
        cls := (
          if direction == Direction.Bottom then
            // Pack sets `.cn-drawer-handle { hidden }` unlayered; `block!` is what beats that for the bottom drawer.
            "cn-drawer-handle mx-auto mt-4 block! h-1.5 w-[100px] shrink-0 rounded-full bg-muted"
          else "cn-drawer-handle mx-auto mt-4 hidden h-1.5 w-[100px] shrink-0 rounded-full bg-muted"
        )
      ),
      mods
    )

  def overlay(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "drawer-overlay",
      cls := s"cn-drawer-overlay fixed inset-0 z-50 bg-black/10 supports-backdrop-filter:backdrop-blur-xs data-open:animate-in data-open:fade-in-0 data-closed:animate-out data-closed:fade-out-0 ${Dialog.exitFillClass}",
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
