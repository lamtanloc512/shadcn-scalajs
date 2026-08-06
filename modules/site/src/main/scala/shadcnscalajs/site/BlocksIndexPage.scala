package shadcnscalajs.site

import com.raquo.laminar.api.L.*

/** `/blocks` — category-grouped directory of every block. */
object BlocksIndexPage:

  def apply(): HtmlElement =
    BlocksLayout(
      div(
        cls := "mx-auto w-full max-w-5xl px-6 py-12",
        div(
          cls := "border-b pb-8",
          p(cls := "mb-2 text-sm font-medium text-primary", "Laminar block library"),
          h1(cls := "text-4xl font-semibold tracking-tight", "Blocks"),
          p(
            cls := "mt-3 text-lg text-muted-foreground",
            "Ready-made page and section compositions built from shadcn-scalajs components. Install one with the CLI and own every line."
          )
        ),
        div(
          cls := "mt-10 flex flex-col gap-10",
          Blocks.byCategory.map { case (category, metas) =>
            div(
              h2(cls := "text-sm font-medium tracking-wide text-muted-foreground uppercase", category),
              div(
                cls := "mt-4 grid grid-cols-1 gap-4 sm:grid-cols-2",
                metas.map { meta =>
                  a(
                    href := s"/blocks/${meta.name}",
                    cls := "flex flex-col gap-1 rounded-lg border bg-card p-4 transition-colors hover:border-primary",
                    span(cls := "text-sm font-medium text-foreground", meta.title),
                    span(cls := "text-sm text-muted-foreground", meta.description)
                  )
                }
              )
            )
          }
        )
      )
    )
