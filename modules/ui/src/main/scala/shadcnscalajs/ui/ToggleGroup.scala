package shadcnscalajs.ui

import com.raquo.laminar.api.L.*

/** shadcn/ui ToggleGroup — a row of Toggle-style buttons, either single-select (bound to `Var[Option[String]]`,
  * radio-like) or multi-select (bound to `Var[Set[String]]`).
  */
object ToggleGroup:

  enum Orientation derives CanEqual:
    case Horizontal, Vertical

  final case class Item private (
      value: String,
      label: Modifier[HtmlElement],
      disabled: Signal[Boolean],
      mods: Seq[Modifier[HtmlElement]]
  )

  object Item:
    def apply(value: String, label: String, mods: Modifier[HtmlElement]*): Item =
      new Item(value, label, Val(false), mods.toSeq)

    def apply(value: String, label: String, disabled: Signal[Boolean], mods: Modifier[HtmlElement]*): Item =
      new Item(value, label, disabled, mods.toSeq)

  private val rootClasses =
    "cn-toggle-group group/toggle-group flex w-fit flex-row items-center gap-[--spacing(var(--gap))] data-vertical:flex-col data-vertical:items-stretch"

  private val itemBase =
    "cn-toggle-group-item shrink-0 focus:z-10 focus-visible:z-10 group-data-horizontal/toggle-group:data-[spacing=0]:data-[variant=outline]:border-l-0 group-data-vertical/toggle-group:data-[spacing=0]:data-[variant=outline]:border-t-0 group-data-horizontal/toggle-group:data-[spacing=0]:data-[variant=outline]:first:border-l group-data-vertical/toggle-group:data-[spacing=0]:data-[variant=outline]:first:border-t"

  private def variantClasses(variant: Toggle.Variant): String = variant match
    case Toggle.Variant.Default => "cn-toggle-variant-default"
    case Toggle.Variant.Outline => "cn-toggle-variant-outline"

  private def sizeClasses(size: Toggle.Size): String = size match
    case Toggle.Size.Default => "cn-toggle-size-default"
    case Toggle.Size.Sm      => "cn-toggle-size-sm"
    case Toggle.Size.Lg      => "cn-toggle-size-lg"

  private def variantData(variant: Toggle.Variant): String = variant match
    case Toggle.Variant.Default => "default"
    case Toggle.Variant.Outline => "outline"

  private def sizeData(size: Toggle.Size): String = size match
    case Toggle.Size.Default => "default"
    case Toggle.Size.Sm      => "sm"
    case Toggle.Size.Lg      => "lg"

  private def itemClasses(variant: Toggle.Variant, size: Toggle.Size): String =
    s"$itemBase ${variantClasses(variant)} ${sizeClasses(size)}"

  private def rootOrientationMods(orientation: Orientation): Modifier[HtmlElement] =
    orientation match
      case Orientation.Horizontal => dataAttr("horizontal") := ""
      case Orientation.Vertical   => dataAttr("vertical") := ""

  def single(
      selected: Var[Option[String]],
      variant: Toggle.Variant,
      size: Toggle.Size,
      items: Item*
  ): HtmlElement =
    renderSingle(selected, variant, size, spacing = 0, Orientation.Horizontal, Nil, items*)

  def single(
      selected: Var[Option[String]],
      variant: Toggle.Variant,
      size: Toggle.Size,
      spacing: Int,
      items: Item*
  ): HtmlElement =
    renderSingle(selected, variant, size, spacing, Orientation.Horizontal, Nil, items*)

  def single(
      selected: Var[Option[String]],
      variant: Toggle.Variant,
      size: Toggle.Size,
      spacing: Int,
      orientation: Orientation,
      items: Item*
  ): HtmlElement =
    renderSingle(selected, variant, size, spacing, orientation, Nil, items*)

  def single(
      selected: Var[Option[String]],
      variant: Toggle.Variant,
      size: Toggle.Size,
      spacing: Int,
      orientation: Orientation,
      rootMods: Seq[Modifier[HtmlElement]],
      items: Item*
  ): HtmlElement =
    renderSingle(selected, variant, size, spacing, orientation, rootMods, items*)

  def multiple(
      selected: Var[Set[String]],
      variant: Toggle.Variant,
      size: Toggle.Size,
      items: Item*
  ): HtmlElement =
    renderMultiple(selected, variant, size, spacing = 0, Orientation.Horizontal, Nil, items*)

  def multiple(
      selected: Var[Set[String]],
      variant: Toggle.Variant,
      size: Toggle.Size,
      spacing: Int,
      items: Item*
  ): HtmlElement =
    renderMultiple(selected, variant, size, spacing, Orientation.Horizontal, Nil, items*)

  def multiple(
      selected: Var[Set[String]],
      variant: Toggle.Variant,
      size: Toggle.Size,
      spacing: Int,
      orientation: Orientation,
      items: Item*
  ): HtmlElement =
    renderMultiple(selected, variant, size, spacing, orientation, Nil, items*)

  def multiple(
      selected: Var[Set[String]],
      variant: Toggle.Variant,
      size: Toggle.Size,
      spacing: Int,
      orientation: Orientation,
      rootMods: Seq[Modifier[HtmlElement]],
      items: Item*
  ): HtmlElement =
    renderMultiple(selected, variant, size, spacing, orientation, rootMods, items*)

  private def renderSingle(
      selected: Var[Option[String]],
      variant: Toggle.Variant,
      size: Toggle.Size,
      spacing: Int,
      orientation: Orientation,
      rootMods: Seq[Modifier[HtmlElement]],
      items: Item*
  ): HtmlElement =
    div(
      role := "group",
      dataAttr("slot") := "toggle-group",
      dataAttr("variant") := variantData(variant),
      dataAttr("size") := sizeData(size),
      dataAttr("spacing") := spacing.toString,
      rootOrientationMods(orientation),
      styleAttr := s"--gap: $spacing",
      cls := rootClasses,
      rootMods,
      children <-- selected.signal.map { current =>
        items.toList.map { item =>
          button(
            typ := "button",
            dataAttr("slot") := "toggle-group-item",
            dataAttr("variant") := variantData(variant),
            dataAttr("size") := sizeData(size),
            dataAttr("spacing") := spacing.toString,
            cls := itemClasses(variant, size),
            dataAttr("state") := (if current.contains(item.value) then "on" else "off"),
            disabled <-- item.disabled,
            onClick --> { _ => selected.set(Some(item.value)) },
            item.mods,
            item.label
          )
        }
      }
    )

  private def renderMultiple(
      selected: Var[Set[String]],
      variant: Toggle.Variant,
      size: Toggle.Size,
      spacing: Int,
      orientation: Orientation,
      rootMods: Seq[Modifier[HtmlElement]],
      items: Item*
  ): HtmlElement =
    div(
      role := "group",
      dataAttr("slot") := "toggle-group",
      dataAttr("variant") := variantData(variant),
      dataAttr("size") := sizeData(size),
      dataAttr("spacing") := spacing.toString,
      rootOrientationMods(orientation),
      styleAttr := s"--gap: $spacing",
      cls := rootClasses,
      rootMods,
      children <-- selected.signal.map { current =>
        items.toList.map { item =>
          button(
            typ := "button",
            dataAttr("slot") := "toggle-group-item",
            dataAttr("variant") := variantData(variant),
            dataAttr("size") := sizeData(size),
            dataAttr("spacing") := spacing.toString,
            cls := itemClasses(variant, size),
            dataAttr("state") := (if current.contains(item.value) then "on" else "off"),
            disabled <-- item.disabled,
            onClick --> { _ =>
              selected.update(s => if s.contains(item.value) then s - item.value else s + item.value)
            },
            item.mods,
            item.label
          )
        }
      }
    )
