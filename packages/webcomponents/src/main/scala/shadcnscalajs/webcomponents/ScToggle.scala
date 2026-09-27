package shadcnscalajs.webcomponents

import com.raquo.laminar.api.L.*
import shadcnscalajs.core.Tags.slotTag
import shadcnscalajs.ui.Toggle

import scala.scalajs.js

class ScToggle extends ScElementBase:

  private val pressedVar = Var(false)
  private val disabledVar = Var(false)
  private val variantVar = Var(Toggle.Variant.Default)
  private val sizeVar = Var(Toggle.Size.Default)
  private val echo = EchoGuard[Boolean]()

  observeAttribute("pressed")(v => { echo.wrote(v.isDefined); pressedVar.set(v.isDefined) })
  observeAttribute("disabled")(v => disabledVar.set(v.isDefined))
  observeAttribute("variant")(v => variantVar.set(ScToggle.parseVariant(v)))
  observeAttribute("size")(v => sizeVar.set(ScToggle.parseSize(v)))
  booleanProperty("pressed")
  booleanProperty("disabled")
  stringProperty("variant")
  stringProperty("size")

  mount(
    div(
      child <-- variantVar.signal.combineWithFn(sizeVar.signal) { (variant, size) =>
        Toggle(pressedVar, variant, size, disabledVar.signal, slotTag())
      },
      pressedVar.signal.changes --> Observer[Boolean] { pressed =>
        if !echo.isEcho(pressed) then
          if pressed then setAttribute("pressed", "") else removeAttribute("pressed")
          emit("sc-change", pressed)
      }
    )
  )

object ScToggle:
  def register(): Unit =
    ScElements.define("sc-toggle", js.constructorOf[ScToggle], "pressed", "disabled", "variant", "size")

  private def parseVariant(v: Option[String]): Toggle.Variant = v match
    case Some("outline") => Toggle.Variant.Outline
    case _               => Toggle.Variant.Default

  private def parseSize(v: Option[String]): Toggle.Size = v match
    case Some("sm") => Toggle.Size.Sm
    case Some("lg") => Toggle.Size.Lg
    case _          => Toggle.Size.Default
