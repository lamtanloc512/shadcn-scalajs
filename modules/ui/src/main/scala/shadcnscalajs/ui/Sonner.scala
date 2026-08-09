package shadcnscalajs.ui

import com.raquo.laminar.api.L.*
import com.raquo.laminar.nodes.RootNode
import org.scalajs.dom

import scala.scalajs.js

/** shadcn/ui Sonner — pure Laminar toast stack (no svelte-sonner). Mount [[Toaster]] once; call [[toast]] / [[success]]
  * / [[error]] / [[info]] / [[warning]] / [[loading]] from anywhere to enqueue notifications.
  *
  * Upstream `sonner.svelte` has no `data-slot` attributes; only the `toaster group` class contract is mirrored here.
  * Pack styling hooks use `cn-toast` on individual toast rows.
  */
object Sonner:

  enum ToastType derives CanEqual:
    case Default, Success, Error, Info, Warning, Loading

  /** An optional action button rendered on the trailing edge of a toast, mirroring sonner's `action: { label, onClick
    * }`.
    */
  final case class ToastAction(label: String, onClick: () => Unit)

  final case class ToastItem(
      id: String,
      message: String,
      description: Option[String],
      toastType: ToastType,
      durationMs: Option[Int],
      action: Option[ToastAction]
  )

  /** Stack geometry lifted from sonner: rows sit one on top of another, 14px apart, and everything past the third fades
    * out behind the front toast. Collapsed, each row behind the front is 5% smaller and one gap lower; hovering expands
    * the stack so every row sits at its own measured offset.
    */
  private val GapPx = 14.0
  private val VisibleToasts = 3
  private val ScaleStep = 0.05
  private val TransitionMs = 400
  private val DefaultDurationMs = 4000

  /** Newest first — index 0 is the front toast, which is what sonner marks `data-front`. */
  private val queueVar = Var(List.empty[ToastItem])

  /** Measured on mount; the expanded offsets need real heights because toasts vary with description length. */
  private val heightsVar = Var(Map.empty[String, Double])
  private val expandedVar = Var(false)

  /** Toasts kept in the DOM while they animate out, so removal is not instant. */
  private val exitingVar = Var(Set.empty[String])
  private var idCounter = 0

  private def nextId(): String =
    idCounter += 1
    s"sonner-$idCounter"

  /** Auto-dismiss clock for one toast. Hovering the stack pauses every countdown and banks the time left, matching
    * sonner — otherwise a toast you moved the mouse over to read would vanish mid-sentence.
    */
  private final class Countdown(
      var handle: Option[js.timers.SetTimeoutHandle],
      var remainingMs: Double,
      var startedAt: Double
  )

  private val countdowns = scala.collection.mutable.Map.empty[String, Countdown]

  private def armCountdown(id: String, ms: Double): Unit =
    val handle = js.timers.setTimeout(ms) { dismiss(id) }
    countdowns.update(id, new Countdown(Some(handle), ms, js.Date.now()))

  private def cancelCountdown(id: String): Unit =
    countdowns.remove(id).foreach(_.handle.foreach(js.timers.clearTimeout))

  private def pauseCountdowns(): Unit =
    countdowns.valuesIterator.foreach { countdown =>
      countdown.handle.foreach { handle =>
        js.timers.clearTimeout(handle)
        countdown.handle = None
        countdown.remainingMs = math.max(0.0d, countdown.remainingMs - (js.Date.now() - countdown.startedAt))
      }
    }

  private def resumeCountdowns(): Unit =
    countdowns.foreach { (id, countdown) =>
      if countdown.handle.isEmpty then
        countdown.startedAt = js.Date.now()
        countdown.handle = Some(js.timers.setTimeout(countdown.remainingMs) { dismiss(id) })
    }

  private def enqueue(
      message: String,
      toastType: ToastType,
      durationMs: Option[Int],
      description: Option[String],
      action: Option[ToastAction]
  ): String =
    val id = nextId()
    queueVar.update(ToastItem(id, message, description, toastType, durationMs, action) :: _)
    durationMs.foreach(ms => armCountdown(id, ms.toDouble))
    id

  def toast(
      message: String,
      description: Option[String] = None,
      action: Option[ToastAction] = None
  ): String =
    enqueue(message, ToastType.Default, Some(DefaultDurationMs), description, action)

  def success(message: String, description: Option[String] = None): String =
    enqueue(message, ToastType.Success, Some(DefaultDurationMs), description, None)

  def error(message: String, description: Option[String] = None): String =
    enqueue(message, ToastType.Error, Some(DefaultDurationMs), description, None)

  def info(message: String, description: Option[String] = None): String =
    enqueue(message, ToastType.Info, Some(DefaultDurationMs), description, None)

  def warning(message: String, description: Option[String] = None): String =
    enqueue(message, ToastType.Warning, Some(DefaultDurationMs), description, None)

  def loading(message: String, description: Option[String] = None): String =
    enqueue(message, ToastType.Loading, None, description, None)

  /** Flags the toast as leaving so it can animate out, then drops it once the transition has run. */
  def dismiss(id: String): Unit =
    if !exitingVar.now().contains(id) then
      cancelCountdown(id)
      exitingVar.update(_ + id)
      js.timers.setTimeout(TransitionMs.toDouble) {
        queueVar.update(_.filterNot(_.id == id))
        exitingVar.update(_ - id)
        heightsVar.update(_ - id)
      }

  private def toastIcon(toastType: ToastType): Option[SvgElement] =
    toastType match
      case ToastType.Success => Some(Icons.check(svg.cls := "size-4 text-green-600 dark:text-green-400"))
      case ToastType.Error   => Some(Icons.alertCircle(svg.cls := "size-4 text-destructive"))
      case ToastType.Info    => Some(Icons.circleHelp(svg.cls := "size-4 text-foreground"))
      case ToastType.Warning => Some(Icons.alertCircle(svg.cls := "size-4 text-amber-600 dark:text-amber-400"))
      case ToastType.Loading => Some(Spinner())
      case ToastType.Default => None

  /** Resting transform for one row. Collapsed the stack is faked with scale; expanded every row moves to the summed
    * height of the rows above it. Un-mounted and leaving rows sit above the viewport edge at zero opacity, which is
    * what produces the slide-down entrance and the slide-up exit.
    */
  private def rowStyle(id: String, mountedSignal: Signal[Boolean]): Signal[String] =
    queueVar.signal
      .combineWith(heightsVar.signal, expandedVar.signal, exitingVar.signal, mountedSignal)
      .map { (items, heights, expanded, exiting, isMounted) =>
        val depth = math.max(0, items.indexWhere(_.id == id))
        val zIndex = 100 - depth
        if !isMounted || exiting.contains(id) then s"transform: translateY(-100%); opacity: 0; z-index: $zIndex;"
        else
          val transform =
            if expanded then
              val offset = items.take(depth).map(item => heights.getOrElse(item.id, 0.0) + GapPx).sum
              s"translateY(${offset}px)"
            else s"translateY(${depth * GapPx}px) scale(${1.0 - depth * ScaleStep})"
          val opacity = if depth >= VisibleToasts then "0" else "1"
          s"transform: $transform; opacity: $opacity; z-index: $zIndex;"
      }

  private def toastRow(id: String, item: ToastItem): HtmlElement =
    val mounted = Var(false)
    li(
      role := "status",
      dataAttr("index") <-- queueVar.signal.map(items => math.max(0, items.indexWhere(_.id == id)).toString),
      dataAttr("expanded") <-- expandedVar.signal.map(_.toString),
      // 13px type and a 6px gap are sonner's own metrics; at 14px the stock demo's date wraps to a second line and
      // every stacked offset drifts from upstream.
      cls := "cn-toast group/toast pointer-events-auto absolute top-0 left-0 flex w-full origin-top items-center gap-1.5 rounded-md border bg-background p-4 text-[13px] text-foreground shadow-lg transition-[transform,opacity] duration-[400ms]",
      styleAttr <-- rowStyle(id, mounted.signal),
      // Two frames: the first commits the off-screen start style, the second flips to the resting one so the browser
      // has something to transition between. A single frame lands both in the same style recalculation and snaps.
      onMountCallback { ctx =>
        heightsVar.update(_.updated(id, ctx.thisNode.ref.offsetHeight.toDouble))
        dom.window.requestAnimationFrame(_ => dom.window.requestAnimationFrame(_ => mounted.set(true)))
      },
      toastIcon(item.toastType).fold(emptyNode)(identity),
      div(
        cls := "flex flex-1 flex-col gap-[2px]",
        div(cls := "font-medium leading-normal", item.message),
        item.description.fold(emptyNode)(desc => div(cls := "leading-[1.4] text-muted-foreground", desc))
      ),
      item.action.fold(emptyNode) { act =>
        button(
          typ := "button",
          cls := "inline-flex h-6 shrink-0 items-center justify-center rounded bg-primary px-2 text-xs font-medium text-primary-foreground transition-colors hover:bg-primary/90",
          act.label,
          onClick --> { _ =>
            act.onClick()
            dismiss(item.id)
          }
        )
      },
      button(
        typ := "button",
        cls := "absolute top-2 right-2 inline-flex size-6 items-center justify-center rounded-md text-muted-foreground opacity-0 transition-opacity hover:bg-muted hover:text-foreground group-hover/toast:opacity-100 focus-visible:opacity-100",
        aria.label := "Dismiss notification",
        Icons.x(svg.cls := "size-3.5"),
        onClick --> { _ => dismiss(item.id) }
      )
    )

  private val toasterVars =
    "--normal-bg: var(--color-popover); --normal-text: var(--color-popover-foreground); --normal-border: var(--color-border);"

  /** The rows are absolutely positioned, so the list needs an explicit height for the hover target to cover the stack
    * you can actually see.
    */
  private val toasterStyle: Signal[String] =
    queueVar.signal.combineWith(heightsVar.signal, expandedVar.signal).map { (items, heights, expanded) =>
      val height =
        if items.isEmpty then 0.0
        else if expanded then items.map(item => heights.getOrElse(item.id, 0.0) + GapPx).sum - GapPx
        else
          val front = items.headOption.flatMap(item => heights.get(item.id)).getOrElse(0.0)
          front + (math.min(items.size, VisibleToasts) - 1) * GapPx
      s"$toasterVars height: ${height}px;"
    }

  private def setExpanded(value: Boolean): Unit =
    expandedVar.set(value)
    if value then pauseCountdowns() else resumeCountdowns()

  private def toasterElement(mods: Seq[Modifier[HtmlElement]]): HtmlElement =
    ol(
      cls := "toaster group pointer-events-none fixed top-6 left-1/2 z-[100] w-[356px] max-w-[calc(100vw-2rem)] -translate-x-1/2 list-none p-0",
      styleAttr <-- toasterStyle,
      onMouseEnter --> { _ => setExpanded(true) },
      onMouseLeave --> { _ => setExpanded(false) },
      mods,
      children <-- queueVar.signal.split(_.id)((id, item, _) => toastRow(id, item))
    )

  /** Mount point for the toast stack — portals to `document.body` so toasts are not clipped by page layout. */
  def Toaster(mods: Modifier[HtmlElement]*): HtmlElement =
    val stack = toasterElement(mods.toSeq)
    var portal = Option.empty[RootNode]
    div(
      cls := "hidden",
      onMountUnmountCallback(
        mount = _ => portal = Some(render(dom.document.body, stack)),
        unmount = _ =>
          portal.foreach(_.unmount())
          portal = None
      )
    )
