package shadcnscalajs.webcomponents

import shadcnscalajs.ui.Message

import scala.scalajs.js

/** `<sc-message>` and its parts — light-DOM hosts, so upstream's `group-data-[align=end]/message:` selectors reach the
  * page's own children. `align="end"` mirrors the row for the sender's own turn.
  */
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

object ScMessage:
  def register(): Unit =
    ScElements.define("sc-message", js.constructorOf[ScMessage], "align")
    ScElements.define("sc-message-group", js.constructorOf[ScMessageGroup])
    ScElements.define("sc-message-avatar", js.constructorOf[ScMessageAvatar])
    ScElements.define("sc-message-content", js.constructorOf[ScMessageContent])
    ScElements.define("sc-message-header", js.constructorOf[ScMessageHeader])
    ScElements.define("sc-message-footer", js.constructorOf[ScMessageFooter])
