package shadcnscalajs.site

import com.raquo.laminar.api.L.*
import shadcnscalajs.ui.Card

/** Shared, theme-aware presentation for source snippets shown in the docs. */
object DocsPresentation:
  val frameClasses = "gap-0! overflow-hidden py-0!"

  def codeBlock(language: String, audience: String, source: String, margin: String = "mt-4"): HtmlElement =
    Card(
      cls := s"$margin $frameClasses",
      div(
        cls := "flex h-9 items-center justify-between border-b bg-muted/40 px-3 text-xs text-muted-foreground",
        span(language),
        span(audience)
      ),
      Card.content(
        cls := "overflow-x-auto bg-muted/20 p-4! px-4! font-mono text-xs leading-6 text-foreground",
        pre(cls := "m-0 whitespace-pre", code(source))
      )
    )
