package shadcnscalajs.ui

import com.raquo.laminar.api.L.*

/** Drawer surface using a native dialog; callers can add side-specific classes. */
object Drawer:
  def apply(isOpenVar: Var[Boolean])(mods: Modifier[HtmlElement]*): HtmlElement =
    Dialog(isOpenVar)(
      cls := "inset-x-0 bottom-0 top-auto max-w-none translate-x-0 translate-y-0 rounded-t-lg sm:max-w-none",
      mods
    )
  def header(mods: Modifier[HtmlElement]*): HtmlElement = div(cls := "grid gap-1.5 p-4 text-center sm:text-left", mods)
  def footer(mods: Modifier[HtmlElement]*): HtmlElement = div(cls := "mt-auto flex flex-col gap-2 p-4", mods)
