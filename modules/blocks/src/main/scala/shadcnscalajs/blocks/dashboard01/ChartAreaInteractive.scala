package shadcnscalajs.blocks.dashboard01

import com.raquo.laminar.api.L.*
import shadcnscalajs.ui.*

/** Port of shadcn-svelte `blocks/dashboard-01/components/chart-area-interactive.svelte` — the "Total Visitors" card
  * with a 90-day / 30-day / 7-day range control.
  *
  * ## Chart API limitation (task-7 brief, elaborated in `task-7-report.md`) Upstream renders `desktop` and `mobile` as
  * ONE stacked `AreaChart` series (layerchart `seriesLayout="stack"`), with a single tooltip that lists both values for
  * the hovered date. `shadcnscalajs.ui.Chart.area` draws exactly one series per call: (a) each call independently
  * normalizes its own min/max to the plot height — there's no shared zero baseline to stack a second series onto — and
  * (b) a `HoverVar`'s state can only be mutated from inside `Chart.scala`'s own private mouse handlers, wired to real
  * browser events on that call's own `<svg>`. Neither is reachable or overridable from this file, so a true stacked
  * chart with one combined two-value tooltip isn't something this component genuinely supports without extending
  * `Chart.scala`, which is out of scope for this task.
  *
  * `Chart.stackedArea` shares one y-scale across both series and stacks `desktop` above `mobile`, matching upstream's
  * geometry. Its hover state still carries one value, so the tooltip reports desktop only rather than upstream's
  * combined two-value tooltip. X-axis labels are thinned to weekly intervals.
  */
object ChartAreaInteractive:

  private final case class DataPoint(label: String, desktop: Double, mobile: Double)

  // Verbatim daily series from upstream `chartData` (2024-04-01 .. 2024-06-30), pre-formatted as short "Mon D"
  // labels: `Chart.tooltip` renders `Point.label` as-is with no date formatter of its own, so the display format has
  // to live in the data rather than in a `labelFormat` hook.
  private val chartData: List[DataPoint] = List(
    DataPoint("Apr 1", 222, 150),
    DataPoint("Apr 2", 97, 180),
    DataPoint("Apr 3", 167, 120),
    DataPoint("Apr 4", 242, 260),
    DataPoint("Apr 5", 373, 290),
    DataPoint("Apr 6", 301, 340),
    DataPoint("Apr 7", 245, 180),
    DataPoint("Apr 8", 409, 320),
    DataPoint("Apr 9", 59, 110),
    DataPoint("Apr 10", 261, 190),
    DataPoint("Apr 11", 327, 350),
    DataPoint("Apr 12", 292, 210),
    DataPoint("Apr 13", 342, 380),
    DataPoint("Apr 14", 137, 220),
    DataPoint("Apr 15", 120, 170),
    DataPoint("Apr 16", 138, 190),
    DataPoint("Apr 17", 446, 360),
    DataPoint("Apr 18", 364, 410),
    DataPoint("Apr 19", 243, 180),
    DataPoint("Apr 20", 89, 150),
    DataPoint("Apr 21", 137, 200),
    DataPoint("Apr 22", 224, 170),
    DataPoint("Apr 23", 138, 230),
    DataPoint("Apr 24", 387, 290),
    DataPoint("Apr 25", 215, 250),
    DataPoint("Apr 26", 75, 130),
    DataPoint("Apr 27", 383, 420),
    DataPoint("Apr 28", 122, 180),
    DataPoint("Apr 29", 315, 240),
    DataPoint("Apr 30", 454, 380),
    DataPoint("May 1", 165, 220),
    DataPoint("May 2", 293, 310),
    DataPoint("May 3", 247, 190),
    DataPoint("May 4", 385, 420),
    DataPoint("May 5", 481, 390),
    DataPoint("May 6", 498, 520),
    DataPoint("May 7", 388, 300),
    DataPoint("May 8", 149, 210),
    DataPoint("May 9", 227, 180),
    DataPoint("May 10", 293, 330),
    DataPoint("May 11", 335, 270),
    DataPoint("May 12", 197, 240),
    DataPoint("May 13", 197, 160),
    DataPoint("May 14", 448, 490),
    DataPoint("May 15", 473, 380),
    DataPoint("May 16", 338, 400),
    DataPoint("May 17", 499, 420),
    DataPoint("May 18", 315, 350),
    DataPoint("May 19", 235, 180),
    DataPoint("May 20", 177, 230),
    DataPoint("May 21", 82, 140),
    DataPoint("May 22", 81, 120),
    DataPoint("May 23", 252, 290),
    DataPoint("May 24", 294, 220),
    DataPoint("May 25", 201, 250),
    DataPoint("May 26", 213, 170),
    DataPoint("May 27", 420, 460),
    DataPoint("May 28", 233, 190),
    DataPoint("May 29", 78, 130),
    DataPoint("May 30", 340, 280),
    DataPoint("May 31", 178, 230),
    DataPoint("Jun 1", 178, 200),
    DataPoint("Jun 2", 470, 410),
    DataPoint("Jun 3", 103, 160),
    DataPoint("Jun 4", 439, 380),
    DataPoint("Jun 5", 88, 140),
    DataPoint("Jun 6", 294, 250),
    DataPoint("Jun 7", 323, 370),
    DataPoint("Jun 8", 385, 320),
    DataPoint("Jun 9", 438, 480),
    DataPoint("Jun 10", 155, 200),
    DataPoint("Jun 11", 92, 150),
    DataPoint("Jun 12", 492, 420),
    DataPoint("Jun 13", 81, 130),
    DataPoint("Jun 14", 426, 380),
    DataPoint("Jun 15", 307, 350),
    DataPoint("Jun 16", 371, 310),
    DataPoint("Jun 17", 475, 520),
    DataPoint("Jun 18", 107, 170),
    DataPoint("Jun 19", 341, 290),
    DataPoint("Jun 20", 408, 450),
    DataPoint("Jun 21", 169, 210),
    DataPoint("Jun 22", 317, 270),
    DataPoint("Jun 23", 480, 530),
    DataPoint("Jun 24", 132, 180),
    DataPoint("Jun 25", 141, 190),
    DataPoint("Jun 26", 434, 380),
    DataPoint("Jun 27", 448, 490),
    DataPoint("Jun 28", 149, 200),
    DataPoint("Jun 29", 103, 160),
    DataPoint("Jun 30", 446, 400)
  )

  private val rangeOptions: List[(String, String)] =
    List("90d" -> "Last 3 months", "30d" -> "Last 30 days", "7d" -> "Last 7 days")

  private def rangeDays(range: String): Int = range match
    case "30d" => 30
    case "7d"  => 7
    case _     => 90

  // Upstream filters against a hardcoded `referenceDate = new Date("2024-06-30")` rather than "today" (the sample
  // data ends on that date) — since `chartData` is one entry per consecutive calendar day ending there, "last N
  // calendar days" is equivalent to "last N list entries" (`N + 1` to make the boundary day inclusive, matching
  // upstream's `item.date >= referenceDate` filter).
  private def filtered(range: String): List[DataPoint] =
    chartData.takeRight(rangeDays(range) + 1)

  private def legendSwatch(color: String, text: String): HtmlElement =
    span(
      cls := "flex items-center gap-1.5",
      span(styleAttr := s"background-color:$color", cls := "size-2 shrink-0 rounded-[2px]"),
      text
    )

  def apply(): HtmlElement =
    val rangeVar = Var("90d")
    // `ToggleGroup.single` is bound to `Var[Option[String]]` (radio-like: re-clicking clears the selection);
    // `Select.stateful` binds to a plain `Var[String]`. Both controls drive the same range, so `toggleVar` mirrors
    // `rangeVar` in both directions (each guarded so a mirrored write doesn't bounce back) rather than picking one
    // control to own the other's `Var` directly.
    val toggleVar = Var(Option("90d"))
    val hover = Chart.hoverVar()

    Card(
      cls := "@container/card",
      onMountBind { _ =>
        rangeVar.signal --> { v => if toggleVar.now() != Some(v) then toggleVar.set(Some(v)) }
      },
      onMountBind { _ =>
        toggleVar.signal --> { opt => opt.foreach(v => if rangeVar.now() != v then rangeVar.set(v)) }
      },
      Card.header(
        Card.title("Total Visitors"),
        Card.description(
          span(cls := "hidden @[540px]/card:block", " Total for the last 3 months "),
          span(cls := "@[540px]/card:hidden", "Last 3 months")
        ),
        Card.action(
          ToggleGroup.single(
            toggleVar,
            Toggle.Variant.Outline,
            Toggle.Size.Default,
            0,
            ToggleGroup.Orientation.Horizontal,
            Seq(cls := "hidden *:data-[slot=toggle-group-item]:!px-4 @[767px]/card:flex"),
            rangeOptions.map((optValue, optLabel) => ToggleGroup.Item(optValue, optLabel))*
          ),
          // `Select.stateful` (unlike the full `Select.apply`) threads its `mods` onto the trigger button itself,
          // matching where upstream puts `w-40`/visibility classes — at the cost of always using `Select.Size.Default`
          // (upstream's trigger is `size="sm"`); `Select.apply` would give us the size back but hardcodes
          // `triggerMods = Nil`, so there's no single entry point offering both. Documented as a minor deviation.
          Select.stateful(
            rangeVar,
            rangeOptions,
            cls := "flex w-40 **:data-[slot=select-value]:block **:data-[slot=select-value]:truncate @[767px]/card:hidden",
            aria.label := "Select a value"
          )
        )
      ),
      Card.content(
        cls := "px-2 pt-4 sm:px-6 sm:pt-6",
        Chart(
          cls := "aspect-auto h-[250px] w-full",
          child <-- rangeVar.signal.map { range =>
            val points = filtered(range)
            Chart.stackedArea(
              points.map(p => (p.label, p.mobile, p.desktop)),
              hover,
              Chart.StackedAreaStyle(
                lowerColor = Chart.color(2),
                upperColor = Chart.color(1),
                showLabels = true,
                labelEvery = 7,
                fillOpacity = 0.4
              )
            )
          },
          Chart.tooltip(hover, seriesLabel = "Desktop", indicator = Chart.TooltipIndicator.Line)
        ),
        div(
          cls := "mt-2 flex items-center justify-center gap-4 text-xs text-muted-foreground",
          legendSwatch(Chart.color(1), "Desktop"),
          legendSwatch(Chart.color(2), "Mobile")
        )
      )
    )
