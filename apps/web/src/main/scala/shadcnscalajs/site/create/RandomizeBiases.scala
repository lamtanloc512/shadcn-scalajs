package shadcnscalajs.site.create

/** Biased randomization tables for the create-page customizer. Ported from shadcn-svelte's
  * `routes/(app)/(layout)/(create)/lib/randomize-biases.ts`.
  */
object RandomizeBiases:

  final case class RandomizeContext(
      style: Option[String] = None,
      baseColor: Option[String] = None,
      theme: Option[String] = None,
      chartColor: Option[String] = None,
      iconLibrary: Option[String] = None,
      font: Option[String] = None,
      menuAccent: Option[String] = None,
      menuColor: Option[String] = None,
      radius: Option[String] = None
  )

  /** Theme → chart color pairings for randomization. */
  val ChartColorPairings: Map[String, List[String]] = Map(
    "red" -> List("teal", "sky"),
    "orange" -> List("teal", "blue"),
    "amber" -> List("cyan", "indigo"),
    "yellow" -> List("sky", "violet"),
    "lime" -> List("indigo", "pink"),
    "green" -> List("purple", "rose"),
    "emerald" -> List("purple", "red"),
    "teal" -> List("fuchsia", "red"),
    "cyan" -> List("rose", "amber"),
    "sky" -> List("red", "yellow"),
    "blue" -> List("orange", "yellow"),
    "indigo" -> List("amber", "yellow"),
    "violet" -> List("yellow", "lime"),
    "purple" -> List("green", "lime"),
    "fuchsia" -> List("lime", "teal"),
    "pink" -> List("green", "cyan"),
    "rose" -> List("emerald", "sky")
  )

  /** Font category used for heading/body contrast during randomization. Mirrors `font-definitions.ts`. */
  val FontTypes: Map[String, String] = Map(
    "geist" -> "sans",
    "inter" -> "sans",
    "noto-sans" -> "sans",
    "nunito-sans" -> "sans",
    "figtree" -> "sans",
    "roboto" -> "sans",
    "raleway" -> "sans",
    "dm-sans" -> "sans",
    "public-sans" -> "sans",
    "outfit" -> "sans",
    "oxanium" -> "sans",
    "manrope" -> "sans",
    "space-grotesk" -> "sans",
    "montserrat" -> "sans",
    "ibm-plex-sans" -> "sans",
    "source-sans-3" -> "sans",
    "instrument-sans" -> "sans",
    "jetbrains-mono" -> "mono",
    "geist-mono" -> "mono",
    "noto-serif" -> "serif",
    "roboto-slab" -> "serif",
    "merriweather" -> "serif",
    "lora" -> "serif",
    "playfair-display" -> "serif",
    "eb-garamond" -> "serif",
    "instrument-serif" -> "serif"
  )

  def themesForBaseColor(baseColorName: String): List[String] =
    val baseColorNames = Preset.BaseColors.map(_._1)
    Preset.Themes.map(_._1).filter { theme =>
      theme == baseColorName || !baseColorNames.contains(theme)
    }

  def applyFontBias(fonts: List[String], context: RandomizeContext): List[String] =
    if context.style.contains("lyra") then fonts.filter(_ == "jetbrains-mono")
    else fonts

  def applyRadiusBias(radii: List[String], context: RandomizeContext): List[String] =
    context.style match
      case Some("lyra") => radii.filter(_ == "none")
      case Some("rhea") => radii.filter(_ != "large")
      case _            => radii

  def applyChartColorBias(chartColors: List[String], context: RandomizeContext): List[String] =
    context.theme.flatMap(ChartColorPairings.get) match
      case Some(pairing) =>
        val filtered = chartColors.filter(pairing.contains)
        if filtered.nonEmpty then filtered else chartColors
      case None => chartColors
