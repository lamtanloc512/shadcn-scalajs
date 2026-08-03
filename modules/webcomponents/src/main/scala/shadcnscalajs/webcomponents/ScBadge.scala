package shadcnscalajs.webcomponents

import com.raquo.laminar.api.L.*
import org.scalajs.dom
import shadcnscalajs.core.DataAttrs.*
import shadcnscalajs.core.Tags.slotTag
import shadcnscalajs.ui.Badge

import scala.scalajs.js

/** `<sc-badge variant="outline">New</sc-badge>` — Web Component export of
  * shadcnscalajs.ui.Badge.
  */
class ScBadge extends ScElementBase:

  private val variantVar = Var(Badge.Variant.Primary)

  observeAttribute("variant")(v => ScBadge.parseVariant(v).foreach(variantVar.set))

  mount(
    Badge(
      dataVariant <-- variantVar.signal.map(v => kebabCase(v.toString)),
      slotTag()
    )
  )

object ScBadge:

  def register(): Unit =
    dom.window.customElements.define("sc-badge", js.constructorOf[ScBadge])

  private def parseVariant(v: Option[String]): Option[Badge.Variant] = v.collect {
    case "primary"     => Badge.Variant.Primary
    case "secondary"   => Badge.Variant.Secondary
    case "outline"     => Badge.Variant.Outline
    case "destructive" => Badge.Variant.Destructive
    case "ghost"        => Badge.Variant.Ghost
    case "link"        => Badge.Variant.Link
  }
