package shadcnscalajs.webcomponents

import com.raquo.laminar.api.L.*
import shadcnscalajs.ui.Progress

import scala.scalajs.js

class ScProgress extends ScElementBase:

  private val valueVar = Var(0)

  observeAttribute("value")(v => valueVar.set(v.flatMap(_.toIntOption).getOrElse(0).max(0).min(100)))
  stringProperty("value")

  mount(
    div(
      child <-- valueVar.signal.map(v => Progress(v))
    )
  )

object ScProgress:
  def register(): Unit =
    ScElements.define("sc-progress", js.constructorOf[ScProgress], "value")
