package shadcnscalajs.ui

import com.raquo.laminar.api.L.*

/** Native range input stand-in corresponding to upstream slider's range part (`data-slot="slider-range"`). */
object Range:
  def apply(mods: Modifier[HtmlElement]*): HtmlElement =
    input(
      typ := "range",
      dataAttr("slot") := "slider-range",
      cls := "input cn-slider-range cn-slider h-5 w-full cursor-pointer accent-primary",
      mods
    )
