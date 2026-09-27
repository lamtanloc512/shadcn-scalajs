package shadcnscalajs.webcomponents

import com.raquo.laminar.api.L.*
import shadcnscalajs.core.Tags.slotTag
import shadcnscalajs.ui.Sheet

import scala.scalajs.js

/** Side panel exported as `<sc-sheet>`. */
class ScSheet extends ScElementBase:

  private val isOpenVar = Var(false)
  private val sideVar = Var(Sheet.Side.Right)
  private val openEcho = new EchoGuard[Boolean]

  observeAttribute("open") { value =>
    val next = value.isDefined
    if isOpenVar.now() != next then
      openEcho.wrote(next)
      isOpenVar.set(next)
  }
  booleanProperty("open")

  observeAttribute("side") { value =>
    val next = value match
      case Some("top")    => Sheet.Side.Top
      case Some("bottom") => Sheet.Side.Bottom
      case Some("left")   => Sheet.Side.Left
      case _              => Sheet.Side.Right
    if sideVar.now() != next then sideVar.set(next)
  }
  stringProperty("side")

  mount(
    div(
      div(onClick --> { _ => isOpenVar.set(true) }, slotTag(nameAttr := "trigger")),
      isOpenVar.signal.changes --> Observer[Boolean] { open =>
        val external = openEcho.isEcho(open)
        if open then
          if !this.hasAttribute("open") then this.setAttribute("open", "")
        else
          if this.hasAttribute("open") then this.removeAttribute("open")
          if !external then emit("sc-close", js.undefined)
      },
      child <-- sideVar.signal.map(side => Sheet(isOpenVar, side)(slotTag()))
    )
  )

object ScSheet:
  def register(): Unit =
    ScElements.define("sc-sheet", js.constructorOf[ScSheet], "open", "side")
