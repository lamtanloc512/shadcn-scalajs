package shadcnscalajs.site

import com.raquo.laminar.api.L.*
import org.scalajs.dom

/** Site chrome for the two block pages (`/blocks`, `/blocks/<name>`).
  *
  * `/blocks/<name>/preview` deliberately does NOT use this — that route is chrome-less so it can be iframed at a real
  * viewport.
  *
  * This is a fourth header in a codebase that already duplicates three inline in `Main.scala`. Unifying all of them
  * belongs to the docs-site IA redesign spec, so this one stays deliberately small: logo, nav, dark-mode toggle, and
  * the style-pack selector.
  */
object BlocksLayout:

  def apply(content: Modifier[HtmlElement]*): HtmlElement =
    div(
      cls := "min-h-dvh bg-background text-foreground antialiased",
      headerTag(
        cls := "sticky inset-x-0 top-0 z-40 border-b bg-background/95 backdrop-blur",
        div(
          cls := "flex h-14 items-center gap-3 px-4",
          a(
            href := "/",
            cls := "flex items-center gap-2 text-sm font-semibold",
            span(cls := "[&_svg]:size-4", foreignHtmlElement(Main.logoEl)),
            "shadcn-scalajs"
          ),
          navTag(
            cls := "hidden items-center gap-1 md:flex",
            aria.label := "Primary",
            a(cls := Main.btnGhost, href := "/", "Home"),
            a(cls := Main.btnGhost, href := "/components", "Components"),
            a(cls := Main.btnGhost + " bg-accent text-accent-foreground", href := "/blocks", "Blocks"),
            a(cls := Main.btnGhost, href := "/create", "Create")
          ),
          div(
            cls := "ml-auto flex items-center gap-2",
            ThemeMenu()
          )
        )
      ),
      content
    )
