package shadcnscalajs.ui

import com.raquo.laminar.api.L.*

object Badge:

  enum Variant derives CanEqual:
    case Primary, Secondary, Outline, Destructive, Ghost, Link

  private val base: String =
    "inline-flex w-fit shrink-0 items-center justify-center gap-1 overflow-hidden rounded-full border border-transparent px-2 py-0.5 text-xs font-medium whitespace-nowrap transition-[color,box-shadow] focus-visible:border-ring focus-visible:ring-[3px] focus-visible:ring-ring/50 aria-invalid:border-destructive aria-invalid:ring-destructive/20 dark:aria-invalid:ring-destructive/40 [&>svg]:pointer-events-none [&>svg]:size-3"

  private val variantClasses: Map[Variant, String] = Map(
    Variant.Primary -> "bg-primary text-primary-foreground [a&]:hover:bg-primary/90",
    Variant.Secondary -> "bg-secondary text-secondary-foreground [a&]:hover:bg-secondary/90",
    Variant.Destructive -> "bg-destructive text-white focus-visible:ring-destructive/20 dark:bg-destructive/60 dark:focus-visible:ring-destructive/40 [a&]:hover:bg-destructive/90",
    Variant.Outline -> "border-border text-foreground [a&]:hover:bg-accent [a&]:hover:text-accent-foreground",
    Variant.Ghost -> "[a&]:hover:bg-accent [a&]:hover:text-accent-foreground",
    Variant.Link -> "text-primary underline-offset-4 [a&]:hover:underline"
  )

  def apply(mods: Modifier[HtmlElement]*): HtmlElement =
    span(cls := s"badge cn-badge $base", mods)

  def of(mods: (BadgeApi.type => Modifier[HtmlElement])*): HtmlElement =
    apply(mods.map(_(BadgeApi))*)

  object BadgeApi:
    def variant(value: Variant): Modifier[HtmlElement] =
      val name = value.toString.toLowerCase match
        case "primary" => "primary"
        case other     => other
      Seq(dataAttr("variant") := name, cls(s"cn-badge-variant-$name"), cls(variantClasses(value)))
