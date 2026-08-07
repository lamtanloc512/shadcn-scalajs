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
    "cn-toggle group/toggle inline-flex items-center justify-center whitespace-nowrap outline-none hover:bg-muted focus-visible:ring-[3px] disabled:pointer-events-none disabled:opacity-50 [&_svg]:pointer-events-none [&_svg]:shrink-0"

  private def variantClasses(variant: Variant): String = variant match
    case Variant.Default => "cn-toggle-variant-default"
    case Variant.Outline => "cn-toggle-variant-outline"

  private def sizeClasses(size: Size): String = size match
    case Size.Default => "cn-toggle-size-default"
    case Size.Sm      => "cn-toggle-size-sm"
    case Size.Lg      => "cn-toggle-size-lg"

  def apply(
      pressedVar: Var[Boolean],
      variant: Variant,
      size: Size,
      mods: Modifier[HtmlElement]*
  ): HtmlElement =
    apply(pressedVar, variant, size, Val(false), mods*)

  def apply(
      pressedVar: Var[Boolean],
      variant: Variant = Variant.Default,
      size: Size = Size.Default,
      isDisabled: Signal[Boolean] = Val(false),
      mods: Modifier[HtmlElement]*
  ): HtmlElement =
    button(
      typ := "button",
      dataAttr("slot") := "toggle",
      cls := s"$base ${variantClasses(variant)} ${sizeClasses(size)}",
      dataAttr("state") <-- pressedVar.signal.map(if _ then "on" else "off"),
      ariaPressedAttr <-- pressedVar.signal,
      disabled <-- isDisabled,
      onClick --> { _ => pressedVar.update(!_) },
      mods
    )
