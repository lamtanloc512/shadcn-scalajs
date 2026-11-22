package shadcnscalajs.webcomponents

import shadcnscalajs.ui.Marker

import scala.scalajs.js
import LightVariantPrimitive.one

/** `<sc-marker variant="default|separator|border">` with `sc-marker-icon` / `sc-marker-content` parts. */
class ScMarker extends LightVariantPrimitive("marker"):
  private def variant(attr: String => Option[String]) =
    one(
      attr("variant"),
      "default" -> Marker.Variant.Default,
      "separator" -> Marker.Variant.Separator,
      "border" -> Marker.Variant.Border
    )(Marker.Variant.Default)
  protected def classes(attr: String => Option[String]): String = Marker.variantClass(variant(attr))
  override protected def stamps(attr: String => Option[String]): Seq[(String, String)] =
    Seq("data-variant" -> variant(attr).toString.toLowerCase)

class ScMarkerIcon extends LightPrimitive("marker-icon", Marker.iconClass):
  this.setAttribute("aria-hidden", "true")

class ScMarkerContent extends LightPrimitive("marker-content", Marker.contentClass)

object ScMarker:
  def register(): Unit =
    ScElements.define("sc-marker", js.constructorOf[ScMarker], "variant")
    ScElements.define("sc-marker-icon", js.constructorOf[ScMarkerIcon])
    ScElements.define("sc-marker-content", js.constructorOf[ScMarkerContent])
