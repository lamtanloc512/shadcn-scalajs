package shadcnscalajs.ui

import com.raquo.laminar.api.L.*

/** shadcn/ui ButtonGroup — joins adjacent controls into a single unit.
  *
  * The joining selectors target `[data-slot]` children rather than `button`, matching upstream, so selects, inputs, and
  * anchors styled as buttons join too. That only works because every component in `modules/ui` now sets `data-slot`.
  */
object ButtonGroup:

  enum Orientation derives CanEqual:
    case Horizontal, Vertical

  private val base: String =
    "button-group cn-button-group flex w-fit items-stretch [&>*]:focus-visible:relative [&>*]:focus-visible:z-10 [&>[data-slot=select-trigger]:not([class*='w-'])]:w-fit [&>input]:flex-1"

  private def orientationClasses(orientation: Orientation): String = orientation match
    case Orientation.Horizontal =>
      "cn-button-group-orientation-horizontal [&>[data-slot]]:rounded-r-none [&>[data-slot]~[data-slot]]:rounded-l-none [&>[data-slot]~[data-slot]]:border-l-0"
    case Orientation.Vertical =>
      "cn-button-group-orientation-vertical flex-col [&>[data-slot]]:rounded-b-none [&>[data-slot]~[data-slot]]:rounded-t-none [&>[data-slot]~[data-slot]]:border-t-0"

  def apply(mods: Modifier[HtmlElement]*): HtmlElement =
    apply(Orientation.Horizontal, mods*)

  def apply(orientation: Orientation, mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      role := "group",
      dataAttr("slot") := "button-group",
      dataAttr("orientation") := orientation.toString.toLowerCase,
      cls := s"$base ${orientationClasses(orientation)}",
      mods
    )

  def vertical(mods: Modifier[HtmlElement]*): HtmlElement =
    apply(Orientation.Vertical, mods*)

  /** A non-interactive label segment inside the group — upstream's `ButtonGroupText`. */
  def text(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "button-group-text",
      cls := "cn-button-group-text flex items-center gap-2 rounded-md border bg-muted px-4 text-sm font-medium [&_svg]:pointer-events-none [&_svg:not([class*='size-'])]:size-4",
      mods
    )

  def separator(mods: Modifier[HtmlElement]*): HtmlElement =
    separator(Separator.Orientation.Vertical, mods*)

  def separator(orientation: Separator.Orientation, mods: Modifier[HtmlElement]*): HtmlElement =
    Separator(
      orientation,
      dataAttr("slot") := "button-group-separator",
      cls := "cn-button-group-separator relative self-stretch bg-input data-[orientation=horizontal]:mx-px data-[orientation=horizontal]:w-auto data-[orientation=vertical]:my-px data-[orientation=vertical]:h-auto",
      mods
    )
