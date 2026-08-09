package shadcnscalajs.ui

import com.raquo.laminar.api.L.*
import org.scalajs.dom

import scala.scalajs.js

/** shadcn/ui Dialog — native `<dialog>` with `showModal()`/`close()`, matching the canonical dialog.tsx surface without
  * Radix. Browser modal dialogs already provide the focus trap and scroll lock; this layer adds the pieces bits-ui
  * would otherwise own: exit animations (keep the dialog open through `data-closed:animate-out`), focus restore to the
  * trigger, and Escape/backdrop dismiss that can be turned off for alert dialogs.
  *
  * The utility classes must stand alone: `modules/ui` is what the CLI copies into consumer projects, which have neither
  * the vendored basecoat CSS nor `shadcn-presets.generated.css`. The `cn-dialog*` hook classes and `data-slot`
  * attributes are additionally what the style-pack presets target — packs are unlayered, so where a pack defines a rule
  * it overrides the utilities below by design.
  */
object Dialog:

  /** A CSS animation drops back to its pre-animation style the instant it ends, so without this the panel snaps back to
    * full opacity for the gap between `animate-out` finishing and `close()` hiding the dialog — and the basecoat
    * `.dialog` opacity transition then fades that fully visible panel out a second time. Holding the exit frame makes
    * the two hand off invisibly.
    */
  private[ui] val exitFillClass: String = "data-closed:fill-mode-forwards"

  /** The mask has to be driven from here rather than left to the stylesheet. Basecoat only fades `.dialog`/
    * `.alert-dialog` backdrops, and only once `[open]` goes away — which is after the panel's exit has finished, so the
    * mask sits at full strength over an empty screen and then fades on its own schedule. Sheet and Drawer backdrops
    * have no transition at all and would cut instantly instead. Both are fixed by fading the mask on `data-closed`, on
    * the same clock as the panel, and by cutting the root's own transition so `close()` takes effect immediately.
    * Basecoat pins the mask to `opacity-100` while `[open]` from an unlayered rule, hence `!`.
    */
  private def rootExitClass(exitMs: Double): String =
    val maskDuration = if exitMs > 150 then "backdrop:duration-200" else "backdrop:duration-100"
    s"backdrop:transition-opacity $maskDuration data-closed:transition-none! data-closed:backdrop:opacity-0!"

  /** Position, surface, and animation for the content panel — everything except the box model, which callers may need
    * to replace wholesale.
    */
  val contentShellClass: String =
    s"cn-dialog-content fixed top-1/2 left-1/2 z-50 grid w-full max-w-[calc(100%-2rem)] -translate-x-1/2 -translate-y-1/2 rounded-xl bg-popover text-sm text-popover-foreground outline-none ring-1 ring-foreground/10 duration-100 data-open:animate-in data-open:fade-in-0 data-open:zoom-in-95 data-closed:animate-out data-closed:fade-out-0 data-closed:zoom-out-95 $exitFillClass"

  /** Upstream's default box model for the content panel. */
  val contentBoxClass: String = "gap-6 p-6 sm:max-w-md"

  /** Behaviour knobs shared by Dialog, Sheet, Drawer, and AlertDialog. */
  final case class Options(
      dismissOnOutsideClick: Boolean = true,
      dismissOnEscape: Boolean = true,
      /** How long to keep the dialog open after `isOpenVar` goes false so `data-closed:animate-out` can finish. Sheet
        * uses a 200ms transition; the centered dialogs use 100ms.
        */
      exitMs: Double = 150
  )

  def apply(isOpenVar: Var[Boolean])(mods: Modifier[HtmlElement]*): HtmlElement =
    apply(isOpenVar, contentBoxClass)(mods*)

  /** For panels that need a different box model — edge-to-edge media, a custom width — pass a replacement instead of
    * appending one. Without tailwind-merge an appended `p-0` does not reliably outrank the default `p-6`; which one
    * wins depends on Tailwind's own utility ordering, not on the order the classes were written.
    */
  def apply(isOpenVar: Var[Boolean], contentBoxClass: String)(mods: Modifier[HtmlElement]*): HtmlElement =
    element(isOpenVar, "dialog", "dialog-content", s"$contentShellClass $contentBoxClass")(mods*)

  def overlay(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "dialog-overlay",
      cls := s"cn-dialog-overlay fixed inset-0 isolate z-50 bg-black/10 duration-100 supports-backdrop-filter:backdrop-blur-xs data-open:animate-in data-open:fade-in-0 data-closed:animate-out data-closed:fade-out-0 $exitFillClass",
      mods
    )

  def close(mods: Modifier[HtmlElement]*): HtmlElement =
    Button
      .of(
        _.variant(Button.Variant.Ghost),
        _.size(Button.Size.IconSm),
        _ => dataAttr("slot") := "dialog-close",
        _ => cls := "cn-dialog-close absolute top-4 right-4",
        _ => Icons.x(),
        _ => span(cls := "sr-only", "Close")
      )
      .amend(mods)

  /** Close button wired to the dialog's open state. */
  def close(isOpenVar: Var[Boolean], mods: Modifier[HtmlElement]*): HtmlElement =
    close(onClick --> { _ => isOpenVar.set(false) }, mods)

  def header(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "dialog-header",
      cls := "cn-dialog-header flex flex-col gap-2",
      mods
    )

  def footer(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "dialog-footer",
      cls := "cn-dialog-footer flex flex-col-reverse gap-2 sm:flex-row sm:justify-end",
      mods
    )

  def title(mods: Modifier[HtmlElement]*): HtmlElement =
    h2(
      dataAttr("slot") := "dialog-title",
      cls := "cn-font-heading cn-dialog-title leading-none font-medium",
      mods
    )

  def description(mods: Modifier[HtmlElement]*): HtmlElement =
    p(
      dataAttr("slot") := "dialog-description",
      cls := "cn-dialog-description text-sm text-muted-foreground *:[a]:underline *:[a]:underline-offset-3 *:[a]:hover:text-foreground",
      mods
    )

  /** Back-compat for [[Sidebar]] mobile sheet — panel styles live on the `<dialog>`, children on the wrapper. */
  private[ui] def element(isOpenVar: Var[Boolean], rootClass: String)(mods: Modifier[HtmlElement]*): HtmlElement =
    element(isOpenVar, rootClass, "sheet-content", "", Options(exitMs = 200))(mods*)

  private[ui] def element(
      isOpenVar: Var[Boolean],
      rootClass: String,
      contentSlot: String,
      contentClass: String,
      options: Options = Options(),
      // Applied to the `<dialog>` itself rather than the content panel — some surfaces are styled by rules that key
      // off an attribute on the dialog and select the panel as its child.
      rootMods: Seq[Modifier[HtmlElement]] = Nil
  )(mods: Modifier[HtmlElement]*): HtmlElement =
    // Closing is asynchronous: `data-closed` has to paint and finish its animation before `close()` hides the dialog.
    var closing = false
    var exitTimer: Option[Int] = None
    var returnFocus: Option[dom.html.Element] = None
    var removeExitListeners: () => Unit = () => ()

    def clearExitTimer(): Unit =
      exitTimer.foreach(dom.window.clearTimeout)
      exitTimer = None

    def endExitPhase(): Unit =
      clearExitTimer()
      removeExitListeners()
      removeExitListeners = () => ()
      closing = false

    def contentOf(el: dom.html.Dialog): Option[dom.Element] =
      Option(el.querySelector(s"[data-slot='$contentSlot']"))

    def restoreFocus(): Unit =
      returnFocus.foreach { node =>
        // The trigger may have been removed (route change); focusing a detached node is a no-op that throws in some
        // browsers, so only restore when it is still in the document.
        if dom.document.contains(node) then node.focus()
      }
      returnFocus = None

    def finishClose(el: dom.html.Dialog): Unit =
      endExitPhase()
      if el.open then el.close()
      restoreFocus()

    def openDialog(el: dom.html.Dialog): Unit =
      endExitPhase()
      // Attributes first: the panel is still holding the previous exit frame, and `showModal()` would paint one frame
      // of it before the enter state landed.
      syncOpenAttrs(el, open = true)
      contentOf(el).foreach(syncOpenAttrs(_, open = true))
      if !el.open then
        Option(dom.document.activeElement)
          .collect {
            case e: dom.html.Element
                if e != dom.document.body && e != dom.document.documentElement && !el.contains(e) =>
              e
          }
          .foreach { focused => returnFocus = Some(focused) }
        el.showModal()

    def requestClose(el: dom.html.Dialog): Unit =
      if el.open && !closing then
        closing = true
        syncOpenAttrs(el, open = false)
        contentOf(el).foreach(syncOpenAttrs(_, open = false))
        // Prefer the content panel's animationend; fall back to the exit duration so a panel with no animation still
        // closes. Multiple animationend events fire for fade+zoom, so only the first one that finds us still closing
        // finishes — later ones are no-ops once `closing` is cleared.
        val onEnd: js.Function1[dom.Event, Unit] = (ev: dom.Event) =>
          ev.target match
            case t: dom.Element if t.getAttribute("data-slot") == contentSlot || t == el =>
              if closing then finishClose(el)
            case _ => ()
        el.addEventListener("animationend", onEnd)
        el.addEventListener("transitionend", onEnd)
        removeExitListeners = () =>
          el.removeEventListener("animationend", onEnd)
          el.removeEventListener("transitionend", onEnd)
        exitTimer = Some(dom.window.setTimeout(() => if closing then finishClose(el), options.exitMs))

    dialogTag(
      // `whitespace-normal` resets what the dialog inherits from wherever it was mounted. A dialog is a top-layer
      // overlay, but it is still a DOM descendant of its trigger: the dashboard's row drawer lives inside a
      // `whitespace-nowrap` table cell, whose nowrap otherwise inherited into the whole panel and stopped every
      // paragraph from wrapping, overflowing the content sideways.
      cls := s"$rootClass whitespace-normal ${rootExitClass(options.exitMs)}",
      rootMods,
      dataAttr("state") <-- isOpenVar.signal.map(open => if open then "open" else "closed"),
      onMountBind { ctx =>
        val el = ctx.thisNode.ref.asInstanceOf[dom.html.Dialog]
        // Escape fires `cancel` before the browser would close the dialog. Preventing default lets us run the exit
        // animation ourselves; without it `display: none` lands immediately and kills `data-closed:animate-out`.
        val onCancel: js.Function1[dom.Event, Unit] = (ev: dom.Event) =>
          ev.preventDefault()
          if options.dismissOnEscape then isOpenVar.set(false)
        // Native form method=dialog or an explicit el.close() from outside still need to mirror into the Var.
        val onNativeClose: js.Function1[dom.Event, Unit] = (_: dom.Event) =>
          endExitPhase()
          if isOpenVar.now() then isOpenVar.set(false)
          restoreFocus()
        el.addEventListener("cancel", onCancel)
        el.addEventListener("close", onNativeClose)
        isOpenVar.signal --> { (open: Boolean) =>
          if open then openDialog(el) else requestClose(el)
        }
      },
      onClick --> { (ev: dom.MouseEvent) =>
        if options.dismissOnOutsideClick && ev.target == ev.currentTarget then isOpenVar.set(false)
      },
      div(
        dataAttr("slot") := contentSlot,
        dataAttr("state") <-- isOpenVar.signal.map(open => if open then "open" else "closed"),
        cls := contentClass,
        mods
      )
    )

  private def syncOpenAttrs(el: dom.Element, open: Boolean): Unit =
    if open then
      el.setAttribute("data-open", "")
      el.removeAttribute("data-closed")
    else
      el.setAttribute("data-closed", "")
      el.removeAttribute("data-open")
