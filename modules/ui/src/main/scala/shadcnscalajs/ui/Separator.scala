package shadcnscalajs.ui

import com.raquo.laminar.api.L.*

/** shadcn/ui Separator — a plain `role=separator` div, styled per orientation. Pure CSS/no-JS tier. */
object Separator:

  enum Orientation derives CanEqual:
    case Horizontal, Vertical

  def apply(orientation: Orientation = Orientation.Horizontal, mods: Modifier[HtmlElement]*): HtmlElement =
    val sizeClasses =
      if orientation == Orientation.Horizontal then "h-px w-full" else "h-full w-px"
    div(
      role := "separator",
      dataAttr("orientation") := orientation.toString.toLowerCase,
      cls := s"shrink-0 bg-border $sizeClasses",
      mods
    )
