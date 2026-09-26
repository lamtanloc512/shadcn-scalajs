package shadcnscalajs.webcomponents

import org.scalajs.dom

/** Light-DOM layout shell: no shadow root, no adopted sheets. The host itself carries `data-slot` and the same class
  * strings Laminar emits, so pack CSS and ancestor selectors (`group/card`, `has-data-[slot=…]`) work exactly as on the
  * site. Interactive controls stay on [[ScElementBase]].
  *
  * Display is left to the stamped utility classes (and a small page/host stylesheet fallback). Setting `style.display`
  * here would override `grid` / `flex` utilities on card header/footer.
  */
abstract class LightPrimitive(slotName: String, className: String) extends dom.HTMLElement:

  // Stamp structure before the element is inserted so FOUC/` :not(:defined)` hide covers the styled host, not a flash of
  // an unclassed custom element.
  this.setAttribute("data-slot", slotName)
  if className.nonEmpty then
    val existing = Option(this.getAttribute("class")).filter(_.nonEmpty)
    val head = className.split(' ').headOption.getOrElse("")
    if head.isEmpty || !this.classList.contains(head) then
      this.setAttribute("class", existing.fold(className)(c => s"$className $c"))
