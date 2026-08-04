package shadcnscalajs.ui

import com.raquo.laminar.api.L.*
import org.scalajs.dom

/** shadcn/ui Dialog — uses native `<dialog>` + Tailwind CSS utilities matching the canonical dialog.tsx (without Radix,
  * since Laminar uses browser-native `<dialog>` with `showModal()`/`close()`).
  */
object Dialog:

  def apply(isOpenVar: Var[Boolean])(mods: Modifier[HtmlElement]*): HtmlElement =
    dialogTag(
      cls := "fixed top-[50%] left-[50%] z-50 grid w-full max-w-[calc(100%-2rem)] translate-x-[-50%] translate-y-[-50%] gap-4 rounded-lg border bg-background p-6 shadow-lg duration-200 sm:max-w-lg",
      styleAttr <-- isOpenVar.signal.map(open => if open then "display:grid" else "display:none"),
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
      mods
    )
