package shadcnscalajs.webcomponents

import com.raquo.laminar.api.L.*
import shadcnscalajs.core.Tags.slotTag
import shadcnscalajs.ui.Resizable

import scala.scalajs.js

class ScResizable extends ScElementBase:

  private val splitVar = Var(50.0)
  private val echo = EchoGuard[Double]()
  private var reflecting = false

  private def setSplit(raw: js.Any): Unit =
    val parsed =
      if raw == null then None
      else if js.typeOf(raw) == "number" then Some(raw.asInstanceOf[Double])
      else if js.typeOf(raw) == "string" then raw.asInstanceOf[String].trim.toDoubleOption
      else None
    parsed.filter(v => !v.isNaN && !v.isInfinity).foreach { value =>
      val clamped = math.max(10.0, math.min(90.0, value))
      echo.wrote(clamped)
      splitVar.set(clamped)
      reflect(clamped)
    }

  private def reflect(value: Double): Unit =
    val text = value.toString
    if this.getAttribute("split") != text then
      reflecting = true
      try this.setAttribute("split", text)
      finally reflecting = false

  observeAttribute("split")(v => if !reflecting then v.foreach(s => setSplit(s)))
  js.Dynamic.global.Object.defineProperty(
    this,
    "split",
    js.Dynamic.literal(
      configurable = true,
      get = (() => splitVar.now()): js.Function0[Double],
      set = ((v: js.Any) => setSplit(v)): js.Function1[js.Any, Unit]
    )
  )

  mount(
    Resizable
      .horizontal(splitVar)(slotTag(nameAttr := "left"), slotTag(nameAttr := "right"))
      .amend(
        splitVar.signal.changes --> Observer[Double] { value =>
          if !echo.isEcho(value) then
            reflect(value)
            emit("sc-change", value)
        }
      )
  )

object ScResizable:
  def register(): Unit =
    ScElements.define("sc-resizable", js.constructorOf[ScResizable], "split")
