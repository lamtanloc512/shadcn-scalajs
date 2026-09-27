package shadcnscalajs.webcomponents

import com.raquo.laminar.api.L.*
import shadcnscalajs.core.Tags.slotTag
import shadcnscalajs.ui.Drawer

import scala.scalajs.js

/** `<sc-drawer open direction="bottom">...</sc-drawer>`. The native-dialog drawer does not support swipe-to-dismiss or
  * snap points.
  */
class ScDrawer extends ScElementBase:

  private val isOpenVar = Var(false)
  private val directionVar = Var(Drawer.Direction.Bottom)
  private val openEcho = EchoGuard[Boolean]()

  observeAttribute("open") { value =>
    val next = value.isDefined
    if isOpenVar.now() != next then
      openEcho.wrote(next)
      isOpenVar.set(next)
  }
  booleanProperty("open")

  observeAttribute("direction") { value =>
    val next = value match
      case Some("top")   => Drawer.Direction.Top
      case Some("right") => Drawer.Direction.Right
      case Some("left")  => Drawer.Direction.Left
      case _             => Drawer.Direction.Bottom
    if directionVar.now() != next then directionVar.set(next)
  }
  stringProperty("direction")

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
      child <-- directionVar.signal.map(direction => Drawer(isOpenVar, direction)(slotTag()))
    )
  )

object ScDrawer:
  def register(): Unit =
    ScElements.define("sc-drawer", js.constructorOf[ScDrawer], "open", "direction")
