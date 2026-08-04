package shadcnscalajs.ui

import com.raquo.laminar.api.L.*

object Empty:
  def apply(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      cls := "empty flex min-h-40 w-full flex-col items-center justify-center gap-6 rounded-lg border border-dashed p-6 text-center",
      mods
    )
  def header(mods: Modifier[HtmlElement]*): HtmlElement =
    headerTag(cls := "flex max-w-sm flex-col items-center gap-2 text-center", mods)
  def title(mods: Modifier[HtmlElement]*): HtmlElement = h3(cls := "text-lg font-semibold", mods)
  def description(mods: Modifier[HtmlElement]*): HtmlElement = p(cls := "text-sm text-muted-foreground", mods)
