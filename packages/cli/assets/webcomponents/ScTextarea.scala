package shadcnscalajs.webcomponents

import com.raquo.laminar.api.L.*
import org.scalajs.dom
import shadcnscalajs.ui.Textarea

import scala.scalajs.js

class ScTextarea extends ScElementBase:

  private var reflectingValue = false
  private val field = Textarea(
    onInput --> Observer[dom.Event] { ev =>
      val current = ev.target.asInstanceOf[dom.html.TextArea].value
      if this.getAttribute("value") != current then
        reflectingValue = true
        try this.setAttribute("value", current)
        finally reflectingValue = false
      emit("sc-change", current)
    }
  )

  private var stampedSlot = false
  private var stampedClass = false
  private var stampedMarker = false

  observeAttribute("value")(v =>
    if !reflectingValue then field.ref.asInstanceOf[dom.html.TextArea].value = v.getOrElse("")
  )
  observeAttribute("placeholder")(v => field.ref.asInstanceOf[dom.html.TextArea].placeholder = v.getOrElse(""))
  observeAttribute("name")(v => field.ref.asInstanceOf[dom.html.TextArea].name = v.getOrElse(""))
  observeAttribute("disabled")(v => field.ref.asInstanceOf[dom.html.TextArea].disabled = v.isDefined)

  stringProperty("value")
  stringProperty("placeholder")
  stringProperty("name")
  booleanProperty("disabled")

  mount(field)

  override def focus(): Unit = field.ref.focus()

  override def connectedCallback(): Unit =
    super.connectedCallback()
    val inGroup = Option(this.parentElement).exists(_.tagName.toLowerCase == "sc-input-group")
    if inGroup then
      if !this.hasAttribute("data-slot") then
        this.setAttribute("data-slot", "input-group-control")
        stampedSlot = true
      if !this.classList.contains("cn-input-group-textarea") then
        this.classList.add("cn-input-group-textarea")
        stampedClass = true
      if !this.hasAttribute("data-sc-input-group") then
        this.setAttribute("data-sc-input-group", "")
        stampedMarker = true
      field.ref.setAttribute("data-slot", "input-group-control")
      field.ref.classList.add("cn-input-group-textarea")
    else
      if stampedSlot && this.getAttribute("data-slot") == "input-group-control" then this.removeAttribute("data-slot")
      if stampedClass then this.classList.remove("cn-input-group-textarea")
      stampedSlot = false
      stampedClass = false
      if stampedMarker then this.removeAttribute("data-sc-input-group")
      stampedMarker = false
      field.ref.setAttribute("data-slot", "textarea")
      field.ref.classList.remove("cn-input-group-textarea")

object ScTextarea:
  def register(): Unit =
    ScElements.define("sc-textarea", js.constructorOf[ScTextarea], "value", "placeholder", "name", "disabled")
