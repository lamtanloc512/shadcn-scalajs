package shadcnscalajs.webcomponents

import org.scalajs.dom

import scala.scalajs.js

/** Registers a custom element and declares the attributes it reacts to.
  *
  * `observedAttributes` has to sit on the constructor's static side for `attributeChangedCallback` to fire; a Scala.js
  * class has no static side, so it is set on the constructor object here before defining. Guarded so loading the bundle
  * twice does not throw.
  */
object ScElements:

  def define(name: String, constructor: js.Dynamic, observedAttributes: String*): Unit =
    val registry = dom.window.customElements.asInstanceOf[js.Dynamic]
    if js.isUndefined(registry.get(name)) then
      constructor.updateDynamic("observedAttributes")(js.Array(observedAttributes*))
      dom.window.customElements.define(name, constructor)

  /** Customized built-in (`<tr is="sc-table-row">`). `extendsTag` is the native tag the class subclasses. */
  def defineBuiltin(name: String, constructor: js.Dynamic, extendsTag: String): Unit =
    val registry = dom.window.customElements.asInstanceOf[js.Dynamic]
    if js.isUndefined(registry.get(name)) then
      registry.define(name, constructor, js.Dictionary("extends" -> extendsTag))

  /** Normalizes a JSON attribute string or a JS array property into an array of dynamics, for the components whose data
    * can arrive either way.
    */
  def toArray(value: js.Any): Option[js.Array[js.Dynamic]] =
    if value == null then None
    else if js.Array.isArray(value) then Some(value.asInstanceOf[js.Array[js.Dynamic]])
    else if js.typeOf(value) == "string" then
      try Some(js.JSON.parse(value.asInstanceOf[String]).asInstanceOf[js.Array[js.Dynamic]])
      catch case _: Throwable => None
    else None
