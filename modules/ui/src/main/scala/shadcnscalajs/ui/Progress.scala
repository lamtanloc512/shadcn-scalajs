package shadcnscalajs.ui

import com.raquo.laminar.api.L.*

object Progress:
  def apply(value: Int = 0, mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      role := "progressbar",
      aria.valueMin := 0d,
      aria.valueMax := 100d,
      aria.valueNow := value.toDouble,
      cls := "progress cn-progress relative h-2 w-full overflow-hidden rounded-full bg-primary/20",
      span(
        cls := "h-full w-full flex-1 bg-primary transition-all",
        styleAttr := s"transform:translateX(-${100 - value}%)"
      ),
      mods
    )
