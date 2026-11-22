package shadcnscalajs.webcomponents

import com.raquo.laminar.api.L.*
import shadcnscalajs.core.Tags.slotTag
import shadcnscalajs.ui.{Button, MessageScroller}

import scala.scalajs.js

/** `<sc-message-scroller>` — a chat viewport that stays pinned to the newest message.
  *
  * Messages are the element's children, slotted into the scrolling viewport, so they keep the page's styles (and the
  * light-DOM `sc-message` / `sc-bubble` hosts work inside it). Give the element a height. `button-variant` picks the
  * jump button's variant; `el.scrollToEnd()` scrolls programmatically. The host reflects `data-at-end` and emits
  * `sc-change` with `{ atEnd }` whenever that flips.
  *
  * {{{
  * <sc-message-scroller style="height: 20rem">
  *   <sc-message>…</sc-message>
  * </sc-message-scroller>
  * }}}
  */
class ScMessageScroller extends ScElementBase:

  private val scroller = MessageScroller.ctx()
  private val variantVar = Var(Button.Variant.Secondary)

  observeAttribute("button-variant")(v =>
    variantVar.set(v match
      case Some("outline") => Button.Variant.Outline
      case Some("default") => Button.Variant.Primary
      case Some("ghost")   => Button.Variant.Ghost
      case _               => Button.Variant.Secondary)
  )
  stringProperty("button-variant")

  this.asInstanceOf[js.Dynamic].scrollToEnd = (() => scroller.scrollToEnd()): js.Function0[Unit]
  this.asInstanceOf[js.Dynamic].scrollToStart =
    (() => scroller.scrollTo(MessageScroller.Direction.Start)): js.Function0[Unit]

  mount(
    MessageScroller.root(scroller)(
      scroller.atEnd.signal.changes --> { atEnd =>
        if atEnd then this.setAttribute("data-at-end", "") else this.removeAttribute("data-at-end")
        emit("sc-change", js.Dynamic.literal(atEnd = atEnd))
      },
      MessageScroller.viewport(scroller)(
        cls := "p-4",
        MessageScroller.content(slotTag())
      ),
      child <-- variantVar.signal.map(variant => MessageScroller.button(scroller, variant = variant)())
    )
  )

/** Optional item wrapper: `content-visibility: auto` so long histories only lay out what is on screen. */
class ScMessageScrollerItem
    extends LightPrimitive(
      "message-scroller-item",
      "block min-w-0 shrink-0 [contain-intrinsic-size:auto_10rem] [content-visibility:auto]"
    )

object ScMessageScroller:
  def register(): Unit =
    ScElements.define("sc-message-scroller", js.constructorOf[ScMessageScroller], "button-variant")
    ScElements.define("sc-message-scroller-item", js.constructorOf[ScMessageScrollerItem])
