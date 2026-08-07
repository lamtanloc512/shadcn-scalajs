package shadcnscalajs.ui

import com.raquo.laminar.api.L.*

import scala.scalajs.js

/** shadcn/ui RangeCalendar — range-mode calendar grid, mirroring upstream `range-calendar.svelte` (bits-ui
  * RangeCalendar) as a thin wrapper over [[Calendar.range]].
  */
object RangeCalendar:

  def apply(selected: Var[Calendar.DateRange], mods: Modifier[HtmlElement]*): HtmlElement =
    Calendar.range(selected, mods*)

  def apply(
      selected: Var[Calendar.DateRange],
      isDisabled: js.Date => Boolean,
      mods: Modifier[HtmlElement]*
  ): HtmlElement =
    Calendar.range(selected, isDisabled, mods*)
