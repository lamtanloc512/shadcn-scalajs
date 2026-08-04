package shadcnscalajs.ui

import com.raquo.laminar.api.L.*

object Item:
  def apply(mods: Modifier[HtmlElement]*): HtmlElement =
    div(cls := "flex w-full items-center gap-2 rounded-md p-4 text-sm", mods)
  def media(mods: Modifier[HtmlElement]*): HtmlElement = div(cls := "flex shrink-0 items-center justify-center", mods)
  def content(mods: Modifier[HtmlElement]*): HtmlElement = div(cls := "flex min-w-0 flex-1 flex-col gap-1", mods)
  def title(mods: Modifier[HtmlElement]*): HtmlElement = div(cls := "truncate font-medium", mods)
  def description(mods: Modifier[HtmlElement]*): HtmlElement =
    div(cls := "line-clamp-2 text-sm text-muted-foreground", mods)
  def actions(mods: Modifier[HtmlElement]*): HtmlElement = div(cls := "ml-auto flex items-center gap-2", mods)
