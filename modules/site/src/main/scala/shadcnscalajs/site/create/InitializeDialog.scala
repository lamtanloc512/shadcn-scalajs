package shadcnscalajs.site.create

import com.raquo.laminar.api.L.*
import org.scalajs.dom

import scala.scalajs.js.Thenable.Implicits.*
import scala.scalajs.js.timers.setTimeout
import scala.concurrent.ExecutionContext.Implicits.global
import shadcnscalajs.site.ThemeConfig
import shadcnscalajs.ui.{Button, Dialog, Icons}

/** Initialize-project dialog and trigger — shows the CLI init command for the current preset. Ported from
  * `initialize-dialog.svelte`.
  */
object InitializeDialog:

  final class Instance private[InitializeDialog] (
      private val state: CreateState,
      val openVar: Var[Boolean],
      private val triggerRef: Var[Option[dom.html.Element]]
  ):
    private val copiedVar = Var(false)

    private def initCommand(code: String): String =
      s"npx shadcn-scalajs@latest init --preset $code"

    private val commandText: Signal[String] = state.presetCode.map(initCommand)

    private def focusTrigger(): Unit = triggerRef.now().foreach(_.focus())

    private def copyCommand(): Unit =
      val text = initCommand(Preset.encode(ThemeConfig.toPreset(state.config.now())))
      val _ = dom.window.navigator.clipboard.writeText(text).toFuture.foreach { _ =>
        copiedVar.set(true)
        setTimeout(2000)(copiedVar.set(false))
      }

    def trigger(mods: Modifier[HtmlElement]*): HtmlElement =
      Button
        .of(
          _.variant(Button.Variant.Primary),
          _.size(Button.Size.Default),
          _ => onMountCallback { ctx => triggerRef.set(Some(ctx.thisNode.ref)) },
          _ => onClick --> { (_: dom.MouseEvent) => openVar.set(true) },
          _ => Icons.squareTerminal(),
          _ => "Initialize Project"
        )
        .amend(mods)

    def panel: HtmlElement =
      Dialog(openVar)(
        openVar.signal --> { open =>
          if !open then focusTrigger()
        },
        cls := "w-full max-w-lg!",
        headerTag(
          h2(cls := "text-lg font-semibold", "Initialize Project"),
          p(
            cls := "text-sm text-muted-foreground",
            "Run the following command to initialize your project with the current preset."
          )
        ),
        div(
          cls := "-mb-6 w-full md:mx-0 rounded-lg border border-border bg-muted/50 p-4 text-sm",
          div(
            cls := "flex items-start gap-3",
            Icons.bookOpen(svg.cls := "mt-0.5 size-4 shrink-0"),
            div(
              p(cls := "font-medium", "Set up your project first"),
              p(
                cls := "mt-1 text-muted-foreground",
                "Refer to the ",
                a(
                  href := "/",
                  cls := "font-medium underline underline-offset-4 hover:text-primary",
                  "installation docs"
                ),
                " for framework setup before initializing shadcn-scalajs with the command below."
              )
            )
          )
        ),
        pre(
          cls := "overflow-x-auto rounded-lg border border-border bg-muted/50 p-4 font-mono text-sm",
          code(text <-- commandText)
        ),
        footerTag(
          cls := "flex flex-col-reverse gap-2 sm:flex-row sm:justify-end",
          Button.of(
            _.variant(Button.Variant.Primary),
            _.size(Button.Size.Default),
            _ => cls := "w-full",
            _ => onClick --> { (_: dom.MouseEvent) => copyCommand() },
            _ => child <-- copiedVar.signal.map(copied => if copied then Icons.check() else Icons.copy()),
            _ => span(text <-- copiedVar.signal.map(copied => if copied then "Copied" else "Copy Command"))
          )
        )
      )

  def apply(state: CreateState): Instance =
    Instance(state, Var(false), Var(Option.empty))
