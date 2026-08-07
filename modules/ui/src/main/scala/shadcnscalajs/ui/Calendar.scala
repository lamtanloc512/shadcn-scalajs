package shadcnscalajs.ui

import com.raquo.laminar.api.L.*

import scala.scalajs.js

/** shadcn/ui Calendar — a month-grid date picker. shadcn/ui wraps react-day-picker; this is a from-scratch grid built
  * on native `js.Date` (no java.time dependency), covering single-date and range selection with month navigation.
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

  private val dayButtonBaseClasses =
    "inline-flex size-8 flex-col items-center justify-center gap-1 rounded-md p-0 text-sm font-normal leading-none whitespace-nowrap select-none hover:bg-accent/50 hover:text-accent-foreground"

  private val rangeDayClasses =
    "data-[range-middle]:rounded-none data-[range-start]:bg-primary data-[range-start]:text-primary-foreground data-[range-start]:hover:text-foreground data-[range-end]:bg-primary data-[range-end]:text-primary-foreground data-[range-end]:hover:text-foreground data-[disabled]:pointer-events-none data-[disabled]:text-muted-foreground data-[disabled]:opacity-50"

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
      mods*
    )

  private def build(
      getSelection: () => DateRange,
      selectionSignal: Signal[DateRange],
      setSelection: DateRange => Unit,
      mode: SelectionMode,
      isDisabled: js.Date => Boolean,
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
      val disabled = isDisabled(date)
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

    div(
      cls := "w-fit rounded-md border p-3",
      div(
        cls := "flex items-center justify-between pb-2",
        button(
          typ := "button",
          cls := "inline-flex size-7 items-center justify-center rounded-md border hover:bg-accent hover:text-accent-foreground",
          aria.label := "Previous month",
          onClick --> { _ => shiftMonth(-1) },
          "‹"
        ),
        div(
          cls := "text-sm font-medium",
          child.text <-- viewMonth.signal.map(d => s"${monthNames(d.getMonth().toInt)} ${d.getFullYear().toInt}")
        ),
        button(
          typ := "button",
          cls := "inline-flex size-7 items-center justify-center rounded-md border hover:bg-accent hover:text-accent-foreground",
          aria.label := "Next month",
          onClick --> { _ => shiftMonth(1) },
          "›"
        )
      ),
      div(
        cls := "grid grid-cols-7 gap-1 pb-1",
        dayLabels.map(l => div(cls := "text-center text-xs font-normal text-muted-foreground", l))
      ),
      div(
        cls := "grid grid-cols-7 gap-1",
        children <-- viewMonth.signal.flatMapSwitch { monthView =>
          selectionSignal.map { dateRange =>
            val firstWeekday = startOfMonth(monthView).getDay().toInt
            val total = daysInMonth(monthView)
            val leading = List.fill(firstWeekday)(div(): HtmlElement)
            val days = (1 to total).toList.map { dayNum =>
              val date = new js.Date(monthView.getFullYear().toInt, monthView.getMonth().toInt, dayNum)
              val (selected, isStart, isEnd, inMiddle, showTodayBorder) =
                dayModifiers(date, dateRange)
              button(
                typ := "button",
                cls := dayButtonBaseClasses,
                cls := (if mode == SelectionMode.Range then rangeDayClasses else ""),
                cls := (if mode == SelectionMode.Single && selected then
                          "bg-primary text-primary-foreground hover:bg-primary hover:text-primary-foreground"
                        else ""),
                cls := (if showTodayBorder then "border bg-accent text-accent-foreground" else ""),
                optionalDataAttr("selected", selected),
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
            }
            leading ++ days
          }
        }
      ),
      mods
    )
