package shadcnscalajs.webcomponents

import com.raquo.laminar.api.L.*
import shadcnscalajs.ui.Switch

import scala.scalajs.js

class ScSwitch extends ScElementBase:

  private val checkedVar = Var(false)
  private val disabledVar = Var(false)

  private val echo = EchoGuard[Boolean]()

  observeAttribute("checked")(v => { echo.wrote(v.isDefined); checkedVar.set(v.isDefined) })
  observeAttribute("disabled")(v => disabledVar.set(v.isDefined))
  booleanProperty("checked")
  booleanProperty("disabled")

  checkedVar.signal.changes.foreach { checked =>
    if !echo.isEcho(checked) then emit("sc-change", checked)
  }(unsafeWindowOwner)

  mount(Switch(checkedVar, disabled <-- disabledVar.signal))

object ScSwitch:
  def register(): Unit =
    ScElements.define("sc-switch", js.constructorOf[ScSwitch], "checked", "disabled")
