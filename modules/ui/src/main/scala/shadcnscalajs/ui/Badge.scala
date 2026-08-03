package shadcnscalajs.ui

import com.raquo.laminar.api.L.*
import shadcnscalajs.core.DataAttrs.*

/** Laminar port of basecoat's `.badge` component (basecoat/src/css/components/badge.css
  * + basecoat/src/css/styles/vega.css:292-317 — `data-variant` only, no `data-size`).
  * Pure CSS/no-JS tier.
  */
object Badge:

  enum Variant derives CanEqual:
    case Primary, Secondary, Outline, Destructive, Ghost, Link

  def apply(mods: Modifier[HtmlElement]*): HtmlElement =
    span(cls := "badge", mods)

  def of(mods: (BadgeApi.type => Modifier[HtmlElement])*): HtmlElement =
    apply(mods.map(_(BadgeApi))*)

  object BadgeApi:
    def variant(value: Variant): Modifier[HtmlElement] = dataVariant := kebabCase(value.toString)
