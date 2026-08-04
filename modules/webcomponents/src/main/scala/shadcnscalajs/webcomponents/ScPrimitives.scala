package shadcnscalajs.webcomponents

import com.raquo.laminar.api.L.*
import org.scalajs.dom
import shadcnscalajs.core.Tags.slotTag
import shadcnscalajs.ui.*

import scala.scalajs.js

/** Lightweight custom-element exports for the stateless Laminar primitives. */
abstract class SlotPrimitive(node: HtmlElement) extends ScElementBase:
  mount(node)

class ScAlert extends SlotPrimitive(Alert(Alert.Variant.Default, slotTag()))
class ScAvatar extends SlotPrimitive(Avatar(slotTag()))
class ScBreadcrumb extends SlotPrimitive(Breadcrumb(slotTag()))
class ScButtonGroup extends SlotPrimitive(ButtonGroup(slotTag()))
class ScCard extends SlotPrimitive(Card(slotTag()))
class ScCheckbox extends SlotPrimitive(Checkbox(slotTag()))
class ScCollapsible extends SlotPrimitive(Collapsible(slotTag()))
class ScCombobox extends SlotPrimitive(Combobox(slotTag()))
class ScCommand extends SlotPrimitive(Command(slotTag()))
class ScChart extends SlotPrimitive(Chart(slotTag()))
class ScEmpty extends SlotPrimitive(Empty(slotTag()))
class ScField extends SlotPrimitive(Field(slotTag()))
class ScForm extends SlotPrimitive(Form(slotTag()))
class ScInputGroup extends SlotPrimitive(InputGroup(slotTag()))
class ScInput extends SlotPrimitive(Input(slotTag()))
class ScItem extends SlotPrimitive(Item(slotTag()))
class ScKbd extends SlotPrimitive(Kbd(slotTag()))
class ScLabel extends SlotPrimitive(Label(slotTag()))
class ScNativeSelect extends SlotPrimitive(NativeSelect(slotTag()))
class ScPopover extends SlotPrimitive(Popover(slotTag()))
class ScProgress extends SlotPrimitive(Progress(0, slotTag()))
class ScRadio extends SlotPrimitive(Radio("", slotTag()))
class ScRange extends SlotPrimitive(Range(slotTag()))
class ScScrollbar extends SlotPrimitive(Scrollbar(slotTag()))
class ScSelect extends SlotPrimitive(Select(slotTag()))
class ScSidebar extends SlotPrimitive(Sidebar(slotTag()))
class ScSkeleton extends SlotPrimitive(Skeleton(slotTag()))
class ScSwitch extends SlotPrimitive(Switch(slotTag()))
class ScTable extends SlotPrimitive(Table(slotTag()))
class ScTabs extends SlotPrimitive(Tabs(slotTag()))
class ScTextarea extends SlotPrimitive(Textarea(slotTag()))
class ScToast extends SlotPrimitive(Toast(Toast.Variant.Default, slotTag()))
class ScTooltip extends SlotPrimitive(Tooltip("", slotTag()))

object ScPrimitives:
  def register(): Unit =
    register("sc-alert", js.constructorOf[ScAlert])
    register("sc-avatar", js.constructorOf[ScAvatar])
    register("sc-breadcrumb", js.constructorOf[ScBreadcrumb])
    register("sc-button-group", js.constructorOf[ScButtonGroup])
    register("sc-card", js.constructorOf[ScCard])
    register("sc-checkbox", js.constructorOf[ScCheckbox])
    register("sc-collapsible", js.constructorOf[ScCollapsible])
    register("sc-combobox", js.constructorOf[ScCombobox])
    register("sc-command", js.constructorOf[ScCommand])
    register("sc-chart", js.constructorOf[ScChart])
    register("sc-empty", js.constructorOf[ScEmpty])
    register("sc-field", js.constructorOf[ScField])
    register("sc-form", js.constructorOf[ScForm])
    register("sc-input-group", js.constructorOf[ScInputGroup])
    register("sc-input", js.constructorOf[ScInput])
    register("sc-item", js.constructorOf[ScItem])
    register("sc-kbd", js.constructorOf[ScKbd])
    register("sc-label", js.constructorOf[ScLabel])
    register("sc-native-select", js.constructorOf[ScNativeSelect])
    register("sc-popover", js.constructorOf[ScPopover])
    register("sc-progress", js.constructorOf[ScProgress])
    register("sc-radio", js.constructorOf[ScRadio])
    register("sc-range", js.constructorOf[ScRange])
    register("sc-scrollbar", js.constructorOf[ScScrollbar])
    register("sc-select", js.constructorOf[ScSelect])
    register("sc-sidebar", js.constructorOf[ScSidebar])
    register("sc-skeleton", js.constructorOf[ScSkeleton])
    register("sc-switch", js.constructorOf[ScSwitch])
    register("sc-table", js.constructorOf[ScTable])
    register("sc-tabs", js.constructorOf[ScTabs])
    register("sc-textarea", js.constructorOf[ScTextarea])
    register("sc-toast", js.constructorOf[ScToast])
    register("sc-tooltip", js.constructorOf[ScTooltip])

  private def register(name: String, constructor: js.Dynamic): Unit =
    dom.window.customElements.define(name, constructor)
