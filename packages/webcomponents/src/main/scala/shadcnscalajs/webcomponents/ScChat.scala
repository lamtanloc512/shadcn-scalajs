package shadcnscalajs.webcomponents

import shadcnscalajs.ui.{Attachment, Bubble, Marker, Message}

import scala.scalajs.js

/** Light-DOM hosts for the conversation layout parts: Message, Bubble, Marker, Attachment, Direction.
  *
  * These are pure layout, and upstream styles them through selectors that reach across parent and child
  * (`group-data-[align=end]/message:*:data-slot:self-end`, `*:data-[slot=bubble-content]:bg-primary`). A shadow root
  * would cut those selectors in half, so every part here is a light-DOM host carrying the same `data-slot` and classes
  * the Laminar helpers emit.
  *
  * {{{
  * <sc-message align="end">
  *   <sc-message-content>
  *     <sc-bubble variant="default"><sc-bubble-content>Hi!</sc-bubble-content></sc-bubble>
  *   </sc-message-content>
  * </sc-message>
  * }}}
  */
private def one[A](raw: Option[String], options: (String, A)*)(default: A): A =
  raw.flatMap(value => options.collectFirst { case (`value`, a) => a }).getOrElse(default)

// ── Message ──

class ScMessage extends LightVariantPrimitive("message"):
  private def align(attr: String => Option[String]): String = if attr("align").contains("end") then "end" else "start"
  protected def classes(attr: String => Option[String]): String = Message.rootClass
  override protected def stamps(attr: String => Option[String]): Seq[(String, String)] =
    Seq("data-align" -> align(attr))

class ScMessageGroup extends LightPrimitive("message-group", Message.groupClass)
class ScMessageAvatar extends LightPrimitive("message-avatar", Message.avatarClass)
class ScMessageContent extends LightPrimitive("message-content", Message.contentClass)
class ScMessageHeader extends LightPrimitive("message-header", Message.headerClass)
class ScMessageFooter extends LightPrimitive("message-footer", Message.footerClass)

// ── Bubble ──

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

// ── Marker ──

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

// ── Attachment ──

class ScAttachment extends LightVariantPrimitive("attachment"):
  private def state(attr: String => Option[String]): String =
    attr("state").filter(Set("idle", "uploading", "processing", "error", "done")).getOrElse("done")
  private def size(attr: String => Option[String]) =
    one(attr("size"), "default" -> Attachment.Size.Default, "sm" -> Attachment.Size.Sm, "xs" -> Attachment.Size.Xs)(
      Attachment.Size.Default
    )
  private def orientation(attr: String => Option[String]) =
    one(
      attr("orientation"),
      "horizontal" -> Attachment.Orientation.Horizontal,
      "vertical" -> Attachment.Orientation.Vertical
    )(Attachment.Orientation.Horizontal)
  protected def classes(attr: String => Option[String]): String = Attachment.rootClass(size(attr), orientation(attr))
  override protected def stamps(attr: String => Option[String]): Seq[(String, String)] =
    Seq(
      "data-state" -> state(attr),
      "data-size" -> size(attr).toString.toLowerCase,
      "data-orientation" -> orientation(attr).toString.toLowerCase
    )

class ScAttachmentMedia extends LightVariantPrimitive("attachment-media"):
  private def variant(attr: String => Option[String]) =
    one(attr("variant"), "icon" -> Attachment.MediaVariant.Icon, "image" -> Attachment.MediaVariant.Image)(
      Attachment.MediaVariant.Icon
    )
  protected def classes(attr: String => Option[String]): String = Attachment.mediaClass(variant(attr))
  override protected def stamps(attr: String => Option[String]): Seq[(String, String)] =
    Seq("data-variant" -> variant(attr).toString.toLowerCase)

class ScAttachmentContent extends LightPrimitive("attachment-content", Attachment.contentClass)
class ScAttachmentTitle extends LightPrimitive("attachment-title", Attachment.titleClass)
class ScAttachmentDescription extends LightPrimitive("attachment-description", Attachment.descriptionClass)
class ScAttachmentActions extends LightPrimitive("attachment-actions", Attachment.actionsClass)
class ScAttachmentGroup extends LightPrimitive("attachment-group", Attachment.groupClass)

// ── Direction ──

/** `<sc-direction dir="rtl">` — the native `dir` attribute does the work; `direction="rtl"` is accepted as upstream's
  * prop name and copied onto `dir`.
  */
class ScDirection extends LightVariantPrimitive("direction-provider"):
  protected def classes(attr: String => Option[String]): String = "block"
  override protected def stamps(attr: String => Option[String]): Seq[(String, String)] =
    attr("direction").filter(Set("ltr", "rtl")).map(d => "dir" -> d).toSeq

object ScChat:
  def register(): Unit =
    ScElements.define("sc-message", js.constructorOf[ScMessage], "align")
    ScElements.define("sc-message-group", js.constructorOf[ScMessageGroup])
    ScElements.define("sc-message-avatar", js.constructorOf[ScMessageAvatar])
    ScElements.define("sc-message-content", js.constructorOf[ScMessageContent])
    ScElements.define("sc-message-header", js.constructorOf[ScMessageHeader])
    ScElements.define("sc-message-footer", js.constructorOf[ScMessageFooter])

    ScElements.define("sc-bubble", js.constructorOf[ScBubble], "variant", "align")
    ScElements.define("sc-bubble-group", js.constructorOf[ScBubbleGroup])
    ScElements.define("sc-bubble-content", js.constructorOf[ScBubbleContent])
    ScElements.define("sc-bubble-reactions", js.constructorOf[ScBubbleReactions], "side", "align")

    ScElements.define("sc-marker", js.constructorOf[ScMarker], "variant")
    ScElements.define("sc-marker-icon", js.constructorOf[ScMarkerIcon])
    ScElements.define("sc-marker-content", js.constructorOf[ScMarkerContent])

    ScElements.define("sc-attachment", js.constructorOf[ScAttachment], "state", "size", "orientation")
    ScElements.define("sc-attachment-media", js.constructorOf[ScAttachmentMedia], "variant")
    ScElements.define("sc-attachment-content", js.constructorOf[ScAttachmentContent])
    ScElements.define("sc-attachment-title", js.constructorOf[ScAttachmentTitle])
    ScElements.define("sc-attachment-description", js.constructorOf[ScAttachmentDescription])
    ScElements.define("sc-attachment-actions", js.constructorOf[ScAttachmentActions])
    ScElements.define("sc-attachment-group", js.constructorOf[ScAttachmentGroup])

    ScElements.define("sc-direction", js.constructorOf[ScDirection], "direction")
