package shadcnscalajs.ui

import com.raquo.laminar.api.L.*

/** shadcn/ui HoverCard — shown on hover rather than click, unlike Popover. Radix's HoverCard uses JS open/close delays;
  * this uses a pure-CSS `group`/`group-hover` reveal instead, avoiding a second hand-rolled timer-based state machine
  * for what is fundamentally a hover-triggered tooltip-like panel.
  */
object HoverCard:

  def apply(mods: Modifier[HtmlElement]*): HtmlElement = div(cls := "group relative inline-block", mods)

  def trigger(mods: Modifier[HtmlElement]*): HtmlElement = span(cls := "cursor-pointer underline-offset-4", mods)

  def content(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      cls := "invisible absolute z-50 mt-2 w-64 rounded-md border bg-popover p-4 text-popover-foreground opacity-0 shadow-md outline-none transition-opacity group-hover:visible group-hover:opacity-100",
      mods
    )
