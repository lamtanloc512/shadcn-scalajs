package shadcnscalajs.ui

import com.raquo.laminar.api.L.*

/** shadcn/ui Bubble — the speech bubble inside a [[Message]].
  *
  * The variant paints the `bubble-content` child rather than the bubble itself, so reactions and other absolutely
  * positioned parts can sit on the bubble's edge without inheriting its fill. A `bubble-content` that is a `button` or
  * `a` gets a hover tint — use [[contentButton]] / [[contentLink]] for the upstream `asChild` cases.
  */
object Bubble:

  enum Variant derives CanEqual:
    case Default, Secondary, Muted, Tinted, Outline, Ghost, Destructive

  enum Align derives CanEqual:
    case Start, End

  enum Side derives CanEqual:
    case Top, Bottom

  private val base: String =
    "cn-bubble group/bubble relative flex w-fit max-w-[80%] min-w-0 flex-col gap-1 group-data-[align=end]/message:self-end data-[align=end]:self-end data-[variant=ghost]:max-w-full"

  private val variantClasses: Map[Variant, String] = Map(
    Variant.Default ->
      "*:data-[slot=bubble-content]:bg-primary *:data-[slot=bubble-content]:text-primary-foreground [&>[data-slot=bubble-content]:is(button,a):hover]:bg-primary/80",
    Variant.Secondary ->
      "*:data-[slot=bubble-content]:bg-secondary *:data-[slot=bubble-content]:text-secondary-foreground [&>[data-slot=bubble-content]:is(button,a):hover]:bg-[color-mix(in_oklch,var(--secondary),var(--foreground)_5%)]",
    Variant.Muted ->
      "*:data-[slot=bubble-content]:bg-muted [&>[data-slot=bubble-content]:is(button,a):hover]:bg-[color-mix(in_oklch,var(--muted),var(--foreground)_5%)]",
    Variant.Tinted ->
      "*:data-[slot=bubble-content]:bg-[oklch(from_var(--primary)_0.93_calc(c*0.4)_h)] *:data-[slot=bubble-content]:text-foreground dark:*:data-[slot=bubble-content]:bg-[oklch(from_var(--primary)_0.3_calc(c*0.4)_h)] [&>[data-slot=bubble-content]:is(button,a):hover]:bg-[oklch(from_var(--primary)_0.88_calc(c*0.5)_h)] dark:[&>[data-slot=bubble-content]:is(button,a):hover]:bg-[oklch(from_var(--primary)_0.35_calc(c*0.5)_h)]",
    Variant.Outline ->
      "*:data-[slot=bubble-content]:border-border *:data-[slot=bubble-content]:bg-background [&>[data-slot=bubble-content]:is(button,a):hover]:bg-muted [&>[data-slot=bubble-content]:is(button,a):hover]:text-foreground dark:[&>[data-slot=bubble-content]:is(button,a):hover]:bg-input/30",
    Variant.Ghost ->
      "border-none *:data-[slot=bubble-content]:rounded-none *:data-[slot=bubble-content]:bg-transparent *:data-[slot=bubble-content]:p-0 [&>[data-slot=bubble-content]:is(button,a):hover]:bg-muted [&>[data-slot=bubble-content]:is(button,a):hover]:text-foreground dark:[&>[data-slot=bubble-content]:is(button,a):hover]:bg-muted/50",
    Variant.Destructive ->
      "*:data-[slot=bubble-content]:bg-destructive/10 *:data-[slot=bubble-content]:text-destructive dark:*:data-[slot=bubble-content]:bg-destructive/20 [&>[data-slot=bubble-content]:is(button,a):hover]:bg-destructive/20 dark:[&>[data-slot=bubble-content]:is(button,a):hover]:bg-destructive/30"
  )

  private val contentClasses: String =
    "cn-bubble-content w-fit max-w-full min-w-0 overflow-hidden rounded-xl border border-transparent px-3 py-2 text-sm leading-relaxed wrap-break-word group-data-[align=end]/bubble:self-end [button]:text-left [button,a]:transition-colors [button,a]:outline-none [button,a]:focus-visible:border-ring [button,a]:focus-visible:ring-3 [button,a]:focus-visible:ring-ring/50"

  private def name(value: Any): String = value.toString.toLowerCase

  def apply(mods: Modifier[HtmlElement]*): HtmlElement = of(Variant.Default)(mods*)

  def of(variant: Variant, align: Align = Align.Start)(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "bubble",
      dataAttr("variant") := name(variant),
      dataAttr("align") := name(align),
      cls := s"$base cn-bubble-variant-${name(variant)} ${variantClasses(variant)}",
      mods
    )

  def group(mods: Modifier[HtmlElement]*): HtmlElement =
    div(dataAttr("slot") := "bubble-group", cls := "cn-bubble-group flex min-w-0 flex-col gap-2", mods)

  def content(mods: Modifier[HtmlElement]*): HtmlElement =
    div(dataAttr("slot") := "bubble-content", cls := contentClasses, mods)

  /** Upstream's `<BubbleContent asChild><button/></BubbleContent>`: a clickable bubble body. */
  def contentButton(mods: Modifier[HtmlElement]*): HtmlElement =
    button(typ := "button", dataAttr("slot") := "bubble-content", cls := contentClasses, mods)

  /** Upstream's `<BubbleContent asChild><a/></BubbleContent>`: a bubble body that navigates. */
  def contentLink(hrefValue: String, mods: Modifier[HtmlElement]*): HtmlElement =
    a(href := hrefValue, dataAttr("slot") := "bubble-content", cls := contentClasses, mods)

  private val reactionsSide: Map[Side, String] = Map(
    Side.Top -> "top-0 -translate-y-3/4",
    Side.Bottom -> "bottom-0 translate-y-3/4"
  )

  private val reactionsAlign: Map[Align, String] = Map(
    Align.Start -> "left-3",
    Align.End -> "right-3"
  )

  /** A reaction strip pinned to the bubble's edge. Defaults match upstream: bottom side, end edge. */
  def reactions(side: Side = Side.Bottom, align: Align = Align.End)(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "bubble-reactions",
      dataAttr("align") := name(align),
      dataAttr("side") := name(side),
      cls := s"cn-bubble-reactions absolute z-10 flex w-fit shrink-0 items-center justify-center gap-1 rounded-full bg-muted px-1.5 py-0.5 text-sm ring-3 ring-card has-[button]:p-0 ${reactionsSide(side)} ${reactionsAlign(align)}",
      mods
    )
