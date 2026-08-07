package shadcnscalajs.site.create

import com.raquo.laminar.api.L.*
import com.raquo.laminar.api.L.svg.{path as svgPath, svg as svgTag}
import shadcnscalajs.ui.{Icons, Item}
import shadcnscalajs.ui.icons.{HugeiconsIconData, LucideIconData, PhosphorIconData, RemixIconData, TablerIconData}

/** All create-page field pickers — style, colors, fonts, icon library, radius, and menu options. */
object Pickers:

  private val baseColorNames: Set[String] = Preset.BaseColors.map(_._1).toSet

  private val styleEntries: List[(String, String, String)] = List(
    ("nova", "Nova", "Reduced padding and margins for compact layouts."),
    ("vega", "Vega", "The classic shadcn/ui look. Clean, neutral, and familiar."),
    ("maia", "Maia", "Soft and rounded, with generous spacing."),
    ("lyra", "Lyra", "Boxy and sharp. Pairs well with mono fonts."),
    ("mira", "Mira", "Compact. Made for dense interfaces."),
    ("luma", "Luma", "Rounded geometry. Soft elevation. Breathable layouts."),
    ("sera", "Sera", "Editorial and typographic."),
    ("rhea", "Rhea", "Like Luma but compact.")
  )

  private val menuColorOptions: List[(String, String)] = List(
    "default" -> "Default / Solid",
    "default-translucent" -> "Default / Translucent",
    "inverted" -> "Inverted / Solid",
    "inverted-translucent" -> "Inverted / Translucent"
  )

  private val menuAccentLabels: Map[String, String] = Map(
    "subtle" -> "Subtle",
    "bold" -> "Bold"
  )

  private val iconPreviewConcepts: List[String] = List(
    "copy",
    "alert-circle",
    "more-horizontal",
    "plus",
    "chevron-down",
    "chevron-right",
    "check",
    "arrow-right",
    "shopping-cart",
    "refresh-cw",
    "arrow-left-right"
  )

  private def formatName(raw: String): String =
    raw.split("-").map(part => part.headOption.map(_.toUpper + part.tail).getOrElse("")).mkString(" ")

  private def themeSwatchHex(themeName: String): String =
    if baseColorNames.contains(themeName) then Preset.BaseColors.find(_._1 == themeName).map(_._2).getOrElse("#737373")
    else Preset.Themes.find(_._1 == themeName).map(_._2).getOrElse("#737373")

  private def isTranslucentMenuColor(menuColor: String): Boolean =
    menuColor == "default-translucent" || menuColor == "inverted-translucent"

  private def menuColorChoice(menuColor: String): String =
    if menuColor == "inverted" || menuColor == "inverted-translucent" then "inverted" else "default"

  private def menuSurfaceChoice(menuColor: String): String =
    if isTranslucentMenuColor(menuColor) then "translucent" else "solid"

  private def menuColorFromChoices(colorChoice: String, surfaceChoice: String): String =
    (colorChoice, surfaceChoice) match
      case ("default", "solid")       => "default"
      case ("default", "translucent") => "default-translucent"
      case ("inverted", "solid")      => "inverted"
      case _                          => "inverted-translucent"

  private def iconSource(library: String) = library match
    case "tabler"    => TablerIconData
    case "hugeicons" => HugeiconsIconData
    case "phosphor"  => PhosphorIconData
    case "remixicon" => RemixIconData
    case _           => LucideIconData

  private def iconForLibrary(concept: String, library: String, mods: Modifier[SvgElement]*): SvgElement =
    val source = iconSource(library)
    val pathData = source.paths.getOrElse(concept, LucideIconData.paths.getOrElse(concept, Seq.empty))
    svgTag(
      svg.viewBox := source.viewBox,
      svg.fill := (if source.strokeBased then "none" else "currentColor"),
      svg.stroke := (if source.strokeBased then "currentColor" else "none"),
      svg.strokeWidth := (if source.strokeBased then "2" else "0"),
      svg.strokeLineCap := (if source.strokeBased then "round" else "butt"),
      svg.strokeLineJoin := (if source.strokeBased then "round" else "miter"),
      svg.cls := "size-4",
      aria.hidden := true,
      pathData.map(d => svgPath(svg.d := d)),
      mods
    )

  private def colorSwatch(hexColor: String, mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      cls := "size-4 rounded-full bg-(--color)",
      styleProp("--color") := hexColor,
      mods
    )

  private def pickerField(
      state: CreateState,
      lockKey: String,
      contentClass: String = "",
      lockPosition: String = "absolute top-1/2 right-10 -translate-y-1/2",
      isTriggerDisabled: Signal[Boolean] = Val(false)
  )(triggerParts: HtmlElement*)(menu: Picker.Root => HtmlElement): HtmlElement =
    div(
      cls := "group/picker relative",
      Picker.root(cls := "relative w-full") { ctx =>
        div(
          Picker.trigger(ctx, isTriggerDisabled = isTriggerDisabled)(triggerParts*),
          Picker.content(ctx, cls := contentClass)(menu(ctx))
        )
      },
      LockButton(state, lockKey, cls := lockPosition)
    )

  def stylePicker(state: CreateState): HtmlElement =
    val selected = state.config.signal.map(_.stylePack)
    pickerField(state, "style", contentClass = "md:w-64")(
      div(
        cls := "flex flex-col justify-start text-left",
        div(cls := "text-xs text-muted-foreground", "Style"),
        div(
          cls := "text-sm font-medium text-foreground",
          child.text <-- selected.map(styleName =>
            styleEntries.find(_._1 == styleName).map(_._2).getOrElse(formatName(styleName))
          )
        )
      ),
      div(
        cls := "pointer-events-none absolute top-1/2 right-4 flex size-4 -translate-y-1/2 items-center justify-center select-none",
        Icons.paintbrush()
      )
    ) { ctx =>
      val styleItems = styleEntries.zipWithIndex.flatMap { case ((styleName, titleText, descriptionText), index) =>
        List(
          Picker.radioItem(
            ctx,
            styleName,
            selected,
            value => state.update(p => Preset.withStyle(p, value))
          )(
            div(
              cls := "flex items-start gap-2",
              div(
                cls := "flex size-4 translate-y-0.5 items-center justify-center",
                Icons.paintbrush()
              ),
              div(
                cls := "flex flex-col justify-start pointer-coarse:gap-1",
                div(titleText),
                div(cls := "text-xs text-muted-foreground pointer-coarse:text-sm", descriptionText)
              )
            )
          )
        ) ++ (if index < styleEntries.length - 1 then List(Picker.separator()) else Nil)
      }
      div(styleItems)
    }

  def baseColorPicker(state: CreateState): HtmlElement =
    val selected = state.config.signal.map(_.baseColor)
    pickerField(state, "baseColor")(
      div(
        cls := "flex flex-col justify-start text-left",
        div(cls := "text-xs text-muted-foreground", "Base Color"),
        div(
          cls := "text-sm font-medium text-foreground",
          child.text <-- selected.map(name => formatName(name))
        )
      ),
      div(
        cls := "pointer-events-none absolute top-1/2 right-4 size-4 -translate-y-1/2 rounded-full bg-(--color) select-none",
        styleProp("--color") <-- selected.map(name =>
          Preset.BaseColors.find(_._1 == name).map(_._2).getOrElse("#737373")
        )
      )
    ) { ctx =>
      val baseItems = Preset.BaseColors.map { case (name, hex) =>
        Picker.radioItem(ctx, name, selected, value => state.update(_.copy(baseColor = value)))(
          div(cls := "flex items-center gap-2", colorSwatch(hex), formatName(name))
        )
      }
      Picker.group()(baseItems*)
    }

  def themePicker(state: CreateState): HtmlElement =
    val selected = state.config.signal.map(_.themeColor)
    val baseColor = state.config.signal.map(_.baseColor)
    pickerField(state, "theme", contentClass = "max-h-96 overflow-y-auto")(
      div(
        cls := "flex flex-col justify-start text-left",
        div(cls := "text-xs text-muted-foreground", "Theme"),
        div(
          cls := "text-sm font-medium text-foreground",
          child.text <-- selected.map(name => formatName(name))
        )
      ),
      div(
        cls := "pointer-events-none absolute top-1/2 right-4 size-4 -translate-y-1/2 rounded-full bg-(--color) select-none",
        styleProp("--color") <-- selected.map(themeSwatchHex)
      )
    ) { ctx =>
      div(
        Picker.group()(
          children <-- baseColor.map { currentBase =>
            Preset.Themes
              .filter { case (name, _) => baseColorNames.contains(name) && name == currentBase }
              .map { case (name, _) =>
                Picker.radioItem(ctx, name, selected, value => state.update(_.copy(theme = value)))(
                  div(
                    cls := "flex items-start gap-2",
                    colorSwatch(themeSwatchHex(name), cls := "translate-y-1"),
                    div(
                      cls := "flex flex-col justify-start pointer-coarse:gap-1",
                      div(formatName(name)),
                      div(cls := "text-xs text-muted-foreground pointer-coarse:text-sm", "Match base color")
                    )
                  )
                )
              }
          }
        ),
        Picker.separator(), {
          val accentItems = Preset.Themes
            .filterNot { case (name, _) => baseColorNames.contains(name) }
            .map { case (name, _) =>
              Picker.radioItem(ctx, name, selected, value => state.update(_.copy(theme = value)))(
                div(cls := "flex items-center gap-2", colorSwatch(themeSwatchHex(name)), formatName(name))
              )
            }
          Picker.group()(accentItems*)
        }
      )
    }

  def chartColorPicker(state: CreateState): HtmlElement =
    val selected = state.config.signal.map(_.chartColor)
    val available = state.config.signal.map(cfg => RandomizeBiases.themesForBaseColor(cfg.baseColor))
    pickerField(state, "chartColor", contentClass = "max-h-96 overflow-y-auto")(
      div(
        cls := "flex flex-col justify-start text-left",
        div(cls := "text-xs text-muted-foreground", "Chart Color"),
        div(
          cls := "text-sm font-medium text-foreground",
          child.text <-- selected.map(name => formatName(name))
        )
      ),
      div(
        cls := "pointer-events-none absolute top-1/2 right-4 size-4 -translate-y-1/2 rounded-full bg-(--color) select-none md:right-2.5",
        styleProp("--color") <-- selected.map(themeSwatchHex)
      )
    ) { ctx =>
      div(
        child <-- available.map { themes =>
          val baseThemes = themes.filter(baseColorNames.contains)
          val accentThemes = themes.filterNot(baseColorNames.contains)
          val baseItems = baseThemes.map { name =>
            Picker.radioItem(ctx, name, selected, value => state.update(_.copy(chartColor = value)))(
              formatName(name)
            )
          }
          val accentItems = accentThemes.map { name =>
            Picker.radioItem(ctx, name, selected, value => state.update(_.copy(chartColor = value)))(
              formatName(name)
            )
          }
          div(
            Picker.group()(baseItems*),
            if accentThemes.nonEmpty then Picker.separator() else emptyNode,
            Picker.group()(accentItems*)
          )
        }
      )
    }

  def fontPicker(state: CreateState, labelText: String, fieldKey: String, fonts: List[String]): HtmlElement =
    val selected =
      if fieldKey == "fontHeading" then state.config.signal.map(_.headingFont)
      else state.config.signal.map(_.bodyFont)
    val bodyFont = state.config.signal.map(_.bodyFont)
    val lockKey = if fieldKey == "fontHeading" then "fontHeading" else "font"
    val lockPos = "absolute top-1/2 right-8 -translate-y-1/2"

    val groupedFonts =
      val pickerFonts = if fieldKey == "fontHeading" then fonts.filter(_ != "inherit") else fonts
      pickerFonts
        .groupBy(name => RandomizeBiases.FontTypes.getOrElse(name, "sans"))
        .toList
        .sortBy(_._1)

    pickerField(state, lockKey, contentClass = "max-h-96 overflow-y-auto md:w-72", lockPosition = lockPos)(
      div(
        cls := "flex flex-col justify-start text-left",
        div(cls := "text-xs text-muted-foreground", labelText),
        div(
          cls := "text-sm font-medium text-foreground",
          child.text <-- selected
            .combineWith(bodyFont)
            .map { (current, body) =>
              if fieldKey == "fontHeading" && current == "inherit" then formatName(body)
              else formatName(current)
            }
        )
      ),
      span(
        cls := "pointer-events-none absolute top-1/2 right-4 flex size-4 -translate-y-1/2 items-center justify-center text-base text-foreground select-none md:right-2.5",
        dataAttr("body-font") <-- selected.combineWith(bodyFont).map { (current, body) =>
          if fieldKey == "fontHeading" && current == "inherit" then body else current
        },
        "Aa"
      )
    ) { ctx =>
      div(
        if fieldKey == "fontHeading" then
          div(
            Picker.group()(
              Picker.radioItem(
                ctx,
                "inherit",
                selected,
                _ => state.update(_.copy(fontHeading = "inherit")),
                closeOnSelect = true
              )(
                child.text <-- bodyFont.map(body => formatName(body))
              )
            ),
            Picker.separator(cls := "opacity-50")
          )
        else emptyNode,
        groupedFonts.map { case (groupType, items) =>
          val fontItems = items.map { fontName =>
            Picker.radioItem(
              ctx,
              fontName,
              selected,
              value =>
                if fieldKey == "fontHeading" then state.update(_.copy(fontHeading = value))
                else state.update(_.copy(font = value))
            )(
              span(
                dataAttr("body-font") := fontName,
                cls := "font-[family-name:var(--font-body)]",
                formatName(fontName)
              )
            )
          }
          Picker.group()((Picker.label(groupType.capitalize) +: fontItems).toList*)
        }
      )
    }

  def iconLibraryPicker(state: CreateState): HtmlElement =
    val selected = state.config.signal.map(_.iconLibrary)
    pickerField(state, "iconLibrary")(
      div(
        cls := "flex flex-col justify-start text-left",
        div(cls := "text-xs text-muted-foreground", "Icon Library"),
        div(
          cls := "text-sm font-medium text-foreground",
          child.text <-- selected.map { library =>
            Preset.IconLibraries.find(_._1 == library).map(_._2).getOrElse(formatName(library))
          }
        )
      ),
      div(
        cls := "pointer-events-none absolute top-1/2 right-4 flex size-4 -translate-y-1/2 items-center justify-center select-none",
        child <-- selected.map(library => iconForLibrary("layout-dashboard", library))
      )
    ) { ctx =>
      val libraryItems = Preset.IconLibraries.zipWithIndex.flatMap { case ((libraryKey, libraryTitle), index) =>
        List(
          Picker.radioItem(
            ctx,
            libraryKey,
            selected,
            value => state.update(_.copy(iconLibrary = value)),
            Val(false),
            false,
            true,
            cls := "pr-2 *:data-[slot=dropdown-menu-radio-item-indicator]:hidden"
          )(
            Item.apply(
              Item.ItemApi.size(Item.Size.Sm),
              Item.content(
                cls := "gap-1",
                Item.title(libraryTitle, cls := "text-xs font-medium text-muted-foreground"),
                div(
                  cls := "-mx-1 grid w-full grid-cols-7 gap-2",
                  iconPreviewConcepts.map(concept => iconForLibrary(concept, libraryKey))
                )
              )
            )
          )
        ) ++ (if index < Preset.IconLibraries.length - 1 then List(Picker.separator(cls := "opacity-50")) else Nil)
      }
      Picker.group()(libraryItems*)
    }

  def radiusPicker(state: CreateState): HtmlElement =
    val styleName = state.config.signal.map(_.stylePack)
    val selectedRaw = state.config.signal.map(_.radius)
    val isRadiusLocked = styleName.map(style => style == "lyra" || style == "sera")
    val isLargeDisabled = styleName.map(_ == "rhea")
    val selected = selectedRaw.combineWith(isRadiusLocked).map { (radius, locked) =>
      if locked then "none" else radius
    }
    pickerField(state, "radius", isTriggerDisabled = isRadiusLocked)(
      div(
        cls := "flex flex-col justify-start text-left",
        div(cls := "text-xs text-muted-foreground", "Radius"),
        div(
          cls := "text-sm font-medium text-foreground",
          child.text <-- selected.map { radiusName =>
            Preset.Radii.find(_._1 == radiusName).map(_._2).getOrElse(formatName(radiusName))
          }
        )
      ),
      div(
        cls := "pointer-events-none absolute top-1/2 right-4 flex size-4 -translate-y-1/2 items-center justify-center text-base text-foreground select-none",
        div(
          cls := "size-4 border-t-2 border-r-2 border-current transition-all",
          styleProp("border-top-right-radius") <-- selected.map { radiusName =>
            Preset.Radii.find(_._1 == radiusName).map(_._3).getOrElse("0.5rem")
          }
        )
      )
    ) { ctx =>
      val radiusItems = Preset.Radii.flatMap { case (radiusName, radiusLabel, _) =>
        val disabled = isLargeDisabled.map(largeDisabled => largeDisabled && radiusName == "large")
        val item =
          if radiusName == "default" then
            Picker.radioItem(
              ctx,
              radiusName,
              selected,
              value => state.update(_.copy(radius = value)),
              isItemDisabled = isRadiusLocked
            )(
              div(
                cls := "flex flex-col justify-start pointer-coarse:gap-1",
                div(radiusLabel),
                div(cls := "text-xs text-muted-foreground pointer-coarse:text-sm", "Use radius from style")
              )
            )
          else
            Picker.radioItem(
              ctx,
              radiusName,
              selected,
              value => state.update(_.copy(radius = value)),
              isItemDisabled = isRadiusLocked.combineWith(disabled).map { (locked, largeOff) =>
                locked || largeOff
              }
            )(radiusLabel)
        if radiusName == "default" then List(item, Picker.separator()) else List(item)
      }
      Picker.group()(radiusItems*)
    }

  def menuColorPicker(state: CreateState, lastSolidMenuAccentVar: Var[String]): HtmlElement =
    val menuColor = state.config.signal.map(_.menuColor)
    val isDark = state.config.signal.map(_.darkMode)
    val colorChoice = menuColor.map(menuColorChoice)
    val surfaceChoice = menuColor.map(menuSurfaceChoice)

    state.config.signal --> { cfg =>
      if !isTranslucentMenuColor(cfg.menuColor) then lastSolidMenuAccentVar.set(cfg.menuAccent)
    }

    def setColor(choice: String): Unit =
      val surface = menuSurfaceChoice(state.config.now().menuColor)
      val nextMenuColor = menuColorFromChoices(choice, surface)
      state.update { preset =>
        val nextAccent =
          if isTranslucentMenuColor(nextMenuColor) then "subtle" else preset.menuAccent
        preset.copy(menuColor = nextMenuColor, menuAccent = nextAccent)
      }

    def setSurface(choice: String): Unit =
      val color = menuColorChoice(state.config.now().menuColor)
      val nextMenuColor = menuColorFromChoices(color, choice)
      val nextAccent =
        if choice == "translucent" then "subtle"
        else lastSolidMenuAccentVar.now()
      state.update(_.copy(menuColor = nextMenuColor, menuAccent = nextAccent))

    pickerField(state, "menuColor", lockPosition = "absolute top-1/2 right-8 -translate-y-1/2")(
      div(
        cls := "flex min-w-0 flex-1 flex-col justify-start overflow-hidden pr-8 text-left md:pr-7",
        div(cls := "text-xs text-muted-foreground", "Menu"),
        div(
          cls := "overflow-hidden text-sm font-medium text-ellipsis whitespace-nowrap text-foreground",
          child.text <-- menuColor.map { current =>
            menuColorOptions.find(_._1 == current).map(_._2).getOrElse(formatName(current))
          }
        )
      ),
      div(
        cls := "pointer-events-none absolute top-1/2 right-4 flex size-4 -translate-y-1/2 items-center justify-center select-none md:right-2.5",
        Icons.menu()
      )
    ) { ctx =>
      div(
        Picker.group()(
          Picker.label("Color"),
          Picker.radioItem(ctx, "default", colorChoice, setColor)("Default"),
          Picker.radioItem(
            ctx,
            "inverted",
            colorChoice,
            setColor,
            isItemDisabled = isDark
          )("Inverted")
        ),
        Picker.separator(),
        Picker.group()(
          Picker.label("Appearance"),
          Picker.radioItem(ctx, "solid", surfaceChoice, setSurface)("Solid"),
          Picker.radioItem(ctx, "translucent", surfaceChoice, setSurface)("Translucent")
        )
      )
    }

  def menuAccentPicker(state: CreateState): HtmlElement =
    val selected = state.config.signal.map(_.menuAccent)
    pickerField(state, "menuAccent")(
      div(
        cls := "flex flex-col justify-start text-left",
        div(cls := "text-xs text-muted-foreground", "Menu Accent"),
        div(
          cls := "text-sm font-medium text-foreground",
          child.text <-- selected.map(name => menuAccentLabels.getOrElse(name, formatName(name)))
        )
      ),
      div(
        cls := "pointer-events-none absolute top-1/2 right-4 flex size-4 -translate-y-1/2 items-center justify-center text-base text-foreground select-none",
        menuAccentPreview(selected)
      )
    ) { ctx =>
      val accentItems = Preset.MenuAccents.map { accentName =>
        Picker.radioItem(ctx, accentName, selected, value => state.update(_.copy(menuAccent = value)))(
          menuAccentLabels.getOrElse(accentName, formatName(accentName))
        )
      }
      Picker.group()(accentItems*)
    }

  private def menuAccentPreview(accentSignal: Signal[String]): SvgElement =
    val accentAttr = svg.svgAttr("data-accent", com.raquo.laminar.codecs.StringAsIsCodec, None)
    svgTag(
      svg.xmlns := "http://www.w3.org/2000/svg",
      svg.width := "24",
      svg.height := "24",
      svg.viewBox := "0 0 24 24",
      svg.fill := "none",
      svg.cls := "size-4 text-foreground",
      svgPath(
        svg.d := "M19 12.1294L12.9388 18.207C11.1557 19.9949 10.2641 20.8889 9.16993 20.9877C8.98904 21.0041 8.80705 21.0041 8.62616 20.9877C7.53195 20.8889 6.64039 19.9949 4.85726 18.207L2.83687 16.1811C1.72104 15.0622 1.72104 13.2482 2.83687 12.1294M19 12.1294L10.9184 4.02587M19 12.1294H2.83687M10.9184 4.02587L2.83687 12.1294M10.9184 4.02587L8.89805 2",
        svg.stroke := "currentColor",
        svg.strokeWidth := "2",
        svg.strokeLineCap := "round",
        svg.strokeLineJoin := "round",
        svg.cls := "fill-muted-foreground/30 data-[accent=bold]:fill-foreground",
        accentAttr <-- accentSignal
      ),
      svgPath(
        svg.d := "M22 20C22 21.1046 21.1046 22 20 22C18.8954 22 18 21.1046 18 20C18 18.8954 20 17 20 17C20 17 22 18.8954 22 20Z",
        svg.stroke := "currentColor",
        svg.strokeWidth := "2",
        svg.strokeLineCap := "round",
        svg.strokeLineJoin := "round",
        svg.cls := "fill-muted-foreground/30 data-[accent=bold]:fill-foreground",
        accentAttr <-- accentSignal
      )
    )
