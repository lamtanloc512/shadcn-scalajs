package shadcnscalajs.webcomponents

import shadcnscalajs.ui.Attachment

import scala.scalajs.js
import LightVariantPrimitive.one

/** `<sc-attachment state="…" size="…" orientation="…">` — light-DOM so the `group-data-[state=…]/attachment:` styles on
  * its media, title, and description parts apply. Use `<sc-button variant="ghost" size="icon-xs">` inside
  * `sc-attachment-actions` for actions.
  */
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

object ScAttachment:
  def register(): Unit =
    ScElements.define("sc-attachment", js.constructorOf[ScAttachment], "state", "size", "orientation")
    ScElements.define("sc-attachment-media", js.constructorOf[ScAttachmentMedia], "variant")
    ScElements.define("sc-attachment-content", js.constructorOf[ScAttachmentContent])
    ScElements.define("sc-attachment-title", js.constructorOf[ScAttachmentTitle])
    ScElements.define("sc-attachment-description", js.constructorOf[ScAttachmentDescription])
    ScElements.define("sc-attachment-actions", js.constructorOf[ScAttachmentActions])
    ScElements.define("sc-attachment-group", js.constructorOf[ScAttachmentGroup])
