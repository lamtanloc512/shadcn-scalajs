package shadcnscalajs.site.create

import com.raquo.laminar.api.L.*
import org.scalajs.dom
import shadcnscalajs.site.ThemeConfig
import shadcnscalajs.ui.*

/** Embedded preview frame for the create shell — same-origin iframe with live theme sync and maximize control. */
object PreviewFrame:

  private val buttonBase =
    "btn cn-button inline-flex shrink-0 items-center justify-center gap-2 rounded-md text-sm font-medium whitespace-nowrap transition-all outline-none focus-visible:border-ring focus-visible:ring-[3px] focus-visible:ring-ring/50 disabled:pointer-events-none disabled:opacity-50 [&_svg]:pointer-events-none [&_svg]:shrink-0 [&_svg:not([class*='size-'])]:size-4"

  def apply(state: CreateState, item: String): HtmlElement =
    val iframeRef = Var[Option[dom.html.IFrame]](None)
    // Signal.now is package-private; derive the initial code from the Var instead.
    val initialPreset = Preset.encode(ThemeConfig.toPreset(state.config.now()))

    def syncIframeTheme(cfg: ThemeConfig): Unit =
      iframeRef.now().foreach { iframe =>
        val doc = iframe.contentDocument
        if doc != null then ThemeConfig.applyToDocument(cfg, doc)
      }

    div(
      dataAttr("slot") := "preview",
      cls := "relative -mx-1 flex flex-1 flex-col justify-center overflow-hidden rounded-2xl border border-border sm:mx-0",
      state.config.signal --> { cfg => syncIframeTheme(cfg) },
      div(
        cls := "z-0 mx-auto flex max-h-(--preview-height) w-full flex-1 flex-col overflow-y-auto",
        a(
          href <-- state.presetCode.map(code => s"/preview/$item?preset=$code&fromPreview=true"),
          cls := s"$buttonBase absolute top-2 right-2 isolate z-10",
          Button.ButtonApi.variant(Button.Variant.Ghost),
          Button.ButtonApi.size(Button.Size.IconSm),
          aria.label := "Maximize preview",
          Icons.maximize()
        ),
        iframe(
          cls := "h-(--preview-height)",
          title := item,
          src := s"/preview/$item?preset=$initialPreset",
          onMountCallback { ctx =>
            iframeRef.set(Some(ctx.thisNode.ref.asInstanceOf[dom.html.IFrame]))
            syncIframeTheme(state.config.now())
          },
          onLoad --> { ev =>
            val iframe = ev.target.asInstanceOf[dom.html.IFrame]
            iframeRef.set(Some(iframe))
            val doc = iframe.contentDocument
            if doc != null then ThemeConfig.applyToDocument(state.config.now(), doc)
          }
        ),
        PreviewSwitcher(item)
      )
    )
