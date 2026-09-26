package shadcnscalajs.site

import com.raquo.laminar.api.L.*

/** The guide and the component pages share this grid.
  *
  * Installation used to render in its own centered column, so the link in the component sidebar dropped the reader into
  * a different page. Both now sit in the same sidebar, article, and table of contents.
  */
object DocsShell:

  def apply(
      route: Signal[Router.Route],
      componentNames: List[String],
      slugify: String => String,
      gallery: => HtmlElement,
      componentPage: String => HtmlElement
  ): HtmlElement =
    div(
      cls := "mx-auto grid w-full max-w-[1800px] grid-cols-1 lg:grid-cols-[15rem_minmax(0,1fr)] xl:grid-cols-[15rem_minmax(0,1fr)_15rem]",
      sidebar(route, componentNames, slugify),
      child <-- route.map {
        case Router.Route.Docs(slug) => DocsPage(slug)
        case Router.Route.ComponentsIndex =>
          val page = gallery
          page.amend(cls := "xl:col-span-2")
          div(cls := "contents", page)
        case Router.Route.Component(slug) =>
          componentPage(if slug.isEmpty then "drawer" else slug)
        case _ => emptyNode
      }
    )

  private def sidebar(
      route: Signal[Router.Route],
      componentNames: List[String],
      slugify: String => String
  ): HtmlElement =
    asideTag(
      cls := "relative hidden lg:block",
      div(
        cls := "pointer-events-none absolute inset-y-8 right-0 w-px bg-[linear-gradient(to_bottom,transparent_0%,var(--border)_12%,var(--border)_88%,transparent_100%)]"
      ),
      navTag(
        cls := "sticky top-14 h-[calc(100svh-3.5rem)] overflow-y-auto px-3 py-8 [scrollbar-width:none] [&::-webkit-scrollbar]:hidden",
        aria.label := "Docs",
        p(cls := "mb-1 px-2 text-xs font-medium text-muted-foreground", "Sections"),
        DocsPage.pages.map { page =>
          val href = if page.slug == "index" then "/docs" else s"/docs/${page.slug}"
          item(
            href,
            page.title,
            route.map {
              case Router.Route.Docs(slug) => slug == page.slug
              case _                       => false
            }
          )
        },
        item("/components", "Components", route.map(_ == Router.Route.ComponentsIndex)),
        a(
          href := "/blocks",
          cls := itemClass,
          "Blocks"
        ),
        p(cls := "mb-1 mt-6 px-2 text-xs font-medium text-muted-foreground", "Components"),
        componentNames.map { name =>
          val slug = slugify(name)
          item(
            s"/components/$slug",
            name,
            route.map {
              case Router.Route.Component(active) =>
                (if active.isEmpty then "drawer" else active) == slug
              case _ => false
            }
          )
        }
      )
    )

  private val itemClass =
    "mb-0.5 flex h-[30px] w-fit items-center rounded-md border border-transparent px-2 text-[0.8rem] font-medium text-foreground hover:bg-muted"

  private def item(hrefValue: String, label: String, active: Signal[Boolean]): HtmlElement =
    a(
      href := hrefValue,
      cls := itemClass,
      cls("border-accent", "bg-accent") <-- active,
      aria.current <-- active.map(on => if on then "page" else "false"),
      label
    )
