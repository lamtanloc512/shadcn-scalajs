package shadcnscalajs.webcomponents

import org.scalajs.dom
import shadcnscalajs.core.Tags.slotTag
import shadcnscalajs.ui.ScrollArea

import scala.scalajs.js

class ScScrollArea extends ScElementBase:

  private val sizing = dom.document.createElement("style")
  sizing.textContent =
    ":host{display:block}[data-slot=scroll-area],[data-slot=scroll-area-viewport]{width:100%;height:100%}"
  shadow.appendChild(sizing)

  mount(ScrollArea(slotTag()))

object ScScrollArea:
  def register(): Unit =
    ScElements.define("sc-scroll-area", js.constructorOf[ScScrollArea])
