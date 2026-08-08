package shadcnscalajs.ui

import com.raquo.laminar.api.L.*
import com.raquo.laminar.nodes.RootNode
import org.scalajs.dom

import scala.scalajs.js

/** Anchored-overlay primitive shared by every floating component — Popover, Tooltip, HoverCard, and the menus.
  *
  * Upstream gets this from bits-ui, which gets it from Floating UI. The parts that matter for matching its behavior:
  *
  *   - The panel is rendered into `document.body`. Inline it gets clipped by any `overflow` ancestor, and no z-index
  *     lifts an element out of an `overflow` subtree. A `backdrop-blur` ancestor is also a containing block for fixed
  *     descendants, so even `position: fixed` would resolve against that ancestor and stay clipped.
  *   - `position: fixed` is set inline rather than through the utility, because menu style packs set
  *     `.cn-menu-translucent { position: relative }` to anchor their backdrop pseudo-element; being unlayered, that
  *     rule beats the utility and would drop the panel into body flow below the page.
  *   - Placement is measured after the browser has applied `display: block`, otherwise the panel is still zero-sized
  *     and every flip decision is made against the wrong height.
  *   - `data-side`, `data-align`, and `data-state` are set on the panel so the upstream animation utilities
  *     (`data-[side=bottom]:slide-in-from-top-2`, `data-[state=open]:animate-in`) have something to match.
  */
object Floating:

  enum Side derives CanEqual:
    case Top, Right, Bottom, Left

  enum Align derives CanEqual:
    case Start, Center, End

  final case class Placement(
      side: Side = Side.Bottom,
      align: Align = Align.Center,
      sideOffset: Double = 4.0,
      collisionPadding: Double = 8.0,
      matchTriggerWidth: Boolean = false
  )

  /** Shared open state and element handles for one trigger/panel pair. */
  final class Anchor private[Floating] (
      val isOpen: Var[Boolean],
      private[Floating] val triggerRef: Var[Option[dom.html.Element]],
      private[Floating] val contentRef: Var[Option[dom.html.Element]]
  ):
    def open(): Unit = isOpen.set(true)
    def close(): Unit = isOpen.set(false)
    def toggle(): Unit = isOpen.update(!_)

  def anchor(): Anchor = Anchor(Var(false), Var(None), Var(None))

  private def compPath(ev: dom.Event): js.Array[dom.EventTarget] =
    ev.asInstanceOf[js.Dynamic].composedPath().asInstanceOf[js.Array[dom.EventTarget]]

  /** Ref capture plus the ARIA and `data-state` contract every trigger needs. Interaction is left to the caller, since
    * popovers and menus open on click while tooltips and hover cards open on hover.
    */
  def triggerBase(a: Anchor): Modifier[HtmlElement] =
    Seq(
      dataAttr("state") <-- a.isOpen.signal.map(open => if open then "open" else "closed"),
      aria.expanded <-- a.isOpen.signal,
      onMountCallback { (ctx: com.raquo.laminar.lifecycle.MountContext[HtmlElement]) =>
        a.triggerRef.set(Some(ctx.thisNode.ref))
      }
    )

  def clickToToggle(a: Anchor): Modifier[HtmlElement] =
    onClick.stopPropagation --> { _ => a.toggle() }

  /** Hover-triggered opening with upstream's delays: hover cards wait before opening, and both hover cards and tooltips
    * wait before closing so the pointer can travel from trigger to panel without dismissing it.
    */
  def hoverToOpen(a: Anchor, openDelay: Double, closeDelay: Double): Modifier[HtmlElement] =
    val timer = Timer()
    Seq(
      onPointerEnter --> { _ => timer.schedule(openDelay, () => a.open()) },
      onPointerLeave --> { _ => timer.schedule(closeDelay, () => a.close()) },
      onFocus --> { _ => a.open() },
      onBlur --> { _ => a.close() },
      onMountUnmountCallback(mount = _ => (), unmount = _ => timer.cancel())
    )

  /** Keeps a hover-triggered panel open while the pointer is inside it. */
  def hoverKeepOpen(a: Anchor, closeDelay: Double): Modifier[HtmlElement] =
    val timer = Timer()
    Seq(
      onPointerEnter --> { _ => timer.cancel() },
      onPointerLeave --> { _ => timer.schedule(closeDelay, () => a.close()) },
      onMountUnmountCallback(mount = _ => (), unmount = _ => timer.cancel())
    )

  private final class Timer:
    private var handle = Option.empty[Int]
    def cancel(): Unit =
      handle.foreach(dom.window.clearTimeout)
      handle = None
    def schedule(delay: Double, action: () => Unit): Unit =
      cancel()
      if delay <= 0 then action()
      else handle = Some(dom.window.setTimeout(() => action(), delay))

  /** The area the trigger's `overflow` ancestors leave visible, in viewport coordinates. A trigger inside a scroll
    * container can be scrolled out of sight while its rect still reports a position; placing against that rect would
    * drop the panel somewhere unrelated on the page.
    */
  private def visibleBounds(trigger: dom.html.Element): (Double, Double, Double, Double) =
    var top = 0.0
    var left = 0.0
    var right = dom.window.innerWidth.toDouble
    var bottom = dom.window.innerHeight.toDouble
    var node = trigger.parentElement
    while node != null && node != dom.document.body do
      val style = dom.window.getComputedStyle(node)
      if style.overflow != "visible" || style.overflowX != "visible" || style.overflowY != "visible" then
        val rect = node.getBoundingClientRect()
        top = math.max(top, rect.top)
        left = math.max(left, rect.left)
        right = math.min(right, rect.right)
        bottom = math.min(bottom, rect.bottom)
      node = node.parentElement
    (top, left, right, bottom)

  private def opposite(side: Side): Side = side match
    case Side.Top    => Side.Bottom
    case Side.Bottom => Side.Top
    case Side.Left   => Side.Right
    case Side.Right  => Side.Left

  private def place(a: Anchor, placement: Placement): Unit =
    for
      trigger <- a.triggerRef.now()
      panel <- a.contentRef.now()
    do
      val rect = trigger.getBoundingClientRect()
      val (clipTop, clipLeft, clipRight, clipBottom) = visibleBounds(trigger)
      val clipped =
        rect.bottom <= clipTop || rect.top >= clipBottom || rect.right <= clipLeft || rect.left >= clipRight

      panel.style.position = "fixed"
      panel.style.visibility = if clipped then "hidden" else "visible"
      panel.style.pointerEvents = if clipped then "none" else "auto"

      if !clipped then
        val pad = placement.collisionPadding
        val gap = placement.sideOffset
        val viewportWidth = dom.window.innerWidth.toDouble
        val viewportHeight = dom.window.innerHeight.toDouble
        if placement.matchTriggerWidth then panel.style.minWidth = s"${rect.width}px"
        val panelWidth = panel.offsetWidth.toDouble
        val panelHeight = panel.offsetHeight.toDouble

        val room = Map(
          Side.Top -> (rect.top - gap - pad),
          Side.Bottom -> (viewportHeight - pad - (rect.bottom + gap)),
          Side.Left -> (rect.left - gap - pad),
          Side.Right -> (viewportWidth - pad - (rect.right + gap))
        )
        val needed = placement.side match
          case Side.Top | Side.Bottom => panelHeight
          case Side.Left | Side.Right => panelWidth
        val flipped = opposite(placement.side)
        val side =
          if room(placement.side) >= needed || room(placement.side) >= room(flipped) then placement.side
          else flipped

        // Along the side axis the panel sits beside the trigger; along the cross axis `align` decides where it starts.
        def crossStart(triggerStart: Double, triggerSize: Double, panelSize: Double): Double =
          placement.align match
            case Align.Start  => triggerStart
            case Align.Center => triggerStart + (triggerSize - panelSize) / 2
            case Align.End    => triggerStart + triggerSize - panelSize

        val (rawLeft, rawTop) = side match
          case Side.Bottom => (crossStart(rect.left, rect.width, panelWidth), rect.bottom + gap)
          case Side.Top    => (crossStart(rect.left, rect.width, panelWidth), rect.top - gap - panelHeight)
          case Side.Right  => (rect.right + gap, crossStart(rect.top, rect.height, panelHeight))
          case Side.Left   => (rect.left - gap - panelWidth, crossStart(rect.top, rect.height, panelHeight))

        val left = math.max(pad, math.min(rawLeft, viewportWidth - pad - panelWidth))
        val top = math.max(pad, math.min(rawTop, viewportHeight - pad - panelHeight))
        panel.style.left = s"${left}px"
        panel.style.top = s"${top}px"
        panel.setAttribute("data-side", side.toString.toLowerCase)
        panel.setAttribute("data-align", placement.align.toString.toLowerCase)

  /** The floating panel: portaled to `document.body`, positioned against the trigger, and dismissed on outside pointer
    * down, Escape, or window blur. Dismissal ignores pointer downs inside the panel itself — it is not a DOM descendant
    * of the trigger, so a naive check would close it before an item's click ever lands.
    */
  def content(a: Anchor, placement: Placement, contentClass: String)(
      children: Modifier[HtmlElement]*
  ): HtmlElement =
    var scrollListener = Option.empty[js.Function1[dom.Event, Unit]]

    val panel = div(
      cls := contentClass,
      display <-- a.isOpen.signal.map(open => if open then "block" else "none"),
      aria.hidden <-- a.isOpen.signal.map(!_),
      dataAttr("state") <-- a.isOpen.signal.map(open => if open then "open" else "closed"),
      onMountBind { _ =>
        a.isOpen.signal --> { open =>
          a.contentRef.now().foreach { el =>
            if open then
              el.setAttribute("data-open", "")
              el.removeAttribute("data-closed")
            else
              el.setAttribute("data-closed", "")
              el.removeAttribute("data-open")
          }
          if open then dom.window.requestAnimationFrame(_ => place(a, placement))
        }
      },
      windowEvents(_.onResize) --> { _ => if a.isOpen.now() then place(a, placement) },
      windowEvents(_.onBlur) --> { _ => if a.isOpen.now() then a.close() },
      documentEvents(_.onMouseDown) --> { (ev: dom.MouseEvent) =>
        if a.isOpen.now() then
          val path = compPath(ev)
          val insideTrigger = a.triggerRef.now().exists(t => path.indexOf(t) != -1)
          val insidePanel = a.contentRef.now().exists(p => path.indexOf(p) != -1)
          if !insideTrigger && !insidePanel then a.close()
      },
      documentEvents(_.onKeyDown) --> { (ev: dom.KeyboardEvent) =>
        if a.isOpen.now() && ev.key == "Escape" then
          a.close()
          a.triggerRef.now().foreach(_.focus())
      },
      onMountUnmountCallback(
        mount = { ctx =>
          a.contentRef.set(Some(ctx.thisNode.ref))
          // Capture phase: scroll containers do not bubble their scroll events.
          val listener: js.Function1[dom.Event, Unit] = _ => if a.isOpen.now() then place(a, placement)
          scrollListener = Some(listener)
          dom.document.addEventListener("scroll", listener, useCapture = true)
        },
        unmount = { _ =>
          scrollListener.foreach(dom.document.removeEventListener("scroll", _, useCapture = true))
          scrollListener = None
          a.contentRef.set(None)
        }
      ),
      children
    )

    var portal = Option.empty[RootNode]
    div(
      cls := "hidden",
      onMountUnmountCallback(
        mount = _ => portal = Some(render(dom.document.body, panel)),
        unmount = _ =>
          portal.foreach(_.unmount())
          portal = None
      )
    )
