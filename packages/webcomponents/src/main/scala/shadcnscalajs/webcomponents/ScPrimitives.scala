package shadcnscalajs.webcomponents

import com.raquo.laminar.api.L.*
import org.scalajs.dom
import shadcnscalajs.core.Tags.slotTag
import shadcnscalajs.ui.*

import scala.scalajs.js

/** Lightweight custom-element exports for the remaining slot-only Laminar primitives. */
abstract class SlotPrimitive(node: HtmlElement) extends ScElementBase:
  mount(node)

/** Light-DOM host so a direct child `svg` can drive Alert's `has-[>svg]` icon column, matching Laminar. */
class ScAlert extends LightPrimitive(Alert.rootSlot, Alert.baseClass):
  this.setAttribute("role", "alert")
  ScAlert.syncVariant(this, Option(this.getAttribute("variant")))

  def attributeChangedCallback(name: String, oldValue: js.Any, newValue: js.Any): Unit =
    if name == "variant" then
      val raw =
        if newValue == null || js.isUndefined(newValue) then None else Some(newValue.toString).filter(_.nonEmpty)
      ScAlert.syncVariant(this, raw)

class ScAlertTitle extends LightPrimitive(Alert.titleSlot, Alert.titleClass)
class ScAlertDescription extends LightPrimitive(Alert.descriptionSlot, Alert.descriptionClass)

object ScAlert:
  private def parseVariant(v: Option[String]): Option[Alert.Variant] = v.collect {
    case "default"     => Alert.Variant.Default
    case "destructive" => Alert.Variant.Destructive
  }

  private[webcomponents] def syncVariant(el: dom.HTMLElement, raw: Option[String]): Unit =
    val variant = parseVariant(raw).getOrElse(Alert.Variant.Default)
    el.setAttribute("role", "alert")
    el.setAttribute("data-slot", Alert.rootSlot)
    el.setAttribute("class", s"${Alert.baseClass} ${Alert.variantClass(variant)}")

class ScAvatar extends SlotPrimitive(Avatar(slotTag()))
class ScBreadcrumb extends SlotPrimitive(Breadcrumb(slotTag()))
class ScButtonGroup extends SlotPrimitive(ButtonGroup(slotTag()))
class ScCollapsible
    extends SlotPrimitive(
      Collapsible(
        Collapsible.trigger(slotTag(nameAttr := "trigger")),
        Collapsible.content(slotTag(nameAttr := "content"))
      )
    )
class ScCommand extends SlotPrimitive(Command(slotTag()))
class ScEmpty extends SlotPrimitive(Empty(slotTag()))
class ScField extends SlotPrimitive(Field(slotTag()))
class ScForm extends SlotPrimitive(Form(slotTag()))
class ScInputGroup extends SlotPrimitive(InputGroup(slotTag()))
class ScInput extends SlotPrimitive(Input(slotTag()))
class ScKbd extends SlotPrimitive(Kbd(slotTag()))
class ScLabel extends SlotPrimitive(Label(slotTag()))
class ScNativeSelect extends SlotPrimitive(NativeSelect(slotTag()))
class ScPopover
    extends SlotPrimitive(
      Popover(
        Popover.trigger(slotTag(nameAttr := "trigger")),
        Popover.content(slotTag(nameAttr := "content"))
      )
    )
class ScRadio extends SlotPrimitive(Radio("", slotTag()))
class ScRange extends SlotPrimitive(Range(slotTag()))
class ScScrollbar extends SlotPrimitive(Scrollbar(slotTag()))
class ScSkeleton extends SlotPrimitive(Skeleton(slotTag()))
class ScTextarea extends SlotPrimitive(Textarea(slotTag()))
class ScToast extends SlotPrimitive(Toast(Toast.Variant.Default, slotTag()))
class ScTooltip extends ScElementBase:
  private val textVar = Var("")

  observeAttribute("text")(v => textVar.set(v.getOrElse("")))
  stringProperty("text")

  mount(ScTooltip.view(textVar))

object ScPrimitives:
  def register(): Unit =
    ScElements.define("sc-alert", js.constructorOf[ScAlert], "variant")
    ScElements.define("sc-alert-title", js.constructorOf[ScAlertTitle])
    ScElements.define("sc-alert-description", js.constructorOf[ScAlertDescription])
    register("sc-avatar", js.constructorOf[ScAvatar])
    register("sc-breadcrumb", js.constructorOf[ScBreadcrumb])
    register("sc-button-group", js.constructorOf[ScButtonGroup])
    register("sc-collapsible", js.constructorOf[ScCollapsible])
    register("sc-command", js.constructorOf[ScCommand])
    register("sc-empty", js.constructorOf[ScEmpty])
    register("sc-field", js.constructorOf[ScField])
    register("sc-form", js.constructorOf[ScForm])
    register("sc-input-group", js.constructorOf[ScInputGroup])
    ScElements.define("sc-input", js.constructorOf[ScInput], "value", "placeholder", "name", "disabled")
    register("sc-kbd", js.constructorOf[ScKbd])
    register("sc-label", js.constructorOf[ScLabel])
    register("sc-native-select", js.constructorOf[ScNativeSelect])
    register("sc-popover", js.constructorOf[ScPopover])
    register("sc-radio", js.constructorOf[ScRadio])
    register("sc-range", js.constructorOf[ScRange])
    register("sc-scrollbar", js.constructorOf[ScScrollbar])
    register("sc-skeleton", js.constructorOf[ScSkeleton])
    ScElements.define("sc-textarea", js.constructorOf[ScTextarea], "value", "placeholder", "name", "disabled")
    register("sc-toast", js.constructorOf[ScToast])
    ScElements.define("sc-tooltip", js.constructorOf[ScTooltip], "text")

  private def register(name: String, constructor: js.Dynamic): Unit =
    ScElements.define(name, constructor)

object ScTooltip:
  private def view(textVar: Var[String]): HtmlElement =
    div(
      children <-- textVar.signal.map(text => List(Tooltip(Tooltip.trigger(slotTag()), Tooltip.content(text))))
    )
