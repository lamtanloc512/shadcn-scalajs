package shadcnscalajs.webcomponents

import com.raquo.laminar.api.L.*
import shadcnscalajs.core.Tags.slotTag
import shadcnscalajs.ui.HoverCard

import scala.scalajs.js

/** Slot-based hover card using HoverCard's default placement and hover delays. */
class ScHoverCard extends ScElementBase:
  mount(
    HoverCard(
      HoverCard.trigger(slotTag(nameAttr := "trigger")),
      HoverCard.content(slotTag(nameAttr := "content"))
    )
  )

object ScHoverCard:
  def register(): Unit =
    ScElements.define("sc-hover-card", js.constructorOf[ScHoverCard])
