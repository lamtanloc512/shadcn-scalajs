package shadcnscalajs.ui

import com.raquo.laminar.api.L.*
import shadcnscalajs.core.DataAttrs.*

/** Laminar port of basecoat's `.btn` component (basecoat/src/css/components/button.css +
  * basecoat/src/css/styles/vega.css:20-80 for the `data-variant`/`data-size` contract). Pure CSS/no-JS tier: all visual
  * variants come from the vendored basecoat CSS bundle — this file only emits the right element/class/attributes.
  */
object Button:

  enum Variant derives CanEqual:
    case Primary, Secondary, Outline, Ghost, Destructive, Link

  enum Size derives CanEqual:
    case Default, Xs, Sm, Lg, Icon, IconXs, IconSm, IconLg

  /** Direct usage: `Button(cls := "w-full", onClick --> observer, "Click me")` */
  def apply(mods: Modifier[HtmlElement]*): HtmlElement =
    button(typ := "button", cls := "btn", mods)

  /** Builder-style usage mirroring the `Component.of(_.prop := value, ...)` pattern already proven by laminar-shoelace
    * / web-components-ui5 in this workspace: Button.of(_.variant(Button.Variant.Outline), _.size(Button.Size.Sm), _ =>
    * "Save")
    */
  def of(mods: (ButtonApi.type => Modifier[HtmlElement])*): HtmlElement =
    apply(mods.map(_(ButtonApi))*)

  object ButtonApi:
    def variant(value: Variant): Modifier[HtmlElement] = dataVariant := kebabCase(value.toString)
    def size(value: Size): Modifier[HtmlElement] = dataSize := kebabCase(value.toString)
