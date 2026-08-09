package shadcnscalajs.blocks.dashboard01

import com.raquo.laminar.api.L.*
import shadcnscalajs.ui.*

/** Port of shadcn-svelte `blocks/dashboard-01/components/section-cards.svelte` — the four KPI cards at the top of the
  * dashboard.
  */
object SectionCards:

  private final case class Metric(
      description: String,
      amount: String,
      trendUp: Boolean,
      badgeText: String,
      footerHeadline: String,
      footerDetail: String
  )

  private val metrics: List[Metric] = List(
    Metric(
      "Total Revenue",
      "$1,250.00",
      trendUp = true,
      "+12.5%",
      "Trending up this month",
      "Visitors for the last 6 months"
    ),
    Metric("New Customers", "1,234", trendUp = false, "-20%", "Down 20% this period", "Acquisition needs attention"),
    Metric("Active Accounts", "45,678", trendUp = true, "+12.5%", "Strong user retention", "Engagement exceed targets"),
    Metric("Growth Rate", "4.5%", trendUp = true, "+4.5%", "Steady performance increase", "Meets growth projections")
  )

  // Upstream's `TrendingDownIcon` has no equivalent concept registered across all five `Icons` libraries; a
  // rotated `trendingUp` is visually indistinguishable from a dedicated down-trend glyph.
  private def trendIcon(trendUp: Boolean, mods: Modifier[SvgElement]*): SvgElement =
    if trendUp then Icons.trendingUp(mods*) else Icons.trendingUp(svg.cls := "rotate-180", mods)

  private def metricCard(metric: Metric): HtmlElement =
    Card(
      cls := "@container/card",
      Card.header(
        Card.description(metric.description),
        Card.title(cls := "text-2xl font-semibold tabular-nums @[250px]/card:text-3xl", metric.amount),
        Card.action(
          Badge.of(
            _.variant(Badge.Variant.Outline),
            _ => trendIcon(metric.trendUp),
            _ => metric.badgeText
          )
        )
      ),
      Card.footer(
        cls := "flex-col items-start gap-1.5 text-sm",
        div(
          cls := "line-clamp-1 flex gap-2 font-medium",
          metric.footerHeadline,
          trendIcon(metric.trendUp, svg.cls := "size-4")
        ),
        div(cls := "text-muted-foreground", metric.footerDetail)
      )
    )

  def apply(): HtmlElement =
    div(
      cls := "grid grid-cols-1 gap-4 px-4 *:data-[slot=card]:bg-gradient-to-t *:data-[slot=card]:from-primary/5 *:data-[slot=card]:to-card *:data-[slot=card]:shadow-xs lg:px-6 @xl/main:grid-cols-2 @5xl/main:grid-cols-4 dark:*:data-[slot=card]:bg-card",
      metrics.map(metricCard)
    )
