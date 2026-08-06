package shadcnscalajs.ui

import com.raquo.laminar.api.L.*

/** shadcn/ui ToggleGroup — a row of Toggle-style buttons, either single-select (bound to `Var[Option[String]]`,
  * radio-like) or multi-select (bound to `Var[Set[String]]`).
  */
object ToggleGroup:

  final case class Item(
      value: String,
      label: Modifier[HtmlElement],
      disabled: Signal[Boolean] = Val(false)
  )

  private val rootClasses =
    "cn-toggle-group group/toggle-group flex w-fit flex-row items-center gap-1 data-vertical:flex-col data-vertical:items-stretch"

  private val itemBase =
    "cn-toggle-group-item shrink-0 focus:z-10 focus-visible:z-10 inline-flex items-center justify-center gap-2 rounded-md text-sm font-medium hover:bg-muted hover:text-muted-foreground disabled:pointer-events-none disabled:opacity-50 data-[state=on]:bg-accent data-[state=on]:text-accent-foreground outline-none transition-[color,box-shadow] focus-visible:border-ring focus-visible:ring-[3px] focus-visible:ring-ring/50"

  private val outlineItemClasses =
    "group-data-horizontal/toggle-group:data-[spacing=0]:data-[variant=outline]:border-l-0 group-data-vertical/toggle-group:data-[spacing=0]:data-[variant=outline]:border-t-0 group-data-horizontal/toggle-group:data-[spacing=0]:data-[variant=outline]:first:border-l group-data-vertical/toggle-group:data-[spacing=0]:data-[variant=outline]:first:border-t"

  private val variantClasses: Map[Toggle.Variant, String] = Map(
    Toggle.Variant.Default -> "bg-transparent",
    Toggle.Variant.Outline -> "border border-input bg-transparent shadow-xs hover:bg-accent hover:text-accent-foreground"
  )

  private val sizeClasses: Map[Toggle.Size, String] = Map(
    Toggle.Size.Default -> "h-9 px-2 min-w-9",
    Toggle.Size.Sm -> "h-8 px-1.5 min-w-8",
    Toggle.Size.Lg -> "h-10 px-2.5 min-w-10"
  )

  private def variantData(variant: Toggle.Variant): String = variant match
    case Toggle.Variant.Default => "default"
    case Toggle.Variant.Outline => "outline"

  private def sizeData(size: Toggle.Size): String = size match
    case Toggle.Size.Default => "default"
    case Toggle.Size.Sm      => "sm"
    case Toggle.Size.Lg      => "lg"

  private def itemClasses(variant: Toggle.Variant, size: Toggle.Size): String =
    val outline = if variant == Toggle.Variant.Outline then outlineItemClasses else ""
    s"$itemBase $outline ${variantClasses(variant)} ${sizeClasses(size)}"

  def single(
      selected: Var[Option[String]],
      variant: Toggle.Variant = Toggle.Variant.Default,
      size: Toggle.Size = Toggle.Size.Default,
      items: Item*
  ): HtmlElement =
    div(
      role := "group",
      dataAttr("slot") := "toggle-group",
      dataAttr("variant") := variantData(variant),
      dataAttr("size") := sizeData(size),
      dataAttr("spacing") := "0",
      cls := rootClasses,
      children <-- selected.signal.map { current =>
        items.toList.map { item =>
          button(
            typ := "button",
            dataAttr("slot") := "toggle-group-item",
            dataAttr("variant") := variantData(variant),
            dataAttr("size") := sizeData(size),
            dataAttr("spacing") := "0",
            cls := itemClasses(variant, size),
            dataAttr("state") := (if current.contains(item.value) then "on" else "off"),
            disabled <-- item.disabled,
            onClick --> { _ => selected.set(Some(item.value)) },
            item.label
          )
        }
      }
    )

  def multiple(
      selected: Var[Set[String]],
      variant: Toggle.Variant = Toggle.Variant.Default,
      size: Toggle.Size = Toggle.Size.Default,
      items: Item*
  ): HtmlElement =
    div(
      role := "group",
      dataAttr("slot") := "toggle-group",
      dataAttr("variant") := variantData(variant),
      dataAttr("size") := sizeData(size),
      dataAttr("spacing") := "0",
      cls := rootClasses,
      children <-- selected.signal.map { current =>
        items.toList.map { item =>
          button(
            typ := "button",
            dataAttr("slot") := "toggle-group-item",
            dataAttr("variant") := variantData(variant),
            dataAttr("size") := sizeData(size),
            dataAttr("spacing") := "0",
            cls := itemClasses(variant, size),
            dataAttr("state") := (if current.contains(item.value) then "on" else "off"),
            disabled <-- item.disabled,
            onClick --> { _ =>
              selected.update(s => if s.contains(item.value) then s - item.value else s + item.value)
            },
            item.label
          )
        }
      }
    )
