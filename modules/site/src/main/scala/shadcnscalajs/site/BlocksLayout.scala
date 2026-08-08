package shadcnscalajs.site

import com.raquo.laminar.api.L.*

/** Site chrome for the two block pages (`/blocks`, `/blocks/<name>`).
  *
  * `/blocks/<name>/preview` deliberately does NOT use this — that route is chrome-less so it can be iframed at a real
  * viewport.
  */
object BlocksLayout:

  def apply(content: Modifier[HtmlElement]*): HtmlElement =
    div(
      cls := "min-h-dvh bg-background text-foreground antialiased",
      SiteChrome.header(active = SiteChrome.Active.Blocks),
      content
    )
