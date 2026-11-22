package shadcnscalajs.ui

import com.raquo.laminar.api.L.*
import org.scalajs.dom

import scala.scalajs.js

/** shadcn/ui Message Scroller — a chat viewport that sticks to the newest message.
  *
  * Upstream builds on `@shadcn/react/message-scroller`. This port keeps the same parts and behaviour with a small state
  * object: while the reader is at the end, content growth (a new message, a streaming reply) keeps the viewport pinned
  * to the bottom; once they scroll up it stays put and the scroll button appears.
  *
  * {{{
  * val scroller = MessageScroller.ctx()
  * MessageScroller.root(scroller)(
  *   MessageScroller.viewport(scroller)(MessageScroller.content(messages...)),
  *   MessageScroller.button(scroller)()
  * )
  * }}}
  */
object MessageScroller:

  enum Direction derives CanEqual:
    case Start, End

  /** Distance from an edge, in pixels, that still counts as being at that edge. */
  private val threshold = 24.0

  final class Ctx private[MessageScroller] ():
    val atStart: Var[Boolean] = Var(true)
    val atEnd: Var[Boolean] = Var(true)
    private[MessageScroller] var viewportEl: Option[dom.html.Element] = None

    def scrollTo(direction: Direction, smooth: Boolean = true): Unit =
      viewportEl.foreach { el =>
        val top = if direction == Direction.End then el.scrollHeight.toDouble else 0.0
        el.asInstanceOf[js.Dynamic]
          .scrollTo(js.Dynamic.literal(top = top, behavior = if smooth then "smooth" else "auto"))
      }

    def scrollToEnd(smooth: Boolean = true): Unit = scrollTo(Direction.End, smooth)

    private[MessageScroller] def measure(): Unit =
      viewportEl.foreach { el =>
        atStart.set(el.scrollTop <= threshold)
        atEnd.set(el.scrollHeight - el.scrollTop - el.clientHeight <= threshold)
      }

  def ctx(): Ctx = new Ctx()

  def root(state: Ctx)(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "message-scroller",
      cls := "cn-message-scroller group/message-scroller relative flex size-full min-h-0 flex-col overflow-hidden",
      mods
    )

  def viewport(state: Ctx)(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "message-scroller-viewport",
      tabIndex := 0,
      cls := "cn-message-scroller-viewport size-full min-h-0 min-w-0 scroll-fade-b scrollbar-thin scrollbar-gutter-stable overflow-y-auto overscroll-contain contain-content outline-none",
      onScroll --> { _ => state.measure() },
      onMountUnmountCallbackWithState[HtmlElement, js.Dynamic](
        mount = { ctx =>
          val el = ctx.thisNode.ref
          state.viewportEl = Some(el)
          // Start at the newest message, as a chat does.
          el.scrollTop = el.scrollHeight.toDouble
          state.measure()
          val observer = js.Dynamic.newInstance(js.Dynamic.global.ResizeObserver)({ (_: js.Any) =>
            // Measure before scrolling: growth that happened while pinned must keep the pin.
            if state.atEnd.now() then el.scrollTop = el.scrollHeight.toDouble
            state.measure()
          }: js.Function1[js.Any, Unit])
          observer.observe(el)
          Option(el.firstElementChild).foreach(child => observer.observe(child))
          observer
        },
        unmount = { (_, observer) =>
          observer.foreach(_.disconnect())
          state.viewportEl = None
        }
      ),
      mods
    )

  def content(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "message-scroller-content",
      cls := "cn-message-scroller-content flex h-max min-h-full flex-col gap-8",
      mods
    )

  def item(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "message-scroller-item",
      cls := "cn-message-scroller-item min-w-0 shrink-0 [contain-intrinsic-size:auto_10rem] [content-visibility:auto]",
      mods
    )

  /** Jumps to an edge; hidden while already there. Defaults match upstream: the end edge, a secondary icon button. */
  def button(
      state: Ctx,
      direction: Direction = Direction.End,
      variant: Button.Variant = Button.Variant.Secondary,
      size: Button.Size = Button.Size.IconSm
  )(mods: Modifier[HtmlElement]*): HtmlElement =
    val active = (if direction == Direction.End then state.atEnd else state.atStart).signal.map(!_)
    Button(
      Button.ButtonApi.variant(variant),
      Button.ButtonApi.size(size),
      dataAttr("slot") := "message-scroller-button",
      dataAttr("direction") := direction.toString.toLowerCase,
      dataAttr("active") <-- active.map(_.toString),
      tabIndex <-- active.map(if _ then 0 else -1),
      aria.hidden <-- active.map(!_),
      cls := "cn-message-scroller-button absolute start-1/2 -translate-x-1/2 border-border bg-background text-foreground transition-[translate,scale,opacity] duration-200 hover:bg-muted hover:text-foreground data-[active=false]:pointer-events-none data-[active=false]:scale-95 data-[active=false]:opacity-0 data-[active=false]:duration-400 data-[active=false]:ease-[cubic-bezier(0.7,0,0.84,0)] data-[active=true]:translate-y-0 data-[active=true]:scale-100 data-[active=true]:opacity-100 data-[active=true]:ease-[cubic-bezier(0.23,1,0.32,1)] data-[direction=end]:bottom-4 data-[direction=end]:data-[active=false]:translate-y-full data-[direction=start]:top-4 data-[direction=start]:data-[active=false]:-translate-y-full rtl:translate-x-1/2 data-[direction=start]:[&_svg]:rotate-180",
      onClick --> { _ => state.scrollTo(direction) },
      Icons.arrowDown(),
      span(cls := "sr-only", if direction == Direction.End then "Scroll to end" else "Scroll to start"),
      mods
    )
