package shadcnscalajs.site

import com.raquo.laminar.api.L.*
import com.raquo.laminar.api.L.svg.{path as svgPath, svg as svgTag}
import shadcnscalajs.ui.*

import scala.scalajs.js

/** Focused recipes from the upstream component pages (the page's first demo is rendered by Main). */
private[site] object ComponentExamplesA:
  private def ex(
      anchor: String,
      title: String,
      preview: HtmlElement,
      code: String,
      description: Option[String] = None
  ) =
    DocExample(anchor, title, description, preview, code)

  def apply(slug: String): Seq[DocExample] = slug match
    case "breadcrumb" =>
      Seq(
        ex(
          "breadcrumb-separator",
          "Custom separator",
          breadcrumbSeparator(),
          "Breadcrumb(Breadcrumb.list(Breadcrumb.item(Breadcrumb.link(\"/\", \"Home\")), Breadcrumb.separator(inlineIcon(\"M5 12h14\")), Breadcrumb.item(Breadcrumb.link(\"/components\", \"Components\")), Breadcrumb.separator(inlineIcon(\"M5 12h14\")), Breadcrumb.item(Breadcrumb.page(\"Breadcrumb\"))))"
        ),
        ex(
          "breadcrumb-dropdown",
          "Dropdown",
          breadcrumbDropdown(),
          "Breadcrumb(Breadcrumb.list(Breadcrumb.item(Breadcrumb.link(\"/\", \"Home\")), Breadcrumb.separator(inlineIcon(\"M5 12h14\")), Breadcrumb.item(DropdownMenu.itemsWithTrigger(Button.appearance(Button.Variant.Ghost))(span(\"Components\"), Icons.chevronDown())(ctx => Seq(ctx.item(\"Documentation\"), ctx.item(\"Themes\"), ctx.item(\"GitHub\")))), Breadcrumb.separator(inlineIcon(\"M5 12h14\")), Breadcrumb.item(Breadcrumb.page(\"Breadcrumb\"))))"
        ),
        ex(
          "breadcrumb-ellipsis",
          "Collapsed",
          breadcrumbDemo(),
          "Breadcrumb(Breadcrumb.list(Breadcrumb.item(Breadcrumb.link(\"/\", \"Home\")), Breadcrumb.separator(), Breadcrumb.item(Breadcrumb.ellipsis()), Breadcrumb.separator(), Breadcrumb.item(Breadcrumb.link(\"/docs/components\", \"Components\")), Breadcrumb.separator(), Breadcrumb.item(Breadcrumb.page(\"Breadcrumb\"))))"
        ),
        ex(
          "breadcrumb-link",
          "Link component",
          Breadcrumb(
            Breadcrumb.list(
              Breadcrumb.item(Breadcrumb.link("/", "Home")),
              Breadcrumb.separator(),
              Breadcrumb.item(Breadcrumb.link("/components", "Components")),
              Breadcrumb.separator(),
              Breadcrumb.item(Breadcrumb.page("Breadcrumb"))
            )
          ),
          "Breadcrumb(Breadcrumb.list(Breadcrumb.item(Breadcrumb.link(\"/\", \"Home\")), Breadcrumb.separator(), Breadcrumb.item(Breadcrumb.link(\"/components\", \"Components\")), Breadcrumb.separator(), Breadcrumb.item(Breadcrumb.page(\"Breadcrumb\"))))"
        ),
        ex(
          "breadcrumb-responsive",
          "Responsive",
          breadcrumbResponsive(),
          "val items = Seq((\"Home\", \"/\"), (\"Components\", \"/docs/components\"), (\"Breadcrumb\", \"\"))\nBreadcrumb(Breadcrumb.list(Breadcrumb.item(Breadcrumb.link(items.head._2, items.head._1)), Breadcrumb.separator(), Breadcrumb.item(Breadcrumb.ellipsis()), Breadcrumb.separator(), Breadcrumb.item(Breadcrumb.link(items(1)._2, items(1)._1)), Breadcrumb.separator(), Breadcrumb.item(Breadcrumb.page(items(2)._1))))"
        )
      )
    case "button-group" =>
      Seq(
        ex(
          "orientation",
          "Orientation",
          ButtonGroup(
            ButtonGroup.Orientation.Vertical,
            aria.label := "Media controls",
            cls := "h-fit",
            Button(Button.appearance(Button.Variant.Outline, Button.Size.Icon), Icons.plus()),
            Button(Button.appearance(Button.Variant.Outline, Button.Size.Icon), Icons.minus())
          ),
          "ButtonGroup(ButtonGroup.Orientation.Vertical, aria.label := \"Media controls\", cls := \"h-fit\", Button(Button.appearance(Button.Variant.Outline, Button.Size.Icon), Icons.plus()), Button(Button.appearance(Button.Variant.Outline, Button.Size.Icon), Icons.minus()))"
        ),
        ex(
          "size",
          "Size",
          div(
            cls := "flex flex-col items-start gap-8",
            ButtonGroup(
              Button(Button.appearance(Button.Variant.Outline, Button.Size.Sm), "Small"),
              Button(Button.appearance(Button.Variant.Outline, Button.Size.Sm), "Button"),
              Button(Button.appearance(Button.Variant.Outline, Button.Size.Sm), "Group"),
              Button(Button.appearance(Button.Variant.Outline, Button.Size.IconSm), Icons.plus())
            ),
            ButtonGroup(
              Button(Button.appearance(Button.Variant.Outline), "Default"),
              Button(Button.appearance(Button.Variant.Outline), "Button"),
              Button(Button.appearance(Button.Variant.Outline), "Group"),
              Button(Button.appearance(Button.Variant.Outline, Button.Size.Icon), Icons.plus())
            ),
            ButtonGroup(
              Button(Button.appearance(Button.Variant.Outline, Button.Size.Lg), "Large"),
              Button(Button.appearance(Button.Variant.Outline, Button.Size.Lg), "Button"),
              Button(Button.appearance(Button.Variant.Outline, Button.Size.Lg), "Group"),
              Button(Button.appearance(Button.Variant.Outline, Button.Size.IconLg), Icons.plus())
            )
          ),
          "div(cls := \"flex flex-col items-start gap-8\", ButtonGroup(Button(Button.appearance(Button.Variant.Outline, Button.Size.Sm), \"Small\"), Button(Button.appearance(Button.Variant.Outline, Button.Size.Sm), \"Button\"), Button(Button.appearance(Button.Variant.Outline, Button.Size.Sm), \"Group\"), Button(Button.appearance(Button.Variant.Outline, Button.Size.IconSm), Icons.plus())), ButtonGroup(Button(Button.appearance(Button.Variant.Outline), \"Default\"), Button(Button.appearance(Button.Variant.Outline), \"Button\"), Button(Button.appearance(Button.Variant.Outline), \"Group\"), Button(Button.appearance(Button.Variant.Outline, Button.Size.Icon), Icons.plus())), ButtonGroup(Button(Button.appearance(Button.Variant.Outline, Button.Size.Lg), \"Large\"), Button(Button.appearance(Button.Variant.Outline, Button.Size.Lg), \"Button\"), Button(Button.appearance(Button.Variant.Outline, Button.Size.Lg), \"Group\"), Button(Button.appearance(Button.Variant.Outline, Button.Size.IconLg), Icons.plus())))"
        ),
        ex(
          "nested",
          "Nested",
          ButtonGroup(
            ButtonGroup(
              Button(Button.appearance(Button.Variant.Outline, Button.Size.Sm), "1"),
              Button(Button.appearance(Button.Variant.Outline, Button.Size.Sm), "2"),
              Button(Button.appearance(Button.Variant.Outline, Button.Size.Sm), "3"),
              Button(Button.appearance(Button.Variant.Outline, Button.Size.Sm), "4"),
              Button(Button.appearance(Button.Variant.Outline, Button.Size.Sm), "5")
            ),
            ButtonGroup(
              Button(
                Button.appearance(Button.Variant.Outline, Button.Size.IconSm),
                aria.label := "Previous",
                inlineIcon("m12 19-7-7 7-7")
              ),
              Button(
                Button.appearance(Button.Variant.Outline, Button.Size.IconSm),
                aria.label := "Next",
                inlineIcon("m12 5 7 7-7 7")
              )
            )
          ),
          "ButtonGroup(ButtonGroup(Button(Button.appearance(Button.Variant.Outline, Button.Size.Sm), \"1\"), Button(Button.appearance(Button.Variant.Outline, Button.Size.Sm), \"2\"), Button(Button.appearance(Button.Variant.Outline, Button.Size.Sm), \"3\"), Button(Button.appearance(Button.Variant.Outline, Button.Size.Sm), \"4\"), Button(Button.appearance(Button.Variant.Outline, Button.Size.Sm), \"5\")), ButtonGroup(Button(Button.appearance(Button.Variant.Outline, Button.Size.IconSm), aria.label := \"Previous\", Icons.arrowRight()), Button(Button.appearance(Button.Variant.Outline, Button.Size.IconSm), aria.label := \"Next\", Icons.arrowRight())))"
        ),
        ex(
          "separator",
          "Separator",
          ButtonGroup(
            Button(Button.appearance(Button.Variant.Secondary, Button.Size.Sm), "Copy"),
            ButtonGroup.separator(),
            Button(Button.appearance(Button.Variant.Secondary, Button.Size.Sm), "Paste")
          ),
          "ButtonGroup(Button(Button.appearance(Button.Variant.Secondary, Button.Size.Sm), \"Copy\"), ButtonGroup.separator(), Button(Button.appearance(Button.Variant.Secondary, Button.Size.Sm), \"Paste\"))"
        ),
        ex(
          "split",
          "Split",
          ButtonGroup(
            Button(Button.appearance(Button.Variant.Secondary), "Button"),
            ButtonGroup.separator(),
            Button(Button.appearance(Button.Variant.Secondary, Button.Size.Icon), Icons.plus())
          ),
          "ButtonGroup(Button(Button.appearance(Button.Variant.Secondary), \"Button\"), ButtonGroup.separator(), Button(Button.appearance(Button.Variant.Secondary, Button.Size.Icon), Icons.plus()))"
        ),
        ex(
          "input",
          "Input",
          ButtonGroup(
            Input(placeholder := "Search..."),
            Button(Button.appearance(Button.Variant.Outline, Button.Size.Icon), aria.label := "Search", Icons.search())
          ),
          "ButtonGroup(Input(placeholder := \"Search...\"), Button(Button.appearance(Button.Variant.Outline, Button.Size.Icon), aria.label := \"Search\", Icons.search()))"
        ),
        ex(
          "input-group",
          "Input Group",
          inputGroupDemo(),
          "val voiceEnabled = Var(false)\nButtonGroup(cls := \"[--radius:9999rem]\", ButtonGroup(Button(Button.appearance(Button.Variant.Outline, Button.Size.Icon), Icons.plus())), ButtonGroup(cls := \"flex-1\", InputGroup(InputGroup.input(placeholder <-- voiceEnabled.signal.map(v => if v then \"Record and send audio...\" else \"Send a message...\")), InputGroup.addon(InputGroup.AddonAlign.InlineEnd, InputGroup.button(InputGroup.ButtonSize.IconXs, onClick --> (_ => voiceEnabled.update(!_)), aria.pressed <-- voiceEnabled.signal.map(_.toString), Icons.audioLines())))))"
        ),
        ex(
          "dropdown-menu",
          "Dropdown Menu",
          dropdownGroup(),
          "ButtonGroup(Button(Button.appearance(Button.Variant.Outline), \"Follow\"), DropdownMenu.itemsWithTrigger(Button.appearance(Button.Variant.Outline), DropdownMenu.Align.End)(Button(Button.appearance(Button.Variant.Outline, Button.Size.IconSm), aria.label := \"More Options\", Icons.moreHorizontal()))(ctx => Seq(ctx.group(ctx.item(Icons.volume2(), \"Mute Conversation\"), ctx.item(Icons.check(), \"Mark as Read\"), ctx.item(\"Report Conversation\"), ctx.item(\"Block User\"), ctx.item(\"Share Conversation\"), ctx.item(Icons.copy(), \"Copy Conversation\")), ctx.separator(), ctx.group(ctx.item(Icons.trash2(), Menu.destructive, \"Delete Conversation\")))))"
        ),
        ex(
          "select",
          "Select",
          selectGroup(),
          "val currency = Var(\"$\")\nButtonGroup(ButtonGroup(Select(currency)(ctx => Seq(ctx.group(ctx.item(\"$\", \"US Dollar\"), ctx.item(\"€\", \"Euro\"), ctx.item(\"£\", \"British Pound\"))), Input(placeholder := \"10.00\", pattern := \"[0-9]*\")), ButtonGroup(Button(Button.appearance(Button.Variant.Outline, Button.Size.Icon), aria.label := \"Send\", Icons.arrowRight())))"
        ),
        ex(
          "popover",
          "Popover",
          popoverGroup(),
          "ButtonGroup(Button(Button.appearance(Button.Variant.Outline, Button.Size.Sm), Icons.bot(), \"Copilot\"), Popover(Popover.trigger(Button(Button.appearance(Button.Variant.Outline, Button.Size.IconSm), aria.label := \"Open Popover\", Icons.chevronDown())), Popover.content(cls := \"rounded-xl p-0 text-sm\", div(cls := \"px-4 py-3\", div(cls := \"text-sm font-medium\", \"Agent Tasks\")), Separator(), div(cls := \"p-4 text-sm\", Textarea(placeholder := \"Describe your task in natural language.\", cls := \"mb-4 resize-none\"), p(cls := \"font-medium\", \"Start a new task with Copilot\"), p(cls := \"text-muted-foreground\", \"Describe your task in natural language. Copilot will work in the background and open a pull request for your review.\")))))"
        )
      )
    case "button" =>
      Seq(
        ex(
          "button-size",
          "Size",
          div(
            cls := "flex flex-col items-start gap-8 sm:flex-row",
            div(
              cls := "flex items-start gap-2",
              Button(Button.appearance(Button.Variant.Outline, Button.Size.Sm), "Small"),
              Button(
                Button.appearance(Button.Variant.Outline, Button.Size.IconSm),
                aria.label := "Submit",
                inlineIcon("M7 7h10v10", "M7 17 17 7")
              )
            ),
            div(
              cls := "flex items-start gap-2",
              Button(Button.appearance(Button.Variant.Outline), "Default"),
              Button(
                Button.appearance(Button.Variant.Outline, Button.Size.Icon),
                aria.label := "Submit",
                inlineIcon("M7 7h10v10", "M7 17 17 7")
              )
            ),
            div(
              cls := "flex items-start gap-2",
              Button(Button.appearance(Button.Variant.Outline, Button.Size.Lg), "Large"),
              Button(
                Button.appearance(Button.Variant.Outline, Button.Size.IconLg),
                aria.label := "Submit",
                inlineIcon("M7 7h10v10", "M7 17 17 7")
              )
            )
          ),
          "div(cls := \"flex flex-col items-start gap-8 sm:flex-row\", div(cls := \"flex items-start gap-2\", Button(Button.appearance(Button.Variant.Outline, Button.Size.Sm), \"Small\"), Button(Button.appearance(Button.Variant.Outline, Button.Size.IconSm), aria.label := \"Submit\", inlineIcon(\"M7 7h10v10\", \"M7 17 17 7\"))), div(cls := \"flex items-start gap-2\", Button(Button.appearance(Button.Variant.Outline), \"Default\"), Button(Button.appearance(Button.Variant.Outline, Button.Size.Icon), aria.label := \"Submit\", inlineIcon(\"M7 7h10v10\", \"M7 17 17 7\"))), div(cls := \"flex items-start gap-2\", Button(Button.appearance(Button.Variant.Outline, Button.Size.Lg), \"Large\"), Button(Button.appearance(Button.Variant.Outline, Button.Size.IconLg), aria.label := \"Submit\", inlineIcon(\"M7 7h10v10\", \"M7 17 17 7\"))))"
        ),
        ex(
          "button-default",
          "Default",
          Button(Button.appearance(Button.Variant.Primary), "Button"),
          "Button(Button.appearance(Button.Variant.Primary), \"Button\")"
        ),
        ex(
          "button-outline",
          "Outline",
          Button(Button.appearance(Button.Variant.Outline), "Outline"),
          "Button(Button.appearance(Button.Variant.Outline), \"Outline\")"
        ),
        ex(
          "button-secondary",
          "Secondary",
          Button(Button.appearance(Button.Variant.Secondary), "Secondary"),
          "Button(Button.appearance(Button.Variant.Secondary), \"Secondary\")"
        ),
        ex(
          "button-ghost",
          "Ghost",
          Button(Button.appearance(Button.Variant.Ghost), "Ghost"),
          "Button(Button.appearance(Button.Variant.Ghost), \"Ghost\")"
        ),
        ex(
          "button-destructive",
          "Destructive",
          Button(Button.appearance(Button.Variant.Destructive), "Destructive"),
          "Button(Button.appearance(Button.Variant.Destructive), \"Destructive\")"
        ),
        ex(
          "button-link",
          "Link",
          Button(Button.appearance(Button.Variant.Link), "Link"),
          "Button(Button.appearance(Button.Variant.Link), \"Link\")"
        ),
        ex(
          "button-icon",
          "Icon",
          Button(
            Button.appearance(Button.Variant.Outline, Button.Size.Icon),
            aria.label := "Submit",
            inlineIcon("M12 2a10 10 0 1 0 10 10", "M12 2a10 10 0 0 1 10 10", "m16 12-4-4-4 4", "M12 16V8")
          ),
          "Button(Button.appearance(Button.Variant.Outline, Button.Size.Icon), aria.label := \"Submit\", inlineIcon(\"M12 2a10 10 0 1 0 10 10\", \"M12 2a10 10 0 0 1 10 10\", \"m16 12-4-4-4 4\", \"M12 16V8\"))"
        ),
        ex(
          "button-with-icon",
          "With Icon",
          Button(
            Button.appearance(Button.Variant.Outline, Button.Size.Sm),
            inlineIcon(
              "M6 3v12",
              "M18 9v12",
              "M6 15a6 6 0 0 0 6-6V3",
              "M6 3a2 2 0 1 0 0 4 2 2 0 0 0 0-4Z",
              "M18 7a2 2 0 1 0 0 4 2 2 0 0 0-4-4Z"
            ),
            "New Branch"
          ),
          "Button(Button.appearance(Button.Variant.Outline, Button.Size.Sm), inlineIcon(\"M6 3v12\", \"M18 9v12\", \"M6 15a6 6 0 0 0 6-6V3\", \"M6 3a2 2 0 1 0 0 4 2 2 0 0 0 0-4Z\", \"M18 7a2 2 0 1 0 0 4 2 2 0 0 0-4-4Z\"), \"New Branch\")"
        ),
        ex(
          "button-rounded",
          "Rounded",
          div(
            cls := "flex flex-col gap-8",
            Button(
              Button.appearance(Button.Variant.Outline, Button.Size.Icon),
              cls := "rounded-full",
              inlineIcon("m5 12 7-7 7 7", "M12 19V5")
            )
          ),
          "div(cls := \"flex flex-col gap-8\", Button(Button.appearance(Button.Variant.Outline, Button.Size.Icon), cls := \"rounded-full\", inlineIcon(\"m5 12 7-7 7 7\", \"M12 19V5\")))"
        ),
        ex(
          "button-loading",
          "Spinner",
          Button(Button.appearance(Button.Variant.Outline, Button.Size.Sm), disabled := true, Spinner(), "Submit"),
          "Button(Button.appearance(Button.Variant.Outline, Button.Size.Sm), disabled := true, Spinner(), \"Submit\")"
        ),
        ex(
          "button-group-demo",
          "Button Group",
          buttonGroupDemo(),
          "ButtonGroup(ButtonGroup(cls := \"hidden sm:flex\", Button(Button.appearance(Button.Variant.Outline, Button.Size.IconSm), aria.label := \"Go Back\", inlineIcon(\"m12 19-7-7 7-7\"))), ButtonGroup(Button(Button.appearance(Button.Variant.Outline, Button.Size.Sm), \"Archive\"), Button(Button.appearance(Button.Variant.Outline, Button.Size.Sm), \"Report\")), ButtonGroup(Button(Button.appearance(Button.Variant.Outline, Button.Size.Sm), \"Snooze\"), DropdownMenu.itemsWithTrigger(Button.appearance(Button.Variant.Outline), DropdownMenu.Align.End)(Button(Button.appearance(Button.Variant.Outline), cls := \"!ps-2\", Icons.chevronDown()))(ctx => Seq(ctx.item(Icons.volume2(), \"Mute Conversation\"), ctx.item(Icons.check(), \"Mark as Read\"), ctx.item(Icons.alertCircle(), \"Report Conversation\"), ctx.item(Icons.user(), \"Block User\"), ctx.item(Icons.forward(), \"Share Conversation\"), ctx.item(Icons.copy(), \"Copy Conversation\"), ctx.separator(), ctx.item(Icons.trash2(), Menu.destructive, \"Delete Conversation\")))))"
        )
      )
    case "calendar" =>
      Seq(
        ex(
          "calendar-02",
          "Range Calendar",
          Calendar(
            Var(Option(new js.Date(2025, 5, 12))),
            cls := "rounded-lg border shadow-sm"
          ),
          "val value = Var((Some(new js.Date(2025, 5, 12)), Some(new js.Date(2025, 5, 18))))\nCalendar.range(value, cls := \"rounded-lg border shadow-sm\")"
        ),
        ex(
          "calendar-13",
          "Month and Year Selector",
          calendarWithSelector(),
          "val value = Var(Option(js.Date(2025, 5, 12)))\nCalendar(value, Calendar.CaptionLayout.Dropdown, cls := \"rounded-lg border shadow-sm\")\nSelect(Var(\"dropdown\"))(ctx => Seq(ctx.item(\"dropdown\", \"Month and Year\"), ctx.item(\"months\", \"Month Only\"), ctx.item(\"years\", \"Year Only\")))"
        ),
        ex(
          "calendar-22",
          "Date of Birth Picker",
          dateOfBirthPicker(),
          "val value = Var(Option.empty[js.Date])\nval open = Var(false)\ndiv(cls := \"flex flex-col gap-3\", Label(forId := \"date\", \"Date of birth\"), Popover(Popover.trigger(Button(Button.appearance(Button.Variant.Outline), \"Select date\", Icons.chevronDown())), Popover.content(cls := \"w-auto overflow-hidden p-0\", Calendar(value, d => d.getTime() > new js.Date().getTime(), Calendar.CaptionLayout.Dropdown))))"
        ),
        ex(
          "calendar-24",
          "Date and Time Picker",
          dateTimePicker(),
          "div(cls := \"flex gap-4\", div(cls := \"flex flex-col gap-3\", Label(forId := \"date\", \"Date\"), Popover(Popover.trigger(Button(Button.appearance(Button.Variant.Outline), \"Select date\", Icons.chevronDown())), Popover.content(cls := \"w-auto overflow-hidden p-0\", Calendar(Var(Option.empty[js.Date]), Calendar.CaptionLayout.Dropdown)))), div(cls := \"flex flex-col gap-3\", Label(forId := \"time\", \"Time\"), Input(typ := \"time\", idAttr := \"time\", defaultValue := \"10:30:00\", step := \"1\")))"
        ),
        ex(
          "calendar-29",
          "Natural Language Picker",
          naturalLanguagePicker(),
          "val inputValue = Var(\"In 2 days\")\nval value = Var(Option(new js.Date(new js.Date().getTime() + 2 * 24 * 60 * 60 * 1000)))\ndiv(cls := \"flex flex-col gap-3\", Label(forId := \"date\", \"Schedule Date\"), div(cls := \"relative flex gap-2\", Input(idAttr := \"date\", controlled(value <-- inputValue.signal, onInput.mapToValue --> inputValue), placeholder := \"Tomorrow or next week\", cls := \"bg-background pe-10\"), Button(Button.appearance(Button.Variant.Ghost), cls := \"absolute end-2 top-1/2 size-6 -translate-y-1/2\", Icons.calendar(), span(cls := \"sr-only\", \"Select date\"))), p(cls := \"px-1 text-sm text-muted-foreground\", \"Your post will be published on \", text <-- value.signal.map(_.fold(\"\")(_.toLocaleDateString(\"en-US\"))), \".\"))"
        )
      )
    case "card" =>
      Seq(
        ex(
          "card-demo",
          "Card",
          Card(
            cls := "-my-4 w-full max-w-sm",
            Card.header(
              Card.title("Login to your account"),
              Card.description("Enter your email below to login to your account"),
              Card.action(Button(Button.appearance(Button.Variant.Link), "Sign Up"))
            ),
            Card.content(
              form(
                div(
                  cls := "flex flex-col gap-6",
                  div(
                    cls := "grid gap-2",
                    Label(forId := "email", "Email"),
                    Input(idAttr := "email", typ := "email", placeholder := "m@example.com", required := true)
                  ),
                  div(
                    cls := "grid gap-2",
                    div(
                      cls := "flex items-center",
                      Label(forId := "password", "Password"),
                      a(
                        href := "##",
                        cls := "ms-auto inline-block text-sm underline-offset-4 hover:underline",
                        "Forgot your password?"
                      )
                    ),
                    Input(idAttr := "password", typ := "password", required := true)
                  )
                )
              )
            ),
            Card.footer(
              cls := "flex-col gap-2",
              Button(Button.appearance(Button.Variant.Primary), typ := "submit", cls := "w-full", "Login"),
              Button(Button.appearance(Button.Variant.Outline), cls := "w-full", "Login with Google")
            )
          ),
          "Card(cls := \"-my-4 w-full max-w-sm\", Card.header(Card.title(\"Login to your account\"), Card.description(\"Enter your email below to login to your account\"), Card.action(Button(Button.appearance(Button.Variant.Link), \"Sign Up\"))), Card.content(form(div(cls := \"flex flex-col gap-6\", div(cls := \"grid gap-2\", Label(forId := \"email\", \"Email\"), Input(idAttr := \"email\", typ := \"email\", placeholder := \"m@example.com\", required := true)), div(cls := \"grid gap-2\", div(cls := \"flex items-center\", Label(forId := \"password\", \"Password\"), a(href := \"##\", cls := \"ms-auto inline-block text-sm underline-offset-4 hover:underline\", \"Forgot your password?\")), Input(idAttr := \"password\", typ := \"password\", required := true))))), Card.footer(cls := \"flex-col gap-2\", Button(Button.appearance(Button.Variant.Primary), cls := \"w-full\", \"Login\"), Button(Button.appearance(Button.Variant.Outline), cls := \"w-full\", \"Login with Google\")))"
        ),
        ex(
          "card-spacing",
          "Spacing",
          spacingCard(),
          "Card(cls := \"gap-4\", Card.header(Card.title(\"Login to your account\"), Card.description(\"Enter your email below to log in.\")), Card.content(Input(placeholder := \"Email\")), Card.footer(Button(Button.appearance(Button.Variant.Primary), \"Login\")))"
        ),
        ex(
          "card-edge-to-edge",
          "Edge-to-edge",
          Card(
            Card.header(
              Card.title("Terms of Service"),
              Card.description("Review the terms before accepting the agreement.")
            ),
            Card.content(
              cls := "-mb-(--card-spacing)",
              div(
                cls := "-mx-(--card-spacing) max-h-48 space-y-4 overflow-y-scroll border-t bg-muted/50 px-(--card-spacing) py-4 text-sm leading-relaxed",
                p(
                  "These terms govern your use of the workspace, including access to shared documents, project files, and collaboration tools."
                ),
                p(
                  "You are responsible for the content you upload and for ensuring that your team has the appropriate permissions to view or edit it."
                ),
                p(
                  "We may update features or limits as the service evolves. When those changes materially affect your workflow, we will notify your workspace administrators."
                ),
                p(
                  "By continuing, you agree to keep your account credentials secure and to follow your organization's acceptable use policies."
                )
              )
            ),
            Card.footer(
              cls := "justify-end gap-2",
              Button(Button.appearance(Button.Variant.Outline), "Decline"),
              Button(Button.appearance(Button.Variant.Primary), "Accept")
            )
          ),
          "Card(Card.header(Card.title(\"Terms of Service\"), Card.description(\"Review the terms before accepting the agreement.\")), Card.content(cls := \"-mb-(--card-spacing)\", div(cls := \"-mx-(--card-spacing) max-h-48 space-y-4 overflow-y-scroll border-t bg-muted/50 px-(--card-spacing) py-4 text-sm leading-relaxed\", p(\"These terms govern your use of the workspace, including access to shared documents, project files, and collaboration tools.\"))), Card.footer(cls := \"justify-end gap-2\", Button(Button.appearance(Button.Variant.Outline), \"Decline\"), Button(Button.appearance(Button.Variant.Primary), \"Accept\")))"
        ),
        ex(
          "card-image",
          "Image",
          Card(
            cls := "relative mx-auto w-full max-w-sm pt-0",
            div(cls := "absolute inset-0 z-30 aspect-video bg-black/35"),
            img(
              src := "https://avatar.vercel.shadcn.com/shadcn1.png",
              alt := "Event cover",
              cls := "relative z-20 aspect-video w-full object-cover brightness-60 grayscale dark:brightness-40"
            ),
            Card.header(
              Card.action(Badge.of(_.variant(Badge.Variant.Secondary), _ => "Featured")),
              Card.title("Design systems meetup"),
              Card.description("A practical talk on component APIs, accessibility, and shipping faster.")
            ),
            Card.footer(Button(Button.appearance(Button.Variant.Primary), cls := "w-full", "View Event"))
          ),
          "Card(cls := \"relative mx-auto w-full max-w-sm pt-0\", div(cls := \"absolute inset-0 z-30 aspect-video bg-black/35\"), img(src := \"https://avatar.vercel.shadcn.com/shadcn1.png\", alt := \"Event cover\", cls := \"relative z-20 aspect-video w-full object-cover brightness-60 grayscale dark:brightness-40\"), Card.header(Card.action(Badge(Badge.Variant.Secondary, \"Featured\")), Card.title(\"Design systems meetup\"), Card.description(\"Join designers and engineers for an evening of practical talks.\")), Card.footer(Button(Button.appearance(Button.Variant.Primary), cls := \"w-full\", \"View Event\")))"
        )
      )
    case "carousel" =>
      Seq(
        ex(
          "carousel-size",
          "Sizes",
          carousel("md:basis-1/2 lg:basis-1/3", 5),
          "val c = Carousel.ctx()\nCarousel.root(cls := \"w-full max-w-sm\", c.content((1 to 5).map(i => c.item(cls := \"md:basis-1/2 lg:basis-1/3\", div(cls := \"p-1\", Card(Card.content(cls := \"flex aspect-square items-center justify-center p-6\", span(cls := \"text-3xl font-semibold\", i.toString))))))*), c.previous(), c.next())"
        ),
        ex(
          "spacing",
          "Spacing",
          carousel("ps-1 md:basis-1/2 lg:basis-1/3", 5, contentClass = "-ms-1"),
          "val c = Carousel.ctx()\nCarousel.root(cls := \"w-full max-w-sm\", c.content((Seq(cls := \"-ms-1\") ++ (1 to 5).map(i => c.item(cls := \"ps-1 md:basis-1/2 lg:basis-1/3\", div(cls := \"p-1\", Card(Card.content(cls := \"flex aspect-square items-center justify-center p-6\", span(cls := \"text-2xl font-semibold\", i.toString))))))*), c.previous(), c.next())"
        ),
        ex(
          "orientation",
          "Orientation",
          carousel("md:basis-1/2", 5, vertical = true),
          "val c = Carousel.ctx(Carousel.Orientation.Vertical)\nCarousel.root(cls := \"w-full max-w-xs\", c.content(Seq(cls := \"-mt-1 h-[200px]\") ++ (1 to 5).map(i => c.item(cls := \"pt-1 md:basis-1/2\", div(cls := \"p-1\", Card(Card.content(cls := \"flex items-center justify-center p-6\", span(cls := \"text-3xl font-semibold\", i.toString))))))*), c.previous(), c.next())"
        ),
        ex(
          "api",
          "API",
          carouselApi(),
          "val c = Carousel.ctx()\nval items = (1 to 5).map(i => c.item(Card(Card.content(cls := \"flex aspect-square items-center justify-center p-6\", span(cls := \"text-4xl font-semibold\", i.toString)))))\ndiv(Carousel.root(cls := \"w-full max-w-xs\", c.content(items*), c.previous(), c.next()), p(cls := \"py-2 text-center text-sm text-muted-foreground\", text <-- c.selectedIndex.combineWithFn(c.count)((i, count) => s\"Slide ${i + 1} of $count\")))"
        ),
        ex(
          "plugin",
          "Plugins",
          carousel("basis-full", 5),
          "val c = Carousel.ctx()\nCarousel.root(cls := \"w-full max-w-xs\", c.content((1 to 5).map(i => c.item(div(cls := \"p-1\", Card(Card.content(cls := \"flex aspect-square items-center justify-center p-6\", span(cls := \"text-4xl font-semibold\", i.toString)))))*), c.previous(), c.next())"
        )
      )
    case _ => Nil

  private def breadcrumbSeparator(): HtmlElement =
    Breadcrumb(
      Breadcrumb.list(
        Breadcrumb.item(Breadcrumb.link("/", "Home")),
        Breadcrumb.separator(inlineIcon("M5 12h14")),
        Breadcrumb.item(Breadcrumb.link("/components", "Components")),
        Breadcrumb.separator(inlineIcon("M5 12h14")),
        Breadcrumb.item(Breadcrumb.page("Breadcrumb"))
      )
    )

  private def breadcrumbDropdown(): HtmlElement =
    Breadcrumb(
      Breadcrumb.list(
        Breadcrumb.item(Breadcrumb.link("/", "Home")),
        Breadcrumb.separator(inlineIcon("M5 12h14")),
        Breadcrumb.item(
          DropdownMenu.itemsWithTrigger(Button.appearance(Button.Variant.Ghost))(
            span("Components"),
            Icons.chevronDown()
          )(ctx => Seq(ctx.item("Documentation"), ctx.item("Themes"), ctx.item("GitHub")))
        ),
        Breadcrumb.separator(inlineIcon("M5 12h14")),
        Breadcrumb.item(Breadcrumb.page("Breadcrumb"))
      )
    )

  private def breadcrumbDemo(): HtmlElement =
    Breadcrumb(
      Breadcrumb.list(
        Breadcrumb.item(Breadcrumb.link("/", "Home")),
        Breadcrumb.separator(),
        Breadcrumb.item(Breadcrumb.ellipsis()),
        Breadcrumb.separator(),
        Breadcrumb.item(Breadcrumb.link("/docs/components", "Components")),
        Breadcrumb.separator(),
        Breadcrumb.item(Breadcrumb.page("Breadcrumb"))
      )
    )

  private def breadcrumbResponsive(): HtmlElement =
    val open = Var(false)
    val items = Seq(
      ("#", "Home"),
      ("#", "Documentation"),
      ("#", "Build Your Application"),
      ("#", "Data Fetching"),
      ("", "Caching and Revalidating")
    )
    val middle =
      if org.scalajs.dom.window.innerWidth >= 768 then
        DropdownMenu.itemsWithTrigger(cls := "flex items-center gap-1", DropdownMenu.Align.Start)(
          aria.label := "Toggle menu",
          Breadcrumb.ellipsis()
        )(ctx => Seq(ctx.item("Documentation")))
      else
        Drawer(open)(
          Drawer.header(
            cls := "text-start",
            Drawer.title("Navigate to"),
            Drawer.description("Select a page to navigate to.")
          ),
          div(cls := "grid gap-1 px-4", a(href := "#", "Documentation")),
          Drawer.footer(cls := "pt-4", Drawer.close("Close"))
        )
    Breadcrumb(
      Breadcrumb.list(
        Breadcrumb.item(Breadcrumb.link(items.head._1, items.head._2)),
        Breadcrumb.separator(),
        Breadcrumb.item(middle),
        Breadcrumb.separator(),
        Breadcrumb.item(Breadcrumb.link(items(3)._1, items(3)._2, cls := "max-w-20 truncate md:max-w-none")),
        Breadcrumb.separator(),
        Breadcrumb.item(Breadcrumb.page(items(4)._2, cls := "max-w-20 truncate md:max-w-none"))
      )
    )

  private def calendarWithSelector(): HtmlElement =
    val selected = Var(Option(new js.Date(2025, 5, 12)))
    val layout = Var("dropdown")
    div(
      cls := "flex flex-col gap-4",
      Calendar(selected, Calendar.CaptionLayout.Dropdown, cls := "rounded-lg border shadow-sm"),
      div(
        cls := "flex flex-col gap-3",
        Label(forId := "dropdown", cls := "px-1", "Dropdown"),
        Select(layout)(ctx =>
          Seq(
            ctx.item("dropdown", "Month and Year"),
            ctx.item("dropdown-months", "Month Only"),
            ctx.item("dropdown-years", "Year Only")
          )
        )
      )
    )

  private def dateOfBirthPicker(): HtmlElement =
    val selected = Var(Option.empty[js.Date])
    val open = Var(false)
    div(
      cls := "flex flex-col gap-3",
      Label(forId := "dob-date", cls := "px-1", "Date of birth"),
      Popover(
        Popover.trigger(
          Button(
            Button.appearance(Button.Variant.Outline),
            cls := "w-48 justify-between font-normal",
            text <-- selected.signal.map(_.fold("Select date")(_.toLocaleDateString())),
            Icons.chevronDown()
          )
        ),
        Popover.content(
          Floating.Placement(align = Floating.Align.Start),
          "w-auto overflow-hidden p-0",
          Calendar(selected, d => d.getTime() > new js.Date().getTime(), Calendar.CaptionLayout.Dropdown)
        )
      )
    )

  private def dateTimePicker(): HtmlElement =
    val selected = Var(Option.empty[js.Date])
    div(
      cls := "flex gap-4",
      div(
        cls := "flex flex-col gap-3",
        Label(forId := "date", cls := "px-1", "Date"),
        Popover(
          Popover.trigger(
            Button(
              Button.appearance(Button.Variant.Outline),
              cls := "w-32 justify-between font-normal",
              text <-- selected.signal.map(_.fold("Select date")(_.toLocaleDateString())),
              Icons.chevronDown()
            )
          ),
          Popover.content(
            Floating.Placement(align = Floating.Align.Start),
            "w-auto overflow-hidden p-0!",
            Calendar(selected, Calendar.CaptionLayout.Dropdown)
          )
        )
      ),
      div(
        cls := "flex flex-col gap-3",
        Label(forId := "time", cls := "px-1", "Time"),
        Input(
          typ := "time",
          idAttr := "time",
          defaultValue := "10:30:00",
          stepAttr := "1",
          cls := "appearance-none bg-background [&::-webkit-calendar-picker-indicator]:hidden [&::-webkit-calendar-picker-indicator]:appearance-none"
        )
      )
    )

  private def naturalLanguagePicker(): HtmlElement =
    val inputValue = Var("In 2 days")
    val date = new js.Date(new js.Date().getTime() + 2 * 24 * 60 * 60 * 1000)
    div(
      cls := "flex flex-col gap-3",
      Label(forId := "schedule-date", cls := "px-1", "Schedule Date"),
      div(
        cls := "relative flex gap-2",
        Input(
          idAttr := "date",
          placeholder := "Tomorrow or next week",
          cls := "bg-background pe-10",
          controlled(value <-- inputValue.signal, onInput.mapToValue --> inputValue)
        ),
        Button(
          Button.appearance(Button.Variant.Ghost),
          cls := "absolute end-2 top-1/2 size-6 -translate-y-1/2",
          Icons.calendar(),
          span(cls := "sr-only", "Select date")
        )
      ),
      div(
        cls := "px-1 text-sm text-muted-foreground",
        "Your post will be published on ",
        span(cls := "font-medium", date.toLocaleDateString()),
        "."
      )
    )

  private def buttonSize(): HtmlElement =
    div(
      cls := "flex flex-col items-start gap-8 sm:flex-row",
      div(
        cls := "flex items-start gap-2",
        Button(Button.appearance(Button.Variant.Outline, Button.Size.Sm), "Small"),
        Button(
          Button.appearance(Button.Variant.Outline, Button.Size.IconSm),
          aria.label := "Submit",
          inlineIcon("M7 7h10v10", "M7 17 17 7")
        )
      ),
      div(
        cls := "flex items-start gap-2",
        Button(Button.appearance(Button.Variant.Outline), "Default"),
        Button(
          Button.appearance(Button.Variant.Outline, Button.Size.Icon),
          aria.label := "Submit",
          inlineIcon("M7 7h10v10", "M7 17 17 7")
        )
      ),
      div(
        cls := "flex items-start gap-2",
        Button(Button.appearance(Button.Variant.Outline, Button.Size.Lg), "Large"),
        Button(
          Button.appearance(Button.Variant.Outline, Button.Size.IconLg),
          aria.label := "Submit",
          inlineIcon("M7 7h10v10", "M7 17 17 7")
        )
      )
    )

  private def inlineIcon(paths: String*): SvgElement =
    svgTag(
      svg.viewBox := "0 0 24 24",
      svg.fill := "none",
      svg.stroke := "currentColor",
      svg.strokeWidth := "2",
      svg.strokeLineCap := "round",
      svg.strokeLineJoin := "round",
      svg.cls := "size-4",
      paths.map(p => svgPath(svg.d := p))
    )

  private def inputGroupDemo(): HtmlElement =
    val voice = Var(false)
    ButtonGroup(
      cls := "[--radius:9999rem]",
      ButtonGroup(Button(Button.appearance(Button.Variant.Outline, Button.Size.Icon), Icons.plus())),
      ButtonGroup(
        cls := "flex-1",
        InputGroup(
          InputGroup.input(
            placeholder <-- voice.signal.map(v => if v then "Record and send audio..." else "Send a message..."),
            disabled <-- voice.signal
          ),
          InputGroup.addon(
            InputGroup.AddonAlign.InlineEnd,
            Tooltip(
              "Voice Mode",
              InputGroup.button(
                InputGroup.ButtonSize.IconXs,
                onClick --> (_ => voice.update(!_)),
                aria.pressed <-- voice.signal.map(_.toString),
                dataAttr("active") <-- voice.signal.map(_.toString),
                cls := "data-[active=true]:bg-orange-100 data-[active=true]:text-orange-700 dark:data-[active=true]:bg-orange-800 dark:data-[active=true]:text-orange-100",
                Icons.audioLines()
              )
            )
          )
        )
      )
    )

  private def selectGroup(): HtmlElement =
    val currency = Var("$")
    ButtonGroup(
      ButtonGroup(
        Select(currency)(ctx =>
          Seq(ctx.group(ctx.item("$", "US Dollar"), ctx.item("€", "Euro"), ctx.item("£", "British Pound")))
        ),
        Input(placeholder := "10.00", pattern := "[0-9]*")
      ),
      ButtonGroup(
        Button(Button.appearance(Button.Variant.Outline, Button.Size.Icon), aria.label := "Send", Icons.arrowRight())
      )
    )

  private def popoverGroup(): HtmlElement =
    ButtonGroup(
      Button(Button.appearance(Button.Variant.Outline, Button.Size.Sm), Icons.bot(), "Copilot"),
      Popover(
        Popover.trigger(
          Button(
            Button.appearance(Button.Variant.Outline, Button.Size.IconSm),
            aria.label := "Open Popover",
            Icons.chevronDown()
          )
        ),
        Popover.content(
          cls := "rounded-xl p-0 text-sm",
          div(cls := "px-4 py-3", div(cls := "text-sm font-medium", "Agent Tasks")),
          Separator(),
          div(
            cls := "p-4 text-sm *:[p:not(:last-child)]:mb-2",
            Textarea(placeholder := "Describe your task in natural language.", cls := "mb-4 resize-none"),
            p(cls := "font-medium", "Start a new task with Copilot"),
            p(
              cls := "text-muted-foreground",
              "Describe your task in natural language. Copilot will work in the background and open a pull request for your review."
            )
          )
        )
      )
    )

  private def dropdownGroup(): HtmlElement =
    ButtonGroup(
      Button(Button.appearance(Button.Variant.Outline), "Follow"),
      DropdownMenu.itemsWithTrigger(Button.appearance(Button.Variant.Outline), DropdownMenu.Align.End)(
        Button(Button.appearance(Button.Variant.Outline), cls := "!ps-2", Icons.chevronDown())
      )(ctx =>
        Seq(
          ctx.group(
            ctx.item(Icons.volume2(), "Mute Conversation"),
            ctx.item(Icons.check(), "Mark as Read"),
            ctx.item(Icons.alertCircle(), "Report Conversation"),
            ctx.item(Icons.user(), "Block User"),
            ctx.item(Icons.forward(), "Share Conversation"),
            ctx.item(Icons.copy(), "Copy Conversation")
          ),
          ctx.separator(),
          ctx.group(ctx.item(Icons.trash2(), Menu.destructive, "Delete Conversation"))
        )
      )
    )

  private def spacingCard(): HtmlElement =
    val spacing = Var(Option("4"))
    val classes = spacing.signal.map(_.fold("[--card-spacing:--spacing(4)]")(v => s"[--card-spacing:--spacing($v)]"))
    div(
      cls := "mx-auto grid w-full max-w-sm gap-4",
      ToggleGroup.single(
        spacing,
        Toggle.Variant.Outline,
        Toggle.Size.Sm,
        ToggleGroup.Item("4", "16px"),
        ToggleGroup.Item("5", "20px"),
        ToggleGroup.Item("6", "24px"),
        ToggleGroup.Item("8", "32px")
      ),
      Card(
        cls <-- classes,
        Card.header(
          Card.title("Login to your account"),
          Card.description("Enter your email below to login to your account"),
          Card.action(Button(Button.appearance(Button.Variant.Link), "Sign Up"))
        ),
        Card.content(Input(placeholder := "Email")),
        Card.footer(Button(Button.appearance(Button.Variant.Primary), "Login"))
      )
    )

  private def buttonGroupDemo(): HtmlElement =
    ButtonGroup(
      ButtonGroup(
        cls := "hidden sm:flex",
        Button(
          Button.appearance(Button.Variant.Outline, Button.Size.IconSm),
          aria.label := "Go Back",
          inlineIcon("m12 19-7-7 7-7")
        )
      ),
      ButtonGroup(
        Button(Button.appearance(Button.Variant.Outline, Button.Size.Sm), "Archive"),
        Button(Button.appearance(Button.Variant.Outline, Button.Size.Sm), "Report")
      ),
      ButtonGroup(
        Button(Button.appearance(Button.Variant.Outline, Button.Size.Sm), "Snooze"),
        dropdownGroup()
      )
    )

  private def carousel(
      itemClass: String,
      count: Int,
      contentClass: String = "",
      vertical: Boolean = false
  ): HtmlElement =
    val c = Carousel.ctx(if vertical then Carousel.Orientation.Vertical else Carousel.Orientation.Horizontal)
    val baseContent = if vertical then s"$contentClass -mt-1 h-[200px]".trim else contentClass
    val items = (1 to count).map(i =>
      c.item(
        cls := (if vertical then s"$itemClass pt-1 md:basis-1/2" else itemClass),
        div(
          cls := "p-1",
          Card(
            Card.content(
              cls := "flex aspect-square items-center justify-center p-6",
              span(
                cls := (if vertical then "text-3xl font-semibold"
                        else if itemClass == "basis-full" then "text-4xl font-semibold"
                        else if contentClass.nonEmpty then "text-2xl font-semibold"
                        else "text-3xl font-semibold"),
                i.toString
              )
            )
          )
        )
      )
    )
    Carousel.root(
      cls := (if vertical then "w-full max-w-xs" else "w-full max-w-sm"),
      c.content((Seq(cls := baseContent) ++ items)*),
      c.previous(),
      c.next()
    )

  private def carouselApi(): HtmlElement =
    val c = Carousel.ctx()
    val items = (1 to 5).map(i =>
      c.item(
        Card(
          Card.content(
            cls := "flex aspect-square items-center justify-center p-6",
            span(cls := "text-4xl font-semibold", i.toString)
          )
        )
      )
    )
    div(
      Carousel.root(c.content(items*), c.previous(), c.next()),
      p(
        cls := "py-2 text-center text-sm text-muted-foreground",
        text <-- c.selectedIndex.combineWithFn(c.count)((i, count) => s"Slide ${i + 1} of $count")
      )
    )
