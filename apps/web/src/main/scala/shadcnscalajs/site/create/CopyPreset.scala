package shadcnscalajs.site.create

import com.raquo.laminar.api.L.*
import org.scalajs.dom

import scala.scalajs.js
import scala.scalajs.js.Thenable.Implicits.*
import scala.scalajs.js.timers.setTimeout
import scala.concurrent.ExecutionContext.Implicits.global
import shadcnscalajs.site.ThemeConfig
import shadcnscalajs.ui.{Button, Icons}

/** Footer control that copies `--preset <code>` to the clipboard. Ported from `copy-preset.svelte`. */
object CopyPreset:

  def apply(state: CreateState, mods: Modifier[HtmlElement]*): HtmlElement =
    val copiedVar = Var(false)

    def handleCopy(): Unit =
      val code = Preset.encode(ThemeConfig.toPreset(state.config.now()))
      val _ = dom.window.navigator.clipboard.writeText(s"--preset $code").toFuture.foreach { _ =>
        copiedVar.set(true)
        setTimeout(2000)(copiedVar.set(false))
      }

    // Apply caller mods outside Button.of — splat of mapped builders cannot sit mid-arg-list.
    Button
      .of(
        _.variant(Button.Variant.Outline),
        _.size(Button.Size.Default),
        _ =>
          cls := "touch-manipulation bg-transparent! px-2! py-0! text-sm! transition-none select-none hover:bg-muted! pointer-coarse:h-10!",
        _ => onClick --> { (_: dom.MouseEvent) => handleCopy() },
        _ => span(text <-- state.presetCode.map(code => s"--preset $code")),
        _ => child <-- copiedVar.signal.map(copied => if copied then Icons.check() else Icons.copy())
      )
      .amend(mods)
