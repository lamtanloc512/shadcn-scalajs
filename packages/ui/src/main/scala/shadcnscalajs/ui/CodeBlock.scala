package shadcnscalajs.ui

import com.raquo.laminar.api.L
import com.raquo.laminar.api.L.*
import org.scalajs.dom

import scala.scalajs.js

/** shadcn/ui Code Block — a framed, scrollable source listing with an optional header, actions, line numbers, and
  * diff/highlight line variants.
  *
  * Highlighting is out of scope, as upstream (which delegates it to a separate hook): pass plain lines to [[content]],
  * or pre-tokenised markup to [[htmlLine]] when a highlighter produced it. Actions placed directly inside the root
  * float over the top-right corner and fade in on hover; inside a [[header]] they sit inline.
  */
object CodeBlock:

  enum Variant derives CanEqual:
    case Default, Destructive

  enum Size derives CanEqual:
    case Default, Sm

  enum LineVariant derives CanEqual:
    case Default, Added, Removed, Highlighted

  private def name(value: Any): String = value.toString.toLowerCase

  private val base: String =
    "cn-code-block group/code-block relative flex w-full min-w-0 flex-col overflow-hidden rounded-lg text-sm has-[>[data-slot=code-block-content]:focus-visible]:ring-3 has-[>[data-slot=code-block-content]:focus-visible]:ring-ring/50 data-[size=sm]:text-xs"

  private val variantClasses: Map[Variant, String] = Map(
    Variant.Default ->
      "border border-input text-foreground has-[>[data-slot=code-block-content]:focus-visible]:border-ring dark:bg-input/30",
    Variant.Destructive -> "border border-destructive/30 text-destructive dark:border-destructive/40"
  )

  def apply(mods: Modifier[HtmlElement]*): HtmlElement = of()(mods*)

  def of(variant: Variant = Variant.Default, size: Size = Size.Default)(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "code-block",
      dataAttr("variant") := name(variant),
      dataAttr("size") := name(size),
      cls := s"$base ${variantClasses(variant)}",
      mods
    )

  def header(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "code-block-header",
      cls := "cn-code-block-header flex min-w-0 shrink-0 items-center gap-2 px-3 pt-3 text-muted-foreground group-data-[size=sm]/code-block:px-2.5 group-data-[size=sm]/code-block:pt-2.5 group-data-[variant=destructive]/code-block:text-destructive *:has-[>[data-slot=code-block-title]]:min-w-0",
      mods
    )

  def title(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "code-block-title",
      cls := "cn-code-block-title min-w-0 truncate font-sans text-sm font-medium text-foreground group-data-[size=sm]/code-block:text-xs group-data-[variant=destructive]/code-block:text-destructive [&_svg]:pointer-events-none [&_svg]:inline-block [&_svg]:align-middle [&_svg:not([class*='size-'])]:size-3.5 *:[svg]:me-1.5",
      mods
    )

  def actions(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "code-block-actions",
      cls := "cn-code-block-actions -my-1 ms-auto -me-1.5 flex shrink-0 items-center gap-1 [[data-slot=code-block]>&]:absolute [[data-slot=code-block]>&]:end-2 [[data-slot=code-block]>&]:top-2 [[data-slot=code-block]>&]:z-10 [[data-slot=code-block]>&]:m-0 [[data-slot=code-block]>&]:opacity-0 [[data-slot=code-block]>&]:transition-opacity [[data-slot=code-block]>&]:group-focus-within/code-block:opacity-100 [[data-slot=code-block]>&]:group-hover/code-block:opacity-100 group-data-[size=sm]/code-block:[[data-slot=code-block]>&]:end-1.5 group-data-[size=sm]/code-block:[[data-slot=code-block]>&]:top-1.5 [[data-slot=code-block]>&]:pointer-coarse:opacity-100 [[data-slot=code-block]>&]:*:[button]:bg-background [[data-slot=code-block]>&]:*:[button]:ring-1 [[data-slot=code-block]>&]:*:[button]:ring-foreground/10",
      mods
    )

  /** Copies `value` (read at click time, so a reactive source can pass a by-name expression) and shows a check for two
    * seconds.
    */
  def copyButton(value: => String, mods: Modifier[HtmlElement]*): HtmlElement =
    val copied = Var(false)
    span(
      cls := "contents",
      Button(
        Button.ButtonApi.variant(Button.Variant.Ghost),
        Button.ButtonApi.size(Button.Size.IconXs),
        dataAttr("slot") := "code-block-copy-button",
        dataAttr("copied") <-- copied.signal.map(if _ then "true" else ""),
        aria.label <-- copied.signal.map(if _ then "Copied" else "Copy"),
        child <-- copied.signal.map(if _ then Icons.check() else Icons.copy()),
        onClick --> { _ =>
          val clipboard = dom.window.navigator.asInstanceOf[js.Dynamic].clipboard
          if !js.isUndefined(clipboard) then
            clipboard
              .writeText(value)
              .asInstanceOf[js.Promise[Unit]]
              .`then`[Unit] { _ =>
                copied.set(true)
                val _ = js.timers.setTimeout(2000)(copied.set(false))
              }
        },
        mods
      ),
      span(aria.live := "polite", cls := "sr-only", text <-- copied.signal.map(if _ then "Copied" else ""))
    )

  private val contentClasses: String =
    "cn-code-block-content m-0 min-h-0 min-w-0 scroll-fade-x scrollbar-thin overflow-auto px-3 py-3 text-start font-mono leading-relaxed outline-none group-has-data-[slot=code-block-footer]/code-block:pb-2 group-has-data-[slot=code-block-header]/code-block:pt-2 group-data-[size=sm]/code-block:px-2.5 group-data-[size=sm]/code-block:py-2.5 group-data-[size=sm]/code-block:group-has-data-[slot=code-block-header]/code-block:pt-1.5 data-wrap:scroll-fade-y data-wrap:wrap-break-word data-wrap:whitespace-pre-wrap"

  private val codeClasses: String =
    "grid min-h-lh min-w-max content-start in-data-numbered:grid-cols-[auto_1fr] in-data-wrap:min-w-0"

  /** The scrolling `pre`. `lines` are usually [[line]]s; pass a plain string to [[source]] for the common case. */
  def content(wrap: Boolean = false, lineNumbers: Boolean = false, startLine: Int = 1)(
      lines: Modifier[HtmlElement]*
  ): HtmlElement =
    pre(
      dataAttr("slot") := "code-block-content",
      if wrap then dataAttr("wrap") := "true" else emptyMod,
      if lineNumbers then dataAttr("numbered") := "true" else emptyMod,
      L.dir := "ltr",
      tabIndex := 0,
      styleAttr := s"counter-reset: line ${startLine - 1}",
      cls := contentClasses,
      code(dataAttr("slot") := "code-block-code", cls := codeClasses, lines)
    )

  /** `content` for a plain string: one [[line]] per source line, so numbering and copy-selection stay correct. */
  def source(text: String, wrap: Boolean = false, lineNumbers: Boolean = false, startLine: Int = 1): HtmlElement =
    content(wrap, lineNumbers, startLine)(text.stripSuffix("\n").split("\n", -1).toSeq.map(l => line(l))*)

  private val lineBase: String =
    "cn-code-block-line relative -mx-3 block min-h-lh px-3 group-data-[size=sm]/code-block:-mx-2.5 group-data-[size=sm]/code-block:px-2.5 before:min-w-6 before:text-end before:text-muted-foreground after:absolute after:start-1 after:top-0 after:text-muted-foreground after:select-none in-data-numbered:col-span-2 in-data-numbered:grid in-data-numbered:grid-cols-subgrid in-data-numbered:gap-x-3 in-data-numbered:[counter-increment:line] in-data-numbered:before:content-[counter(line)] in-data-numbered:before:select-none in-data-numbered:supports-[content:counter(line)_/_'']:before:content-[counter(line)_/_'']"

  private val lineVariantClasses: Map[LineVariant, String] = Map(
    LineVariant.Default -> "",
    LineVariant.Added -> "bg-muted after:content-['+']",
    LineVariant.Removed -> "bg-destructive/10 after:text-destructive after:content-['−']",
    LineVariant.Highlighted -> "bg-muted"
  )

  private def lineShell(variant: LineVariant, inner: HtmlElement): HtmlElement =
    span(
      dataAttr("slot") := "code-block-line",
      dataAttr("variant") := name(variant),
      cls := s"$lineBase ${lineVariantClasses(variant)}",
      inner
    )

  /** One line of plain text. An empty line holds a newline so copying a selection keeps blank lines. */
  def line(text: String, variant: LineVariant = LineVariant.Default): HtmlElement =
    lineShell(
      variant,
      span(dataAttr("slot") := "code-block-line-content", cls := "min-w-0", if text.isEmpty then "\n" else text)
    )

  /** One line of arbitrary content — tokens, links, a highlighted `span`. */
  def lineOf(variant: LineVariant = LineVariant.Default)(mods: Modifier[HtmlElement]*): HtmlElement =
    lineShell(variant, span(dataAttr("slot") := "code-block-line-content", cls := "min-w-0", mods))

  /** One line of trusted, pre-tokenised HTML from a syntax highlighter. Never pass user input here. */
  def htmlLine(html: String, variant: LineVariant = LineVariant.Default): HtmlElement =
    lineShell(
      variant,
      span(
        dataAttr("slot") := "code-block-line-content",
        cls := "min-w-0",
        onMountCallback { ctx => ctx.thisNode.ref.innerHTML = if html.isEmpty then "\n" else html }
      )
    )

  def footer(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "code-block-footer",
      cls := "cn-code-block-footer flex min-w-0 shrink-0 items-center gap-2 px-3 pb-3 text-muted-foreground group-data-[size=sm]/code-block:px-2.5 group-data-[size=sm]/code-block:pb-2.5",
      mods
    )
