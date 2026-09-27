package shadcnscalajs.webcomponents

import com.raquo.laminar.api.L.*
import shadcnscalajs.ui.{Calendar, RangeCalendar}

import scala.scalajs.js

/** `<sc-range-calendar start="2026-08-10" end="2026-08-15"></sc-range-calendar>` fires `sc-change` on user selection.
  */
class ScRangeCalendar extends ScElementBase:

  private val selectedVar = Var[Calendar.DateRange]((None, None))
  private val echo = EchoGuard[(Option[String], Option[String])]()
  private var reflecting = false

  private def reflect(name: String, value: Option[String]): Unit =
    value match
      case Some(iso) => if getAttribute(name) != iso then setAttribute(name, iso)
      case None      => if hasAttribute(name) then removeAttribute(name)

  private def readSelection(): Calendar.DateRange =
    val start = ScRangeCalendar.parseDate(Option(getAttribute("start")))
    val end = ScRangeCalendar.parseDate(Option(getAttribute("end")))
    (start, end) match
      case (Some(s), Some(e)) if s.getTime() > e.getTime() => (Some(s), None)
      case other                                           => other

  private def updateSelection(): Unit =
    val selection = readSelection()
    echo.wrote(ScRangeCalendar.isoRange(selection))
    selectedVar.set(selection)

  observeAttribute("start")(_ => if !reflecting then updateSelection())
  observeAttribute("end")(_ => if !reflecting then updateSelection())
  stringProperty("start")
  stringProperty("end")

  mount(
    RangeCalendar(selectedVar).amend(
      selectedVar.signal.changes --> Observer[Calendar.DateRange] { selection =>
        val iso = ScRangeCalendar.isoRange(selection)
        if !echo.isEcho(iso) then
          reflecting = true
          try
            reflect("start", iso._1)
            reflect("end", iso._2)
          finally reflecting = false
          emit("sc-change", js.Dynamic.literal(start = iso._1.orNull, end = iso._2.orNull))
      }
    )
  )

object ScRangeCalendar:
  def register(): Unit =
    ScElements.define("sc-range-calendar", js.constructorOf[ScRangeCalendar], "start", "end")

  private def parseDate(raw: Option[String]): Option[js.Date] =
    raw.filter(_.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}")).flatMap { iso =>
      val year = iso.substring(0, 4).toInt
      val month = iso.substring(5, 7).toInt
      val day = iso.substring(8, 10).toInt
      val date = new js.Date(2000, month - 1, day)
      date.setFullYear(year)
      if toIso(date) == iso then Some(date) else None
    }

  private def isoRange(range: Calendar.DateRange): (Option[String], Option[String]) =
    (range._1.map(toIso), range._2.map(toIso))

  private def toIso(date: js.Date): String =
    f"${date.getFullYear().toInt}%04d-${date.getMonth().toInt + 1}%02d-${date.getDate().toInt}%02d"
