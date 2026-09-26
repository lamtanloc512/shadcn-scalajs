package shadcnscalajs.blocks.login01

import com.raquo.laminar.api.L.*

/** Port of shadcn/ui new-york-v4 `blocks/login-01/page.tsx`.
  *
  * Upstream ships this as a file-routed page (`target: app/login/page.tsx`). Laminar has no file-based routing, so it
  * is a plain composition the consumer mounts wherever their own router wants it.
  */
object Login01:

  def apply(): HtmlElement =
    div(
      cls := "flex min-h-svh w-full items-center justify-center p-6 md:p-10",
      div(cls := "w-full max-w-sm", LoginForm())
    )
