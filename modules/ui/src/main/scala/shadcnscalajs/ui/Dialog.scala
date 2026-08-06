package shadcnscalajs.ui

import com.raquo.laminar.api.L.*
import org.scalajs.dom

/** shadcn/ui Dialog — uses native `<dialog>` + Tailwind CSS utilities matching the canonical dialog.tsx (without Radix,
  * since Laminar uses browser-native `<dialog>` with `showModal()`/`close()`).
  */
object Dialog:

  def apply(isOpenVar: Var[Boolean])(mods: Modifier[HtmlElement]*): HtmlElement =
    element(isOpenVar, "dialog")(mods*)

  private[ui] def element(isOpenVar: Var[Boolean], rootClass: String)(mods: Modifier[HtmlElement]*): HtmlElement =
    dialogTag(
      cls := rootClass,
      onMountBind { ctx =>
        val el = ctx.thisNode.ref
        // Native Escape / form method=dialog close the <dialog> without touching isOpenVar —
        // mirror that back so callers stay in sync and can run dismiss side-effects.
        val onNativeClose: scala.scalajs.js.Function1[dom.Event, Unit] =
          (_: dom.Event) => if isOpenVar.now() then isOpenVar.set(false)
        el.addEventListener("close", onNativeClose)
        isOpenVar.signal --> { (open: Boolean) =>
          if open then { if !el.open then el.showModal() }
          else if el.open then el.close()
        }
      },
      onClick --> { (ev: dom.MouseEvent) =>
        if ev.target == ev.currentTarget then isOpenVar.set(false)
      },
      div(mods)
    )
