package shadcnscalajs.webcomponents

import com.raquo.laminar.api.L.*
import shadcnscalajs.core.Tags.slotTag
import shadcnscalajs.ui.Sonner

import scala.scalajs.js

/** One toaster per page. Sonner's queue is global, so multiple instances would render the same notifications. */
class ScSonner extends ScElementBase:
  // Toaster portals into document.body. Mark its list as a component scope so its CSS stays local.
  mount(div(slotTag(), Sonner.Toaster(dataAttr("sc-component") := "")))

  private val api = this.asInstanceOf[js.Dynamic]
  api.updateDynamic("toast")(((message: String) => Sonner.toast(message)): js.Function1[String, String])
  api.updateDynamic("success")(((message: String) => Sonner.success(message)): js.Function1[String, String])
  api.updateDynamic("error")(((message: String) => Sonner.error(message)): js.Function1[String, String])
  api.updateDynamic("info")(((message: String) => Sonner.info(message)): js.Function1[String, String])
  api.updateDynamic("warning")(((message: String) => Sonner.warning(message)): js.Function1[String, String])
  api.updateDynamic("loading")(((message: String) => Sonner.loading(message)): js.Function1[String, String])
  api.updateDynamic("dismiss")(((id: String) => Sonner.dismiss(id)): js.Function1[String, Unit])

object ScSonner:
  def register(): Unit =
    ScElements.define("sc-sonner", js.constructorOf[ScSonner])
