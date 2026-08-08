package shadcnscalajs.ui

import com.raquo.laminar.api.L.*

/** Stand-in corresponding to upstream scroll-area scrollbar (`data-slot="scroll-area-scrollbar"`). Keeps a plain
  * overflow container for existing demos; a later wave wires real scrollbar chrome.
  */
object Scrollbar:
  def apply(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "scroll-area-scrollbar",
      dataAttr("orientation") := "vertical",
      cls := "cn-scroll-area-scrollbar relative flex touch-none overflow-auto p-px transition-colors select-none",
      mods
    )
