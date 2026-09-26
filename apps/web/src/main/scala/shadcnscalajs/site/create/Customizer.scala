package shadcnscalajs.site.create

import com.raquo.laminar.api.L.*
import shadcnscalajs.ui.{Card, Field, Separator}

/** Create-page customizer column — rich pickers, per-field locks, footer actions, and sponsor slots. */
object Customizer:

  /** Every customizer field, in create-page order — shared with the site header's theme menu so both surfaces edit the
    * same config through the same controls.
    */
  def fields(state: CreateState, mods: Modifier[HtmlElement]*): HtmlElement =
    val lastSolidMenuAccentVar = Var {
      val cfg = state.config.now()
      if cfg.menuColor == "default-translucent" || cfg.menuColor == "inverted-translucent" then Preset.MenuAccents.head
      else cfg.menuAccent
    }

    div(
      cls := "flex min-w-0 flex-col",
      mods,
      Field.group(cls := "flex-row gap-2.5 p-3 md:flex-col md:gap-3.25", Pickers.stylePicker(state)),
      Separator(Separator.Orientation.Horizontal),
      Field.group(
        cls := "flex-row gap-2.5 p-3 md:flex-col md:gap-3.25",
        Pickers.baseColorPicker(state),
        Pickers.themePicker(state),
        Pickers.chartColorPicker(state)
      ),
      Separator(Separator.Orientation.Horizontal),
      Field.group(
        cls := "flex-row gap-2.5 p-3 md:flex-col md:gap-3.25",
        Pickers.fontPicker(state, "Heading", "fontHeading", Preset.FontHeadings),
        Pickers.fontPicker(state, "Font", "font", Preset.Fonts)
      ),
      Separator(Separator.Orientation.Horizontal),
      Field.group(
        cls := "flex-row gap-2.5 p-3 md:flex-col md:gap-3.25",
        Pickers.iconLibraryPicker(state),
        Pickers.radiusPicker(state)
      ),
      Separator(Separator.Orientation.Horizontal),
      Field.group(
        cls := "flex-row gap-2.5 p-3 md:flex-col md:gap-3.25",
        Pickers.menuColorPicker(state, lastSolidMenuAccentVar),
        Pickers.menuAccentPicker(state)
      )
    )

  def apply(state: CreateState): HtmlElement =
    val initDialog = InitializeDialog(state)

    div(
      // Marks this column as chrome rather than themed content: globals.css exempts it from the
      // style-pack card reset so the panel keeps its own elevation and the sponsor cards their border.
      dataAttr("slot") := "customizer",
      cls := "top-24 right-12 isolate z-10 flex min-h-0 w-full flex-col gap-2 self-start md:w-(--customizer-width)",
      Card(
        dataAttr("size") := "sm",
        cls := "dark max-h-(--preview-height) min-h-0 w-full gap-0 rounded-2xl bg-card/90 shadow-xl backdrop-blur-xl",
        Card.header(
          cls := "hidden items-center justify-between gap-2 border-b px-3! group-data-reversed/layout:flex-row-reverse md:flex",
          MainMenu(state)
        ),
        Card.content(
          cls := "no-scrollbar min-h-0 flex-1 overflow-x-auto overflow-y-hidden p-0 md:overflow-y-auto",
          fields(state)
        ),
        Card.footer(
          cls := "flex min-w-0 gap-2 px-3! md:flex-col md:**:[button,a]:w-full",
          CopyPreset(state, cls := "flex-1 md:flex-none"),
          OpenPreset(state, buttonLabel = "Open", cls := "max-w-20 min-w-0 flex-1 sm:max-w-none md:flex-none"),
          MainMenu.shuffleButton(state)
        ),
        Card.footer(
          cls := "flex min-w-0 gap-2 px-3! pt-2 md:flex-col md:**:[button,a]:w-full",
          initDialog.trigger(cls := "md:w-full")
        )
      ),
      div(
        cls := "hidden w-full flex-1 flex-col gap-12 md:flex",
        sponsorCard("Build faster with shadcn-scalajs", "Copy components into your Scala.js project with the CLI.")
      ),
      div(
        cls := "hidden flex-col gap-12 md:flex",
        sponsorCard("Open source", "Star the repo and share feedback — every preset code is portable.")
      ),
      initDialog.panel
    )

  private def sponsorCard(headingText: String, bodyText: String): HtmlElement =
    Card(
      cls := "gap-3 rounded-xl border border-foreground/10 bg-muted/30 p-4 shadow-none",
      Card.title(cls := "text-sm font-semibold", headingText),
      Card.description(bodyText)
    )
