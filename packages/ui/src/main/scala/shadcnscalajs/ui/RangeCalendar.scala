package shadcnscalajs.ui

import com.raquo.laminar.api.L.*

import scala.scalajs.js

/** shadcn/ui RangeCalendar — range-mode calendar grid, mirroring upstream `range-calendar.svelte` (bits-ui
  * RangeCalendar) as a thin wrapper over [[Calendar.rangeCalendar]].
  */
object RangeCalendar:

  def apply(selected: Var[Calendar.DateRange], mods: Modifier[HtmlElement]*): HtmlElement =
    Calendar.rangeCalendar(selected, _ => false, Calendar.CaptionLayout.Label, mods*)

  def apply(
      selected: Var[Calendar.DateRange],
      isDisabled: js.Date => Boolean,
      mods: Modifier[HtmlElement]*
  ): HtmlElement =
    Calendar.rangeCalendar(selected, isDisabled, Calendar.CaptionLayout.Label, mods*)

  def apply(
      selected: Var[Calendar.DateRange],
      isDisabled: js.Date => Boolean,
      captionLayout: Calendar.CaptionLayout,
      mods: Modifier[HtmlElement]*
  ): HtmlElement =
    Calendar.rangeCalendar(selected, isDisabled, captionLayout, mods*)
