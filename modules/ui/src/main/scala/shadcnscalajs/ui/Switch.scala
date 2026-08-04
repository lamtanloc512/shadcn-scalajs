package shadcnscalajs.ui

import com.raquo.laminar.api.L.*

object Switch:
  private val base =
    "peer inline-flex h-5 w-9 shrink-0 cursor-pointer items-center rounded-full border-2 border-transparent shadow-xs transition-colors outline-none focus-visible:ring-[3px] focus-visible:ring-ring/50 disabled:cursor-not-allowed disabled:opacity-50"

  def apply(mods: Modifier[HtmlElement]*): HtmlElement =
    button(
      typ := "button",
      role := "switch",
      aria.checked := "false",
      cls := s"$base bg-input",
      span(
        cls := "pointer-events-none block size-4 rounded-full bg-background ring-0 transition-transform"
      ),
      mods
    )

  def apply(checkedVar: Var[Boolean], mods: Modifier[HtmlElement]*): HtmlElement =
    button(
      typ := "button",
      role := "switch",
      aria.checked <-- checkedVar.signal.map(_.toString),
      cls := base,
      cls <-- checkedVar.signal.map(if _ then "bg-primary" else "bg-input"),
      onClick --> { _ => checkedVar.update(!_) },
      span(
        cls := "pointer-events-none block size-4 rounded-full bg-background ring-0 transition-transform",
        cls <-- checkedVar.signal.map(if _ then "translate-x-4" else "translate-x-0")
      ),
      mods
    )
