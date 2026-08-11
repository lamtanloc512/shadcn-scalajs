package shadcnscalajs.webcomponents

import com.raquo.laminar.api.L.*
import shadcnscalajs.ui.Switch

import scala.scalajs.js

class ScSwitch extends ScElementBase:

  private val checkedVar = Var(false)

  private val echo = EchoGuard[Boolean]()

  observeAttribute("checked")(v => { echo.wrote(v.isDefined); checkedVar.set(v.isDefined) })
  booleanProperty("checked")

  checkedVar.signal.changes.foreach { checked =>
    if !echo.isEcho(checked) then emit("sc-change", checked)
  }(unsafeWindowOwner)

  mount(Switch(checkedVar))

object ScSwitch:
  def register(): Unit =
    ScElements.define("sc-switch", js.constructorOf[ScSwitch], "checked")
