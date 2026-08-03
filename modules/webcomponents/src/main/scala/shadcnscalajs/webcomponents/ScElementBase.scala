package shadcnscalajs.webcomponents

import com.raquo.laminar.api.L.*
import com.raquo.laminar.nodes.DetachedRoot
import org.scalajs.dom

import scala.scalajs.js

/** Shared base class for every `Sc*` custom-element wrapper (ScButton,
  * ScDialog, ...), so the registration/Shadow-DOM/CSS/lifecycle boilerplate
  * is written once. The HTMLElement-subclass + customElements.define +
  * Shadow DOM + Laminar `renderDetached` chain this relies on was validated
  * against a real browser by the Phase 0 spike (see ScHello.scala).
  *
  * Subclasses must call `mount(...)` at the end of their own constructor,
  * after their own `Var`s etc. are initialized — `mount` can't be called
  * from this base class's own constructor because the subclass's fields
  * aren't initialized yet at that point (standard superclass-constructor-
  * runs-first ordering).
  */
abstract class ScElementBase extends dom.HTMLElement:

  protected val shadow: dom.ShadowRoot =
    this.attachShadow(js.Dynamic.literal(mode = "open").asInstanceOf[dom.ShadowRootInit])

  ScElementBase.styleSheetText.foreach { css =>
    val styleEl = dom.document.createElement("style")
    styleEl.textContent = css
    shadow.appendChild(styleEl)
  }

  private var detachedRootOpt: Option[DetachedRoot[HtmlElement]] = None

  protected def mount(rootNode: HtmlElement): Unit =
    val root = renderDetached(rootNode = rootNode, activateNow = false)
    shadow.appendChild(root.ref)
    detachedRootOpt = Some(root)

  /** Reads `name`'s current value immediately, then re-invokes `onChange`
    * every time the attribute is mutated — a MutationObserver-based stand-in
    * for the spec's static `observedAttributes` + `attributeChangedCallback`,
    * which Scala.js has no clean way to express (a Scala.js-defined class has
    * no "static side" the way a plain ES2015 class does).
    */
  protected def observeAttribute(name: String)(onChange: Option[String] => Unit): Unit =
    onChange(Option(this.getAttribute(name)))
    val observer = new dom.MutationObserver((records, _) =>
      records.foreach { record =>
        if record.attributeName == name then onChange(Option(this.getAttribute(name)))
      }
    )
    observer.observe(
      this,
      new dom.MutationObserverInit {
        attributes = true
        attributeFilter = js.Array(name)
      }
    )

  // Custom element lifecycle callbacks — names are mandated by the spec.
  def connectedCallback(): Unit = detachedRootOpt.foreach(_.activate())
  def disconnectedCallback(): Unit = detachedRootOpt.foreach(_.deactivate())

object ScElementBase:
  /** Vendored basecoat CDN CSS text, set once at app startup (see
    * modules/site's bootstrap) and shared by every instance's injected
    * `<style>` tag. A plain per-shadow-root `<style>` rather than
    * `adoptedStyleSheets`, since the pinned scalajs-dom facade (2.8.0)
    * doesn't type the Constructable Stylesheets API — see the implementation
    * plan's Web Component export layer section for this decision.
    */
  var styleSheetText: Option[String] = None
