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

  mount(
    span(
      cls := BadgeStyles.base,
      cls <-- variantVar.signal.map(BadgeStyles.variantClass),
      slotTag()
    )
  )

object ScBadge:

  def register(): Unit =
    ScElements.define("sc-badge", js.constructorOf[ScBadge], "variant")

  private def parseVariant(v: Option[String]): Option[Badge.Variant] = v.collect {
    case "primary" => Badge.Variant.Primary; case "secondary"   => Badge.Variant.Secondary
    case "outline" => Badge.Variant.Outline; case "destructive" => Badge.Variant.Destructive
    case "ghost"   => Badge.Variant.Ghost; case "link"          => Badge.Variant.Link
  }

private object BadgeStyles:
  val base =
    "inline-flex w-fit shrink-0 items-center justify-center gap-1 overflow-hidden rounded-full border border-transparent px-2 py-0.5 text-xs font-medium whitespace-nowrap transition-[color,box-shadow] focus-visible:border-ring focus-visible:ring-[3px] focus-visible:ring-ring/50 aria-invalid:border-destructive aria-invalid:ring-destructive/20 dark:aria-invalid:ring-destructive/40 [&>svg]:pointer-events-none [&>svg]:size-3"

  val variantClass: Badge.Variant => String =
    case Badge.Variant.Primary   => "bg-primary text-primary-foreground"
    case Badge.Variant.Secondary => "bg-secondary text-secondary-foreground"
    case Badge.Variant.Destructive =>
      "bg-destructive text-white focus-visible:ring-destructive/20 dark:bg-destructive/60 dark:focus-visible:ring-destructive/40"
    case Badge.Variant.Outline => "border-border text-foreground"
    case Badge.Variant.Ghost   => ""
    case Badge.Variant.Link    => "text-primary underline-offset-4"
