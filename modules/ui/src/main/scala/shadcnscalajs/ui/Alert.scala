package shadcnscalajs.ui

import com.raquo.laminar.api.L.*

object Alert:
  enum Variant derives CanEqual:
    case Default, Destructive

  def apply(variant: Variant = Variant.Default, mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      role := "alert",
      cls := s"alert cn-alert relative w-full rounded-lg border px-4 py-3 text-sm ${variant match
          case Variant.Default => "cn-alert-variant-default bg-background text-foreground"
          case Variant.Destructive =>
            "cn-alert-variant-destructive border-destructive/50 text-destructive dark:border-destructive [&>svg]:text-destructive"
        }",
      mods
    )

  def title(mods: Modifier[HtmlElement]*): HtmlElement =
    div(dataAttr("title") := "", cls := "cn-alert-title mb-1 font-medium leading-none tracking-tight", mods)
  def description(mods: Modifier[HtmlElement]*): HtmlElement = sectionTag(cls := "cn-alert-description text-sm [&_p]:leading-relaxed", mods)
