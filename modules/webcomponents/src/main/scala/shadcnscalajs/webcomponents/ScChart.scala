package shadcnscalajs.webcomponents

import com.raquo.laminar.api.L.*
import shadcnscalajs.ui.Chart

import scala.scalajs.js

/** `<sc-chart type="bar" data='[["Jan",40],["Feb",60]]'></sc-chart>` */
class ScChart extends ScElementBase:

  private val typeVar = Var("bar")
  private val dataVar = Var(List.empty[(String, Double)])
  private val tooltipLabelVar = Var("")
  private val showLabelsVar = Var(false)
  private val innerRadiusVar = Var(0.8)
  private val revision = Var(0)

  private def bump(): Unit = revision.update(_ + 1)

  observeAttribute("type")(v => { typeVar.set(v.getOrElse("bar")); bump() })
  observeAttribute("data")(v => { dataVar.set(parseData(v.orNull)); bump() })
  observeAttribute("tooltip-label")(v => tooltipLabelVar.set(v.getOrElse("")))
  observeAttribute("show-labels")(v => { showLabelsVar.set(v.isDefined); bump() })
  observeAttribute("inner-radius")(v => {
    v.flatMap(_.toDoubleOption).foreach(innerRadiusVar.set)
    bump()
  })
  stringProperty("type")
  jsonProperty("data")(v => { dataVar.set(parseData(v)); bump() })
  stringProperty("tooltip-label")
  booleanProperty("show-labels")
  stringProperty("inner-radius")

  mount(ScChart.view(typeVar, dataVar, tooltipLabelVar, showLabelsVar, innerRadiusVar, revision))

  private def parseData(value: js.Any): List[(String, Double)] =
    ScElements
      .toArray(value)
      .map(_.toList.flatMap { raw =>
        if js.Array.isArray(raw) then
          val arr = raw.asInstanceOf[js.Array[js.Dynamic]]
          if arr.length >= 2 then
            val label = "" + arr(0)
            val amount =
              if js.typeOf(arr(1)) == "number" then arr(1).asInstanceOf[Double]
              else ("" + arr(1)).toDoubleOption.getOrElse(0.0)
            Some(label -> amount)
          else None
        else
          val label = raw.label.asInstanceOf[js.UndefOr[String]].getOrElse("" + raw)
          val amount = raw.amount
            .asInstanceOf[js.UndefOr[Double]]
            .orElse(raw.value.asInstanceOf[js.UndefOr[Double]])
            .getOrElse(0.0)
          Some(label -> amount)
      })
      .getOrElse(Nil)

object ScChart:
  def register(): Unit =
    ScElements.define(
      "sc-chart",
      js.constructorOf[ScChart],
      "type",
      "data",
      "tooltip-label",
      "show-labels",
      "inner-radius"
    )

  private def view(
      typeVar: Var[String],
      dataVar: Var[List[(String, Double)]],
      tooltipLabelVar: Var[String],
      showLabelsVar: Var[Boolean],
      innerRadiusVar: Var[Double],
      revision: Var[Int]
  ): HtmlElement =
    val hover = Chart.hoverVar()
    div(
      cls := "relative h-full w-full min-h-[120px]",
      child <-- revision.signal.map { _ =>
        val kind = typeVar.now()
        val data = dataVar.now()
        val showLabels = showLabelsVar.now()
        val innerRadius = innerRadiusVar.now()
        kind match
          case "area" =>
            Chart.area(data, hover, Chart.AreaStyle(showLabels = showLabels), svg.cls := "h-full w-full")
          case "donut" =>
            Chart.donut(
              data,
              hover,
              Chart.DonutStyle(innerRadiusRatio = innerRadius),
              svg.cls := "h-full w-full"
            )
          case _ =>
            Chart.bar(data, hover, Chart.BarStyle(showLabels = showLabels), svg.cls := "h-full w-full")
      },
      child.maybe <-- tooltipLabelVar.signal.map { label =>
        Option.when(label.nonEmpty)(Chart.tooltip(hover, seriesLabel = label))
      }
    )
