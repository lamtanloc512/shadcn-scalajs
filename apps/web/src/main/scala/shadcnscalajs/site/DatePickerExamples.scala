package shadcnscalajs.site

import com.raquo.laminar.api.L.*
import shadcnscalajs.ui.*

import scala.scalajs.js

/** The demos upstream's date picker page lists below its main preview.
  *
  * shadcn/ui documents the date picker as a recipe rather than a component, so each of these is a different composition
  * of `Popover` + `Calendar` (+ `Input`) rather than a different prop on one primitive — which is exactly what makes
  * them worth showing.
  */
private[site] object DatePickerExamples:

  def all: Seq[DocExample] = Seq(
    DocExample(
      "date-of-birth-picker",
      "Date of Birth Picker",
      Some("A narrow trigger that closes on select, with future dates disabled."),
      dateOfBirth(),
      dateOfBirthCode
    ),
    DocExample(
      "picker-with-input",
      "Picker with Input",
      Some("The field and the calendar stay in sync: type a date, or pick one and the field rewrites itself."),
      pickerWithInput(),
      pickerWithInputCode
    ),
    DocExample(
      "date-and-time-picker",
      "Date and Time Picker",
      Some("A date trigger beside a native time input."),
      dateAndTime(),
      dateAndTimeCode
    ),
    DocExample(
      "natural-language-picker",
      "Natural Language Picker",
      Some("The field accepts phrases like \"tomorrow\", \"next friday\", or \"in 3 weeks\"."),
      naturalLanguage(),
      naturalLanguageCode
    )
  )

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

  private val weekdayNames =
    List("sunday", "monday", "tuesday", "wednesday", "thursday", "friday", "saturday")

  private def startOfDay(d: js.Date): js.Date =
    new js.Date(d.getFullYear().toInt, d.getMonth().toInt, d.getDate().toInt)

  private def today(): js.Date = startOfDay(new js.Date())

  private def addDays(d: js.Date, days: Int): js.Date =
    new js.Date(d.getFullYear().toInt, d.getMonth().toInt, d.getDate().toInt + days)

  private def addMonths(d: js.Date, months: Int): js.Date =
    new js.Date(d.getFullYear().toInt, d.getMonth().toInt + months, d.getDate().toInt)

  private def sameDay(a: js.Date, b: js.Date): Boolean =
    a.getFullYear() == b.getFullYear() && a.getMonth() == b.getMonth() && a.getDate() == b.getDate()

  private def twoDigits(n: Int): String = if n < 10 then s"0$n" else n.toString

  /** "August 09, 2026" — upstream's `{ day: "2-digit", month: "long", year: "numeric" }`. */
  private def formatLong(d: js.Date): String =
    s"${monthNames(d.getMonth().toInt)} ${twoDigits(d.getDate().toInt)}, ${d.getFullYear().toInt}"

  /** "8/9/2026" — upstream's bare `toLocaleDateString()`. */
  private def formatNumeric(d: js.Date): String =
    s"${d.getMonth().toInt + 1}/${d.getDate().toInt}/${d.getFullYear().toInt}"

  private def parseExact(text: String): Option[js.Date] =
    val parsed = js.Date.parse(text)
    if parsed.isNaN then None else Some(startOfDay(new js.Date(parsed)))

  private val inNUnits = """^in (\d+) (day|week|month|year)s?$""".r
  private val weekdayPhrase =
    """^(?:next |this |on )?(sunday|monday|tuesday|wednesday|thursday|friday|saturday)$""".r

  /** A small stand-in for upstream's `chrono-node` dependency: enough phrasings to make the demo behave, falling back
    * to the engine's own date parsing for anything spelled out.
    */
  private def parseNatural(raw: String): Option[js.Date] =
    val text = raw.trim.toLowerCase.replaceAll("\\s+", " ")
    val base = today()
    text match
      case ""           => None
      case "today"      => Some(base)
      case "tomorrow"   => Some(addDays(base, 1))
      case "yesterday"  => Some(addDays(base, -1))
      case "next week"  => Some(addDays(base, 7))
      case "last week"  => Some(addDays(base, -7))
      case "next month" => Some(addMonths(base, 1))
      case "next year"  => Some(addMonths(base, 12))
      case inNUnits(count, unit) =>
        count.toIntOption.map { n =>
          unit match
            case "day"   => addDays(base, n)
            case "week"  => addDays(base, n * 7)
            case "month" => addMonths(base, n)
            case _       => addMonths(base, n * 12)
        }
      case weekdayPhrase(name) =>
        val ahead = (weekdayNames.indexOf(name) - base.getDay().toInt + 7) % 7
        Some(addDays(base, if ahead == 0 then 7 else ahead))
      case other => parseExact(other)

  private def chevronTrigger(id: String, width: String, label: Signal[String]): Popover.Trigger =
    Popover.trigger(
      idAttr := id,
      Button.appearance(Button.Variant.Outline),
      // `!` because the button's own `justify-center` and `font-medium` are utilities too, and class order in the
      // markup does not decide which of two same-layer utilities wins.
      cls := s"$width justify-between! font-normal!",
      span(child.text <-- label),
      Icons.chevronDown()
    )

  private def calendarPanel(align: Floating.Align, body: HtmlElement): Popover.Content =
    Popover.content(
      Floating.Placement(align = align),
      // Packs set padding on `.cn-popover-content` from an unlayered rule, so the calendar needs `p-0!` to sit flush.
      "w-auto overflow-hidden p-0!",
      body
    )

  private def dateOfBirth(): HtmlElement =
    val selected = Var(Option.empty[js.Date])
    val anchor = Floating.anchor()
    val latest = today()
    div(
      cls := "flex flex-col gap-3",
      selected.signal.changes --> { _ => anchor.close() },
      Label(forId := "dob-date", cls := "px-1", "Date of birth"),
      Popover.withAnchor(anchor)(
        chevronTrigger("dob-date", "w-48", selected.signal.map(_.fold("Select date")(formatNumeric))),
        calendarPanel(
          Floating.Align.Start,
          Calendar(selected, _.getTime() > latest.getTime(), Calendar.CaptionLayout.Dropdown)
        )
      )
    )

  /** The field-plus-calendar pair both write the selection, so the calendar's write is told apart from the field's by
    * asking whether the text already spells the date that arrived — otherwise every keystroke would be reformatted out
    * from under the person typing.
    */
  private def syncFieldToCalendar(
      field: Var[String],
      selected: Var[Option[js.Date]],
      anchor: Floating.Anchor,
      parse: String => Option[js.Date]
  ): Modifier[HtmlElement] =
    selected.signal.changes --> { picked =>
      picked.foreach { date =>
        if !parse(field.now()).exists(sameDay(_, date)) then
          field.set(formatLong(date))
          anchor.close()
      }
    }

  /** The trigger sits inside the input, so the popover wrapper — which is its own positioning context — is what gets
    * placed, not the button.
    */
  private def inlineCalendarButton(anchor: Floating.Anchor, selected: Var[Option[js.Date]]): HtmlElement =
    div(
      cls := "absolute end-2 top-1/2 -translate-y-1/2",
      Popover.withAnchor(anchor)(
        Popover.trigger(
          Button.appearance(Button.Variant.Ghost, Button.Size.IconSm),
          cls := "size-6",
          Icons.calendar(svg.cls := "size-3.5"),
          span(cls := "sr-only", "Select date")
        ),
        calendarPanel(Floating.Align.End, Calendar(selected, Calendar.CaptionLayout.Dropdown))
      )
    )

  private def openOnArrowDown(anchor: Floating.Anchor): Modifier[HtmlElement] =
    onKeyDown --> { ev =>
      if ev.key == "ArrowDown" then
        ev.preventDefault()
        anchor.open()
    }

  private def pickerWithInput(): HtmlElement =
    val initial = today()
    val selected = Var(Option(initial))
    val field = Var(formatLong(initial))
    val anchor = Floating.anchor()
    div(
      cls := "flex flex-col gap-3",
      syncFieldToCalendar(field, selected, anchor, parseExact),
      Label(forId := "subscription-date", cls := "px-1", "Subscription Date"),
      div(
        cls := "relative flex gap-2",
        Input(
          idAttr := "subscription-date",
          cls := "bg-background pe-10",
          placeholder := "June 01, 2025",
          controlled(
            value <-- field.signal,
            onInput.mapToValue --> { typed =>
              field.set(typed)
              parseExact(typed).foreach(date => selected.set(Some(date)))
            }
          ),
          openOnArrowDown(anchor)
        ),
        inlineCalendarButton(anchor, selected)
      )
    )

  private def dateAndTime(): HtmlElement =
    val selected = Var(Option.empty[js.Date])
    val anchor = Floating.anchor()
    div(
      cls := "flex gap-4",
      selected.signal.changes --> { _ => anchor.close() },
      div(
        cls := "flex flex-col gap-3",
        Label(forId := "meeting-date", cls := "px-1", "Date"),
        Popover.withAnchor(anchor)(
          chevronTrigger("meeting-date", "w-32", selected.signal.map(_.fold("Select date")(formatNumeric))),
          calendarPanel(Floating.Align.Start, Calendar(selected, Calendar.CaptionLayout.Dropdown))
        )
      ),
      div(
        cls := "flex flex-col gap-3",
        Label(forId := "meeting-time", cls := "px-1", "Time"),
        Input(
          idAttr := "meeting-time",
          typ := "time",
          stepAttr := "1",
          defaultValue := "10:30:00",
          cls := "w-32 appearance-none bg-background [&::-webkit-calendar-picker-indicator]:hidden"
        )
      )
    )

  private def naturalLanguage(): HtmlElement =
    val opening = "In 2 days"
    val selected = Var(parseNatural(opening))
    val field = Var(opening)
    val anchor = Floating.anchor()
    div(
      cls := "flex flex-col gap-3",
      syncFieldToCalendar(field, selected, anchor, parseNatural),
      Label(forId := "schedule-date", cls := "px-1", "Schedule Date"),
      div(
        cls := "relative flex gap-2",
        Input(
          idAttr := "schedule-date",
          cls := "bg-background pe-10",
          placeholder := "Tomorrow or next week",
          controlled(
            value <-- field.signal,
            onInput.mapToValue --> { typed =>
              field.set(typed)
              parseNatural(typed).foreach(date => selected.set(Some(date)))
            }
          ),
          openOnArrowDown(anchor)
        ),
        inlineCalendarButton(anchor, selected)
      ),
      div(
        cls := "px-1 text-sm text-muted-foreground",
        "Your post will be published on ",
        span(cls := "font-medium", child.text <-- selected.signal.map(_.fold("a date you pick")(formatLong))),
        "."
      )
    )

  private val dateOfBirthCode =
    """val selected = Var(Option.empty[js.Date])
val anchor = Floating.anchor()
val latest = new js.Date()

div(
  cls := "flex flex-col gap-3",
  selected.signal.changes --> { _ => anchor.close() },
  Label(forId := "dob-date", cls := "px-1", "Date of birth"),
  Popover.withAnchor(anchor)(
    Popover.trigger(
      idAttr := "dob-date",
      Button.appearance(Button.Variant.Outline),
      cls := "w-48 justify-between! font-normal!",
      span(child.text <-- selected.signal.map(_.fold("Select date")(format))),
      Icons.chevronDown()
    ),
    Popover.content(
      Floating.Placement(align = Floating.Align.Start),
      "w-auto overflow-hidden p-0!",
      Calendar(selected, _.getTime() > latest.getTime(), Calendar.CaptionLayout.Dropdown)
    )
  )
)"""

  private val pickerWithInputCode =
    """val selected = Var(Option(new js.Date()))
val field = Var(format(new js.Date()))
val anchor = Floating.anchor()

div(
  cls := "flex flex-col gap-3",
  // Only a calendar pick rewrites the field: if the text already spells that
  // date, the change came from typing and must be left alone.
  selected.signal.changes --> { picked =>
    picked.foreach { date =>
      if !parse(field.now()).exists(sameDay(_, date)) then
        field.set(format(date))
        anchor.close()
    }
  },
  Label(forId := "subscription-date", cls := "px-1", "Subscription Date"),
  div(
    cls := "relative flex gap-2",
    Input(
      idAttr := "subscription-date",
      cls := "bg-background pe-10",
      controlled(
        value <-- field.signal,
        onInput.mapToValue --> { typed =>
          field.set(typed)
          parse(typed).foreach(date => selected.set(Some(date)))
        }
      )
    ),
    div(
      cls := "absolute end-2 top-1/2 -translate-y-1/2",
      Popover.withAnchor(anchor)(
        Popover.trigger(
          Button.appearance(Button.Variant.Ghost, Button.Size.IconSm),
          cls := "size-6",
          Icons.calendar(cls := "size-3.5"),
          span(cls := "sr-only", "Select date")
        ),
        Popover.content(
          Floating.Placement(align = Floating.Align.End),
          "w-auto overflow-hidden p-0!",
          Calendar(selected, Calendar.CaptionLayout.Dropdown)
        )
      )
    )
  )
)"""

  private val dateAndTimeCode =
    """val selected = Var(Option.empty[js.Date])
val anchor = Floating.anchor()

div(
  cls := "flex gap-4",
  selected.signal.changes --> { _ => anchor.close() },
  div(
    cls := "flex flex-col gap-3",
    Label(forId := "meeting-date", cls := "px-1", "Date"),
    Popover.withAnchor(anchor)(
      Popover.trigger(
        idAttr := "meeting-date",
        Button.appearance(Button.Variant.Outline),
        cls := "w-32 justify-between! font-normal!",
        span(child.text <-- selected.signal.map(_.fold("Select date")(format))),
        Icons.chevronDown()
      ),
      Popover.content(
        Floating.Placement(align = Floating.Align.Start),
        "w-auto overflow-hidden p-0!",
        Calendar(selected, Calendar.CaptionLayout.Dropdown)
      )
    )
  ),
  div(
    cls := "flex flex-col gap-3",
    Label(forId := "meeting-time", cls := "px-1", "Time"),
    Input(
      idAttr := "meeting-time",
      typ := "time",
      stepAttr := "1",
      defaultValue := "10:30:00",
      cls := "w-32 appearance-none bg-background [&::-webkit-calendar-picker-indicator]:hidden"
    )
  )
)"""

  private val naturalLanguageCode =
    """// parseNatural understands "today", "tomorrow", "next friday",
// "in 3 weeks", and falls back to js.Date.parse for spelled-out dates.
val selected = Var(parseNatural("In 2 days"))
val field = Var("In 2 days")
val anchor = Floating.anchor()

div(
  cls := "flex flex-col gap-3",
  selected.signal.changes --> { picked =>
    picked.foreach { date =>
      if !parseNatural(field.now()).exists(sameDay(_, date)) then
        field.set(format(date))
        anchor.close()
    }
  },
  Label(forId := "schedule-date", cls := "px-1", "Schedule Date"),
  div(
    cls := "relative flex gap-2",
    Input(
      idAttr := "schedule-date",
      cls := "bg-background pe-10",
      placeholder := "Tomorrow or next week",
      controlled(
        value <-- field.signal,
        onInput.mapToValue --> { typed =>
          field.set(typed)
          parseNatural(typed).foreach(date => selected.set(Some(date)))
        }
      ),
      onKeyDown --> { ev =>
        if ev.key == "ArrowDown" then
          ev.preventDefault()
          anchor.open()
      }
    ),
    calendarButtonInsideField(anchor, selected)
  ),
  div(
    cls := "px-1 text-sm text-muted-foreground",
    "Your post will be published on ",
    span(cls := "font-medium", child.text <-- selected.signal.map(_.fold("a date you pick")(format))),
    "."
  )
)"""
