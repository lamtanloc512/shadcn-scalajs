package shadcnscalajs.webcomponents

import com.raquo.laminar.api.L.*
import shadcnscalajs.core.Tags.slotTag
import shadcnscalajs.ui.AlertDialog

import scala.scalajs.js

class ScAlertDialog extends ScElementBase:

  private val isOpenVar = Var(false)
  private val echo = EchoGuard[Boolean]()

  observeAttribute("open") { value =>
    val open = value.isDefined
    if isOpenVar.now() != open then
      echo.wrote(open)
      isOpenVar.set(open)
  }
  booleanProperty("open")

  mount(
    div(
      div(onClick --> { _ => isOpenVar.set(true) }, slotTag(nameAttr := "trigger")),
      AlertDialog(isOpenVar)(slotTag()),
      isOpenVar.signal.changes --> Observer[Boolean] { open =>
        val external = echo.isEcho(open)
        if open then
          if !this.hasAttribute("open") then this.setAttribute("open", "")
        else
          if this.hasAttribute("open") then this.removeAttribute("open")
          if !external then emit("sc-close", js.undefined)
      }
    )
  )

object ScAlertDialog:
  def register(): Unit =
    ScElements.define("sc-alert-dialog", js.constructorOf[ScAlertDialog], "open")
