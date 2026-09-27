package shadcnscalajs.webcomponents

import com.raquo.laminar.api.L.*
import shadcnscalajs.core.Tags.slotTag
import shadcnscalajs.ui.AspectRatio

import scala.scalajs.js

class ScAspectRatio extends ScElementBase:

  private val ratioVar = Var(1.0)

  observeAttribute("ratio")(v =>
    ratioVar.set(v.flatMap(_.toDoubleOption).filter(r => r > 0 && !r.isInfinity && !r.isNaN).getOrElse(1.0))
  )
  stringProperty("ratio")

  mount(div(child <-- ratioVar.signal.map(r => AspectRatio(r, slotTag()))))

object ScAspectRatio:
  def register(): Unit =
    ScElements.define("sc-aspect-ratio", js.constructorOf[ScAspectRatio], "ratio")
