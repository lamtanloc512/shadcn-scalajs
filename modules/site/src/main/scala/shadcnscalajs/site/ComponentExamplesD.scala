package shadcnscalajs.site

import com.raquo.laminar.api.L.*
import shadcnscalajs.ui.*

/** Focused recipes from the local upstream shadcn-svelte examples. */
private[site] object ComponentExamplesD:
  private def ex(anchor: String, title: String, preview: HtmlElement, code: String): DocExample =
    DocExample(anchor, title, None, preview, code)

  private def card(label: String)(mods: Modifier[HtmlElement]*): HtmlElement =
    div(cls := "w-full max-w-md rounded-lg border p-6", h3(cls := "mb-4 font-medium", label), mods)

  private def menuRow(label: String, active: Boolean = false): HtmlElement =
    Sidebar.menuItem(Sidebar.menuButton(active)(span(label)))

  private def sidebar(content: Modifier[HtmlElement]*): HtmlElement =
    Sidebar.provider()(Sidebar.root(Sidebar.Collapsible.None)(content))

  private def verticalResizable(): HtmlElement =
    Resizable.paneGroup(
      dataAttr("direction") := "vertical",
      cls := "min-h-[200px] max-w-md rounded-lg border",
      div(
        styleAttr := "flex-basis: 25%;",
        cls := "flex-1",
        div(cls := "flex h-full items-center justify-center p-6", span(cls := "font-semibold", "Header"))
      ),
      Resizable.handle(),
      div(
        styleAttr := "flex-basis: 75%;",
        cls := "flex-1",
        div(cls := "flex h-full items-center justify-center p-6", span(cls := "font-semibold", "Content"))
      )
    )

  def apply(slug: String): Seq[DocExample] = slug match
    case "resizable" =>
      Seq(
        ex(
          "demo",
          "Demo",
          Resizable.paneGroup(
            cls := "max-w-md rounded-lg border",
            div(
              styleAttr := "flex-basis: 50%;",
              div(cls := "flex h-[200px] items-center justify-center p-6", span(cls := "font-semibold", "One"))
            ),
            Resizable.handle(),
            div(
              styleAttr := "flex-basis: 50%;",
              Resizable.paneGroup(
                dataAttr("direction") := "vertical",
                div(
                  styleAttr := "flex-basis: 25%;",
                  div(cls := "flex h-full items-center justify-center p-6", span(cls := "font-semibold", "Two"))
                ),
                Resizable.handle(),
                div(
                  styleAttr := "flex-basis: 75%;",
                  div(cls := "flex h-full items-center justify-center p-6", span(cls := "font-semibold", "Three"))
                )
              )
            )
          ),
          """Resizable.paneGroup(
  cls := "max-w-md rounded-lg border",
  div(styleAttr := "flex-basis: 50%;", div(cls := "flex h-[200px] items-center justify-center p-6", span(cls := "font-semibold", "One"))),
  Resizable.handle(),
  div(styleAttr := "flex-basis: 50%;", Resizable.paneGroup(
    dataAttr("direction") := "vertical",
    div(styleAttr := "flex-basis: 25%;", div(cls := "flex h-full items-center justify-center p-6", span(cls := "font-semibold", "Two"))),
    Resizable.handle(),
    div(styleAttr := "flex-basis: 75%;", div(cls := "flex h-full items-center justify-center p-6", span(cls := "font-semibold", "Three")))
  ))
)"""
        ),
        ex(
          "vertical",
          "Vertical",
          verticalResizable(),
          """Resizable.paneGroup(
  dataAttr("direction") := "vertical",
  cls := "min-h-[200px] max-w-md rounded-lg border",
  div(styleAttr := "flex-basis: 25%;", div(cls := "flex h-full items-center justify-center p-6", span(cls := "font-semibold", "Header"))),
  Resizable.handle(),
  div(styleAttr := "flex-basis: 75%;", div(cls := "flex h-full items-center justify-center p-6", span(cls := "font-semibold", "Content")))
)"""
        ),
        ex(
          "handle",
          "Handle",
          Resizable.paneGroup(
            cls := "min-h-[200px] max-w-md rounded-lg border",
            div(
              styleAttr := "flex-basis: 25%;",
              div(cls := "flex h-full items-center justify-center p-6", span(cls := "font-semibold", "Sidebar"))
            ),
            Resizable.handle(true),
            div(
              styleAttr := "flex-basis: 75%;",
              div(cls := "flex h-full items-center justify-center p-6", span(cls := "font-semibold", "Content"))
            )
          ),
          """Resizable.paneGroup(
  cls := "min-h-[200px] max-w-md rounded-lg border",
  div(styleAttr := "flex-basis: 25%;", div(cls := "flex h-full items-center justify-center p-6", span(cls := "font-semibold", "Sidebar"))),
  Resizable.handle(true),
  div(styleAttr := "flex-basis: 75%;", div(cls := "flex h-full items-center justify-center p-6", span(cls := "font-semibold", "Content")))
)"""
        )
      )
    case "scroll-area" =>
      val tags = (0 until 50).map(i => s"v1.2.0-beta.${50 - i}")
      val tagNodes =
        tags.map(tag => div(cls := "text-sm", tag, Separator(Separator.Orientation.Horizontal, cls := "my-2")))
      val artwork = List(
        (
          "Ornella Binni",
          "https://images.unsplash.com/photo-1465869185982-5a1a7522cbcb?auto=format&fit=crop&w=300&q=80"
        ),
        ("Tom Byrom", "https://images.unsplash.com/photo-1548516173-3cabfa4607e9?auto=format&fit=crop&w=300&q=80"),
        (
          "Vladimir Malyavko",
          "https://images.unsplash.com/photo-1494337480532-3725c85fd2ab?auto=format&fit=crop&w=300&q=80"
        )
      )
      val artistNodes = artwork.map { case (artist, art) =>
        figure(
          cls := "shrink-0",
          div(
            cls := "overflow-hidden rounded-md",
            img(
              src := art,
              alt := s"Photo by $artist",
              cls := "aspect-[3/4] h-fit w-fit object-cover",
              width := "300",
              height := "400"
            )
          ),
          figCaption(
            cls := "pt-2 text-xs text-muted-foreground",
            "Photo by ",
            span(cls := "font-semibold text-foreground", artist)
          )
        )
      }
      Seq(
        ex(
          "demo",
          "Demo",
          ScrollArea(
            cls := "h-72 w-48 rounded-md border",
            div(cls := "p-4", h4(cls := "mb-4 text-sm leading-none font-medium", "Tags"), tagNodes)
          ),
          "val tags = (0 until 50).map(i => s\\\"v1.2.0-beta.${50 - i}\\\")\\nScrollArea(cls := \\\"h-72 w-48 rounded-md border\\\", div(cls := \\\"p-4\\\", h4(cls := \\\"mb-4 text-sm leading-none font-medium\\\", \\\"Tags\\\"), tags.map(tag => div(cls := \\\"text-sm\\\", tag, Separator(Separator.Orientation.Horizontal, cls := \\\"my-2\\\")))))"
        ),
        ex(
          "horizontal",
          "Horizontal",
          ScrollArea(
            cls := "w-96 rounded-md border whitespace-nowrap",
            dataAttr("orientation") := "horizontal",
            div(cls := "flex w-max space-x-4 p-4", artistNodes)
          ),
          "val works = List(\\\"Ornella Binni\\\", \\\"Tom Byrom\\\", \\\"Vladimir Malyavko\\\")\\nScrollArea(cls := \\\"w-96 rounded-md border whitespace-nowrap\\\", div(cls := \\\"flex w-max space-x-4 p-4\\\", works.map(artist => figure(cls := \\\"shrink-0\\\", figCaption(cls := \\\"pt-2 text-xs text-muted-foreground\\\", \\\"Photo by \\\", span(cls := \\\"font-semibold text-foreground\\\", artist)))))"
        )
      )
    case "select" =>
      val value = Var("")
      val fruits = List(
        ("apple", "Apple"),
        ("banana", "Banana"),
        ("blueberry", "Blueberry"),
        ("grapes", "Grapes"),
        ("pineapple", "Pineapple")
      )
      Seq(
        ex(
          "demo",
          "Demo",
          div(
            cls := "w-[180px]",
            Select(value, "Select a fruit") { ctx =>
              Seq(
                ctx.group(
                  Select.label("Fruits"),
                  fruits.map { case (v, label) =>
                    ctx.item(v, label, if v == "grapes" then disabled := true else emptyMod)
                  }
                )
              )
            }
          ),
          """val fruits = List(("apple", "Apple"), ("banana", "Banana"), ("blueberry", "Blueberry"), ("grapes", "Grapes"), ("pineapple", "Pineapple"))
val value = Var("")
div(cls := "w-[180px]", Select(value, "Select a fruit") { ctx =>
  Seq(ctx.group(Select.label("Fruits"), fruits.map { case (fruitValue, label) => ctx.item(fruitValue, label, if fruitValue == "grapes" then disabled := true else emptyMod) }))
})"""
        )
      )
    case "sidebar" =>
      val common: Modifier[HtmlElement] = Sidebar.content(
        Sidebar.group(
          Sidebar.groupLabel(span("Workspace")),
          Sidebar.groupContent(Sidebar.menu(menuRow("Overview", true), menuRow("Settings")))
        )
      )
      Seq(
        ex(
          "demo-sidebar",
          "Demo sidebar",
          sidebar(Sidebar.header(h2("Acme")), common, Sidebar.footer(Sidebar.menu(menuRow("Account")))),
          "Sidebar.provider(open)(Sidebar.root()(Sidebar.header(h2(\"Acme\")), Sidebar.content(Sidebar.menu(menuRow(\"Dashboard\")))))"
        ),
        ex(
          "header",
          "Header",
          sidebar(Sidebar.header(h2("Acme Inc.")), Sidebar.content(Sidebar.menu(menuRow("Dashboard")))),
          "Sidebar.root()(Sidebar.header(h2(\"Acme Inc.\")), Sidebar.content(Sidebar.menu(menuRow(\"Dashboard\"))))"
        ),
        ex(
          "footer",
          "Footer",
          sidebar(
            Sidebar.content(Sidebar.menu(menuRow("Dashboard"))),
            Sidebar.footer(Sidebar.menu(menuRow("Sign out")))
          ),
          "Sidebar.root()(Sidebar.content(Sidebar.menu(menuRow(\"Dashboard\"))), Sidebar.footer(Sidebar.menu(menuRow(\"Sign out\"))))"
        ),
        ex(
          "group",
          "Group",
          sidebar(
            Sidebar.group(
              Sidebar.groupLabel(span("Platform")),
              Sidebar.groupContent(Sidebar.menu(menuRow("Models"), menuRow("Datasets")))
            )
          ),
          "Sidebar.root()(Sidebar.group(Sidebar.groupLabel(span(\"Platform\")), Sidebar.groupContent(Sidebar.menu(menuRow(\"Models\")))))"
        ),
        ex(
          "group-collapsible",
          "Collapsible group",
          sidebar(
            Sidebar.group(
              Sidebar.groupLabel(span("Projects")),
              Sidebar.groupContent(Sidebar.menu(menuRow("Alpha"), menuRow("Beta")))
            )
          ),
          "Sidebar.root()(Sidebar.group(Sidebar.groupLabel(span(\"Projects\")), Sidebar.groupContent(Sidebar.menu(menuRow(\"Alpha\")))))"
        ),
        ex(
          "group-action",
          "Group action",
          sidebar(
            Sidebar.group(
              Sidebar.groupLabel(span("Projects"), Sidebar.groupAction(Icons.plus())),
              Sidebar.groupContent(Sidebar.menu(menuRow("Alpha")))
            )
          ),
          "Sidebar.group(Sidebar.groupLabel(span(\"Projects\"), Sidebar.groupAction(Icons.plus())))"
        ),
        ex(
          "menu",
          "Menu",
          sidebar(Sidebar.menu(menuRow("Home"), menuRow("Analytics"), menuRow("Reports"))),
          "Sidebar.root()(Sidebar.menu(menuRow(\"Home\"), menuRow(\"Analytics\")))"
        ),
        ex(
          "menu-action",
          "Menu action",
          sidebar(
            Sidebar.menu(
              Sidebar.menuItem(
                Sidebar.menuButton()(span("Inbox")),
                Sidebar.menuAction(showOnHover = true)(Icons.moreHorizontal())
              )
            )
          ),
          "Sidebar.menu(Sidebar.menuItem(Sidebar.menuButton()(span(\"Inbox\")), Sidebar.menuAction()(Icons.moreHorizontal())))"
        ),
        ex(
          "menu-sub",
          "Submenu",
          sidebar(
            Sidebar.menu(
              menuRow("Settings"),
              Sidebar.menuSub(
                Sidebar.menuSubItem(Sidebar.menuSubButton()(span("Profile"))),
                Sidebar.menuSubItem(Sidebar.menuSubButton(true)(span("Security")))
              )
            )
          ),
          "Sidebar.menu(menuRow(\"Settings\"), Sidebar.menuSub(Sidebar.menuSubItem(Sidebar.menuSubButton()(span(\"Profile\")))))"
        ),
        ex(
          "menu-collapsible",
          "Collapsible menu",
          sidebar(Sidebar.menu(menuRow("Documents"))),
          "Sidebar.menu(menuRow(\"Documents\"))"
        ),
        ex(
          "menu-badge",
          "Menu badge",
          sidebar(Sidebar.menu(Sidebar.menuItem(Sidebar.menuButton()(span("Inbox")), Sidebar.menuBadge(span("12"))))),
          "Sidebar.menu(Sidebar.menuItem(Sidebar.menuButton()(span(\"Inbox\")), Sidebar.menuBadge(span(\"12\"))))"
        ),
        ex(
          "controlled",
          "Controlled", {
            val state = Var(true);
            Sidebar.provider(state)(Sidebar.root(Sidebar.Collapsible.None)(Sidebar.menu(menuRow("Dashboard"))))
          },
          "val open = Var(true)\nSidebar.provider(open)(Sidebar.root()(Sidebar.menu(menuRow(\"Dashboard\"))))"
        )
      )
    case "skeleton" =>
      Seq(
        ex(
          "demo",
          "Demo",
          Skeleton(cls := "h-[20px] w-[100px] rounded-full"),
          "Skeleton(cls := \"h-[20px] w-[100px] rounded-full\")"
        ),
        ex(
          "card",
          "Card",
          div(
            cls := "flex flex-col space-y-3",
            Skeleton(cls := "h-[125px] w-[250px] rounded-xl"),
            div(cls := "space-y-2", Skeleton(cls := "h-4 w-[250px]"), Skeleton(cls := "h-4 w-[200px]"))
          ),
          """div(
  cls := "flex flex-col space-y-3",
  Skeleton(cls := "h-[125px] w-[250px] rounded-xl"),
  div(cls := "space-y-2", Skeleton(cls := "h-4 w-[250px]"), Skeleton(cls := "h-4 w-[200px]"))
)"""
        )
      )
    case "sonner" =>
      Seq(
        ex(
          "types",
          "Types",
          div(
            cls := "flex flex-wrap gap-2",
            Sonner.Toaster(),
            Button.of(
              _.variant(Button.Variant.Outline),
              _ => onClick --> Observer(_ => Sonner.toast("Event has been created")),
              _ => "Default"
            ),
            Button.of(
              _.variant(Button.Variant.Outline),
              _ => onClick --> Observer(_ => Sonner.success("Event has been created")),
              _ => "Success"
            ),
            Button.of(
              _.variant(Button.Variant.Outline),
              _ => onClick --> Observer(_ => Sonner.info("Be at the area 10 minutes before the event time")),
              _ => "Info"
            ),
            Button.of(
              _.variant(Button.Variant.Outline),
              _ => onClick --> Observer(_ => Sonner.warning("Event start time cannot be earlier than 8am")),
              _ => "Warning"
            ),
            Button.of(
              _.variant(Button.Variant.Outline),
              _ => onClick --> Observer(_ => Sonner.error("Event has not been created")),
              _ => "Error"
            ),
            Button.of(
              _.variant(Button.Variant.Outline),
              _ => onClick --> Observer(_ => Sonner.loading("Loading...")),
              _ => "Promise"
            )
          ),
          """Sonner.Toaster()
Button.of(_.variant(Button.Variant.Outline), _ => onClick --> Observer(_ => Sonner.success("Event has been created")), _ => "Success")"""
        )
      )
    case "spinner" =>
      Seq(
        ex(
          "demo",
          "Demo",
          div(
            cls := "flex w-full max-w-xs flex-col gap-4 [--radius:1rem]",
            Item.of(
              _.variant(Item.Variant.Muted),
              _ => Item.media(Item.MediaVariant.Default, Spinner()),
              _ => Item.content(Item.title("Processing payment...")),
              _ => Item.content(cls := "flex-none justify-end", span(cls := "text-sm tabular-nums", "$100.00"))
            )
          ),
          """Item.root(Item.media(Spinner()), Item.content(Item.title("Processing payment...")), Item.content(span("$100.00")))"""
        ),
        ex(
          "custom",
          "Custom",
          div(Spinner(svg.cls := "size-5 text-blue-500")),
          "Spinner(svg.cls := \"size-5 text-blue-500\")"
        ),
        ex(
          "size",
          "Size",
          div(
            cls := "flex items-center gap-6",
            Spinner(svg.cls := "size-3"),
            Spinner(svg.cls := "size-4"),
            Spinner(svg.cls := "size-6"),
            Spinner(svg.cls := "size-8")
          ),
          "div(Spinner(svg.cls := \"size-3\"), Spinner(svg.cls := \"size-4\"), Spinner(svg.cls := \"size-6\"), Spinner(svg.cls := \"size-8\"))"
        ),
        ex(
          "color",
          "Color",
          div(cls := "flex gap-3", div(Spinner(svg.cls := "text-red-500")), div(Spinner(svg.cls := "text-green-500"))),
          "div(Spinner(svg.cls := \"text-red-500\"), Spinner(svg.cls := \"text-green-500\"))"
        ),
        ex(
          "button",
          "Button",
          div(
            cls := "flex flex-col items-center gap-4",
            Button.of(
              _.variant(Button.Variant.Primary),
              _.size(Button.Size.Sm),
              _ => disabled := true,
              _ => Spinner(),
              _ => "Loading..."
            ),
            Button.of(
              _.variant(Button.Variant.Outline),
              _.size(Button.Size.Sm),
              _ => disabled := true,
              _ => Spinner(),
              _ => "Please wait"
            ),
            Button.of(
              _.variant(Button.Variant.Secondary),
              _.size(Button.Size.Sm),
              _ => disabled := true,
              _ => Spinner(),
              _ => "Processing"
            )
          ),
          """div(
  cls := "flex flex-col items-center gap-4",
  Button.of(
    _.variant(Button.Variant.Primary),
    _.size(Button.Size.Sm),
    _ => disabled := true,
    _ => Spinner(),
    _ => "Loading..."
  ),
  Button.of(
    _.variant(Button.Variant.Outline),
    _.size(Button.Size.Sm),
    _ => disabled := true,
    _ => Spinner(),
    _ => "Please wait"
  ),
  Button.of(
    _.variant(Button.Variant.Secondary),
    _.size(Button.Size.Sm),
    _ => disabled := true,
    _ => Spinner(),
    _ => "Processing"
  )
)"""
        ),
        ex(
          "badge",
          "Badge",
          Badge.of(_ => div(Spinner(svg.cls := "size-3")), _ => "Syncing"),
          "Badge.of(_ => Spinner(), _ => \"Syncing\")"
        ),
        ex(
          "input-group",
          "Input group",
          div(cls := "flex items-center gap-2", Input(placeholder := "Searching…"), Spinner()),
          "div(Input(placeholder := \"Searching…\"), Spinner())"
        ),
        ex(
          "empty",
          "Empty",
          card("No results")(Spinner(), p(cls := "mt-2 text-sm text-muted-foreground", "Loading results…")),
          "div(Spinner(), p(\"Loading results…\"))"
        ),
        ex(
          "item",
          "Item",
          div(cls := "flex items-center gap-3", Spinner(), span("Uploading file")),
          "div(Spinner(), span(\"Uploading file\"))"
        )
      )
    case "textarea" =>
      Seq(
        ex(
          "textarea-demo",
          "Textarea",
          Textarea(placeholder := "Type your message here."),
          "Textarea(placeholder := \"Type your message here.\")"
        ),
        ex(
          "disabled",
          "Disabled",
          Textarea(disabled := true, placeholder := "Type your message here."),
          "Textarea(disabled := true, placeholder := \"Type your message here.\")"
        ),
        ex(
          "with-label",
          "With label",
          div(
            cls := "grid w-full gap-1.5",
            Label(forId := "message", "Your message"),
            Textarea(placeholder := "Type your message here.", idAttr := "message")
          ),
          "div(cls := \"grid w-full gap-1.5\", Label(forId := \"message\", \"Your message\"), Textarea(placeholder := \"Type your message here.\", idAttr := \"message\"))"
        ),
        ex(
          "with-text",
          "With text",
          div(
            cls := "grid w-full gap-1.5",
            Label(forId := "message-2", "Your Message"),
            Textarea(placeholder := "Type your message here.", idAttr := "message-2"),
            p(cls := "text-sm text-muted-foreground", "Your message will be copied to the support team.")
          ),
          "div(cls := \"grid w-full gap-1.5\", Label(forId := \"message-2\", \"Your Message\"), Textarea(placeholder := \"Type your message here.\", idAttr := \"message-2\"), p(cls := \"text-sm text-muted-foreground\", \"Your message will be copied to the support team.\"))"
        ),
        ex(
          "with-button",
          "With button",
          div(cls := "grid w-full gap-2", Textarea(placeholder := "Type your message here."), Button("Send message")),
          "div(cls := \"grid w-full gap-2\", Textarea(placeholder := \"Type your message here.\"), Button(\"Send message\"))"
        )
      )
    case "toggle-group" =>
      def group(
          v: Toggle.Variant = Toggle.Variant.Default,
          s: Toggle.Size = Toggle.Size.Default,
          disabled: Boolean = false,
          spacing: Int = 0
      ): HtmlElement =
        ToggleGroup.multiple(
          Var(Set.empty[String]),
          v,
          s,
          spacing,
          ToggleGroup.Item(
            "bold",
            Icons.bold(),
            aria.label := "Toggle bold",
            if disabled then com.raquo.laminar.api.L.disabled := true else emptyMod
          ),
          ToggleGroup.Item(
            "italic",
            Icons.italic(),
            aria.label := "Toggle italic",
            if disabled then com.raquo.laminar.api.L.disabled := true else emptyMod
          ),
          ToggleGroup.Item(
            "strikethrough",
            Icons.underline(),
            aria.label := "Toggle strikethrough",
            if disabled then com.raquo.laminar.api.L.disabled := true else emptyMod
          )
        )
      Seq(
        ex(
          "outline",
          "Outline",
          group(Toggle.Variant.Outline),
          "ToggleGroup.multiple(selected, Toggle.Variant.Outline, Toggle.Size.Default, ToggleGroup.Item(\"bold\", Icons.bold(), aria.label := \"Toggle bold\"), ToggleGroup.Item(\"italic\", Icons.italic(), aria.label := \"Toggle italic\"), ToggleGroup.Item(\"strikethrough\", Icons.underline(), aria.label := \"Toggle strikethrough\"))"
        ),
        ex(
          "single",
          "Single",
          group(),
          "ToggleGroup.single(selected, Toggle.Variant.Default, Toggle.Size.Default, ToggleGroup.Item(\"bold\", \"Bold\"))"
        ),
        ex(
          "sm",
          "Small",
          group(s = Toggle.Size.Sm),
          "ToggleGroup.multiple(selected, Toggle.Variant.Default, Toggle.Size.Sm, ToggleGroup.Item(\"bold\", \"Bold\"))"
        ),
        ex(
          "lg",
          "Large",
          group(s = Toggle.Size.Lg),
          "ToggleGroup.multiple(selected, Toggle.Variant.Default, Toggle.Size.Lg, ToggleGroup.Item(\"bold\", \"Bold\"))"
        ),
        ex(
          "disabled",
          "Disabled",
          group(disabled = true),
          "ToggleGroup.multiple(selected, Toggle.Variant.Default, Toggle.Size.Default, ToggleGroup.Item(\"bold\", \"Bold\", disabled := true))"
        ),
        ex(
          "spacing",
          "Spacing",
          ToggleGroup.multiple(
            Var(Set.empty[String]),
            Toggle.Variant.Outline,
            Toggle.Size.Sm,
            2,
            ToggleGroup.Item(
              "star",
              Seq[Modifier[HtmlElement]](
                Icons.star(),
                span("Star"),
                aria.label := "Toggle star",
                cls := "data-[state=on]:bg-transparent data-[state=on]:[&>svg]:fill-yellow-500 data-[state=on]:[&>svg]:stroke-yellow-500"
              )
            ),
            ToggleGroup.Item(
              "heart",
              Seq[Modifier[HtmlElement]](
                Icons.heart(),
                span("Heart"),
                aria.label := "Toggle heart",
                cls := "data-[state=on]:bg-transparent data-[state=on]:[&>svg]:fill-red-500 data-[state=on]:[&>svg]:stroke-red-500"
              )
            ),
            ToggleGroup.Item(
              "bookmark",
              Seq[Modifier[HtmlElement]](
                Icons.bookmark(),
                span("Bookmark"),
                aria.label := "Toggle bookmark",
                cls := "data-[state=on]:bg-transparent data-[state=on]:[&>svg]:fill-blue-500 data-[state=on]:[&>svg]:stroke-blue-500"
              )
            )
          ),
          "ToggleGroup.multiple(selected, Toggle.Variant.Outline, Toggle.Size.Sm, spacing = 2, ToggleGroup.Item(\"star\", Icons.star(), span(\"Star\"), aria.label := \"Toggle star\", cls := \"data-[state=on]:bg-transparent data-[state=on]:[&>svg]:fill-yellow-500 data-[state=on]:[&>svg]:stroke-yellow-500\"))"
        )
      )
    case "toggle" =>
      def toggle(
          v: Toggle.Variant = Toggle.Variant.Outline,
          s: Toggle.Size = Toggle.Size.Sm,
          disabled: Boolean = false,
          text: Boolean = false,
          kind: String = "bookmark"
      ): HtmlElement =
        val pressed = Var(false);
        Toggle(
          pressed,
          v,
          s,
          Seq[Modifier[HtmlElement]](
            aria.label := (if text || kind == "italic" then "Toggle italic"
                           else if kind == "underline" then "Toggle underline"
                           else "Toggle bookmark"),
            if disabled then com.raquo.laminar.api.L.disabled := true else emptyMod,
            cls := "data-[state=on]:bg-transparent data-[state=on]:[&>svg]:fill-blue-500 data-[state=on]:[&>svg]:stroke-blue-500",
            if text then Icons.italic(svg.cls := "me-2 size-4")
            else if kind == "italic" then Icons.italic(svg.cls := "size-4")
            else if kind == "underline" then Icons.underline(svg.cls := "size-4")
            else Icons.bookmark(),
            if text then "Italic" else if kind == "bookmark" then "Bookmark" else emptyMod
          )*
        )
      Seq(
        ex(
          "toggle-demo",
          "Toggle",
          toggle(),
          "val pressed = Var(false)\nToggle(pressed, Toggle.Variant.Outline, Toggle.Size.Sm, aria.label := \"Toggle bookmark\", Icons.bookmark(), \"Bookmark\")"
        ),
        ex(
          "outline",
          "Outline",
          toggle(Toggle.Variant.Outline, Toggle.Size.Default, kind = "italic"),
          "Toggle(pressed, Toggle.Variant.Outline, Toggle.Size.Default, aria.label := \"Toggle italic\", Icons.italic(svg.cls := \"size-4\"))"
        ),
        ex(
          "with-text",
          "With text",
          toggle(v = Toggle.Variant.Default, s = Toggle.Size.Default, text = true),
          "Toggle(pressed, Toggle.Variant.Default, Toggle.Size.Default, span(\"Label\"))"
        ),
        ex(
          "sm",
          "Small",
          toggle(s = Toggle.Size.Sm),
          "Toggle(pressed, Toggle.Variant.Outline, Toggle.Size.Sm, aria.label := \"Toggle bookmark\", Icons.bookmark(), \"Bookmark\")"
        ),
        ex("lg", "Large", toggle(s = Toggle.Size.Lg), "Toggle(pressed, Toggle.Variant.Default, Toggle.Size.Lg)"),
        ex(
          "disabled",
          "Disabled",
          toggle(disabled = true),
          "Toggle(pressed, Toggle.Variant.Default, Toggle.Size.Default, Val(true))"
        )
      )
    case "typography" =>
      val article = articleTag(
        h1(
          cls := "scroll-m-20 text-4xl font-extrabold tracking-tight text-balance lg:text-5xl",
          "Taxing Laughter: The Joke Tax Chronicles"
        ),
        p(
          cls := "text-xl leading-7 text-muted-foreground [&:not(:first-child)]:mt-6",
          "Once upon a time, in a far-off land, there was a very lazy king who spent all day lounging on his throne. One day, his advisors came to him with a problem: the kingdom was running out of money."
        ),
        h2(
          cls := "mt-10 scroll-m-20 border-b pb-2 text-3xl font-semibold tracking-tight transition-colors first:mt-0",
          "The King's Plan"
        ),
        p(
          cls := "leading-7 [&:not(:first-child)]:mt-6",
          "The king thought long and hard, and finally came up with ",
          a(href := "##", cls := "font-medium text-primary underline underline-offset-4", "a brilliant plan"),
          ": he would tax the jokes in the kingdom."
        ),
        blockQuote(
          cls := "mt-6 border-s-2 ps-6 italic",
          "After all, he said, everyone enjoys a good joke, so it's only fair that they should pay for the privilege."
        ),
        h3(cls := "mt-8 scroll-m-20 text-2xl font-semibold tracking-tight", "The Joke Tax"),
        p(
          cls := "leading-7",
          "The king's subjects were not amused. They grumbled and complained, but the king was firm:"
        ),
        ul(
          cls := "my-6 ms-6 list-disc [&>li]:mt-2",
          li("1st level of puns: 5 gold coins"),
          li("2nd level of jokes: 10 gold coins"),
          li("3rd level of one-liners : 20 gold coins")
        ),
        p(
          cls := "leading-7 [&:not(:first-child)]:mt-6",
          "As a result, people stopped telling jokes, and the kingdom fell into a gloom. But there was one person who refused to let the king's foolishness get him down: a court jester named Jokester."
        ),
        h3(cls := "mt-8 scroll-m-20 text-2xl font-semibold tracking-tight", "Jokester's Revolt"),
        p(
          cls := "leading-7 [&:not(:first-child)]:mt-6",
          "Jokester began sneaking into the castle in the middle of the night and leaving jokes all over the place: under the king's pillow, in his soup, even in the royal toilet. The king was furious, but he couldn't seem to stop Jokester."
        ),
        p(
          cls := "leading-7 [&:not(:first-child)]:mt-6",
          "And then, one day, the people of the kingdom discovered that the jokes left by Jokester were so funny that they couldn't help but laugh. And once they started laughing, they couldn't stop."
        ),
        h3(cls := "mt-8 scroll-m-20 text-2xl font-semibold tracking-tight", "The People's Rebellion"),
        p(
          cls := "leading-7 [&:not(:first-child)]:mt-6",
          "The people of the kingdom, feeling uplifted by the laughter, started to tell jokes and puns again, and soon the entire kingdom was in on the joke."
        ),
        Table(
          Table.header(Table.row(Table.head("King's Treasury"), Table.head("People's happiness"))),
          Table.body(
            Table.row(Table.cell("Empty"), Table.cell("Overflowing")),
            Table.row(Table.cell("Modest"), Table.cell("Satisfied")),
            Table.row(Table.cell("Full"), Table.cell("Ecstatic"))
          )
        ),
        p(
          cls := "leading-7 [&:not(:first-child)]:mt-6",
          "The king, seeing how much happier his subjects were, realized the error of his ways and repealed the joke tax. Jokester was declared a hero, and the kingdom lived happily ever after."
        ),
        p(
          cls := "leading-7 [&:not(:first-child)]:mt-6",
          "The moral of the story is: never underestimate the power of a good laugh and always be careful of bad ideas."
        )
      )
      val texts = List(
        "h1" -> h1(
          cls := "scroll-m-20 text-4xl font-extrabold tracking-tight lg:text-5xl",
          "Taxing Laughter: The Joke Tax Chronicles"
        ),
        "h2" -> h2(
          cls := "scroll-m-20 border-b pb-2 text-3xl font-semibold tracking-tight transition-colors first:mt-0",
          "The People of the Kingdom"
        ),
        "h3" -> h3(cls := "scroll-m-20 text-2xl font-semibold tracking-tight", "The Joke Tax"),
        "h4" -> h4(cls := "scroll-m-20 text-xl font-semibold tracking-tight", "People stopped telling jokes"),
        "p" -> p(
          cls := "leading-7 [&:not(:first-child)]:mt-6",
          "The king, seeing how much happier his subjects were, realized the error of his ways and repealed the joke tax."
        ),
        "blockquote" -> blockQuote(cls := "mt-6 border-s-2 ps-6 italic", "After all, everyone enjoys a good joke."),
        "table" -> Table(
          Table.header(Table.row(Table.head("King's Treasury"), Table.head("People's happiness"))),
          Table.body(Table.row(Table.cell("Empty"), Table.cell("Overflowing")))
        ),
        "list" -> ul(
          cls := "my-6 ms-6 list-disc",
          li("1st level of puns: 5 gold coins"),
          li("2nd level of jokes: 10 gold coins")
        ),
        "inline-code" -> code(
          cls := "relative rounded bg-muted px-[0.3rem] py-[0.2rem] font-mono text-sm font-semibold",
          "@lucide/svelte"
        ),
        "lead" -> p(
          cls := "text-xl text-muted-foreground",
          "A modal dialog that interrupts the user with important content and expects a response."
        ),
        "large" -> p(cls := "text-lg font-semibold", "Are you sure absolutely sure?"),
        "small" -> p(cls := "text-sm leading-none font-medium", "Email address"),
        "muted" -> p(cls := "text-sm text-muted-foreground", "Enter your email address.")
      )
      texts.map((anchor, node) => ex(anchor, anchor, articleTag(cls := "prose", node), s"articleTag($anchor)"))
    case _ => Nil
