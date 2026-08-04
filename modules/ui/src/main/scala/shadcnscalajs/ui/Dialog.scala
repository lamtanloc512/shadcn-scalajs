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
        isOpenVar.signal --> { (open: Boolean) =>
          val el = ctx.thisNode.ref
          if open then { if !el.open then el.showModal() }
          else el.close()
        }
      },
      onClick --> { (ev: dom.MouseEvent) =>
        if ev.target == ev.currentTarget then isOpenVar.set(false)
      },
      div(mods)
    )
