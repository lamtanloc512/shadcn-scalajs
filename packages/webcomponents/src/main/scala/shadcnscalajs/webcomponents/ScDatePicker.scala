package shadcnscalajs.webcomponents

import com.raquo.laminar.api.L.*
import shadcnscalajs.ui.DatePicker

import scala.scalajs.js

/** Single-date picker with a nullable ISO date value. */
class ScDatePicker extends ScElementBase:

  private val selectedVar = Var(Option.empty[js.Date])
  private val placeholderVar = Var("Pick a date")
  private val echo = EchoGuard[Option[String]]()

  observeAttribute("value") { raw =>
    val parsed = ScDatePicker.parseDate(raw)
    echo.wrote(parsed.map(ScDatePicker.toIso))
    selectedVar.set(parsed)
  }
  observeAttribute("placeholder")(raw => placeholderVar.set(raw.getOrElse("Pick a date")))
  stringProperty("value")
  stringProperty("placeholder")

  mount(
    div(
      child <-- placeholderVar.signal.map(DatePicker(selectedVar, _))
    ).amend(
      selectedVar.signal.changes --> Observer[Option[js.Date]] { selected =>
        val iso = selected.map(ScDatePicker.toIso)
        if !echo.isEcho(iso) then
          if iso.isEmpty then removeAttribute("value")
          else setAttribute("value", iso.get)
          emit("sc-change", iso.orNull)
      }
    )
  )

object ScDatePicker:
  def register(): Unit =
    ScElements.define("sc-date-picker", js.constructorOf[ScDatePicker], "value", "placeholder")

  private def parseDate(raw: Option[String]): Option[js.Date] =
    raw.filter(_.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}")).flatMap { iso =>
      val year = iso.substring(0, 4).toInt
      val month = iso.substring(5, 7).toInt
      val day = iso.substring(8, 10).toInt
      val date = new js.Date(year, month - 1, day)
      if year >= 100 && date.getFullYear().toInt == year &&
        date.getMonth().toInt == month - 1 && date.getDate().toInt == day
      then Some(date)
      else None
    }

  private def toIso(date: js.Date): String =
    f"${date.getFullYear().toInt}%04d-${date.getMonth().toInt + 1}%02d-${date.getDate().toInt}%02d"
