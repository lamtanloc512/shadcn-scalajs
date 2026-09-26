package shadcnscalajs.site.create.preview02

import com.raquo.laminar.api.L.*
import shadcnscalajs.ui.*

/** preview-02 mosaic card — port of savings-progress.svelte */
object SavingsProgress:

  private val ChartData = List(
    ("saved", 24000.0),
    ("remaining", 6000.0)
  )

  def apply(): HtmlElement =
    val hover = Chart.hoverVar()

    Card(
      dataAttr("card") := "savings-progress",
      Card.content(
        Chart(
          cls := "relative mx-auto aspect-square max-h-[220px]",
          Chart.donut(
            ChartData,
            hover,
            Chart.DonutStyle(
              colors = List("var(--chart-2)", "var(--chart-1)"),
              innerRadiusRatio = 0.8,
              padding = 28.0
            )
          ),
          div(
            cls := "pointer-events-none absolute inset-0 flex flex-col items-center justify-center",
            div(cls := "text-2xl font-bold", "$24,000"),
            div(cls := "mt-2 text-sm text-muted-foreground", "80% of $30,000")
          ),
          Chart.tooltip(hover, hideLabel = true)
        )
      ),
      Card.footer(
        cls := "flex-col gap-0",
        div(
          cls := "flex w-full items-center justify-between py-3",
          span(cls := "text-sm text-muted-foreground", "Projected Finish"),
          span(cls := "text-sm font-semibold", "October 2024")
        ),
        Separator(),
        div(
          cls := "flex w-full items-center justify-between py-3",
          span(cls := "text-sm text-muted-foreground", "Monthly Average"),
          span(cls := "text-sm font-semibold tabular-nums", "$1,250")
        ),
        Separator(),
        div(
          cls := "flex w-full items-center justify-between py-3",
          span(cls := "text-sm text-muted-foreground", "Top Contributor"),
          span(cls := "text-sm font-semibold", "Auto-Transfer")
        )
      )
    )
