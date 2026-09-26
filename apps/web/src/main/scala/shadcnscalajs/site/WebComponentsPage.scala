package shadcnscalajs.site

import com.raquo.laminar.api.L.*
import com.raquo.laminar.codecs.StringAsIsCodec
import shadcnscalajs.site.create.Preset
import shadcnscalajs.ui.Switch

import scala.scalajs.js

/** Browser-native Web Component playground. Each edit is rendered in an isolated iframe that imports the standalone
  * bundle, so the preview exercises the same artifact a non-Scala application uses.
  */
object WebComponentsPage:

  private final case class Example(name: String, source: String)
  private final case class PlaygroundTheme(pack: String, accent: String, dark: Boolean)
  private final case class HtmlDiagnostic(line: Int, column: Int, message: String)

  private val curatedExamples = List(
    Example(
      "Card and buttons",
      """<script type="module" src="/sc-components.js"></script>

<sc-card class="w-full max-w-sm">
  <sc-card-header>
    <sc-card-title>Project update</sc-card-title>
    <sc-card-description>A Card composed from Laminar primitives.</sc-card-description>
  </sc-card-header>
  <sc-card-content>Your latest deployment is ready.</sc-card-content>
</sc-card>"""
    ),
    Example(
      "Form controls and events",
      """<script type="module" src="/sc-components.js"></script>

<div class="grid gap-5">
  <sc-select id="plan" placeholder="Choose a plan"
    options='[{"value":"starter","label":"Starter"},{"value":"pro","label":"Pro"}]'>
  </sc-select>
  <sc-slider id="seats" value="35" min="0" max="100"></sc-slider>
  <label class="flex items-center gap-3">
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

<div class="flex items-center gap-3">
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

<div class="flex items-center gap-3">
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

  private val examples = curatedExamples ++ WebComponentExamples.catalog.map { case (slug, example) =>
    Example(s"Components / ${slug.split('-').map(_.capitalize).mkString(" ")}", example.source)
  }

  private val srcDocAttr = htmlAttr("srcdoc", StringAsIsCodec)
  private val sandboxAttr = htmlAttr("sandbox", StringAsIsCodec)
  private val editorHtmlFile = "html"
  private val editorCssFile = "css"
  private val defaultTailwindCss = """/* Tailwind v4 runs in this preview only. Customize tokens or add CSS here. */
@theme inline {
  --color-background: var(--background);
  --color-foreground: var(--foreground);
  --color-primary: var(--primary);
  --color-primary-foreground: var(--primary-foreground);
  --color-muted: var(--muted);
  --color-muted-foreground: var(--muted-foreground);
  --color-border: var(--border);
  --color-brand: oklch(0.62 0.24 300);
}
"""

  def apply(): HtmlElement =
    val selectedExampleVar = Var(0)
    val sourceVar = Var(examples.head.source)
    val tailwindCssVar = Var(defaultTailwindCss)
    val activeEditorFileVar = Var(editorHtmlFile)
    val themeVar = Var(PlaygroundTheme(Preset.Styles.head, "blue", dark = false))
    val sourceSignal = sourceVar.signal
    val tailwindCssSignal = tailwindCssVar.signal
    val activeEditorFileSignal = activeEditorFileVar.signal
    val themeSignal = themeVar.signal
    val htmlDiagnosticVar = Var(Option.empty[HtmlDiagnostic])
    val htmlDiagnosticSignal = htmlDiagnosticVar.signal
    val showErrorVar = Var(true)
    val showErrorSignal = showErrorVar.signal
    val dismissedDiagnosticVar = Var(Option.empty[HtmlDiagnostic])
    val dismissedDiagnosticSignal = dismissedDiagnosticVar.signal
    val visibleDiagnosticSignal = htmlDiagnosticSignal
      .combineWithFn(showErrorSignal) { (diagnostic, showError) =>
        if showError then diagnostic else None
      }
      .combineWithFn(dismissedDiagnosticSignal) { (diagnostic, dismissed) =>
        diagnostic.filter(_ != dismissed)
      }
      .distinct
    val documentSignal = sourceSignal
      .combineWithFn(tailwindCssSignal)((source, tailwindCss) => source -> tailwindCss)
      .combineWithFn(themeSignal) { case ((source, tailwindCss), theme) =>
        renderDocument(source, tailwindCss, theme)
      }
    var updateEditor: String => Unit = _ => ()
    var updateEditorFile: String => Unit = _ => ()
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
          editorTabs(
            activeEditorFileSignal,
            themeSignal,
            Observer[String] { file =>
              activeEditorFileVar.set(file)
              updateEditorFile(file)
            }
          ),
          div(
            idAttr := "web-components-editor-panel",
            cls := "min-h-0 flex-1 overflow-hidden text-[13px]",
            cls <-- themeSignal.map(theme => if theme.dark then "!bg-zinc-950" else "!bg-white"),
            aria.label := "Playground code editor",
            role := "tabpanel",
            onMountUnmountCallback(
              mount = { mountCtx =>
                val host = mountCtx.thisNode.ref.asInstanceOf[js.Dynamic]
                val controller: js.Dynamic = js.Dynamic.global.ScPlaygroundEditor.mount(
                  mountCtx.thisNode.ref,
                  sourceVar.now(),
                  tailwindCssVar.now(),
                  ((next: String) => sourceVar.set(next)): js.Function1[String, Unit],
                  ((next: String) => tailwindCssVar.set(next)): js.Function1[String, Unit],
                  themeVar.now().dark,
                  ((raw: js.Dynamic) =>
                    val diagnostic =
                      if raw == null || js.isUndefined(raw) then None
                      else
                        Some(HtmlDiagnostic(raw.line.toString.toInt, raw.column.toString.toInt, raw.message.toString))
                    if diagnostic != htmlDiagnosticVar.now() then
                      htmlDiagnosticVar.set(diagnostic)
                      dismissedDiagnosticVar.set(None)
                  ): js.Function1[js.Dynamic, Unit]
                )
                host.__scEditor = controller
                updateEditor = next => controller.setHtmlValue(next)
                updateEditorFile = file => controller.setActive(file)
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
              // Model updates are equality-guarded and suppress-flagged so edits never echo
              // from Laminar back into Monaco (no feedback loop or caret jump).
              sourceSignal.changes --> Observer[String] { next =>
                val editor = host.__scEditor
                if editor != null && !js.isUndefined(editor) then editor.setHtmlValue(next)
              }
              tailwindCssSignal.changes --> Observer[String] { next =>
                val editor = host.__scEditor
                if editor != null && !js.isUndefined(editor) then editor.setCssValue(next)
              }
              activeEditorFileSignal.changes --> Observer[String] { file =>
                val editor = host.__scEditor
                if editor != null && !js.isUndefined(editor) then editor.setActive(file)
              }
              themeSignal.changes --> Observer[PlaygroundTheme] { theme =>
                val editor = host.__scEditor
                if editor != null && !js.isUndefined(editor) then editor.setTheme(theme.dark)
              }
            }
          )
        ),
        sectionTag(
          cls := "flex min-h-[28rem] min-w-0 flex-col",
          cls <-- themeSignal.map(theme =>
            if theme.dark then "!bg-zinc-950 !text-zinc-100" else "!bg-white !text-zinc-900"
          ),
          panelTitle(
            "Preview",
            "Runs in an isolated document using sc-components.js.",
            themeSignal,
            showErrorToggle(showErrorVar)
          ),
          child.maybe <-- visibleDiagnosticSignal.map(
            _.map(diagnostic =>
              errorBanner(diagnostic, themeSignal, Observer(_ => dismissedDiagnosticVar.set(Some(diagnostic))))
            )
          ),
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

  private def editorTabs(
      activeFile: Signal[String],
      theme: Signal[PlaygroundTheme],
      onSelect: Observer[String]
  ): HtmlElement =
    div(
      cls := "flex h-12 shrink-0 items-end gap-1 border-b px-3",
      cls <-- theme.map(theme =>
        if theme.dark then "!bg-zinc-950 !text-zinc-100 !border-zinc-800"
        else "!bg-white !text-zinc-900 !border-zinc-200"
      ),
      role := "tablist",
      aria.label := "Playground files",
      editorTab("index.html", editorHtmlFile, activeFile, theme, onSelect),
      editorTab("tailwind.css", editorCssFile, activeFile, theme, onSelect)
    )

  private def editorTab(
      labelText: String,
      file: String,
      activeFile: Signal[String],
      theme: Signal[PlaygroundTheme],
      onSelect: Observer[String]
  ): HtmlElement =
    val isActiveSignal = activeFile.map(_ == file).distinct
    val tabClassSignal = theme.combineWithFn(isActiveSignal) { (theme, isActive) =>
      if isActive then if theme.dark then "!border-zinc-100 !text-zinc-100" else "!border-zinc-900 !text-zinc-900"
      else if theme.dark then "!border-transparent !text-zinc-400 hover:!text-zinc-200"
      else "!border-transparent !text-zinc-500 hover:!text-zinc-800"
    }
    button(
      idAttr := s"web-components-$file-tab",
      typ := "button",
      role := "tab",
      aria.controls := "web-components-editor-panel",
      aria.selected <-- isActiveSignal,
      tabIndex <-- isActiveSignal.map(if _ then 0 else -1),
      cls := "h-10 border-b-2 px-3 text-sm font-medium transition-colors",
      cls <-- tabClassSignal,
      onClick.mapTo(file) --> onSelect,
      labelText
    )

  private def panelTitle(
      name: String,
      description: String,
      theme: Signal[PlaygroundTheme],
      control: HtmlElement = span()
  ): HtmlElement =
    div(
      cls := "flex h-12 shrink-0 items-center justify-between gap-3 border-b px-4",
      cls <-- theme.map(theme =>
        if theme.dark then "!bg-zinc-950 !text-zinc-100 !border-zinc-800"
        else "!bg-white !text-zinc-900 !border-zinc-200"
      ),
      span(cls := "text-sm font-medium", name),
      span(cls := "truncate text-xs text-muted-foreground", description),
      control
    )

  private def showErrorToggle(showErrorVar: Var[Boolean]): HtmlElement =
    label(
      cls := "flex shrink-0 items-center gap-2 text-xs font-medium",
      Switch(showErrorVar, aria.label := "Show Error"),
      span("Show Error")
    )

  private def errorBanner(
      diagnostic: HtmlDiagnostic,
      theme: Signal[PlaygroundTheme],
      onDismiss: Observer[Unit]
  ): HtmlElement =
    div(
      cls := "mx-4 mt-3 flex items-start justify-between gap-3 rounded-md border px-3 py-2 text-sm",
      cls <-- theme.map(theme =>
        if theme.dark then "!border-red-900 !bg-red-950/50 !text-red-200"
        else "!border-red-200 !bg-red-50 !text-red-700"
      ),
      role := "alert",
      aria.live := "polite",
      span(s"(${diagnostic.line}:${diagnostic.column}) ${diagnostic.message}"),
      button(
        typ := "button",
        cls := "shrink-0 rounded-sm p-0.5 text-lg leading-none opacity-70 hover:opacity-100",
        aria.label := "Dismiss error",
        onClick.mapTo(()) --> onDismiss,
        "×"
      )
    )

  private def renderDocument(source: String, tailwindCss: String, theme: PlaygroundTheme): String =
    val darkClass = if theme.dark then " class=\"dark\"" else ""
    val safeTailwindCss = tailwindCss.replaceAll("(?i)</style", "<\\\\/style")
    val tailwindRuntimeUrl = js.Dynamic.global.ScPlaygroundEditor.tailwindBrowserUrl.toString
    s"""<!doctype html>
<html lang="en"$darkClass data-sc-assets-base="/" data-sc-pack-base="/styles" data-style-pack="${theme.pack}" data-base-color="neutral" data-theme-color="${theme.accent}" data-chart-color="${theme.accent}" data-radius="default">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <style type="text/tailwindcss">
$safeTailwindCss
  </style>
  <script src="$tailwindRuntimeUrl"></script>
  <style>
    * { box-sizing: border-box; }
    html { color-scheme: ${
        if theme.dark then "dark" else "light"
      }; background: var(--background, white); color: var(--foreground, black); font-family: var(--font-body, ui-sans-serif, system-ui); }
    body { min-height: 100vh; margin: 0; padding: clamp(1.5rem, 6vw, 4rem); display: grid; place-items: center; }
    body > :not(script) { width: min(28rem, 100%); }
    output { color: var(--muted-foreground, #666); font: 0.8rem ui-monospace, monospace; }
    :not(:defined) { visibility: hidden; }
  </style>
</head>
<body>
$source
<script type="module">
  import '/sc-components.js'
  // Apply playground controls after the production bundle has installed its public API.
  window.ShadcnScalaJS?.setTheme({stylePack: "${theme.pack}", themeColor: "${theme.accent}", darkMode: ${theme.dark}})
</script>
</body>
</html>"""
