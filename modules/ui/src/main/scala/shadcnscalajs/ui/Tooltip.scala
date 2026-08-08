package shadcnscalajs.ui

import com.raquo.laminar.api.L.*
import com.raquo.laminar.codecs.StringAsIsCodec

/** shadcn/ui Tooltip — a portaled, anchored panel with an arrow, opened on hover or focus.
  *
  * Replaces a stand-in that set the native `title` attribute, which the browser renders in its own OS style: no
  * positioning, no delay control, no theming, and nothing for the packs to target.
  */
object Tooltip:

  private val titleAttr = htmlAttr("title", StringAsIsCodec)

  val contentClass: String =
    "cn-tooltip-content group/tooltip-content z-50 w-fit max-w-xs rounded-md bg-foreground px-3 py-1.5 text-xs text-background outline-hidden data-open:animate-in data-open:fade-in-0 data-open:zoom-in-95 data-closed:animate-out data-closed:fade-out-0 data-closed:zoom-out-95 data-[side=bottom]:slide-in-from-top-2 data-[side=top]:slide-in-from-bottom-2 data-[side=left]:slide-in-from-right-2 data-[side=right]:slide-in-from-left-2"

  /** Upstream's default delays: open promptly, close with a grace period so the pointer can reach the panel. */
  private val openDelay = 200.0
  private val closeDelay = 100.0

  final case class Trigger private[Tooltip] (mods: Seq[Modifier[HtmlElement]])
  final case class Content private[Tooltip] (placement: Floating.Placement, mods: Seq[Modifier[HtmlElement]])

  def trigger(mods: Modifier[HtmlElement]*): Trigger = Trigger(mods)

  def content(mods: Modifier[HtmlElement]*): Content =
    Content(Floating.Placement(side = Floating.Side.Top), mods)

  def content(placement: Floating.Placement, mods: Modifier[HtmlElement]*): Content =
    Content(placement, mods)

  /** Text-first form, kept because it is the shape most call sites and `ScTooltip` use: the children are the trigger.
    */
  def apply(text: String, mods: Modifier[HtmlElement]*): HtmlElement =
    apply(trigger(mods), content(text))

  def apply(triggerSpec: Trigger, contentSpec: Content): HtmlElement =
    val a = Floating.anchor()
    span(
      dataAttr("slot") := "tooltip",
      cls := "cn-tooltip relative inline-flex",
      span(
        dataAttr("slot") := "tooltip-trigger",
        cls := "inline-flex",
        Floating.triggerBase(a),
        Floating.hoverToOpen(a, openDelay, closeDelay),
        triggerSpec.mods
      ),
      Floating.content(a, contentSpec.placement, contentClass)(
        dataAttr("slot") := "tooltip-content",
        role := "tooltip",
        Floating.hoverKeepOpen(a, closeDelay),
        contentSpec.mods,
        // `data-side` is set on the panel, so the arrow reads it through the panel's group rather than its own
        // attribute — a bare `data-[side=top]:` here would never match.
        span(
          dataAttr("slot") := "tooltip-arrow",
          cls := "cn-tooltip-arrow absolute size-2.5 rotate-45 bg-foreground group-data-[side=top]/tooltip-content:top-full group-data-[side=top]/tooltip-content:left-1/2 group-data-[side=top]/tooltip-content:-translate-x-1/2 group-data-[side=top]/tooltip-content:-translate-y-1/2 group-data-[side=bottom]/tooltip-content:bottom-full group-data-[side=bottom]/tooltip-content:left-1/2 group-data-[side=bottom]/tooltip-content:-translate-x-1/2 group-data-[side=bottom]/tooltip-content:translate-y-1/2 group-data-[side=left]/tooltip-content:left-full group-data-[side=left]/tooltip-content:top-1/2 group-data-[side=left]/tooltip-content:-translate-x-1/2 group-data-[side=left]/tooltip-content:-translate-y-1/2 group-data-[side=right]/tooltip-content:right-full group-data-[side=right]/tooltip-content:top-1/2 group-data-[side=right]/tooltip-content:translate-x-1/2 group-data-[side=right]/tooltip-content:-translate-y-1/2"
        )
      )
    )

  /** The native `title` fallback, for cases that need the OS tooltip rather than a rendered panel. */
  def native(text: String, mods: Modifier[HtmlElement]*): HtmlElement =
    span(titleAttr := text, dataAttr("tooltip") := text, cls := "cursor-help", mods)
