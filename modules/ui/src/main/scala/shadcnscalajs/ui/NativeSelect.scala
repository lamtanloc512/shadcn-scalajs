package shadcnscalajs.ui

import com.raquo.laminar.api.L
import com.raquo.laminar.api.L.*

object NativeSelect:

  /** Root control — returns the wrapper so the chevron icon can sit beside the native `<select>`. Select-specific
    * modifiers (`value`, `onChange`, `idAttr`, options) are applied to the inner `<select>`.
    */
  def apply(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "native-select-wrapper",
      dataAttr("size") := "default",
      cls := "cn-native-select-wrapper group/native-select relative w-full has-[select:disabled]:opacity-50",
      select(
        dataAttr("slot") := "native-select",
        dataAttr("size") := "default",
        cls := "cn-native-select flex h-9 w-full min-w-0 appearance-none items-center justify-between rounded-md border border-input bg-transparent py-1 pr-8 pl-3 text-sm shadow-xs outline-none transition-[color,box-shadow] focus-visible:border-ring focus-visible:ring-[3px] focus-visible:ring-ring/50 disabled:pointer-events-none disabled:cursor-not-allowed disabled:opacity-50",
        mods
      ),
      Icons.chevronDown(
        Icons.svgSlot := "native-select-icon",
        svg.cls := "cn-native-select-icon pointer-events-none absolute top-1/2 right-2.5 size-4 -translate-y-1/2 select-none text-muted-foreground"
      )
    )

  def option(mods: Modifier[HtmlElement]*): HtmlElement =
    L.option(
      dataAttr("slot") := "native-select-option",
      cls := "bg-[Canvas] text-[CanvasText]",
      mods
    )

  def optGroup(mods: Modifier[HtmlElement]*): HtmlElement =
    L.optGroup(
      dataAttr("slot") := "native-select-opt-group",
      mods
    )
