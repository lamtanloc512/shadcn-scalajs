package shadcnscalajs.ui

import com.raquo.laminar.api.L.*
import org.scalajs.dom

/** Laminar port of basecoat's Dialog (basecoat/src/css/components/dialog.css + site/src/docs/components/dialog.mdx).
  * Native-element-only tier: modality, focus handling, and inert background are entirely delegated to the browser's
  * native `<dialog>` + `showModal()`/`close()` — basecoat ships no dialog.js at all for this, and neither do we.
  *
  * `isOpenVar` is the single source of truth; toggling it imperatively calls showModal()/close() (dialog state can't be
  * driven by the `open` attribute alone — only showModal() gets you the backdrop + focus trap + inert page).
  */
object Dialog:

  def apply(isOpenVar: Var[Boolean])(mods: Modifier[HtmlElement]*): HtmlElement =
    dialogTag(
      cls := "dialog",
      onMountBind { ctx =>
        isOpenVar.signal --> { (open: Boolean) =>
          val el = ctx.thisNode.ref
          if open then { if !el.open then el.showModal() }
          else el.close()
        }
      },
      // Clicking the backdrop closes the dialog, matching basecoat's
      // `onclick="if (event.target === this) this.close()"` pattern
      // (basecoat/site/src/docs/components/dialog.mdx:64-91).
      onClick --> { (ev: dom.MouseEvent) =>
        if ev.target == ev.currentTarget then isOpenVar.set(false)
      },
      mods
    )
