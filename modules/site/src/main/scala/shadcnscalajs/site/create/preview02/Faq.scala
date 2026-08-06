package shadcnscalajs.site.create.preview02

import com.raquo.laminar.api.L.*
import shadcnscalajs.ui.*

/** preview-02 mosaic card — port of
  * `/Users/elam/Personal/shadcn-svelte/docs/src/lib/registry/examples/create/preview-02/cards/faq.svelte`
  */
object Faq:

  private final case class QA(question: String, answer: String)

  private val generalQuestions: List[QA] = List(
    QA(
      "How secure is my financial data with Ledger?",
      "We use bank-level AES-256 encryption, SOC 2 Type II certified infrastructure, and never store your credentials. All connections use read-only access tokens. We are a SEC registered investment advisor."
    ),
    QA(
      "How do I connect my bank or investment accounts?",
      "Go to Settings > Linked Accounts and search for your institution. We support over 12,000 banks and brokerages via Plaid and MX."
    ),
    QA(
      "Can I export my data for tax purposes?",
      "Yes. Navigate to Reports > Tax Export to download a CSV or PDF summary of your transactions, dividends, and capital gains for any tax year."
    )
  )

  private val billingQuestions: List[QA] = List(
    QA(
      "What is the difference between Basic and Pro pricing tiers?",
      "Basic includes budgeting, goal tracking, and up to 3 linked accounts. Pro adds unlimited accounts, dividend tracking, portfolio analysis, and priority support."
    ),
    QA(
      "How do I cancel my subscription?",
      "Go to Settings > Billing > Manage Plan and click Cancel. Your access continues until the end of your current billing period."
    ),
    QA(
      "Do you offer a free trial?",
      "Yes. All new accounts start with a 14-day Pro trial. No credit card required."
    )
  )

  private val goalsQuestions: List[QA] = List(
    QA(
      "How do I set up a custom financial goal?",
      "Click New Goal from the Savings Targets card. Choose a category, set a target amount and date, and we'll calculate the monthly contribution needed."
    ),
    QA(
      "Can I track multiple goals at once?",
      "Yes. Pro accounts can track unlimited goals. Basic accounts support up to 3 active goals."
    ),
    QA(
      "How are monthly contributions calculated?",
      "We divide the remaining amount by the number of months until your target date, adjusted for your current savings rate and any auto-transfer schedules."
    )
  )

  private val tabTriggerClasses =
    "inline-flex flex-1 items-center justify-center rounded-md px-3 py-1 text-sm font-medium transition-all hover:text-foreground focus-visible:ring-[3px] focus-visible:ring-ring/50 data-[state=active]:bg-background data-[state=active]:text-foreground data-[state=active]:shadow"

  private def questionAccordion(openIndexVar: Var[Option[Int]], questions: List[QA]): HtmlElement =
    div(
      cls := "w-full",
      Accordion(
        openIndexVar,
        questions.map(qa => Accordion.Section(qa.question, qa.answer))*
      )
    )

  private def tabPanel(selectedTab: Signal[String], tabValue: String, panel: HtmlElement): HtmlElement =
    div(
      role := "tabpanel",
      cls := "flex-1 outline-none w-full",
      display <-- selectedTab.map(value => if value == tabValue then "block" else "none"),
      panel
    )

  private def tabTrigger(selectedTabVar: Var[String], tabValue: String, label: String): HtmlElement =
    button(
      typ := "button",
      role := "tab",
      cls := tabTriggerClasses,
      dataAttr("state") <-- selectedTabVar.signal.map(v => if v == tabValue then "active" else "inactive"),
      aria.selected <-- selectedTabVar.signal.map(_ == tabValue),
      onClick --> { _ => selectedTabVar.set(tabValue) },
      label
    )

  def apply(): HtmlElement =
    val selectedTabVar = Var("general")
    val generalOpenVar = Var[Option[Int]](Some(0))
    val billingOpenVar = Var[Option[Int]](Some(0))
    val goalsOpenVar = Var[Option[Int]](Some(0))

    Card(
      dataAttr("card") := "faq",
      Card.content(
        Tabs(
          Tabs.list(
            cls := "w-full",
            tabTrigger(selectedTabVar, "general", "General"),
            tabTrigger(selectedTabVar, "billing", "Billing"),
            tabTrigger(selectedTabVar, "goals", "Goals")
          ),
          tabPanel(selectedTabVar.signal, "general", questionAccordion(generalOpenVar, generalQuestions)),
          tabPanel(selectedTabVar.signal, "billing", questionAccordion(billingOpenVar, billingQuestions)),
          tabPanel(selectedTabVar.signal, "goals", questionAccordion(goalsOpenVar, goalsQuestions))
        )
      ),
      Card.footer(
        cls := "flex flex-col gap-2",
        Button.of(_.variant(Button.Variant.Outline), _ => cls := "w-full", _ => "Contact Support"),
        Button.of(_.variant(Button.Variant.Link), _ => cls := "w-full", _ => "Learn More")
      )
    )
