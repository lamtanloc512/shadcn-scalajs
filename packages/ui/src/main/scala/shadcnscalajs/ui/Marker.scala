package shadcnscalajs.ui

import com.raquo.laminar.api.L.*

/** shadcn/ui Marker — an inline status or divider line in a conversation ("Today", "Thinking…", "Joined the chat").
  *
  * `Separator` draws rules on both sides of the centered content; `Border` underlines the row.
  */
object Marker:

  // Class lists shared with the light-DOM `sc-*` web component hosts.
  val iconClass: String =
    "cn-marker-icon size-4 shrink-0 [&_svg:not([class*='size-'])]:size-4"

  val contentClass: String =
    "cn-marker-content min-w-0 wrap-break-word group-data-[variant=separator]/marker:flex-none group-data-[variant=separator]/marker:text-center *:[a]:underline *:[a]:underline-offset-3 *:[a]:hover:text-foreground"

  enum Variant derives CanEqual:
    case Default, Separator, Border

  private val base: String =
    "cn-marker group/marker relative flex min-h-4 w-full items-center gap-2 text-left text-sm text-muted-foreground [&_svg:not([class*='size-'])]:size-4 [a]:underline [a]:underline-offset-3 [a]:hover:text-foreground"

  private val variantClasses: Map[Variant, String] = Map(
    Variant.Default -> "",
    Variant.Separator ->
      "before:mr-1 before:h-px before:min-w-0 before:flex-1 before:bg-border after:ml-1 after:h-px after:min-w-0 after:flex-1 after:bg-border",
    Variant.Border -> "border-b border-border pb-2"
  )

  def variantClass(variant: Variant): String = s"$base ${variantClasses(variant)}"

  def apply(mods: Modifier[HtmlElement]*): HtmlElement = of(Variant.Default)(mods*)

  def of(variant: Variant)(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "marker",
      dataAttr("variant") := variant.toString.toLowerCase,
      cls := variantClass(variant),
      mods
    )

  def icon(mods: Modifier[HtmlElement]*): HtmlElement =
    span(
      dataAttr("slot") := "marker-icon",
      aria.hidden := true,
      cls := iconClass,
      mods
    )

  def content(mods: Modifier[HtmlElement]*): HtmlElement =
    span(
      dataAttr("slot") := "marker-content",
      cls := contentClass,
      mods
    )
