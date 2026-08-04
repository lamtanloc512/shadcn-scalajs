package shadcnscalajs.ui

import com.raquo.laminar.api.L.*

/** shadcn/ui ToggleGroup — a row of Toggle-style buttons, either single-select (bound to `Var[Option[String]]`,
  * radio-like) or multi-select (bound to `Var[Set[String]]`).
  */
object ToggleGroup:

  final case class Item(value: String, label: Modifier[HtmlElement])

  private val itemClasses =
    "inline-flex items-center justify-center gap-2 rounded-md h-9 px-2 min-w-9 text-sm font-medium hover:bg-muted hover:text-muted-foreground data-[state=on]:bg-accent data-[state=on]:text-accent-foreground outline-none transition-[color,box-shadow] focus-visible:border-ring focus-visible:ring-[3px] focus-visible:ring-ring/50"

  def single(selected: Var[Option[String]], items: Item*): HtmlElement =
    div(
      role := "group",
      cls := "flex items-center gap-1",
      children <-- selected.signal.map { current =>
        items.toList.map { item =>
          button(
            typ := "button",
            cls := itemClasses,
            dataAttr("state") := (if current.contains(item.value) then "on" else "off"),
            onClick --> { _ => selected.set(Some(item.value)) },
            item.label
          )
        }
      }
    )

  def multiple(selected: Var[Set[String]], items: Item*): HtmlElement =
    div(
      role := "group",
      cls := "flex items-center gap-1",
      children <-- selected.signal.map { current =>
        items.toList.map { item =>
          button(
            typ := "button",
            cls := itemClasses,
            dataAttr("state") := (if current.contains(item.value) then "on" else "off"),
            onClick --> { _ =>
              selected.update(s => if s.contains(item.value) then s - item.value else s + item.value)
            },
            item.label
          )
        }
      }
    )
