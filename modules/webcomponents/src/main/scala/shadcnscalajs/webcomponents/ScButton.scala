package shadcnscalajs.webcomponents

import com.raquo.laminar.api.L.*
import com.raquo.laminar.codecs.StringAsIsCodec
import shadcnscalajs.core.Tags.slotTag
import shadcnscalajs.ui.Button

import scala.scalajs.js

class ScButton extends ScElementBase:
  private val variantVar = Var(Button.Variant.Primary)
  private val sizeVar = Var(Button.Size.Default)
  private val disabledVar = Var(false)
  private val ariaHasPopupVar = Var(Option.empty[String])
  private val ariaExpandedVar = Var(Option.empty[String])
  private val ariaControlsVar = Var(Option.empty[String])
  private val stateVar = Var(Option.empty[String])
  private val ariaHasPopupAttr = htmlAttr("aria-haspopup", StringAsIsCodec)
  private val ariaExpandedAttr = htmlAttr("aria-expanded", StringAsIsCodec)
  private val ariaControlsAttr = htmlAttr("aria-controls", StringAsIsCodec)

  observeAttribute("variant")(v => ScButton.parseVariant(v).foreach(variantVar.set))
  observeAttribute("size")(v => ScButton.parseSize(v).foreach(sizeVar.set))
  observeAttribute("disabled")(v => disabledVar.set(v.isDefined))
  observeAttribute("aria-haspopup")(ariaHasPopupVar.set)
  observeAttribute("aria-expanded")(ariaExpandedVar.set)
  observeAttribute("aria-controls")(ariaControlsVar.set)
  observeAttribute("data-state")(stateVar.set)
  stringProperty("variant")
  stringProperty("size")
  booleanProperty("disabled")

  mount(
    button(
      typ := "button",
      dataAttr("slot") := "button",
      cls <-- variantVar.signal.combineWithFn(sizeVar.signal)(Button.classes),
      disabled <-- disabledVar.signal,
      ariaHasPopupAttr <-- ariaHasPopupVar.signal.map(_.getOrElse("")),
      ariaExpandedAttr <-- ariaExpandedVar.signal.map(_.getOrElse("")),
      ariaControlsAttr <-- ariaControlsVar.signal.map(_.getOrElse("")),
      dataAttr("state") <-- stateVar.signal.map(_.getOrElse("")),
      dataAttr("variant") <-- variantVar.signal.map(_.toString.toLowerCase),
      dataAttr("size") <-- sizeVar.signal.map(_.toString.toLowerCase),
      slotTag()
    )
  )

object ScButton:
  def register(): Unit =
    ScElements.define(
      "sc-button",
      js.constructorOf[ScButton],
      "variant",
      "size",
      "disabled",
      "aria-haspopup",
      "aria-expanded",
      "aria-controls",
      "data-state"
    )

  private def parseVariant(v: Option[String]): Option[Button.Variant] = v.collect {
    case "primary"     => Button.Variant.Primary; case "secondary" => Button.Variant.Secondary
    case "outline"     => Button.Variant.Outline; case "ghost"     => Button.Variant.Ghost
    case "destructive" => Button.Variant.Destructive; case "link"  => Button.Variant.Link
  }

  private def parseSize(v: Option[String]): Option[Button.Size] = v.collect {
    case "default" => Button.Size.Default; case "xs"     => Button.Size.Xs; case "sm"          => Button.Size.Sm
    case "lg"      => Button.Size.Lg; case "icon"        => Button.Size.Icon
    case "icon-xs" => Button.Size.IconXs; case "icon-sm" => Button.Size.IconSm; case "icon-lg" => Button.Size.IconLg
  }
