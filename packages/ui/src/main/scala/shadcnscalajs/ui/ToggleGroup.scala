package shadcnscalajs.ui

import com.raquo.laminar.api.L.*
import com.raquo.laminar.codecs.BooleanAsTrueFalseStringCodec
import org.scalajs.dom
import scala.annotation.targetName

/** shadcn/ui ToggleGroup — a row of Toggle-style buttons, either single-select (bound to `Var[Option[String]]`,
  * radio-like) or multi-select (bound to `Var[Set[String]]`).
  */
object ToggleGroup:

  private val ariaPressedAttr: HtmlAttr[Boolean] = htmlAttr("aria-pressed", BooleanAsTrueFalseStringCodec)

  enum Orientation derives CanEqual:
    case Horizontal, Vertical

  final case class Item private (
      value: String,
      label: Modifier[HtmlElement],
      disabled: Signal[Boolean],
      mods: Seq[Modifier[HtmlElement]]
  )

  object Item:
    def apply(value: String, label: Modifier[HtmlElement], mods: Modifier[HtmlElement]*): Item =
      new Item(value, label, Val(false), mods.toSeq)

    @targetName("applyWithDisabled")
    def apply(
        value: String,
        label: Modifier[HtmlElement],
        disabled: Signal[Boolean],
        mods: Modifier[HtmlElement]*
    ): Item =
      new Item(value, label, disabled, mods.toSeq)

  private val rootClasses =
    "cn-toggle-group group/toggle-group flex w-fit flex-row items-center gap-[--spacing(var(--gap))] data-vertical:flex-col data-vertical:items-stretch"

  private val itemBase =
    "cn-toggle-group-item shrink-0 focus:z-10 focus-visible:z-10 group-data-horizontal/toggle-group:data-[spacing=0]:data-[variant=outline]:border-l-0 group-data-vertical/toggle-group:data-[spacing=0]:data-[variant=outline]:border-t-0 group-data-horizontal/toggle-group:data-[spacing=0]:data-[variant=outline]:first:border-l group-data-vertical/toggle-group:data-[spacing=0]:data-[variant=outline]:first:border-t"

  private def variantData(variant: Toggle.Variant): String = variant match
    case Toggle.Variant.Default => "default"
    case Toggle.Variant.Outline => "outline"

  private def sizeData(size: Toggle.Size): String = size match
    case Toggle.Size.Default => "default"
    case Toggle.Size.Sm      => "sm"
    case Toggle.Size.Lg      => "lg"

  private def itemClasses(variant: Toggle.Variant, size: Toggle.Size): String =
    s"$itemBase ${Toggle.classes(variant, size)}"

  private def rootOrientationMods(orientation: Orientation): Modifier[HtmlElement] =
    orientation match
      case Orientation.Horizontal => dataAttr("horizontal") := ""
      case Orientation.Vertical   => dataAttr("vertical") := ""

  /** Bits UI uses roving focus inside a toggle group. Keep all buttons tabbable for native fallback, while making the
    * orientation arrows plus Home/End move focus and skip disabled items.
    */
  private def keyboardNavigation(orientation: Orientation): Modifier[HtmlElement] =
    onKeyDown --> { event =>
      val delta = (orientation, event.key) match
        case (Orientation.Horizontal, "ArrowRight") => Some(1)
        case (Orientation.Horizontal, "ArrowLeft")  => Some(-1)
        case (Orientation.Vertical, "ArrowDown")    => Some(1)
        case (Orientation.Vertical, "ArrowUp")      => Some(-1)
        case _                                      => None
      val jump = event.key match
        case "Home" => Some(0)
        case "End"  => Some(Int.MaxValue)
        case _      => None

      if delta.nonEmpty || jump.nonEmpty then
        val root = event.currentTarget.asInstanceOf[dom.html.Element]
        val nodes = root.querySelectorAll("[data-slot='toggle-group-item']:not(:disabled)")
        val buttons =
          (0 until nodes.length).map(index => nodes.item(index).asInstanceOf[dom.html.Button]).toVector
        val current = event.target.asInstanceOf[dom.Element].closest("[data-slot='toggle-group-item']")
        val currentIndex = buttons.indexWhere(button => current != null && button == current)
        if buttons.nonEmpty && currentIndex >= 0 then
          event.preventDefault()
          val nextIndex = jump match
            case Some(0)            => 0
            case Some(Int.MaxValue) => buttons.length - 1
            case _                  => (currentIndex + delta.get + buttons.length) % buttons.length
          buttons(nextIndex).focus()
    }

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
      keyboardNavigation(orientation),
      rootMods,
      items.toList.map { item =>
        button(
          typ := "button",
          dataAttr("slot") := "toggle-group-item",
          dataAttr("variant") := variantData(variant),
          dataAttr("size") := sizeData(size),
          dataAttr("spacing") := spacing.toString,
          cls := itemClasses(variant, size),
          dataAttr("state") <-- selected.signal.map(current => if current.contains(item.value) then "on" else "off"),
          ariaPressedAttr <-- selected.signal.map(_.contains(item.value)),
          disabled <-- item.disabled,
          onClick --> { _ =>
            selected.update(value => if value.contains(item.value) then None else Some(item.value))
          },
          item.mods,
          item.label
        )
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
      keyboardNavigation(orientation),
      rootMods,
      items.toList.map { item =>
        button(
          typ := "button",
          dataAttr("slot") := "toggle-group-item",
          dataAttr("variant") := variantData(variant),
          dataAttr("size") := sizeData(size),
          dataAttr("spacing") := spacing.toString,
          cls := itemClasses(variant, size),
          dataAttr("state") <-- selected.signal.map(current => if current.contains(item.value) then "on" else "off"),
          ariaPressedAttr <-- selected.signal.map(_.contains(item.value)),
          disabled <-- item.disabled,
          onClick --> { _ =>
            selected.update(s => if s.contains(item.value) then s - item.value else s + item.value)
          },
          item.mods,
          item.label
        )
      }
    )
