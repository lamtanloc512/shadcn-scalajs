package shadcnscalajs.ui

import com.raquo.laminar.api.L.*

object Alert:
  enum Variant derives CanEqual:
    case Default, Destructive

  def apply(variant: Variant = Variant.Default, mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      role := "alert",
      cls := s"relative w-full rounded-lg border px-4 py-3 text-sm ${variant match
          case Variant.Default => "bg-background text-foreground"
          case Variant.Destructive =>
            "border-destructive/50 text-destructive dark:border-destructive [&>svg]:text-destructive"
        }",
      mods
    )

  def title(mods: Modifier[HtmlElement]*): HtmlElement =
    div(cls := "mb-1 font-medium leading-none tracking-tight", mods)
  def description(mods: Modifier[HtmlElement]*): HtmlElement = div(cls := "text-sm [&_p]:leading-relaxed", mods)
