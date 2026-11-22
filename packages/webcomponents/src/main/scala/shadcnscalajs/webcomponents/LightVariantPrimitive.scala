package shadcnscalajs.webcomponents

import org.scalajs.dom

import scala.scalajs.js

/** A light-DOM host whose classes and `data-*` attributes follow its own attributes — `variant`, `align`, `state`.
  *
  * Like [[LightPrimitive]] the host *is* the styled element, so upstream's parent/child selectors (`*:data-[slot=…]`,
  * `group-data-[align=end]/message:`) see the page's own children. Only the tokens this host stamped are swapped on a
  * change; classes the page put on the element survive.
  *
  * Stamping waits for `connectedCallback` / `attributeChangedCallback`: both run in the same upgrade step, before
  * paint, and neither trips the "a custom element constructor must not add attributes" rule `createElement` enforces.
  */
abstract class LightVariantPrimitive(slotName: String) extends dom.HTMLElement:

  /** Class list for the current attributes. */
  protected def classes(attr: String => Option[String]): String

  /** `data-*` (or other) attributes to mirror, e.g. `"data-align" -> "end"`. */
  protected def stamps(attr: String => Option[String]): Seq[(String, String)] = Nil

  private var stamped: Seq[String] = Nil

  private def attr(name: String): Option[String] = Option(this.getAttribute(name)).filter(_.nonEmpty)

  protected def sync(): Unit =
    if this.getAttribute("data-slot") != slotName then this.setAttribute("data-slot", slotName)
    val next = classes(attr).split(' ').filter(_.nonEmpty).toSeq
    stamped.filterNot(next.contains).foreach(this.classList.remove)
    next.foreach(token => if !this.classList.contains(token) then this.classList.add(token))
    stamped = next
    stamps(attr).foreach { (name, value) =>
      if this.getAttribute(name) != value then this.setAttribute(name, value)
    }

  def connectedCallback(): Unit =
    this.setAttribute("data-sc-component", "")
    sync()

  def attributeChangedCallback(name: String, oldValue: js.Any, newValue: js.Any): Unit =
    if oldValue != newValue then sync()

object LightVariantPrimitive:
  /** Picks the option whose attribute value matches, else the default. */
  def one[A](raw: Option[String], options: (String, A)*)(default: A): A =
    raw.flatMap(value => options.collectFirst { case (`value`, a) => a }).getOrElse(default)
