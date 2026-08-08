package shadcnscalajs.ui

import com.raquo.laminar.api.L.*
import com.raquo.laminar.api.L.svg.{path as svgPath, svg as svgTag}

object Spinner:
  def apply(mods: Modifier[SvgElement]*): SvgElement =
    svgTag(
      aria.label := "Loading",
      Icons.svgSlot := "spinner",
      svg.cls := "cn-spinner size-4 animate-spin",
      svg.viewBox := "0 0 24 24",
      svg.fill := "none",
      svg.stroke := "currentColor",
      svg.strokeWidth := "2",
      svg.strokeLineCap := "round",
      svg.strokeLineJoin := "round",
      svgPath(svg.d := "M21 12a9 9 0 1 1-6.219-8.56"),
      mods
    )
