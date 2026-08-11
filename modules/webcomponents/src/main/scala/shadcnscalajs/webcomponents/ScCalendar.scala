package shadcnscalajs.webcomponents

import com.raquo.laminar.api.L.*
import shadcnscalajs.ui.Calendar

import scala.scalajs.js

/** `<sc-calendar value="2026-08-10"></sc-calendar>` — fires `sc-change` with an ISO date string or null. */
class ScCalendar extends ScElementBase:

  private val selectedVar = Var(Option.empty[js.Date])

  private val echo = EchoGuard[Option[String]]()

  observeAttribute("value") { v =>
    val parsed = ScCalendar.parseDate(v)
    echo.wrote(parsed.map(ScCalendar.toIso))
    selectedVar.set(parsed)
  }
  stringProperty("value")

  selectedVar.signal.changes.foreach { date =>
    val iso = date.map(ScCalendar.toIso)
    if !echo.isEcho(iso) then emit("sc-change", iso.orNull)
  }(unsafeWindowOwner)

  mount(Calendar(selectedVar))

object ScCalendar:
  def register(): Unit =
    ScElements.define("sc-calendar", js.constructorOf[ScCalendar], "value")

  private def parseDate(value: Option[String]): Option[js.Date] =
    value.filter(_.nonEmpty).flatMap { raw =>
      val d = new js.Date(raw)
      if js.Dynamic.global.isNaN(d.getTime().asInstanceOf[js.Any]).asInstanceOf[Boolean] then None
      else Some(d)
    }

  private def toIso(date: js.Date): String =
    val y = date.getFullYear().toInt
    val m = date.getMonth().toInt + 1
    val d = date.getDate().toInt
    f"$y%04d-$m%02d-$d%02d"
