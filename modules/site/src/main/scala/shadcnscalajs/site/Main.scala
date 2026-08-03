package shadcnscalajs.site

import com.raquo.laminar.api.L.*
import org.scalajs.dom
import shadcnscalajs.ui.*

/** Docs/demo app: dogfoods `modules/ui` directly via native Laminar usage (as opposed to the `webcomponents` module's
  * custom-element wrappers, demonstrated separately by `public/plain-html-demo.html`).
  */
object Main:

  def main(args: Array[String]): Unit =
    val container = dom.document.getElementById("root")
    render(container, app())

  private def app(): HtmlElement =
    val dialogOpenVar = Var(false)
    val accordionOpenVar = Var(Option(0))

    div(
      idAttr := "demo",
      h1("shadcn-scalajs"),
      p("Native Laminar usage of modules/ui. See plain-html-demo.html for the Web Component version."),
      h2("Button"),
      div(
        Button.of(_.variant(Button.Variant.Primary), _ => "Primary"),
        " ",
        Button.of(_.variant(Button.Variant.Outline), _ => "Outline"),
        " ",
        Button.of(_.variant(Button.Variant.Destructive), _.size(Button.Size.Sm), _ => "Delete")
      ),
      h2("Badge"),
      div(
        Badge.of(_.variant(Badge.Variant.Primary), _ => "New"),
        " ",
        Badge.of(_.variant(Badge.Variant.Outline), _ => "Beta")
      ),
      h2("Dialog"),
      Button(onClick --> { _ => dialogOpenVar.set(true) }, "Open dialog"),
      Dialog(dialogOpenVar)(
        div(
          p("This is a native <dialog> element — no focus-trap JS needed."),
          Button(onClick --> { _ => dialogOpenVar.set(false) }, "Close")
        )
      ),
      h2("Accordion"),
      Accordion(
        accordionOpenVar,
        Accordion.Section("What is shadcn-scalajs?", "A Scala.js + Laminar port of shadcn/ui, styled with basecoat."),
        Accordion
          .Section("Is it a Web Component too?", "Yes — every component also compiles to a standalone custom element.")
      ),
      h2("Dropdown Menu"),
      DropdownMenu("Open menu")(
        DropdownMenu.Item("Profile", () => dom.console.log("Profile selected")),
        DropdownMenu.Item("Log out", () => dom.console.log("Log out selected"))
      )
    )
