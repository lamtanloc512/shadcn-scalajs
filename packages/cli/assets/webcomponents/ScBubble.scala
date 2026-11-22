package shadcnscalajs.webcomponents

import shadcnscalajs.ui.Bubble

import scala.scalajs.js
import LightVariantPrimitive.one

/** `<sc-bubble variant="…" align="…">` — light-DOM, because each variant paints its `sc-bubble-content` child through
  * `*:data-[slot=bubble-content]:…` selectors a shadow root would cut.
  */
class ScBubble extends LightVariantPrimitive("bubble"):
  private def variant(attr: String => Option[String]): Bubble.Variant =
    one(
      attr("variant"),
      "default" -> Bubble.Variant.Default,
      "secondary" -> Bubble.Variant.Secondary,
      "muted" -> Bubble.Variant.Muted,
      "tinted" -> Bubble.Variant.Tinted,
      "outline" -> Bubble.Variant.Outline,
      "ghost" -> Bubble.Variant.Ghost,
      "destructive" -> Bubble.Variant.Destructive
    )(Bubble.Variant.Default)
  protected def classes(attr: String => Option[String]): String = Bubble.rootClass(variant(attr))
  override protected def stamps(attr: String => Option[String]): Seq[(String, String)] =
    Seq(
      "data-variant" -> variant(attr).toString.toLowerCase,
      "data-align" -> (if attr("align").contains("end") then "end" else "start")
    )

class ScBubbleGroup extends LightPrimitive("bubble-group", Bubble.groupClass)
class ScBubbleContent extends LightPrimitive("bubble-content", s"${Bubble.contentClass} block")

class ScBubbleReactions extends LightVariantPrimitive("bubble-reactions"):
  private def side(attr: String => Option[String]) =
    one(attr("side"), "top" -> Bubble.Side.Top, "bottom" -> Bubble.Side.Bottom)(Bubble.Side.Bottom)
  private def align(attr: String => Option[String]) =
    one(attr("align"), "start" -> Bubble.Align.Start, "end" -> Bubble.Align.End)(Bubble.Align.End)
  protected def classes(attr: String => Option[String]): String = Bubble.reactionsClass(side(attr), align(attr))
  override protected def stamps(attr: String => Option[String]): Seq[(String, String)] =
    Seq("data-side" -> side(attr).toString.toLowerCase, "data-align" -> align(attr).toString.toLowerCase)

object ScBubble:
  def register(): Unit =
    ScElements.define("sc-bubble", js.constructorOf[ScBubble], "variant", "align")
    ScElements.define("sc-bubble-group", js.constructorOf[ScBubbleGroup])
    ScElements.define("sc-bubble-content", js.constructorOf[ScBubbleContent])
    ScElements.define("sc-bubble-reactions", js.constructorOf[ScBubbleReactions], "side", "align")
