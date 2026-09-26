package shadcnscalajs.ui

import com.raquo.laminar.api.L.*

/** Legacy toast surface kept for docs call sites. Upstream ships sonner only (no toast/ directory, no `data-slot`
  * parts), so this file only aligns class hooks with sonner's toast row (`cn-toast`) and does not invent slots.
  */
object Toast:
  enum Variant derives CanEqual:
    case Default, Destructive

  def apply(variant: Variant = Variant.Default, mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      role := "status",
      cls := s"cn-toast toast-content group pointer-events-auto relative flex w-full items-center justify-between gap-4 overflow-hidden rounded-md border p-6 pr-8 shadow-lg transition-all ${variant match
          case Variant.Default     => "bg-background text-foreground"
          case Variant.Destructive => "destructive group border-destructive bg-destructive text-destructive-foreground"
        }",
      mods
    )
  def title(mods: Modifier[HtmlElement]*): HtmlElement = div(cls := "text-sm font-semibold", mods)
  def description(mods: Modifier[HtmlElement]*): HtmlElement = div(cls := "text-sm opacity-90", mods)
