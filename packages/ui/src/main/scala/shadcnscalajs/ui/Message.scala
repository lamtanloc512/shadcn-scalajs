package shadcnscalajs.ui

import com.raquo.laminar.api.L.*

/** shadcn/ui Message — the row that lays out one chat turn: an avatar, a header, the content stack, and a footer.
  *
  * Pair it with [[Bubble]] for the message body. `Align.End` mirrors the row (the sender's own turn), and every direct
  * `data-slot` child of the content stack follows that alignment.
  */
object Message:

  // Class lists shared with the light-DOM `sc-*` web component hosts.
  val rootClass: String =
    "cn-message group/message relative flex w-full min-w-0 gap-2 text-sm data-[align=end]:flex-row-reverse"

  val groupClass: String =
    "cn-message-group flex min-w-0 flex-col gap-2"

  val avatarClass: String =
    "cn-message-avatar flex w-fit min-w-8 shrink-0 items-center justify-center self-end overflow-hidden rounded-full bg-muted group-has-data-[slot=message-footer]/message:-translate-y-8"

  val contentClass: String =
    "cn-message-content flex w-full min-w-0 flex-col gap-2.5 wrap-break-word group-data-[align=end]/message:*:data-slot:self-end"

  val headerClass: String =
    "cn-message-header flex max-w-full min-w-0 items-center px-3 text-xs font-medium text-muted-foreground group-has-data-[variant=ghost]/message:px-0"

  val footerClass: String =
    "cn-message-footer flex max-w-full min-w-0 items-center px-3 text-xs font-medium text-muted-foreground group-has-data-[variant=ghost]/message:px-0 group-data-[align=end]/message:justify-end"

  enum Align derives CanEqual:
    case Start, End

  private def alignName(align: Align): String = align.toString.toLowerCase

  def apply(mods: Modifier[HtmlElement]*): HtmlElement = withAlign(Align.Start)(mods*)

  def end(mods: Modifier[HtmlElement]*): HtmlElement = withAlign(Align.End)(mods*)

  def withAlign(align: Align)(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "message",
      dataAttr("align") := alignName(align),
      cls := rootClass,
      mods
    )

  def group(mods: Modifier[HtmlElement]*): HtmlElement =
    div(dataAttr("slot") := "message-group", cls := groupClass, mods)

  def avatar(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "message-avatar",
      cls := avatarClass,
      mods
    )

  def content(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "message-content",
      cls := contentClass,
      mods
    )

  def header(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "message-header",
      cls := headerClass,
      mods
    )

  def footer(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "message-footer",
      cls := footerClass,
      mods
    )
