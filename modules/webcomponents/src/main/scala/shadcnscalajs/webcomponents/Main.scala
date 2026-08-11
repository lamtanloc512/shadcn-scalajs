package shadcnscalajs.webcomponents

import org.scalajs.dom

/** Registers every Sc* element after the shared stylesheet is installed, so upgrades never paint without styles. */
object Main:
  def main(args: Array[String]): Unit =
    dom
      .fetch("./sc-components.css")
      .`then`[String](_.text())
      .`then`[Unit] { (css: String) =>
        ScStyles.use(css)
        register()
      }

  private def register(): Unit =
    ScButton.register()
    ScBadge.register()
    ScDialog.register()
    ScAccordion.register()
    ScDropdownMenu.register()
    ScSelect.register()
    ScCombobox.register()
    ScSlider.register()
    ScToggleGroup.register()
    ScCalendar.register()
    ScRadioGroup.register()
    ScSeparator.register()
    ScSpinner.register()
    ScProgress.register()
    ScChart.register()
    ScTabs.register()
    ScCheckbox.register()
    ScSwitch.register()
    ScItem.register()
    ScSidebar.register()
    ScCard.register()
    ScTableParts.register()
    ScPrimitives.register()
    ScSiteHeader.register()
