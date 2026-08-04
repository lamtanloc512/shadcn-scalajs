package shadcnscalajs.ui

import com.raquo.laminar.api.L.*

/** Alert dialog composition over the native Laminar Dialog primitive. */
object AlertDialog:
  def apply(isOpenVar: Var[Boolean])(mods: Modifier[HtmlElement]*): HtmlElement = Dialog(isOpenVar)(mods*)
  def title(mods: Modifier[HtmlElement]*): HtmlElement = h2(cls := "text-lg font-semibold", mods)
  def description(mods: Modifier[HtmlElement]*): HtmlElement = p(cls := "text-sm text-muted-foreground", mods)
  def footer(mods: Modifier[HtmlElement]*): HtmlElement =
    div(cls := "flex flex-col-reverse gap-2 sm:flex-row sm:justify-end", mods)
