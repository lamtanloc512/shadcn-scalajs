package shadcnscalajs.site

import com.raquo.laminar.api.L.*
import org.scalajs.dom
import shadcnscalajs.blocks.dashboard01.Dashboard01
import shadcnscalajs.ui.*

/** `/create` — the theme customizer: a sidebar of controls writing into a shared, persisted `ThemeConfig`, and
  * `Dashboard01` rendered live next to it (inline, not iframed, so it shares this page's own `Var` directly). See
  * docs/superpowers/specs/2026-08-06-create-theme-customizer-design.md.
  */
object CreatePage:

  private val asideTag = htmlTag("aside")

  private def selectField(
      label: String,
      options: List[(String, String)],
      currentValue: ThemeConfig => String,
      update: (ThemeConfig, String) => ThemeConfig,
      themeConfig: Var[ThemeConfig]
  ): HtmlElement =
    Field(
      Field.label(label),
      select(
        cls := "h-8 w-full rounded-md border border-input bg-background px-2 text-sm",
        value <-- themeConfig.signal.map(currentValue),
        onChange --> { ev =>
          val next = update(themeConfig.now(), ev.target.asInstanceOf[dom.html.Select].value)
          themeConfig.set(next)
          ThemeConfig.store(next)
        },
        options.map { case (v, label) => option(value := v, label) }
      )
    )

  private val stylePackOptions =
    List(
      "vega" -> "Vega",
      "nova" -> "Nova",
      "maia" -> "Maia",
      "lyra" -> "Lyra",
      "mira" -> "Mira",
      "luma" -> "Luma",
      "sera" -> "Sera",
      "rhea" -> "Rhea"
    )
  private val baseColorOptions =
    List("neutral" -> "Neutral", "gray" -> "Gray", "zinc" -> "Zinc", "stone" -> "Stone", "slate" -> "Slate")
  private val themeColorOptions = List(
    "red" -> "Red",
    "orange" -> "Orange",
    "amber" -> "Amber",
    "yellow" -> "Yellow",
    "lime" -> "Lime",
    "green" -> "Green",
    "emerald" -> "Emerald",
    "teal" -> "Teal",
    "cyan" -> "Cyan",
    "sky" -> "Sky",
    "blue" -> "Blue",
    "indigo" -> "Indigo",
    "violet" -> "Violet",
    "purple" -> "Purple",
    "fuchsia" -> "Fuchsia",
    "pink" -> "Pink",
    "rose" -> "Rose"
  )
  private val fontOptions = List("default" -> "Default", "inter" -> "Inter", "geist" -> "Geist", "dm-sans" -> "DM Sans")
  private val iconLibraryOptions = List("lucide" -> "Lucide", "hugeicons" -> "Hugeicons")
  private val radiusOptions =
    List("default" -> "Default", "none" -> "None", "small" -> "Small", "medium" -> "Medium", "large" -> "Large")
  private val menuColorOptions = List("default" -> "Default", "inverted" -> "Inverted")
  private val menuAccentOptions = List("subtle" -> "Subtle", "solid" -> "Solid")

  def apply(): HtmlElement =
    val themeConfig = Var(ThemeConfig.load())

    div(
      cls := "min-h-dvh bg-background text-foreground antialiased",
      themeConfig.signal --> { cfg => ThemeConfig.applyToDocument(cfg) },
      div(
        cls := "flex min-h-dvh",
        asideTag(
          cls := "w-72 shrink-0 border-r bg-card p-4",
          h2(cls := "mb-4 text-sm font-semibold", "Customize"),
          div(
            cls := "flex flex-col gap-3",
            selectField("Style", stylePackOptions, _.stylePack, (c, v) => c.copy(stylePack = v), themeConfig),
            selectField("Base Color", baseColorOptions, _.baseColor, (c, v) => c.copy(baseColor = v), themeConfig),
            selectField("Theme", themeColorOptions, _.themeColor, (c, v) => c.copy(themeColor = v), themeConfig),
            selectField("Chart Color", themeColorOptions, _.chartColor, (c, v) => c.copy(chartColor = v), themeConfig),
            selectField("Heading Font", fontOptions, _.headingFont, (c, v) => c.copy(headingFont = v), themeConfig),
            selectField("Font", fontOptions, _.bodyFont, (c, v) => c.copy(bodyFont = v), themeConfig),
            selectField(
              "Icon Library",
              iconLibraryOptions,
              _.iconLibrary,
              (c, v) => c.copy(iconLibrary = v),
              themeConfig
            ),
            selectField("Radius", radiusOptions, _.radius, (c, v) => c.copy(radius = v), themeConfig),
            selectField("Menu Color", menuColorOptions, _.menuColor, (c, v) => c.copy(menuColor = v), themeConfig),
            selectField("Menu Accent", menuAccentOptions, _.menuAccent, (c, v) => c.copy(menuAccent = v), themeConfig)
          )
        ),
        div(
          cls := "flex-1 overflow-auto",
          Dashboard01(
            dataAttr("menu-color") <-- themeConfig.signal.map(_.menuColor),
            dataAttr("menu-accent") <-- themeConfig.signal.map(_.menuAccent)
          )
        )
      )
    )
