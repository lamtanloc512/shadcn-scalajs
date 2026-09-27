package shadcnscalajs.webcomponents

import com.raquo.laminar.api.L.*
import org.scalajs.dom
import shadcnscalajs.ui.ThemeSwitcher
import shadcnscalajs.ui.ThemeSwitcher.Theme

import scala.scalajs.js

class ScThemeSwitcher extends ScElementBase:

  private val themeVar = Var(Theme.System)
  private val echo = EchoGuard[Theme]()
  private var mediaQuery: Option[dom.MediaQueryList] = None
  private val onSystemChange: js.Function1[dom.Event, Unit] = _ => applySystemTheme()

  observeAttribute("theme") { raw =>
    val next = ScThemeSwitcher.parse(raw)
    echo.wrote(next)
    themeVar.set(next)
    applyTheme(next)
  }
  stringProperty("theme")

  mount(
    ThemeSwitcher(themeVar).amend(
      themeVar.signal.changes --> Observer[Theme] { next =>
        if !echo.isEcho(next) then
          this.setAttribute("theme", next.value)
          applyTheme(next)
          emit("sc-change", next.value)
      }
    )
  )

  override def connectedCallback(): Unit =
    super.connectedCallback()
    // An unconfigured switcher must not override the host page's current theme just by mounting.
    if hasAttribute("theme") then applyTheme(themeVar.now())

  override def disconnectedCallback(): Unit =
    stopSystemTheme()
    super.disconnectedCallback()

  private def applyTheme(theme: Theme): Unit =
    if this.isConnected then
      theme match
        case Theme.Light =>
          stopSystemTheme()
          ScThemeState.setDark(false)
        case Theme.Dark =>
          stopSystemTheme()
          ScThemeState.setDark(true)
        case Theme.System =>
          if mediaQuery.isEmpty then
            val query = dom.window.matchMedia("(prefers-color-scheme: dark)")
            mediaQuery = Some(query)
            query.addEventListener("change", onSystemChange)
          applySystemTheme()

  private def applySystemTheme(): Unit =
    mediaQuery.foreach { query =>
      val classes = dom.document.documentElement.classList
      if query.matches then classes.add("dark") else classes.remove("dark")
    }

  private def stopSystemTheme(): Unit =
    mediaQuery.foreach(_.removeEventListener("change", onSystemChange))
    mediaQuery = None

object ScThemeSwitcher:
  def register(): Unit =
    ScElements.define("sc-theme-switcher", js.constructorOf[ScThemeSwitcher], "theme")

  private def parse(raw: Option[String]): Theme =
    Theme.values.find(_.value == raw.orNull).getOrElse(Theme.System)
