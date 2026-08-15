package shadcnscalajs.site

import com.raquo.laminar.api.L.*
import org.scalajs.dom
import shadcnscalajs.ui.*
import com.raquo.laminar.codecs.StringAsIsCodec

import scala.scalajs.js

/** Documentation and a small live preview for the standalone Web Component bundle. */
object WebComponentsPage:
  private val cardTag = htmlTag("sc-card")
  private val cardHeaderTag = htmlTag("sc-card-header")
  private val cardTitleTag = htmlTag("sc-card-title")
  private val cardDescriptionTag = htmlTag("sc-card-description")
  private val cardContentTag = htmlTag("sc-card-content")
  private val badgeTag = htmlTag("sc-badge")
  private val buttonTag = htmlTag("sc-button")
  private val variantAttr = htmlAttr("variant", StringAsIsCodec)

  def apply(): HtmlElement =
    val ready = Var(false)
    loadBundle(ready)

    div(
      cls := "mx-auto w-full max-w-5xl px-5 py-10 sm:px-8 lg:px-10",
      h1(cls := "text-3xl font-semibold tracking-tight", "Web Components"),
      p(
        cls := "mt-2 max-w-2xl text-base text-muted-foreground",
        "Use the generated custom elements from plain HTML, React, Vue, or any browser application."
      ),
      div(
        cls := "mt-8 grid gap-8 lg:grid-cols-[minmax(0,1fr)_18rem]",
        mainTag(
          cls := "min-w-0",
          h2(idAttr := "quick-start", cls := "text-xl font-semibold", "Quick start"),
          p(
            cls := "mt-3 text-sm leading-6 text-muted-foreground",
            "Load the bundle once, then compose custom elements with regular HTML attributes and slots."
          ),
          DocsPresentation.codeBlock(
            "html",
            "Web Components",
            """<script type=\"module\" src=\"/sc-components.js\"></script>

<sc-card>
  <sc-card-header>
    <sc-card-title>Welcome back</sc-card-title>
    <sc-card-description>Your account is ready.</sc-card-description>
  </sc-card-header>
  <sc-card-content>
    <sc-button variant=\"primary\">Continue</sc-button>
  </sc-card-content>
</sc-card>"""
          ),
          h2(idAttr := "live-preview", cls := "mt-12 text-xl font-semibold", "Live preview"),
          p(
            cls := "mt-3 text-sm leading-6 text-muted-foreground",
            "The preview below uses the same generated elements as the standalone demo."
          ),
          Card(
            cls := s"mt-4 ${DocsPresentation.frameClasses} p-6!",
            display <-- ready.signal.map(if _ then "block" else "none"),
            cardTag(
              cardHeaderTag(
                cardTitleTag("A native custom element"),
                cardDescriptionTag("Shadow DOM styling and document theme changes stay synchronized.")
              ),
              cardContentTag(
                cls := "flex items-center gap-3",
                badgeTag(variantAttr := "secondary", "Ready"),
                buttonTag(variantAttr := "primary", "Continue")
              )
            )
          ),
          div(
            cls := "mt-4 rounded-lg border border-dashed p-6 text-sm text-muted-foreground",
            display <-- ready.signal.map(if _ then "none" else "block"),
            "Loading Web Components..."
          ),
          h2(idAttr := "events-and-slots", cls := "mt-12 text-xl font-semibold", "Events and slots"),
          p(
            cls := "mt-3 text-sm leading-6 text-muted-foreground",
            "Interactive controls emit composed, bubbling sc-change events. Slot-based elements such as sc-dropdown-menu accept their trigger through a named slot."
          ),
          DocsPresentation.codeBlock(
            "html",
            "Web Components",
            """<sc-dropdown-menu items='[{\"label\":\"Profile\"}]'>
  <sc-button slot=\"trigger\" variant=\"outline\">Open</sc-button>
</sc-dropdown-menu>

<script>
  document.querySelector(\"sc-select\")?.addEventListener(\"sc-change\", event => {
    console.log(event.detail)
  })
</script>"""
          )
        ),
        asideTag(
          cls := "hidden lg:block",
          navTag(
            cls := "sticky top-20 space-y-2 border-l pl-4 text-sm",
            aria.label := "On this page",
            a(href := "#quick-start", cls := "block text-muted-foreground hover:text-foreground", "Quick start"),
            a(href := "#live-preview", cls := "block text-muted-foreground hover:text-foreground", "Live preview"),
            a(
              href := "#events-and-slots",
              cls := "block text-muted-foreground hover:text-foreground",
              "Events and slots"
            ),
            a(
              href := "/plain-html-demo.html",
              cls := "block pt-3 text-muted-foreground hover:text-foreground",
              "Standalone mosaic"
            )
          )
        )
      )
    )

  private def loadBundle(ready: Var[Boolean]): Unit =
    dom.document.documentElement.setAttribute("data-sc-assets-base", "/")
    val script = dom.document.createElement("script").asInstanceOf[dom.html.Script]
    script.setAttribute("type", "module")
    script.src = "/sc-components.js"
    def waitForBundle(): Unit =
      if js.isUndefined(js.Dynamic.global.customElements.get("sc-button")) then
        dom.window.requestAnimationFrame(_ => waitForBundle())
      else ready.set(true)

    script.onload = _ => waitForBundle()
    dom.document.head.appendChild(script)
