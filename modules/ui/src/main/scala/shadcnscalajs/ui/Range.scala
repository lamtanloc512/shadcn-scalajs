package shadcnscalajs.ui

import com.raquo.laminar.api.L.*

object Range:
  def apply(mods: Modifier[HtmlElement]*): HtmlElement =
    input(typ := "range", cls := "h-5 w-full cursor-pointer accent-primary", mods)
