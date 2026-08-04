package shadcnscalajs.ui

import com.raquo.laminar.api.L.*

/** shadcn/ui NavigationMenu — a horizontal nav with hover-revealed panels, same CSS-hover approach as HoverCard
  * (Radix's NavigationMenu tracks open/close state in JS; a `group`/`group-hover` reveal covers the common case without
  * a second state machine).
  */
object NavigationMenu:

  def apply(mods: Modifier[HtmlElement]*): HtmlElement =
    navTag(cls := "relative z-10 flex max-w-max flex-1 items-center justify-center", mods)

  def list(mods: Modifier[HtmlElement]*): HtmlElement =
    ul(cls := "flex flex-1 list-none items-center justify-center gap-1", mods)

  def item(mods: Modifier[HtmlElement]*): HtmlElement = li(cls := "group/item relative", mods)

  def trigger(mods: Modifier[HtmlElement]*): HtmlElement =
    button(
      typ := "button",
      cls := "inline-flex h-9 w-max items-center justify-center rounded-md bg-background px-4 py-2 text-sm font-medium hover:bg-accent hover:text-accent-foreground",
      mods
    )

  def content(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      cls := "invisible absolute left-0 top-full z-50 mt-1.5 w-full min-w-64 rounded-md border bg-popover p-4 text-popover-foreground opacity-0 shadow-md transition-opacity group-hover/item:visible group-hover/item:opacity-100",
      mods
    )
