package shadcnscalajs.ui

import com.raquo.laminar.api.L.*
import org.scalajs.dom

/** shadcn/ui Slider — custom track/range/thumb markup matching upstream `slider.svelte`, with controlled single-value
  * and multi-value (list) forms for preview-02 cards.
  */
object Slider:

  private val rootClasses =
    "cn-slider relative flex w-full touch-none items-center select-none data-disabled:opacity-50 data-vertical:h-full data-vertical:w-auto data-vertical:flex-col"

  private val trackClasses =
    "cn-slider-track relative grow overflow-hidden bg-muted data-horizontal:w-full data-vertical:h-full"

  private val rangeClasses =
    "cn-slider-range absolute select-none data-horizontal:h-full data-vertical:w-full"

  private val thumbClasses =
    "cn-slider-thumb block shrink-0 select-none disabled:pointer-events-none disabled:opacity-50"

  /** Uncontrolled native range — kept for `/components/slider` preview (`Slider(value := "50", …)`). */
  def apply(mods: Modifier[HtmlElement]*): HtmlElement = Range(mods*)

  def single(
      valueVar: Var[Double],
      min: Double = 0,
      max: Double = 100,
      step: Double = 1,
      mods: Modifier[HtmlElement]*
  ): HtmlElement =
    controlled(
      () => List(valueVar.now()),
      valueVar.signal.map(v => List(clamp(v, min, max, step))),
      list => valueVar.set(list.headOption.map(clamp(_, min, max, step)).getOrElse(min)),
      min,
      max,
      step,
      mods*
    )

  def multiple(
      valuesVar: Var[List[Double]],
      min: Double = 0,
      max: Double = 100,
      step: Double = 1,
      mods: Modifier[HtmlElement]*
  ): HtmlElement =
    controlled(
      () => normalizeValues(valuesVar.now(), min, max, step),
      valuesVar.signal.map(normalizeValues(_, min, max, step)),
      list => valuesVar.set(normalizeValues(list, min, max, step)),
      min,
      max,
      step,
      mods*
    )

  private def clamp(v: Double, min: Double, max: Double, step: Double): Double =
    val bounded = math.max(min, math.min(max, v))
    if step <= 0 then bounded
    else
      val steps = math.round((bounded - min) / step)
      math.min(max, min + steps * step)

  private def normalizeValues(values: List[Double], min: Double, max: Double, step: Double): List[Double] =
    if values.isEmpty then List(clamp((min + max) / 2, min, max, step))
    else values.map(clamp(_, min, max, step))

  private def pct(v: Double, min: Double, max: Double): Double =
    if max <= min then 0 else ((v - min) / (max - min)) * 100

  private def valueFromPct(pct: Double, min: Double, max: Double, step: Double): Double =
    clamp(min + (math.max(0, math.min(100, pct)) / 100) * (max - min), min, max, step)

  private def rangeStyle(values: List[Double], min: Double, max: Double): String =
    if values.isEmpty then "left:0;width:0%"
    else if values.length == 1 then s"left:0;width:${pct(values.head, min, max)}%"
    else
      val lo = values.min
      val hi = values.max
      s"left:${pct(lo, min, max)}%;width:${pct(hi, min, max) - pct(lo, min, max)}%"

  private def controlled(
      readValues: () => List[Double],
      valuesSignal: Signal[List[Double]],
      writeValues: List[Double] => Unit,
      min: Double,
      max: Double,
      step: Double,
      mods: Modifier[HtmlElement]*
  ): HtmlElement =
    val draggingVar = Var(false)
    val activeIndexVar = Var(0)
    val trackRef = Var(Option.empty[dom.html.Element])

    def setIndexValue(index: Int, raw: Double): Unit =
      val current = readValues()
      val clamped = clamp(raw, min, max, step)
      val next =
        if current.isEmpty then List(clamped)
        else current.updated(index, clamped)
      writeValues(next)

    def updateFromClientX(clientX: Double): Unit =
      trackRef.now().foreach { track =>
        val rect = track.getBoundingClientRect()
        val p = ((clientX - rect.left) / rect.width) * 100
        setIndexValue(activeIndexVar.now(), valueFromPct(p, min, max, step))
      }

    div(
      dataAttr("slot") := "slider",
      dataAttr("orientation") := "horizontal",
      cls := rootClasses,
      onMountBind { _ =>
        documentEvents(_.onMouseMove) --> { (ev: dom.MouseEvent) =>
          if draggingVar.now() then updateFromClientX(ev.clientX)
        }
      },
      onMountBind { _ =>
        documentEvents(_.onMouseUp) --> { _ => draggingVar.set(false) }
      },
      span(
        dataAttr("slot") := "slider-track",
        dataAttr("orientation") := "horizontal",
        // `data-horizontal:` compiles to `&[data-horizontal]`, not `[data-orientation=horizontal]`.
        // The track's height and the range's fill come only from that variant — in the base classes
        // here and in every style pack — so without this flag both collapse to zero height.
        dataAttr("horizontal") := "",
        cls := trackClasses,
        onMountCallback { ctx => trackRef.set(Some(ctx.thisNode.ref)) },
        onMouseDown --> { ev =>
          activeIndexVar.set(0)
          updateFromClientX(ev.clientX)
          draggingVar.set(true)
        },
        span(
          dataAttr("slot") := "slider-range",
          dataAttr("horizontal") := "",
          cls := rangeClasses,
          styleAttr <-- valuesSignal.map(rangeStyle(_, min, max))
        )
      ),
      children <-- valuesSignal.map { values =>
        val vs = if values.isEmpty then List(min) else values
        vs.zipWithIndex.map { case (v, idx) =>
          span(
            dataAttr("slot") := "slider-thumb",
            cls := thumbClasses,
            role := "slider",
            aria.valueMin := min,
            aria.valueMax := max,
            aria.valueNow := v,
            tabIndex := 0,
            // Positioned inline rather than with `absolute top-1/2` utilities: packs set
            // `.cn-slider-thumb { position: relative }` to anchor their enlarged `::after` hit area,
            // and being unlayered they beat the utility class, dropping the thumb into flex flow
            // beside the track. An inline style outranks them and keeps the pack's hit area working.
            styleAttr := s"position:absolute;top:50%;left:${pct(v, min, max)}%;transform:translate(-50%, -50%)",
            onMouseDown --> { ev =>
              ev.preventDefault()
              ev.stopPropagation()
              activeIndexVar.set(idx)
              draggingVar.set(true)
            }
          )
        }
      },
      mods
    )
