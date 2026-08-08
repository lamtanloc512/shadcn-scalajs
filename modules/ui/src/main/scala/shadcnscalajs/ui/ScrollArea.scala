package shadcnscalajs.ui

import com.raquo.laminar.api.L.*
import org.scalajs.dom

/** shadcn/ui ScrollArea — a scrolling viewport with a styled overlay scrollbar.
  *
  * Upstream hides the native scrollbar and draws its own thumb, which is what makes the control look the same across
  * platforms. That needs measurement rather than CSS alone: the thumb's height is the viewport/content ratio and its
  * offset tracks `scrollTop`, so both are recomputed on scroll and on resize.
  */
object ScrollArea:

  private val hideNativeScrollbar = "[scrollbar-width:none] [&::-webkit-scrollbar]:hidden"

  def apply(mods: Modifier[HtmlElement]*): HtmlElement =
    val thumbHeight = Var(0.0)
    val thumbOffset = Var(0.0)
    val hasOverflow = Var(false)
    val viewportRef = Var(Option.empty[dom.html.Element])

    def measure(el: dom.html.Element): Unit =
      val content = el.scrollHeight.toDouble
      val visible = el.clientHeight.toDouble
      if content <= visible + 1 then hasOverflow.set(false)
      else
        hasOverflow.set(true)
        val ratio = visible / content
        val height = math.max(24.0, visible * ratio)
        // The thumb travels the track minus its own height, in proportion to how far the content has scrolled.
        val maxScroll = content - visible
        val progress = if maxScroll <= 0 then 0.0 else el.scrollTop / maxScroll
        thumbHeight.set(height)
        thumbOffset.set(progress * (visible - height))

    div(
      dataAttr("slot") := "scroll-area",
      cls := "cn-scroll-area relative overflow-hidden",
      div(
        dataAttr("slot") := "scroll-area-viewport",
        cls := s"cn-scroll-area-viewport size-full overflow-y-auto rounded-[inherit] transition-[color,box-shadow] outline-none focus-visible:ring-[3px] focus-visible:ring-ring/50 $hideNativeScrollbar",
        onMountBind { ctx =>
          val el = ctx.thisNode.ref
          viewportRef.set(Some(el))
          measure(el)
          // A late layout pass (fonts, images, a portal) changes the content height after mount.
          dom.window.requestAnimationFrame(_ => measure(el))
          windowEvents(_.onResize) --> { _ => measure(el) }
        },
        onScroll --> { ev => measure(ev.target.asInstanceOf[dom.html.Element]) },
        mods
      ),
      div(
        dataAttr("slot") := "scroll-area-scrollbar",
        dataAttr("orientation") := "vertical",
        cls := "cn-scroll-area-scrollbar absolute top-0 right-0 flex h-full w-2.5 touch-none border-l border-l-transparent p-px transition-colors select-none",
        display <-- hasOverflow.signal.map(if _ then "flex" else "none"),
        div(
          dataAttr("slot") := "scroll-area-thumb",
          cls := "cn-scroll-area-thumb relative w-full flex-1 rounded-full bg-border",
          styleAttr <-- thumbHeight.signal
            .combineWith(thumbOffset.signal)
            .map((h, offset) => s"height:${h}px;transform:translateY(${offset}px)"),
          onPointerDown --> { (ev: dom.PointerEvent) => startDrag(ev, viewportRef.now()) }
        )
      )
    )

  /** Dragging the thumb scrolls the viewport by the same proportion, with the pointer captured so the gesture survives
    * leaving the thumb's own bounds.
    */
  private def startDrag(ev: dom.PointerEvent, viewport: Option[dom.html.Element]): Unit =
    viewport.foreach { el =>
      ev.preventDefault()
      val thumb = ev.currentTarget.asInstanceOf[dom.html.Element]
      val startY = ev.clientY
      val startScroll = el.scrollTop
      val visible = el.clientHeight.toDouble
      val travel = visible - thumb.getBoundingClientRect().height
      val maxScroll = el.scrollHeight.toDouble - visible

      val onMove: scala.scalajs.js.Function1[dom.PointerEvent, Unit] = (move: dom.PointerEvent) =>
        if travel > 0 then
          val delta = (move.clientY - startY) / travel * maxScroll
          el.scrollTop = math.max(0.0, math.min(maxScroll, startScroll + delta))

      lazy val onUp: scala.scalajs.js.Function1[dom.PointerEvent, Unit] = (_: dom.PointerEvent) =>
        dom.window.removeEventListener("pointermove", onMove)
        dom.window.removeEventListener("pointerup", onUp)

      dom.window.addEventListener("pointermove", onMove)
      dom.window.addEventListener("pointerup", onUp)
    }

  def viewport(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "scroll-area-viewport",
      cls := s"cn-scroll-area-viewport size-full overflow-y-auto rounded-[inherit] outline-none focus-visible:ring-[3px] focus-visible:ring-ring/50 $hideNativeScrollbar",
      mods
    )

  def scrollbar(mods: Modifier[HtmlElement]*): HtmlElement =
    scrollbar("vertical")(mods*)

  def scrollbar(orientation: String)(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "scroll-area-scrollbar",
      dataAttr("orientation") := orientation,
      cls := "cn-scroll-area-scrollbar flex touch-none p-px transition-colors select-none data-[orientation=vertical]:h-full data-[orientation=vertical]:w-2.5 data-[orientation=vertical]:border-l data-[orientation=vertical]:border-l-transparent data-[orientation=horizontal]:h-2.5 data-[orientation=horizontal]:flex-col data-[orientation=horizontal]:border-t data-[orientation=horizontal]:border-t-transparent",
      thumb(),
      mods
    )

  def thumb(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "scroll-area-thumb",
      cls := "cn-scroll-area-thumb relative flex-1 rounded-full bg-border",
      mods
    )
