package shadcnscalajs.ui

import com.raquo.laminar.api.L.*

import scala.scalajs.js

/** shadcn/ui DatePicker — a composition of a button-styled Popover trigger + Calendar, matching the canonical
  * date-picker.tsx recipe (shadcn/ui documents this as a composition, not a separate Radix primitive, either).
  */
object DatePicker:

  private val monthNames = List(
    "January",
    "February",
    "March",
    "April",
    "May",
    "June",
    "July",
    "August",
    "September",
    "October",
    "November",
    "December"
  )

  private val shortMonthNames = List(
    "Jan",
    "Feb",
    "Mar",
    "Apr",
    "May",
    "Jun",
    "Jul",
    "Aug",
    "Sep",
    "Oct",
    "Nov",
    "Dec"
  )

  private def formatSingle(d: js.Date): String =
    s"${monthNames(d.getMonth().toInt)} ${d.getDate().toInt}, ${d.getFullYear().toInt}"

  private def formatShort(d: js.Date): String =
    s"${shortMonthNames(d.getMonth().toInt)} ${d.getDate().toInt}, ${d.getFullYear().toInt}"

  private def formatRange(range: Calendar.DateRange): String =
    val (start, end) = range
    (start, end) match
      case (Some(s), Some(e)) => s"${formatShort(s)} - ${formatShort(e)}"
      case (Some(s), None)    => formatShort(s)
      case _                  => ""

  def apply(selected: Var[Option[js.Date]], placeholder: String = "Pick a date"): HtmlElement =
    Popover(
      Popover.trigger(
        cls := "inline-flex h-9 items-center justify-center gap-2 rounded-md border bg-background px-4 py-2 text-sm font-medium shadow-xs hover:bg-accent hover:text-accent-foreground",
        child.text <-- selected.signal.map {
          case Some(d) => formatSingle(d)
          case None    => placeholder
        }
      ),
      Popover.content(cls := "w-auto p-0", Calendar(selected))
    )

  def withRange(
      selected: Var[Calendar.DateRange],
      placeholder: String = "Pick a date range"
  ): HtmlElement =
    withRange(selected, placeholder, _ => false)

  def withRange(
      selected: Var[Calendar.DateRange],
      placeholder: String,
      isDisabled: js.Date => Boolean
  ): HtmlElement =
    Popover(
      Popover.trigger(
        cls := "inline-flex h-9 items-center justify-center gap-2 rounded-md border bg-background px-4 py-2 text-sm font-medium shadow-xs hover:bg-accent hover:text-accent-foreground",
        child.text <-- selected.signal.map { range =>
          val text = formatRange(range)
          if text.nonEmpty then text else placeholder
        }
      ),
      Popover.content(cls := "w-auto p-0", RangeCalendar(selected, isDisabled))
    )
