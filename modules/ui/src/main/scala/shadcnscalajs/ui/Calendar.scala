package shadcnscalajs.ui

import com.raquo.laminar.api.L.*

import scala.scalajs.js

/** shadcn/ui Calendar — a month-grid date picker. shadcn/ui wraps react-day-picker / bits-ui; this is a from-scratch
  * grid built on native `js.Date` (no java.time dependency), covering single-date and range selection with month
  * navigation. Parts are rendered internally (not exported) so style-pack `[data-slot=calendar-*]` selectors still
  * match.
  */
object Calendar:

  type DateRange = (Option[js.Date], Option[js.Date])

  /** The month/year caption. `Label` is upstream's default — a static heading between the arrows. `Dropdown` is
    * `captionLayout="dropdown"`: month and year `<select>`s, so a date-of-birth picker doesn't page a hundred times.
    */
  enum CaptionLayout derives CanEqual:
    case Label, Dropdown

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
  private val shortMonthNames =
    List("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
  private val dayLabels = List("Su", "Mo", "Tu", "We", "Th", "Fr", "Sa")

  /** How far the year `<select>` reaches. A century back covers birth dates; a decade forward covers scheduling. The
    * viewed year is always folded in so navigating past the window with the arrows never blanks the dropdown.
    */
  private val yearsBack = 100
  private val yearsForward = 10

  private val singleDayClasses =
    "flex size-8 flex-col items-center justify-center gap-1 rounded-md p-0 text-sm leading-none font-normal whitespace-nowrap select-none not-data-selected:hover:bg-accent/50 not-data-selected:hover:text-accent-foreground data-[selected]:bg-primary data-[selected]:text-primary-foreground [&[data-today]:not([data-selected])]:bg-accent [&[data-today]:not([data-selected])]:text-accent-foreground data-[disabled]:pointer-events-none data-[disabled]:text-muted-foreground data-[disabled]:opacity-50 focus:relative focus:border-ring focus:ring-ring/50"

  private val rangeDayClasses =
    "flex size-8 flex-col items-center justify-center gap-1 rounded-md p-0 text-sm leading-none font-normal whitespace-nowrap select-none not-data-selected:hover:bg-accent/50 not-data-selected:hover:text-accent-foreground data-[range-middle]:rounded-none data-[range-middle]:bg-accent data-[range-middle]:text-accent-foreground [&[data-today]:not([data-selected])]:bg-accent [&[data-today]:not([data-selected])]:text-accent-foreground data-[range-start]:bg-primary data-[range-start]:text-primary-foreground data-[range-end]:bg-primary data-[range-end]:text-primary-foreground data-[disabled]:pointer-events-none data-[disabled]:text-muted-foreground data-[disabled]:opacity-50 focus:relative focus:border-ring focus:ring-ring/50"

  /** Month arrows are the `variant="ghost"` icon buttons upstream, so they take their look from [[Button.appearance]]
    * and follow the active style pack's radius and size rather than freezing at `rounded-md size-8`.
    */
  private val navButtonStyle: Modifier[HtmlElement] =
    Seq[Modifier[HtmlElement]](
      Button.appearance(Button.Variant.Ghost, Button.Size.IconSm),
      cls := "bg-transparent select-none rtl:rotate-180 [&_svg]:size-4"
    )

  private def startOfMonth(d: js.Date): js.Date = new js.Date(d.getFullYear().toInt, d.getMonth().toInt, 1)

  private def dayTime(d: js.Date): Double =
    new js.Date(d.getFullYear().toInt, d.getMonth().toInt, d.getDate().toInt).getTime()

  private def sameDay(a: js.Date, b: js.Date): Boolean =
    a.getFullYear() == b.getFullYear() && a.getMonth() == b.getMonth() && a.getDate() == b.getDate()

  private def sameMonth(a: js.Date, b: js.Date): Boolean =
    a.getFullYear() == b.getFullYear() && a.getMonth() == b.getMonth()

  private def compareDay(a: js.Date, b: js.Date): Int =
    val diff = dayTime(a) - dayTime(b)
    if diff < 0 then -1 else if diff > 0 then 1 else 0

  private def daysInMonth(d: js.Date): Int =
    new js.Date(d.getFullYear().toInt, d.getMonth().toInt + 1, 0).getDate().toInt

  private def optionalDataAttr(name: String, enabled: Boolean): Modifier[HtmlElement] =
    if enabled then dataAttr(name) := "" else emptyMod

  private enum SelectionMode:
    case Single, Range

  def apply(selected: Var[Option[js.Date]], mods: Modifier[HtmlElement]*): HtmlElement =
    apply(selected, _ => false, CaptionLayout.Label, mods*)

  /** `isDisabled` covers upstream's `minValue`/`maxValue`/`disabled` matcher props: a date-of-birth picker passes
    * `_.getTime() > today`, a booking picker excludes taken days. It sits before the varargs so a predicate is never
    * mistaken for a `Modifier`.
    */
  def apply(
      selected: Var[Option[js.Date]],
      isDisabled: js.Date => Boolean,
      mods: Modifier[HtmlElement]*
  ): HtmlElement =
    apply(selected, isDisabled, CaptionLayout.Label, mods*)

  /** `captionLayout` is a distinct type from both the predicate and a `Modifier`, so these caption overloads never
    * collide with the ones above under overload resolution.
    */
  def apply(
      selected: Var[Option[js.Date]],
      captionLayout: CaptionLayout,
      mods: Modifier[HtmlElement]*
  ): HtmlElement =
    apply(selected, _ => false, captionLayout, mods*)

  def apply(
      selected: Var[Option[js.Date]],
      isDisabled: js.Date => Boolean,
      captionLayout: CaptionLayout,
      mods: Modifier[HtmlElement]*
  ): HtmlElement =
    build(
      () => (selected.now(), None),
      selected.signal.map(s => (s, None)),
      range => selected.set(range._1),
      SelectionMode.Single,
      isDisabled,
      captionLayout,
      "calendar",
      mods*
    )

  def range(selected: Var[DateRange], mods: Modifier[HtmlElement]*): HtmlElement =
    range(selected, _ => false, CaptionLayout.Label, mods*)

  def range(
      selected: Var[DateRange],
      isDisabled: js.Date => Boolean,
      mods: Modifier[HtmlElement]*
  ): HtmlElement =
    range(selected, isDisabled, CaptionLayout.Label, mods*)

  def range(
      selected: Var[DateRange],
      isDisabled: js.Date => Boolean,
      captionLayout: CaptionLayout,
      mods: Modifier[HtmlElement]*
  ): HtmlElement =
    build(
      () => selected.now(),
      selected.signal,
      selected.set,
      SelectionMode.Range,
      isDisabled,
      captionLayout,
      "calendar",
      mods*
    )

  private[ui] def rangeCalendar(
      selected: Var[DateRange],
      isDisabled: js.Date => Boolean,
      captionLayout: CaptionLayout,
      mods: Modifier[HtmlElement]*
  ): HtmlElement =
    build(
      () => selected.now(),
      selected.signal,
      selected.set,
      SelectionMode.Range,
      isDisabled,
      captionLayout,
      "range-calendar",
      mods*
    )

  private def build(
      getSelection: () => DateRange,
      selectionSignal: Signal[DateRange],
      setSelection: DateRange => Unit,
      mode: SelectionMode,
      isDisabled: js.Date => Boolean,
      captionLayout: CaptionLayout,
      slotPrefix: String,
      mods: Modifier[HtmlElement]*
  ): HtmlElement =
    val today = new js.Date()
    val initialRange = getSelection()
    val initialAnchor = initialRange._1.orElse(initialRange._2).getOrElse(today)
    val viewMonth = Var(startOfMonth(initialAnchor))

    def shiftMonth(delta: Int): Unit =
      val current = viewMonth.now()
      viewMonth.set(new js.Date(current.getFullYear().toInt, current.getMonth().toInt + delta, 1))

    def setMonth(month: Int): Unit =
      val current = viewMonth.now()
      viewMonth.set(new js.Date(current.getFullYear().toInt, month, 1))

    def setYear(year: Int): Unit =
      val current = viewMonth.now()
      viewMonth.set(new js.Date(year, current.getMonth().toInt, 1))

    /** A native `<select>` dressed as the small caption button in the mockup: the pack-styled border and radius come
      * from `Button.appearance`, and the real control sits invisibly on top so the browser still owns the option list.
      */
    def captionSelect(
        role: String,
        valueSignal: Signal[String],
        onPick: String => Unit,
        options: Seq[(String, String)]
    ): HtmlElement =
      div(
        dataAttr("slot") := s"$slotPrefix-dropdown-root",
        cls := "relative inline-flex items-center",
        Button.appearance(Button.Variant.Outline, Button.Size.Sm),
        cls := "h-7 gap-1 pr-1.5 pl-2 font-medium",
        span(
          dataAttr("slot") := s"$slotPrefix-dropdown-label",
          cls := "select-none",
          child.text <-- valueSignal.map { v =>
            options.collectFirst { case (value, label) if value == v => label }.getOrElse(v)
          }
        ),
        Icons.chevronDown(svg.cls := "size-3.5 opacity-60"),
        select(
          dataAttr("slot") := s"$slotPrefix-dropdown",
          aria.label := role,
          cls := "absolute inset-0 cursor-pointer opacity-0",
          controlled(
            value <-- valueSignal,
            onChange.mapToValue --> { v => onPick(v) }
          ),
          options.map { case (optValue, label) => option(value := optValue, label) }
        )
      )

    def captionDropdowns(): HtmlElement =
      val currentYear = today.getFullYear().toInt
      val viewedYear = viewMonth.now().getFullYear().toInt
      val minYear = math.min(currentYear - yearsBack, viewedYear)
      val maxYear = math.max(currentYear + yearsForward, viewedYear)
      div(
        dataAttr("slot") := s"$slotPrefix-dropdowns",
        cls := "flex items-center gap-1.5",
        captionSelect(
          "Month",
          viewMonth.signal.map(_.getMonth().toInt.toString),
          v => v.toIntOption.foreach(setMonth),
          shortMonthNames.zipWithIndex.map { case (name, idx) => (idx.toString, name) }
        ),
        captionSelect(
          "Year",
          viewMonth.signal.map(_.getFullYear().toInt.toString),
          v => v.toIntOption.foreach(setYear),
          (minYear to maxYear).map(y => (y.toString, y.toString))
        )
      )

    def selectSingle(date: js.Date): Unit =
      if !isDisabled(date) then setSelection((Some(date), None))

    def selectRange(date: js.Date): Unit =
      if isDisabled(date) then return
      val (start, end) = getSelection()
      (start, end) match
        case (Some(_), Some(_)) => setSelection((Some(date), None))
        case (Some(s), None) =>
          if sameDay(s, date) then setSelection((Some(date), Some(date)))
          else if compareDay(date, s) < 0 then setSelection((Some(date), Some(s)))
          else setSelection((Some(s), Some(date)))
        case _ => setSelection((Some(date), None))

    def dayModifiers(date: js.Date, range: DateRange): (Boolean, Boolean, Boolean, Boolean, Boolean) =
      val (start, end) = range
      val isToday = sameDay(date, today)
      mode match
        case SelectionMode.Single =>
          val selected = start.exists(sameDay(_, date))
          (selected, false, false, false, isToday && !selected)
        case SelectionMode.Range =>
          val (rangeStart, rangeEnd) = (start, end) match
            case (Some(s), Some(e)) =>
              if compareDay(s, e) <= 0 then (Some(s), Some(e)) else (Some(e), Some(s))
            case other => other
          val isStart = rangeStart.exists(sameDay(_, date))
          val isEnd = rangeEnd.exists(sameDay(_, date))
          val inMiddle = (rangeStart, rangeEnd) match
            case (Some(s), Some(e)) =>
              val t = dayTime(date)
              t > dayTime(s) && t < dayTime(e)
            case _ => false
          val selected = isStart || isEnd || inMiddle
          (selected, isStart, isEnd, inMiddle, isToday && !selected)

    /** A selection can also arrive from outside — a text field the caller keeps in sync, or a preset button — and the
      * grid has to be showing that month for the choice to be visible at all. Clicking a day never triggers a move,
      * since every rendered day belongs to the displayed month. In range mode the end date leads: after picking a start
      * in August and paging to September, following the start would drag the view back.
      */
    val followSelectedMonth: Modifier[HtmlElement] =
      selectionSignal --> { range =>
        range._2.orElse(range._1).foreach { date =>
          if !sameMonth(date, viewMonth.now()) then viewMonth.set(startOfMonth(date))
        }
      }

    val cellSlot = s"$slotPrefix-cell"
    val daySlot = s"$slotPrefix-day"
    val dayButtonClasses = if mode == SelectionMode.Range then rangeDayClasses else singleDayClasses
    val gridClasses =
      if slotPrefix == "range-calendar" then "mt-4 flex w-full border-collapse flex-col gap-1"
      else "flex w-full border-collapse flex-col"
    val monthClasses =
      if slotPrefix == "range-calendar" then "flex flex-col"
      else "flex w-full flex-col gap-4"

    div(
      dataAttr("slot") := slotPrefix,
      cls := "cn-calendar group/calendar w-fit bg-background p-3 [--cell-size:--spacing(8)] [--cell-radius:var(--radius-md)] in-data-[slot=card-content]:bg-transparent in-data-[slot=popover-content]:bg-transparent",
      followSelectedMonth,
      div(
        dataAttr("slot") := s"$slotPrefix-months",
        cls := "relative flex flex-col gap-4 md:flex-row",
        div(
          dataAttr("slot") := s"$slotPrefix-month",
          cls := monthClasses,
          div(
            cls := "relative flex w-full items-center justify-center pt-1",
            navTag(
              dataAttr("slot") := s"$slotPrefix-nav",
              cls := "absolute inset-x-0 top-0 flex w-full items-center justify-between gap-1",
              button(
                typ := "button",
                dataAttr("slot") := s"$slotPrefix-prev-button",
                navButtonStyle,
                aria.label := "Previous month",
                onClick --> { _ => shiftMonth(-1) },
                Icons.chevronRight(svg.cls := "rotate-180")
              ),
              button(
                typ := "button",
                dataAttr("slot") := s"$slotPrefix-next-button",
                navButtonStyle,
                aria.label := "Next month",
                onClick --> { _ => shiftMonth(1) },
                Icons.chevronRight()
              )
            ),
            div(
              dataAttr("slot") := s"$slotPrefix-header",
              cls := "flex h-8 w-full items-center justify-center gap-1.5 text-sm font-medium",
              captionLayout match
                case CaptionLayout.Label =>
                  div(
                    dataAttr("slot") := s"$slotPrefix-heading",
                    cls := "px-8 text-sm font-medium",
                    child.text <-- viewMonth.signal.map(d =>
                      s"${monthNames(d.getMonth().toInt)} ${d.getFullYear().toInt}"
                    )
                  )
                case CaptionLayout.Dropdown => captionDropdowns()
            )
          ),
          div(
            dataAttr("slot") := s"$slotPrefix-grid",
            cls := gridClasses,
            div(
              dataAttr("slot") := s"$slotPrefix-grid-head",
              div(
                dataAttr("slot") := s"$slotPrefix-grid-row",
                cls := "grid grid-cols-7 select-none",
                dayLabels.map { l =>
                  div(
                    dataAttr("slot") := s"$slotPrefix-head-cell",
                    cls := "w-8 rounded-md text-center text-[0.8rem] font-normal text-muted-foreground",
                    l
                  )
                }
              )
            ),
            div(
              dataAttr("slot") := s"$slotPrefix-grid-body",
              div(
                dataAttr("slot") := s"$slotPrefix-grid-row",
                cls := "mt-2 grid w-full grid-cols-7",
                children <-- viewMonth.signal
                  .combineWithFn(selectionSignal)((month, range) => (month, range))
                  .map { case (monthView, dateRange) =>
                    val firstWeekday = startOfMonth(monthView).getDay().toInt
                    val total = daysInMonth(monthView)
                    val leading = List.fill(firstWeekday)(
                      div(
                        dataAttr("slot") := cellSlot,
                        cls := "relative size-8 p-0 text-center text-sm"
                      ): HtmlElement
                    )
                    val days = (1 to total).toList.map { dayNum =>
                      val date = new js.Date(monthView.getFullYear().toInt, monthView.getMonth().toInt, dayNum)
                      val (selected, isStart, isEnd, inMiddle, showToday) =
                        dayModifiers(date, dateRange)
                      div(
                        dataAttr("slot") := cellSlot,
                        cls := "relative size-8 p-0 text-center text-sm focus-within:z-20",
                        optionalDataAttr("selected", selected),
                        button(
                          typ := "button",
                          dataAttr("slot") := daySlot,
                          cls := dayButtonClasses,
                          optionalDataAttr("selected", selected),
                          optionalDataAttr("today", showToday),
                          optionalDataAttr("range-start", isStart),
                          optionalDataAttr("range-middle", inMiddle),
                          optionalDataAttr("range-end", isEnd),
                          optionalDataAttr("disabled", isDisabled(date)),
                          onClick --> { _ =>
                            mode match
                              case SelectionMode.Single => selectSingle(date)
                              case SelectionMode.Range  => selectRange(date)
                          },
                          dayNum.toString
                        )
                      )
                    }
                    leading ++ days
                  }
              )
            )
          )
        )
      ),
      mods
    )
