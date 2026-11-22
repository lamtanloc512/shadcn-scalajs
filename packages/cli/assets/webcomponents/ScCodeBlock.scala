package shadcnscalajs.webcomponents

import com.raquo.laminar.api.L.*
import org.scalajs.dom
import shadcnscalajs.ui.{CodeBlock, Icons}

import scala.scalajs.js

/** `<sc-code-block>` — a source listing whose code is the element's own text, or its `code` attribute / property.
  *
  * {{{
  * <sc-code-block filename="Button.scala" line-numbers highlight="3" added="4" removed="2">
  *   object Button:
  *     def apply(mods: Modifier[HtmlElement]*): HtmlElement = ...
  * </sc-code-block>
  * }}}
  *
  * Text written inside the tag is dedented (common leading whitespace and surrounding blank lines removed), so it can
  * be indented with the surrounding HTML. The light-DOM text is never shown — it only feeds the shadow listing — and
  * edits to it are picked up. `highlight`, `added`, and `removed` take comma-separated 1-based line numbers. The copy
  * button is on unless `no-copy` is present. `filename` adds a header row (not `title`, which would be a native
  * tooltip).
  */
class ScCodeBlock extends ScElementBase:

  private val codeAttr = Var(Option.empty[String])
  private val textVar = Var(ScCodeBlock.dedent(Option(this.textContent).getOrElse("")))
  private val titleVar = Var(Option.empty[String])
  private val variantVar = Var(CodeBlock.Variant.Default)
  private val sizeVar = Var(CodeBlock.Size.Default)
  private val numbersVar = Var(false)
  private val wrapVar = Var(false)
  private val copyVar = Var(true)
  private val startVar = Var(1)
  private val highlightVar = Var(Set.empty[Int])
  private val addedVar = Var(Set.empty[Int])
  private val removedVar = Var(Set.empty[Int])

  observeAttribute("code")(codeAttr.set)
  observeAttribute("filename")(v => titleVar.set(v.filter(_.nonEmpty)))
  observeAttribute("variant")(v =>
    variantVar.set(if v.contains("destructive") then CodeBlock.Variant.Destructive else CodeBlock.Variant.Default)
  )
  observeAttribute("size")(v => sizeVar.set(if v.contains("sm") then CodeBlock.Size.Sm else CodeBlock.Size.Default))
  observeAttribute("line-numbers")(v => numbersVar.set(v.isDefined))
  observeAttribute("wrap")(v => wrapVar.set(v.isDefined))
  observeAttribute("no-copy")(v => copyVar.set(v.isEmpty))
  observeAttribute("start-line")(v => startVar.set(v.flatMap(_.trim.toIntOption).getOrElse(1)))
  observeAttribute("highlight")(v => highlightVar.set(ScCodeBlock.lineSet(v)))
  observeAttribute("added")(v => addedVar.set(ScCodeBlock.lineSet(v)))
  observeAttribute("removed")(v => removedVar.set(ScCodeBlock.lineSet(v)))
  stringProperty("code")
  stringProperty("filename")
  stringProperty("variant")
  stringProperty("size")
  booleanProperty("line-numbers")
  booleanProperty("wrap")

  // Text children may still be streaming in when the element upgrades, and frameworks replace them on re-render.
  new dom.MutationObserver((_, _) => textVar.set(ScCodeBlock.dedent(Option(this.textContent).getOrElse(""))))
    .observe(this, new dom.MutationObserverInit { childList = true; characterData = true; subtree = true })

  private val source: Signal[String] =
    codeAttr.signal.combineWith(textVar.signal).map((attr, text) => attr.getOrElse(text))

  /** What the copy button copies — read at click time. */
  private var latest = ""

  mount(
    div(
      cls := "contents",
      source --> { v => latest = v },
      child <-- variantVar.signal.combineWith(sizeVar.signal).map { (variant, size) =>
        CodeBlock.of(variant, size)(
          child.maybe <-- titleVar.signal.combineWith(copyVar.signal).map {
            case (Some(t), copy) =>
              Some(
                CodeBlock.header(
                  CodeBlock.title(Icons.fileText(), t),
                  if copy then CodeBlock.actions(CodeBlock.copyButton(latest)) else emptyNode
                )
              )
            case (None, true) => Some(CodeBlock.actions(CodeBlock.copyButton(latest)))
            case _            => None
          },
          child <-- source
            .combineWith(numbersVar.signal, wrapVar.signal, startVar.signal)
            .combineWith(highlightVar.signal, addedVar.signal, removedVar.signal)
            .map { case (text, numbers, wrap, start, highlight, added, removed) =>
              val lines = text.split("\n", -1).toSeq
              CodeBlock.content(wrap, numbers, start)(
                lines.zipWithIndex.map { (line, i) =>
                  val n = i + 1
                  val variant =
                    if added(n) then CodeBlock.LineVariant.Added
                    else if removed(n) then CodeBlock.LineVariant.Removed
                    else if highlight(n) then CodeBlock.LineVariant.Highlighted
                    else CodeBlock.LineVariant.Default
                  CodeBlock.line(line, variant)
                }*
              )
            }
        )
      }
    )
  )

object ScCodeBlock:
  def register(): Unit =
    ScElements.define(
      "sc-code-block",
      js.constructorOf[ScCodeBlock],
      "code",
      "filename",
      "variant",
      "size",
      "line-numbers",
      "wrap",
      "no-copy",
      "start-line",
      "highlight",
      "added",
      "removed"
    )

  private def lineSet(raw: Option[String]): Set[Int] =
    raw.toSeq
      .flatMap(_.split("[,\\s]+"))
      .flatMap { token =>
        token.split("-") match
          case Array(a, b) => (for x <- a.toIntOption; y <- b.toIntOption yield (x to y).toSeq).getOrElse(Nil)
          case _           => token.toIntOption.toSeq
      }
      .toSet

  /** Strips the blank lines around the block and the indentation every non-blank line shares. */
  private[webcomponents] def dedent(text: String): String =
    val lines = text.replace("\r\n", "\n").split("\n", -1).toSeq
    val trimmed = lines.dropWhile(_.trim.isEmpty).reverse.dropWhile(_.trim.isEmpty).reverse
    val indent = trimmed.filter(_.trim.nonEmpty).map(_.takeWhile(c => c == ' ' || c == '\t').length).minOption
    trimmed.map(line => line.drop(indent.getOrElse(0))).mkString("\n")
