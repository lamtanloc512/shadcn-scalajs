package shadcnscalajs.blocks.signup01

import com.raquo.laminar.api.L.*

/** Port of shadcn/ui new-york-v4 `blocks/signup-01/page.tsx`. See Login01's note on file-based routing. */
object Signup01:

  def apply(): HtmlElement =
    div(
      cls := "flex min-h-svh w-full items-center justify-center p-6 md:p-10",
      div(cls := "w-full max-w-sm", SignupForm())
    )
