package shadcnscalajs.ui

import com.raquo.laminar.api.L.*

/** shadcn/ui Alert — styled with Tailwind CSS utilities matching the canonical new-york-v4 alert.tsx.
  *
  * The utility classes must stand alone: `modules/ui` is what the CLI copies into consumer projects, which have neither
  * the vendored basecoat CSS nor `shadcn-presets.generated.css`. The `cn-alert*` hook classes and `data-slot`
  * attributes are additionally what the style-pack presets target (see `bases/base/ui/alert.tsx` upstream, and
  * `modules/site/src/styles/shadcn-presets.generated.css`) — packs are unlayered, so where a pack defines a rule it
  * overrides the utilities below by design.
  */
object Alert:
  enum Variant derives CanEqual:
    case Default, Destructive

  // shadcn/ui alert base classes (from alert.tsx line 7)
  private val base: String =
    "relative grid w-full grid-cols-[0_1fr] items-start gap-y-0.5 rounded-lg border px-4 py-3 text-sm has-[>svg]:grid-cols-[calc(var(--spacing)*4)_1fr] has-[>svg]:gap-x-3 [&>svg]:size-4 [&>svg]:translate-y-0.5 [&>svg]:text-current"

  private val variantClasses: Map[Variant, String] = Map(
    Variant.Default -> "bg-card text-card-foreground",
    Variant.Destructive ->
      "bg-card text-destructive *:data-[slot=alert-description]:text-destructive/90 [&>svg]:text-current"
  )

  /** Root and per-variant class strings, public so `modules/webcomponents`' `ScAlert` can drive the variant off a
    * signal without duplicating these strings (that duplication is why `ScButton`/`ScBadge` drift from their `ui`
    * counterparts).
    */
  val baseClass: String = s"cn-alert group/alert $base"

  def variantClass(variant: Variant): String =
    s"cn-alert-variant-${variant.toString.toLowerCase} ${variantClasses(variant)}"

  def apply(variant: Variant = Variant.Default, mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "alert",
      role := "alert",
      cls := s"$baseClass ${variantClass(variant)}",
      mods
    )

  def title(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "alert-title",
      cls := "cn-alert-title col-start-2 line-clamp-1 min-h-4 font-medium tracking-tight",
      mods
    )

  def description(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "alert-description",
      cls := "cn-alert-description col-start-2 grid justify-items-start gap-1 text-sm text-muted-foreground [&_p]:leading-relaxed",
      mods
    )
