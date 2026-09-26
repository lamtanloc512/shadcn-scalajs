package shadcnscalajs.site

import com.raquo.laminar.api.L.*
import com.raquo.laminar.api.{L as Laminar}
import shadcnscalajs.ui.*

/** Examples copied from the corresponding shadcn-svelte registry examples. */
private[site] object ComponentExamplesC:
  private def ex(anchor: String, title: String, preview: HtmlElement, code: String): DocExample =
    DocExample(anchor, title, None, preview, code)

  private def otp(length: Int = 6, groups: Seq[Seq[Int]] = Seq(0 until 6)): HtmlElement =
    val value = Var("")
    val field = InputOTP.ctx(value, length)
    InputOTP.root(field)(
      groups.zipWithIndex.flatMap { case (slots, index) =>
        Seq[Modifier[HtmlElement]](InputOTP.group(slots.map(field.slot(_))*)) ++
          (if index < groups.size - 1 then Seq(InputOTP.separator()) else Nil)
      }*
    )

  private def item(title: String, description: String): HtmlElement =
    Item.of(_ => Item.content(Item.title(title), Item.description(description)))

  def apply(slug: String): Seq[DocExample] = slug match
    case "input-group" =>
      Seq(
        ex(
          "default",
          "Default",
          div(
            cls := "grid w-full max-w-sm gap-6",
            InputGroup(
              InputGroup.input(placeholder := "Search..."),
              InputGroup.addon(Icons.search()),
              InputGroup.addon(InputGroup.AddonAlign.InlineEnd, "12 results")
            )
          ),
          "div(cls := \"grid w-full max-w-sm gap-6\", InputGroup(InputGroup.input(placeholder := \"Search...\"), InputGroup.addon(Icons.search()), InputGroup.addon(InputGroup.AddonAlign.InlineEnd, \"12 results\")))"
        ),
        ex(
          "icon",
          "Icon",
          div(
            cls := "grid w-full max-w-sm gap-6",
            InputGroup(InputGroup.input(placeholder := "Search..."), InputGroup.addon(Icons.search())),
            InputGroup(
              InputGroup.input(typ := "email", placeholder := "Enter your email"),
              InputGroup.addon(icon("mail"))
            ),
            InputGroup(
              InputGroup.input(placeholder := "Card number"),
              InputGroup.addon(Icons.creditCard()),
              InputGroup.addon(InputGroup.AddonAlign.InlineEnd, Icons.check())
            ),
            InputGroup(
              InputGroup.input(placeholder := "Card number"),
              InputGroup.addon(InputGroup.AddonAlign.InlineEnd, Icons.star(), icon("info"))
            )
          ),
          "div(cls := \"grid w-full max-w-sm gap-6\", InputGroup(InputGroup.input(placeholder := \"Search...\"), InputGroup.addon(Icons.search())), InputGroup(InputGroup.input(typ := \"email\", placeholder := \"Enter your email\"), InputGroup.addon(Icons.icon(\"mail\")())), InputGroup(InputGroup.input(placeholder := \"Card number\"), InputGroup.addon(Icons.creditCard()), InputGroup.addon(InputGroup.AddonAlign.InlineEnd, Icons.check())), InputGroup(InputGroup.input(placeholder := \"Card number\"), InputGroup.addon(InputGroup.AddonAlign.InlineEnd, Icons.star(), Icons.icon(\"info\")())))"
        ),
        ex(
          "text",
          "Text",
          div(
            cls := "grid w-full max-w-sm gap-6",
            InputGroup(
              InputGroup.addon(InputGroup.text("$")),
              InputGroup.input(placeholder := "0.00"),
              InputGroup.addon(InputGroup.AddonAlign.InlineEnd, InputGroup.text("USD"))
            ),
            InputGroup(
              InputGroup.addon(InputGroup.text("https://")),
              InputGroup.input(placeholder := "example.com", cls := "!ps-0.5"),
              InputGroup.addon(InputGroup.AddonAlign.InlineEnd, InputGroup.text(".com"))
            ),
            InputGroup(
              InputGroup.input(placeholder := "Enter your username"),
              InputGroup.addon(InputGroup.AddonAlign.InlineEnd, InputGroup.text("@company.com"))
            ),
            InputGroup(
              InputGroup.textarea(placeholder := "Enter your message"),
              InputGroup.addon(
                InputGroup.AddonAlign.BlockEnd,
                InputGroup.text(cls := "text-xs text-muted-foreground", "120 characters left")
              )
            )
          ),
          "div(cls := \"grid w-full max-w-sm gap-6\", InputGroup(InputGroup.addon(InputGroup.text(\"$\")), InputGroup.input(placeholder := \"0.00\"), InputGroup.addon(InputGroup.AddonAlign.InlineEnd, InputGroup.text(\"USD\"))), InputGroup(InputGroup.addon(InputGroup.text(\"https://\")), InputGroup.input(placeholder := \"example.com\", cls := \"!ps-0.5\"), InputGroup.addon(InputGroup.AddonAlign.InlineEnd, InputGroup.text(\".com\"))), InputGroup(InputGroup.input(placeholder := \"Enter your username\"), InputGroup.addon(InputGroup.AddonAlign.InlineEnd, InputGroup.text(\"@company.com\"))), InputGroup(InputGroup.textarea(placeholder := \"Enter your message\"), InputGroup.addon(InputGroup.AddonAlign.BlockEnd, InputGroup.text(cls := \"text-xs text-muted-foreground\", \"120 characters left\"))))"
        ),
        ex(
          "button",
          "Button",
          div(
            cls := "grid w-full max-w-sm gap-6",
            InputGroup(
              InputGroup.input(readOnly := true, placeholder := "https://x.com/shadcn"),
              InputGroup.addon(
                InputGroup.AddonAlign.InlineEnd,
                InputGroup.button(InputGroup.ButtonSize.IconXs, aria.label := "Copy", title := "Copy", Icons.copy())
              )
            ),
            InputGroup(
              InputGroup.addon(
                InputGroup.button(
                  InputGroup.ButtonSize.IconXs,
                  Button.ButtonApi.variant(Button.Variant.Secondary),
                  aria.label := "Connection information",
                  Icons.circleHelp()
                )
              ),
              InputGroup.addon(cls := "ps-1.5 text-muted-foreground", InputGroup.text("https://")),
              InputGroup.input(),
              InputGroup.addon(
                InputGroup.AddonAlign.InlineEnd,
                InputGroup.button(InputGroup.ButtonSize.IconXs, aria.label := "Favorite", Icons.star())
              )
            ),
            InputGroup(
              InputGroup.input(placeholder := "Type to search..."),
              InputGroup.addon(
                InputGroup.AddonAlign.InlineEnd,
                InputGroup
                  .button(InputGroup.ButtonSize.Xs, Button.ButtonApi.variant(Button.Variant.Secondary), "Search")
              )
            )
          ),
          "div(cls := \"grid w-full max-w-sm gap-6\", InputGroup(InputGroup.input(readOnly := true, placeholder := \"https://x.com/shadcn\"), InputGroup.addon(InputGroup.AddonAlign.InlineEnd, InputGroup.button(InputGroup.ButtonSize.IconXs, aria.label := \"Copy\", title := \"Copy\", Icons.copy()))), InputGroup(InputGroup.addon(InputGroup.button(InputGroup.ButtonSize.IconXs, Button.ButtonApi.variant(Button.Variant.Secondary), aria.label := \"Connection information\", Icons.circleHelp())), InputGroup.addon(cls := \"ps-1.5 text-muted-foreground\", InputGroup.text(\"https://\")), InputGroup.input(), InputGroup.addon(InputGroup.AddonAlign.InlineEnd, InputGroup.button(InputGroup.ButtonSize.IconXs, aria.label := \"Favorite\", Icons.star()))), InputGroup(InputGroup.input(placeholder := \"Type to search...\"), InputGroup.addon(InputGroup.AddonAlign.InlineEnd, InputGroup.button(InputGroup.ButtonSize.Xs, Button.ButtonApi.variant(Button.Variant.Secondary), \"Search\"))))"
        ),
        ex(
          "textarea",
          "Textarea",
          div(
            cls := "grid w-full max-w-md gap-4",
            InputGroup(
              InputGroup.addon(
                InputGroup.AddonAlign.BlockStart,
                cls := "border-b",
                InputGroup.text(cls := "font-mono font-medium", icon("brand-javascript"), "script.js"),
                InputGroup.button(InputGroup.ButtonSize.IconXs, cls := "ms-auto", Icons.refreshCw()),
                InputGroup
                  .button(InputGroup.ButtonSize.IconXs, Button.ButtonApi.variant(Button.Variant.Ghost), Icons.copy())
              ),
              InputGroup.textarea(placeholder := "console.log('Hello, world!');", cls := "min-h-[200px]"),
              InputGroup.addon(
                InputGroup.AddonAlign.BlockEnd,
                cls := "border-t",
                InputGroup.text("Line 1, Column 1"),
                InputGroup.button(InputGroup.ButtonSize.Sm, cls := "ms-auto", "Run ", icon("corner-down-left"))
              )
            )
          ),
          "div(cls := \"grid w-full max-w-md gap-4\", InputGroup(InputGroup.addon(InputGroup.AddonAlign.BlockStart, cls := \"border-b\", InputGroup.text(cls := \"font-mono font-medium\", icon(\"brand-javascript\"), \"script.js\"), InputGroup.button(InputGroup.ButtonSize.IconXs, cls := \"ms-auto\", Icons.refreshCw()), InputGroup.button(InputGroup.ButtonSize.IconXs, Button.ButtonApi.variant(Button.Variant.Ghost), Icons.copy())), InputGroup.textarea(placeholder := \"console.log('Hello, world!');\", cls := \"min-h-[200px]\"), InputGroup.addon(InputGroup.AddonAlign.BlockEnd, cls := \"border-t\", InputGroup.text(\"Line 1, Column 1\"), InputGroup.button(InputGroup.ButtonSize.Sm, cls := \"ms-auto\", \"Run \", icon(\"corner-down-left\")))))"
        ),
        ex(
          "spinner",
          "Spinner",
          div(
            cls := "grid w-full max-w-sm gap-4",
            InputGroup(
              dataAttr("disabled") := "",
              InputGroup.input(disabled := true, placeholder := "Searching..."),
              InputGroup.addon(InputGroup.AddonAlign.InlineEnd, Spinner())
            ),
            InputGroup(
              dataAttr("disabled") := "",
              InputGroup.input(disabled := true, placeholder := "Processing..."),
              InputGroup.addon(Spinner())
            ),
            InputGroup(
              dataAttr("disabled") := "",
              InputGroup.input(disabled := true, placeholder := "Saving changes..."),
              InputGroup.addon(InputGroup.AddonAlign.InlineEnd, InputGroup.text("Saving..."), Spinner())
            ),
            InputGroup(
              dataAttr("disabled") := "",
              InputGroup.input(disabled := true, placeholder := "Refreshing data..."),
              InputGroup.addon(icon("loader", Laminar.svg.cls := "animate-spin")),
              InputGroup.addon(
                InputGroup.AddonAlign.InlineEnd,
                InputGroup.text(cls := "text-muted-foreground", "Please wait...")
              )
            )
          ),
          "div(cls := \"grid w-full max-w-sm gap-4\", InputGroup(dataAttr(\"disabled\") := \"\", InputGroup.input(disabled := true, placeholder := \"Searching...\"), InputGroup.addon(InputGroup.AddonAlign.InlineEnd, Spinner())), InputGroup(dataAttr(\"disabled\") := \"\", InputGroup.input(disabled := true, placeholder := \"Processing...\"), InputGroup.addon(Spinner())), InputGroup(dataAttr(\"disabled\") := \"\", InputGroup.input(disabled := true, placeholder := \"Saving changes...\"), InputGroup.addon(InputGroup.AddonAlign.InlineEnd, InputGroup.text(\"Saving...\"), Spinner())), InputGroup(dataAttr(\"disabled\") := \"\", InputGroup.input(disabled := true, placeholder := \"Refreshing data...\"), InputGroup.addon(Icons.icon(\"loader\")(Laminar.svg.cls := \"animate-spin\")), InputGroup.addon(InputGroup.AddonAlign.InlineEnd, InputGroup.text(cls := \"text-muted-foreground\", \"Please wait...\"))))"
        ),
        ex(
          "label",
          "Label",
          div(
            cls := "grid w-full max-w-sm gap-4",
            InputGroup(
              InputGroup.input(idAttr := "email", placeholder := "shadcn"),
              InputGroup.addon(Label(forId := "email", "@"))
            ),
            InputGroup(
              InputGroup.input(idAttr := "email-2", placeholder := "shadcn@vercel.com"),
              InputGroup.addon(
                InputGroup.AddonAlign.BlockStart,
                Label(forId := "email-2", cls := "text-foreground", "Email"),
                InputGroup.button(
                  InputGroup.ButtonSize.IconXs,
                  cls := "ms-auto rounded-full",
                  aria.label := "Help",
                  Icons.circleHelp()
                )
              )
            )
          ),
          "div(cls := \"grid w-full max-w-sm gap-4\", InputGroup(InputGroup.input(idAttr := \"email\", placeholder := \"shadcn\"), InputGroup.addon(Label(forId := \"email\", \"@\"))), InputGroup(InputGroup.input(idAttr := \"email-2\", placeholder := \"shadcn@vercel.com\"), InputGroup.addon(InputGroup.AddonAlign.BlockStart, Label(forId := \"email-2\", cls := \"text-foreground\", \"Email\"), InputGroup.button(InputGroup.ButtonSize.IconXs, cls := \"ms-auto rounded-full\", aria.label := \"Help\", Icons.circleHelp()))))"
        ),
        ex(
          "custom-input",
          "Custom Input",
          div(
            cls := "grid w-full max-w-sm gap-6",
            InputGroup.textarea(
              dataAttr("slot") := "input-group-control",
              cls := "flex field-sizing-content min-h-16 w-full resize-none rounded-md bg-transparent px-3 py-2.5 text-base transition-[color,box-shadow] outline-none md:text-sm",
              placeholder := "Autoresize textarea...",
              InputGroup.addon(
                InputGroup.AddonAlign.BlockEnd,
                InputGroup.button(InputGroup.ButtonSize.Sm, cls := "ms-auto", "Submit")
              )
            )
          ),
          "InputGroup.textarea(dataAttr(\"slot\") := \"input-group-control\", cls := \"flex field-sizing-content min-h-16 w-full resize-none rounded-md bg-transparent px-3 py-2.5 text-base transition-[color,box-shadow] outline-none md:text-sm\", placeholder := \"Autoresize textarea...\", InputGroup.addon(InputGroup.AddonAlign.BlockEnd, InputGroup.button(InputGroup.ButtonSize.Sm, cls := \"ms-auto\", \"Submit\")))"
        ),
        ex(
          "dropdown",
          "Dropdown",
          div(
            cls := "grid w-full max-w-sm gap-4",
            InputGroup(
              InputGroup.input(placeholder := "Enter file name"),
              InputGroup.addon(
                InputGroup.AddonAlign.InlineEnd,
                InputGroup.button(InputGroup.ButtonSize.IconXs, aria.label := "More", Icons.moreHorizontal())
              )
            ),
            InputGroup(
              InputGroup.input(placeholder := "Enter search query"),
              InputGroup.addon(
                InputGroup.AddonAlign.InlineEnd,
                Button.of(
                  _.variant(Button.Variant.Ghost),
                  _.size(Button.Size.Xs),
                  _ => "Search In...",
                  _ => Icons.chevronDown(Laminar.svg.cls := "size-3")
                )
              )
            )
          ),
          "div(cls := \"grid w-full max-w-sm gap-4\", InputGroup(InputGroup.input(placeholder := \"Enter file name\"), InputGroup.addon(InputGroup.AddonAlign.InlineEnd, InputGroup.button(InputGroup.ButtonSize.IconXs, aria.label := \"More\", Icons.moreHorizontal()))), InputGroup(InputGroup.input(placeholder := \"Enter search query\"), InputGroup.addon(InputGroup.AddonAlign.InlineEnd, Button.of(_.variant(Button.Variant.Ghost), _.size(Button.Size.Xs), _ => \"Search In...\", _ => Icons.chevronDown(Laminar.svg.cls := \"size-3\")))))"
        ),
        ex(
          "button-group",
          "Button Group",
          div(
            cls := "grid w-full max-w-sm gap-6",
            ButtonGroup(
              InputGroup.text(Label(forId := "url", "https://")),
              InputGroup(InputGroup.input(idAttr := "url"), InputGroup.addon(Icons.arrowLeftRight())),
              InputGroup.text(".com")
            )
          ),
          "ButtonGroup(InputGroup.text(Label(forId := \"url\", \"https://\")), InputGroup(InputGroup.input(idAttr := \"url\"), InputGroup.addon(Icons.arrowLeftRight())), InputGroup.text(\".com\"))"
        )
      )
    case "input-otp" =>
      val controlled = Var("")
      val field = InputOTP.ctx(controlled, 6)
      Seq(
        ex(
          "pattern",
          "Pattern",
          otp(),
          "InputOTP.root(InputOTP.ctx(Var(\"\"), 6))(InputOTP.group((0 until 6).map(_.slot)*))"
        ),
        ex(
          "separator",
          "Separator",
          otp(6, Seq(0 until 2, 2 until 4, 4 until 6)),
          "InputOTP.root(InputOTP.ctx(Var(\"\"), 6))(InputOTP.group((0 until 2).map(_.slot)*), InputOTP.separator(), InputOTP.group((2 until 4).map(_.slot)*), InputOTP.separator(), InputOTP.group((4 until 6).map(_.slot)*))"
        ),
        ex(
          "controlled",
          "Controlled",
          div(
            cls := "space-y-2",
            InputOTP.root(field)(InputOTP.group((0 until 6).map(field.slot(_))*)),
            p(
              cls := "text-center text-sm",
              text <-- controlled.signal.map(v =>
                if v.isEmpty then "Enter your one-time password." else s"You entered: $v"
              )
            )
          ),
          "val value = Var(\"\")\nval field = InputOTP.ctx(value, 6)\nInputOTP.root(field)(InputOTP.group((0 until 6).map(field.slot(_))*))"
        )
      )
    case "input" =>
      Seq(
        ex(
          "input-demo",
          "Default",
          Input(cls := "max-w-xs", typ := "email", placeholder := "Email"),
          "Input(typ := \"email\", placeholder := \"Email\", cls := \"max-w-xs\")"
        ),
        ex(
          "file",
          "File",
          div(
            cls := "grid w-full max-w-sm items-center gap-1.5",
            Label(forId := "picture", "Picture"),
            Input(idAttr := "picture", typ := "file")
          ),
          "div(cls := \"grid w-full max-w-sm items-center gap-1.5\", Label(forId := \"picture\", \"Picture\"), Input(idAttr := \"picture\", typ := \"file\"))"
        ),
        ex(
          "disabled",
          "Disabled",
          Input(disabled := true, typ := "email", placeholder := "Email", cls := "max-w-sm"),
          "Input(disabled := true, typ := \"email\", placeholder := \"Email\", cls := \"max-w-sm\")"
        ),
        ex(
          "with-label",
          "With Label",
          div(
            cls := "flex w-full max-w-sm flex-col gap-1.5",
            Label(forId := "email-field", "Email"),
            Input(typ := "email", idAttr := "email-field", placeholder := "Email")
          ),
          "div(cls := \"flex w-full max-w-sm flex-col gap-1.5\", Label(forId := \"email-field\", \"Email\"), Input(typ := \"email\", idAttr := \"email-field\", placeholder := \"Email\"))"
        ),
        ex(
          "with-button",
          "With Button",
          div(
            cls := "flex w-full max-w-sm items-center gap-2",
            Input(typ := "email", placeholder := "Email"),
            Button.of(_.variant(Button.Variant.Outline), _ => "Subscribe")
          ),
          "div(cls := \"flex w-full max-w-sm items-center gap-2\", Input(typ := \"email\", placeholder := \"Email\"), Button.of(_.variant(Button.Variant.Outline), _ => \"Subscribe\"))"
        )
      )
    case "item" =>
      Seq(
        ex(
          "variants",
          "Variants",
          div(
            cls := "flex flex-col gap-6",
            Item.of(
              _ =>
                Item.content(
                  Item.title("Default Variant"),
                  Item.description("Standard styling with subtle background and borders.")
                ),
              _ => Item.actions(Button.of(_.variant(Button.Variant.Outline), _.size(Button.Size.Sm), _ => "Open"))
            ),
            Item.of(
              _.variant(Item.Variant.Outline),
              _ =>
                Item.content(
                  Item.title("Outline Variant"),
                  Item.description("Outlined style with clear borders and transparent background.")
                ),
              _ => Item.actions(Button.of(_.variant(Button.Variant.Outline), _.size(Button.Size.Sm), _ => "Open"))
            ),
            Item.of(
              _.variant(Item.Variant.Muted),
              _ =>
                Item.content(
                  Item.title("Muted Variant"),
                  Item.description("Subdued appearance with muted colors for secondary content.")
                ),
              _ => Item.actions(Button.of(_.variant(Button.Variant.Outline), _.size(Button.Size.Sm), _ => "Open"))
            )
          ),
          "div(cls := \"flex flex-col gap-6\", Item.of(_ => Item.content(Item.title(\"Default Variant\"), Item.description(\"Standard styling with subtle background and borders.\")), _ => Item.actions(Button.of(_.variant(Button.Variant.Outline), _.size(Button.Size.Sm), _ => \"Open\")), Item.of(_.variant(Item.Variant.Outline), _ => Item.content(Item.title(\"Outline Variant\"), Item.description(\"Outlined style with clear borders and transparent background.\")), _ => Item.actions(Button.of(_.variant(Button.Variant.Outline), _.size(Button.Size.Sm), _ => \"Open\")), Item.of(_.variant(Item.Variant.Muted), _ => Item.content(Item.title(\"Muted Variant\"), Item.description(\"Subdued appearance with muted colors for secondary content.\")), _ => Item.actions(Button.of(_.variant(Button.Variant.Outline), _.size(Button.Size.Sm), _ => \"Open\")))"
        ),
        ex(
          "size",
          "Size",
          div(
            cls := "flex w-full max-w-md flex-col gap-6",
            Item.of(
              _.variant(Item.Variant.Outline),
              _ =>
                Item.content(Item.title("Basic Item"), Item.description("A simple item with title and description.")),
              _ => Item.actions(Button.of(_.variant(Button.Variant.Outline), _.size(Button.Size.Sm), _ => "Action"))
            ),
            Item.of(
              _.variant(Item.Variant.Outline),
              _.size(Item.Size.Sm),
              _ => Item.media(Item.MediaVariant.Icon, Icons.badgeCheck(Laminar.svg.cls := "size-5")),
              _ => Item.content(Item.title("Your profile has been verified.")),
              _ => Item.actions(Icons.chevronRight(Laminar.svg.cls := "size-4"))
            )
          ),
          "div(cls := \"flex w-full max-w-md flex-col gap-6\", Item.of(_.variant(Item.Variant.Outline), _ => Item.content(Item.title(\"Basic Item\"), Item.description(\"A simple item with title and description.\")), _ => Item.actions(Button.of(_.variant(Button.Variant.Outline), _.size(Button.Size.Sm), _ => \"Action\")), Item.of(_.variant(Item.Variant.Outline), _.size(Item.Size.Sm), _ => Item.media(Item.MediaVariant.Icon, Icons.badgeCheck(Laminar.svg.cls := \"size-5\")), _ => Item.content(Item.title(\"Your profile has been verified.\")), _ => Item.actions(Icons.chevronRight(Laminar.svg.cls := \"size-4\")))"
        ),
        ex(
          "icon",
          "Icon",
          Item.of(
            _.variant(Item.Variant.Outline),
            _ => Item.media(Item.MediaVariant.Icon, icon("shield-alert")),
            _ =>
              Item.content(Item.title("Security Alert"), Item.description("New login detected from unknown device.")),
            _ => Item.actions(Button.of(_.variant(Button.Variant.Outline), _.size(Button.Size.Sm), _ => "Review"))
          ),
          "Item.of(_.variant(Item.Variant.Outline), _ => Item.media(Item.MediaVariant.Icon, Icons.icon(\"shield-alert\")()), _ => Item.content(Item.title(\"Security Alert\"), Item.description(\"New login detected from unknown device.\")), _ => Item.actions(Button.of(_.variant(Button.Variant.Outline), _.size(Button.Size.Sm), _ => \"Review\")))"
        ),
        ex(
          "avatar",
          "Avatar",
          Item.of(
            _.variant(Item.Variant.Outline),
            _ =>
              Item.media(
                Item.MediaVariant.Default,
                Avatar(Avatar.image("https://github.com/evilrabbit.png", "Evil Rabbit"))
              ),
            _ => Item.content(Item.title("Evil Rabbit"), Item.description("Last seen 5 months ago")),
            _ =>
              Item.actions(
                Button.of(
                  _.variant(Button.Variant.Outline),
                  _.size(Button.Size.Icon),
                  _ => Seq[Modifier[HtmlElement]](cls := "rounded-full", aria.label := "Invite", Icons.plus())
                )
              )
          ),
          "Item.of(_.variant(Item.Variant.Outline), _ => Item.media(Item.MediaVariant.Default, Avatar(Avatar.image(\"https://github.com/evilrabbit.png\", \"Evil Rabbit\"))), _ => Item.content(Item.title(\"Evil Rabbit\"), Item.description(\"Last seen 5 months ago\")), _ => Item.actions(Button.of(_.variant(Button.Variant.Outline), _.size(Button.Size.Icon), _ => Seq[Modifier[HtmlElement]](cls := \"rounded-full\", aria.label := \"Invite\", Icons.plus()))))"
        ),
        ex(
          "image",
          "Image",
          div(
            cls := "flex w-full max-w-md flex-col gap-4",
            Item.of(
              _.variant(Item.Variant.Outline),
              _ =>
                Item.media(
                  Item.MediaVariant.Image,
                  img(
                    src := "https://avatar.vercel.sh/Midnight City Lights",
                    alt := "Midnight City Lights",
                    cls := "size-8 rounded object-cover grayscale"
                  )
                ),
              _ =>
                Item.content(
                  Item.title("Midnight City Lights - ", span(cls := "text-muted-foreground", "Electric Nights")),
                  Item.description("Neon Dreams")
                ),
              _ => Item.content(cls := "flex-none text-center", Item.description("3:45"))
            ),
            Item.of(
              _.variant(Item.Variant.Outline),
              _ =>
                Item.media(
                  Item.MediaVariant.Image,
                  img(
                    src := "https://avatar.vercel.sh/Coffee Shop Conversations",
                    alt := "Coffee Shop Conversations",
                    cls := "size-8 rounded object-cover grayscale"
                  )
                ),
              _ =>
                Item.content(
                  Item.title("Coffee Shop Conversations - ", span(cls := "text-muted-foreground", "Urban Stories")),
                  Item.description("The Morning Brew")
                ),
              _ => Item.content(cls := "flex-none text-center", Item.description("4:05"))
            ),
            Item.of(
              _.variant(Item.Variant.Outline),
              _ =>
                Item.media(
                  Item.MediaVariant.Image,
                  img(
                    src := "https://avatar.vercel.sh/Digital Rain",
                    alt := "Digital Rain",
                    cls := "size-8 rounded object-cover grayscale"
                  )
                ),
              _ =>
                Item.content(
                  Item.title("Digital Rain - ", span(cls := "text-muted-foreground", "Binary Beats")),
                  Item.description("Cyber Symphony")
                ),
              _ => Item.content(cls := "flex-none text-center", Item.description("3:30"))
            )
          ),
          "div(cls := \"flex w-full max-w-md flex-col gap-4\", Item.of(_.variant(Item.Variant.Outline), _ => Item.media(Item.MediaVariant.Image, img(src := \"https://avatar.vercel.sh/Midnight City Lights\", alt := \"Midnight City Lights\", cls := \"size-8 rounded object-cover grayscale\")), _ => Item.content(Item.title(\"Midnight City Lights - \", span(cls := \"text-muted-foreground\", \"Electric Nights\")), Item.description(\"Neon Dreams\")), _ => Item.content(cls := \"flex-none text-center\", Item.description(\"3:45\"))), Item.of(_.variant(Item.Variant.Outline), _ => Item.media(Item.MediaVariant.Image, img(src := \"https://avatar.vercel.sh/Coffee Shop Conversations\", alt := \"Coffee Shop Conversations\", cls := \"size-8 rounded object-cover grayscale\")), _ => Item.content(Item.title(\"Coffee Shop Conversations - \", span(cls := \"text-muted-foreground\", \"Urban Stories\")), Item.description(\"The Morning Brew\")), _ => Item.content(cls := \"flex-none text-center\", Item.description(\"4:05\"))), Item.of(_.variant(Item.Variant.Outline), _ => Item.media(Item.MediaVariant.Image, img(src := \"https://avatar.vercel.sh/Digital Rain\", alt := \"Digital Rain\", cls := \"size-8 rounded object-cover grayscale\")), _ => Item.content(Item.title(\"Digital Rain - \", span(cls := \"text-muted-foreground\", \"Binary Beats\")), Item.description(\"Cyber Symphony\")), _ => Item.content(cls := \"flex-none text-center\", Item.description(\"3:30\"))))"
        ),
        ex(
          "group",
          "Group",
          Item.group(
            item("shadcn", "shadcn@vercel.com"),
            Item.separator(),
            item("maxleiter", "maxleiter@vercel.com"),
            Item.separator(),
            item("evilrabbit", "evilrabbit@vercel.com")
          ),
          "Item.group(item(\"shadcn\", \"shadcn@vercel.com\"), Item.separator(), item(\"maxleiter\", \"maxleiter@vercel.com\"), Item.separator(), item(\"evilrabbit\", \"evilrabbit@vercel.com\"))"
        ),
        ex(
          "header",
          "Header",
          Item.group(
            cls := "grid grid-cols-3 gap-4",
            Item.of(
              _.variant(Item.Variant.Outline),
              _ =>
                Item.header(
                  img(
                    src := "https://images.unsplash.com/photo-1650804068570-7fb2e3dbf888?q=80&w=640&auto=format&fit=crop",
                    alt := "v0-1.5-sm",
                    widthAttr := 128,
                    heightAttr := 128,
                    cls := "aspect-square w-full rounded-sm object-cover"
                  )
                ),
              _ => Item.content(Item.title("v0-1.5-sm"), Item.description("Everyday tasks and UI generation."))
            )
          ),
          "Item.group(cls := \"grid grid-cols-3 gap-4\", Item.of(_.variant(Item.Variant.Outline), _ => Item.header(img(src := \"https://images.unsplash.com/photo-1650804068570-7fb2e3dbf888?q=80&w=640&auto=format&fit=crop\", alt := \"v0-1.5-sm\", widthAttr := 128, heightAttr := 128, cls := \"aspect-square w-full rounded-sm object-cover\")), _ => Item.content(Item.title(\"v0-1.5-sm\"), Item.description(\"Everyday tasks and UI generation.\"))))"
        ),
        ex(
          "link",
          "Link",
          Item.of(
            _ =>
              Item.content(
                Item.title("Visit our documentation"),
                Item.description("Learn how to get started with our components.")
              ),
            _ => Item.actions(Icons.chevronRight(Laminar.svg.cls := "size-4"))
          ),
          "Item.of(_ => Item.content(Item.title(\"Visit our documentation\"), Item.description(\"Learn how to get started with our components.\")), _ => Item.actions(Icons.chevronRight(Laminar.svg.cls := \"size-4\")))"
        )
      )
    case "kbd" =>
      Seq(
        ex(
          "default",
          "Default",
          div(
            cls := "flex flex-col items-center gap-4",
            Kbd.group(Kbd("⌘"), Kbd("⇧"), Kbd("⌥"), Kbd("⌃")),
            Kbd.group(Kbd("Ctrl"), span("+"), Kbd("B"))
          ),
          "div(cls := \"flex flex-col items-center gap-4\", Kbd.group(Kbd(\"⌘\"), Kbd(\"⇧\"), Kbd(\"⌥\"), Kbd(\"⌃\")), Kbd.group(Kbd(\"Ctrl\"), span(\"+\"), Kbd(\"B\")))"
        ),
        ex(
          "group",
          "Group",
          div(
            cls := "flex flex-col items-center gap-4",
            p(
              cls := "text-sm text-muted-foreground",
              "Use ",
              Kbd.group(Kbd("Ctrl + B"), Kbd("Ctrl + K")),
              " to open the command palette"
            )
          ),
          "div(cls := \"flex flex-col items-center gap-4\", Kbd.group(Kbd(\"Ctrl + B\"), Kbd(\"Ctrl + K\")))"
        ),
        ex(
          "button",
          "Button",
          div(
            cls := "flex flex-wrap items-center gap-4",
            Button.of(
              _.variant(Button.Variant.Outline),
              _.size(Button.Size.Sm),
              _ => Seq[Modifier[HtmlElement]](cls := "pe-2", "Accept ", Kbd("⏎"))
            ),
            Button.of(
              _.variant(Button.Variant.Outline),
              _.size(Button.Size.Sm),
              _ => Seq[Modifier[HtmlElement]](cls := "pe-2", "Cancel ", Kbd("Esc"))
            )
          ),
          "Button.of(_.variant(Button.Variant.Outline), _.size(Button.Size.Sm), _ => Seq(\"Accept \", Kbd(\"⏎\")))"
        ),
        ex(
          "input-group",
          "Input Group",
          div(
            cls := "flex w-full max-w-xs flex-col gap-6",
            InputGroup(
              InputGroup.input(placeholder := "Search..."),
              InputGroup.addon(Icons.search()),
              InputGroup.addon(InputGroup.AddonAlign.InlineEnd, Kbd("⌘"), Kbd("K"))
            )
          ),
          "InputGroup(InputGroup.input(placeholder := \"Search...\"), InputGroup.addon(Icons.search()), InputGroup.addon(InputGroup.AddonAlign.InlineEnd, Kbd(\"⌘\"), Kbd(\"K\")))"
        )
      )
    case "native-select" =>
      Seq(
        ex(
          "default",
          "Default",
          NativeSelect(
            NativeSelect.option(value := "", "Select status"),
            NativeSelect.option(value := "todo", "Todo"),
            NativeSelect.option(value := "in-progress", "In Progress"),
            NativeSelect.option(value := "done", "Done"),
            NativeSelect.option(value := "cancelled", "Cancelled")
          ),
          "NativeSelect(NativeSelect.option(value := \"\", \"Select status\"), NativeSelect.option(value := \"todo\", \"Todo\"), NativeSelect.option(value := \"in-progress\", \"In Progress\"), NativeSelect.option(value := \"done\", \"Done\"), NativeSelect.option(value := \"cancelled\", \"Cancelled\"))"
        ),
        ex(
          "groups",
          "With Groups",
          NativeSelect(
            NativeSelect.option(value := "", "Select department"),
            NativeSelect.optGroup(
              labelAttr := "Engineering",
              NativeSelect.option(value := "frontend", "Frontend"),
              NativeSelect.option(value := "backend", "Backend"),
              NativeSelect.option(value := "devops", "DevOps")
            ),
            NativeSelect.optGroup(
              labelAttr := "Sales",
              NativeSelect.option(value := "sales-rep", "Sales Rep"),
              NativeSelect.option(value := "account-manager", "Account Manager"),
              NativeSelect.option(value := "sales-director", "Sales Director")
            ),
            NativeSelect.optGroup(
              labelAttr := "Operations",
              NativeSelect.option(value := "support", "Customer Support"),
              NativeSelect.option(value := "product-manager", "Product Manager"),
              NativeSelect.option(value := "ops-manager", "Operations Manager")
            )
          ),
          "NativeSelect(NativeSelect.option(value := \"\", \"Select department\"), NativeSelect.optGroup(labelAttr := \"Engineering\", NativeSelect.option(value := \"frontend\", \"Frontend\"), NativeSelect.option(value := \"backend\", \"Backend\"), NativeSelect.option(value := \"devops\", \"DevOps\")), NativeSelect.optGroup(labelAttr := \"Sales\", NativeSelect.option(value := \"sales-rep\", \"Sales Rep\"), NativeSelect.option(value := \"account-manager\", \"Account Manager\"), NativeSelect.option(value := \"sales-director\", \"Sales Director\")), NativeSelect.optGroup(labelAttr := \"Operations\", NativeSelect.option(value := \"support\", \"Customer Support\"), NativeSelect.option(value := \"product-manager\", \"Product Manager\"), NativeSelect.option(value := \"ops-manager\", \"Operations Manager\")))"
        ),
        ex(
          "disabled",
          "Disabled State",
          NativeSelect(
            disabled := true,
            NativeSelect.option(value := "", "Select priority"),
            NativeSelect.option(value := "low", "Low"),
            NativeSelect.option(value := "medium", "Medium"),
            NativeSelect.option(value := "high", "High"),
            NativeSelect.option(value := "critical", "Critical")
          ),
          "NativeSelect(disabled := true, NativeSelect.option(value := \"\", \"Select priority\"), NativeSelect.option(value := \"low\", \"Low\"), NativeSelect.option(value := \"medium\", \"Medium\"), NativeSelect.option(value := \"high\", \"High\"), NativeSelect.option(value := \"critical\", \"Critical\"))"
        ),
        ex(
          "invalid",
          "Invalid State",
          NativeSelect(
            aria.invalid := "true",
            NativeSelect.option(value := "", "Select role"),
            NativeSelect.option(value := "admin", "Admin"),
            NativeSelect.option(value := "editor", "Editor"),
            NativeSelect.option(value := "viewer", "Viewer"),
            NativeSelect.option(value := "guest", "Guest")
          ),
          "NativeSelect(aria.invalid := \"true\", NativeSelect.option(value := \"\", \"Select role\"), NativeSelect.option(value := \"admin\", \"Admin\"), NativeSelect.option(value := \"editor\", \"Editor\"), NativeSelect.option(value := \"viewer\", \"Viewer\"), NativeSelect.option(value := \"guest\", \"Guest\"))"
        )
      )
    case _ => Nil

  private def icon(name: String, mods: Modifier[SvgElement]*): SvgElement = Icons.icon(name)(mods*)
