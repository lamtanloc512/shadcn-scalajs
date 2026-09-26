package shadcnscalajs.webcomponents

import shadcnscalajs.ui.Card

import scala.scalajs.js

/** `<sc-card>` and compound parts — light-DOM hosts mirroring [[Card]] helpers. */
class ScCard extends LightPrimitive(Card.rootSlot, Card.rootClass):
  this.setAttribute("data-size", "default")

class ScCardHeader extends LightPrimitive(Card.headerSlot, Card.headerClass)
class ScCardTitle extends LightPrimitive(Card.titleSlot, Card.titleClass)
class ScCardDescription extends LightPrimitive(Card.descriptionSlot, Card.descriptionClass)
class ScCardAction extends LightPrimitive(Card.actionSlot, Card.actionClass)
class ScCardContent extends LightPrimitive(Card.contentSlot, Card.contentClass)
class ScCardFooter extends LightPrimitive(Card.footerSlot, Card.footerClass)

object ScCard:
  def register(): Unit =
    ScElements.define("sc-card", js.constructorOf[ScCard])
    ScElements.define("sc-card-header", js.constructorOf[ScCardHeader])
    ScElements.define("sc-card-title", js.constructorOf[ScCardTitle])
    ScElements.define("sc-card-description", js.constructorOf[ScCardDescription])
    ScElements.define("sc-card-action", js.constructorOf[ScCardAction])
    ScElements.define("sc-card-content", js.constructorOf[ScCardContent])
    ScElements.define("sc-card-footer", js.constructorOf[ScCardFooter])
