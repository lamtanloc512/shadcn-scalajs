package shadcnscalajs.ui

import com.raquo.laminar.api.L.*

/** shadcn/ui AspectRatio — a plain div using the native CSS `aspect-ratio` property (Radix's AspectRatio primitive does
  * the same thing under the hood, just via inline style computed in JS). Pure CSS/no-JS tier.
  */
object AspectRatio:

  def apply(ratio: Double, mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "aspect-ratio",
      cls := "cn-aspect-ratio relative w-full",
      styleAttr := s"aspect-ratio: $ratio",
      mods
    )
