package shadcnscalajs.webcomponents

import com.raquo.laminar.api.L.*
import shadcnscalajs.ui.Slider

import scala.scalajs.js

class ScSlider extends ScElementBase:

  private val valueVar = Var(50.0)
  private val minVar = Var(0.0)
  private val maxVar = Var(100.0)
  private val stepVar = Var(1.0)
  private val revision = Var(0)

  private val echo = EchoGuard[Double]()

  private def bump(): Unit = revision.update(_ + 1)

  private def write(value: Double): Unit = { echo.wrote(value); valueVar.set(value) }

  observeAttribute("value")(v => write(ScSlider.parseSingle(v.orNull)))
  observeAttribute("min")(v => { v.flatMap(_.toDoubleOption).foreach(minVar.set); bump() })
  observeAttribute("max")(v => { v.flatMap(_.toDoubleOption).foreach(maxVar.set); bump() })
  observeAttribute("step")(v => { v.flatMap(_.toDoubleOption).foreach(stepVar.set); bump() })
  jsonProperty("value")(v => write(ScSlider.parseSingle(v)))
  stringProperty("min")
  stringProperty("max")
  stringProperty("step")

  valueVar.signal.changes.foreach(value => if !echo.isEcho(value) then emit("sc-change", value))(unsafeWindowOwner)

  mount(ScSlider.view(valueVar, minVar, maxVar, stepVar, revision))

object ScSlider:
  def register(): Unit =
    ScElements.define("sc-slider", js.constructorOf[ScSlider], "value", "min", "max", "step")

  private def parseSingle(value: js.Any): Double =
    if value == null then 50.0
    else if js.typeOf(value) == "number" then value.asInstanceOf[Double]
    else if js.typeOf(value) == "string" then
      val s = value.asInstanceOf[String].trim
      if s.startsWith("[") then
        ScElements.toArray(s).flatMap(_.headOption).map(_.asInstanceOf[Double]).getOrElse(50.0)
      else s.toDoubleOption.getOrElse(50.0)
    else if js.Array.isArray(value) then value.asInstanceOf[js.Array[Double]].headOption.getOrElse(50.0)
    else 50.0

  private def view(
      valueVar: Var[Double],
      minVar: Var[Double],
      maxVar: Var[Double],
      stepVar: Var[Double],
      revision: Var[Int]
  ): HtmlElement =
    div(
      children <-- revision.signal.map { _ =>
        List(Slider.single(valueVar, min = minVar.now(), max = maxVar.now(), step = stepVar.now()))
      }
    )
