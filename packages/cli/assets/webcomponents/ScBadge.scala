package shadcnscalajs.webcomponents

import com.raquo.laminar.api.L.*
import org.scalajs.dom
import shadcnscalajs.core.Tags.slotTag
import shadcnscalajs.ui.Badge

import scala.scalajs.js

class ScBadge extends ScElementBase:

  private val variantVar = Var(Badge.Variant.Primary)

  observeAttribute("variant")(v => ScBadge.parseVariant(v).foreach(variantVar.set))
  stringProperty("variant")

  mount(ScBadge.view(variantVar))

object ScBadge:

  // Render the consumer's copied Badge source, rather than duplicating its classes in this adapter.
  private def view(variantVar: Var[Badge.Variant]): HtmlElement =
    div(child <-- variantVar.signal.map(v => Badge.of(_.variant(v), _ => slotTag())))

  def register(): Unit =
    ScElements.define("sc-badge", js.constructorOf[ScBadge], "variant")

  private def parseVariant(v: Option[String]): Option[Badge.Variant] = v.collect {
    case "primary" => Badge.Variant.Primary; case "secondary"   => Badge.Variant.Secondary
    case "outline" => Badge.Variant.Outline; case "destructive" => Badge.Variant.Destructive
    case "ghost"   => Badge.Variant.Ghost; case "link"          => Badge.Variant.Link
  }
