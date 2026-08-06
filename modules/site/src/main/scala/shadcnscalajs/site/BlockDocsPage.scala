package shadcnscalajs.site

import com.raquo.laminar.api.L.*
import org.scalajs.dom

import scala.scalajs.js
import scala.scalajs.js.JSON

/** `/blocks/<name>` — Preview/Code tabs, file tree, install command. */
object BlockDocsPage:

  private final case class SourceFile(target: String, content: String)

  def apply(name: String): HtmlElement =
    val meta = Blocks.find(name)
    val files = Var(List.empty[SourceFile])
    val showCode = Var(false)
    val iframeKey = Var(0)

    fetchFiles(name, files)

    BlocksLayout(
      div(
        cls := "mx-auto w-full max-w-5xl px-6 py-12",
        h1(cls := "text-3xl font-semibold tracking-tight", meta.map(_.title).getOrElse(name)),
        p(cls := "mt-2 text-base text-muted-foreground", meta.map(_.description).getOrElse("")),
        div(
          cls := "mt-6 flex items-center gap-2",
          button(
            typ := "button",
            cls := "inline-flex h-8 items-center rounded-md border px-3 text-sm font-medium",
            cls("bg-accent text-accent-foreground") <-- showCode.signal.map(!_),
            onClick --> { _ => showCode.set(false) },
            "Preview"
          ),
          button(
            typ := "button",
            cls := "inline-flex h-8 items-center rounded-md border px-3 text-sm font-medium",
            cls("bg-accent text-accent-foreground") <-- showCode.signal,
            onClick --> { _ => showCode.set(true) },
            "Code"
          ),
          button(
            typ := "button",
            cls := "ml-auto inline-flex h-8 items-center rounded-md border px-3 text-sm font-medium",
            onClick --> { _ => iframeKey.update(_ + 1) },
            "Refresh"
          ),
          a(
            href := s"/blocks/$name/preview",
            target := "_blank",
            rel := "noopener",
            cls := "inline-flex h-8 items-center rounded-md border px-3 text-sm font-medium",
            "Open in New Tab"
          )
        ),
        div(
          cls := "mt-4 overflow-hidden rounded-lg border",
          cls("hidden") <-- showCode.signal,
          child <-- iframeKey.signal.map { key =>
            iframe(
              cls := "h-[640px] w-full bg-background",
              title := s"$name preview",
              src := s"/blocks/$name/preview?r=$key"
            )
          }
        ),
        div(
          cls := "mt-4 flex flex-col gap-4",
          cls("hidden") <-- showCode.signal.map(!_),
          children <-- files.signal.map(_.map { f =>
            div(
              cls := "overflow-hidden rounded-lg border",
              div(cls := "border-b bg-muted px-4 py-2 font-mono text-xs text-muted-foreground", f.target),
              pre(cls := "overflow-x-auto p-4 text-sm", code(f.content))
            )
          })
        ),
        div(
          cls := "mt-10",
          h2(cls := "text-lg font-semibold", "Installation"),
          pre(
            cls := "mt-3 overflow-x-auto rounded-lg border bg-muted p-4 text-sm",
            code(s"npx shadcn-scalajs add $name")
          )
        )
      )
    )

  private def fetchFiles(name: String, into: Var[List[SourceFile]]): Unit =
    dom
      .fetch(s"/registry/$name.json")
      .`then`[String](_.text())
      .`then`[Unit] { text =>
        val parsed = JSON.parse(text)
        val arr = parsed.files.asInstanceOf[js.Array[js.Dynamic]]
        into.set(arr.toList.map(f => SourceFile(f.target.asInstanceOf[String], f.content.asInstanceOf[String])))
      }
