package shadcnscalajs.core

import com.raquo.laminar.api.L.*
import com.raquo.laminar.tags.HtmlTag
import org.scalajs.dom

object Tags:

  /** Not predefined in Laminar's HtmlTags. Used by webcomponents wrappers to
    * project a custom element's light-DOM children into its shadow tree.
    */
  val slotTag: HtmlTag[dom.HTMLElement] = htmlTag("slot")
