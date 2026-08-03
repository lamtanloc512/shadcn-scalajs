package shadcnscalajs.ui

import com.raquo.laminar.api.L.*
import com.raquo.laminar.codecs.StringAsIsCodec
import org.scalajs.dom
import shadcnscalajs.core.CommonAttrs.dataPopover
import shadcnscalajs.core.DataAttrs.dataVariant

import scala.scalajs.js

/** Hand-rolled reactive port of basecoat's dropdown-menu.js state machine (basecoat/src/js/dropdown-menu.js: roving
  * `aria-activedescendant`, `aria-expanded` open/close, click-outside-closes) onto Airstream Var/EventBus. There is no
  * Radix-equivalent primitives library for Laminar, so this tier of component reimplements the behavior directly,
  * following the Var idioms used in laminar-full-stack-demo's CounterView / FormStateView instead of imperative DOM
  * manipulation.
  */
object DropdownMenu:

  final case class Item(label: String, onSelect: () => Unit, disabled: Boolean = false)

  private var idCounter = 0
  private def nextId(): String = { idCounter += 1; s"dropdown-menu-$idCounter" }

  def apply(trigger: Modifier[HtmlElement]*)(items: Item*): HtmlElement =
    val isOpenVar = Var(false)
    val activeIndexVar = Var(firstEnabledIndex(items, 0))
    val menuId = nextId()

    def itemDomId(idx: Int): String = s"$menuId-item-$idx"

    def enabledIndices: Seq[Int] = items.zipWithIndex.collect { case (item, idx) if !item.disabled => idx }

    def moveActive(delta: Int): Unit =
      val indices = enabledIndices
      if indices.nonEmpty then
        val currentPos = math.max(indices.indexOf(activeIndexVar.now()), 0)
        val nextPos = (((currentPos + delta) % indices.size) + indices.size) % indices.size
        activeIndexVar.set(indices(nextPos))

    def select(idx: Int): Unit =
      items.lift(idx).filterNot(_.disabled).foreach { item =>
        item.onSelect()
        isOpenVar.set(false)
      }

    div(
      cls := "dropdown-menu",
      position := "relative",
      onMountBind { ctx =>
        // Click-outside-closes, matching basecoat's document-level popover
        // coordination (dropdown-menu.js:59,163-169), reimplemented via
        // Laminar's documentEvents helper instead of a manual addEventListener.
        // Uses composedPath() rather than ev.target: inside a Shadow DOM
        // (e.g. the ScDropdownMenu web-component wrapper), a document-level
        // listener sees ev.target retargeted to the shadow host, so a plain
        // `.contains(ev.target)` check misidentifies every click inside the
        // menu's own shadow tree as an "outside" click (found via manual
        // browser testing — it silently ate every item selection).
        // composedPath() reports the real originating node regardless of
        // shadow boundaries, so this check is correct in both contexts.
        documentEvents(_.onMouseDown) --> { (ev: dom.MouseEvent) =>
          if isOpenVar.now() && DropdownMenu.composedPath(ev).indexOf(ctx.thisNode.ref) == -1 then isOpenVar.set(false)
        }
      },
      button(
        typ := "button",
        cls := "btn",
        dataVariant := "outline",
        aria.hasPopup := true,
        aria.expanded <-- isOpenVar.signal,
        aria.activeDescendant <-- activeIndexVar.signal.map(itemDomId),
        trigger,
        onClick --> { _ => isOpenVar.update(!_) },
        onKeyDown --> { (ev: dom.KeyboardEvent) =>
          ev.key match
            case "ArrowDown" =>
              ev.preventDefault()
              if !isOpenVar.now() then isOpenVar.set(true) else moveActive(1)
            case "ArrowUp" =>
              ev.preventDefault()
              if !isOpenVar.now() then isOpenVar.set(true) else moveActive(-1)
            case "Enter" | " " if isOpenVar.now() =>
              ev.preventDefault()
              select(activeIndexVar.now())
            case "Escape" =>
              isOpenVar.set(false)
            case _ => ()
        }
      ),
      div(
        dataPopover := true,
        idAttr := menuId,
        aria.hidden <-- isOpenVar.signal.map(!_),
        display <-- isOpenVar.signal.map(open => if open then "block" else "none"),
        div(
          role := "menu",
          children <-- activeIndexVar.signal.map { activeIdx =>
            items.zipWithIndex.map { case (item, idx) => renderItem(item, idx, activeIdx, itemDomId, select) }.toList
          }
        )
      )
    )

  // Not typed in the pinned scalajs-dom facade (2.8.0); a thin escape hatch
  // onto the real native Event.prototype.composedPath().
  private def composedPath(ev: dom.Event): js.Array[dom.EventTarget] =
    ev.asInstanceOf[js.Dynamic].composedPath().asInstanceOf[js.Array[dom.EventTarget]]

  private def firstEnabledIndex(items: Seq[Item], from: Int): Int =
    items.zipWithIndex.collectFirst { case (item, idx) if !item.disabled => idx }.getOrElse(from)

  private def renderItem(
      item: Item,
      idx: Int,
      activeIdx: Int,
      itemDomId: Int => String,
      select: Int => Unit
  ): HtmlElement =
    div(
      idAttr := itemDomId(idx),
      role := "menuitem",
      cls := Map("active" -> (idx == activeIdx)),
      aria.disabled := item.disabled,
      item.label,
      onClick --> { _ => select(idx) }
    )
