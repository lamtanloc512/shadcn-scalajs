package shadcnscalajs.ui

import com.raquo.laminar.api.L.*

object Toast:
  enum Variant derives CanEqual:
    case Default, Destructive

  def apply(variant: Variant = Variant.Default, mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      role := "status",
      cls := s"group pointer-events-auto relative flex w-full items-center justify-between gap-4 overflow-hidden rounded-md border p-6 pr-8 shadow-lg transition-all ${variant match
          case Variant.Default     => "bg-background text-foreground"
          case Variant.Destructive => "destructive group border-destructive bg-destructive text-destructive-foreground"
        }",
      mods
    )
  def title(mods: Modifier[HtmlElement]*): HtmlElement = div(cls := "text-sm font-semibold", mods)
  def description(mods: Modifier[HtmlElement]*): HtmlElement = div(cls := "text-sm opacity-90", mods)
