package shadcnscalajs.ui

import com.raquo.laminar.api.L.*

/** shadcn/ui Popover — an anchored, portaled panel built on [[Floating]].
  *
  * This replaces an earlier `<details>`/`<summary>` stand-in, which could not be positioned, escaped no `overflow`
  * ancestor, and had none of upstream's side/align or `data-state` animation contract.
  *
  * Trigger and content are descriptors rather than elements, because the pair has to share one open state: `Popover`
  * creates the [[Floating.Anchor]] that wires them together. Use [[withAnchor]] when the caller needs the anchor too,
  * for instance to render a close button inside the panel.
  */
object Popover:

  /** Surface, animation, and the side-aware slide-in utilities. Width and padding live in the box class so callers can
    * replace them: packs set padding on `.cn-popover-content` from an unlayered rule, so an appended `p-0` is not
    * reliable — a caller that needs a flush panel (a calendar, a command list) should pass `p-0!`.
    */
  val contentShellClass: String =
    "popover-content cn-popover-content cn-popover-content-logical z-50 rounded-md border bg-popover text-popover-foreground shadow-md outline-hidden data-open:animate-in data-open:fade-in-0 data-open:zoom-in-95 data-closed:animate-out data-closed:fade-out-0 data-closed:zoom-out-95 data-[side=bottom]:slide-in-from-top-2 data-[side=top]:slide-in-from-bottom-2 data-[side=left]:slide-in-from-right-2 data-[side=right]:slide-in-from-left-2"

  val defaultBoxClass: String = "w-72 p-4"

  final case class Trigger private[Popover] (mods: Seq[Modifier[HtmlElement]])

  final case class Content private[Popover] (
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
    withAnchor(Floating.anchor())(triggerSpec, contentSpec)

  def withAnchor(a: Floating.Anchor)(triggerSpec: Trigger, contentSpec: Content): HtmlElement =
    div(
      dataAttr("slot") := "popover",
      cls := "popover relative inline-block",
      button(
        typ := "button",
        dataAttr("slot") := "popover-trigger",
        aria.hasPopup := true,
        Floating.triggerBase(a),
        Floating.clickToToggle(a),
        triggerSpec.mods
      ),
      Floating.content(a, contentSpec.placement, s"$contentShellClass ${contentSpec.boxClass}")(
        dataAttr("slot") := "popover-content",
        role := "dialog",
        contentSpec.mods
      )
    )

  /** A standalone styled panel for call sites that own their open state and placement. */
  def panel(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "popover-content",
      cls := s"$contentShellClass $defaultBoxClass",
      mods
    )

  def close(a: Floating.Anchor, mods: Modifier[HtmlElement]*): HtmlElement =
    button(
      typ := "button",
      dataAttr("slot") := "popover-close",
      cls := "cn-popover-close absolute top-3 right-3 inline-flex size-6 items-center justify-center rounded-md text-muted-foreground hover:bg-accent hover:text-accent-foreground",
      onClick --> { _ => a.close() },
      Icons.x(),
      span(cls := "sr-only", "Close"),
      mods
    )

  def header(mods: Modifier[HtmlElement]*): HtmlElement =
    div(dataAttr("slot") := "popover-header", cls := "cn-popover-header flex flex-col gap-1", mods)

  def title(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "popover-title",
      cls := "cn-font-heading cn-popover-title leading-none font-medium",
      mods
    )

  def description(mods: Modifier[HtmlElement]*): HtmlElement =
    p(
      dataAttr("slot") := "popover-description",
      cls := "cn-popover-description text-sm text-muted-foreground",
      mods
    )
