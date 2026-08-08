package shadcnscalajs.webcomponents

import org.scalajs.dom

/** Fetches the Tailwind-compiled CSS bundle (built from `src/styles/globals.css`) co-located as `sc-components.css`, so
  * every Sc* custom element's shadow root gets the full Tailwind design tokens + utility classes injected.
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
        ScSelect.register()
        ScCombobox.register()
        ScPrimitives.register()
      }
