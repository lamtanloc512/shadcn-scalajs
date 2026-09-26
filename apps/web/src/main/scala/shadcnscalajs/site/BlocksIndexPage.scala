package shadcnscalajs.site

import com.raquo.laminar.api.L.*
import org.scalajs.dom

/** `/blocks` — category gallery. Each card is a scaled, chrome-less preview of the real block. */
object BlocksIndexPage:

  def apply(): HtmlElement =
    BlocksLayout(
      div(
        cls := "mx-auto w-full max-w-6xl px-6 py-12",
        div(
          cls := "border-b pb-8",
          p(cls := "mb-2 text-sm font-medium text-primary", "Laminar block library"),
          h1(cls := "text-4xl font-semibold tracking-tight", "Blocks"),
          p(
            cls := "mt-3 max-w-2xl text-lg text-muted-foreground",
            "Page and section compositions built from shadcn-scalajs components. Open one for the source, or install it and own every line."
          ),
          p(
            cls := "mt-4 font-mono text-sm text-muted-foreground",
            "npx shadcn-scalajs@latest add login-01"
          )
        ),
        div(
          cls := "mt-10 flex flex-col gap-12",
          Blocks.byCategory.map { case (category, metas) =>
            sectionTag(
              h2(
                cls := "text-sm font-medium tracking-wide text-muted-foreground uppercase",
                category
              ),
              div(
                cls := "mt-4 grid grid-cols-1 gap-6 md:grid-cols-2",
                metas.map(card)
              )
            )
          }
        )
      )
    )

  /** Desktop viewport, scaled to the card. The block lays out at full width, then the frame shrinks it. */
  private def card(meta: Blocks.Meta): HtmlElement =
    val shown = Var(false)
    val shownSignal = shown.signal
    val iframeRef = Var(Option.empty[dom.html.IFrame])
    var intersection = Option.empty[dom.IntersectionObserver]
    var themeObserver = Option.empty[dom.MutationObserver]

    def syncFrame(): Unit =
      iframeRef.now().foreach { frame =>
        val doc = frame.contentDocument
        if doc != null then ThemeConfig.applyToDocument(ThemeConfig.load(), doc)
      }

    a(
      href := s"/blocks/${meta.name}",
      cls := "group flex flex-col overflow-hidden rounded-xl border bg-card shadow-sm transition-colors hover:border-primary",
      onMountUnmountCallback(
        mount = ctx =>
          val observer = new dom.IntersectionObserver((entries, obs) =>
            if entries.find(_.isIntersecting).isDefined then
              shown.set(true)
              obs.disconnect()
          )
          intersection = Some(observer)
          observer.observe(ctx.thisNode.ref)
          val themes = new dom.MutationObserver((_, _) => syncFrame())
          themes.observe(
            dom.document.documentElement,
            new dom.MutationObserverInit {
              attributes = true
            }
          )
          themeObserver = Some(themes)
        ,
        unmount = _ =>
          intersection.foreach(_.disconnect())
          themeObserver.foreach(_.disconnect())
          intersection = None
          themeObserver = None
      ),
      div(
        cls := "relative aspect-[16/10] overflow-hidden border-b bg-muted/30",
        aria.hidden := true,
        child <-- shownSignal.map {
          case false =>
            div(
              cls := "absolute inset-0 flex items-center justify-center text-xs text-muted-foreground",
              "Loading preview"
            )
          case true =>
            iframe(
              cls := "pointer-events-none absolute top-0 left-0 h-[250%] w-[250%] origin-top-left scale-[0.4] border-0 bg-background",
              title := s"${meta.title} preview",
              src := s"/blocks/${meta.name}/preview",
              onLoad --> Observer { (ev: dom.Event) =>
                val frame = ev.target.asInstanceOf[dom.html.IFrame]
                iframeRef.set(Some(frame))
                syncFrame()
              }
            )
        }
      ),
      div(
        cls := "flex flex-col gap-1 p-4",
        span(cls := "text-sm font-medium text-foreground", meta.title),
        span(cls := "text-sm text-muted-foreground", meta.description)
      )
    )
