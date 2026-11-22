package shadcnscalajs.ui

import com.raquo.laminar.api.L
import com.raquo.laminar.api.L.*

/** shadcn/ui Direction — sets the text direction for a subtree.
  *
  * Upstream wraps Radix's `DirectionProvider` so primitives read `ltr`/`rtl` from React context. Laminar has no
  * context, and none is needed: the components use logical utilities (`ms-*`, `ps-*`, `start-*`, `rtl:`) that follow
  * the DOM's own `dir` attribute. A `provider` is therefore just an element carrying `dir`, and [[current]] reads the
  * direction that is in effect for any mounted element.
  */
object Direction:

  enum Dir derives CanEqual:
    case Ltr, Rtl

  def name(dir: Dir): String = dir.toString.toLowerCase

  /** Wraps children in a `div` with `dir` set. Use `display: contents` styling via `cls` if the wrapper must not box.
    */
  def provider(dir: Dir)(mods: Modifier[HtmlElement]*): HtmlElement =
    div(dataAttr("slot") := "direction-provider", L.dir := name(dir), mods)

  /** The same, driven by a signal, for a toggle that flips a live preview. */
  def provider(dir: Signal[Dir])(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "direction-provider",
      L.dir <-- dir.map(name),
      mods
    )

  /** Upstream's `useDirection()`: the computed direction of a mounted element. */
  def current(el: org.scalajs.dom.Element): Dir =
    if org.scalajs.dom.window.getComputedStyle(el).direction == "rtl" then Dir.Rtl else Dir.Ltr
