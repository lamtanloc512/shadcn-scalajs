package shadcnscalajs.webcomponents

import com.raquo.laminar.api.L.*
import shadcnscalajs.core.Tags.slotTag
import shadcnscalajs.ui.Checkbox

import scala.scalajs.js

class ScCheckbox extends ScElementBase:

  private val checkedVar = Var(false)
  private val indeterminateVar = Var(false)
  private val disabledVar = Var(false)

  private val echo = EchoGuard[Boolean]()

  observeAttribute("checked")(v => { echo.wrote(v.isDefined); checkedVar.set(v.isDefined) })
  observeAttribute("indeterminate")(v => indeterminateVar.set(v.isDefined))
  observeAttribute("disabled")(v => disabledVar.set(v.isDefined))
  booleanProperty("checked")
  booleanProperty("indeterminate")
  booleanProperty("disabled")

  mount(
    Checkbox(checkedVar, indeterminateVar.signal, slotTag(), disabled <-- disabledVar.signal).amend(
      checkedVar.signal.changes --> Observer[Boolean] { checked =>
        if !echo.isEcho(checked) then
          emit(
            "sc-change",
            js.Dynamic.literal(checked = checked, indeterminate = indeterminateVar.now())
          )
      }
    )
  )

object ScCheckbox:
  def register(): Unit =
    ScElements.define("sc-checkbox", js.constructorOf[ScCheckbox], "checked", "indeterminate", "disabled")
