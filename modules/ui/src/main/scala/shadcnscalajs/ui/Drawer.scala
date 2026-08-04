package shadcnscalajs.ui

import com.raquo.laminar.api.L.*

/** Drawer surface using a native dialog; callers can add side-specific classes. */
object Drawer:
  def apply(isOpenVar: Var[Boolean])(mods: Modifier[HtmlElement]*): HtmlElement =
    Dialog.element(isOpenVar, "drawer")(mods*)
  def header(mods: Modifier[HtmlElement]*): HtmlElement =
    headerTag(cls := "grid gap-1.5 p-4 text-center sm:text-left", mods)
  def footer(mods: Modifier[HtmlElement]*): HtmlElement = footerTag(cls := "mt-auto flex flex-col gap-2 p-4", mods)
