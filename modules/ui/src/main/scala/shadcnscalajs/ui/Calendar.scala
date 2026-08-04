package shadcnscalajs.ui

import com.raquo.laminar.api.L.*

import scala.scalajs.js

/** shadcn/ui Calendar — a month-grid date picker. shadcn/ui wraps react-day-picker; this is a from-scratch grid built
  * on native `js.Date` (no java.time dependency), covering single-date selection and month navigation — the common case
  * react-day-picker's default `mode="single"` covers. Range selection and disabled-date rules are not implemented.
  */
object Calendar:

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

  private def startOfMonth(d: js.Date): js.Date = new js.Date(d.getFullYear().toInt, d.getMonth().toInt, 1)

  private def sameDay(a: js.Date, b: js.Date): Boolean =
    a.getFullYear() == b.getFullYear() && a.getMonth() == b.getMonth() && a.getDate() == b.getDate()

  private def daysInMonth(d: js.Date): Int =
    new js.Date(d.getFullYear().toInt, d.getMonth().toInt + 1, 0).getDate().toInt

  def apply(selected: Var[Option[js.Date]], mods: Modifier[HtmlElement]*): HtmlElement =
    val today = new js.Date()
    val viewMonth = Var(startOfMonth(selected.now().getOrElse(today)))

    def shiftMonth(delta: Int): Unit =
      val current = viewMonth.now()
      viewMonth.set(new js.Date(current.getFullYear().toInt, current.getMonth().toInt + delta, 1))

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
        children <-- viewMonth.signal.combineWith(selected.signal).map { case (view, sel) =>
          val firstWeekday = startOfMonth(view).getDay().toInt
          val total = daysInMonth(view)
          val leading = List.fill(firstWeekday)(div(): HtmlElement)
          val days = (1 to total).toList.map { dayNum =>
            val date = new js.Date(view.getFullYear().toInt, view.getMonth().toInt, dayNum)
            val isSelected = sel.exists(sameDay(_, date))
            val isToday = sameDay(date, today)
            button(
              typ := "button",
              cls := "inline-flex size-8 items-center justify-center rounded-md text-sm hover:bg-accent hover:text-accent-foreground",
              cls := (if isSelected then
                        "bg-primary text-primary-foreground hover:bg-primary hover:text-primary-foreground"
                      else ""),
              cls := (if isToday && !isSelected then "border" else ""),
              onClick --> { _ => selected.set(Some(date)) },
              dayNum.toString
            )
          }
          leading ++ days
        }
      ),
      mods
    )
