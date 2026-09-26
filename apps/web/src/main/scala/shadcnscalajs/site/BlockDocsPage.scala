package shadcnscalajs.site

import com.raquo.laminar.api.L.*
import org.scalajs.dom
import shadcnscalajs.ui.*

import scala.scalajs.js
import scala.scalajs.js.JSON

/** `/blocks/<name>` — Preview/Code tabs, file tree, install command. */
object BlockDocsPage:

  private final case class SourceFile(target: String, content: String)

  def apply(name: String): HtmlElement =
    val meta = Blocks.find(name)
    val files = Var(List.empty[SourceFile])
    val selectedTab = Var("preview")
    val iframeKey = Var(0)
    val iframeRef = Var(Option.empty[dom.html.IFrame])
    var themeObserver = Option.empty[dom.MutationObserver]
    val themeAttributes = List(
      "data-style-pack",
      "data-base-color",
      "data-theme-color",
      "data-chart-color",
      "data-heading-font",
      "data-body-font",
      "data-icon-library",
      "data-radius",
      "data-menu-color",
      "data-menu-accent"
    )

    def syncIframeTheme(): Unit =
      iframeRef.now().foreach { iframe =>
        val doc = iframe.contentDocument
        if doc != null then
          ThemeConfig.applyToDocument(ThemeConfig.load(), doc)
          val source = dom.document.documentElement
          val target = doc.documentElement
          themeAttributes.foreach { attr =>
            Option(source.getAttribute(attr)).fold(target.removeAttribute(attr))(target.setAttribute(attr, _))
          }
          if source.classList.contains("dark") then target.classList.add("dark") else target.classList.remove("dark")
      }

    fetchFiles(name, files)

    val previewPanel =
      div(
        cls := "overflow-hidden rounded-lg border",
        onMountUnmountCallback(
          mount = _ =>
            val observer = new dom.MutationObserver((_, _) => syncIframeTheme())
            observer.observe(
              dom.document.documentElement,
              new dom.MutationObserverInit {
                attributes = true
              }
            )
            themeObserver = Some(observer)
          ,
          unmount = _ =>
            themeObserver.foreach(_.disconnect())
            themeObserver = None
            iframeRef.set(None)
        ),
        child <-- iframeKey.signal.map { key =>
          iframe(
            cls := "h-[640px] w-full bg-background",
            title := s"$name preview",
            src := s"/blocks/$name/preview?r=$key",
            onMountCallback { ctx =>
              iframeRef.set(Some(ctx.thisNode.ref.asInstanceOf[dom.html.IFrame]))
              syncIframeTheme()
            },
            onLoad --> { ev =>
              iframeRef.set(Some(ev.target.asInstanceOf[dom.html.IFrame]))
              syncIframeTheme()
            }
          )
        }
      )

    val codePanel =
      div(
        cls := "flex flex-col gap-4",
        children <-- files.signal.map(_.map { f =>
          div(
            cls := "overflow-hidden rounded-lg border",
            div(cls := "border-b bg-muted px-4 py-2 font-mono text-xs text-muted-foreground", f.target),
            pre(cls := "overflow-x-auto p-4 text-sm", code(f.content))
          )
        })
      )

    BlocksLayout(
      div(
        cls := "mx-auto w-full max-w-5xl px-6 py-12",
        h1(cls := "text-3xl font-semibold tracking-tight", meta.map(_.title).getOrElse(name)),
        p(cls := "mt-2 text-base text-muted-foreground", meta.map(_.description).getOrElse("")),
        Tabs(
          cls := "mt-6 gap-4",
          div(
            cls := "flex flex-wrap items-center gap-2",
            Tabs.list(
              Tabs.ListVariant.Default,
              tabTrigger(selectedTab, "preview", "Preview"),
              tabTrigger(selectedTab, "code", "Code")
            ),
            Button(
              Button.ButtonApi.variant(Button.Variant.Outline),
              Button.ButtonApi.size(Button.Size.Sm),
              cls := "ml-auto",
              onClick --> { _ => iframeKey.update(_ + 1) },
              "Refresh"
            ),
            Button.anchor(
              s"/blocks/$name/preview",
              Button.ButtonApi.variant(Button.Variant.Outline),
              Button.ButtonApi.size(Button.Size.Sm),
              target := "_blank",
              rel := "noopener",
              "Open in New Tab"
            )
          ),
          Tabs.content(
            display <-- selectedTab.signal.map(v => if v == "preview" then "block" else "none"),
            previewPanel
          ),
          Tabs.content(
            display <-- selectedTab.signal.map(v => if v == "code" then "block" else "none"),
            codePanel
          )
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

  private def tabTrigger(selected: Var[String], value: String, label: String): HtmlElement =
    Tabs.trigger(
      dataAttr("value") := value,
      tabIndex <-- selected.signal.map(v => if v == value then 0 else -1),
      inContext { thisNode =>
        selected.signal --> { v =>
          if v == value then thisNode.ref.setAttribute("data-active", "true")
          else thisNode.ref.removeAttribute("data-active")
        }
      },
      aria.selected <-- selected.signal.map(_ == value),
      onClick --> { _ => selected.set(value) },
      label
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
