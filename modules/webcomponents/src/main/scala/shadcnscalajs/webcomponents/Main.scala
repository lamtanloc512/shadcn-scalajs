package shadcnscalajs.webcomponents

import org.scalajs.dom

/** Fetches the vendored basecoat CSS bundle (co-located `sc-components.css`, relative to the page loading this script —
  * see vendor/NOTICE.md for provenance), then registers every `Sc*` custom element once it's loaded, so the very first
  * element upgrade already has `ScElementBase.styleSheetText` set and renders styled from the start.
  */
object Main:
  def main(args: Array[String]): Unit =
    dom
      .fetch("./sc-components.css")
      .`then`[String](_.text())
      .`then` { (css: String) =>
        ScElementBase.styleSheetText = Some(css)
        ScButton.register()
        ScBadge.register()
        ScDialog.register()
        ScAccordion.register()
        ScDropdownMenu.register()
      }
