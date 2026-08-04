package shadcnscalajs.ui

import com.raquo.laminar.api.L.*

object ThemeSwitcher:
  enum Theme(val value: String) derives CanEqual:
    case Light extends Theme("light")
    case Dark extends Theme("dark")
    case System extends Theme("system")

  def apply(themeVar: Var[Theme], mods: Modifier[HtmlElement]*): HtmlElement =
    select(
      cls := "select theme-switcher",
      aria.label := "Theme",
      value <-- themeVar.signal.map(_.value),
      onChange.mapToValue --> { value =>
        themeVar.set(Theme.values.find(_.value == value).getOrElse(Theme.System))
      },
      option(value := Theme.Light.value, "Light"),
      option(value := Theme.Dark.value, "Dark"),
      option(value := Theme.System.value, "System"),
      mods
    )
