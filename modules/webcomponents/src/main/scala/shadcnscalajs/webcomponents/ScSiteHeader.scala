package shadcnscalajs.webcomponents

import com.raquo.laminar.api.L.*
import shadcnscalajs.ui.{Button, DropdownMenu, Icons}

import scala.scalajs.js

/** `<sc-site-header>` — the site's sticky header for pages outside the Laminar app, with a working style-pack picker and
  * dark-mode toggle.
  *
  * A clone rather than a reuse of `SiteChrome`: `modules/site` depends on this module, so importing it back would be a
  * cycle. The layout and the nav destinations are copied from `SiteChrome.header`; the theme control is the pack and dark
  * parts of `ThemeMenu` only, and links to `/create` for the rest of the customizer, exactly as that menu does.
  */
class ScSiteHeader extends ScElementBase:

  // The host is the element that participates in page layout, so stickiness belongs here: a `position: sticky` element
  // inside the shadow root is confined to the host's own header-height box and would never travel with the scroll.
  this.style.display = "block"
  this.style.position = "sticky"
  this.style.top = "0"
  this.style.zIndex = "40"

  ScThemeState.applyStored()

  private val packVar = Var(ScThemeState.currentPack())
  private val darkVar = Var(ScThemeState.isDark())

  mount(ScSiteHeader.view(packVar, darkVar))

object ScSiteHeader:

  def register(): Unit = ScElements.define("sc-site-header", js.constructorOf[ScSiteHeader])

  private val triggerClasses =
    "inline-flex h-8 shrink-0 items-center justify-center gap-2 rounded-md border border-input bg-background px-2.5 text-sm font-medium whitespace-nowrap transition-colors outline-none hover:bg-accent hover:text-accent-foreground focus-visible:ring-[3px] focus-visible:ring-ring/50 data-[state=open]:bg-accent"

  private val iconButtonClasses =
    "inline-flex size-8 shrink-0 items-center justify-center rounded-md text-sm transition-colors outline-none hover:bg-accent hover:text-accent-foreground focus-visible:ring-[3px] focus-visible:ring-ring/50"

  private def navGhost(href: String, mods: Modifier[HtmlElement]*): HtmlElement =
    Button.anchor(href, Button.ButtonApi.variant(Button.Variant.Ghost), mods)

  private def view(packVar: Var[String], darkVar: Var[Boolean]): HtmlElement =
    headerTag(
      cls := "flex shrink-0 items-center gap-2 border-b bg-background/95 backdrop-blur",
      div(
        cls := "flex h-14 w-full items-center justify-between gap-2 px-4",
        div(
          cls := "flex min-w-0 items-center gap-1",
          navGhost("/", aria.label := "shadcn-scalajs home", span(cls := "truncate font-semibold", "shadcn-scalajs")),
          navTag(
            cls := "hidden items-center gap-1 md:flex",
            aria.label := "Primary",
            navGhost("/", "Home"),
            navGhost("/components", "Components"),
            navGhost("/blocks", "Blocks"),
            navGhost("/create", "Create"),
            navGhost("/plain-html-demo.html", cls := "bg-accent text-accent-foreground", "Web Components")
          )
        ),
        div(
          cls := "ml-auto flex min-w-0 flex-1 items-center justify-end gap-2",
          packPicker(packVar),
          darkToggle(darkVar)
        )
      )
    )

  private def packPicker(packVar: Var[String]): HtmlElement =
    DropdownMenu.withTrigger(cls := triggerClasses, DropdownMenu.Align.End)(
      aria.label := "Style pack",
      Icons.paintbrush(svg.cls := "size-4"),
      span(cls := "hidden sm:inline", child.text <-- packVar.signal.map(_.capitalize)),
      Icons.chevronDown(svg.cls := "size-3.5 opacity-60")
    )(
      ScThemeState.packs.map { name =>
        DropdownMenu.Item.checkbox(
          name.capitalize,
          packVar.signal.map(_ == name),
          () => {
            packVar.set(name)
            ScThemeState.setPack(name)
          }
        )
      }*
    )

  private def darkToggle(darkVar: Var[Boolean]): HtmlElement =
    button(
      typ := "button",
      cls := iconButtonClasses,
      aria.label := "Toggle dark mode",
      onClick --> { _ =>
        val next = !darkVar.now()
        darkVar.set(next)
        ScThemeState.setDark(next)
      },
      child <-- darkVar.signal.map(on => if on then Icons.sun(svg.cls := "size-4") else Icons.moon(svg.cls := "size-4"))
    )
