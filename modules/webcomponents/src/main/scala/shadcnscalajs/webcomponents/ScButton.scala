package shadcnscalajs.webcomponents

import com.raquo.laminar.api.L.*
import org.scalajs.dom
import shadcnscalajs.core.DataAttrs.*
import shadcnscalajs.core.Tags.slotTag
import shadcnscalajs.ui.Button

import scala.scalajs.js

/** `<sc-button variant="outline" size="sm">Save</sc-button>` — Web Component
  * export of shadcnscalajs.ui.Button, so plain JS/React/Vue/HTML consumers
  * can use it without a Scala toolchain (see implementation plan's Web
  * Component export layer section).
  */
class ScButton extends ScElementBase:

  private val variantVar = Var(Button.Variant.Primary)
  private val sizeVar = Var(Button.Size.Default)

  observeAttribute("variant")(v => ScButton.parseVariant(v).foreach(variantVar.set))
  observeAttribute("size")(v => ScButton.parseSize(v).foreach(sizeVar.set))

  mount(
    Button(
      dataVariant <-- variantVar.signal.map(v => kebabCase(v.toString)),
      dataSize <-- sizeVar.signal.map(v => kebabCase(v.toString)),
      slotTag()
    )
  )

object ScButton:

  def register(): Unit =
    dom.window.customElements.define("sc-button", js.constructorOf[ScButton])

  private def parseVariant(v: Option[String]): Option[Button.Variant] = v.collect {
    case "primary"     => Button.Variant.Primary
    case "secondary"   => Button.Variant.Secondary
    case "outline"     => Button.Variant.Outline
    case "ghost"       => Button.Variant.Ghost
    case "destructive" => Button.Variant.Destructive
    case "link"        => Button.Variant.Link
  }

  private def parseSize(v: Option[String]): Option[Button.Size] = v.collect {
    case "default" => Button.Size.Default
    case "xs"      => Button.Size.Xs
    case "sm"      => Button.Size.Sm
    case "lg"      => Button.Size.Lg
    case "icon"    => Button.Size.Icon
    case "icon-xs" => Button.Size.IconXs
    case "icon-sm" => Button.Size.IconSm
    case "icon-lg" => Button.Size.IconLg
  }
