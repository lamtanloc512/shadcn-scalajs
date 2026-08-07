package shadcnscalajs.site.create

import com.raquo.laminar.api.L.*
import org.scalajs.dom

import scala.scalajs.js
import scala.util.Random
import scala.util.Try
import shadcnscalajs.site.ThemeConfig

/** Create-page customizer state: preset persistence, per-field locks, undo/redo history, biased randomization, and URL
  * sync. Ported from shadcn-svelte's `design-system-provider-state.svelte.ts`.
  */
final class CreateState:

  private val presetStorageKey = "design-system-preset"
  private val locksStorageKey = "locks"

  private var shortcutsInstalled = false

  private def initialPresetCode: String =
    readUrlPreset
      .orElse(readStoredPreset)
      .getOrElse(Preset.encode(Preset.default))

  private def initialDarkMode: Boolean =
    ThemeConfig.load().darkMode

  private def initialConfig: ThemeConfig =
    val preset = Preset.decode(initialPresetCode).getOrElse(Preset.default)
    ThemeConfig.fromPreset(preset, darkMode = initialDarkMode)

  private def initialHistoryLog: Vector[String] =
    Vector(Preset.encode(ThemeConfig.toPreset(initialConfig)))

  private val historyLog = Var(initialHistoryLog)
  private val historyCursor = Var(0)

  val config: Var[ThemeConfig] = Var(initialConfig)

  val presetCode: Signal[String] =
    config.signal.map(cfg => Preset.encode(ThemeConfig.toPreset(cfg)))

  val locks: Var[Map[String, Boolean]] = Var(loadLocks())

  val canUndo: Signal[Boolean] = historyCursor.signal.map(_ > 0)

  val canRedo: Signal[Boolean] =
    historyCursor.signal
      .combineWith(historyLog.signal)
      .map { (cursor, log) => cursor < log.length - 1 }

  locally {
    val code = Preset.encode(ThemeConfig.toPreset(config.now()))
    persistPreset(code)
    ThemeConfig.store(config.now())
    syncUrl(code)
  }

  def update(f: PresetConfig => PresetConfig): Unit =
    val next = f(ThemeConfig.toPreset(config.now()))
    commitPreset(Preset.encode(next), recordHistory = true)

  def toggleLock(key: String): Unit =
    val next = locks.now().updated(key, !locks.now().getOrElse(key, false))
    locks.set(next)
    persistLocks(next)

  def isLocked(key: String): Signal[Boolean] =
    locks.signal.map(_.getOrElse(key, false))

  def undo(): Unit =
    val cursor = historyCursor.now()
    if cursor > 0 then
      val nextCursor = cursor - 1
      historyCursor.set(nextCursor)
      applyPresetCodeFromHistory(historyLog.now()(nextCursor))

  def redo(): Unit =
    val cursor = historyCursor.now()
    val log = historyLog.now()
    if cursor < log.length - 1 then
      val nextCursor = cursor + 1
      historyCursor.set(nextCursor)
      applyPresetCodeFromHistory(log(nextCursor))

  def randomize(): Unit =
    val current = ThemeConfig.toPreset(config.now())
    val lockMap = locks.now()

    def locked(key: String): Boolean = lockMap.getOrElse(key, false)
    def pick(values: List[String]): String = values(Random.nextInt(values.length))

    val selectedBaseColor =
      if locked("baseColor") then current.baseColor
      else pick(Preset.BaseColors.map(_._1))

    val selectedStyle =
      if locked("style") then current.style
      else pick(Preset.Styles)

    var context = RandomizeBiases.RandomizeContext(
      baseColor = Some(selectedBaseColor),
      style = Some(selectedStyle)
    )

    val availableThemes = RandomizeBiases.themesForBaseColor(selectedBaseColor)
    val availableFonts = RandomizeBiases.applyFontBias(Preset.Fonts, context)
    val availableRadii =
      RandomizeBiases.applyRadiusBias(Preset.Radii.map(_._1), context)

    val selectedTheme =
      if locked("theme") then current.theme
      else pick(availableThemes)

    context = context.copy(theme = Some(selectedTheme))

    val availableChartThemes =
      RandomizeBiases.applyChartColorBias(availableThemes, context)

    val selectedChartColor =
      if locked("chartColor") then current.chartColor
      else pick(availableChartThemes)

    val selectedFont =
      if locked("font") then current.font
      else pick(availableFonts)

    val selectedFontHeading =
      if locked("fontHeading") then current.fontHeading
      else if Random.nextDouble() < 0.7 then "inherit"
      else
        val bodyType = RandomizeBiases.FontTypes.get(selectedFont)
        val contrastFonts = availableFonts.filter { font =>
          font != selectedFont && RandomizeBiases.FontTypes.get(font) != bodyType
        }
        if contrastFonts.nonEmpty then pick(contrastFonts)
        else pick(availableFonts)

    val selectedRadius =
      if locked("radius") then current.radius
      else pick(availableRadii)

    val selectedIconLibrary =
      if locked("iconLibrary") then current.iconLibrary
      else pick(Preset.IconLibraries.map(_._1))

    val selectedMenuAccent =
      if locked("menuAccent") then current.menuAccent
      else pick(Preset.MenuAccents)

    val selectedMenuColor =
      if locked("menuColor") then current.menuColor
      else pick(Preset.MenuColors)

    update(_ =>
      PresetConfig(
        style = selectedStyle,
        baseColor = selectedBaseColor,
        theme = selectedTheme,
        chartColor = selectedChartColor,
        iconLibrary = selectedIconLibrary,
        font = selectedFont,
        fontHeading = selectedFontHeading,
        radius = selectedRadius,
        menuAccent = selectedMenuAccent,
        menuColor = selectedMenuColor
      )
    )

  def reset(): Unit =
    update(_ => Preset.default)

  def toggleDark(): Unit =
    val next = config.now().copy(darkMode = !config.now().darkMode)
    config.set(next)
    ThemeConfig.store(next)

  def shareUrl: String =
    s"${dom.window.location.origin}/create?preset=${Preset.encode(ThemeConfig.toPreset(config.now()))}"

  def applyPresetCode(code: String): Boolean =
    if !Preset.isPresetCode(code) then false
    else
      Preset.decode(code) match
        case Some(preset) =>
          update(_ => preset)
          true
        case None => false

  def installShortcuts(): Unit =
    if shortcutsInstalled then return
    shortcutsInstalled = true
    dom.document.addEventListener(
      "keydown",
      ((ev: dom.KeyboardEvent) => handleKeyDown(ev)): js.Function1[dom.KeyboardEvent, Unit]
    )

  private def readUrlPreset: Option[String] =
    val params = new dom.URLSearchParams(dom.window.location.search)
    val code = params.get("preset")
    if code != null && Preset.isPresetCode(code) then Some(code) else None

  private def readStoredPreset: Option[String] =
    val raw = dom.window.localStorage.getItem(presetStorageKey)
    if raw != null && Preset.isPresetCode(raw) then Some(raw) else None

  private def loadLocks(): Map[String, Boolean] =
    val defaults = CreateState.LockKeys.map(_ -> false).toMap
    Try {
      val raw = dom.window.localStorage.getItem(locksStorageKey)
      if raw == null then defaults
      else
        val parsed = js.JSON.parse(raw).asInstanceOf[js.Dynamic]
        defaults.map { case (key, fallback) =>
          key -> parsed
            .selectDynamic(key)
            .asInstanceOf[js.UndefOr[Boolean]]
            .getOrElse(fallback)
        }
    }.getOrElse(defaults)

  private def persistLocks(next: Map[String, Boolean]): Unit =
    val literal = js.Dynamic.literal(
      style = next.getOrElse("style", false),
      baseColor = next.getOrElse("baseColor", false),
      theme = next.getOrElse("theme", false),
      chartColor = next.getOrElse("chartColor", false),
      iconLibrary = next.getOrElse("iconLibrary", false),
      font = next.getOrElse("font", false),
      fontHeading = next.getOrElse("fontHeading", false),
      item = next.getOrElse("item", false),
      menuAccent = next.getOrElse("menuAccent", false),
      menuColor = next.getOrElse("menuColor", false),
      radius = next.getOrElse("radius", false),
      template = next.getOrElse("template", false)
    )
    dom.window.localStorage.setItem(locksStorageKey, js.JSON.stringify(literal))

  private def persistPreset(code: String): Unit =
    dom.window.localStorage.setItem(presetStorageKey, code)

  private def syncUrl(code: String): Unit =
    val url = new dom.URL(dom.window.location.href)
    url.searchParams.set("preset", code)
    dom.window.history.replaceState(
      js.Dynamic.literal(),
      "",
      url.pathname + url.search + url.hash
    )

  private def commitPreset(code: String, recordHistory: Boolean): Unit =
    val preset = Preset.decode(code).getOrElse(Preset.default)
    val finalCode = Preset.encode(preset)
    val nextConfig = ThemeConfig.fromPreset(preset, darkMode = config.now().darkMode)

    if recordHistory then
      val truncated = historyLog.now().take(historyCursor.now() + 1)
      val log =
        if truncated.nonEmpty && truncated.last == finalCode then truncated
        else truncated :+ finalCode
      historyLog.set(log)
      historyCursor.set(log.length - 1)

    config.set(nextConfig)
    persistPreset(finalCode)
    ThemeConfig.store(nextConfig)
    syncUrl(finalCode)

  private def applyPresetCodeFromHistory(code: String): Unit =
    commitPreset(code, recordHistory = false)

  private def isEditableTarget(ev: dom.KeyboardEvent): Boolean =
    val path =
      ev.asInstanceOf[js.Dynamic].composedPath().asInstanceOf[js.Array[dom.EventTarget]]
    if path.length == 0 then false
    else
      path(0) match
        case _: dom.html.Input    => true
        case _: dom.html.TextArea => true
        case _: dom.html.Select   => true
        case el: dom.html.Element => el.isContentEditable
        case _                    => false

  private def handleKeyDown(ev: dom.KeyboardEvent): Unit =
    val key = ev.key
    val metaOrCtrl = ev.metaKey || ev.ctrlKey

    if (key == "r" || key == "R") && !metaOrCtrl then
      if isEditableTarget(ev) then return
      ev.preventDefault()
      if ev.shiftKey then reset() else randomize()
      return

    if (key == "d" || key == "D") && !metaOrCtrl then
      if isEditableTarget(ev) then return
      ev.preventDefault()
      toggleDark()
      return

    if (key == "z" || key == "Z") && metaOrCtrl then
      ev.preventDefault()
      if ev.shiftKey then redo() else undo()

object CreateState:

  /** Twelve lock keys kept structurally compatible with the reference `Lockable` type. */
  val LockKeys: List[String] = List(
    "style",
    "baseColor",
    "theme",
    "chartColor",
    "iconLibrary",
    "font",
    "fontHeading",
    "item",
    "menuAccent",
    "menuColor",
    "radius",
    "template"
  )
