package shadcnscalajs.site

import com.raquo.laminar.api.L.*
import org.scalajs.dom
import shadcnscalajs.ui.*

/** The documentation landing page for shadcn-scalajs.
  *
  * The page deliberately dogfoods the Laminar library: every interactive example below is rendered from the same source
  * that the CLI gives to consumers. The visual shell follows Basecoat's documentation layout while using the vendored
  * Lyra style pack.
  */
object Main:

  def main(args: Array[String]): Unit =
    val container = dom.document.getElementById("root")
    render(container, app())

  private def app(): HtmlElement =
    val darkMode = Var(false)
    val dialogOpen = Var(false)
    val accordionOpen = Var(Option(0))

    div(
      cls("site-shell"),
      cls <-- darkMode.signal.map(if _ then "dark" else ""),
      headerTag(
        cls("site-header"),
        div(
          cls("site-header-inner"),
          a(cls("brand"), href := "#top", span(cls("brand-mark"), "✣"), span("shadcn-scalajs")),
          navTag(
            cls("site-nav"),
            a(href := "#components", "Components"),
            a(href := "#installation", "Installation"),
            a(href := "/plain-html-demo.html", "Web Components")
          ),
          div(
            cls("site-actions"),
            a(cls("icon-link"), href := "https://github.com", target := "_blank", aria.label := "GitHub", "↗"),
            Button(onClick --> { _ => darkMode.update(!_) }, "Theme")
          )
        )
      ),
      mainTag(
        idAttr := "top",
        cls("site-main"),
        sectionTag(
          cls("hero"),
          Badge.of(_.variant(Badge.Variant.Outline), _ => "SCALA.JS · LAMINAR · BASECOAT"),
          h1("The shadcn/ui philosophy,\nwithout React."),
          p(
            cls("hero-copy"),
            "Copy the source. Own every line. Build accessible UI elements with Scala.js and Laminar, styled by the Lyra design system."
          ),
          div(
            cls("hero-actions"),
            Button(onClick --> { _ => dom.window.location.hash = "installation" }, "Get started"),
            Button.of(_.variant(Button.Variant.Outline), _ => "Explore components")
          ),
          div(cls("hero-meta"), span(cls("status-dot")), span("v0.1 · five components · open source"))
        ),
        sectionTag(
          cls("bento-grid"),
          articleTag(
            cls("bento-card bento-card-dark"),
            span(cls("eyebrow"), "LAMINAR FIRST"),
            h2("A component library that feels like Scala."),
            p("Typed builders, native elements, and Airstream state — compiled into browser-ready UI."),
            div(cls("mini-code"), "Button.of(_.variant(Primary), _ => \"Ship it\")")
          ),
          articleTag(
            cls("bento-card"),
            span(cls("eyebrow"), "COPY · PASTE · OWN"),
            h2("No runtime lock-in."),
            p("The CLI writes readable Scala files into your project. Your components stay yours."),
            div(cls("metric"), span(cls("metric-value"), "0"), span(" generated React dependencies"))
          ),
          articleTag(
            cls("bento-card bento-card-accent"),
            span(cls("eyebrow"), "LYRA STYLE PACK"),
            h2("Design tokens that travel."),
            p("Use the same Basecoat and shadcn themes across native Laminar and standalone Web Components."),
            div(
              cls("token-row"),
              span(cls("token-swatch token-swatch-primary")),
              span(cls("token-swatch token-swatch-muted")),
              span(cls("token-swatch token-swatch-border"))
            )
          ),
          articleTag(
            cls("bento-card bento-card-wide"),
            div(
              cls("wide-copy"),
              span(cls("eyebrow"), "TWO OUTPUTS, ONE SOURCE"),
              h2("Laminar components → UI elements"),
              p(
                "Build once in modules/ui. Consume directly from Scala.js or publish as <sc-button>, <sc-dialog>, and more."
              )
            ),
            div(
              cls("flow-diagram"),
              span(cls("flow-node"), "Button.scala"),
              span(cls("flow-arrow"), "→"),
              span(cls("flow-node"), "sc-button"),
              span(cls("flow-arrow"), "→"),
              span(cls("flow-node"), "any web stack")
            )
          )
        ),
        sectionTag(
          idAttr := "components",
          cls("section-block"),
          div(
            cls("section-heading"),
            div(
              span(cls("eyebrow"), "THE LIBRARY"),
              h2("Small primitives. Composable by design."),
              p("These examples are live Laminar components, not screenshots.")
            )
          ),
          div(
            cls("component-showcase"),
            articleTag(
              cls("showcase-card"),
              div(
                cls("showcase-card-header"),
                h3("Button"),
                Badge.of(_.variant(Badge.Variant.Secondary), _ => "pure CSS")
              ),
              p("Variants are typed and map directly to Basecoat data attributes."),
              div(
                cls("showcase-controls"),
                Button("Primary"),
                Button.of(_.variant(Button.Variant.Outline), _ => "Outline"),
                Button.of(_.variant(Button.Variant.Destructive), _.size(Button.Size.Sm), _ => "Delete")
              )
            ),
            articleTag(
              cls("showcase-card"),
              div(
                cls("showcase-card-header"),
                h3("Badge"),
                Badge.of(_.variant(Badge.Variant.Secondary), _ => "pure CSS")
              ),
              p("Compact status labels with the same theme tokens."),
              div(
                cls("showcase-controls"),
                Badge.of(_.variant(Badge.Variant.Primary), _ => "New"),
                Badge.of(_.variant(Badge.Variant.Outline), _ => "Beta"),
                Badge.of(_.variant(Badge.Variant.Destructive), _ => "Deprecated")
              )
            ),
            articleTag(
              cls("showcase-card"),
              div(
                cls("showcase-card-header"),
                h3("Dialog"),
                Badge.of(_.variant(Badge.Variant.Secondary), _ => "native <dialog>")
              ),
              p("Browser primitives keep interaction accessible and dependency-free."),
              Button(onClick --> { _ => dialogOpen.set(true) }, "Open dialog"),
              Dialog(dialogOpen)(
                div(
                  cls("dialog-content"),
                  h3("Built with Laminar"),
                  p("This is a native dialog rendered from the UI library."),
                  Button(onClick --> { _ => dialogOpen.set(false) }, "Close")
                )
              )
            ),
            articleTag(
              cls("showcase-card"),
              div(
                cls("showcase-card-header"),
                h3("Accordion"),
                Badge.of(_.variant(Badge.Variant.Secondary), _ => "native <details>")
              ),
              p("Progressive disclosure with a typed single-open state."),
              Accordion(
                accordionOpen,
                Accordion.Section("Why Scala.js?", "Share types and behavior from the server to the browser."),
                Accordion.Section("Why Basecoat?", "Semantic HTML and shadcn-compatible design tokens.")
              )
            )
          )
        ),
        sectionTag(
          idAttr := "installation",
          cls("install-section"),
          div(
            cls("section-heading"),
            div(
              span(cls("eyebrow"), "GET STARTED"),
              h2("Install the library. Keep the source."),
              p("Initialize a project, add a component, and compile it with sbt.")
            )
          ),
          div(
            cls("install-grid"),
            div(
              cls("install-copy"),
              h3("1. Initialize"),
              p("Point the CLI at the registry and choose your Scala source directory."),
              div(
                cls("code-block"),
                code("node shadcn-scalajs init --registry ./registry"),
                code("node shadcn-scalajs add button dialog")
              )
            ),
            div(
              cls("install-copy"),
              h3("2. Use it"),
              p("Import the copied component in any Laminar view."),
              div(cls("code-block"), code("import shadcnscalajs.ui.Button"), code("Button(\"Hello from Scala.js\")"))
            )
          )
        )
      ),
      footerTag(
        cls("site-footer"),
        span("shadcn-scalajs"),
        span("Built with Scala.js, Laminar, and Basecoat Lyra."),
        a(href := "/plain-html-demo.html", "See the Web Component demo →")
      )
    )
