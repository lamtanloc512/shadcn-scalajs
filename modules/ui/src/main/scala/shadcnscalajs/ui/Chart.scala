package shadcnscalajs.ui

import com.raquo.laminar.api.L.*

object Chart:
  def apply(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      role := "img",
      cls := "chart flex aspect-video w-full items-center justify-center rounded-lg border bg-card p-4",
      mods
    )
