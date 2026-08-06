package shadcnscalajs.site

import org.scalajs.dom

import scala.scalajs.js
import scala.util.Try

/** Site-wide theme/customizer state, persisted to `localStorage` so it survives real browser navigations — this site
  * has no client-side router, so `Main.main()` re-runs from scratch on every page load with fresh `Var`s. Every page's
  * mount function seeds a `Var[ThemeConfig]` from `load()` and applies it via `applyToDocument`; any control that
  * changes a field calls `store` so the next page load picks it up. See
  * docs/superpowers/specs/2026-08-06-create-theme-customizer-design.md for the full design.
  */
final case class ThemeConfig(
    stylePack: String = "lyra",
    darkMode: Boolean = false,
    baseColor: String = "neutral",
    themeColor: String = "orange",
    chartColor: String = "orange",
    headingFont: String = "default",
    bodyFont: String = "default",
    iconLibrary: String = "lucide",
    radius: String = "default",
    menuColor: String = "default",
    menuAccent: String = "subtle"
)

object ThemeConfig:

  private val storageKey = "shadcn-scalajs:theme"

  val default: ThemeConfig = ThemeConfig()

  /** Reads the persisted config, falling back to `default` on a missing key, a JSON parse error, or a corrupt shape — a
    * user editing localStorage by hand (or an old, differently-shaped value from before a field was added) must never
    * crash page load.
    */
  def load(): ThemeConfig =
    Try {
      val raw = dom.window.localStorage.getItem(storageKey)
      if raw == null then default
      else
        val parsed = js.JSON.parse(raw).asInstanceOf[js.Dynamic]
        def str(field: String, fallback: String): String =
          parsed.selectDynamic(field).asInstanceOf[js.UndefOr[String]].getOrElse(fallback)
        def bool(field: String, fallback: Boolean): Boolean =
          parsed.selectDynamic(field).asInstanceOf[js.UndefOr[Boolean]].getOrElse(fallback)
        ThemeConfig(
          stylePack = str("stylePack", default.stylePack),
          darkMode = bool("darkMode", default.darkMode),
          baseColor = str("baseColor", default.baseColor),
          themeColor = str("themeColor", default.themeColor),
          chartColor = str("chartColor", default.chartColor),
          headingFont = str("headingFont", default.headingFont),
          bodyFont = str("bodyFont", default.bodyFont),
          iconLibrary = str("iconLibrary", default.iconLibrary),
          radius = str("radius", default.radius),
          menuColor = str("menuColor", default.menuColor),
          menuAccent = str("menuAccent", default.menuAccent)
        )
    }.getOrElse(default)

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

  /** Sets every `data-*` attribute `globals.css`'s attribute-selector blocks key off of, plus the `dark` class, on
    * `<html>` itself — not some inner div. `rem`-based Tailwind classes (used throughout this codebase) only ever
    * resolve against `<html>`'s own state, never an ancestor div's; putting `dark` here too (rather than on each page's
    * own root div, as today) means the `&:is(.dark *)` custom variant in globals.css covers the *entire* document
    * unconditionally, which is a strict superset of today's behavior, not a behavior change.
    */
  def applyToDocument(cfg: ThemeConfig): Unit =
    val html = dom.document.documentElement
    html.setAttribute("data-style-pack", cfg.stylePack)
    html.setAttribute("data-base-color", cfg.baseColor)
    html.setAttribute("data-theme-color", cfg.themeColor)
    html.setAttribute("data-chart-color", cfg.chartColor)
    html.setAttribute("data-heading-font", cfg.headingFont)
    html.setAttribute("data-body-font", cfg.bodyFont)
    html.setAttribute("data-icon-library", cfg.iconLibrary)
    html.setAttribute("data-radius", cfg.radius)
    if cfg.darkMode then html.classList.add("dark") else html.classList.remove("dark")
