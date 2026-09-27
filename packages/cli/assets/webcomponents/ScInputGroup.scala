package shadcnscalajs.webcomponents

import org.scalajs.dom
import shadcnscalajs.ui.InputGroup

import scala.scalajs.js

/** A light-DOM group: the input/textarea hosts must be direct flex children, not hidden behind a shadow slot. */
class ScInputGroup extends LightPrimitive("input-group", InputGroup.baseClass):
  private val onGroupClick: js.Function1[dom.MouseEvent, Unit] = ev =>
    ev.target match
      case target: dom.Element if target.closest("sc-input, sc-textarea, button, a") == null =>
        querySelector(":scope > [data-slot=input-group-control]") match
          case control: dom.HTMLElement => control.focus()
          case _                        => ()
      case _ => ()

  override def connectedCallback(): Unit =
    super.connectedCallback()
    if !hasAttribute("role") then setAttribute("role", "group")
    addEventListener("click", onGroupClick)

  def disconnectedCallback(): Unit = removeEventListener("click", onGroupClick)

object ScInputGroup:
  def register(): Unit =
    ScElements.define("sc-input-group", js.constructorOf[ScInputGroup])
