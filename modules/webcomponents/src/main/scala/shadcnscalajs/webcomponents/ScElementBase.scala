package shadcnscalajs.webcomponents

import com.raquo.laminar.api.L.*
import com.raquo.laminar.nodes.DetachedRoot
import org.scalajs.dom

import scala.collection.mutable
import scala.scalajs.js

/** Shared base for every `Sc*` custom element: shadow root, shared stylesheet, theme mirroring, attribute observation,
  * and lifecycle. Subclasses must call `mount(...)` at the end of their own constructor.
  */
abstract class ScElementBase extends dom.HTMLElement:

  protected val shadow: dom.ShadowRoot =
    this.attachShadow(js.Dynamic.literal(mode = "open").asInstanceOf[dom.ShadowRootInit])

  ScStyles.adopt(shadow)

  // `.dark` and the style packs are ancestor selectors, which an ancestor outside the shadow boundary can't satisfy, so
  // the document's flags are mirrored onto this wrapper. `display: contents` keeps it out of layout.
  private val themeHost: dom.Element =
    val el = dom.document.createElement("div")
    el.setAttribute("data-slot", "sc-theme-host")
    el.setAttribute("style", "display: contents")
    el

  private var detachedRootOpt: Option[DetachedRoot[HtmlElement]] = None
  private val attributeHandlers = mutable.Map.empty[String, Option[String] => Unit]
  private val echoGuards = mutable.ListBuffer.empty[EchoGuard[?]]

  /** Drops the `sc-change` that a write from outside would otherwise bounce straight back.
    *
    * `sc-change` means "the user changed this", matching the DOM contract, so setting the attribute or property must
    * stay silent. A synchronous flag can't express that: Airstream delivers the resulting change in a *later*
    * transaction, after the caller has already cleared any flag of its own. Two elements kept in sync by a listener
    * then ping-pong forever and blow `Transaction.maxDepth`. So the written value is recorded here and the matching
    * change is swallowed once.
    */
  protected final class EchoGuard[A]:
    echoGuards += this

    private var pending: Option[A] = None

    def wrote(value: A): Unit = pending = Some(value)

    def isEcho(value: A): Boolean =
      val echo = pending.contains(value)
      pending = None
      echo

    private[ScElementBase] def clear(): Unit = pending = None

  protected def mount(rootNode: HtmlElement): Unit =
    val root = renderDetached(rootNode = rootNode, activateNow = false)
    themeHost.appendChild(root.ref)
    shadow.appendChild(themeHost)
    ScTheme.mirror(shadow)
    detachedRootOpt = Some(root)
    // `observeAttribute` seeds each handler during construction, before the emitting subscription exists, so those
    // initial writes leave a guard armed against a change nobody is listening for yet.
    echoGuards.foreach(_.clear())

  protected def observeAttribute(name: String)(onChange: Option[String] => Unit): Unit =
    attributeHandlers(name) = onChange
    onChange(Option(this.getAttribute(name)))

  // Frameworks (React 19, Vue `:prop`, Angular `[prop]`) set a JS property first and only fall back to the attribute
  // for strings, so rich values arrive as `[object Object]` unless the element exposes a real property. `onValue` gets
  // the raw JS value — an array/object straight through, or a JSON string parsed by the caller.
  protected def jsonProperty(name: String)(onValue: js.Any => Unit): Unit =
    var current: js.Any = null
    defineProperty(name, () => current, v => { current = v; onValue(v) })

  // A property that reads and writes the same-named attribute, for the plain string props (variant, size, placeholder).
  protected def stringProperty(name: String): Unit =
    defineProperty(
      name,
      () => this.getAttribute(name),
      v => if v == null then this.removeAttribute(name) else this.setAttribute(name, v.toString)
    )

  // A boolean property backed by the presence of an attribute, e.g. `el.open = true`.
  protected def booleanProperty(name: String): Unit =
    defineProperty(
      name,
      () => this.hasAttribute(name),
      v => if js.DynamicImplicits.truthValue(v.asInstanceOf[js.Dynamic]) then this.setAttribute(name, "") else this.removeAttribute(name)
    )

  private def defineProperty(name: String, get: () => js.Any, set: js.Any => Unit): Unit =
    js.Dynamic.global.Object.defineProperty(
      this,
      name,
      js.Dynamic.literal(
        configurable = true,
        get = (() => get()): js.Function0[js.Any],
        set = ((v: js.Any) => set(v)): js.Function1[js.Any, Unit]
      )
    )

  // `bubbles` + `composed` are both required: React delegates `on*` to the root, and `composed` lets the event leave
  // the shadow tree.
  protected def emit(name: String, detail: js.Any): Unit =
    this.dispatchEvent(
      new dom.CustomEvent(
        name,
        js.Dynamic.literal(detail = detail, bubbles = true, composed = true).asInstanceOf[dom.CustomEventInit]
      )
    )

  def connectedCallback(): Unit = detachedRootOpt.foreach(_.activate())
  def disconnectedCallback(): Unit = detachedRootOpt.foreach(_.deactivate())
  def attributeChangedCallback(name: String, oldValue: js.Any, newValue: js.Any): Unit =
    attributeHandlers.get(name).foreach(_(Option(this.getAttribute(name))))
