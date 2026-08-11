package shadcnscalajs.webcomponents

import com.raquo.laminar.api.L.*
import shadcnscalajs.ui.{Separator, Spinner}

import scala.scalajs.js

class ScSeparator extends ScElementBase:

  private val orientationVar = Var(Separator.Orientation.Horizontal)

  observeAttribute("orientation")(v =>
    orientationVar.set(
      if v.contains("vertical") then Separator.Orientation.Vertical
      else Separator.Orientation.Horizontal
    )
  )
  stringProperty("orientation")

  mount(div(child <-- orientationVar.signal.map(o => Separator(o))))

object ScSeparator:
  def register(): Unit =
    ScElements.define("sc-separator", js.constructorOf[ScSeparator], "orientation")

class ScSpinner extends ScElementBase:
  mount(div(cls := "inline-flex", Spinner()))

object ScSpinner:
  def register(): Unit =
    ScElements.define("sc-spinner", js.constructorOf[ScSpinner])
