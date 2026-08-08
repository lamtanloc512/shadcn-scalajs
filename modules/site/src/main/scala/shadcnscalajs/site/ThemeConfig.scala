package shadcnscalajs.site

import org.scalajs.dom

import scala.scalajs.js
import scala.util.Try
import shadcnscalajs.site.create.{Preset, PresetConfig}

/** Site-wide theme/customizer state, persisted to `localStorage` so it survives real browser navigations — this site
  * has no client-side router, so `Main.main()` re-runs from scratch on every page load with fresh `Var`s. Every page's
  * mount function seeds a `Var[ThemeConfig]` from `load()` and applies it via `applyToDocument`; any control that
  * changes a field calls `store` so the next page load picks it up. See
  * docs/superpowers/specs/2026-08-06-create-theme-customizer-design.md for the full design.
  */
final case class ThemeConfig(
    stylePack: String = Preset.Styles.head,
    darkMode: Boolean = false,
    baseColor: String = Preset.BaseColors.head._1,
    themeColor: String = Preset.Themes.head._1,
    chartColor: String = Preset.ChartColors.head,
    headingFont: String = Preset.FontHeadings.head,
    bodyFont: String = Preset.Fonts.head,
    iconLibrary: String = Preset.IconLibraries.head._1,
    radius: String = Preset.Radii.head._1,
    menuColor: String = Preset.MenuColors.head,
    menuAccent: String = Preset.MenuAccents.head
)

object ThemeConfig:

  private val storageKey = "shadcn-scalajs:theme"

  val default: ThemeConfig = fromPreset(Preset.default, darkMode = false)

  private val stylePacks = Preset.Styles
  private val baseColors = Preset.BaseColors.map(_._1)
  private val themeColors = Preset.Themes.map(_._1)
  private val chartColors = Preset.ChartColors
  private val headingFonts = Preset.FontHeadings
  private val bodyFonts = Preset.Fonts
  private val iconLibraries = Preset.IconLibraries.map(_._1)
  private val radii = Preset.Radii.map(_._1)
  private val menuColors = Preset.MenuColors
  private val menuAccents = Preset.MenuAccents

  private var observerInstalled = false
  private var menuFrameId: Int = 0
  private var currentMenuColor: String = default.menuColor

  def fromPreset(c: PresetConfig, darkMode: Boolean): ThemeConfig =
    ThemeConfig(
      stylePack = c.style,
      darkMode = darkMode,
      baseColor = c.baseColor,
      themeColor = c.theme,
      chartColor = c.chartColor,
      headingFont = c.fontHeading,
      bodyFont = c.font,
      iconLibrary = c.iconLibrary,
      radius = c.radius,
      menuColor = c.menuColor,
      menuAccent = c.menuAccent
    )

  def toPreset(cfg: ThemeConfig): PresetConfig =
    PresetConfig(
      style = cfg.stylePack,
      baseColor = cfg.baseColor,
      theme = cfg.themeColor,
      chartColor = cfg.chartColor,
      iconLibrary = cfg.iconLibrary,
      font = cfg.bodyFont,
      fontHeading = cfg.headingFont,
      radius = cfg.radius,
      menuAccent = cfg.menuAccent,
      menuColor = cfg.menuColor
    )

  /** Reads the persisted config, falling back to `default` on a missing key, a JSON parse error, or a corrupt shape — a
    * user editing localStorage by hand (or an old, differently-shaped value from before a field was added) must never
    * crash page load. Values outside the reference preset vocabulary are coerced to each field's default.
    */
  def load(): ThemeConfig =
    Try {
      val raw = dom.window.localStorage.getItem(storageKey)
      if raw == null then storedPreset.getOrElse(default)
      else
        val parsed = js.JSON.parse(raw).asInstanceOf[js.Dynamic]
        def str(field: String, fallback: String): String =
          parsed.selectDynamic(field).asInstanceOf[js.UndefOr[String]].getOrElse(fallback)
        def bool(field: String, fallback: Boolean): Boolean =
          parsed.selectDynamic(field).asInstanceOf[js.UndefOr[Boolean]].getOrElse(fallback)
        ThemeConfig(
          stylePack = coerce(str("stylePack", default.stylePack), stylePacks, default.stylePack),
          darkMode = bool("darkMode", default.darkMode),
          baseColor = coerce(str("baseColor", default.baseColor), baseColors, default.baseColor),
          themeColor = coerce(str("themeColor", default.themeColor), themeColors, default.themeColor),
          chartColor = coerce(str("chartColor", default.chartColor), chartColors, default.chartColor),
          headingFont = coerce(str("headingFont", default.headingFont), headingFonts, default.headingFont),
          bodyFont = coerce(str("bodyFont", default.bodyFont), bodyFonts, default.bodyFont),
          iconLibrary = coerce(str("iconLibrary", default.iconLibrary), iconLibraries, default.iconLibrary),
          radius = coerce(str("radius", default.radius), radii, default.radius),
          menuColor = coerce(str("menuColor", default.menuColor), menuColors, default.menuColor),
          menuAccent = coerce(str("menuAccent", default.menuAccent), menuAccents, default.menuAccent)
        )
    }.getOrElse(default)

  /** The customizer's preset code, for visitors who have used `/create` but never touched a page-level theme control.
    * Preset codes carry no dark-mode bit, so that field keeps its default.
    */
  private def storedPreset: Option[ThemeConfig] =
    Option(dom.window.localStorage.getItem(Preset.storageKey))
      .filter(Preset.isPresetCode)
      .flatMap(Preset.decode)
      .map(preset => fromPreset(preset, darkMode = default.darkMode))

  def store(cfg: ThemeConfig): Unit =
    val literal = js.Dynamic.literal(
      stylePack = cfg.stylePack,
      darkMode = cfg.darkMode,
      baseColor = cfg.baseColor,
      themeColor = cfg.themeColor,
      chartColor = cfg.chartColor,
      headingFont = cfg.headingFont,
      bodyFont = cfg.bodyFont,
      iconLibrary = cfg.iconLibrary,
      radius = cfg.radius,
      menuColor = cfg.menuColor,
      menuAccent = cfg.menuAccent
    )
    dom.window.localStorage.setItem(storageKey, js.JSON.stringify(literal))
    // `/create` seeds itself from the preset code, not from this JSON. Without this write, opening the customizer
    // reverts whatever the docs, gallery, or blocks header last selected.
    dom.window.localStorage.setItem(Preset.storageKey, Preset.encode(toPreset(cfg)))

  /** Sets every `data-*` attribute `globals.css`'s attribute-selector blocks key off of, plus the `dark` class, on
    * `<html>` itself — not some inner div. `rem`-based Tailwind classes (used throughout this codebase) only ever
    * resolve against `<html>`'s own state, never an ancestor div's; putting `dark` here too (rather than on each page's
    * own root div, as today) means the `&:is(.dark *)` custom variant in globals.css covers the *entire* document
    * unconditionally, which is a strict superset of today's behavior, not a behavior change.
    */
  def applyToDocument(cfg: ThemeConfig): Unit =
    applyToDocument(cfg, dom.document)

  def applyToDocument(cfg: ThemeConfig, doc: dom.Document): Unit =
    val html = doc.documentElement
    html.setAttribute("data-style-pack", cfg.stylePack)
    html.setAttribute("data-base-color", cfg.baseColor)
    html.setAttribute("data-theme-color", cfg.themeColor)
    html.setAttribute("data-chart-color", cfg.chartColor)
    html.setAttribute("data-heading-font", cfg.headingFont)
    html.setAttribute("data-body-font", cfg.bodyFont)
    html.setAttribute("data-icon-library", cfg.iconLibrary)
    html.setAttribute("data-radius", cfg.radius)
    html.setAttribute("data-menu-color", cfg.menuColor)
    html.setAttribute("data-menu-accent", cfg.menuAccent)
    if cfg.darkMode then html.classList.add("dark") else html.classList.remove("dark")

    currentMenuColor = cfg.menuColor
    if doc eq dom.document then
      scheduleMenuUpdate(doc)
      ensureMenuObserver(doc)
    else updateMenuElements(doc, cfg.menuColor)

  private def coerce(raw: String, allowed: List[String], fallback: String): String =
    if allowed.contains(raw) then raw else fallback

  private def scheduleMenuUpdate(doc: dom.Document): Unit =
    if menuFrameId != 0 then return
    menuFrameId = dom.window.requestAnimationFrame { _ =>
      menuFrameId = 0
      updateMenuElements(doc, currentMenuColor)
    }

  /** `body` lives on `HTMLDocument`, not the `Document` these helpers accept (so an iframe's `contentDocument` can be
    * passed in), hence the cast.
    */
  private def bodyOf(doc: dom.Document): dom.html.Element =
    doc.asInstanceOf[dom.HTMLDocument].body

  private def ensureMenuObserver(doc: dom.Document): Unit =
    if observerInstalled || bodyOf(doc) == null then return
    observerInstalled = true
    val observer = new dom.MutationObserver((_, _) => scheduleMenuUpdate(doc))
    observer.observe(
      bodyOf(doc),
      new dom.MutationObserverInit {
        childList = true
        subtree = true
      }
    )

  private def updateMenuElements(doc: dom.Document, menuColor: String): Unit =
    val isInvertedMenu = menuColor == "inverted" || menuColor == "inverted-translucent"
    val isTranslucentMenu = menuColor == "default-translucent" || menuColor == "inverted-translucent"

    val nodeList = doc.querySelectorAll(".cn-menu-target, [data-menu-translucent]")
    val count = nodeList.length
    if count == 0 then return

    var i = 0
    while i < count do
      val element = nodeList.item(i).asInstanceOf[dom.html.Element]
      element.style.transition = "none"
      i += 1

    i = 0
    while i < count do
      val element = nodeList.item(i).asInstanceOf[dom.html.Element]
      if element.classList.contains("cn-menu-target") then
        if isInvertedMenu then element.classList.add("dark")
        else element.classList.remove("dark")

      if isTranslucentMenu then
        element.classList.add("cn-menu-translucent")
        element.removeAttribute("data-menu-translucent")
      else if element.classList.contains("cn-menu-translucent") then
        element.classList.remove("cn-menu-translucent")
        element.setAttribute("data-menu-translucent", "")
      i += 1

    val bodyEl = bodyOf(doc)
    if bodyEl != null then
      val _ = bodyEl.offsetHeight

    i = 0
    while i < count do
      val element = nodeList.item(i).asInstanceOf[dom.html.Element]
      element.style.transition = ""
      i += 1
