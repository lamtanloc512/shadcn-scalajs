package shadcnscalajs.ui

import com.raquo.laminar.api.L.*

object Spinner:
  def apply(mods: Modifier[HtmlElement]*): HtmlElement =
    span(
      role := "status",
      aria.label := "Loading",
      cls := "spinner inline-block size-4 animate-spin rounded-full border-2 border-current border-r-transparent",
      mods
    )
