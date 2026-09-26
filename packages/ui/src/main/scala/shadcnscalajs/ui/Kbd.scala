package shadcnscalajs.ui

import com.raquo.laminar.api.L.*

object Kbd:
  private val kbdTag = htmlTag("kbd")
  def apply(mods: Modifier[HtmlElement]*): HtmlElement =
    kbdTag(
      dataAttr("slot") := "kbd",
      cls := "kbd cn-kbd pointer-events-none inline-flex h-5 select-none items-center gap-1 rounded border bg-muted px-1.5 font-mono text-[10px] font-medium text-muted-foreground",
      mods
    )
  def group(mods: Modifier[HtmlElement]*): HtmlElement =
    div(dataAttr("slot") := "kbd-group", cls := "cn-kbd-group flex items-center gap-1", mods)
