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
  private val dayLabels = List("Su", "Mo", "Tu", "We", "Th", "Fr", "Sa")

  private val singleDayClasses =
    "flex size-8 flex-col items-center justify-center gap-1 rounded-md p-0 text-sm leading-none font-normal whitespace-nowrap select-none not-data-selected:hover:bg-accent/50 not-data-selected:hover:text-accent-foreground data-[selected]:bg-primary data-[selected]:text-primary-foreground data-[selected]:hover:text-foreground [&[data-today]:not([data-selected])]:bg-accent [&[data-today]:not([data-selected])]:text-accent-foreground data-[disabled]:pointer-events-none data-[disabled]:text-muted-foreground data-[disabled]:opacity-50 focus:relative focus:border-ring focus:ring-ring/50"

  private val rangeDayClasses =
    "flex size-8 flex-col items-center justify-center gap-1 rounded-md p-0 text-sm leading-none font-normal whitespace-nowrap select-none not-data-selected:hover:bg-accent/50 not-data-selected:hover:text-accent-foreground data-[range-middle]:rounded-none [&[data-today]:not([data-selected])]:bg-accent [&[data-today]:not([data-selected])]:text-accent-foreground data-[range-start]:bg-primary data-[range-start]:text-primary-foreground data-[range-start]:hover:text-foreground data-[range-end]:bg-primary data-[range-end]:text-primary-foreground data-[range-end]:hover:text-foreground data-[disabled]:pointer-events-none data-[disabled]:text-muted-foreground data-[disabled]:opacity-50 focus:relative focus:border-ring focus:ring-ring/50"

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
    build(
      () => (selected.now(), None),
      selected.signal.map(s => (s, None)),
      range => selected.set(range._1),
      SelectionMode.Single,
      _ => false,
      "calendar",
      mods*
    )

  def range(selected: Var[DateRange], mods: Modifier[HtmlElement]*): HtmlElement =
    range(selected, _ => false, mods*)

  def range(
      selected: Var[DateRange],
      isDisabled: js.Date => Boolean,
      mods: Modifier[HtmlElement]*
  ): HtmlElement =
    build(
      () => selected.now(),
      selected.signal,
      selected.set,
      SelectionMode.Range,
      isDisabled,
      "calendar",
      mods*
    )

  private[ui] def rangeCalendar(
      selected: Var[DateRange],
      isDisabled: js.Date => Boolean,
      mods: Modifier[HtmlElement]*
  ): HtmlElement =
    build(
      () => selected.now(),
      selected.signal,
      selected.set,
      SelectionMode.Range,
      isDisabled,
      "range-calendar",
      mods*
    )

  private def build(
      getSelection: () => DateRange,
      selectionSignal: Signal[DateRange],
      setSelection: DateRange => Unit,
      mode: SelectionMode,
      isDisabled: js.Date => Boolean,
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
              div(
                dataAttr("slot") := s"$slotPrefix-heading",
                cls := "px-8 text-sm font-medium",
                child.text <-- viewMonth.signal.map(d => s"${monthNames(d.getMonth().toInt)} ${d.getFullYear().toInt}")
              )
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
                children <-- viewMonth.signal.flatMapSwitch { monthView =>
                  selectionSignal.map { dateRange =>
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
                }
              )
            )
          )
        )
      ),
      mods
    )
