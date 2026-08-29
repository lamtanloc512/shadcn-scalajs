package shadcnscalajs.site

import com.raquo.laminar.api.L.*
import shadcnscalajs.ui.{Badge, Button, Card, Separator}

object InstallationPage:

  private def shellCode(source: String): HtmlElement =
    pre(
      cls := "mt-4 overflow-x-auto rounded-lg border bg-muted/40 p-4 text-sm",
      code(cls := "font-mono", source)
    )

  private def step(id: String, number: String, title: String, content: Modifier[HtmlElement]*): HtmlElement =
    sectionTag(
      idAttr := id,
      cls := "scroll-mt-24",
      div(
        cls := "flex items-center gap-3",
        span(
          cls := "flex size-7 shrink-0 items-center justify-center rounded-full bg-primary text-xs font-semibold text-primary-foreground",
          number
        ),
        h2(cls := "text-xl font-semibold tracking-tight", title)
      ),
      div(cls := "ml-10 mt-3 text-sm leading-7 text-muted-foreground", content)
    )

  def apply(): HtmlElement =
    mainTag(
      cls := "mx-auto w-full max-w-6xl px-6 py-10 lg:py-14",
      div(
        cls := "grid gap-12 lg:grid-cols-[minmax(0,1fr)_220px]",
        articleTag(
          cls := "min-w-0 max-w-3xl",
          Badge.of(_.variant(Badge.Variant.Secondary), _ => "Alpha"),
          h1(cls := "mt-4 text-4xl font-semibold tracking-tight", "Installation"),
          p(
            cls := "mt-4 max-w-2xl text-lg leading-8 text-muted-foreground",
            "Scaffold a Scala.js application with shared JVM/JavaScript contracts, backend-neutral services, and a Laminar UI that is ready for shadcn-scalajs components."
          ),
          Separator(Separator.Orientation.Horizontal, cls := "my-10"),
          div(
            cls := "space-y-10",
            step(
              "prerequisites",
              "1",
              "Install the prerequisites",
              p("Use JDK 21, sbt 1.10 or newer, and Node.js 20 or newer."),
              shellCode("java --version\nsbt --version\nnode --version")
            ),
            step(
              "create-project",
              "2",
              "Create an empty project directory",
              p("Run the initializer inside the directory that should become your project."),
              shellCode("mkdir my-app\ncd my-app\nnpx shadcn-scalajs@latest init --preset buFywLo"),
              p(
                cls := "mt-3",
                "The CLI asks for a project name and an artifact group such as ",
                code(cls := "rounded bg-muted px-1.5 py-0.5 text-foreground", "org.ethan.app"),
                ". The preset selects the initial style pack and theme tokens for the generated UI."
              ),
              Card(
                cls := "mt-4 gap-2 border-dashed p-4 text-sm shadow-none",
                p(cls := "font-medium text-foreground", "Working inside an examples directory?"),
                p(
                  "When the current directory is named ",
                  code("examples"),
                  " or ",
                  code(".examples"),
                  ", the CLI creates ",
                  code("examples/<project-name>"),
                  " and prints the directory to enter next."
                )
              )
            ),
            step(
              "packages",
              "3",
              "Understand the generated packages",
              shellCode(
                "packages/\n├── shared/    # code compiled for Scala.js and the JVM\n├── services/  # backend-neutral JVM services\n└── ui/        # Laminar, Vite, Tailwind CSS v4, and components"
              ),
              p(
                cls := "mt-3",
                "Put cross-platform domain models and contracts in ",
                code("shared"),
                ", your chosen backend implementation in ",
                code("services"),
                ", and browser code in ",
                code("ui"),
                "."
              )
            ),
            step(
              "development",
              "4",
              "Start development",
              shellCode("npm install\nnpm run dev"),
              p(
                cls := "mt-3",
                "The dev command starts the sbt watcher, waits for that run to become ready, and then starts Vite. Editing Scala sources rebuilds and reloads the browser automatically without the sbt restart race."
              )
            ),
            step(
              "components",
              "5",
              "Add components",
              shellCode("npx shadcn-scalajs@latest add button card dialog"),
              p(
                cls := "mt-3",
                "Component source is copied into ",
                code("packages/ui/src/main/scala/shadcnscalajs"),
                ". Theme tokens and the selected style pack live in ",
                code("packages/ui/src/styles"),
                ". You own and can edit every generated file."
              )
            ),
            step(
              "production",
              "6",
              "Build for production",
              shellCode("npm run compile\nnpm run build"),
              p(cls := "mt-3", "The optimized frontend is written to ", code("packages/ui/dist"), ".")
            )
          ),
          div(
            cls := "mt-12 flex flex-wrap gap-3",
            Button.anchor(
              "/components",
              Button.ButtonApi.variant(Button.Variant.Primary),
              Button.ButtonApi.size(Button.Size.Default),
              "Browse components"
            ),
            Button.anchor(
              "/create",
              Button.ButtonApi.variant(Button.Variant.Outline),
              Button.ButtonApi.size(Button.Size.Default),
              "Customize a preset"
            )
          )
        ),
        asideTag(
          cls := "hidden lg:block",
          navTag(
            cls := "sticky top-24 border-l pl-5 text-sm",
            aria.label := "On this page",
            p(cls := "mb-3 text-xs font-medium uppercase tracking-wide text-muted-foreground", "On this page"),
            div(
              cls := "flex flex-col gap-2 text-muted-foreground",
              a(href := "#prerequisites", cls := "hover:text-foreground", "Prerequisites"),
              a(href := "#create-project", cls := "hover:text-foreground", "Create a project"),
              a(href := "#packages", cls := "hover:text-foreground", "Project packages"),
              a(href := "#development", cls := "hover:text-foreground", "Development"),
              a(href := "#components", cls := "hover:text-foreground", "Components"),
              a(href := "#production", cls := "hover:text-foreground", "Production")
            )
          )
        )
      )
    )
