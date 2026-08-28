package shadcnscalajs.webcomponents

import com.raquo.laminar.api.L.*
import shadcnscalajs.ui.{Toggle, ToggleGroup}

import scala.scalajs.js

class ScToggleGroup extends ScElementBase:

  private val typeVar = Var("single")
  private val singleVar = Var(Option.empty[String])
  private val multiVar = Var(Set.empty[String])
  private val itemsVar = Var(List.empty[(String, String)])
  private val variantVar = Var(Toggle.Variant.Outline)
  private val sizeVar = Var(Toggle.Size.Sm)
  private val revision = Var(0)

  private val singleEcho = EchoGuard[Option[String]]()
  private val multiEcho = EchoGuard[Set[String]]()

  private def bump(): Unit = revision.update(_ + 1)

  observeAttribute("type")(v => { typeVar.set(v.getOrElse("single")); bump() })
  observeAttribute("value")(v => { applyValue(v.orNull); bump() })
  observeAttribute("items")(v => { itemsVar.set(parseItems(v.orNull)); bump() })
  observeAttribute("variant")(v => { ScToggleGroup.parseVariant(v).foreach(variantVar.set); bump() })
  observeAttribute("size")(v => { ScToggleGroup.parseSize(v).foreach(sizeVar.set); bump() })
  stringProperty("type")
  jsonProperty("value")(v => { applyValue(v); bump() })
  jsonProperty("items")(v => { itemsVar.set(parseItems(v)); bump() })
  stringProperty("variant")
  stringProperty("size")

  mount(
    ScToggleGroup
      .view(typeVar, singleVar, multiVar, itemsVar, variantVar, sizeVar, revision)
      .amend(
        singleVar.signal.changes --> Observer[Option[String]](o =>
          if !singleEcho.isEcho(o) then emit("sc-change", o.orNull)
        ),
        multiVar.signal.changes --> Observer[Set[String]](s =>
          if !multiEcho.isEcho(s) then emit("sc-change", js.Array(s.toSeq*))
        )
      )
  )

  private def applyValue(value: js.Any): Unit =
    if typeVar.now() == "multiple" then
      val next = ScToggleGroup.parseMulti(value)
      multiEcho.wrote(next)
      multiVar.set(next)
    else
      val next = ScToggleGroup.parseSingle(value)
      singleEcho.wrote(next)
      singleVar.set(next)

  private def parseItems(value: js.Any): List[(String, String)] =
    ScElements
      .toArray(value)
      .map(_.toList.map { raw =>
        val itemValue = raw.value.asInstanceOf[String]
        itemValue -> raw.label.asInstanceOf[js.UndefOr[String]].getOrElse(itemValue)
      })
      .getOrElse(Nil)

object ScToggleGroup:
  def register(): Unit =
    ScElements.define(
      "sc-toggle-group",
      js.constructorOf[ScToggleGroup],
      "type",
      "value",
      "items",
      "variant",
      "size"
    )

  private def parseVariant(v: Option[String]): Option[Toggle.Variant] = v.collect {
    case "default" => Toggle.Variant.Default
    case "outline" => Toggle.Variant.Outline
  }

  private def parseSize(v: Option[String]): Option[Toggle.Size] = v.collect {
    case "default" => Toggle.Size.Default
    case "sm"      => Toggle.Size.Sm
    case "lg"      => Toggle.Size.Lg
  }

  private def parseSingle(value: js.Any): Option[String] =
    if value == null then None
    else if js.typeOf(value) == "string" then
      val s = value.asInstanceOf[String]
      if s.isEmpty then None else Some(s)
    else Some(value.toString)

  private def parseMulti(value: js.Any): Set[String] =
    ScElements.toArray(value).map(_.toList.map(_.asInstanceOf[String]).toSet).getOrElse {
      if js.typeOf(value) == "string" then
        val s = value.asInstanceOf[String].trim
        if s.isEmpty then Set.empty
        else if s.startsWith("[") then
          ScElements.toArray(s).map(_.toList.map(_.asInstanceOf[String]).toSet).getOrElse(Set.empty)
        else Set(s)
      else Set.empty
    }

  private def view(
      typeVar: Var[String],
      singleVar: Var[Option[String]],
      multiVar: Var[Set[String]],
      itemsVar: Var[List[(String, String)]],
      variantVar: Var[Toggle.Variant],
      sizeVar: Var[Toggle.Size],
      revision: Var[Int]
  ): HtmlElement =
    div(
      children <-- revision.signal.map { _ =>
        val items = itemsVar.now().map { case (v, l) => ToggleGroup.Item(v, l) }
        val variant = variantVar.now()
        val size = sizeVar.now()
        if typeVar.now() == "multiple" then List(ToggleGroup.multiple(multiVar, variant, size, items*))
        else List(ToggleGroup.single(singleVar, variant, size, items*))
      }
    )
