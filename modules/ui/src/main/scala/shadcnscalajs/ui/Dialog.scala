package shadcnscalajs.ui

import com.raquo.laminar.api.L.*
import org.scalajs.dom

/** shadcn/ui Dialog — uses native `<dialog>` + Tailwind CSS utilities matching the canonical dialog.tsx (without Radix,
  * since Laminar uses browser-native `<dialog>` with `showModal()`/`close()`).
  *
  * The utility classes must stand alone: `modules/ui` is what the CLI copies into consumer projects, which have neither
  * the vendored basecoat CSS nor `shadcn-presets.generated.css`. The `cn-dialog*` hook classes and `data-slot`
  * attributes are additionally what the style-pack presets target — packs are unlayered, so where a pack defines a rule
  * it overrides the utilities below by design.
  */
object Dialog:

  /** Position, surface, and animation for the content panel — everything except the box model, which callers may need
    * to replace wholesale.
    */
  val contentShellClass: String =
    "cn-dialog-content fixed top-1/2 left-1/2 z-50 grid w-full max-w-[calc(100%-2rem)] -translate-x-1/2 -translate-y-1/2 rounded-xl bg-popover text-sm text-popover-foreground outline-none ring-1 ring-foreground/10 duration-100 data-open:animate-in data-open:fade-in-0 data-open:zoom-in-95 data-closed:animate-out data-closed:fade-out-0 data-closed:zoom-out-95"

  /** Upstream's default box model for the content panel. */
  val contentBoxClass: String = "gap-6 p-6 sm:max-w-md"

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
      cls := "cn-dialog-overlay fixed inset-0 isolate z-50 bg-black/10 duration-100 supports-backdrop-filter:backdrop-blur-xs data-open:animate-in data-open:fade-in-0 data-closed:animate-out data-closed:fade-out-0",
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
    element(isOpenVar, rootClass, "sheet-content", "")(mods*)

  private[ui] def element(
      isOpenVar: Var[Boolean],
      rootClass: String,
      contentSlot: String,
      contentClass: String
  )(mods: Modifier[HtmlElement]*): HtmlElement =
    dialogTag(
      cls := rootClass,
      dataAttr("state") <-- isOpenVar.signal.map(open => if open then "open" else "closed"),
      onMountBind { ctx =>
        val el = ctx.thisNode.ref
        // Native Escape / form method=dialog close the <dialog> without touching isOpenVar —
        // mirror that back so callers stay in sync and can run dismiss side-effects.
        val onNativeClose: scala.scalajs.js.Function1[dom.Event, Unit] =
          (_: dom.Event) => if isOpenVar.now() then isOpenVar.set(false)
        el.addEventListener("close", onNativeClose)
        isOpenVar.signal --> { (open: Boolean) =>
          syncOpenAttrs(el, open)
          if open then { if !el.open then el.showModal() }
          else if el.open then el.close()
        }
      },
      onClick --> { (ev: dom.MouseEvent) =>
        if ev.target == ev.currentTarget then isOpenVar.set(false)
      },
      div(
        dataAttr("slot") := contentSlot,
        dataAttr("state") <-- isOpenVar.signal.map(open => if open then "open" else "closed"),
        cls := contentClass,
        onMountBind { ctx =>
          isOpenVar.signal --> { (open: Boolean) => syncOpenAttrs(ctx.thisNode.ref, open) }
        },
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
