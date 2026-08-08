package shadcnscalajs.ui

import com.raquo.laminar.api.L.*
import org.scalajs.dom

/** shadcn/ui Carousel — shadcn/ui wraps embla-carousel for swipe/drag physics; this uses native CSS scroll-snap plus
  * `scrollLeft` on the prev/next buttons instead of a JS drag-physics library, matching the project's general
  * preference for native browser mechanisms over hand-rolled/vendored JS where one exists.
  */
object Carousel:

  // Laminar's `aria` object has no `roledescription` key.
  private val ariaRoleDescription =
    htmlAttr("aria-roledescription", com.raquo.laminar.codecs.StringAsIsCodec)

  private val buttonBase: String =
    "inline-flex shrink-0 items-center justify-center gap-2 rounded-md border bg-background text-sm font-medium shadow-xs outline-none hover:bg-accent hover:text-accent-foreground focus-visible:border-ring focus-visible:ring-[3px] focus-visible:ring-ring/50 disabled:pointer-events-none disabled:opacity-50 dark:border-input dark:bg-input/30 dark:hover:bg-input/50 [&_svg]:pointer-events-none [&_svg]:shrink-0 [&_svg:not([class*='size-'])]:size-4 size-8"

  def apply(items: HtmlElement*): HtmlElement =
    val containerRef = Var(Option.empty[dom.html.Element])

    def scrollByViewport(direction: Int): Unit =
      containerRef.now().foreach { el =>
        el.scrollLeft = el.scrollLeft + (el.clientWidth.toDouble * direction)
      }

    div(
      dataAttr("slot") := "carousel",
      cls := "relative",
      role := "region",
      ariaRoleDescription := "carousel",
      div(
        dataAttr("slot") := "carousel-content",
        cls := "overflow-hidden",
        div(
          cls := "flex -ms-4 gap-4 overflow-x-auto scroll-smooth snap-x snap-mandatory [scrollbar-width:none] [&::-webkit-scrollbar]:hidden",
          onMountCallback { ctx => containerRef.set(Some(ctx.thisNode.ref)) },
          items.map(slide => item(cls := "snap-start sm:basis-1/2 lg:basis-1/3", slide)).toList
        )
      ),
      previous(onClick --> { _ => scrollByViewport(-1) }),
      next(onClick --> { _ => scrollByViewport(1) })
    )

  def content(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "carousel-content",
      cls := "overflow-hidden",
      div(cls := "flex -ms-4", mods)
    )

  def item(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "carousel-item",
      role := "group",
      ariaRoleDescription := "slide",
      cls := "min-w-0 shrink-0 grow-0 basis-full ps-4",
      mods
    )

  def previous(mods: Modifier[HtmlElement]*): HtmlElement =
    button(
      typ := "button",
      dataAttr("slot") := "carousel-previous",
      dataAttr("variant") := "outline",
      dataAttr("size") := "icon-sm",
      cls := s"cn-carousel-previous absolute touch-manipulation inset-y-0 -start-12 my-auto $buttonBase",
      aria.label := "Previous slide",
      // Icons has no chevronLeft — rotate chevronRight.
      Icons.chevronRight(svg.cls := "rotate-180"),
      span(cls := "sr-only", "Previous slide"),
      mods
    )

  def next(mods: Modifier[HtmlElement]*): HtmlElement =
    button(
      typ := "button",
      dataAttr("slot") := "carousel-next",
      dataAttr("variant") := "outline",
      dataAttr("size") := "icon-sm",
      cls := s"cn-carousel-next absolute touch-manipulation inset-y-0 -end-12 my-auto $buttonBase",
      aria.label := "Next slide",
      Icons.chevronRight(),
      span(cls := "sr-only", "Next slide"),
      mods
    )
