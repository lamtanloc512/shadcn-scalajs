package shadcnscalajs.site

import com.raquo.laminar.api.L.*

/** The guide article shown at `/docs`. The shell sidebar already lists these pages. */
object DocsPage:

  final case class Page(slug: String, title: String, description: String)

  val pages: List[Page] = List(
    Page("index", "Introduction", "Copy the components into your project. There is no runtime library."),
    Page("installation", "Installation", "Create a Scala.js and Laminar project."),
    Page("existing-project", "Existing project", "Point a project you already have at the registry."),
    Page("project-layout", "Project layout", "Where shared, services, and ui belong."),
    Page("components", "Add components", "Copy components and blocks with the CLI."),
    Page("mcp", "MCP server", "Let an agent list and read the registry.")
  )

  def title(slug: String): String =
    pages.find(_.slug == slug).map(page => s"${page.title} – shadcn-scalajs").getOrElse("Docs – shadcn-scalajs")

  def apply(slug: String): HtmlElement =
    val page = pages.find(_.slug == slug)
    mainTag(
      cls := "min-w-0 px-5 py-10 sm:px-8 lg:px-10 xl:col-span-2",
      articleTag(
        cls := "mx-auto max-w-2xl",
        page match
          case Some(found) =>
            Seq(
              h1(cls := "text-3xl font-semibold tracking-tight", found.title),
              p(cls := "mt-2 text-base text-muted-foreground", found.description),
              div(cls := "mt-10 space-y-10 text-sm leading-7 text-muted-foreground", body(found.slug))
            )
          case None =>
            Seq(
              h1(cls := "text-3xl font-semibold tracking-tight", "Page not in the guide"),
              p(cls := "mt-2 text-base text-muted-foreground", s"There is no docs page named $slug.")
            )
      )
    )

  private def body(slug: String): HtmlElement = slug match
    case "installation"     => installation
    case "existing-project" => existingProject
    case "project-layout"   => layout
    case "components"       => addComponents
    case "mcp"              => mcp
    case _                  => introduction

  private def h(text: String): HtmlElement =
    h2(cls := "text-xl font-semibold tracking-tight text-foreground", text)

  private def shell(source: String): HtmlElement =
    pre(
      cls := "mt-3 overflow-x-auto rounded-lg border bg-muted/40 p-4 text-sm text-foreground",
      code(cls := "font-mono", source)
    )

  private def mono(value: String): HtmlElement =
    code(cls := "rounded bg-muted px-1.5 py-0.5 font-mono text-foreground", value)

  private def introduction: HtmlElement =
    div(
      cls := "space-y-6",
      p(
        "The CLI copies component source into your project. You edit those files. Nothing is imported from a published component package at runtime."
      ),
      p(
        "The components are Laminar views styled with Tailwind CSS v4 utilities, following shadcn/ui's new-york-v4 source. A generated app also has a JVM module for services and a shared module for contracts that compile to both platforms."
      ),
      h("What you own"),
      p(
        "After ",
        mono("add"),
        ", a button is a Scala file under ",
        mono("packages/ui/src/main/scala/shadcnscalajs/"),
        ". Change the markup, the variants, or the classes there. Running ",
        mono("add"),
        " again for that same item, or for anything that depends on it, writes the registry copy back over the file."
      ),
      h("What you need"),
      p(
        "JDK 21, sbt 1.10 or newer, and Node.js 20 or newer. The first ",
        mono("npm run dev"),
        " compiles Scala.js before Vite starts. Later saves reload the browser without restarting sbt."
      ),
      p(
        "Continue with ",
        a(href := "/docs/installation", cls := "text-foreground underline underline-offset-4", "Installation"),
        "."
      )
    )

  private def installation: HtmlElement =
    div(
      cls := "space-y-6",
      p(
        "Use an empty directory. ",
        mono("init"),
        " refuses to write into a directory that already has files unless you pass ",
        mono("--force"),
        ". For a project you already have, use ",
        a(href := "/docs/existing-project", cls := "text-foreground underline underline-offset-4", "Existing project"),
        "."
      ),
      h("Prerequisites"),
      shell("java --version    # 21\nsbt --version     # 1.10 or newer\nnode --version    # 20 or newer"),
      p(
        "sbt installed through Coursier is often missing from a new shell:"
      ),
      shell("export PATH=\"$PATH:$HOME/Library/Application Support/Coursier/bin\""),
      h("Create a project"),
      shell("mkdir my-app\ncd my-app\nnpx shadcn-scalajs@latest init"),
      p(
        "The prompts are the project name and the sbt artifact group, for example ",
        mono("com.example.app"),
        "."
      ),
      shell(
        "npx shadcn-scalajs@latest init \\\n  --project-name my-app \\\n  --group com.example.app"
      ),
      p(
        "A preset code from the ",
        a(href := "/create", cls := "text-foreground underline underline-offset-4", "customizer"),
        " picks the style pack and theme tokens. Omit ",
        mono("--preset"),
        " and the project starts on the Nova pack. ",
        mono("--style"),
        " accepts nova, vega, maia, lyra, mira, luma, sera, or rhea."
      ),
      shell(
        "npx shadcn-scalajs@latest init \\\n  --project-name my-app \\\n  --group com.example.app \\\n  --preset buFywLo"
      ),
      p(
        "When the current directory is named ",
        mono("examples"),
        " or ",
        mono(".examples"),
        ", init creates ",
        mono("examples/<project-name>"),
        " and prints the path to enter next."
      ),
      h("Run it"),
      shell("npm install\nnpm run dev"),
      p(
        "The browser stays blank until the first Scala.js link finishes. That wait is the compile, not a hung dev server. Saving a Scala file rebuilds and reloads the page."
      ),
      h("Production"),
      shell("npm run compile\nnpm run build"),
      p("The optimized frontend is written to ", mono("packages/ui/dist"), ".")
    )

  private def existingProject: HtmlElement =
    div(
      cls := "space-y-6",
      p(
        mono("init --no-scaffold"),
        " writes only ",
        mono("shadcn-scalajs.json"),
        ". It does not create ",
        mono("build.sbt"),
        ", packages, or a Vite app."
      ),
      shell(
        "npx shadcn-scalajs@latest init --no-scaffold \\\n  --source-dir src/main/scala/shadcnscalajs"
      ),
      p(
        mono("--source-dir"),
        " is the directory that already matches the ",
        mono("shadcnscalajs"),
        " package. ",
        mono("add"),
        " writes ",
        mono("ui/Button.scala"),
        " under that directory and does not rewrite package declarations. The folder name has to stay ",
        mono("shadcnscalajs"),
        "."
      ),
      p(
        "Pass ",
        mono("--registry"),
        " when the components should come from somewhere other than ",
        mono("https://shadcn-scalajs.vercel.app/registry"),
        "."
      ),
      p(
        "The project still needs Tailwind CSS v4, the theme files ",
        mono("add"),
        " installs on first use (",
        mono("tokens.css"),
        " and one ",
        mono("pack-*.css"),
        "), and the small ",
        mono("shadcnscalajs.core"),
        " sources. A scaffold from ",
        mono("init"),
        " includes that core package. ",
        mono("init"),
        " will not replace an existing ",
        mono("shadcn-scalajs.json"),
        " unless you pass ",
        mono("--force"),
        "."
      )
    )

  private def layout: HtmlElement =
    div(
      cls := "space-y-6",
      shell(
        "my-app/\n├── packages/\n│   ├── shared/       # contracts compiled for Scala.js and the JVM\n│   ├── services/     # JVM services, with no HTTP framework chosen\n│   └── ui/           # Laminar, Vite, Tailwind CSS v4, components\n├── project/\n├── build.sbt\n├── package.json\n└── shadcn-scalajs.json"
      ),
      h("shared"),
      p("Platform-neutral models and contracts. This module is compiled for Scala.js and for the JVM."),
      h("services"),
      p(
        "The JVM boundary. Add http4s, ZIO HTTP, Pekko HTTP, Play, or another server here. Keep Laminar types out of it."
      ),
      h("ui"),
      p(
        "The browser. Copied components live in ",
        mono("packages/ui/src/main/scala/shadcnscalajs/"),
        ". Theme tokens and the active style pack live in ",
        mono("packages/ui/src/styles/"),
        ". ",
        mono("globals.css"),
        " imports Tailwind, the tokens, and one pack."
      ),
      p(
        mono("shadcn-scalajs.json"),
        " records the registry URL, the source directory, and the active style pack. ",
        mono("add"),
        " reads it."
      )
    )

  private def addComponents: HtmlElement =
    div(
      cls := "space-y-6",
      p(
        "Run ",
        mono("add"),
        " from the directory that contains ",
        mono("shadcn-scalajs.json"),
        "."
      ),
      shell("npx shadcn-scalajs@latest add button card dialog"),
      p(
        "Names match the registry: ",
        mono("button"),
        ", ",
        mono("dropdown-menu"),
        ", ",
        mono("login-01"),
        ", ",
        mono("dashboard-01"),
        ". The command walks ",
        mono("registryDependencies"),
        " and writes those files too."
      ),
      p("Theme CSS is installed from files bundled in the CLI. Pass ", mono("--style"), " to switch packs:"),
      shell("npx shadcn-scalajs@latest add button --style lyra"),
      p(
        "Browse names in the ",
        a(href := "/components", cls := "text-foreground underline underline-offset-4", "component gallery"),
        " and the ",
        a(href := "/blocks", cls := "text-foreground underline underline-offset-4", "block gallery"),
        "."
      ),
      h("After the files land"),
      p(
        "A button with no variant and no size is primary, at the default height. Pass both when you want another look:"
      ),
      shell(
        "Button.of(_.variant(Button.Variant.Outline), _.size(Button.Size.Sm), _ => \"Save\")"
      ),
      p(
        "Blocks are mountable elements, not routes. A login block is ",
        mono("Login01()"),
        " wherever your own router wants that screen."
      ),
      p(
        mono("add"),
        " writes the registry copy each time it installs an item, including dependencies. Run it before you customize those files."
      )
    )

  private def mcp: HtmlElement =
    div(
      cls := "space-y-6",
      p(
        mono("shadcn-scalajs mcp"),
        " is a Model Context Protocol server on stdio. It reads the same registry as ",
        mono("add"),
        ". The official shadcn MCP server expects React registry items, so it does not understand ",
        mono("scala:ui"),
        " and ",
        mono("scala:block"),
        " payloads."
      ),
      h("Connect a client"),
      shell(
        "{\n  \"mcpServers\": {\n    \"shadcn-scalajs\": {\n      \"command\": \"npx\",\n      \"args\": [\"-y\", \"shadcn-scalajs@latest\", \"mcp\"]\n    }\n  }\n}"
      ),
      p(
        "The server uses the registry in ",
        mono("shadcn-scalajs.json"),
        " when that file is in the working directory. Otherwise it uses ",
        mono("https://shadcn-scalajs.vercel.app/registry"),
        "."
      ),
      p(
        "Protocol messages go to stdout. Logs go to stderr. The published ",
        mono("mcp"),
        " command is available after the next CLI release. From this repository, build ",
        mono("packages/cli"),
        " and run:"
      ),
      shell("node packages/cli/dist/index.js mcp \\\n  --registry apps/web/public/registry"),
      h("Tools"),
      ul(
        cls := "list-disc space-y-2 pl-5",
        li(mono("list_items"), " — names, titles, types, and registry dependencies."),
        li(mono("search_items"), " — items whose name or title contains the query."),
        li(mono("get_item"), " — description, dependencies, file targets, and the add command."),
        li(mono("install_command"), " — the npx add command for the names you pass.")
      ),
      p("The server does not write files. The agent runs the printed ", mono("add"), " command when it is ready.")
    )
