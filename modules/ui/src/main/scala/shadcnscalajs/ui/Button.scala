package shadcnscalajs.ui

import com.raquo.laminar.api.L.*

/** shadcn/ui Button — styled with Tailwind CSS utilities matching the canonical new-york-v4 button.tsx. Uses the same
  * variant/size enums as the basecoat version so the Scala API is unchanged.
  */
object Button:

  enum Variant derives CanEqual:
    case Primary, Secondary, Outline, Ghost, Destructive, Link

  enum Size derives CanEqual:
    case Default, Xs, Sm, Lg, Icon, IconXs, IconSm, IconLg

  // shadcn/ui button base classes (from button.tsx line 8)
  private val base: String =
    "inline-flex shrink-0 items-center justify-center gap-2 rounded-md text-sm font-medium whitespace-nowrap transition-all outline-none focus-visible:border-ring focus-visible:ring-[3px] focus-visible:ring-ring/50 disabled:pointer-events-none disabled:opacity-50 aria-invalid:border-destructive aria-invalid:ring-destructive/20 dark:aria-invalid:ring-destructive/40 [&_svg]:pointer-events-none [&_svg]:shrink-0 [&_svg:not([class*='size-'])]:size-4"

  private val variantClasses: Map[Variant, String] = Map(
    Variant.Primary -> "bg-primary text-primary-foreground hover:bg-primary/90",
    Variant.Destructive -> "bg-destructive text-white hover:bg-destructive/90 focus-visible:ring-destructive/20 dark:bg-destructive/60 dark:focus-visible:ring-destructive/40",
    Variant.Outline -> "border bg-background shadow-xs hover:bg-accent hover:text-accent-foreground dark:border-input dark:bg-input/30 dark:hover:bg-input/50",
    Variant.Secondary -> "bg-secondary text-secondary-foreground hover:bg-secondary/80",
    Variant.Ghost -> "hover:bg-accent hover:text-accent-foreground dark:hover:bg-accent/50",
    Variant.Link -> "text-primary underline-offset-4 hover:underline"
  )

  private val sizeClasses: Map[Size, String] = Map(
    Size.Default -> "h-9 px-4 py-2 has-[>svg]:px-3",
    Size.Xs -> "h-6 gap-1 rounded-md px-2 text-xs has-[>svg]:px-1.5 [&_svg:not([class*='size-'])]:size-3",
    Size.Sm -> "h-8 gap-1.5 rounded-md px-3 has-[>svg]:px-2.5",
    Size.Lg -> "h-10 rounded-md px-6 has-[>svg]:px-4",
    Size.Icon -> "size-9",
    Size.IconXs -> "size-6 rounded-md [&_svg:not([class*='size-'])]:size-3",
    Size.IconSm -> "size-8",
    Size.IconLg -> "size-10"
  )

  /** Direct usage: `Button(cls := "w-full", onClick --> observer, "Click me")` */
  def apply(mods: Modifier[HtmlElement]*): HtmlElement =
    button(typ := "button", dataAttr("slot") := "button", cls := s"btn cn-button group/button $base", mods)

  /** The `href` branch of upstream's button: an anchor carrying `data-slot="button"` so it picks up the same pack rules
    * and the same button-group joining selectors as a real button.
    */
  def anchor(hrefValue: String, mods: Modifier[HtmlElement]*): HtmlElement =
    a(href := hrefValue, dataAttr("slot") := "button", cls := s"btn cn-button group/button $base", mods)

  private def variantName(value: Variant): String = value.toString.toLowerCase

  private def sizeName(value: Size): String =
    value.toString.replace("Icon", "icon-").stripSuffix("-").toLowerCase match
      case "icon-" => "icon"
      case other   => other

  /** Everything a real button carries — base classes, variant, and size — for parts that must look like one without
    * being one: a menu or popover trigger, an anchor styled as a button.
    *
    * Prefer this over [[classes]]. Half of a button's skin hangs off `data-variant`/`data-size`, which no class list
    * can set: basecoat paints `.btn:not([data-variant])` as a solid primary button, and style packs size through the
    * `cn-button-*` hooks, so a trigger dressed in classes alone comes out filled and a different height from the
    * buttons beside it.
    */
  def appearance(variant: Variant = Variant.Primary, size: Size = Size.Default): Modifier[HtmlElement] =
    Seq[Modifier[HtmlElement]](
      cls := s"btn cn-button group/button $base",
      ButtonApi.variant(variant),
      ButtonApi.size(size)
    )

  /** The class list alone, upstream's `buttonVariants({ variant, size })`, for the rare caller that can only pass a
    * string — a reactive `cls <--` whose variant changes, say. Such a caller must set `data-variant` and `data-size`
    * itself; [[appearance]] does both and is what most parts want.
    */
  def classes(variant: Variant = Variant.Primary, size: Size = Size.Default): String =
    s"btn cn-button group/button $base cn-button-variant-${variantName(variant)} ${variantClasses(variant)} cn-button-size-${sizeName(size)} ${sizeClasses(size)}"

  /** Builder-style: `Button.of(_.variant(Button.Variant.Outline), _.size(Button.Size.Sm), _ => "Save")` */
  def of(mods: (ButtonApi.type => Modifier[HtmlElement])*): HtmlElement =
    apply(mods.map(_(ButtonApi))*)

  object ButtonApi:
    def variant(value: Variant): Modifier[HtmlElement] =
      val name = variantName(value)
      Seq(dataAttr("variant") := name, cls(s"cn-button-variant-$name"), cls(variantClasses(value)))
    def size(value: Size): Modifier[HtmlElement] =
      val name = sizeName(value)
      Seq(dataAttr("size") := name, cls(s"cn-button-size-$name"), cls(sizeClasses(value)))
