package shadcnscalajs.webcomponents

import org.scalajs.dom

import scala.collection.mutable
import scala.scalajs.js

/** Small framework-neutral theme API installed as `window.ShadcnScalaJS` by the standalone bundle.
  *
  * Design tokens are ordinary CSS custom properties. `setTokens` writes them on `<html>` and [[ScTheme]] mirrors those
  * explicit overrides into every Shadow Root after preset tokens, so they reliably win without exposing internals.
  */
private[webcomponents] object ScThemeApi:

  private val apiTokens = mutable.Set.empty[String]

  def install(): Unit =
    // `updateDynamic` emits a bare assignment, which throws in an ES module's strict mode when the global does not
    // already exist. Define the property through globalThis instead so the bundle works in a fresh plain HTML page.
    val api = js.Dynamic.literal(
      setTokens = ((tokens: js.Any) => setTokens(tokens)): js.Function1[js.Any, Unit],
      resetTokens = (() => resetTokens()): js.Function0[Unit],
      setTheme = ((theme: js.Any) => setTheme(theme)): js.Function1[js.Any, Unit]
    )
    js.Dynamic.global.Object.defineProperty(
      js.Dynamic.global.globalThis,
      "ShadcnScalaJS",
      js.Dynamic.literal(value = api, writable = true, configurable = true)
    )

  private def setTokens(raw: js.Any): Unit =
    if raw == null || js.typeOf(raw) != "object" then return
    val tokens = raw.asInstanceOf[js.Dynamic]
    val keys = js.Object.keys(raw.asInstanceOf[js.Object])
    keys.foreach { rawName =>
      val name = if rawName.startsWith("--") then rawName else s"--$rawName"
      val tokenValue = tokens.selectDynamic(rawName)
      if tokenValue == null then
        dom.document.documentElement.asInstanceOf[dom.html.Element].style.removeProperty(name)
        apiTokens -= name
      else
        dom.document.documentElement.asInstanceOf[dom.html.Element].style.setProperty(name, tokenValue.toString)
        apiTokens += name
    }
    ScTheme.refreshAll()

  private def resetTokens(): Unit =
    val styles = dom.document.documentElement.asInstanceOf[dom.html.Element].style
    apiTokens.foreach(styles.removeProperty)
    apiTokens.clear()
    ScTheme.refreshAll()

  private def setTheme(raw: js.Any): Unit =
    if raw == null || js.typeOf(raw) != "object" then return
    val theme = raw.asInstanceOf[js.Dynamic]
    stringField(theme, "stylePack").filter(ScThemeState.packs.contains).foreach(ScThemeState.setPack)
    setAttribute(theme, "baseColor", "data-base-color")
    setAttribute(theme, "themeColor", "data-theme-color")
    setAttribute(theme, "chartColor", "data-chart-color")
    setAttribute(theme, "radius", "data-radius")
    setAttribute(theme, "headingFont", "data-heading-font")
    setAttribute(theme, "bodyFont", "data-body-font")
    setAttribute(theme, "menuColor", "data-menu-color")
    setAttribute(theme, "menuAccent", "data-menu-accent")
    val dark = theme.selectDynamic("darkMode")
    if js.typeOf(dark) == "boolean" then ScThemeState.setDark(dark.asInstanceOf[Boolean])
    val tokens = theme.selectDynamic("tokens")
    if !js.isUndefined(tokens) then setTokens(tokens)
    ScTheme.refreshAll()

  private def setAttribute(theme: js.Dynamic, field: String, attribute: String): Unit =
    stringField(theme, field).foreach(dom.document.documentElement.setAttribute(attribute, _))

  private def stringField(value: js.Dynamic, field: String): Option[String] =
    val selected = value.selectDynamic(field)
    if js.typeOf(selected) == "string" then Some(selected.toString) else None
