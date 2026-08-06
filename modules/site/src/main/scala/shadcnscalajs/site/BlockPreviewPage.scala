package shadcnscalajs.site

import com.raquo.laminar.api.L.*

/** `/blocks/<name>/preview` — chrome-less: mounts only the block, so the docs page can embed it in an iframe at a real
  * viewport without the docs header/sidebar interfering.
  */
object BlockPreviewPage:

  def apply(name: String): HtmlElement =
    Blocks
      .render(name)
      .getOrElse(
        div(cls := "flex min-h-svh items-center justify-center text-sm text-muted-foreground", s"Unknown block: $name")
      )
