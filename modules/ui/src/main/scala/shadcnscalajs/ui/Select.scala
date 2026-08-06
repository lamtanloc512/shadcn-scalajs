package shadcnscalajs.ui

import com.raquo.laminar.api.L.*

object Select:
  private val baseClasses =
    "select flex h-9 w-full items-center justify-between rounded-md border border-input bg-background px-3 py-2 text-sm shadow-xs outline-none focus-visible:border-ring focus-visible:ring-[3px] focus-visible:ring-ring/50"

  def apply(mods: Modifier[HtmlElement]*): HtmlElement =
    select(cls := baseClasses, mods)

  def stateful(selectedVar: Var[String], options: List[(String, String)], mods: Modifier[HtmlElement]*): HtmlElement =
    select(
      cls := baseClasses,
      value <-- selectedVar.signal,
      onChange.mapToValue --> { selected => selectedVar.set(selected) },
      options.map { case (optValue, optLabel) =>
        option(value := optValue, optLabel)
      },
      mods
    )
