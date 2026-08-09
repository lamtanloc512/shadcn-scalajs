package shadcnscalajs.blocks.login03

import com.raquo.laminar.api.L.*
import shadcnscalajs.ui.*

/** Port of shadcn-svelte `blocks/login-03/+page.svelte`. */
object Login03:

  def apply(): HtmlElement =
    div(
      cls := "flex min-h-svh flex-col items-center justify-center gap-6 bg-muted p-6 md:p-10",
      div(
        cls := "flex w-full max-w-sm flex-col gap-6",
        a(
          href := "#",
          cls := "flex items-center gap-2 self-center font-medium",
          div(
            cls := "flex size-6 items-center justify-center rounded-md bg-primary text-primary-foreground",
            Icons.galleryVerticalEnd()
          ),
          "Acme Inc."
        ),
        LoginForm()
      )
    )
