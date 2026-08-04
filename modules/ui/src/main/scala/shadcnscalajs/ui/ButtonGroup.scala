package shadcnscalajs.ui

import com.raquo.laminar.api.L.*

object ButtonGroup:
  def apply(mods: Modifier[HtmlElement]*): HtmlElement =
    div(cls := "flex w-fit items-stretch [&>button]:focus-visible:z-10 [&>button]:relative", mods)
  def vertical(mods: Modifier[HtmlElement]*): HtmlElement =
    div(cls := "flex flex-col items-stretch [&>button]:focus-visible:z-10 [&>button]:relative", mods)
