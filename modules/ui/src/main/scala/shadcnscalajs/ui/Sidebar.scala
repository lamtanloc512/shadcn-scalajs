package shadcnscalajs.ui

import com.raquo.laminar.api.L.*

object Sidebar:
  private val asideTag = htmlTag("aside")

  def apply(mods: Modifier[HtmlElement]*): HtmlElement =
    asideTag(cls := "flex h-full w-64 flex-col border-r bg-background", mods)
  def header(mods: Modifier[HtmlElement]*): HtmlElement = div(cls := "flex h-14 items-center border-b px-4", mods)
  def content(mods: Modifier[HtmlElement]*): HtmlElement =
    div(cls := "flex min-h-0 flex-1 flex-col gap-2 overflow-auto p-2", mods)
  def footer(mods: Modifier[HtmlElement]*): HtmlElement = div(cls := "border-t p-2", mods)
  def menu(mods: Modifier[HtmlElement]*): HtmlElement = navTag(cls := "flex flex-col gap-1", mods)
  def menuItem(mods: Modifier[HtmlElement]*): HtmlElement = a(
    href := "#",
    cls := "flex items-center gap-2 rounded-md px-2 py-1.5 text-sm hover:bg-accent hover:text-accent-foreground",
    mods
  )
