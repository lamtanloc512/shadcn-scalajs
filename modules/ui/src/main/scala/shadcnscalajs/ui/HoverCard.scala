package shadcnscalajs.ui

import com.raquo.laminar.api.L.*

/** shadcn/ui HoverCard — a portaled, anchored panel opened on hover with upstream's open delay.
  *
  * Replaces a CSS `group-hover` stand-in, which appeared instantly, could not escape an `overflow` ancestor, and had no
  * way to stay open while the pointer travelled from trigger to panel.
  */
object HoverCard:

  val contentShellClass: String =
    "cn-hover-card-content z-50 rounded-md border bg-popover text-popover-foreground shadow-md outline-hidden data-open:animate-in data-open:fade-in-0 data-open:zoom-in-95 data-closed:animate-out data-closed:fade-out-0 data-closed:zoom-out-95 data-[side=bottom]:slide-in-from-top-2 data-[side=top]:slide-in-from-bottom-2 data-[side=left]:slide-in-from-right-2 data-[side=right]:slide-in-from-left-2"

  val defaultBoxClass: String = "w-64 p-4"

  /** Upstream's hover card waits longer than a tooltip before opening, since the panel is larger and more disruptive.
    */
  private val openDelay = 700.0
  private val closeDelay = 300.0

  final case class Trigger private[HoverCard] (mods: Seq[Modifier[HtmlElement]])

  final case class Content private[HoverCard] (
      placement: Floating.Placement,
      boxClass: String,
      mods: Seq[Modifier[HtmlElement]]
  )

  def trigger(mods: Modifier[HtmlElement]*): Trigger = Trigger(mods)

  def content(mods: Modifier[HtmlElement]*): Content =
    Content(Floating.Placement(), defaultBoxClass, mods)

  def content(placement: Floating.Placement, mods: Modifier[HtmlElement]*): Content =
    Content(placement, defaultBoxClass, mods)

  def content(placement: Floating.Placement, boxClass: String, mods: Modifier[HtmlElement]*): Content =
    Content(placement, boxClass, mods)

  def apply(triggerSpec: Trigger, contentSpec: Content): HtmlElement =
    val a = Floating.anchor()
    div(
      dataAttr("slot") := "hover-card",
      cls := "cn-hover-card relative inline-block",
      span(
        dataAttr("slot") := "hover-card-trigger",
        cls := "cursor-pointer underline-offset-4",
        Floating.triggerBase(a),
        Floating.hoverToOpen(a, openDelay, closeDelay),
        triggerSpec.mods
      ),
      Floating.content(a, contentSpec.placement, s"$contentShellClass ${contentSpec.boxClass}")(
        dataAttr("slot") := "hover-card-content",
        Floating.hoverKeepOpen(a, closeDelay),
        contentSpec.mods
      )
    )
