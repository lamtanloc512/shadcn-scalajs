package shadcnscalajs.site.create

import com.raquo.laminar.api.L.*
import org.scalajs.dom

import shadcnscalajs.site.Main
import shadcnscalajs.ui.{Button, Dialog}

/** First-visit welcome dialog persisted in localStorage. Ported from `welcome-dialog.svelte`. */
object WelcomeDialog:

  private val storageKey = "shadcn-create-welcome-dialog"

  private def isDismissed: Boolean =
    dom.window.localStorage.getItem(storageKey) == "true"

  private def persistDismissed(): Unit =
    dom.window.localStorage.setItem(storageKey, "true")

  def apply(): HtmlElement =
    val openVar = Var(!isDismissed)

    def dismiss(): Unit =
      persistDismissed()
      openVar.set(false)

    // The `!` matters: style packs set padding and gap on `.cn-dialog-content` from an unlayered rule, which a plain
    // `p-0` cannot outrank. This panel is edge-to-edge artwork, so it has to win.
    Dialog(openVar, "max-w-92! min-w-0 gap-0! overflow-hidden p-0! sm:max-w-sm")(
      // Escape / backdrop close Dialog → openVar; persist so it stays dismissed.
      openVar.signal --> { open =>
        if !open then persistDismissed()
      },
      cls := "dialog-ring rounded-xl dark:bg-neutral-900",
      div(
        cls := "flex aspect-[2/1.2] w-full items-center justify-center rounded-t-xl bg-neutral-950 text-center text-neutral-100 sm:aspect-2/1",
        div(
          cls := "font-mono text-2xl font-bold",
          span(cls := "[&_svg]:size-12", foreignHtmlElement(Main.logoEl))
        )
      ),
      headerTag(
        cls := "gap-1 p-4",
        h2(cls := "text-left text-base font-semibold", "Build your own shadcn-scalajs"),
        p(
          cls := "text-left leading-relaxed text-foreground text-sm text-muted-foreground",
          "Customize everything from the ground up. Pick your component library, font, color scheme, and more."
        ),
        p(
          cls := "mt-2 text-left text-sm leading-relaxed font-medium text-foreground",
          "Available for Scala.js projects with Laminar."
        )
      ),
      footerTag(
        cls := "m-0 flex flex-col-reverse gap-2 sm:flex-row sm:justify-end p-4 pt-0",
        Button.of(
          _.variant(Button.Variant.Primary),
          _.size(Button.Size.Default),
          _ => cls := "w-full rounded-lg shadow-none",
          _ => onClick --> { (_: dom.MouseEvent) => dismiss() },
          _ => "Get Started"
        )
      )
    )
