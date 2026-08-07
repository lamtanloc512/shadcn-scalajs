package shadcnscalajs.site.create

import scala.util.Random

/** Preset encoding/decoding for the create-page design system customizer.
  *
  * Bit-packs design-system params into a single integer, then encodes as base62 with a version prefix character. Ported
  * from shadcn-svelte's `packages/cli/src/preset/preset.ts` — value array order and field bit widths are load-bearing
  * for round-trip compatibility with shared preset codes.
  */
final case class PresetConfig(
    style: String,
    baseColor: String,
    theme: String,
    chartColor: String,
    iconLibrary: String,
    font: String,
    fontHeading: String,
    radius: String,
    menuAccent: String,
    menuColor: String
)

object Preset:

  val Styles: List[String] =
    List("nova", "vega", "maia", "lyra", "mira", "luma", "sera", "rhea")

  /** Radius written when a style pack is selected. Lookup → assign → persist.
    * Lyra/Sera are square (`none`); every other pack uses the token default.
    */
  val StyleRadius: Map[String, String] =
    Styles.map(s => s -> (if s == "lyra" || s == "sera" then "none" else "default")).toMap

  /** Apply a style-pack selection: set `style` and the mapped radius in one step. */
  def withStyle(cfg: PresetConfig, style: String): PresetConfig =
    cfg.copy(style = style, radius = StyleRadius.getOrElse(style, "default"))

  val BaseColors: List[(String, String)] = List(
    ("neutral", "#737373"),
    ("stone", "#79716B"),
    ("zinc", "#71717B"),
    ("mauve", "#79697B"),
    ("olive", "#7C7C67"),
    ("mist", "#67787C"),
    ("taupe", "#7C6D67")
  )

  val Themes: List[(String, String)] = List(
    ("neutral", "#737373"),
    ("stone", "#78716c"),
    ("zinc", "#71717a"),
    ("amber", "#FD9A00"),
    ("blue", "#2B7FFF"),
    ("cyan", "#00B8DB"),
    ("emerald", "#00BC7D"),
    ("fuchsia", "#E12AFB"),
    ("green", "#00C950"),
    ("indigo", "#615FFF"),
    ("lime", "#7CCF00"),
    ("orange", "#FF6900"),
    ("pink", "#F6339A"),
    ("purple", "#AD46FF"),
    ("red", "#FB2C36"),
    ("rose", "#FF2056"),
    ("sky", "#00A6F4"),
    ("teal", "#00BBA7"),
    ("violet", "#8E51FF"),
    ("yellow", "#EFB100"),
    ("mauve", "#79697b"),
    ("olive", "#7c7c67"),
    ("mist", "#67787c"),
    ("taupe", "#7c6d67")
  )

  val ChartColors: List[String] = Themes.map(_._1)

  /** Before v2, base-color themes borrowed a colored chart palette from these themes. v1 codes carry no `chartColor`
    * field, so decoding one restores the palette it would originally have rendered with rather than falling back to
    * `neutral` (which would silently grey out every chart in a shared v1 preset).
    */
  private val V1ChartColors: Map[String, String] = Map(
    "neutral" -> "blue",
    "stone" -> "lime",
    "zinc" -> "amber",
    "mauve" -> "emerald",
    "olive" -> "violet",
    "mist" -> "rose",
    "taupe" -> "cyan"
  )

  val IconLibraries: List[(String, String)] = List(
    ("lucide", "Lucide"),
    ("tabler", "Tabler"),
    ("hugeicons", "HugeIcons"),
    ("phosphor", "Phosphor"),
    ("remixicon", "Remix Icon")
  )

  val Fonts: List[String] = List(
    "inter",
    "noto-sans",
    "nunito-sans",
    "figtree",
    "roboto",
    "raleway",
    "dm-sans",
    "public-sans",
    "outfit",
    "jetbrains-mono",
    "geist",
    "geist-mono",
    "lora",
    "merriweather",
    "playfair-display",
    "noto-serif",
    "roboto-slab",
    "oxanium",
    "manrope",
    "space-grotesk",
    "montserrat",
    "ibm-plex-sans",
    "source-sans-3",
    "instrument-sans",
    "eb-garamond",
    "instrument-serif"
  )

  val FontHeadings: List[String] = "inherit" :: Fonts

  /** `(name, label, cssValue)` — `cssValue` must stay in sync with
    * `[data-radius=…]` in `globals.css`. Tailwind `rounded-*` utilities are
    * multiples of `--radius`, so `none` (0rem) flattens every control.
    */
  val Radii: List[(String, String, String)] = List(
    ("default", "Default", "0.625rem"),
    ("none", "None", "0rem"),
    ("small", "Small", "0.45rem"),
    ("medium", "Medium", "0.625rem"),
    ("large", "Large", "0.875rem")
  )

  val MenuAccents: List[String] = List("subtle", "bold")

  val MenuColors: List[String] =
    List("default", "inverted", "default-translucent", "inverted-translucent")

  val default: PresetConfig = PresetConfig(
    style = Styles.head,
    baseColor = BaseColors.head._1,
    theme = Themes.head._1,
    chartColor = ChartColors.head,
    iconLibrary = IconLibraries.head._1,
    font = Fonts.head,
    fontHeading = FontHeadings.head,
    radius = Radii.head._1,
    menuAccent = MenuAccents.head,
    menuColor = MenuColors.head
  )

  private val Base62 = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"

  private val CurrentVersion = "b"
  private val ValidVersions = Set("a", "b")

  private final case class PresetField(key: String, values: List[String], bits: Int)

  private val FieldsV1: List[PresetField] = List(
    PresetField("menuColor", MenuColors, 3),
    PresetField("menuAccent", MenuAccents, 3),
    PresetField("radius", Radii.map(_._1), 4),
    PresetField("font", Fonts, 6),
    PresetField("iconLibrary", IconLibraries.map(_._1), 6),
    PresetField("theme", Themes.map(_._1), 6),
    PresetField("baseColor", BaseColors.map(_._1), 6),
    PresetField("style", Styles, 6)
  )

  private val FieldsV2: List[PresetField] = FieldsV1 ++ List(
    PresetField("chartColor", ChartColors, 6),
    PresetField("fontHeading", FontHeadings, 5)
  )

  def encode(cfg: PresetConfig): String =
    var bits = 0.0
    var offset = 0
    FieldsV2.foreach { field =>
      val idx = fieldValueIndex(cfg, field)
      bits += idx * math.pow(2, offset)
      offset += field.bits
    }
    CurrentVersion + toBase62(bits)

  def decode(code: String): Option[PresetConfig] =
    if code == null || code.isEmpty || code.length < 2 then None
    else
      val version = code.substring(0, 1)
      if !ValidVersions.contains(version) then None
      else
        val fields = if version == "a" then FieldsV1 else FieldsV2
        val bits = fromBase62(code.substring(1))
        if bits < 0 then None
        else
          val decoded = scala.collection.mutable.Map.empty[String, String]
          var offset = 0
          fields.foreach { field =>
            val idx =
              (math.floor(bits / math.pow(2, offset)) % math.pow(2, field.bits)).toInt
            decoded(field.key) = if idx < field.values.length then field.values(idx) else field.values.head
            offset += field.bits
          }
          if version == "a" then
            decoded("fontHeading") = "inherit"
            decoded("chartColor") =
              V1ChartColors.getOrElse(decoded.getOrElse("baseColor", BaseColors.head._1), ChartColors.head)
          Some(toPresetConfig(decoded.toMap))

  def isPresetCode(value: String): Boolean =
    if value == null || value.length < 2 || value.length > 10 then false
    else if !ValidVersions.contains(value.substring(0, 1)) then false
    else value.substring(1).forall(c => Base62.indexOf(c) >= 0)

  def randomConfig(): PresetConfig =
    def pick(values: List[String]): String = values(Random.nextInt(values.length))
    PresetConfig(
      style = pick(Styles),
      baseColor = pick(BaseColors.map(_._1)),
      theme = pick(Themes.map(_._1)),
      chartColor = pick(ChartColors),
      iconLibrary = pick(IconLibraries.map(_._1)),
      font = pick(Fonts),
      fontHeading = pick(FontHeadings),
      radius = pick(Radii.map(_._1)),
      menuAccent = pick(MenuAccents),
      menuColor = pick(MenuColors)
    )

  private def fieldValueIndex(cfg: PresetConfig, field: PresetField): Int =
    val value = fieldValue(cfg, field.key)
    val idx = field.values.indexOf(value)
    if idx == -1 then 0 else idx

  private def fieldValue(cfg: PresetConfig, key: String): String = key match
    case "style"       => cfg.style
    case "baseColor"   => cfg.baseColor
    case "theme"       => cfg.theme
    case "chartColor"  => cfg.chartColor
    case "iconLibrary" => cfg.iconLibrary
    case "font"        => cfg.font
    case "fontHeading" => cfg.fontHeading
    case "radius"      => cfg.radius
    case "menuAccent"  => cfg.menuAccent
    case "menuColor"   => cfg.menuColor
    case _             => ""

  private def toPresetConfig(values: Map[String, String]): PresetConfig =
    PresetConfig(
      style = values.getOrElse("style", Styles.head),
      baseColor = values.getOrElse("baseColor", BaseColors.head._1),
      theme = values.getOrElse("theme", Themes.head._1),
      chartColor = values.getOrElse("chartColor", ChartColors.head),
      iconLibrary = values.getOrElse("iconLibrary", IconLibraries.head._1),
      font = values.getOrElse("font", Fonts.head),
      fontHeading = values.getOrElse("fontHeading", FontHeadings.head),
      radius = values.getOrElse("radius", Radii.head._1),
      menuAccent = values.getOrElse("menuAccent", MenuAccents.head),
      menuColor = values.getOrElse("menuColor", MenuColors.head)
    )

  private def toBase62(num: Double): String =
    if num == 0.0 then "0"
    else
      var result = ""
      var n = num
      while n > 0 do
        result = Base62.charAt((n % 62).toInt).toString + result
        n = math.floor(n / 62)
      result

  private def fromBase62(str: String): Double =
    var result = 0.0
    var i = 0
    while i < str.length do
      val idx = Base62.indexOf(str.charAt(i))
      if idx == -1 then return -1.0
      result = result * 62 + idx
      i += 1
    result
