package shadcnscalajs.webcomponents

import com.raquo.laminar.api.L.*
import shadcnscalajs.core.Tags.slotTag
import shadcnscalajs.ui.Item

import scala.scalajs.js

class ScItem extends ScElementBase:

  private val variantVar = Var(Item.Variant.Default)
  private val sizeVar = Var(Item.Size.Default)

  observeAttribute("variant")(v => ScItem.parseVariant(v).foreach(variantVar.set))
  observeAttribute("size")(v => ScItem.parseSize(v).foreach(sizeVar.set))
  stringProperty("variant")
  stringProperty("size")

  mount(
    div(
      dataAttr("slot") := "item",
      cls := Item.baseClass,
      cls <-- variantVar.signal.map(Item.variantClass),
      cls <-- sizeVar.signal.map(Item.sizeClass),
      dataAttr("variant") <-- variantVar.signal.map(_.toString.toLowerCase),
      dataAttr("size") <-- sizeVar.signal.map(_.toString.toLowerCase),
      slotTag()
    )
  )

object ScItem:
  def register(): Unit =
    ScElements.define("sc-item", js.constructorOf[ScItem], "variant", "size")

  private def parseVariant(v: Option[String]): Option[Item.Variant] = v.collect {
    case "default" => Item.Variant.Default
    case "outline" => Item.Variant.Outline
    case "muted"   => Item.Variant.Muted
  }

  private def parseSize(v: Option[String]): Option[Item.Size] = v.collect {
    case "default" => Item.Size.Default
    case "sm"      => Item.Size.Sm
    case "xs"      => Item.Size.Xs
  }
