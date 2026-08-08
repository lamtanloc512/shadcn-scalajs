package shadcnscalajs.ui

import com.raquo.laminar.api.L.*

/** shadcn/ui ScrollArea — structural parts match upstream; this wave keeps a plain overflow root (no custom scrollbar
  * engine). Compose [[viewport]] / [[scrollbar]] / [[thumb]] when building the full upstream tree.
  */
object ScrollArea:

  def apply(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "scroll-area",
      cls := "relative overflow-auto",
      mods
    )

  def viewport(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "scroll-area-viewport",
      cls := "cn-scroll-area-viewport size-full rounded-[inherit] transition-[color,box-shadow] outline-none focus-visible:ring-[3px] focus-visible:ring-ring/50 focus-visible:outline-1",
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
