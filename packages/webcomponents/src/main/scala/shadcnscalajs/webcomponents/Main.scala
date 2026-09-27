package shadcnscalajs.webcomponents

import org.scalajs.dom

/** Registers every Sc* element after the shared stylesheet is installed, so upgrades never paint without styles. */
object Main:
  def main(args: Array[String]): Unit =
    // The site's plain-HTML demo restores its shared theme. External bundles opt out so an unrelated page's
    // localStorage cannot replace the consumer's CSS variables and document attributes.
    if !dom.document.documentElement.hasAttribute("data-sc-no-stored-theme") then ScThemeState.applyStored()
    ScThemeApi.install()
    dom
      .fetch(ScStyles.baseHref)
      .`then`[String](_.text())
      .`then`[Unit] { (css: String) =>
        ScStyles.use(css)
        register()
      }
      .`catch` { _ =>
        // Keep the stylesheet gate: an undefined custom element is safer than an unstyled interactive control.
        ()
      }

  private def register(): Unit =
    ScButton.register()
    ScBadge.register()
    ScAspectRatio.register()
    ScToggle.register()
    ScDialog.register()
    ScAlertDialog.register()
    ScSheet.register()
    ScDrawer.register()
    ScAccordion.register()
    ScDropdownMenu.register()
    ScSelect.register()
    ScCombobox.register()
    ScSlider.register()
    ScToggleGroup.register()
    ScCalendar.register()
    ScDatePicker.register()
    ScRangeCalendar.register()
    ScHoverCard.register()
    ScContextMenu.register()
    ScMenubar.register()
    ScNavigationMenu.register()
    ScResizable.register()
    ScSonner.register()
    ScThemeSwitcher.register()
    ScRadioGroup.register()
    ScSeparator.register()
    ScSpinner.register()
    ScProgress.register()
    ScPagination.register()
    ScInputGroup.register()
    ScInput.register()
    ScTextarea.register()
    ScInputOTP.register()
    ScScrollArea.register()
    ScChart.register()
    ScCarousel.register()
    ScDataTable.register()
    ScTabs.register()
    ScCheckbox.register()
    ScSwitch.register()
    ScItem.register()
    ScSidebar.register()
    ScCard.register()
    ScTableParts.register()
    ScPrimitives.register()
    ScSiteHeader.register()
