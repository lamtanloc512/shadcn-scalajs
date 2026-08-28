package shadcnscalajs.site

import com.raquo.laminar.api.L.*
import com.raquo.laminar.codecs.StringAsIsCodec
import shadcnscalajs.site.create.Preset

import scala.scalajs.js

/** Browser-native Web Component playground. Each edit is rendered in an isolated iframe that imports the standalone
  * bundle, so the preview exercises the same artifact a non-Scala application uses.
  */
object WebComponentsPage:

  private final case class Example(name: String, source: String)
  private final case class PlaygroundTheme(pack: String, accent: String, dark: Boolean)

  private val examples = List(
    Example(
      "Card and buttons",
      """<script type="module" src="/sc-components.js"></script>

<sc-card style="width: min(28rem, 100%)">
  <sc-card-header>
    <sc-card-title>Ship your next interface</sc-card-title>
    <sc-card-description>
      Framework-agnostic components powered by Scala.js and Laminar.
    </sc-card-description>
  </sc-card-header>
  <sc-card-content style="display:flex; gap:.75rem; align-items:center">
    <sc-badge variant="secondary">Web Component</sc-badge>
    <sc-button variant="primary">Get started</sc-button>
    <sc-button variant="outline">Documentation</sc-button>
  </sc-card-content>
</sc-card>"""
    ),
    Example(
      "Form controls and events",
      """<script type="module" src="/sc-components.js"></script>

<div style="display:grid; gap:1.25rem; width:min(28rem, 100%)">
  <sc-select id="plan" placeholder="Choose a plan"
    options='[{"value":"starter","label":"Starter"},{"value":"pro","label":"Pro"}]'>
  </sc-select>
  <sc-slider id="seats" value="35" min="0" max="100"></sc-slider>
  <label style="display:flex; align-items:center; gap:.75rem">
    <sc-switch id="updates"></sc-switch>
    Email product updates
  </label>
  <output id="result">Interact with a control</output>
</div>

<script>
  const result = document.querySelector('#result')
  document.addEventListener('sc-change', event => {
    result.value = JSON.stringify(event.detail)
  })
</script>"""
    ),
    Example(
      "Menus and overlays",
      """<script type="module" src="/sc-components.js"></script>

<div style="display:flex; gap:.75rem; align-items:center">
  <sc-dropdown-menu
    items='[{"label":"Profile"},{"label":"Billing"},{"label":"Sign out","destructive":true}]'>
    <sc-button slot="trigger" variant="outline">Open menu</sc-button>
  </sc-dropdown-menu>

  <sc-tooltip text="Rendered inside a Shadow Root">
    <sc-button variant="secondary">Hover me</sc-button>
  </sc-tooltip>
</div>"""
    ),
    Example(
      "Token overrides",
      """<script type="module" src="/sc-components.js"></script>

<div style="display:flex; gap:.75rem; align-items:center">
  <!-- Inline tokens are scoped to this component. -->
  <sc-button variant="primary"
    style="--primary:oklch(0.62 0.24 300); --radius:1.25rem">
    Scoped tokens
  </sc-button>
  <sc-button variant="outline">Theme preset</sc-button>
</div>

<script type="module">
  // Or set tokens for every shadcn-scalajs component on the page.
  window.ShadcnScalaJS.setTokens({
    '--ring': 'oklch(0.62 0.24 300)'
  })
</script>"""
    )
  )

  private val srcDocAttr = htmlAttr("srcdoc", StringAsIsCodec)
  private val sandboxAttr = htmlAttr("sandbox", StringAsIsCodec)

  def apply(): HtmlElement =
    val selectedExampleVar = Var(0)
    val sourceVar = Var(examples.head.source)
    val themeVar = Var(PlaygroundTheme(Preset.Styles.head, "blue", dark = false))
    val sourceSignal = sourceVar.signal
    val themeSignal = themeVar.signal
    val documentSignal = sourceSignal.combineWithFn(themeSignal)(renderDocument)
    var updateEditor: String => Unit = _ => ()
    var updateEditorTheme: Boolean => Unit = _ => ()
    div(
      cls := "flex min-h-dvh flex-col bg-muted/20",
      cls <-- themeSignal.map(theme =>
        if theme.dark then "!bg-zinc-950 !text-zinc-100" else "!bg-white !text-zinc-900"
      ),
      header(
        sourceVar,
        selectedExampleVar,
        themeVar,
        source => updateEditor(source),
        dark => updateEditorTheme(dark)
      ),
      div(
        cls := "grid min-h-0 flex-1 lg:grid-cols-2",
        sectionTag(
          cls := "flex min-h-[28rem] min-w-0 flex-col border-b lg:border-r lg:border-b-0",
          cls <-- themeSignal.map(theme =>
            if theme.dark then "!bg-zinc-950 !text-zinc-100" else "!bg-white !text-zinc-900"
          ),
          panelTitle("HTML", "Import the bundle, then use native custom-element markup.", themeSignal),
          div(
            cls := "min-h-0 flex-1 overflow-hidden text-[13px]",
            cls <-- themeSignal.map(theme => if theme.dark then "!bg-zinc-950" else "!bg-white"),
            aria.label := "Web Component HTML source",
            role := "textbox",
            tabIndex := 0,
            onMountUnmountCallback(
              mount = { mountCtx =>
                val host = mountCtx.thisNode.ref.asInstanceOf[js.Dynamic]
                val controller: js.Dynamic = js.Dynamic.global.ScPlaygroundEditor.mount(
                  mountCtx.thisNode.ref,
                  sourceVar.now(),
                  ((next: String) => sourceVar.set(next)): js.Function1[String, Unit],
                  themeVar.now().dark
                )
                host.__scEditor = controller
                updateEditor = next => controller.setValue(next)
                updateEditorTheme = dark => controller.setTheme(dark)
              },
              unmount = mountCtx =>
                val host = mountCtx.ref.asInstanceOf[js.Dynamic]
                val controller = host.__scEditor
                if controller != null && !js.isUndefined(controller) then controller.destroy()
                host.__scEditor = null
            ),
            onMountBind { mountCtx =>
              val host = mountCtx.thisNode.ref.asInstanceOf[js.Dynamic]
              sourceSignal.changes --> Observer[String](next => host.__scEditor.setValue(next))
              themeSignal.changes --> Observer[PlaygroundTheme](theme => host.__scEditor.setTheme(theme.dark))
            }
          )
        ),
        sectionTag(
          cls := "flex min-h-[28rem] min-w-0 flex-col",
          cls <-- themeSignal.map(theme =>
            if theme.dark then "!bg-zinc-950 !text-zinc-100" else "!bg-white !text-zinc-900"
          ),
          panelTitle("Preview", "Runs in an isolated document using sc-components.js.", themeSignal),
          iframe(
            cls := "min-h-0 flex-1 w-full bg-white",
            title := "Web Component playground preview",
            sandboxAttr := "allow-scripts allow-forms allow-modals",
            srcDocAttr <-- documentSignal
          )
        )
      )
    )

  private def header(
      sourceVar: Var[String],
      selectedExampleVar: Var[Int],
      themeVar: Var[PlaygroundTheme],
      updateEditor: String => Unit,
      updateEditorTheme: Boolean => Unit
  ): HtmlElement =
    div(
      cls := "grid grid-cols-2 gap-2 border-b px-3 py-2 lg:flex lg:flex-nowrap lg:items-center lg:overflow-x-auto",
      cls <-- themeVar.signal.map(theme =>
        if theme.dark then "!bg-zinc-950 !text-zinc-100 !border-zinc-800"
        else "!bg-white !text-zinc-900 !border-zinc-200"
      ),
      div(
        cls := "col-span-2 min-w-0 lg:min-w-44 lg:flex-1",
        h1(cls := "text-base font-semibold", "Web Components Playground"),
        p(cls := "text-xs text-muted-foreground", "Edit HTML, switch tokens, and preview the production bundle.")
      ),
      labelledSelect(
        "Example",
        selectedExampleVar.signal.map(_.toString),
        examples.zipWithIndex.map((example, index) => index.toString -> example.name),
        themeVar.signal,
        raw =>
          raw.toIntOption.foreach { index =>
            selectedExampleVar.set(index)
            val nextSource = examples(index).source
            sourceVar.set(nextSource)
            updateEditor(nextSource)
          }
      ),
      labelledSelect(
        "Style",
        themeVar.signal.map(_.pack),
        Preset.Styles.map(pack => pack -> pack.capitalize),
        themeVar.signal,
        pack => themeVar.update(theme => theme.copy(pack = pack))
      ),
      labelledSelect(
        "Accent",
        themeVar.signal.map(_.accent),
        Preset.Themes.map(_._1).distinct.map(name => name -> name.capitalize),
        themeVar.signal,
        accent => themeVar.update(theme => theme.copy(accent = accent))
      ),
      label(
        cls := "flex h-8 shrink-0 items-center gap-2 rounded-md border px-2 text-xs font-medium self-end",
        cls <-- themeVar.signal.map(theme =>
          if theme.dark then "!text-zinc-100 !border-zinc-700" else "!text-zinc-900 !border-zinc-200"
        ),
        input(
          typ := "checkbox",
          checked <-- themeVar.signal.map(_.dark),
          onChange.mapToChecked --> Observer[Boolean] { dark =>
            themeVar.update(_.copy(dark = dark))
            updateEditorTheme(dark)
          }
        ),
        "Dark"
      )
    )

  private def labelledSelect(
      labelText: String,
      selected: Signal[String],
      choices: List[(String, String)],
      theme: Signal[PlaygroundTheme],
      onSelect: String => Unit
  ): HtmlElement =
    label(
      cls := "grid min-w-0 gap-1 text-[11px] font-medium",
      cls <-- theme.map(theme => if theme.dark then "!text-zinc-300" else "!text-zinc-600"),
      span(labelText),
      select(
        cls := "h-8 min-w-0 w-full rounded-md border px-2 text-xs outline-none focus:ring-2 focus:ring-ring lg:min-w-28",
        cls <-- theme.map(theme =>
          if theme.dark then "!bg-zinc-900 !text-zinc-100 !border-zinc-700"
          else "!bg-white !text-zinc-900 !border-zinc-200"
        ),
        value <-- selected,
        onChange.mapToValue --> Observer(onSelect),
        choices.map((key, text) => option(value := key, text))
      )
    )

  private def panelTitle(name: String, description: String, theme: Signal[PlaygroundTheme]): HtmlElement =
    div(
      cls := "flex h-12 shrink-0 items-center justify-between gap-3 border-b px-4",
      cls <-- theme.map(theme =>
        if theme.dark then "!bg-zinc-950 !text-zinc-100 !border-zinc-800"
        else "!bg-white !text-zinc-900 !border-zinc-200"
      ),
      span(cls := "text-sm font-medium", name),
      span(cls := "truncate text-xs text-muted-foreground", description)
    )

  private def renderDocument(source: String, theme: PlaygroundTheme): String =
    val darkClass = if theme.dark then " class=\"dark\"" else ""
    s"""<!doctype html>
<html lang="en"$darkClass data-sc-assets-base="/" data-sc-pack-base="/styles" data-style-pack="${theme.pack}" data-base-color="neutral" data-theme-color="${theme.accent}" data-chart-color="${theme.accent}" data-radius="default">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <style>
    * { box-sizing: border-box; }
    html { color-scheme: ${
        if theme.dark then "dark" else "light"
      }; background: var(--background, white); color: var(--foreground, black); font-family: var(--font-body, ui-sans-serif, system-ui); }
    body { min-height: 100vh; margin: 0; padding: clamp(1.5rem, 6vw, 4rem); display: grid; place-items: center; }
    output { color: var(--muted-foreground, #666); font: 0.8rem ui-monospace, monospace; }
    :not(:defined) { visibility: hidden; }
  </style>
</head>
<body>
$source
<script type="module">
  // Apply playground controls after the production bundle has installed its public API.
  window.ShadcnScalaJS?.setTheme({stylePack: "${theme.pack}", themeColor: "${theme.accent}", darkMode: ${theme.dark}})
</script>
</body>
</html>"""
