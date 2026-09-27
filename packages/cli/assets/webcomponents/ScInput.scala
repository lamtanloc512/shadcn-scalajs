package shadcnscalajs.webcomponents

import com.raquo.laminar.api.L.*
import org.scalajs.dom
import shadcnscalajs.ui.Input

import scala.scalajs.js

class ScInput extends ScElementBase:

  private var reflectingValue = false
  private val inputNode = Input(
    onInput --> Observer[dom.Event] { ev =>
      val current = ev.target.asInstanceOf[dom.html.Input].value
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
    if !reflectingValue then inputNode.ref.asInstanceOf[dom.html.Input].value = v.getOrElse("")
  )
  observeAttribute("placeholder")(v => inputNode.ref.asInstanceOf[dom.html.Input].placeholder = v.getOrElse(""))
  observeAttribute("type")(v => inputNode.ref.asInstanceOf[dom.html.Input].`type` = v.getOrElse("text"))
  observeAttribute("name")(v => inputNode.ref.asInstanceOf[dom.html.Input].name = v.getOrElse(""))
  observeAttribute("disabled")(v => inputNode.ref.asInstanceOf[dom.html.Input].disabled = v.isDefined)

  stringProperty("value")
  stringProperty("placeholder")
  stringProperty("type")
  stringProperty("name")
  booleanProperty("disabled")

  mount(inputNode)

  override def focus(): Unit = inputNode.ref.focus()

  override def connectedCallback(): Unit =
    super.connectedCallback()
    val inGroup = Option(this.parentElement).exists(_.tagName.toLowerCase == "sc-input-group")
    if inGroup then
      if !this.hasAttribute("data-slot") then
        this.setAttribute("data-slot", "input-group-control")
        stampedSlot = true
      if !this.classList.contains("cn-input-group-input") then
        this.classList.add("cn-input-group-input")
        stampedClass = true
      if !this.hasAttribute("data-sc-input-group") then
        this.setAttribute("data-sc-input-group", "")
        stampedMarker = true
      inputNode.ref.setAttribute("data-slot", "input-group-control")
      inputNode.ref.classList.add("cn-input-group-input")
    else
      if stampedSlot && this.getAttribute("data-slot") == "input-group-control" then this.removeAttribute("data-slot")
      if stampedClass then this.classList.remove("cn-input-group-input")
      stampedSlot = false
      stampedClass = false
      if stampedMarker then this.removeAttribute("data-sc-input-group")
      stampedMarker = false
      inputNode.ref.setAttribute("data-slot", "input")
      inputNode.ref.classList.remove("cn-input-group-input")

object ScInput:
  def register(): Unit =
    ScElements.define("sc-input", js.constructorOf[ScInput], "value", "placeholder", "type", "name", "disabled")
