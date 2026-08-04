package shadcnscalajs.ui

import com.raquo.laminar.api.L.*

/** shadcn/ui Card primitives.
  *
  * The helpers intentionally accept Laminar modifiers so they can be composed with application-specific content without
  * introducing a React-style wrapper API.
  */
object Card:

  def apply(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      cls := "flex flex-col gap-6 rounded-xl border bg-card py-6 text-card-foreground shadow-sm",
      mods
    )

  def header(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      cls := "grid auto-rows-min grid-rows-[auto_auto] items-start gap-2 px-6",
      mods
    )

  def title(mods: Modifier[HtmlElement]*): HtmlElement =
    div(cls := "leading-none font-semibold", mods)

  def description(mods: Modifier[HtmlElement]*): HtmlElement =
    div(cls := "text-sm text-muted-foreground", mods)

  def action(mods: Modifier[HtmlElement]*): HtmlElement =
    div(cls := "col-start-2 row-span-2 row-start-1 self-start justify-self-end", mods)

  def content(mods: Modifier[HtmlElement]*): HtmlElement =
    div(cls := "px-6", mods)

  def footer(mods: Modifier[HtmlElement]*): HtmlElement =
    div(cls := "flex items-center px-6", mods)
