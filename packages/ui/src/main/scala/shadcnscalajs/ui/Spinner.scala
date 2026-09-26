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
      svgPath(svg.d := "M12 2v4"),
      svgPath(svg.d := "m16.2 7.8 2.9-2.9"),
      svgPath(svg.d := "M18 12h4"),
      svgPath(svg.d := "m16.2 16.2 2.9 2.9"),
      svgPath(svg.d := "M12 18v4"),
      svgPath(svg.d := "m4.9 19.1 2.9-2.9"),
      svgPath(svg.d := "M2 12h4"),
      svgPath(svg.d := "m4.9 4.9 2.9 2.9"),
      mods
    )
