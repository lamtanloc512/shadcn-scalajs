package shadcnscalajs.ui

import com.raquo.laminar.api.L.*

/** shadcn/ui Empty — styled with Tailwind CSS utilities matching the canonical new-york-v4 empty components.
  *
  * The utility classes must stand alone: `modules/ui` is what the CLI copies into consumer projects, which have neither
  * the vendored basecoat CSS nor `shadcn-presets.generated.css`. The `cn-empty*` hook classes and `data-slot`
  * attributes are additionally what the style-pack presets target — packs are unlayered, so where a pack defines a rule
  * it overrides the utilities below by design.
  */
object Empty:

  enum MediaVariant derives CanEqual:
    case Default, Icon

  private val base: String =
    "cn-empty flex w-full min-w-0 flex-1 flex-col items-center justify-center text-center text-balance"

  private val mediaBase: String =
    "cn-empty-media flex shrink-0 items-center justify-center [&_svg]:pointer-events-none [&_svg]:shrink-0"

  private val mediaVariantClasses: Map[MediaVariant, String] = Map(
    MediaVariant.Default -> "cn-empty-media-default bg-transparent",
    MediaVariant.Icon -> "cn-empty-media-icon"
  )

  private def mediaVariantName(variant: MediaVariant): String = variant match
    case MediaVariant.Default => "default"
    case MediaVariant.Icon    => "icon"

  def apply(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "empty",
      cls := base,
      mods
    )

  /** Compose root modifiers and child nodes, matching the Svelte Root component's slot. */
  def apply(mods: Modifier[HtmlElement]*)(children: Node*): HtmlElement =
    div(
      dataAttr("slot") := "empty",
      cls := base,
      mods,
      children
    )

  def header(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "empty-header",
      cls := "cn-empty-header flex max-w-sm flex-col items-center",
      mods
    )

  def media(variant: MediaVariant = MediaVariant.Default, mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "empty-icon",
      dataAttr("variant") := mediaVariantName(variant),
      cls := s"$mediaBase ${mediaVariantClasses(variant)}",
      mods
    )

  def title(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "empty-title",
      cls := "cn-font-heading cn-empty-title",
      mods
    )

  def description(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "empty-description",
      cls := "cn-empty-description text-sm/relaxed text-muted-foreground [&>a]:underline [&>a]:underline-offset-4 [&>a:hover]:text-primary",
      mods
    )

  def content(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "empty-content",
      cls := "cn-empty-content flex w-full max-w-sm min-w-0 flex-col items-center text-balance",
      mods
    )
