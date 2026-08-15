package shadcnscalajs.webcomponents

import org.scalajs.dom

import scala.scalajs.js

/** The site's persisted theme, read and written from the Web Component bundle.
  *
  * Deliberately the same `localStorage` key, JSON shape and `<html>` attributes as the site's own `ThemeConfig`, so a
  * pack chosen on a plain HTML page survives navigating into the Laminar app and back. This bundle cannot depend on
  * `modules/site` — the site depends on *it* — so the key and the attribute list are duplicated here rather than
  * shared.
  *
  * `/create` seeds its own customizer from a separate preset-code key that this object doesn't write, so a pack chosen
  * here shows up in every site header but not preselected in the customizer itself.
  */
private[webcomponents] object ScThemeState:

  private val storageKey = "shadcn-scalajs:theme"

  val packs: List[String] = List("nova", "vega", "maia", "lyra", "mira", "luma", "sera", "rhea")

  /** Every `<html>` attribute the token blocks in globals.css key off, paired with the JSON field carrying it. */
  private val attributes: List[(String, String)] = List(
    "data-style-pack" -> "stylePack",
    "data-base-color" -> "baseColor",
    "data-theme-color" -> "themeColor",
    "data-chart-color" -> "chartColor",
    "data-heading-font" -> "headingFont",
    "data-body-font" -> "bodyFont",
    "data-icon-library" -> "iconLibrary",
    "data-radius" -> "radius",
    "data-menu-color" -> "menuColor",
    "data-menu-accent" -> "menuAccent"
  )

  val cssAttributes: js.Array[String] = js.Array(attributes.map(_._1)*)

  /** Mirrors the stored theme onto `<html>`, so components match whatever was last chosen anywhere on the site. */
  def applyStored(): Unit =
    val cfg = stored()
    val html = dom.document.documentElement
    attributes.foreach((attr, field) => stringField(cfg, field).foreach(html.setAttribute(attr, _)))
    setDarkClass(js.DynamicImplicits.truthValue(cfg.selectDynamic("darkMode")))

  def currentPack(): String =
    stringField(stored(), "stylePack")
      .filter(packs.contains)
      .orElse(ScStyles.bakedPack)
      .getOrElse(packs.head)

  def isDark(): Boolean = dom.document.documentElement.classList.contains("dark")

  /** One of `default`, `inverted`, `default-translucent`, `inverted-translucent`. */
  def menuColor(): String = stringField(stored(), "menuColor").getOrElse("default")

  def setPack(name: String): Unit =
    val html = dom.document.documentElement
    html.setAttribute("data-style-pack", name)
    // The site pairs each pack with a radius: lyra and sera are square, every other pack uses the token default.
    val radius = if name == "lyra" || name == "sera" then "none" else "default"
    html.setAttribute("data-radius", radius)
    merge { cfg =>
      cfg.updateDynamic("stylePack")(name)
      cfg.updateDynamic("radius")(radius)
    }

  def setDark(on: Boolean): Unit =
    setDarkClass(on)
    merge(_.updateDynamic("darkMode")(on))

  private def setDarkClass(on: Boolean): Unit =
    val classes = dom.document.documentElement.classList
    if on then classes.add("dark") else classes.remove("dark")

  private def stored(): js.Dynamic =
    try
      val raw = dom.window.localStorage.getItem(storageKey)
      if raw == null then js.Dynamic.literal()
      else
        val parsed = js.JSON.parse(raw)
        if js.typeOf(parsed) == "object" && parsed != null then parsed.asInstanceOf[js.Dynamic]
        else js.Dynamic.literal()
    catch case _: Throwable => js.Dynamic.literal()

  private def stringField(cfg: js.Dynamic, field: String): Option[String] =
    val value = cfg.selectDynamic(field)
    if js.typeOf(value) == "string" then Some(value.toString) else None

  /** Read–modify–write, so fields this bundle doesn't model — fonts, colors, icon library — survive a pack change. */
  private def merge(update: js.Dynamic => Unit): Unit =
    val cfg = stored()
    update(cfg)
    try dom.window.localStorage.setItem(storageKey, js.JSON.stringify(cfg))
    catch case _: Throwable => ()
