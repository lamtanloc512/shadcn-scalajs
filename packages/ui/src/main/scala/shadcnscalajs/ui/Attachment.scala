package shadcnscalajs.ui

import com.raquo.laminar.api.L.*

/** shadcn/ui Attachment — a file chip for composers and messages: media, title, description, and actions.
  *
  * `state` drives the look: `Idle` dashes the border, `Uploading`/`Processing` shimmer the title and dim image media,
  * `Error` tints the chip destructive. Pass a `Signal[State]` to [[reactive]] for a live upload.
  */
object Attachment:

  // Class lists shared with the light-DOM `sc-*` web component hosts.
  val contentClass: String =
    "cn-attachment-content max-w-full min-w-0 flex-1 leading-tight group-data-[orientation=vertical]/attachment:px-1"

  val titleClass: String =
    "cn-attachment-title block max-w-full min-w-0 truncate font-medium group-data-[state=processing]/attachment:shimmer group-data-[state=uploading]/attachment:shimmer"

  val descriptionClass: String =
    "cn-attachment-description mt-0.5 block max-w-full min-w-0 truncate text-xs text-muted-foreground group-data-[state=error]/attachment:text-destructive/80"

  val actionsClass: String =
    "cn-attachment-actions relative z-20 flex shrink-0 items-center group-data-[orientation=vertical]/attachment:absolute group-data-[orientation=vertical]/attachment:top-3 group-data-[orientation=vertical]/attachment:right-3 group-data-[orientation=vertical]/attachment:gap-1"

  val triggerClass: String =
    "cn-attachment-trigger absolute inset-0 z-10 outline-none"

  val groupClass: String =
    "cn-attachment-group flex min-w-0 scroll-fade-x snap-x snap-mandatory scroll-px-1 scrollbar-none gap-3 overflow-x-auto overscroll-x-contain py-1 *:data-[slot=attachment]:flex-none *:data-[slot=attachment]:snap-start"

  enum State derives CanEqual:
    case Idle, Uploading, Processing, Error, Done

  enum Size derives CanEqual:
    case Default, Sm, Xs

  enum Orientation derives CanEqual:
    case Horizontal, Vertical

  enum MediaVariant derives CanEqual:
    case Icon, Image

  private def name(value: Any): String = value.toString.toLowerCase

  private val base: String =
    "cn-attachment group/attachment relative flex w-fit max-w-full min-w-0 shrink-0 flex-wrap rounded-xl border bg-card text-card-foreground transition-colors focus-within:ring-1 focus-within:ring-ring/50 has-[>a,>button]:hover:bg-muted/50 data-[state=error]:border-destructive/30 data-[state=idle]:border-dashed"

  private val sizeClasses: Map[Size, String] = Map(
    Size.Default ->
      "gap-2 text-sm has-data-[slot=attachment-content]:px-2.5 has-data-[slot=attachment-content]:py-2 has-data-[slot=attachment-media]:p-2",
    Size.Sm ->
      "gap-2.5 text-xs has-data-[slot=attachment-content]:px-2 has-data-[slot=attachment-content]:py-1.5 has-data-[slot=attachment-media]:p-1.5",
    Size.Xs ->
      "gap-1.5 rounded-lg text-xs has-data-[slot=attachment-content]:px-1.5 has-data-[slot=attachment-content]:py-1 has-data-[slot=attachment-media]:p-1"
  )

  private val orientationClasses: Map[Orientation, String] = Map(
    Orientation.Horizontal -> "min-w-40 items-center",
    Orientation.Vertical -> "w-24 flex-col has-data-[slot=attachment-content]:w-30"
  )

  def rootClass(size: Size, orientation: Orientation): String =
    s"$base ${sizeClasses(size)} ${orientationClasses(orientation)}"

  def mediaClass(variant: MediaVariant): String = s"$mediaBase ${mediaVariantClasses(variant)}"

  private def frame(size: Size, orientation: Orientation): Modifier[HtmlElement] =
    Seq[Modifier[HtmlElement]](
      dataAttr("slot") := "attachment",
      dataAttr("size") := name(size),
      dataAttr("orientation") := name(orientation),
      cls := rootClass(size, orientation)
    )

  def apply(mods: Modifier[HtmlElement]*): HtmlElement = of()(mods*)

  def of(
      state: State = State.Done,
      size: Size = Size.Default,
      orientation: Orientation = Orientation.Horizontal
  )(mods: Modifier[HtmlElement]*): HtmlElement =
    div(frame(size, orientation), dataAttr("state") := name(state), mods)

  def reactive(
      state: Signal[State],
      size: Size = Size.Default,
      orientation: Orientation = Orientation.Horizontal
  )(mods: Modifier[HtmlElement]*): HtmlElement =
    div(frame(size, orientation), dataAttr("state") <-- state.map(name), mods)

  private val mediaBase: String =
    "cn-attachment-media relative flex aspect-square w-10 shrink-0 items-center justify-center overflow-hidden rounded-lg bg-muted text-foreground group-data-[orientation=vertical]/attachment:w-full group-data-[size=sm]/attachment:w-8 group-data-[size=xs]/attachment:w-7 group-data-[size=xs]/attachment:rounded-md group-data-[state=error]/attachment:bg-destructive/10 group-data-[state=error]/attachment:text-destructive group-data-[orientation=vertical]/attachment:*:data-[slot=spinner]:size-6! [&_svg]:pointer-events-none [&_svg:not([class*='size-'])]:size-4 group-data-[orientation=vertical]/attachment:[&_svg:not([class*='size-'])]:size-6 group-data-[size=xs]/attachment:[&_svg:not([class*='size-'])]:size-3.5"

  private val mediaVariantClasses: Map[MediaVariant, String] = Map(
    MediaVariant.Icon -> "",
    MediaVariant.Image ->
      "opacity-60 group-data-[state=done]/attachment:opacity-100 group-data-[state=idle]/attachment:opacity-100 *:[img]:aspect-square *:[img]:w-full *:[img]:object-cover"
  )

  def media(variant: MediaVariant = MediaVariant.Icon)(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "attachment-media",
      dataAttr("variant") := name(variant),
      cls := mediaClass(variant),
      mods
    )

  def content(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "attachment-content",
      cls := contentClass,
      mods
    )

  def title(mods: Modifier[HtmlElement]*): HtmlElement =
    span(
      dataAttr("slot") := "attachment-title",
      cls := titleClass,
      mods
    )

  def description(mods: Modifier[HtmlElement]*): HtmlElement =
    span(
      dataAttr("slot") := "attachment-description",
      cls := descriptionClass,
      mods
    )

  def actions(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "attachment-actions",
      cls := actionsClass,
      mods
    )

  /** A ghost icon button sized for the chip — upstream's `AttachmentAction`. */
  def action(mods: Modifier[HtmlElement]*): HtmlElement =
    Button(
      Button.ButtonApi.variant(Button.Variant.Ghost),
      Button.ButtonApi.size(Button.Size.IconXs),
      dataAttr("slot") := "attachment-action",
      mods
    )

  /** A full-bleed invisible button that makes the whole chip clickable (preview, open). Actions stay above it. */
  def trigger(mods: Modifier[HtmlElement]*): HtmlElement =
    button(
      typ := "button",
      dataAttr("slot") := "attachment-trigger",
      cls := triggerClass,
      mods
    )

  def group(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "attachment-group",
      cls := groupClass,
      mods
    )
