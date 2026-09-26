package shadcnscalajs.site

import com.raquo.laminar.api.L.*
import org.scalajs.dom
import shadcnscalajs.ui.*

/** Focused examples for chart, combobox, command, dialog, drawer, dropdown-menu, empty, and field. */
private[site] object ComponentExamplesB:
  private def ex(anchor: String, title: String, preview: HtmlElement, code: String): DocExample =
    DocExample(anchor, title, None, preview, code)

  private val chartData = List(
    ("January", 186.0),
    ("February", 305.0),
    ("March", 237.0),
    ("April", 73.0),
    ("May", 209.0),
    ("June", 214.0)
  )
  private val mobileData = List(
    ("January", 80.0),
    ("February", 200.0),
    ("March", 120.0),
    ("April", 190.0),
    ("May", 130.0),
    ("June", 140.0)
  )

  private def chart(
      axisTick: Boolean = false,
      tooltip: Boolean = false,
      legend: Boolean = false,
      indicator: Chart.TooltipIndicator = Chart.TooltipIndicator.Dot
  ): HtmlElement =
    val hover = Chart.hoverVar()
    val desktop = Chart.bar(
      chartData,
      hover,
      Chart.BarStyle(color = "#2563eb", padding = 0.25, labelFormat = if axisTick then _.take(3) else identity)
    )
    val mobile = Chart.bar(
      mobileData,
      hover,
      Chart.BarStyle(color = "#60a5fa", padding = 0.25, labelFormat = if axisTick then _.take(3) else identity)
    )
    Chart(
      cls := "min-h-[200px] w-full",
      div(cls := "relative h-56 w-full", desktop, mobile),
      if tooltip then Chart.tooltip(hover, indicator = indicator) else emptyMod,
      if legend then div(cls := "flex justify-center gap-4 text-sm", span("Desktop"), span("Mobile")) else emptyMod
    )

  private val frameworks = Seq(
    Combobox.Item("sveltekit", "SvelteKit"),
    Combobox.Item("next.js", "Next.js"),
    Combobox.Item("nuxt.js", "Nuxt.js"),
    Combobox.Item("remix", "Remix"),
    Combobox.Item("astro", "Astro")
  )

  private def combo(): HtmlElement =
    Combobox(
      Var(Option.empty[String]),
      frameworks,
      placeholder = "Select a framework...",
      searchPlaceholder = "Search framework...",
      emptyText = "No framework found.",
      cls := "w-[200px]"
    )

  private def commandDialog(): HtmlElement =
    val open = Var(false)
    div(
      p(
        cls := "text-sm text-muted-foreground",
        "Press ",
        kbd(
          cls := "pointer-events-none inline-flex h-5 items-center gap-1 rounded border bg-muted px-1.5 font-mono text-[10px] font-medium text-muted-foreground opacity-100 select-none",
          span(cls := "text-xs", "⌘"),
          "J"
        )
      ),
      Button
        .of(_.variant(Button.Variant.Outline), _ => onClick --> { _ => open.update(!_) }, _ => "Open command palette"),
      Command.dialog(
        open,
        Command.input(placeholder := "Type a command or search..."),
        Command.list(
          Command.empty("No results found."),
          Command.group(
            "Suggestions",
            Command.item(span(Icons.calendar(svg.cls := "me-2 size-4"), "Calendar")),
            Command.item(span(Icons.circleHelp(svg.cls := "me-2 size-4"), "Search Emoji")),
            Command.item(span(Icons.fileBarChart(svg.cls := "me-2 size-4"), "Calculator"))
          ),
          Command.separator(),
          Command.group(
            "Settings",
            Command.item(span(Icons.user(svg.cls := "me-2 size-4"), "Profile"), Command.shortcut("⌘P")),
            Command.item(span(Icons.creditCard(svg.cls := "me-2 size-4"), "Billing"), Command.shortcut("⌘B")),
            Command.item(span(Icons.settings2(svg.cls := "me-2 size-4"), "Settings"), Command.shortcut("⌘S"))
          )
        )
      )
    )

  private def dialogClose(): HtmlElement =
    val open = Var(false)
    div(
      Button.of(_.variant(Button.Variant.Outline), _ => onClick --> { _ => open.set(true) }, _ => "Share"),
      Dialog(open, "gap-6 p-6 sm:max-w-md")(
        Dialog.header(
          Dialog.title("Share link"),
          Dialog.description("Anyone who has this link will be able to view this.")
        ),
        div(
          cls := "flex items-center gap-2",
          div(
            cls := "grid flex-1 gap-2",
            label(cls := "sr-only", "Link"),
            Input(idAttr := "link", value := "https://shadcn-svelte.com/docs/installation")
          )
        ),
        Dialog.footer(
          cls := "sm:justify-start",
          Dialog.close(open, Button.ButtonApi.variant(Button.Variant.Secondary), "Close")
        )
      )
    )

  private def drawerDirection(): HtmlElement =
    div(
      cls := "flex flex-wrap gap-2",
      Seq(
        Drawer.Direction.Top,
        Drawer.Direction.Right,
        Drawer.Direction.Bottom,
        Drawer.Direction.Left
      ).map { side =>
        val open = Var(false)
        div(
          Button.of(
            _.variant(Button.Variant.Outline),
            _ => cls := "capitalize",
            _ => onClick --> { _ => open.set(true) },
            _ => side.toString.toLowerCase
          ),
          Drawer(open, side)(
            Drawer.header(Drawer.title("Move Goal"), Drawer.description("Set your daily activity goal.")),
            div(
              cls := "no-scrollbar overflow-y-auto px-4",
              (1 to 10).map(_ =>
                p(
                  cls := "mb-4 leading-normal",
                  "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."
                )
              )
            ),
            Drawer.footer(
              Button.of(_.variant(Button.Variant.Primary), _ => "Submit"),
              Drawer.close(
                onClick --> { _ => open.set(false) },
                Button.ButtonApi.variant(Button.Variant.Outline),
                "Cancel"
              )
            )
          )
        )
      }
    )

  private def drawerDialog(): HtmlElement =
    val open = Var(false)
    div(
      Button.of(_.variant(Button.Variant.Outline), _ => onClick --> { _ => open.set(true) }, _ => "Edit Profile"),
      Drawer(open)(
        Drawer.header(
          cls := "text-start",
          Drawer.title("Edit profile"),
          Drawer.description("Make changes to your profile here. Click save when you're done.")
        ),
        form(
          cls := "grid items-start gap-4 px-4",
          div(cls := "grid gap-2", label("Email"), Input(typ := "email", value := "shadcn@example.com")),
          div(cls := "grid gap-2", label("Username"), Input(value := "@shadcn")),
          Button.of(_.variant(Button.Variant.Primary), _ => typ := "submit", _ => "Save changes")
        ),
        Drawer.footer(cls := "pt-2", Drawer.close(Button.ButtonApi.variant(Button.Variant.Outline), "Cancel"))
      )
    )

  private def menu(body: Menu.Ctx => Seq[Modifier[HtmlElement]]): HtmlElement =
    DropdownMenu.itemsWithTrigger(DropdownMenu.outlineTrigger)("Open")(body)

  private def dropdownDialog(): HtmlElement =
    val newOpen = Var(false)
    val shareOpen = Var(false)
    div(
      DropdownMenu.itemsWithTrigger(Button.appearance(Button.Variant.Outline, Button.Size.IconSm))(
        Icons.moreHorizontal()
      ) { ctx =>
        Seq(
          DropdownMenu.label("File Actions"),
          ctx.group(
            ctx.item(() => newOpen.set(true), "New File..."),
            ctx.item(() => shareOpen.set(true), "Share..."),
            ctx.item(aria.disabled := true, "Download")
          )
        )
      },
      Dialog(newOpen, "sm:max-w-[425px]")(
        Dialog.header(
          Dialog.title("Create New File"),
          Dialog.description("Provide a name for your new file. Click create when you're done.")
        ),
        Field.group(
          cls := "pb-3",
          Field(
            Field.label("File Name"),
            Input(idAttr := "filename", nameAttr := "filename", placeholder := "document.txt")
          )
        ),
        Dialog.footer(
          Dialog.close(newOpen, Button.ButtonApi.variant(Button.Variant.Outline), "Cancel"),
          Button.of(_.variant(Button.Variant.Primary), _ => "Create")
        )
      ),
      Dialog(shareOpen, "sm:max-w-[425px]")(
        Dialog.header(
          Dialog.title("Share File"),
          Dialog.description("Anyone with the link will be able to view this file.")
        ),
        Field.group(
          cls := "py-3",
          Field(
            Field.label("Email Address"),
            Input(idAttr := "email", nameAttr := "email", typ := "email", placeholder := "shadcn@vercel.com")
          ),
          Field(
            Field.label("Message (Optional)"),
            Textarea(idAttr := "message", nameAttr := "message", placeholder := "Check out this file")
          )
        ),
        Dialog.footer(
          Dialog.close(shareOpen, Button.ButtonApi.variant(Button.Variant.Outline), "Cancel"),
          Button.of(_.variant(Button.Variant.Primary), _ => "Send Invite")
        )
      )
    )

  private def emptyRoot(
      title: String,
      description: String,
      media: HtmlElement,
      content: HtmlElement,
      mods: Modifier[HtmlElement]*
  ): HtmlElement =
    Empty(
      (mods ++ Seq(
        Empty.header(Empty.media(Empty.MediaVariant.Icon, media), Empty.title(title), Empty.description(description)),
        Empty.content(content)
      ))*
    )

  private def fieldInput(): HtmlElement =
    div(
      cls := "w-full max-w-md",
      Field.set(
        Field.group(
          Field(
            Field.label("Username", forId := "username"),
            Input(idAttr := "username", typ := "text", placeholder := "Max Leiter"),
            Field.description("Choose a unique username for your account.")
          ),
          Field(
            Field.label("Password", forId := "password"),
            Field.description("Must be at least 8 characters long."),
            Input(idAttr := "password", typ := "password", placeholder := "••••••••")
          )
        )
      )
    )

  private def field(labelText: String, control: HtmlElement, description: String): HtmlElement =
    Field(Field.label(labelText), control, if description.nonEmpty then Field.description(description) else emptyMod)

  def apply(slug: String): Seq[DocExample] = slug match
    case "chart" =>
      Seq(
        ex(
          "chart-bar-demo",
          "Bar Chart",
          chart(),
          "val chartData = List((\"January\", 186.0), (\"February\", 305.0), (\"March\", 237.0), (\"April\", 73.0), (\"May\", 209.0), (\"June\", 214.0))\nChart(cls := \"min-h-[200px] w-full\", Chart.bar(chartData, Chart.BarStyle(color = \"#2563eb\")))"
        ),
        ex(
          "axis-tick",
          "Axis Tick",
          chart(axisTick = true),
          "Chart(Chart.bar(chartData, Chart.BarStyle(labelFormat = _.take(3))))"
        ),
        ex(
          "tooltip",
          "Tooltip",
          chart(tooltip = true),
          "val hover = Chart.hoverVar()\nChart(Chart.bar(chartData, hover), Chart.tooltip(hover))"
        ),
        ex(
          "legend",
          "Legend",
          chart(legend = true, tooltip = true),
          "Chart(Chart.bar(chartData, hover), Chart.tooltip(hover), legend)"
        ),
        ex(
          "chart-tooltip",
          "Custom Tooltip",
          chart(tooltip = true, indicator = Chart.TooltipIndicator.Line),
          "val hover = Chart.hoverVar()\nChart(Chart.bar(chartData, hover), Chart.tooltip(hover, indicator = Chart.TooltipIndicator.Line))"
        )
      )
    case "combobox" =>
      Seq(
        ex(
          "combobox-demo",
          "Combobox",
          combo(),
          "val frameworks = Seq(Combobox.Item(\"sveltekit\", \"SvelteKit\"), Combobox.Item(\"next.js\", \"Next.js\"), Combobox.Item(\"nuxt.js\", \"Nuxt.js\"), Combobox.Item(\"remix\", \"Remix\"), Combobox.Item(\"astro\", \"Astro\"))\nCombobox(Var(Option.empty[String]), frameworks, placeholder = \"Select a framework...\", searchPlaceholder = \"Search framework...\", emptyText = \"No framework found.\", cls := \"w-[200px]\")"
        ),
        ex(
          "popover",
          "Popover",
          Popover(
            Popover.trigger(Button.of(_.variant(Button.Variant.Outline), _ => "Open popover")),
            Popover.content(
              Popover
                .header(Popover.title("About"), Popover.description("A popover is an anchored, interactive panel."))
            )
          ),
          "Popover(Popover.trigger(Button.of(_.variant(Button.Variant.Outline), _ => \"Open popover\")), Popover.content(Popover.header(Popover.title(\"About\"), Popover.description(\"A popover is an anchored, interactive panel.\"))))"
        ),
        ex(
          "dropdown-menu",
          "Dropdown Menu",
          menu(ctx => Seq(ctx.item(() => (), "Profile"), ctx.item(() => (), "Settings"))),
          "DropdownMenu.itemsWithTrigger(DropdownMenu.outlineTrigger)(\"Open\")(ctx => Seq(ctx.item(() => (), \"Profile\"), ctx.item(() => (), \"Settings\")))"
        ),
        ex(
          "responsive",
          "Responsive",
          Field(Field.Orientation.Responsive, Field.label("Workspace"), combo()),
          "Field(Field.Orientation.Responsive, Field.label(\"Workspace\"), combo())"
        )
      )
    case "command" =>
      Seq(
        ex(
          "command-dialog",
          "Dialog",
          commandDialog(),
          "val open = Var(false)\nCommand.dialog(open, Command.input(placeholder := \"Type a command or search...\"), Command.list(Command.empty(\"No results found.\"), Command.group(\"Suggestions\", Command.item(span(Icons.calendar(svg.cls := \"me-2 size-4\"), \"Calendar\")), Command.item(span(Icons.circleHelp(svg.cls := \"me-2 size-4\"), \"Search Emoji\")), Command.item(span(Icons.fileBarChart(svg.cls := \"me-2 size-4\"), \"Calculator\"))), Command.separator(), Command.group(\"Settings\", Command.item(span(Icons.user(svg.cls := \"me-2 size-4\"), \"Profile\"), Command.shortcut(\"⌘P\")), Command.item(span(Icons.creditCard(svg.cls := \"me-2 size-4\"), \"Billing\"), Command.shortcut(\"⌘B\")), Command.item(span(Icons.settings2(svg.cls := \"me-2 size-4\"), \"Settings\"), Command.shortcut(\"⌘S\")))))"
        )
      )
    case "dialog" =>
      Seq(
        ex(
          "dialog-close-button",
          "Close Button",
          dialogClose(),
          "val open = Var(false)\nDialog(open, \"gap-6 p-6 sm:max-w-md\")(Dialog.header(Dialog.title(\"Share link\"), Dialog.description(\"Anyone who has this link will be able to view this.\")), Dialog.footer(cls := \"sm:justify-start\", Dialog.close(open, Button.ButtonApi.variant(Button.Variant.Secondary), \"Close\")))"
        )
      )
    case "drawer" =>
      Seq(
        ex(
          "drawer-direction",
          "Direction",
          drawerDirection(),
          "Seq(Drawer.Direction.Top, Drawer.Direction.Right, Drawer.Direction.Bottom, Drawer.Direction.Left).map { side =>\n  val open = Var(false)\n  Drawer(open, side)(Drawer.header(Drawer.title(\"Move Goal\"), Drawer.description(\"Set your daily activity goal.\")))\n}"
        ),
        ex(
          "drawer-dialog",
          "Dialog",
          drawerDialog(),
          "val open = Var(false)\nDrawer(open)(Drawer.header(Drawer.title(\"Edit profile\"), Drawer.description(\"Make changes to your profile here. Click save when you're done.\")), Button.of(_.variant(Button.Variant.Primary), _ => typ := \"submit\", _ => \"Save changes\"))"
        )
      )
    case "dropdown-menu" =>
      Seq(
        ex(
          "checkboxes",
          "Checkboxes", {
            val status = Var(true);
            DropdownMenu.itemsWithTrigger(DropdownMenu.outlineTrigger)("Open") { ctx =>
              Seq(
                ctx.label("Appearance"),
                ctx.separator(),
                ctx.checkboxItem(status, "Status Bar"),
                ctx.checkboxItem(Var(false), "Activity Bar", aria.disabled := true),
                ctx.checkboxItem(Var(false), "Panel")
              )
            }
          },
          "val showStatusBar = Var(true)\nval showActivityBar = Var(false)\nval showPanel = Var(false)\nDropdownMenu.itemsWithTrigger(DropdownMenu.outlineTrigger)(\"Open\")(ctx => Seq(ctx.label(\"Appearance\"), ctx.separator(), ctx.checkboxItem(showStatusBar, \"Status Bar\"), ctx.checkboxItem(showActivityBar, aria.disabled := true, \"Activity Bar\"), ctx.checkboxItem(showPanel, \"Panel\")))"
        ),
        ex(
          "radio-group",
          "Radio Group", {
            val position = Var("bottom");
            DropdownMenu.itemsWithTrigger(DropdownMenu.outlineTrigger)("Open") { ctx =>
              Seq(
                ctx.label("Panel Position"),
                ctx.separator(),
                ctx.radioItem(position, "Top"),
                ctx.radioItem(position, "Bottom"),
                ctx.radioItem(position, "Right")
              )
            }
          },
          "val position = Var(\"bottom\")\nDropdownMenu.itemsWithTrigger(DropdownMenu.outlineTrigger)(\"Open\")(ctx => Seq(ctx.label(\"Panel Position\"), ctx.separator(), ctx.radioItem(position, \"Top\"), ctx.radioItem(position, \"Bottom\"), ctx.radioItem(position, \"Right\")))"
        ),
        ex(
          "dialog",
          "Dialog",
          dropdownDialog(),
          "val showNewDialog = Var(false)\nval showShareDialog = Var(false)\n// DropdownMenu items open the corresponding Dialog; the preview contains both file-action dialogs."
        )
      )
    case "empty" =>
      Seq(
        ex(
          "outline",
          "Outline",
          emptyRoot(
            "Cloud Storage Empty",
            "Upload files to your cloud storage to access them anywhere.",
            div(Icons.cloud()),
            Button.of(_.variant(Button.Variant.Outline), _.size(Button.Size.Sm), _ => "Upload Files"),
            cls := "border border-dashed"
          ),
          "Empty(cls := \"border border-dashed\")(Empty.header(Empty.media(Empty.MediaVariant.Icon, Icons.cloud()), Empty.title(\"Cloud Storage Empty\"), Empty.description(\"Upload files to your cloud storage to access them anywhere.\")), Empty.content(Button.of(_.variant(Button.Variant.Outline), _.size(Button.Size.Sm), _ => \"Upload Files\")))"
        ),
        ex(
          "background",
          "Background",
          Empty(cls := "h-full bg-gradient-to-b from-muted/50 from-30% to-background")(
            Empty.header(
              Empty.media(Empty.MediaVariant.Icon, Icons.bell()),
              Empty.title("No Notifications"),
              Empty.description("You're all caught up. New notifications will appear here.")
            ),
            Empty.content(
              Button
                .of(_.variant(Button.Variant.Outline), _.size(Button.Size.Sm), _ => Icons.refreshCw(), _ => "Refresh")
            )
          ),
          "Empty(cls := \"h-full bg-gradient-to-b from-muted/50 from-30% to-background\")(Empty.header(Empty.media(Empty.MediaVariant.Icon, Icons.bell()), Empty.title(\"No Notifications\"), Empty.description(\"You're all caught up. New notifications will appear here.\")), Empty.content(Button.of(_.variant(Button.Variant.Outline), _.size(Button.Size.Sm), _ => Icons.refreshCw(), _ => \"Refresh\")))"
        ),
        ex(
          "avatar",
          "Avatar",
          emptyRoot(
            "User Offline",
            "This user is currently offline. You can leave a message to notify them or try again later.",
            Avatar(
              Avatar.Size.Default,
              Avatar.image("https://github.com/shadcn.png", "", cls := "grayscale"),
              Avatar.fallback("LR")
            ),
            Button.of(_.size(Button.Size.Sm), _ => "Leave Message")
          ),
          "Empty(Empty.header(Empty.media(Empty.MediaVariant.Default, Avatar(Avatar.image(\"https://github.com/shadcn.png\", cls := \"grayscale\"), Avatar.fallback(\"LR\"))), Empty.title(\"User Offline\"), Empty.description(\"This user is currently offline. You can leave a message to notify them or try again later.\")), Empty.content(Button.of(_.size(Button.Size.Sm), _ => \"Leave Message\")))"
        ),
        ex(
          "avatar-group",
          "Avatar Group",
          Empty(cls := "flex-none border")(
            Empty.header(
              Empty.media(
                Empty.MediaVariant.Default,
                Avatar.group(
                  cls := "grayscale",
                  Avatar(Avatar.image("https://github.com/shadcn.png", "@shadcn"), Avatar.fallback("CN")),
                  Avatar(Avatar.image("https://github.com/maxleiter.png", "@maxleiter"), Avatar.fallback("LR")),
                  Avatar(Avatar.image("https://github.com/evilrabbit.png", "@evilrabbit"), Avatar.fallback("ER"))
                )
              ),
              Empty.title("No Team Members"),
              Empty.description("Invite your team to collaborate on this project.")
            ),
            Empty.content(Button.of(_.size(Button.Size.Sm), _ => Icons.plus(), _ => "Invite Members"))
          ),
          "Empty(cls := \"flex-none border\")(Empty.header(Empty.media(Avatar.group(Avatar(Avatar.image(\"https://github.com/shadcn.png\"), Avatar.fallback(\"CN\")), Avatar(Avatar.image(\"https://github.com/maxleiter.png\"), Avatar.fallback(\"LR\")), Avatar(Avatar.image(\"https://github.com/evilrabbit.png\"), Avatar.fallback(\"ER\")))), Empty.title(\"No Team Members\"), Empty.description(\"Invite your team to collaborate on this project.\")), Empty.content(Button.of(_.size(Button.Size.Sm), _ => Icons.plus(), _ => \"Invite Members\")))"
        ),
        ex(
          "input-group",
          "Input Group",
          Empty()(
            Empty.header(
              Empty.title("404 - Not Found"),
              Empty.description("The page you're looking for doesn't exist. Try searching for what you need below.")
            ),
            Empty.content(
              InputGroup(
                InputGroup.input(placeholder := "Try searching for pages..."),
                InputGroup.addon(InputGroup.AddonAlign.InlineStart, Icons.search()),
                InputGroup.addon(InputGroup.AddonAlign.InlineEnd, Kbd("/")),
                Empty.description("Need help? "),
                a(href := "#/", "Contact support")
              )
            )
          ),
          "Empty(Empty.header(Empty.title(\"404 - Not Found\"), Empty.description(\"The page you're looking for doesn't exist. Try searching for what you need below.\")), Empty.content(InputGroup(InputGroup.input(placeholder := \"Try searching for pages...\"), InputGroup.addon(InputGroup.AddonAlign.InlineStart, Icons.search()), InputGroup.addon(InputGroup.AddonAlign.InlineEnd, Kbd(\"/\")), Empty.description(\"Need help? \"), a(href := \"#/\", \"Contact support\"))))"
        )
      )
    case "field" =>
      Seq(
        ex(
          "input",
          "Input",
          fieldInput(),
          "div(cls := \"w-full max-w-md\", Field.set(Field.group(Field(Field.label(\"Username\", forId := \"username\"), Input(idAttr := \"username\", typ := \"text\", placeholder := \"Max Leiter\"), Field.description(\"Choose a unique username for your account.\")), Field(Field.label(\"Password\", forId := \"password\"), Field.description(\"Must be at least 8 characters long.\"), Input(idAttr := \"password\", typ := \"password\", placeholder := \"••••••••\")))))"
        ),
        ex(
          "textarea",
          "Textarea",
          div(
            cls := "w-full max-w-md",
            Field.set(
              Field.group(
                field(
                  "Feedback",
                  Textarea(idAttr := "feedback", placeholder := "Your feedback helps us improve...", rows := 4),
                  "Share your thoughts about our service."
                )
              )
            )
          ),
          "div(cls := \"w-full max-w-md\", Field.set(Field.group(Field(Field.label(\"Feedback\"), Textarea(idAttr := \"feedback\", placeholder := \"Your feedback helps us improve...\", rows := 4), Field.description(\"Share your thoughts about our service.\")))))"
        ),
        ex(
          "select",
          "Select",
          div(
            cls := "w-full max-w-md",
            Field(
              Field.label("Department"),
              Select.stateful(
                Var(""),
                List(
                  "engineering" -> "Engineering",
                  "design" -> "Design",
                  "marketing" -> "Marketing",
                  "sales" -> "Sales",
                  "support" -> "Customer Support",
                  "hr" -> "Human Resources",
                  "finance" -> "Finance",
                  "operations" -> "Operations"
                )
              ),
              Field.description("Select your department or area of work.")
            )
          ),
          "val department = Var(\"\")\nField(Field.label(\"Department\"), Select.stateful(department, List(\"engineering\" -> \"Engineering\", \"design\" -> \"Design\", \"marketing\" -> \"Marketing\", \"sales\" -> \"Sales\", \"support\" -> \"Customer Support\", \"hr\" -> \"Human Resources\", \"finance\" -> \"Finance\", \"operations\" -> \"Operations\")), Field.description(\"Select your department or area of work.\"))"
        ),
        ex(
          "slider",
          "Slider",
          Field(
            Field.label("Price Range"),
            Field.description("Set your budget range ($200 - $800)."),
            Slider.multiple(Var(List(200.0, 800.0)), 0.0, 1000.0, 10.0, cls := "mt-2 w-full")
          ),
          "val value = Var(List(200.0, 800.0))\nField(Field.label(\"Price Range\"), Field.description(\"Set your budget range ($200 - $800).\"), Slider.multiple(value, 0.0, 1000.0, 10.0, cls := \"mt-2 w-full\"))"
        ),
        ex(
          "field-set",
          "Fieldset",
          div(
            cls := "w-full max-w-md space-y-6",
            Field.set(
              Field.legend("Address Information"),
              Field.description("We need your address to deliver your order."),
              Field.group(
                field("Street Address", Input(placeholder := "123 Main St"), ""),
                div(
                  cls := "grid grid-cols-2 gap-4",
                  field("City", Input(placeholder := "New York"), ""),
                  field("Postal Code", Input(placeholder := "90502"), "")
                )
              )
            )
          ),
          "Field.set(Field.legend(\"Address Information\"), Field.description(\"We need your address to deliver your order.\"), Field.group(Field(Field.label(\"Street Address\"), Input(placeholder := \"123 Main St\")), div(cls := \"grid grid-cols-2 gap-4\", Field(Field.label(\"City\"), Input(placeholder := \"New York\")), Field(Field.label(\"Postal Code\"), Input(placeholder := \"90502\")))))"
        ),
        ex(
          "checkbox",
          "Checkbox",
          div(
            cls := "w-full max-w-md",
            Field.group(
              Field.set(
                Field.legend(Field.LegendVariant.Label, "Show these items on the desktop"),
                Field.description("Select the items you want to show on the desktop."),
                Field.group(
                  cls := "gap-3",
                  Field(
                    Field.Orientation.Horizontal,
                    Checkbox(idAttr := "finder-pref-9k2-hard-disks-ljj", checked := true),
                    Field.label("Hard disks", cls := "font-normal")
                  ),
                  Field(
                    Field.Orientation.Horizontal,
                    Checkbox(idAttr := "finder-pref-9k2-external-disks-1yg"),
                    Field.label("External disks", cls := "font-normal")
                  ),
                  Field(
                    Field.Orientation.Horizontal,
                    Checkbox(idAttr := "finder-pref-9k2-cds-dvds-fzt"),
                    Field.label("CDs, DVDs, and iPods", cls := "font-normal")
                  ),
                  Field(
                    Field.Orientation.Horizontal,
                    Checkbox(idAttr := "finder-pref-9k2-connected-servers-6l2"),
                    Field.label("Connected servers", cls := "font-normal")
                  )
                )
              )
            ),
            Field.separator()(),
            Field(
              Field.Orientation.Horizontal,
              Checkbox(idAttr := "finder-pref-9k2-sync-folders-nep", checked := true),
              Field.content(
                Field.label("Sync Desktop & Documents folders"),
                Field.description(
                  "Your Desktop & Documents folders are being synced with iCloud Drive. You can access them from other devices."
                )
              )
            )
          ),
          "div(cls := \"w-full max-w-md\", Field.group(Field.set(Field.legend(Field.LegendVariant.Label, \"Show these items on the desktop\"), Field.description(\"Select the items you want to show on the desktop.\"), Field.group(Field(Field.Orientation.Horizontal, Checkbox(idAttr := \"hard-disks\", checked := true), Field.label(\"Hard disks\")), Field(Field.Orientation.Horizontal, Checkbox(idAttr := \"external-disks\"), Field.label(\"External disks\")), Field(Field.Orientation.Horizontal, Checkbox(idAttr := \"cds-dvds\"), Field.label(\"CDs, DVDs, and iPods\")), Field(Field.Orientation.Horizontal, Checkbox(idAttr := \"connected-servers\"), Field.label(\"Connected servers\"))))), Field.separator(), Field(Field.Orientation.Horizontal, Checkbox(idAttr := \"sync-folders\", checked := true), Field.content(Field.label(\"Sync Desktop & Documents folders\"), Field.description(\"Your Desktop & Documents folders are being synced with iCloud Drive. You can access them from other devices.\"))))"
        ),
        ex(
          "radio",
          "Radio",
          Field.set(
            Field.label("Subscription Plan"),
            Field.description("Yearly and lifetime plans offer significant savings."),
            Field.group(
              Field(Field.Orientation.Horizontal, Radio("plan"), Field.label("Monthly ($9.99/month)")),
              Field(Field.Orientation.Horizontal, Radio("plan"), Field.label("Yearly ($99.99/year)")),
              Field(Field.Orientation.Horizontal, Radio("plan"), Field.label("Lifetime ($299.99)"))
            )
          ),
          "Field.set(Field.label(\"Subscription Plan\"), Field.description(\"Yearly and lifetime plans offer significant savings.\"), Field.group(Field(Field.Orientation.Horizontal, Radio(\"plan\"), Field.label(\"Monthly ($9.99/month)\")), Field(Field.Orientation.Horizontal, Radio(\"plan\"), Field.label(\"Yearly ($99.99/year)\")), Field(Field.Orientation.Horizontal, Radio(\"plan\"), Field.label(\"Lifetime ($299.99)\"))))"
        ),
        ex(
          "switch",
          "Switch",
          Field(
            Field.Orientation.Horizontal,
            Field.content(
              Field.label("Multi-factor authentication"),
              Field.description(
                "Enable multi-factor authentication. If you do not have a two-factor device, you can use a one-time code sent to your email."
              )
            ),
            Switch(idAttr := "2fa")
          ),
          "Field(Field.Orientation.Horizontal, Field.content(Field.label(\"Multi-factor authentication\"), Field.description(\"Enable multi-factor authentication. If you do not have a two-factor device, you can use a one-time code sent to your email.\")), Switch(idAttr := \"2fa\"))"
        ),
        ex(
          "field-group",
          "Field Group",
          Field.group(
            Field.set(
              Field.label("Responses"),
              Field.description(
                "Get notified when ChatGPT responds to requests that take time, like research or image generation."
              )
            ),
            Field.separator()(),
            Field.set(Field.label("Tasks"), Field.description("Get notified when tasks you've created have updates."))
          ),
          "Field.group(Field.set(Field.label(\"Responses\"), Field.description(\"Get notified when ChatGPT responds to requests that take time, like research or image generation.\")), Field.separator()(), Field.set(Field.label(\"Tasks\"), Field.description(\"Get notified when tasks you've created have updates.\")))"
        ),
        ex(
          "responsive-layout",
          "Responsive Layout",
          Field.set(
            Field.legend("Profile"),
            Field.description("Fill in your profile information."),
            Field.separator()(),
            Field.group(
              Field(
                Field.Orientation.Responsive,
                Field.content(Field.label("Name"), Field.description("Provide your full name for identification")),
                Input(placeholder := "Evil Rabbit")
              ),
              Field.separator()(),
              Field(
                Field.Orientation.Responsive,
                Button.of(_.variant(Button.Variant.Primary), _ => "Submit"),
                Button.of(_.variant(Button.Variant.Outline), _ => "Cancel")
              )
            )
          ),
          "Field.set(Field.legend(\"Profile\"), Field.description(\"Fill in your profile information.\"), Field.group(Field(Field.Orientation.Responsive, Field.content(Field.label(\"Name\"), Field.description(\"Provide your full name for identification\")), Input(placeholder := \"Evil Rabbit\")), Field.separator()(), Field(Field.Orientation.Responsive, Button.of(_.variant(Button.Variant.Primary), _ => \"Submit\"), Button.of(_.variant(Button.Variant.Outline), _ => \"Cancel\"))))"
        )
      )
    case _ => Nil
