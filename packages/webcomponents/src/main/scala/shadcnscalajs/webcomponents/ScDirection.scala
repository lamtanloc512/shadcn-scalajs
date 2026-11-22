package shadcnscalajs.webcomponents

import scala.scalajs.js

/** `<sc-direction dir="rtl">` — the native `dir` attribute does the work; `direction="rtl"` is accepted as upstream's
  * prop name and copied onto `dir`.
  */
class ScDirection extends LightVariantPrimitive("direction-provider"):
  protected def classes(attr: String => Option[String]): String = "block"
  override protected def stamps(attr: String => Option[String]): Seq[(String, String)] =
    attr("direction").filter(Set("ltr", "rtl")).map(d => "dir" -> d).toSeq

object ScDirection:
  def register(): Unit =
    ScElements.define("sc-direction", js.constructorOf[ScDirection], "direction")
