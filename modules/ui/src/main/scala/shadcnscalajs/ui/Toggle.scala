package shadcnscalajs.ui

import com.raquo.laminar.api.L.*
import com.raquo.laminar.codecs.BooleanAsTrueFalseStringCodec

/** shadcn/ui Toggle — a two-state pressed/unpressed button bound to a `Var[Boolean]`, matching Switch's reactive
  * pattern.
  */
object Toggle:

  enum Variant derives CanEqual:
    case Default, Outline

  enum Size derives CanEqual:
    case Default, Sm, Lg

  private val ariaPressedAttr: HtmlAttr[Boolean] = htmlAttr("aria-pressed", BooleanAsTrueFalseStringCodec)

  private val base =
    "inline-flex items-center justify-center gap-2 rounded-md text-sm font-medium hover:bg-muted hover:text-muted-foreground disabled:pointer-events-none disabled:opacity-50 data-[state=on]:bg-accent data-[state=on]:text-accent-foreground outline-none transition-[color,box-shadow] focus-visible:border-ring focus-visible:ring-[3px] focus-visible:ring-ring/50"

  private val variantClasses: Map[Variant, String] = Map(
    Variant.Default -> "bg-transparent",
    Variant.Outline -> "border border-input bg-transparent shadow-xs hover:bg-accent hover:text-accent-foreground"
  )

  private val sizeClasses: Map[Size, String] = Map(
    Size.Default -> "h-9 px-2 min-w-9",
    Size.Sm -> "h-8 px-1.5 min-w-8",
    Size.Lg -> "h-10 px-2.5 min-w-10"
  )

  def apply(
      pressedVar: Var[Boolean],
      variant: Variant = Variant.Default,
      size: Size = Size.Default,
      mods: Modifier[HtmlElement]*
  ): HtmlElement =
    button(
      typ := "button",
      cls := s"$base ${variantClasses(variant)} ${sizeClasses(size)}",
      dataAttr("state") <-- pressedVar.signal.map(if _ then "on" else "off"),
      ariaPressedAttr <-- pressedVar.signal,
      onClick --> { _ => pressedVar.update(!_) },
      mods
    )
