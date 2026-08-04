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

  def apply(selected: Var[Option[js.Date]], placeholder: String = "Pick a date"): HtmlElement =
    Popover(
      Popover.trigger(
        cls := "inline-flex h-9 items-center justify-center gap-2 rounded-md border bg-background px-4 py-2 text-sm font-medium shadow-xs hover:bg-accent hover:text-accent-foreground",
        child.text <-- selected.signal.map {
          case Some(d) => s"${monthNames(d.getMonth().toInt)} ${d.getDate().toInt}, ${d.getFullYear().toInt}"
          case None    => placeholder
        }
      ),
      Popover.content(Calendar(selected))
    )
