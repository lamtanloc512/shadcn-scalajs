package shadcnscalajs.ui

import com.raquo.laminar.api.L.*

/** shadcn/ui Item — styled with Tailwind CSS utilities matching the canonical new-york-v4 item components.
  *
  * The utility classes must stand alone: `modules/ui` is what the CLI copies into consumer projects, which have neither
  * the vendored basecoat CSS nor `shadcn-presets.generated.css`. The `cn-item*` hook classes and `data-slot` attributes
  * are additionally what the style-pack presets target — packs are unlayered, so where a pack defines a rule it
  * overrides the utilities below by design.
  */
object Item:

  enum Variant derives CanEqual:
    case Default, Outline, Muted

  enum Size derives CanEqual:
    case Default, Sm, Xs

  enum MediaVariant derives CanEqual:
    case Default, Icon, Image

  // shadcn/ui item base classes (from item.svelte)
  private val base: String =
    "cn-item group/item [a]:hover:bg-muted flex w-full flex-wrap items-center rounded-lg border text-sm transition-colors duration-100 outline-none focus-visible:border-ring focus-visible:ring-[3px] focus-visible:ring-ring/50 [a]:transition-colors"

  private val variantClasses: Map[Variant, String] = Map(
    Variant.Default -> "cn-item-variant-default border-transparent",
    Variant.Outline -> "cn-item-variant-outline border-border",
    Variant.Muted -> "cn-item-variant-muted bg-muted/50 border-transparent"
  )

  private val sizeClasses: Map[Size, String] = Map(
    Size.Default -> "cn-item-size-default gap-2.5 px-3 py-2.5",
    Size.Sm -> "cn-item-size-sm gap-2.5 px-3 py-2.5",
    Size.Xs -> "cn-item-size-xs gap-2 px-2.5 py-2 in-data-[slot=dropdown-menu-content]:p-0"
  )

  private val mediaBase: String =
    "cn-item-media flex shrink-0 items-center justify-center gap-2 group-has-data-[slot=item-description]/item:translate-y-0.5 group-has-data-[slot=item-description]/item:self-start [&_svg]:pointer-events-none"

  private val mediaVariantClasses: Map[MediaVariant, String] = Map(
    MediaVariant.Default -> "cn-item-media-variant-default bg-transparent",
    MediaVariant.Icon -> "cn-item-media-variant-icon [&_svg:not([class*='size-'])]:size-4",
    MediaVariant.Image ->
      "cn-item-media-variant-image size-10 overflow-hidden rounded-sm group-data-[size=sm]/item:size-8 group-data-[size=xs]/item:size-6 [&_img]:size-full [&_img]:object-cover"
  )

  val baseClass: String = base

  def variantClass(variant: Variant): String = variantClasses(variant)

  def sizeClass(size: Size): String = sizeClasses(size)

  def apply(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "item",
      dataAttr("variant") := Variant.Default.toString.toLowerCase,
      dataAttr("size") := Size.Default.toString.toLowerCase,
      cls := s"$base ${variantClasses(Variant.Default)} ${sizeClasses(Size.Default)}",
      mods
    )

  /** Builder-style: `Item.of(_.variant(Item.Variant.Muted), _.size(Item.Size.Sm), _ => ...)` */
  def of(mods: (ItemApi.type => Modifier[HtmlElement])*): HtmlElement =
    div(
      dataAttr("slot") := "item",
      cls := base,
      mods.map(_(ItemApi))
    )

  object ItemApi:
    def variant(value: Variant): Modifier[HtmlElement] =
      val name = value.toString.toLowerCase
      Seq(dataAttr("variant") := name, cls(s"cn-item-variant-$name"), cls(variantClasses(value)))

    def size(value: Size): Modifier[HtmlElement] =
      val name = value.toString.toLowerCase
      Seq(dataAttr("size") := name, cls(s"cn-item-size-$name"), cls(sizeClasses(value)))

  def group(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      role := "list",
      dataAttr("slot") := "item-group",
      cls := "cn-item-group group/item-group flex w-full flex-col gap-4 has-data-[size=sm]:gap-2.5 has-data-[size=xs]:gap-2",
      mods
    )

  def header(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "item-header",
      cls := "cn-item-header flex basis-full items-center justify-between gap-2",
      mods
    )

  def footer(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "item-footer",
      cls := "cn-item-footer flex basis-full items-center justify-between gap-2",
      mods
    )

  def separator(mods: Modifier[HtmlElement]*): HtmlElement =
    Separator(
      Separator.Orientation.Horizontal,
      dataAttr("slot") := "item-separator",
      cls := "cn-item-separator my-2",
      mods
    )

  def media(variant: MediaVariant = MediaVariant.Default, mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "item-media",
      dataAttr("variant") := variant.toString.toLowerCase,
      cls := s"$mediaBase ${mediaVariantClasses(variant)}",
      mods
    )

  def content(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "item-content",
      cls := "cn-item-content flex flex-1 flex-col gap-1 group-data-[size=xs]/item:gap-0 [&+[data-slot=item-content]]:flex-none",
      mods
    )

  def title(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "item-title",
      cls := "cn-font-heading cn-item-title line-clamp-1 flex w-fit items-center gap-2 text-sm leading-snug font-medium underline-offset-4",
      mods
    )

  def description(mods: Modifier[HtmlElement]*): HtmlElement =
    p(
      dataAttr("slot") := "item-description",
      cls := "cn-item-description line-clamp-2 text-left text-sm leading-normal font-normal text-muted-foreground group-data-[size=xs]/item:text-xs [&>a]:underline [&>a]:underline-offset-4 [&>a:hover]:text-primary",
      mods
    )

  def actions(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "item-actions",
      cls := "cn-item-actions flex items-center gap-2",
      mods
    )
