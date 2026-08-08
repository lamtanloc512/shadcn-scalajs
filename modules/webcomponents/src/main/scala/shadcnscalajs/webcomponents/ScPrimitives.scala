package shadcnscalajs.webcomponents

import com.raquo.laminar.api.L.*
import org.scalajs.dom
import shadcnscalajs.core.Tags.slotTag
import shadcnscalajs.ui.*

import scala.scalajs.js

/** Lightweight custom-element exports for the stateless Laminar primitives. */
abstract class SlotPrimitive(node: HtmlElement) extends ScElementBase:
  mount(node)

/** Not a `SlotPrimitive`: `<sc-alert variant="destructive">` has to re-style the root when the attribute changes, so
  * the root is built here from `Alert`'s own exported class strings rather than by calling `Alert(...)` once.
  */
class ScAlert extends ScElementBase:

  private val variantVar = Var(Alert.Variant.Default)

  observeAttribute("variant")(v => ScAlert.parseVariant(v).foreach(variantVar.set))

  mount(
    div(
      dataAttr("slot") := "alert",
      role := "alert",
      cls := Alert.baseClass,
      cls <-- variantVar.signal.map(Alert.variantClass),
      slotTag()
    )
  )

object ScAlert:
  private def parseVariant(v: Option[String]): Option[Alert.Variant] = v.collect {
    case "default"     => Alert.Variant.Default
    case "destructive" => Alert.Variant.Destructive
  }

class ScAvatar extends SlotPrimitive(Avatar(slotTag()))
class ScBreadcrumb extends SlotPrimitive(Breadcrumb(slotTag()))
class ScButtonGroup extends SlotPrimitive(ButtonGroup(slotTag()))
class ScCard extends SlotPrimitive(Card(slotTag()))
class ScCheckbox extends SlotPrimitive(Checkbox(slotTag()))

/** Two-part components take named slots: `<sc-collapsible><span slot="trigger">…</span><div slot="content">…</div>`. */
class ScCollapsible
    extends SlotPrimitive(
      Collapsible(
        Collapsible.trigger(slotTag(nameAttr := "trigger")),
        Collapsible.content(slotTag(nameAttr := "content"))
      )
    )
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
class ScPopover
    extends SlotPrimitive(
      Popover(
        Popover.trigger(slotTag(nameAttr := "trigger")),
        Popover.content(slotTag(nameAttr := "content"))
      )
    )
class ScProgress extends SlotPrimitive(Progress(0, slotTag()))
class ScRadio extends SlotPrimitive(Radio("", slotTag()))
class ScRange extends SlotPrimitive(Range(slotTag()))
class ScScrollbar extends SlotPrimitive(Scrollbar(slotTag()))
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
