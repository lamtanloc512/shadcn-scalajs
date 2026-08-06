package shadcnscalajs.blocks.dashboard01

import com.raquo.laminar.api.L.*
import shadcnscalajs.ui.*

/** An original analytics-dashboard composition — not a copy of any specific reference layout's cards, copy, or dataset
  * — built to exercise every `shadcnscalajs.site.ThemeConfig` knob (colors, radius, fonts, icon library) as the live
  * preview on `/create`. See docs/superpowers/specs/2026-08-06-create-theme-customizer-design.md.
  */
object Dashboard01:

  private val asideTag = htmlTag("aside")
  private val mainTag = htmlTag("main")

  private final case class NavItem(label: String, active: Boolean)
  private val navItems = List(
    NavItem("Overview", active = true),
    NavItem("Transactions", active = false),
    NavItem("Reports", active = false),
    NavItem("Settings", active = false)
  )

  private final case class Transaction(name: String, category: String, amount: String, status: String)
  private val transactions = List(
    Transaction("Coffee Roasters Co.", "Food & Drink", "-$4.50", "Completed"),
    Transaction("Cloud Hosting Inc.", "Software", "-$29.00", "Completed"),
    Transaction("Client Payment", "Income", "+$1,200.00", "Completed"),
    Transaction("Design Subscription", "Software", "-$12.00", "Pending")
  )

  // Written out as full literal class names (not string-interpolated) so Tailwind's static content
  // scanner — which only recognizes complete class-name tokens verbatim in source — actually
  // generates all five `bg-chart-N` utilities instead of just whichever happens to appear literally
  // elsewhere in the codebase.
  private val chartColorClasses =
    List("bg-chart-1", "bg-chart-2", "bg-chart-3", "bg-chart-4", "bg-chart-5")

  def apply(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      cls := "cn-dashboard-01 flex min-h-svh w-full bg-muted/30",
      mods,
      asideTag(
        cls := "hidden w-56 shrink-0 border-r bg-card md:block",
        div(cls := "flex h-14 items-center gap-2 border-b px-4 text-sm font-semibold", "Acme Analytics"),
        navTag(
          cls := "flex flex-col gap-1 p-3",
          navItems.map { item =>
            a(
              href := "#",
              dataAttr("active") := item.active.toString,
              cls := s"flex items-center gap-2 rounded-md px-3 py-2 text-sm ${
                  if item.active then "bg-accent text-accent-foreground"
                  else "text-muted-foreground hover:bg-accent hover:text-accent-foreground"
                }",
              item.label
            )
          }
        )
      ),
      mainTag(
        cls := "flex-1 p-6",
        div(
          cls := "mb-6 flex items-center justify-between",
          h1(cls := "font-heading text-2xl font-semibold", "Overview"),
          Button.of(_.variant(Button.Variant.Outline), _.size(Button.Size.Sm), _ => span("Export", Icons.chevronDown()))
        ),
        div(
          cls := "grid gap-4 md:grid-cols-3",
          Card(
            cls := "md:col-span-2",
            Card.header(Card.title("Weekly Activity"), Card.description("Sessions over the last 7 days")),
            Card.content(
              Chart(
                div(
                  cls := "flex h-full w-full items-end gap-3 px-2",
                  List(38, 62, 45, 80, 55, 90, 70).zipWithIndex.map { case (pct, i) =>
                    div(cls := s"flex-1 rounded-t ${chartColorClasses(i % 5)}", height := s"$pct%")
                  }
                )
              )
            )
          ),
          Card(
            Card.header(Card.title("Monthly Goal")),
            Card.content(
              div(
                cls := "mb-2 flex items-center justify-between text-sm",
                span("Revenue"),
                span(cls := "font-medium", "68%")
              ),
              Progress(68, cls := "mb-4"),
              div(
                cls := "mb-2 flex items-center justify-between text-sm",
                span("New Customers"),
                span(cls := "font-medium", "42%")
              ),
              Progress(42)
            )
          )
        ),
        div(
          cls := "mt-4 grid gap-4 md:grid-cols-3",
          Card(
            cls := "md:col-span-2",
            Card.header(Card.title("Recent Transactions")),
            Card.content(
              Table(
                Table.header(
                  Table.row(Table.head("Name"), Table.head("Category"), Table.head("Amount"), Table.head("Status"))
                ),
                Table.body(
                  transactions.map { t =>
                    Table.row(
                      Table.cell(t.name),
                      Table.cell(cls := "text-muted-foreground", t.category),
                      Table.cell(t.amount),
                      if t.status == "Completed" then
                        Table.cell(
                          cls := "flex items-center gap-1.5",
                          Icons.check(),
                          Badge.of(
                            _.variant(Badge.Variant.Secondary),
                            _ => t.status
                          )
                        )
                      else
                        Table.cell(
                          Badge.of(
                            _.variant(Badge.Variant.Outline),
                            _ => t.status
                          )
                        )
                    )
                  }
                )
              )
            )
          ),
          Card(
            Card.header(Card.title("Quick Update")),
            Card.content(
              Field(Field.label("Display name"), Input(placeholder := "Ada Lovelace")),
              Field(cls := "mt-3", Field.label("Support email"), Input(placeholder := "team@example.com"))
            ),
            Card.footer(Button(cls := "w-full", "Save changes"))
          )
        )
      )
    )
