package shadcnscalajs.ui

import com.raquo.laminar.api.L.*
import org.scalajs.dom

/** shadcn/ui InputGroup — styled with Tailwind CSS utilities matching the canonical new-york-v4 input-group components.
  *
  * The utility classes must stand alone: `modules/ui` is what the CLI copies into consumer projects, which have neither
  * the vendored basecoat CSS nor `shadcn-presets.generated.css`. The `cn-input-group*` hook classes and `data-slot`
  * attributes are additionally what the style-pack presets target — packs are unlayered, so where a pack defines a rule
  * it overrides the utilities below by design.
  */
object InputGroup:

  enum AddonAlign derives CanEqual:
    case InlineStart, InlineEnd, BlockStart, BlockEnd

  enum ButtonSize derives CanEqual:
    case Xs, Sm, IconXs, IconSm

  private val base: String =
    "group/input-group cn-input-group relative flex w-full min-w-0 items-center outline-none has-[>textarea]:h-auto"

  private val addonBase: String =
    "cn-input-group-addon flex cursor-text items-center justify-center select-none"

  private val addonAlignClasses: Map[AddonAlign, String] = Map(
    AddonAlign.InlineStart -> "cn-input-group-addon-align-inline-start order-first",
    AddonAlign.InlineEnd -> "cn-input-group-addon-align-inline-end order-last",
    AddonAlign.BlockStart -> "cn-input-group-addon-align-block-start order-first w-full justify-start",
    AddonAlign.BlockEnd -> "cn-input-group-addon-align-block-end order-last w-full justify-start"
  )

  private val buttonSizeClasses: Map[ButtonSize, String] = Map(
    ButtonSize.Xs -> "cn-input-group-button-size-xs",
    ButtonSize.Sm -> "cn-input-group-button-size-sm",
    ButtonSize.IconXs -> "cn-input-group-button-size-icon-xs",
    ButtonSize.IconSm -> "cn-input-group-button-size-icon-sm"
  )

  private def addonAlignName(align: AddonAlign): String = align match
    case AddonAlign.InlineStart => "inline-start"
    case AddonAlign.InlineEnd   => "inline-end"
    case AddonAlign.BlockStart  => "block-start"
    case AddonAlign.BlockEnd    => "block-end"

  private def buttonSizeName(size: ButtonSize): String = size match
    case ButtonSize.Xs     => "xs"
    case ButtonSize.Sm     => "sm"
    case ButtonSize.IconXs => "icon-xs"
    case ButtonSize.IconSm => "icon-sm"

  def apply(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "input-group",
      role := "group",
      cls := base,
      mods
    )

  def addon(align: AddonAlign = AddonAlign.InlineStart, mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      role := "group",
      dataAttr("slot") := "input-group-addon",
      dataAttr("align") := addonAlignName(align),
      cls := s"$addonBase ${addonAlignClasses(align)}",
      onClick --> { ev =>
        val target = ev.target.asInstanceOf[dom.Element]
        if target.closest("button") == null then
          val parent = ev.currentTarget.asInstanceOf[dom.html.Element].parentElement
          if parent != null then
            parent.querySelector("input") match
              case input: dom.html.Input => input.focus()
              case _                     => ()
      },
      mods
    )

  def input(mods: Modifier[HtmlElement]*): HtmlElement =
    Input(
      dataAttr("slot") := "input-group-control",
      // Strip Input's own chrome so the group is one control. `cn-input-group-input` is the
      // style-pack hook; `!` utilities stand alone for CLI consumers and beat Input's `border` /
      // `shadow-xs` / `ring` in Tailwind source-order conflicts.
      cls := "cn-input-group-input flex-1 rounded-none border-0! bg-transparent! shadow-none! ring-0! focus-visible:ring-0! aria-invalid:ring-0! dark:bg-transparent!",
      mods
    )

  def text(mods: Modifier[HtmlElement]*): HtmlElement =
    span(
      cls := "cn-input-group-text flex items-center [&_svg]:pointer-events-none",
      mods
    )

  def button(size: ButtonSize = ButtonSize.Xs, mods: Modifier[HtmlElement]*): HtmlElement =
    Button(
      Button.ButtonApi.variant(Button.Variant.Ghost),
      cls := s"cn-input-group-button flex items-center shadow-none ${buttonSizeClasses(size)}",
      dataAttr("size") := buttonSizeName(size),
      mods
    )
