package shadcnscalajs.blocks.login04

import com.raquo.laminar.api.L.*

/** Port of shadcn-svelte `blocks/login-04/+page.svelte`. */
object Login04:

  def apply(): HtmlElement =
    div(
      cls := "flex min-h-svh flex-col items-center justify-center bg-muted p-6 md:p-10",
      div(cls := "w-full max-w-sm md:max-w-3xl", LoginForm())
    )
