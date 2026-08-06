package shadcnscalajs.blocks.calendar01

import com.raquo.laminar.api.L.*
import shadcnscalajs.ui.*

import scala.scalajs.js

/** Port of shadcn/ui `blocks/calendar-01.tsx` — a single date picker with a bordered container. */
object Calendar01:

  def apply(): HtmlElement =
    val selected = Var(Option(new js.Date(2025, 5, 12)))
    div(
      cls := "flex min-h-svh w-full items-center justify-center p-6 md:p-10",
      Calendar(selected, cls := "rounded-lg border shadow-sm")
    )
