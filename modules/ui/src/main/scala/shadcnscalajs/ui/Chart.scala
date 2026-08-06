package shadcnscalajs.ui

import com.raquo.laminar.api.L.*
import com.raquo.laminar.api.L.svg.{
  defs as svgDefs,
  g as svgG,
  linearGradient as svgLinearGradient,
  path as svgPath,
  rect as svgRect,
  stop as svgStop,
  svg as svgTag,
  text as svgText
}
import org.scalajs.dom

/** shadcn/ui Chart — Laminar-owned SVG helpers (bar, area, donut) matching preview-02 card shapes. No charting library;
  * geometry is computed in Scala and colors use `--chart-1`..`--chart-5` CSS tokens.
  */
object Chart:

  final case class Point(label: String, amount: Double, color: String)

  enum TooltipIndicator derives CanEqual:
    case Dot, Line, Dashed

  /** Shared hover state — pass to `Chart.bar` / `Chart.area` / `Chart.donut` and `Chart.tooltip`. */
  final class HoverVar(initial: Option[Point] = None):
    // hoverVar must be initialized before signal — Scala initializes vals in source order,
    // and reading an uninitialized field under Scala.js throws NPE (UndefinedBehaviorError).
    private[Chart] val hoverVar: Var[Option[Point]] = Var(initial)
    val signal: Signal[Option[Point]] = hoverVar.signal

  def hoverVar(initial: Option[Point] = None): HoverVar = HoverVar(initial)

  /** Chart container (`ChartContainer` equivalent). `aspectRatio = Some("video")` adds `aspect-video`; omit when a
    * fixed height class is supplied (most preview-02 cards use `h-[…] w-full`).
    */
  def apply(mods: Modifier[HtmlElement]*): HtmlElement = apply(None, mods*)

  def apply(aspectRatio: Option[String], mods: Modifier[HtmlElement]*): HtmlElement =
    val aspectCls = aspectRatio.fold("")(r => s" aspect-$r")
    div(
      role := "img",
      dataAttr("slot") := "chart",
      cls := s"chart relative flex w-full justify-center overflow-visible text-xs$aspectCls",
      mods
    )

  /** Bar chart options — pass as a mod: `Chart.bar(data, Chart.BarStyle(color = Chart.color(2)), …)`. */
  final case class BarStyle(
      color: String = "var(--chart-2)",
      showLabels: Boolean = true,
      labelFormat: String => String = identity,
      padding: Double = 0.25
  )

  /** Area chart options. */
  final case class AreaStyle(
      color: String = "var(--chart-1)",
      showLabels: Boolean = true,
      labelFormat: String => String = identity,
      fillOpacity: Double = 0.25
  )

  /** Donut chart options. */
  final case class DonutStyle(
      colors: List[String] = List("var(--chart-2)", "var(--chart-1)"),
      innerRadiusRatio: Double = 0.8,
      padding: Double = 28.0
  )

  def color(index: Int): String = s"var(--chart-${index.max(1).min(5)})"

  /** Vertical bars with baseline and optional x-axis labels. */
  def bar(series: List[(String, Double)], mods: (Modifier[SvgElement] | HoverVar | BarStyle)*): SvgElement =
    val (hover, style, svgMods) = parseMods(mods*)
    renderBar(series, hover, style, svgMods)

  /** Smoothed area + stroke with gradient fill under the curve. */
  def area(series: List[(String, Double)], mods: (Modifier[SvgElement] | HoverVar | AreaStyle)*): SvgElement =
    val (hover, style, svgMods) = parseAreaMods(mods*)
    renderArea(series, hover, style, svgMods)

  /** Donut / pie arcs. */
  def donut(slices: List[(String, Double)], mods: (Modifier[SvgElement] | HoverVar | DonutStyle)*): SvgElement =
    val (hover, style, svgMods) = parseDonutMods(mods*)
    renderDonut(slices, hover, style, svgMods)

  /** Hover tooltip (label + colored swatch + value) driven by a `HoverVar` or its signal. */
  def tooltip(
      hover: HoverVar | Signal[Option[Point]],
      seriesLabel: String = "",
      hideLabel: Boolean = false,
      indicator: TooltipIndicator = TooltipIndicator.Dot,
      mods: Modifier[HtmlElement]*
  ): HtmlElement =
    val hoverSignal = hover match
      case h: HoverVar  => h.signal
      case s: Signal[?] => s.asInstanceOf[Signal[Option[Point]]]

    val cursorVar = Var((0.0, 0.0))
    div(
      cls := "pointer-events-none fixed z-50 grid min-w-[9rem] -translate-x-1/2 translate-y-2 items-start gap-1.5 rounded-lg border border-border/50 bg-background px-2.5 py-1.5 text-xs shadow-xl transition-[left,top,opacity] duration-150 ease-out",
      display <-- hoverSignal.map(_.fold("none")(_ => "grid")),
      styleAttr <-- cursorVar.signal.map { case (x, y) => s"left:${x}px;top:${y}px" },
      onMountBind { _ =>
        documentEvents(_.onMouseMove) --> { (ev: dom.MouseEvent) =>
          cursorVar.set((ev.clientX, ev.clientY))
        }
      },
      child <-- hoverSignal.map {
        case None => emptyNode
        case Some(point) =>
          div(
            if !hideLabel then div(cls := "font-medium", point.label) else emptyMod,
            div(
              cls := "grid gap-1.5",
              div(
                cls := indicatorRowClass(indicator),
                indicatorSwatch(indicator, point.color),
                div(
                  cls := "flex flex-1 shrink-0 items-center justify-between leading-none",
                  span(cls := "text-muted-foreground", if seriesLabel.nonEmpty then seriesLabel else point.label),
                  span(cls := "font-mono font-medium text-foreground tabular-nums", formatAmount(point.amount))
                )
              )
            )
          )
      },
      mods
    )

  private var nextGradientId = 0

  private def nextId(prefix: String): String =
    nextGradientId += 1
    s"cn-chart-$prefix-$nextGradientId"

  private def parseMods(
      mods: (Modifier[SvgElement] | HoverVar | BarStyle)*
  ): (HoverVar, BarStyle, List[Modifier[SvgElement]]) =
    val hover = mods.collectFirst { case h: HoverVar => h }.getOrElse(hoverVar())
    val style = mods.collectFirst { case s: BarStyle => s }.getOrElse(BarStyle())
    val svgMods = mods.collect { case m: Modifier[?] => m.asInstanceOf[Modifier[SvgElement]] }.toList
    (hover, style, svgMods)

  private def parseAreaMods(
      mods: (Modifier[SvgElement] | HoverVar | AreaStyle)*
  ): (HoverVar, AreaStyle, List[Modifier[SvgElement]]) =
    val hover = mods.collectFirst { case h: HoverVar => h }.getOrElse(hoverVar())
    val style = mods.collectFirst { case s: AreaStyle => s }.getOrElse(AreaStyle())
    val svgMods = mods.collect { case m: Modifier[?] => m.asInstanceOf[Modifier[SvgElement]] }.toList
    (hover, style, svgMods)

  private def parseDonutMods(
      mods: (Modifier[SvgElement] | HoverVar | DonutStyle)*
  ): (HoverVar, DonutStyle, List[Modifier[SvgElement]]) =
    val hover = mods.collectFirst { case h: HoverVar => h }.getOrElse(hoverVar())
    val style = mods.collectFirst { case s: DonutStyle => s }.getOrElse(DonutStyle())
    val svgMods = mods.collect { case m: Modifier[?] => m.asInstanceOf[Modifier[SvgElement]] }.toList
    (hover, style, svgMods)

  private def renderBar(
      series: List[(String, Double)],
      hover: HoverVar,
      style: BarStyle,
      svgMods: List[Modifier[SvgElement]]
  ): SvgElement =
    if series.isEmpty then emptySvg(svgMods)
    else
      val width = 400.0
      val height = 200.0
      val marginTop = 12.0
      val marginBottom = if style.showLabels then 28.0 else 8.0
      val marginX = 8.0
      val plotW = width - marginX * 2
      val plotH = height - marginTop - marginBottom
      val maxVal = series.map(_._2).maxOption.filter(_ > 0).getOrElse(1.0)
      val n = series.length
      val step = plotW / n
      val barW = step * (1.0 - style.padding)
      val baselineY = marginTop + plotH
      val rx = math.min(barW / 2.0, 4.0)

      svgTag(
        svg.viewBox := s"0 0 $width $height",
        svg.cls := "size-full",
        svg.fill := "none",
        onMouseMove --> { (ev: dom.MouseEvent) =>
          val rect = ev.currentTarget.asInstanceOf[dom.svg.SVG].getBoundingClientRect()
          val relX = (ev.clientX - rect.left) / rect.width * width
          val idx = ((relX - marginX) / step).floor.toInt.max(0).min(n - 1)
          val (label, amount) = series(idx)
          hover.hoverVar.set(Some(Point(label, amount, style.color)))
        },
        onMouseLeave --> { _ => hover.hoverVar.set(None) },
        svgG(
          svgRect(
            svg.x := marginX.toString,
            svg.y := baselineY.toString,
            svg.width := plotW.toString,
            svg.height := "1",
            svg.fill := "var(--border)"
          )
        ),
        series.zipWithIndex.map { case ((label, amount), i) =>
          val barH = (amount / maxVal) * plotH
          val x = marginX + i * step + (step - barW) / 2.0
          val y = baselineY - barH
          svgG(
            svgRect(
              svg.x := x.toString,
              svg.y := y.toString,
              svg.width := barW.toString,
              svg.height := barH.max(0).toString,
              svg.rx := rx.toString,
              svg.fill := style.color
            ),
            if style.showLabels then
              svgText(
                svg.x := (x + barW / 2.0).toString,
                svg.y := (baselineY + 16).toString,
                svg.textAnchor := "middle",
                svg.cls := "fill-muted-foreground text-[10px]",
                style.labelFormat(label)
              )
            else emptyMod
          )
        },
        svgMods
      )

  private def renderArea(
      series: List[(String, Double)],
      hover: HoverVar,
      style: AreaStyle,
      svgMods: List[Modifier[SvgElement]]
  ): SvgElement =
    if series.isEmpty then emptySvg(svgMods)
    else
      val width = 400.0
      val height = 200.0
      val marginTop = 12.0
      val marginBottom = if style.showLabels then 28.0 else 8.0
      val marginX = 8.0
      val plotW = width - marginX * 2
      val plotH = height - marginTop - marginBottom
      val maxVal = series.map(_._2).maxOption.filter(_ > 0).getOrElse(1.0)
      val minVal = series.map(_._2).minOption.getOrElse(0.0)
      val valRange = (maxVal - minVal).max(1.0)
      val n = series.length
      val step = if n <= 1 then plotW else plotW / (n - 1)

      def xAt(i: Int): Double = marginX + i * step
      def yAt(v: Double): Double = marginTop + plotH - ((v - minVal) / valRange) * plotH
      def point(i: Int): (Double, Double) = (xAt(i), yAt(series(i)._2))

      val pts = series.indices.map(point).toList
      val linePath = naturalPath(pts)
      val areaPath = s"$linePath L ${xAt(n - 1)} ${marginTop + plotH} L ${xAt(0)} ${marginTop + plotH} Z"
      val gradientId = nextId("area")

      svgTag(
        svg.viewBox := s"0 0 $width $height",
        svg.cls := "size-full",
        svg.fill := "none",
        onMouseMove --> { (ev: dom.MouseEvent) =>
          val rect = ev.currentTarget.asInstanceOf[dom.svg.SVG].getBoundingClientRect()
          val relX = (ev.clientX - rect.left) / rect.width * width
          val idx =
            if n <= 1 then 0
            else ((relX - marginX) / step).round.toInt.max(0).min(n - 1)
          val (label, amount) = series(idx)
          hover.hoverVar.set(Some(Point(label, amount, style.color)))
        },
        onMouseLeave --> { _ => hover.hoverVar.set(None) },
        svgDefs(
          svgLinearGradient(
            svg.idAttr := gradientId,
            svg.x1 := "0",
            svg.y1 := "0",
            svg.x2 := "0",
            svg.y2 := "1",
            svgStop(
              svg.offsetAttr := "0%",
              svg.stopColor := style.color,
              svg.stopOpacity := style.fillOpacity.toString
            ),
            svgStop(svg.offsetAttr := "100%", svg.stopColor := style.color, svg.stopOpacity := "0")
          )
        ),
        svgPath(svg.d := areaPath, svg.fill := s"url(#$gradientId)"),
        svgPath(
          svg.d := linePath,
          svg.fill := "none",
          svg.stroke := style.color,
          svg.strokeWidth := "2"
        ),
        if style.showLabels then
          series.zipWithIndex.map { case ((label, _), i) =>
            svgText(
              svg.x := xAt(i).toString,
              svg.y := (marginTop + plotH + 16).toString,
              svg.textAnchor := "middle",
              svg.cls := "fill-muted-foreground text-[10px]",
              style.labelFormat(label)
            )
          }
        else emptyMod,
        svgMods
      )

  private def renderDonut(
      slices: List[(String, Double)],
      hover: HoverVar,
      style: DonutStyle,
      svgMods: List[Modifier[SvgElement]]
  ): SvgElement =
    if slices.isEmpty then emptySvg(svgMods)
    else
      val size = 200.0
      val pad = style.padding
      val cx = size / 2.0
      val cy = size / 2.0
      val outerR = (size / 2.0) - pad
      val innerR = outerR * style.innerRadiusRatio
      val total = slices.map(_._2).sum.max(0.0001)
      var startAngle = -math.Pi / 2.0

      val arcData = slices.zipWithIndex.map { case ((label, amount), i) =>
        val sweep = (amount / total) * 2.0 * math.Pi
        val endAngle = startAngle + sweep
        val sliceColor = style.colors.lift(i).getOrElse(color((i % 5) + 1))
        val entry = (label, amount, sliceColor, startAngle, endAngle)
        startAngle = endAngle
        entry
      }

      svgTag(
        svg.viewBox := s"0 0 $size $size",
        svg.cls := "size-full",
        svg.fill := "none",
        onMouseMove --> { (ev: dom.MouseEvent) =>
          val rect = ev.currentTarget.asInstanceOf[dom.svg.SVG].getBoundingClientRect()
          val relX = (ev.clientX - rect.left) / rect.width * size - cx
          val relY = (ev.clientY - rect.top) / rect.height * size - cy
          val dist = math.hypot(relX, relY)
          if dist >= innerR && dist <= outerR then
            var angle = math.atan2(relY, relX)
            if angle < -math.Pi / 2.0 then angle += 2.0 * math.Pi
            arcData.find { case (_, _, _, start, end) => angle >= start - 0.001 && angle < end - 0.001 }.foreach {
              case (label, amount, sliceColor, _, _) =>
                hover.hoverVar.set(Some(Point(label, amount, sliceColor)))
            }
          else hover.hoverVar.set(None)
        },
        onMouseLeave --> { _ => hover.hoverVar.set(None) },
        arcData.map { case (_, _, sliceColor, start, end) =>
          svgPath(svg.d := arcPath(cx, cy, outerR, innerR, start, end), svg.fill := sliceColor)
        },
        svgMods
      )

  private def emptySvg(svgMods: List[Modifier[SvgElement]]): SvgElement =
    svgTag(svg.viewBox := "0 0 400 200", svg.cls := "size-full", svg.fill := "none", svgMods)

  /** Cardinal-spline → cubic-bezier path approximating d3 `curveNatural`. */
  private def naturalPath(points: List[(Double, Double)]): String =
    if points.isEmpty then ""
    else if points.length == 1 then s"M ${points.head._1} ${points.head._2}"
    else
      val head = s"M ${points.head._1} ${points.head._2}"
      val segments = points.indices.init.map { i =>
        val p0 = points(math.max(i - 1, 0))
        val p1 = points(i)
        val p2 = points(i + 1)
        val p3 = points(math.min(i + 2, points.length - 1))
        val cp1x = p1._1 + (p2._1 - p0._1) / 6.0
        val cp1y = p1._2 + (p2._2 - p0._2) / 6.0
        val cp2x = p2._1 - (p3._1 - p1._1) / 6.0
        val cp2y = p2._2 - (p3._2 - p1._2) / 6.0
        f" C $cp1x%.2f $cp1y%.2f $cp2x%.2f $cp2y%.2f ${p2._1}%.2f ${p2._2}%.2f"
      }
      head + segments.mkString

  private def arcPath(
      cx: Double,
      cy: Double,
      outerR: Double,
      innerR: Double,
      start: Double,
      end: Double
  ): String =
    val x1 = cx + outerR * math.cos(start)
    val y1 = cy + outerR * math.sin(start)
    val x2 = cx + outerR * math.cos(end)
    val y2 = cy + outerR * math.sin(end)
    val x3 = cx + innerR * math.cos(end)
    val y3 = cy + innerR * math.sin(end)
    val x4 = cx + innerR * math.cos(start)
    val y4 = cy + innerR * math.sin(start)
    val largeArc = if end - start > math.Pi then 1 else 0
    f"M $x1%.3f $y1%.3f A $outerR%.3f $outerR%.3f 0 $largeArc 1 $x2%.3f $y2%.3f L $x3%.3f $y3%.3f A $innerR%.3f $innerR%.3f 0 $largeArc 0 $x4%.3f $y4%.3f Z"

  private def indicatorRowClass(indicator: TooltipIndicator): String =
    indicator match
      case TooltipIndicator.Dot    => "flex w-full flex-wrap items-center gap-2"
      case TooltipIndicator.Line   => "flex w-full flex-wrap items-stretch gap-2"
      case TooltipIndicator.Dashed => "flex w-full flex-wrap items-stretch gap-2"

  private def indicatorSwatch(indicator: TooltipIndicator, swatchColor: String): HtmlElement =
    val style = s"--color-bg:$swatchColor;--color-border:$swatchColor"
    indicator match
      case TooltipIndicator.Dot =>
        div(styleAttr := style, cls := "size-2.5 shrink-0 rounded-[2px] border border-(--color-border) bg-(--color-bg)")
      case TooltipIndicator.Line =>
        div(
          styleAttr := style,
          cls := "h-full w-1 shrink-0 rounded-[2px] border border-(--color-border) bg-(--color-bg)"
        )
      case TooltipIndicator.Dashed =>
        div(
          styleAttr := style,
          cls := "my-0.5 w-0 shrink-0 border-[1.5px] border-dashed border-(--color-border) bg-transparent"
        )

  private def formatAmount(amount: Double): String =
    if amount.isWhole then amount.toLong.toString else f"$amount%.2f"
