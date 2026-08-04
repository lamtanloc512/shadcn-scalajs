package shadcnscalajs.ui

import com.raquo.laminar.api.L.*
import org.scalajs.dom

/** shadcn/ui Carousel — shadcn/ui wraps embla-carousel for swipe/drag physics; this uses native CSS scroll-snap plus
  * `scrollLeft` on the prev/next buttons instead of a JS drag-physics library, matching the project's general
  * preference for native browser mechanisms over hand-rolled/vendored JS where one exists.
  */
object Carousel:

  def apply(items: HtmlElement*): HtmlElement =
    val containerRef = Var(Option.empty[dom.html.Element])

    def scrollByViewport(direction: Int): Unit =
      containerRef.now().foreach { el =>
        el.scrollLeft = el.scrollLeft + (el.clientWidth.toDouble * direction)
      }

    div(
      cls := "relative",
      div(
        cls := "flex gap-4 overflow-x-auto scroll-smooth snap-x snap-mandatory [scrollbar-width:none] [&::-webkit-scrollbar]:hidden",
        onMountCallback { ctx => containerRef.set(Some(ctx.thisNode.ref)) },
        items.map(item => div(cls := "min-w-0 shrink-0 grow-0 basis-full snap-start sm:basis-1/2 lg:basis-1/3", item))
      ),
      button(
        typ := "button",
        cls := "absolute left-2 top-1/2 -translate-y-1/2 inline-flex size-8 items-center justify-center rounded-full border bg-background shadow-xs hover:bg-accent",
        aria.label := "Previous slide",
        onClick --> { _ => scrollByViewport(-1) },
        "‹"
      ),
      button(
        typ := "button",
        cls := "absolute right-2 top-1/2 -translate-y-1/2 inline-flex size-8 items-center justify-center rounded-full border bg-background shadow-xs hover:bg-accent",
        aria.label := "Next slide",
        onClick --> { _ => scrollByViewport(1) },
        "›"
      )
    )
