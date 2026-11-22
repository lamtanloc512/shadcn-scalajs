package shadcnscalajs.site

import com.raquo.laminar.api.L.*
import org.scalajs.dom
import shadcnscalajs.ui.*

import scala.scalajs.js
import scala.util.control.NonFatal

/** Every source listing in the docs: highlighted, laid out, numbered, copyable, and collapsed when long.
  *
  * Listings are the registry's own [[CodeBlock]], so the docs exercise the component they document. Formatting and
  * tokenising happen in `src/docs-highlight.js`; Scala only lays the returned lines out.
  */
object DocsCode:

  private def highlighter = js.Dynamic.global.ScDocsHighlight

  /** Source as it is shown and copied — long Scala one-liners are broken up the way scalafmt would. */
  def format(language: String, source: String): String =
    if js.isUndefined(highlighter) then source.stripTrailing
    else highlighter.format(language, source).asInstanceOf[String]

  private def highlightedLines(language: String, source: String): Seq[String] =
    if js.isUndefined(highlighter) then source.split("\n", -1).toSeq.map(escape)
    else highlighter.lines(language, source).asInstanceOf[js.Array[String]].toSeq

  private def escape(text: String): String =
    text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")

  def languageLabel(language: String): String = language match
    case "scala"                 => "Scala"
    case "html" | "xml"          => "HTML"
    case "shell" | "bash" | "sh" => "Terminal"
    case "json"                  => "JSON"
    case "css"                   => "CSS"
    case "javascript" | "js"     => "JavaScript"
    case other                   => other

  def languageIcon(language: String): SvgElement = language match
    case "shell" | "bash" | "sh" => Icons.terminal()
    case _                       => Icons.fileText()

  /** The highlighted, scrollable listing alone — no frame. Long listings collapse behind an Expand button. */
  def listing(
      language: String,
      source: String,
      lineNumbers: Boolean = true,
      collapsible: Boolean = true,
      maxLines: Int = 16
  ): HtmlElement =
    val lines = highlightedLines(language, source)
    val long = collapsible && lines.size > maxLines
    val expanded = Var(false)
    val collapsed = expanded.signal.map(e => long && !e)
    div(
      cls := "relative min-w-0",
      dataAttr("language") := language,
      div(
        cls := "min-w-0",
        cls("sc-code-collapsed", "max-h-[22rem]", "overflow-hidden") <-- collapsed,
        CodeBlock
          .content(lineNumbers = lineNumbers && lines.size > 1)(lines.map(CodeBlock.htmlLine(_)))
          .amend(cls := "px-4! py-3.5! text-[0.8125rem] leading-6")
      ),
      Option
        .when(long)(
          div(
            cls := "flex justify-center pb-3",
            cls("absolute", "inset-x-0", "bottom-0", "z-10", "pb-4") <-- collapsed,
            Button.of(
              _.variant(Button.Variant.Secondary),
              _.size(Button.Size.Sm),
              _ => cls := "shadow-sm",
              _ => onClick --> { _ => expanded.update(!_) },
              _ => text <-- expanded.signal.map(if _ then "Collapse" else s"Expand · ${lines.size} lines")
            )
          )
        )
        .getOrElse(emptyNode)
    )

  /** A standalone listing with a title row: language icon, title, copy button. */
  def block(
      language: String,
      source: String,
      title: Option[String] = None,
      lineNumbers: Boolean = true,
      collapsible: Boolean = true,
      margin: String = "mt-4"
  ): HtmlElement =
    val formatted = format(language, source)
    CodeBlock(
      cls := s"sc-doc-code $margin rounded-xl border-border/70",
      dataAttr("sc-docs-code") := language,
      CodeBlock.header(
        cls := "border-b pb-2.5",
        CodeBlock.title(
          cls := "font-normal text-muted-foreground",
          languageIcon(language),
          title.getOrElse(languageLabel(language))
        ),
        CodeBlock.actions(CodeBlock.copyButton(formatted))
      ),
      listing(language, formatted, lineNumbers, collapsible)
    )

  /** A one-line command: no header, copy floats in the corner. */
  def command(source: String, margin: String = "mt-4"): HtmlElement =
    CodeBlock(
      cls := s"sc-doc-code $margin rounded-xl border-border/70",
      dataAttr("sc-docs-code") := "shell",
      CodeBlock.actions(CodeBlock.copyButton(source)),
      listing("shell", source, lineNumbers = false, collapsible = false)
    )

  // ── Underlined tab row shared by the docs frames ────────────────────────────────────────────────────────────────

  /** A row of text tabs with an underline on the active one, roving focus, and `aria-selected`. */
  def tabRow(selected: Var[String], tabs: Seq[(String, String)], idPrefix: String): HtmlElement =
    val values = tabs.map(_._1)
    div(
      role := "tablist",
      cls := "flex items-center gap-4",
      onKeyDown --> { (ev: dom.KeyboardEvent) =>
        val idx = values.indexOf(selected.now())
        val next = ev.key match
          case "ArrowRight" => Some(values((idx + 1) % values.size))
          case "ArrowLeft"  => Some(values((idx - 1 + values.size) % values.size))
          case "Home"       => values.headOption
          case "End"        => values.lastOption
          case _            => None
        next.foreach { value =>
          ev.preventDefault()
          selected.set(value)
          Option(dom.document.getElementById(s"$idPrefix-$value-tab")).foreach(_.asInstanceOf[dom.html.Element].focus())
        }
      },
      tabs.map { (value, label) =>
        button(
          typ := "button",
          role := "tab",
          idAttr := s"$idPrefix-$value-tab",
          dataAttr("value") := value,
          aria.controls := s"$idPrefix-$value-panel",
          aria.selected <-- selected.signal.map(_ == value),
          tabIndex <-- selected.signal.map(v => if v == value then 0 else -1),
          cls := "relative -mb-px inline-flex h-10 items-center border-b-2 border-transparent px-0.5 text-sm font-medium text-muted-foreground transition-colors outline-none hover:text-foreground focus-visible:text-foreground focus-visible:ring-[3px] focus-visible:ring-ring/50 aria-selected:border-foreground aria-selected:text-foreground",
          onClick --> { _ => selected.set(value) },
          label
        )
      }
    )

  // ── Installation ────────────────────────────────────────────────────────────────────────────────────────────────

  private val packageManagerKey = "shadcn-scalajs:package-manager"

  private val managers: Seq[(String, String)] = Seq("pnpm" -> "pnpm", "npm" -> "npm", "yarn" -> "yarn", "bun" -> "bun")

  /** Shared by every install block on the site, and remembered, as on shadcn/ui. */
  val packageManager: Var[String] =
    val stored =
      try Option(dom.window.localStorage.getItem(packageManagerKey))
      catch case NonFatal(_) => None
    Var(stored.filter(v => managers.exists(_._1 == v)).getOrElse("npm"))

  private def runner(manager: String): String = manager match
    case "pnpm" => "pnpm dlx"
    case "yarn" => "yarn dlx"
    case "bun"  => "bunx --bun"
    case _      => "npx"

  def addCommand(manager: String, names: String): String =
    s"${runner(manager)} shadcn-scalajs@latest add $names"

  // Persist once for the whole site; every install block reads and writes the same Var.
  packageManager.signal.changes.foreach { value =>
    try dom.window.localStorage.setItem(packageManagerKey, value)
    catch case NonFatal(_) => ()
  }(using unsafeWindowOwner)

  private var blockCounter = 0

  /** A terminal block whose command follows the remembered package manager. */
  def packageCommand(names: String, margin: String = "mt-4"): HtmlElement =
    blockCounter += 1
    CodeBlock(
      cls := s"sc-doc-code $margin rounded-xl border-border/70",
      dataAttr("sc-docs-code") := "shell",
      div(
        cls := "flex items-center gap-3 border-b ps-3.5 pe-2",
        span(cls := "flex text-muted-foreground [&_svg]:size-3.5", Icons.terminal()),
        div(cls := "flex-1", tabRow(packageManager, managers, s"pm-$blockCounter")),
        CodeBlock.copyButton(addCommand(packageManager.now(), names))
      ),
      child <-- packageManager.signal.map(pm =>
        listing("shell", addCommand(pm, names), lineNumbers = false, collapsible = false)
      )
    )

  /** shadcn/ui's Installation section: a CLI tab with package-manager commands, and a Manual tab that shows the
    * registry's own copy of the source and the items it depends on.
    */
  def installation(slug: String, title: String): HtmlElement =
    val tab = Var("cli")
    val opened = Var(false)
    val idPrefix = s"$slug-install"
    div(
      div(cls := "border-b", tabRow(tab, Seq("cli" -> "CLI", "manual" -> "Manual"), idPrefix)),
      div(
        idAttr := s"$idPrefix-cli-panel",
        role := "tabpanel",
        display <-- tab.signal.map(v => if v == "cli" then "block" else "none"),
        packageCommand(slug)
      ),
      div(
        idAttr := s"$idPrefix-manual-panel",
        role := "tabpanel",
        display <-- tab.signal.map(v => if v == "manual" then "block" else "none"),
        // Fetched only once the reader opens the tab: the source is large and most readers use the CLI.
        tab.signal --> { v => if v == "manual" then opened.set(true) },
        child <-- opened.signal.distinct.map(open => if open then manualSteps(slug, title) else div())
      )
    )

  private final case class RegistryFile(path: String, target: String, content: String)
  private final case class RegistryItem(dependencies: Seq[String], files: Seq[RegistryFile])

  private def fetchItem(slug: String): EventStream[Either[String, RegistryItem]] =
    EventStream
      .fromJsPromise(
        dom
          .fetch(s"/registry/$slug.json")
          .`then`[String](_.text())
      )
      .map { text =>
        val json = js.JSON.parse(text)
        val deps = json.registryDependencies.asInstanceOf[js.UndefOr[js.Array[String]]].map(_.toSeq).getOrElse(Nil)
        val files = json.files.asInstanceOf[js.Array[js.Dynamic]].toSeq.map { file =>
          // Published items carry `target` (the path under the consumer's source dir); sidecars carry `path`.
          val target = file.target
            .asInstanceOf[js.UndefOr[String]]
            .toOption
            .orElse(file.path.asInstanceOf[js.UndefOr[String]].toOption)
            .getOrElse(s"ui/$slug.scala")
          RegistryFile(target, target, file.content.asInstanceOf[String])
        }
        Right(RegistryItem(deps.filterNot(_ == "theme"), files)): Either[String, RegistryItem]
      }
      .recover { case err => Some(Left(err.getMessage)) }

  private def step(number: Int, title: String, body: Modifier[HtmlElement]*): HtmlElement =
    div(
      cls := "relative ms-3 border-s ps-7 pb-8 last:pb-0",
      span(
        cls := "absolute -start-3.5 top-0 flex size-7 items-center justify-center rounded-full border bg-background font-mono text-xs font-medium",
        number.toString
      ),
      h3(cls := "pt-0.5 text-sm font-semibold text-foreground", title),
      body
    )

  private def manualSteps(slug: String, title: String): HtmlElement =
    div(
      cls := "mt-6",
      child <-- fetchItem(slug).map {
        case Left(message) =>
          p(cls := "text-sm text-destructive", s"Could not load /registry/$slug.json: $message")
        case Right(item) =>
          var n = 0
          def next(): Int = { n += 1; n }
          div(
            if item.dependencies.nonEmpty then
              step(
                next(),
                "Add the components it builds on.",
                p(
                  cls := "mt-2 text-sm leading-6 text-muted-foreground",
                  s"$title imports ",
                  item.dependencies.zipWithIndex.flatMap { (dep, i) =>
                    Seq[Modifier[HtmlElement]](
                      if i == 0 then emptyMod else ", ",
                      a(
                        href := s"/components/$dep",
                        cls := "font-medium text-foreground underline underline-offset-4",
                        dep
                      )
                    )
                  },
                  ". Install those first, or copy them the same way."
                ),
                packageCommand(item.dependencies.mkString(" "))
              )
            else emptyNode,
            step(
              next(),
              "Copy and paste the following code into your project.",
              item.files.map(file => block("scala", file.content, title = Some(file.target), margin = "mt-4"))
            ),
            step(
              next(),
              "Keep the package name.",
              p(
                cls := "mt-2 text-sm leading-6 text-muted-foreground",
                "The file declares ",
                code(cls := "sc-inline-code", "package shadcnscalajs.ui"),
                ". Put it under your source directory's ",
                code(cls := "sc-inline-code", "shadcnscalajs/ui/"),
                " folder, or change the package and its imports together."
              )
            )
          )
      }
    )
