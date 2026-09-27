package shadcnscalajs.webcomponents

import com.raquo.laminar.api.L.*
import org.scalajs.dom
import shadcnscalajs.core.Tags.slotTag
import shadcnscalajs.ui.Dialog

import scala.scalajs.js

/** `<sc-dialog open>...</sc-dialog>` — Web Component export of shadcnscalajs.ui.Dialog. The `open` attribute is the
  * source of truth in both directions: setting/removing it opens/closes the dialog, and the element reflects it back
  * (e.g. after Escape or a backdrop click) plus fires a `close` CustomEvent so a plain JS caller can observe it via
  * `el.addEventListener("close", ...)`.
  */
class ScDialog extends ScElementBase:

  private val isOpenVar = Var(false)

  // Guarded against the Var's current value: without this, our own
  // `removeAttribute("open")` below re-triggers this observer, re-setting
  // the (already-false) Var, which re-fires `.changes` and double-dispatches
  // the `close` event for a single user action (found via manual testing).
  observeAttribute("open") { v =>
    val newOpen = v.isDefined
    if isOpenVar.now() != newOpen then isOpenVar.set(newOpen)
  }
  booleanProperty("open")

  mount(
    Dialog(isOpenVar)(
      onMountBind { _ =>
        isOpenVar.signal.changes --> { (open: Boolean) =>
          if !open then
            if this.hasAttribute("open") then this.removeAttribute("open")
            emit("sc-close", js.undefined)
        }
      },
      slotTag()
    )
  )

object ScDialog:
  def register(): Unit =
    ScElements.define("sc-dialog", js.constructorOf[ScDialog], "open")
