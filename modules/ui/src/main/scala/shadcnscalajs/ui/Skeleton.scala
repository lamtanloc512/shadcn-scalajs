package shadcnscalajs.ui

import com.raquo.laminar.api.L.*

object Skeleton:
  def apply(mods: Modifier[HtmlElement]*): HtmlElement =
    div(cls := "animate-pulse rounded-md bg-primary/10", mods)
