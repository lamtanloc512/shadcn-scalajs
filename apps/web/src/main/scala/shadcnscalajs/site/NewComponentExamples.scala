package shadcnscalajs.site

import com.raquo.laminar.api.L.*
import shadcnscalajs.ui.*

import scala.scalajs.js

/** Primary demos for the chat-oriented components ported from shadcn/ui's newest registry items.
  *
  * Each case returns the rendered demo together with the source shown under it, from the same match, so the two cannot
  * drift apart — the same 1:1 rule `Main.liveExample` / `Main.usageSource` follow.
  */
object NewComponentExamples:

  final case class Demo(preview: HtmlElement, source: String)

  def apply(slug: String): Option[Demo] = slug match
    case "attachment"       => Some(attachment)
    case "bubble"           => Some(bubble)
    case "code-block"       => Some(codeBlock)
    case "direction"        => Some(direction)
    case "marker"           => Some(marker)
    case "message"          => Some(message)
    case "message-scroller" => Some(messageScroller)
    case "questionnaire"    => Some(questionnaire)
    case "trace"            => Some(trace)
    case _                  => None

  private def canvas(mods: Modifier[HtmlElement]*): HtmlElement =
    div(cls := "flex min-h-[400px] w-full items-center justify-center p-6 sm:p-10", mods)

  // ── Attachment ──

  private def attachment: Demo =
    val uploading = Var(Attachment.State.Uploading)
    Demo(
      canvas(
        div(
          cls := "flex w-full max-w-md flex-col gap-4",
          Attachment.group(
            Attachment.of()(
              Attachment.media(Attachment.MediaVariant.Image)(
                img(src := "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=200&q=80", alt := "")
              ),
              Attachment.content(
                Attachment.title("landscape.jpg"),
                Attachment.description("2.4 MB")
              ),
              Attachment.actions(Attachment.action(aria.label := "Remove", Icons.x()))
            ),
            Attachment.reactive(uploading.signal)(
              Attachment.media()(
                child <-- uploading.signal.map(s => if s == Attachment.State.Done then Icons.fileText() else Spinner())
              ),
              Attachment.content(
                Attachment.title("quarterly-report.pdf"),
                Attachment.description(
                  text <-- uploading.signal.map(s =>
                    if s == Attachment.State.Done then "PDF · 1.2 MB" else "Uploading…"
                  )
                )
              ),
              Attachment.actions(Attachment.action(aria.label := "Remove", Icons.x()))
            ),
            Attachment.of(state = Attachment.State.Error)(
              Attachment.media()(Icons.circleX()),
              Attachment.content(
                Attachment.title("archive.zip"),
                Attachment.description("File is too large")
              )
            )
          ),
          div(
            cls := "flex gap-2",
            Button.of(
              _.variant(Button.Variant.Outline),
              _.size(Button.Size.Sm),
              _ => onClick --> { _ => uploading.set(Attachment.State.Done) },
              _ => "Finish upload"
            ),
            Button.of(
              _.variant(Button.Variant.Ghost),
              _.size(Button.Size.Sm),
              _ => onClick --> { _ => uploading.set(Attachment.State.Uploading) },
              _ => "Reset"
            )
          )
        )
      ),
      """val uploading = Var(Attachment.State.Uploading)

Attachment.group(
  Attachment.of()(
    Attachment.media(Attachment.MediaVariant.Image)(img(src := "/landscape.jpg", alt := "")),
    Attachment.content(Attachment.title("landscape.jpg"), Attachment.description("2.4 MB")),
    Attachment.actions(Attachment.action(aria.label := "Remove", Icons.x()))
  ),
  // State drives the look: Uploading shimmers the title, Done settles it.
  Attachment.reactive(uploading.signal)(
    Attachment.media()(child <-- uploading.signal.map(s => if s == Attachment.State.Done then Icons.fileText() else Spinner())),
    Attachment.content(
      Attachment.title("quarterly-report.pdf"),
      Attachment.description(text <-- uploading.signal.map(s => if s == Attachment.State.Done then "PDF · 1.2 MB" else "Uploading…"))
    ),
    Attachment.actions(Attachment.action(aria.label := "Remove", Icons.x()))
  ),
  Attachment.of(state = Attachment.State.Error)(
    Attachment.media()(Icons.circleX()),
    Attachment.content(Attachment.title("archive.zip"), Attachment.description("File is too large"))
  )
)"""
    )

  // ── Bubble ──

  private def bubble: Demo =
    Demo(
      canvas(
        div(
          cls := "flex w-full max-w-md flex-col gap-6",
          Message(
            Message.content(
              Bubble.of(Bubble.Variant.Muted)(
                Bubble.content("Can you summarise the release notes for 0.4?")
              )
            )
          ),
          Message.end(
            Message.content(
              Bubble.of(Bubble.Variant.Default)(
                Bubble.content("Nine new components, a CodeBlock-based docs viewer, and RTL support."),
                Bubble.reactions()("🎉")
              )
            )
          ),
          Message(
            Message.content(
              Bubble.of(Bubble.Variant.Tinted)(Bubble.content("Tinted follows the primary color.")),
              Bubble.of(Bubble.Variant.Outline)(Bubble.contentButton("Outline — and clickable")),
              Bubble.of(Bubble.Variant.Destructive)(Bubble.content("Destructive, for a failed send."))
            )
          )
        )
      ),
      """Message(
  Message.content(
    Bubble.of(Bubble.Variant.Muted)(Bubble.content("Can you summarise the release notes for 0.4?"))
  )
)

Message.end(
  Message.content(
    Bubble.of(Bubble.Variant.Default)(
      Bubble.content("Nine new components, a CodeBlock-based docs viewer, and RTL support."),
      Bubble.reactions()("🎉")
    )
  )
)

Message(
  Message.content(
    Bubble.of(Bubble.Variant.Tinted)(Bubble.content("Tinted follows the primary color.")),
    Bubble.of(Bubble.Variant.Outline)(Bubble.contentButton("Outline — and clickable")),
    Bubble.of(Bubble.Variant.Destructive)(Bubble.content("Destructive, for a failed send."))
  )
)"""
    )

  // ── Code Block ──

  private val buttonSource =
    """object Button:
  def apply(mods: Modifier[HtmlElement]*): HtmlElement =
    button(typ := "button", dataAttr("slot") := "button", mods)"""

  private def codeBlock: Demo =
    Demo(
      canvas(
        div(
          cls := "flex w-full max-w-lg flex-col gap-4",
          CodeBlock(
            CodeBlock.header(
              CodeBlock.title(Icons.fileText(), "Button.scala"),
              CodeBlock.actions(CodeBlock.copyButton(buttonSource))
            ),
            CodeBlock.source(buttonSource, lineNumbers = true)
          ),
          CodeBlock(
            CodeBlock.actions(CodeBlock.copyButton("npx shadcn-scalajs@latest add code-block")),
            CodeBlock.source("npx shadcn-scalajs@latest add code-block")
          ),
          CodeBlock.of(size = CodeBlock.Size.Sm)(
            CodeBlock.content(lineNumbers = true)(
              CodeBlock.line("val theme = Var(Theme.System)"),
              CodeBlock.line("val theme = Var(Theme.Dark)", CodeBlock.LineVariant.Removed),
              CodeBlock.line("val theme = Var(Theme.Light)", CodeBlock.LineVariant.Added),
              CodeBlock.line("ThemeSwitcher(theme)", CodeBlock.LineVariant.Highlighted)
            )
          )
        )
      ),
      """val source = "object Button: ..."

CodeBlock(
  CodeBlock.header(
    CodeBlock.title(Icons.fileText(), "Button.scala"),
    CodeBlock.actions(CodeBlock.copyButton(source))
  ),
  CodeBlock.source(source, lineNumbers = true)
)

// Actions placed directly in the root float in the corner and fade in on hover.
CodeBlock(
  CodeBlock.actions(CodeBlock.copyButton("npx shadcn-scalajs@latest add code-block")),
  CodeBlock.source("npx shadcn-scalajs@latest add code-block")
)

// Diff and highlight lines.
CodeBlock.of(size = CodeBlock.Size.Sm)(
  CodeBlock.content(lineNumbers = true)(
    CodeBlock.line("val theme = Var(Theme.System)"),
    CodeBlock.line("val theme = Var(Theme.Dark)", CodeBlock.LineVariant.Removed),
    CodeBlock.line("val theme = Var(Theme.Light)", CodeBlock.LineVariant.Added),
    CodeBlock.line("ThemeSwitcher(theme)", CodeBlock.LineVariant.Highlighted)
  )
)"""
    )

  // ── Direction ──

  private def direction: Demo =
    val choice = Var(Option("rtl"))
    val dirSignal = choice.signal.map(c => if c.contains("ltr") then Direction.Dir.Ltr else Direction.Dir.Rtl)
    Demo(
      canvas(
        div(
          cls := "flex w-full max-w-sm flex-col gap-4",
          ToggleGroup.single(
            choice,
            Toggle.Variant.Outline,
            Toggle.Size.Sm,
            ToggleGroup.Item("ltr", Seq[Modifier[HtmlElement]]("LTR")),
            ToggleGroup.Item("rtl", Seq[Modifier[HtmlElement]]("RTL"))
          ),
          Direction.provider(dirSignal)(
            Card(
              Card.header(
                Card.title(
                  child.text <-- dirSignal.map(d => if d == Direction.Dir.Rtl then "تسجيل الدخول" else "Sign in")
                ),
                Card.description(
                  child.text <-- dirSignal.map(d =>
                    if d == Direction.Dir.Rtl then "أدخل بريدك الإلكتروني للمتابعة."
                    else "Enter your email to continue."
                  )
                )
              ),
              Card.content(
                cls := "flex flex-col gap-3",
                Input(typ := "email", placeholder := "name@example.com"),
                div(
                  cls := "flex justify-end gap-2",
                  Button.of(
                    _.variant(Button.Variant.Outline),
                    _ => child.text <-- dirSignal.map(d => if d == Direction.Dir.Rtl then "إلغاء" else "Cancel")
                  ),
                  Button.of(
                    _.variant(Button.Variant.Primary),
                    _ => child.text <-- dirSignal.map(d => if d == Direction.Dir.Rtl then "متابعة" else "Continue"),
                    _ => Icons.arrowRight(svg.cls := "rtl:rotate-180")
                  )
                )
              )
            )
          )
        )
      ),
      """val dir = Var(Direction.Dir.Rtl)

// Components use logical utilities (ms-*, ps-*, start-*, rtl:), so setting `dir`
// on an ancestor is all it takes to flip them.
Direction.provider(dir.signal)(
  Card(
    Card.header(Card.title("تسجيل الدخول"), Card.description("أدخل بريدك الإلكتروني للمتابعة.")),
    Card.content(
      cls := "flex flex-col gap-3",
      Input(typ := "email", placeholder := "name@example.com"),
      div(
        cls := "flex justify-end gap-2",
        Button.of(_.variant(Button.Variant.Outline), _ => "إلغاء"),
        Button.of(_.variant(Button.Variant.Primary), _ => "متابعة", _ => Icons.arrowRight(svg.cls := "rtl:rotate-180"))
      )
    )
  )
)"""
    )

  // ── Marker ──

  private def marker: Demo =
    Demo(
      canvas(
        div(
          cls := "flex w-full max-w-md flex-col gap-6",
          Marker.of(Marker.Variant.Separator)(Marker.content("Today")),
          Marker(Marker.icon(Spinner()), Marker.content(span(cls := "shimmer", "Thinking…"))),
          Marker(Marker.icon(Icons.user()), Marker.content("Ada joined the conversation")),
          Marker.of(Marker.Variant.Border)(
            Marker.icon(Icons.sparkles()),
            Marker.content("Answer generated from ", a(href := "#", "3 sources"))
          )
        )
      ),
      """Marker.of(Marker.Variant.Separator)(Marker.content("Today"))

Marker(Marker.icon(Spinner()), Marker.content(span(cls := "shimmer", "Thinking…")))

Marker(Marker.icon(Icons.user()), Marker.content("Ada joined the conversation"))

Marker.of(Marker.Variant.Border)(
  Marker.icon(Icons.sparkles()),
  Marker.content("Answer generated from ", a(href := "#", "3 sources"))
)"""
    )

  // ── Message ──

  private def avatar(initials: String): HtmlElement =
    Message.avatar(Avatar(Avatar.fallback(initials)))

  private def message: Demo =
    Demo(
      canvas(
        div(
          cls := "flex w-full max-w-md flex-col gap-6",
          Message(
            avatar("AL"),
            Message.content(
              Message.header("Ada Lovelace"),
              Bubble.of(Bubble.Variant.Muted)(
                Bubble.content("Has the registry been rebuilt since the new components landed?")
              ),
              Message.footer("09:41")
            )
          ),
          Message.end(
            avatar("ME"),
            Message.content(
              Bubble(Bubble.content("Yes — 93 items, all served from /registry.")),
              Message.footer("09:42 · Read")
            )
          ),
          Message(
            avatar("AI"),
            Message.content(
              Bubble.of(Bubble.Variant.Ghost)(
                Bubble.content(
                  "A ghost bubble drops the fill for long-form assistant replies, so prose reads like a document instead of a chat balloon."
                )
              )
            )
          )
        )
      ),
      """Message(
  Message.avatar(Avatar(Avatar.fallback("AL"))),
  Message.content(
    Message.header("Ada Lovelace"),
    Bubble.of(Bubble.Variant.Muted)(Bubble.content("Has the registry been rebuilt since the new components landed?")),
    Message.footer("09:41")
  )
)

// The sender's own turn: the row mirrors and the content aligns to the end.
Message.end(
  Message.avatar(Avatar(Avatar.fallback("ME"))),
  Message.content(
    Bubble(Bubble.content("Yes — 93 items, all served from /registry.")),
    Message.footer("09:42 · Read")
  )
)

Message(
  Message.avatar(Avatar(Avatar.fallback("AI"))),
  Message.content(Bubble.of(Bubble.Variant.Ghost)(Bubble.content("A ghost bubble drops the fill for long-form replies.")))
)"""
    )

  // ── Message Scroller ──

  private val replies = Seq(
    "Sure — what should it cover?",
    "The new components and the docs viewer.",
    "Got it. Drafting now.",
    "Here is a first pass. Want it shorter?",
    "Shorter, and lead with the RTL support.",
    "Done. Anything else?"
  )

  private def messageScroller: Demo =
    val scroller = MessageScroller.ctx()
    val messages = Var(
      (0 until 8).map(i => (i, i % 2 == 1, s"Message ${i + 1} — scroll up and new ones will not pull you down."))
    )
    def send(): Unit =
      messages.update { ms =>
        val n = ms.size
        ms :+ ((n, n % 2 == 1, replies(n % replies.size)))
      }
    Demo(
      canvas(
        div(
          cls := "flex w-full max-w-md flex-col gap-3",
          div(
            cls := "h-80 overflow-hidden rounded-xl border bg-card",
            MessageScroller.root(scroller)(
              MessageScroller.viewport(scroller)(
                cls := "p-4",
                MessageScroller.content(
                  cls := "gap-4",
                  children <-- messages.signal.split(_._1) { (_, initial, _) =>
                    val (_, mine, body) = initial
                    MessageScroller.item(
                      Message.withAlign(if mine then Message.Align.End else Message.Align.Start)(
                        Message.content(
                          Bubble.of(if mine then Bubble.Variant.Default else Bubble.Variant.Muted)(Bubble.content(body))
                        )
                      )
                    )
                  }
                )
              ),
              MessageScroller.button(scroller)()
            )
          ),
          Button.of(_.variant(Button.Variant.Outline), _ => onClick --> { _ => send() }, _ => "Add a message")
        )
      ),
      """val scroller = MessageScroller.ctx()
val messages = Var(Seq.empty[(Int, Boolean, String)])

div(
  cls := "h-80 overflow-hidden rounded-xl border bg-card",
  MessageScroller.root(scroller)(
    MessageScroller.viewport(scroller)(
      cls := "p-4",
      MessageScroller.content(
        children <-- messages.signal.split(_._1) { (_, initial, _) =>
          val (_, mine, body) = initial
          MessageScroller.item(
            Message.withAlign(if mine then Message.Align.End else Message.Align.Start)(
              Message.content(Bubble.of(if mine then Bubble.Variant.Default else Bubble.Variant.Muted)(Bubble.content(body)))
            )
          )
        }
      )
    ),
    // Hidden while the reader is at the newest message.
    MessageScroller.button(scroller)()
  )
)"""
    )

  // ── Questionnaire ──

  private def questionnaire: Demo =
    import Questionnaire.{Choice, Kind, Question}
    val state = Questionnaire.state(
      Seq(
        Question(
          "framework",
          "Which backend should the scaffold use?",
          choices = Seq(
            Choice("http4s", "http4s", Some("Typelevel, pure functional"), Some("1")),
            Choice("zio-http", "ZIO HTTP", Some("ZIO effects end to end"), Some("2")),
            Choice("none", "None for now", Some("Keep services framework-free"), Some("3"))
          )
        ),
        Question(
          "extras",
          "What else should be set up?",
          kind = Kind.Multiple,
          description = Some("Pick any that apply."),
          choices = Seq(
            Choice("ci", "GitHub Actions CI", shortcut = Some("a")),
            Choice("docker", "Dockerfile", shortcut = Some("b")),
            Choice("e2e", "Playwright tests", shortcut = Some("c"))
          ),
          required = false,
          skippable = true
        ),
        Question("name", "What should the project be called?", kind = Kind.Text, placeholder = "my-app")
      )
    )
    val summary = Var(Map.empty[String, Seq[String]])
    Demo(
      canvas(
        div(
          cls := "w-full max-w-md rounded-xl border bg-card p-5",
          Questionnaire(
            state,
            onSubmit = answers => summary.set(answers),
            done = div(
              cls := "flex flex-col gap-3 text-sm",
              p(cls := "font-medium", "Thanks — here is what you chose:"),
              ul(
                cls := "list-disc ps-5 text-muted-foreground",
                children <-- summary.signal.map(_.toSeq.sortBy(_._1).map((k, v) => li(s"$k: ${v.mkString(", ")}")))
              ),
              Button.of(
                _.variant(Button.Variant.Outline),
                _.size(Button.Size.Sm),
                _ => cls := "w-fit",
                _ => onClick --> { _ => state.reset() },
                _ => "Start over"
              )
            )
          )()
        )
      ),
      """import Questionnaire.{Choice, Kind, Question}

val state = Questionnaire.state(
  Seq(
    Question(
      "framework",
      "Which backend should the scaffold use?",
      choices = Seq(
        Choice("http4s", "http4s", Some("Typelevel, pure functional"), Some("1")),
        Choice("zio-http", "ZIO HTTP", Some("ZIO effects end to end"), Some("2")),
        Choice("none", "None for now", Some("Keep services framework-free"), Some("3"))
      )
    ),
    Question(
      "extras",
      "What else should be set up?",
      kind = Kind.Multiple,
      description = Some("Pick any that apply."),
      choices = Seq(Choice("ci", "GitHub Actions CI", shortcut = Some("a")), Choice("docker", "Dockerfile", shortcut = Some("b"))),
      required = false,
      skippable = true
    ),
    Question("name", "What should the project be called?", kind = Kind.Text, placeholder = "my-app")
  )
)

Questionnaire(state, onSubmit = answers => println(answers), done = p("Thanks!"))()"""
    )

  // ── Trace ──

  private def trace: Demo =
    val open = Var(true)
    Demo(
      canvas(
        div(
          cls := "w-full max-w-md",
          Trace(
            Trace.line(Trace.icon(Icons.search()), Trace.title("Searched the registry"), Trace.description("93 items")),
            Trace.collapsible(open)(
              trigger = Seq(Trace.line(Trace.icon(Icons.fileText()), Trace.title("Read 3 files"))),
              content = Seq(
                ul(
                  cls := "flex flex-col gap-1 font-mono text-xs",
                  li("packages/ui/.../Bubble.scala"),
                  li("packages/ui/.../Message.scala"),
                  li("apps/web/src/docs-highlight.js")
                )
              )
            ),
            button(
              typ := "button",
              Trace.line(Trace.icon(Icons.terminal()), Trace.title("Ran sbt ui/compile"), Trace.description("8s"))
            ),
            Trace.line(Trace.Status.Error)(
              Trace.icon(Icons.circleX()),
              Trace.title("npm test failed"),
              Trace.description("1 of 12")
            ),
            Trace.line(Trace.Status.Running)(Trace.icon(Spinner()), Trace.title("Fixing the failing test…")),
            Trace.line(Trace.Status.Pending)(Trace.icon(Icons.check()), Trace.title("Commit and push"))
          )
        )
      ),
      """val open = Var(true)

Trace(
  Trace.line(Trace.icon(Icons.search()), Trace.title("Searched the registry"), Trace.description("93 items")),
  Trace.collapsible(open)(
    trigger = Seq(Trace.line(Trace.icon(Icons.fileText()), Trace.title("Read 3 files"))),
    content = Seq(ul(li("Bubble.scala"), li("Message.scala"), li("docs-highlight.js")))
  ),
  // Wrap a line in a button or link and it becomes interactive.
  button(typ := "button", Trace.line(Trace.icon(Icons.terminal()), Trace.title("Ran sbt ui/compile"), Trace.description("8s"))),
  Trace.line(Trace.Status.Error)(Trace.icon(Icons.circleX()), Trace.title("npm test failed"), Trace.description("1 of 12")),
  Trace.line(Trace.Status.Running)(Trace.icon(Spinner()), Trace.title("Fixing the failing test…")),
  Trace.line(Trace.Status.Pending)(Trace.icon(Icons.check()), Trace.title("Commit and push"))
)"""
    )
