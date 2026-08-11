package shadcnscalajs.webcomponents

import com.raquo.laminar.api.L.*
import shadcnscalajs.core.Tags.slotTag
import shadcnscalajs.ui.Checkbox

import scala.scalajs.js

class ScCheckbox extends ScElementBase:

  private val checkedVar = Var(false)
  private val indeterminateVar = Var(false)

  private val echo = EchoGuard[Boolean]()

  observeAttribute("checked")(v => { echo.wrote(v.isDefined); checkedVar.set(v.isDefined) })
  observeAttribute("indeterminate")(v => indeterminateVar.set(v.isDefined))
  booleanProperty("checked")
  booleanProperty("indeterminate")

  checkedVar.signal.changes.foreach { checked =>
    if !echo.isEcho(checked) then
      emit(
        "sc-change",
        js.Dynamic.literal(checked = checked, indeterminate = indeterminateVar.now())
      )
  }(unsafeWindowOwner)

  mount(Checkbox(checkedVar, indeterminateVar.signal, slotTag()))

object ScCheckbox:
  def register(): Unit =
    ScElements.define("sc-checkbox", js.constructorOf[ScCheckbox], "checked", "indeterminate")
