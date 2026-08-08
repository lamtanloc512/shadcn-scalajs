package shadcnscalajs.ui

import com.raquo.laminar.api.L.*
import org.scalajs.dom

import scala.scalajs.js

/** shadcn/ui Carousel — upstream wraps embla-carousel for the drag physics and the transform-based track; this drives
  * the same DOM with native CSS scroll-snap and programmatic scrolling, matching the project's preference for native
  * browser mechanisms over vendored JS where one exists.
  *
  * Everything embla exposes through context and that the parts actually read is reproduced on [[Ctx]]: the selected
  * index, the snap count, whether either direction can still scroll, and `scrollTo`/`scrollPrev`/`scrollNext`. What is
  * not reproduced is pointer dragging and the plugin system (autoplay, wheel gestures) — a scroll container gives
  * dragging on touch for free but not with a mouse.
  *
  * The parts live on [[Ctx]] rather than on the object because each one reads that state, the same reason upstream's
  * parts throw when used outside a `Carousel.Root`.
  */
object Carousel:

  enum Orientation derives CanEqual:
    case Horizontal, Vertical

  // Laminar's `aria` object has no `roledescription` key.
  private val ariaRoleDescription =
    htmlAttr("aria-roledescription", com.raquo.laminar.codecs.StringAsIsCodec)

  /** One carousel instance: the scroll state every part reads, plus the parts themselves. */
  final class Ctx private[ui] (val orientation: Orientation):
    private val scroller = Var(Option.empty[dom.html.Element])
    private val selectedIndexVar = Var(0)
    private val countVar = Var(0)
    private val canPrev = Var(false)
    private val canNext = Var(false)

    /** Index of the slide currently at the scroll origin. */
    val selectedIndex: Signal[Int] = selectedIndexVar.signal

    /** Number of slides, for a "slide N of M" readout. */
    val count: Signal[Int] = countVar.signal

    val canScrollPrev: Signal[Boolean] = canPrev.signal
    val canScrollNext: Signal[Boolean] = canNext.signal

    private def vertical: Boolean = orientation == Orientation.Vertical

    private def slidesOf(el: dom.Element): List[dom.html.Element] =
      el.querySelectorAll(":scope > [data-slot=carousel-item]").toList.collect { case e: dom.html.Element => e }

    /** Distance from the scroll origin to a slide's leading edge. Measured from live rects rather than `offsetTop` /
      * `offsetLeft` so it holds regardless of which ancestor is the offset parent.
      */
    private def leadingEdge(el: dom.html.Element, slide: dom.html.Element): Double =
      if vertical then slide.getBoundingClientRect().top - el.getBoundingClientRect().top + el.scrollTop
      else slide.getBoundingClientRect().left - el.getBoundingClientRect().left + el.scrollLeft

    /** Recomputes what the buttons and the readout show. The 1px slack absorbs the sub-pixel scroll offsets browsers
      * report at either end, which would otherwise leave a button enabled with nowhere to go.
      */
    private def measure(el: dom.html.Element): Unit =
      val slides = slidesOf(el)
      val position = if vertical then el.scrollTop else el.scrollLeft
      val furthest = (if vertical then el.scrollHeight - el.clientHeight else el.scrollWidth - el.clientWidth).toDouble

      countVar.set(slides.length)
      canPrev.set(position > 1)
      canNext.set(position < furthest - 1)
      if slides.nonEmpty then
        val nearest = slides.zipWithIndex.minBy((slide, _) => Math.abs(leadingEdge(el, slide) - position))
        selectedIndexVar.set(nearest._2)

    /** Brings a slide to the scroll origin. `jump` skips the animation, as embla's second argument does. */
    def scrollTo(index: Int, jump: Boolean = false): Unit =
      scroller.now().foreach { el =>
        val slides = slidesOf(el)
        if slides.nonEmpty then
          val target = leadingEdge(el, slides(Math.max(0, Math.min(index, slides.length - 1))))
          val options = js.Dynamic.literal(behavior = if jump then "auto" else "smooth")
          if vertical then options.updateDynamic("top")(target) else options.updateDynamic("left")(target)
          el.asInstanceOf[js.Dynamic].scrollTo(options)
      }

    def scrollPrev(): Unit = scrollTo(selectedIndexVar.now() - 1)

    def scrollNext(): Unit = scrollTo(selectedIndexVar.now() + 1)

    def content(mods: Modifier[HtmlElement]*): HtmlElement =
      div(
        dataAttr("slot") := "carousel-content",
        cls := "overflow-hidden",
        div(
          cls := "flex",
          cls := (if vertical then "-mt-4 flex-col" else "-ms-4"),
          // The scroll mechanics embla replaces with transforms. The scrollbar is hidden because the prev/next
          // buttons are the control surface.
          cls := (if vertical then "snap-y overflow-y-auto" else "snap-x overflow-x-auto"),
          cls := "snap-mandatory scroll-smooth [scrollbar-width:none] [&::-webkit-scrollbar]:hidden",
          onMountBind { ctx =>
            val el = ctx.thisNode.ref
            scroller.set(Some(el))
            measure(el)
            // Slides are usually still unstyled on mount, so the first measurement can see a collapsed track.
            dom.window.requestAnimationFrame(_ => measure(el))
            windowEvents(_.onResize) --> { _ => measure(el) }
          },
          onScroll --> { ev => measure(ev.target.asInstanceOf[dom.html.Element]) },
          mods
        )
      )

    def item(mods: Modifier[HtmlElement]*): HtmlElement =
      div(
        dataAttr("slot") := "carousel-item",
        role := "group",
        ariaRoleDescription := "slide",
        cls := "min-w-0 shrink-0 grow-0 basis-full snap-start",
        cls := (if vertical then "pt-4" else "ps-4"),
        mods
      )

    def previous(mods: Modifier[HtmlElement]*): HtmlElement =
      navButton(
        slot = "carousel-previous",
        label = "Previous slide",
        placement = if vertical then "start-1/2 -top-12 -translate-x-1/2 rotate-90" else "inset-y-0 -start-12 my-auto",
        enabled = canPrev.signal,
        // Icons carries no chevron-left concept; the glyph is symmetric, so the half turn is the same shape.
        icon = Icons.chevronRight(svg.cls := "rotate-180"),
        activate = () => scrollPrev(),
        mods = mods
      )

    def next(mods: Modifier[HtmlElement]*): HtmlElement =
      navButton(
        slot = "carousel-next",
        label = "Next slide",
        placement = if vertical then "start-1/2 -bottom-12 -translate-x-1/2 rotate-90" else "inset-y-0 -end-12 my-auto",
        enabled = canNext.signal,
        icon = Icons.chevronRight(),
        activate = () => scrollNext(),
        mods = mods
      )

    private def navButton(
        slot: String,
        label: String,
        placement: String,
        enabled: Signal[Boolean],
        icon: SvgElement,
        activate: () => Unit,
        mods: Seq[Modifier[HtmlElement]]
    ): HtmlElement =
      Button(
        Button.ButtonApi.variant(Button.Variant.Outline),
        Button.ButtonApi.size(Button.Size.IconSm),
        // Overrides the `button` slot Button sets, the way upstream's spread does.
        dataAttr("slot") := slot,
        cls := s"cn-$slot absolute touch-manipulation",
        cls := placement,
        disabled <-- enabled.map(!_),
        aria.disabled <-- enabled.map(!_),
        onClick --> { _ => activate() },
        // Upstream puts the arrow keys on the buttons rather than the region, so they only steer once a button is
        // focused and never swallow arrow keys aimed at content inside a slide.
        onKeyDown --> { (ev: dom.KeyboardEvent) =>
          ev.key match
            case "ArrowLeft"  => ev.preventDefault(); scrollPrev()
            case "ArrowRight" => ev.preventDefault(); scrollNext()
            case _            => ()
        },
        icon,
        span(cls := "sr-only", label),
        mods
      )

  /** Creates a carousel's state, which its parts hang off. Upstream reads this out of Svelte context; here the caller
    * holds it, which also covers what upstream needs `setApi` for — reading the position from outside the region, as a
    * "slide N of M" readout does.
    */
  def ctx(orientation: Orientation = Orientation.Horizontal): Ctx = Ctx(orientation)

  /** The carousel region, upstream's `Carousel.Root` — `mods` are the parts built from a [[Ctx]]. */
  def root(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "carousel",
      cls := "relative",
      role := "region",
      ariaRoleDescription := "carousel",
      mods
    )

  /** Flat form: one slide per element, with the prev/next buttons wired up. */
  def apply(slides: HtmlElement*): HtmlElement =
    val carousel = ctx()
    root(
      carousel.content(slides.map(slide => carousel.item(slide)).toList),
      carousel.previous(),
      carousel.next()
    )
