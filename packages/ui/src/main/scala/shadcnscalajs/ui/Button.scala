package shadcnscalajs.ui

import com.raquo.laminar.api.L.*

/** shadcn/ui Button. Visuals come from the active style pack via `cn-button-*` hooks. The utilities below are the
  * no-pack fallback and match Nova, the default pack. `Primary` is upstream's `default` variant — the pack has no
  * `primary` hook, so emitting that name left every default button on the old utilities.
  */
object Button:

  enum Variant derives CanEqual:
    case Primary, Secondary, Outline, Ghost, Destructive, Link

  enum Size derives CanEqual:
    case Default, Xs, Sm, Lg, Icon, IconXs, IconSm, IconLg

  private val base: String =
    "inline-flex shrink-0 items-center justify-center gap-2 rounded-lg border border-transparent bg-clip-padding text-sm font-medium whitespace-nowrap transition-all outline-none select-none focus-visible:border-ring focus-visible:ring-[3px] focus-visible:ring-ring/50 disabled:pointer-events-none disabled:opacity-50 aria-invalid:border-destructive aria-invalid:ring-destructive/20 dark:aria-invalid:ring-destructive/40 [&_svg]:pointer-events-none [&_svg]:shrink-0 [&_svg:not([class*='size-'])]:size-4"

  private val variantClasses: Map[Variant, String] = Map(
    Variant.Primary -> "bg-primary text-primary-foreground hover:bg-primary/80",
    Variant.Destructive -> "bg-destructive/10 text-destructive hover:bg-destructive/20 focus-visible:border-destructive/40 focus-visible:ring-destructive/20 dark:bg-destructive/20 dark:hover:bg-destructive/30 dark:focus-visible:ring-destructive/40",
    Variant.Outline -> "border-border bg-background hover:bg-muted hover:text-foreground dark:border-input dark:bg-input/30 dark:hover:bg-input/50",
    Variant.Secondary -> "bg-secondary text-secondary-foreground hover:bg-secondary/80",
    Variant.Ghost -> "hover:bg-muted hover:text-foreground dark:hover:bg-muted/50",
    Variant.Link -> "text-primary underline-offset-4 hover:underline"
  )

  private val sizeClasses: Map[Size, String] = Map(
    Size.Default -> "h-8 gap-1.5 px-2.5",
    Size.Xs -> "h-6 gap-1 rounded-[min(var(--radius-md),10px)] px-2 text-xs [&_svg:not([class*='size-'])]:size-3",
    Size.Sm -> "h-7 gap-1 rounded-[min(var(--radius-md),12px)] px-2.5 text-[0.8rem] [&_svg:not([class*='size-'])]:size-3.5",
    Size.Lg -> "h-9 gap-1.5 px-2.5",
    Size.Icon -> "size-8",
    Size.IconXs -> "size-6 rounded-[min(var(--radius-md),10px)] [&_svg:not([class*='size-'])]:size-3",
    Size.IconSm -> "size-7 rounded-[min(var(--radius-md),12px)]",
    Size.IconLg -> "size-9"
  )

  /** Direct usage: `Button(cls := "w-full", onClick --> observer, "Click me")` */
  def apply(mods: Modifier[HtmlElement]*): HtmlElement =
    button(typ := "button", dataAttr("slot") := "button", cls := s"cn-button group/button $base", defaultHooks, mods)

  /** The `href` branch of upstream's button: an anchor carrying `data-slot="button"` so it picks up the same pack rules
    * and the same button-group joining selectors as a real button.
    */
  def anchor(hrefValue: String, mods: Modifier[HtmlElement]*): HtmlElement =
    a(href := hrefValue, dataAttr("slot") := "button", cls := s"cn-button group/button $base", defaultHooks, mods)

  /** Pack rules key off `cn-button-variant-default` / `cn-button-size-default`. Callers that pass a variant or size set
    * those attributes during construction; this only fills the ones they left off, after modifiers have run.
    */
  private val defaultHooks: Modifier[HtmlElement] =
    onMountCallback { ctx =>
      val el = ctx.thisNode.ref
      if !el.hasAttribute("data-variant") then
        el.setAttribute("data-variant", "default")
        el.classList.add("cn-button-variant-default")
      if !el.hasAttribute("data-size") then
        el.setAttribute("data-size", "default")
        el.classList.add("cn-button-size-default")
    }

  def variantData(value: Variant): String = value match
    case Variant.Primary => "default"
    case other           => other.toString.toLowerCase

  def sizeData(value: Size): String =
    value.toString.replace("Icon", "icon-").stripSuffix("-").toLowerCase match
      case "icon-" => "icon"
      case other    => other

  private def variantName(value: Variant): String = variantData(value)

  private def sizeName(value: Size): String = sizeData(value)

  /** Everything a real button carries — base classes, variant, and size — for parts that must look like one without
    * being one: a menu or popover trigger, an anchor styled as a button.
    *
    * Prefer this over [[classes]]. Variant and size are data attributes plus utilities. A trigger that only copies the
    * class string, without those attributes, falls through to the primary / default-size rules in `globals.css`.
    */
  def appearance(variant: Variant = Variant.Primary, size: Size = Size.Default): Modifier[HtmlElement] =
    Seq[Modifier[HtmlElement]](
      cls := s"cn-button group/button $base",
      ButtonApi.variant(variant),
      ButtonApi.size(size)
    )

  /** The class list alone, upstream's `buttonVariants({ variant, size })`, for the rare caller that can only pass a
    * string — a reactive `cls <--` whose variant changes, say. Such a caller must set `data-variant` and `data-size`
    * itself; [[appearance]] does both and is what most parts want.
    */
  def classes(variant: Variant = Variant.Primary, size: Size = Size.Default): String =
    s"cn-button group/button $base cn-button-variant-${variantName(variant)} ${variantClasses(variant)} cn-button-size-${sizeName(size)} ${sizeClasses(size)}"

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
