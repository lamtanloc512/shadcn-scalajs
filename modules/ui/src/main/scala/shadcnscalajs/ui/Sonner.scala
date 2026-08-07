package shadcnscalajs.ui

import com.raquo.laminar.api.L.*
import com.raquo.laminar.nodes.RootNode
import org.scalajs.dom

import scala.scalajs.js

/** shadcn/ui Sonner — pure Laminar toast stack (no svelte-sonner). Mount [[Toaster]] once; call [[toast]] / [[success]]
  * / [[error]] / [[info]] / [[warning]] / [[loading]] from anywhere to enqueue notifications.
  */
object Sonner:

  enum ToastType derives CanEqual:
    case Default, Success, Error, Info, Warning, Loading

  final case class ToastItem(
      id: String,
      message: String,
      toastType: ToastType,
      durationMs: Option[Int]
  )

  private val queueVar = Var(List.empty[ToastItem])
  private var idCounter = 0
  private val DefaultDurationMs = 4000

  private def nextId(): String =
    idCounter += 1
    s"sonner-$idCounter"

  private def enqueue(message: String, toastType: ToastType, durationMs: Option[Int]): String =
    val id = nextId()
    queueVar.update(_ :+ ToastItem(id, message, toastType, durationMs))
    durationMs.foreach { ms =>
      js.timers.setTimeout(ms.toDouble) {
        dismiss(id)
      }
    }
    id

  def toast(message: String): String =
    enqueue(message, ToastType.Default, Some(DefaultDurationMs))

  def success(message: String): String =
    enqueue(message, ToastType.Success, Some(DefaultDurationMs))

  def error(message: String): String =
    enqueue(message, ToastType.Error, Some(DefaultDurationMs))

  def info(message: String): String =
    enqueue(message, ToastType.Info, Some(DefaultDurationMs))

  def warning(message: String): String =
    enqueue(message, ToastType.Warning, Some(DefaultDurationMs))

  def loading(message: String): String =
    enqueue(message, ToastType.Loading, None)

  def dismiss(id: String): Unit =
    queueVar.update(_.filterNot(_.id == id))

  private def toastIcon(toastType: ToastType): Option[SvgElement] =
    toastType match
      case ToastType.Success => Some(Icons.check(svg.cls := "size-4 text-green-600 dark:text-green-400"))
      case ToastType.Error   => Some(Icons.alertCircle(svg.cls := "size-4 text-destructive"))
      case ToastType.Info    => Some(Icons.circleHelp(svg.cls := "size-4 text-foreground"))
      case ToastType.Warning => Some(Icons.alertCircle(svg.cls := "size-4 text-amber-600 dark:text-amber-400"))
      case ToastType.Loading => Some(Spinner())
      case ToastType.Default => None

  private def toastRow(item: ToastItem): HtmlElement =
    div(
      role := "status",
      cls := "pointer-events-auto relative flex w-full items-start gap-3 overflow-hidden rounded-md border bg-background p-4 pr-8 text-foreground shadow-lg",
      toastIcon(item.toastType).fold(emptyNode)(identity),
      div(cls := "flex-1 text-sm font-medium leading-snug", item.message),
      button(
        typ := "button",
        cls := "absolute top-2 right-2 inline-flex size-6 items-center justify-center rounded-md text-muted-foreground hover:bg-muted hover:text-foreground",
        aria.label := "Dismiss notification",
        Icons.x(svg.cls := "size-3.5"),
        onClick --> { _ => dismiss(item.id) }
      )
    )

  private def toasterElement(mods: Seq[Modifier[HtmlElement]]): HtmlElement =
    div(
      cls := "toaster group pointer-events-none fixed bottom-0 z-[100] flex max-h-screen w-full flex-col-reverse gap-2 p-4 sm:top-auto sm:right-0 sm:bottom-0 sm:flex-col md:max-w-[420px]",
      mods,
      children <-- queueVar.signal.map(_.map(toastRow).toList)
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
