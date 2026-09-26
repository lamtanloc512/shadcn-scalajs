package shadcnscalajs.site

import com.raquo.laminar.api.L.*

/** Body wrapper for the two block pages (`/blocks`, `/blocks/<name>`).
  *
  * The page background and header now come from the shell, which outlives navigations, so this is transparent to
  * layout and exists only to keep both block pages entering the routed slot as a single element.
  *
  * `/blocks/<name>/preview` deliberately does NOT use this — that route is chrome-less so it can be iframed at a real
  * viewport.
  */
object BlocksLayout:

  def apply(content: Modifier[HtmlElement]*): HtmlElement =
    div(cls := "contents", content)
