package shadcnscalajs.ui

import com.raquo.laminar.api.L.*
import org.scalajs.dom

/** Native `<input type="range">` styled to match [[Slider]] — the form-native, no-JS-state alternative to the composite
  * slider, which builds its own track/range/thumb markup.
  *
  * It deliberately does not reuse the `cn-slider*` hooks: those describe the composite slider's inner parts, so a style
  * pack's `.cn-slider-range { background: var(--primary) }` painted this input as a solid bar with the browser's own
  * track and thumb drawn on top.
  */
object Range:

  private val trackClasses =
    "[&::-webkit-slider-runnable-track]:h-1.5 [&::-webkit-slider-runnable-track]:rounded-full " +
      "[&::-webkit-slider-runnable-track]:bg-[linear-gradient(to_right,var(--primary)_var(--cn-range-fill,0%),var(--muted)_var(--cn-range-fill,0%))] " +
      "[&::-moz-range-track]:h-1.5 [&::-moz-range-track]:rounded-full [&::-moz-range-track]:bg-[var(--muted)] " +
      "[&::-moz-range-progress]:h-1.5 [&::-moz-range-progress]:rounded-full [&::-moz-range-progress]:bg-[var(--primary)]"

  // The thumb is taller than the track, so it needs pulling up by half the difference to sit centred.
  private val thumbClasses =
    "[&::-webkit-slider-thumb]:-mt-[5px] [&::-webkit-slider-thumb]:size-4 [&::-webkit-slider-thumb]:appearance-none " +
      "[&::-webkit-slider-thumb]:rounded-full [&::-webkit-slider-thumb]:border [&::-webkit-slider-thumb]:border-primary " +
      "[&::-webkit-slider-thumb]:bg-background [&::-webkit-slider-thumb]:shadow-sm " +
      "[&::-moz-range-thumb]:size-4 [&::-moz-range-thumb]:appearance-none [&::-moz-range-thumb]:rounded-full " +
      "[&::-moz-range-thumb]:border [&::-moz-range-thumb]:border-primary [&::-moz-range-thumb]:bg-background"

  private val focusClasses =
    "focus-visible:[&::-webkit-slider-thumb]:ring-[3px] focus-visible:[&::-webkit-slider-thumb]:ring-ring/50 " +
      "focus-visible:[&::-moz-range-thumb]:ring-[3px] focus-visible:[&::-moz-range-thumb]:ring-ring/50"

  private val baseClasses =
    "cn-range h-4 w-full cursor-pointer appearance-none bg-transparent outline-none " +
      "disabled:pointer-events-none disabled:opacity-50"

  /** WebKit has no `::-moz-range-progress` equivalent, so the filled part of the track is a gradient stop driven by
    * this variable.
    */
  private def syncFill(el: dom.html.Input): Unit =
    val min = el.min.toDoubleOption.getOrElse(0.0)
    val max = el.max.toDoubleOption.getOrElse(100.0)
    val value = el.value.toDoubleOption.getOrElse(min)
    val fill = if max <= min then 0.0 else ((value - min) / (max - min)) * 100
    el.style.setProperty("--cn-range-fill", s"${math.max(0.0, math.min(100.0, fill))}%")

  def apply(mods: Modifier[HtmlElement]*): HtmlElement =
    input(
      typ := "range",
      dataAttr("slot") := "range",
      cls := s"$baseClasses $trackClasses $thumbClasses $focusClasses",
      mods,
      onInput --> { ev => syncFill(ev.target.asInstanceOf[dom.html.Input]) },
      onMountCallback { ctx => syncFill(ctx.thisNode.ref) }
    )
