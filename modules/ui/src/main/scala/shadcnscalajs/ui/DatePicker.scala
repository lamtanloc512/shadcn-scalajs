package shadcnscalajs.ui

import com.raquo.laminar.api.L.*

import scala.scalajs.js

/** shadcn/ui DatePicker — a composition of a button-styled Popover trigger + Calendar, matching the canonical
  * date-picker recipe (shadcn/ui documents this as a composition, not a separate Radix primitive).
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

  /** The trigger is a real outline button — [[Button.appearance]] rather than a copied class string, so its height,
    * padding, and radius follow the active style pack like every other button on the page. `justify-start!` has to
    * outrank the button's own `justify-center`, which Tailwind may emit later.
    */
  private val triggerStyle: Modifier[HtmlElement] =
    Seq[Modifier[HtmlElement]](
      Button.appearance(Button.Variant.Outline),
      cls := "w-[280px] justify-start!"
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
        dataAttr("slot") := "popover-trigger",
        triggerStyle,
        Icons.calendar(),
        span(
          child.text <-- selected.signal.map {
            case Some(d) => formatSingle(d)
            case None    => placeholder
          },
          cls <-- selected.signal.map {
            case None => "text-muted-foreground"
            case _    => ""
          }
        )
      ),
      // `!` because packs set padding on `.cn-popover-content` from an unlayered rule; the calendar must sit flush.
      Popover.content(
        Floating.Placement(align = Floating.Align.Start),
        "w-auto p-0!",
        Calendar(selected, Calendar.CaptionLayout.Dropdown)
      )
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
        dataAttr("slot") := "popover-trigger",
        triggerStyle,
        Icons.calendar(),
        span(
          child.text <-- selected.signal.map { range =>
            val text = formatRange(range)
            if text.nonEmpty then text else placeholder
          },
          cls <-- selected.signal.map { range =>
            if formatRange(range).isEmpty then "text-muted-foreground" else ""
          }
        )
      ),
      Popover.content(
        Floating.Placement(align = Floating.Align.Start),
        "w-auto p-0!",
        RangeCalendar(selected, isDisabled, Calendar.CaptionLayout.Dropdown)
      )
    )
