package shadcnscalajs.site

import com.raquo.laminar.api.L.*
import org.scalajs.dom

import scala.scalajs.js

/** Canonical HTML examples for the standalone custom-element audience.
  *
  * One source object is shared by the playground Example selector and every docs Web Component tab. Preview is always
  * mounted from that same source string so the two surfaces cannot diverge.
  */
final case class WebComponentExample(tag: String, source: String, marker: String)

object WebComponentExamples:

  private def entry(tag: String, marker: String)(source: String): WebComponentExample =
    WebComponentExample(tag, source.stripMargin.trim, marker)

  /** Mount the exact HTML source into a host so docs preview and playground iframe stay 1:1. */
  def preview(example: WebComponentExample): HtmlElement =
    div(
      cls := "flex min-h-[12rem] w-full items-center justify-center p-6",
      dataAttr("sc-example-tag") := example.tag,
      dataAttr("sc-example-marker") := example.marker,
      onMountCallback { ctx =>
        val host = ctx.thisNode.ref
        host.innerHTML = example.source
        // innerHTML does not execute <script>; re-insert so dialog/event demos can wire themselves.
        val scripts = host.querySelectorAll("script")
        var i = 0
        while i < scripts.length do
          val old = scripts.item(i).asInstanceOf[dom.Element]
          val script = dom.document.createElement("script")
          Option(old.getAttribute("type")).foreach(t => script.setAttribute("type", t))
          Option(old.getAttribute("src")) match
            case Some(src) => script.setAttribute("src", src)
            case None      => script.textContent = old.textContent
          old.parentNode.replaceChild(script, old)
          i += 1
        val tags = example.source
          .split("[\\s>]")
          .toList
          .collect {
            case t if t.startsWith("sc-") => t.takeWhile(c => c != '"' && c != '\'' && c != '/')
          }
          .distinct
        js.Dynamic.global.ScComponentsRuntime
          .asInstanceOf[js.UndefOr[js.Dynamic]]
          .toOption
          .foreach { runtime =>
            val load =
              runtime.loadScComponents.asInstanceOf[js.UndefOr[js.Function1[js.Array[String], js.Promise[js.Any]]]]
            load.toOption.foreach { fn =>
              val _ = fn(js.Array(tags*))
            }
          }
      }
    )

  private val entries: Map[String, WebComponentExample] = Map(
    "accordion" -> entry("sc-accordion", "Shipping options")("""
      |<sc-accordion style="width:min(28rem,100%)"
      |  sections='[{"title":"Shipping options","content":"Standard (5-7 days) and express (2-3 days) are available."},{"title":"Return policy","content":"Return unused items within 30 days of delivery."},{"title":"Support","content":"Email support@example.com during business hours."}]'>
      |</sc-accordion>
      |"""),
    "alert" -> entry("sc-alert", "Payment failed")("""
      |<sc-alert variant="destructive" style="width:min(28rem,100%)">
      |  <sc-alert-title>Payment failed</sc-alert-title>
      |  <sc-alert-description>Your card was declined. Update billing details to try again.</sc-alert-description>
      |</sc-alert>
      |"""),
    "avatar" -> entry("sc-avatar", "JD")("""
      |<sc-avatar style="display:inline-flex;width:2.5rem;height:2.5rem;border-radius:9999px;overflow:hidden;background:var(--muted);align-items:center;justify-content:center;font-weight:600">
      |  JD
      |</sc-avatar>
      |"""),
    "badge" -> entry("sc-badge", "New")("""
      |<sc-badge variant="secondary">New</sc-badge>
      |"""),
    "breadcrumb" -> entry("sc-breadcrumb", "Components")("""
      |<sc-breadcrumb style="display:flex;gap:.5rem;align-items:center;font-size:.875rem">
      |  <a href="#">Home</a><span>/</span><a href="#">Components</a><span>/</span><span>Button</span>
      |</sc-breadcrumb>
      |"""),
    "button" -> entry("sc-button", "Save changes")("""
      |<div style="display:flex;gap:.75rem;align-items:center;flex-wrap:wrap">
      |  <sc-button variant="primary" size="lg">Save changes</sc-button>
      |  <sc-button variant="outline">Cancel</sc-button>
      |  <sc-button variant="ghost" disabled>Disabled</sc-button>
      |</div>
      |"""),
    "button-group" -> entry("sc-button-group", "Left")("""
      |<sc-button-group style="display:inline-flex;gap:0">
      |  <sc-button variant="outline">Left</sc-button>
      |  <sc-button variant="outline">Center</sc-button>
      |  <sc-button variant="outline">Right</sc-button>
      |</sc-button-group>
      |"""),
    "calendar" -> entry("sc-calendar", "2025-01-15")("""
      |<sc-calendar style="width:20rem;min-height:18rem" value="2025-01-15" aria-label="Choose date"></sc-calendar>
      |"""),
    "card" -> entry("sc-card", "Ship your next interface")("""
      |<sc-card style="width:min(28rem,100%)">
      |  <sc-card-header>
      |    <sc-card-title>Ship your next interface</sc-card-title>
      |    <sc-card-description>Framework-agnostic components powered by Scala.js and Laminar.</sc-card-description>
      |  </sc-card-header>
      |  <sc-card-content style="display:flex;gap:.75rem;align-items:center">
      |    <sc-badge variant="secondary">Web Component</sc-badge>
      |    <sc-button variant="primary">Get started</sc-button>
      |  </sc-card-content>
      |</sc-card>
      |"""),
    "chart" -> entry("sc-chart", "Jan")("""
      |<sc-chart style="width:min(28rem,100%);height:16rem" type="bar"
      |  data='[["Jan",40],["Feb",65],["Mar",48],["Apr",80]]'
      |  tooltip-label="Revenue" show-labels>
      |</sc-chart>
      |"""),
    "checkbox" -> entry("sc-checkbox", "Accept terms")("""
      |<label style="display:flex;align-items:center;gap:.75rem;font-size:.875rem">
      |  <sc-checkbox style="width:1.25rem;height:1.25rem" checked aria-label="Accept terms"></sc-checkbox>
      |  Accept terms
      |</label>
      |"""),
    "collapsible" -> entry("sc-collapsible", "Show more")("""
      |<sc-collapsible style="width:min(24rem,100%)">
      |  <button slot="trigger" type="button">Show more</button>
      |  <div slot="content" style="padding:.75rem 0;color:var(--muted-foreground)">Extra details expand under the trigger.</div>
      |</sc-collapsible>
      |"""),
    "combobox" -> entry("sc-combobox", "Choose framework")("""
      |<sc-combobox style="width:min(20rem,100%)" placeholder="Choose framework"
      |  items='[{"value":"scala","label":"Scala.js"},{"value":"react","label":"React"},{"value":"vue","label":"Vue"}]'>
      |</sc-combobox>
      |"""),
    "command" -> entry("sc-command", "Search commands")("""
      |<sc-command style="width:min(24rem,100%);border:1px solid var(--border);border-radius:.75rem;padding:.75rem">
      |  <input placeholder="Search commands…" style="width:100%;margin-bottom:.5rem" />
      |  <div>Open settings</div>
      |  <div>Invite teammate</div>
      |</sc-command>
      |"""),
    "dialog" -> entry("sc-dialog", "Open dialog")("""
      |<div data-sc-dialog-demo style="display:grid;gap:.75rem;justify-items:start">
      |  <sc-button id="open-dialog" variant="outline">Open dialog</sc-button>
      |  <sc-dialog id="demo-dialog">
      |    <div style="display:grid;gap:.75rem;min-width:18rem">
      |      <strong>Welcome</strong>
      |      <p style="margin:0;color:var(--muted-foreground)">Your workspace is ready.</p>
      |      <sc-button id="close-dialog" variant="primary">Continue</sc-button>
      |    </div>
      |  </sc-dialog>
      |</div>
      |<script>
      |  (() => {
      |    const root = document.currentScript?.previousElementSibling || document.querySelector('[data-sc-dialog-demo]')
      |    const dialog = root?.querySelector('#demo-dialog') || document.getElementById('demo-dialog')
      |    const openBtn = root?.querySelector('#open-dialog') || document.getElementById('open-dialog')
      |    const closeBtn = root?.querySelector('#close-dialog') || document.getElementById('close-dialog')
      |    openBtn?.addEventListener('click', () => dialog?.setAttribute('open', ''))
      |    closeBtn?.addEventListener('click', () => dialog?.removeAttribute('open'))
      |  })()
      |</script>
      |"""),
    "dropdown-menu" -> entry("sc-dropdown-menu", "Open menu")("""
      |<sc-dropdown-menu items='[{"label":"Profile"},{"label":"Billing"},{"label":"Sign out"}]'>
      |  <sc-button slot="trigger" variant="outline">Open menu</sc-button>
      |</sc-dropdown-menu>
      |"""),
    "empty" -> entry("sc-empty", "No results")("""
      |<sc-empty style="width:min(24rem,100%);border:1px dashed var(--border);border-radius:.75rem;padding:2rem;text-align:center">
      |  <strong>No results</strong>
      |  <p style="margin:.5rem 0 0;color:var(--muted-foreground)">Try a different search query.</p>
      |</sc-empty>
      |"""),
    "field" -> entry("sc-field", "Email")("""
      |<sc-field style="display:grid;gap:.35rem;width:min(20rem,100%)">
      |  <sc-label>Email</sc-label>
      |  <sc-input placeholder="you@example.com" value="you@example.com"></sc-input>
      |</sc-field>
      |"""),
    "form" -> entry("sc-form", "Subscribe")("""
      |<sc-form style="display:grid;gap:.75rem;width:min(22rem,100%)">
      |  <sc-field style="display:grid;gap:.35rem">
      |    <sc-label>Email</sc-label>
      |    <sc-input type="email" placeholder="you@example.com"></sc-input>
      |  </sc-field>
      |  <sc-button variant="primary">Subscribe</sc-button>
      |</sc-form>
      |"""),
    "input" -> entry("sc-input", "you@example.com")("""
      |<sc-input style="width:min(20rem,100%)" placeholder="you@example.com" value="you@example.com" name="email"></sc-input>
      |"""),
    "input-group" -> entry("sc-input-group", "Search docs")("""
      |<sc-input-group style="display:flex;gap:.5rem;align-items:center;width:min(24rem,100%)">
      |  <sc-input style="flex:1" placeholder="Search docs" value="Button"></sc-input>
      |  <sc-button variant="outline">Search</sc-button>
      |</sc-input-group>
      |"""),
    "item" -> entry("sc-item", "Notifications")("""
      |<sc-item variant="outline" size="default" style="width:min(24rem,100%);display:flex;justify-content:space-between;gap:1rem;padding:.75rem 1rem;border:1px solid var(--border);border-radius:.75rem">
      |  <span>Notifications</span>
      |  <sc-badge variant="secondary">3</sc-badge>
      |</sc-item>
      |"""),
    "kbd" -> entry("sc-kbd", "⌘K")("""
      |<sc-kbd style="font-family:ui-monospace,monospace;border:1px solid var(--border);border-radius:.375rem;padding:.15rem .4rem">⌘K</sc-kbd>
      |"""),
    "label" -> entry("sc-label", "Username")("""
      |<sc-label style="font-size:.875rem;font-weight:500">Username</sc-label>
      |"""),
    "native-select" -> entry("sc-native-select", "Pro")("""
      |<sc-native-select style="width:min(16rem,100%)">
      |  <select aria-label="Plan">
      |    <option>Free</option>
      |    <option selected>Pro</option>
      |    <option>Enterprise</option>
      |  </select>
      |</sc-native-select>
      |"""),
    "popover" -> entry("sc-popover", "Open popover")("""
      |<sc-popover>
      |  <sc-button slot="trigger" variant="outline">Open popover</sc-button>
      |  <div slot="content" style="padding:.75rem;min-width:12rem">Popover content with named slots.</div>
      |</sc-popover>
      |"""),
    "progress" -> entry("sc-progress", "72")("""
      |<sc-progress style="width:min(20rem,100%);height:.75rem" value="72" aria-label="Upload progress"></sc-progress>
      |"""),
    "radio" -> entry("sc-radio", "Option A")("""
      |<label style="display:flex;align-items:center;gap:.5rem">
      |  <sc-radio><input type="radio" name="demo" value="a" checked></sc-radio>
      |  Option A
      |</label>
      |"""),
    "radio-group" -> entry("sc-radio-group", "Comfortable")("""
      |<sc-radio-group name="density" value="comfortable" style="width:min(22rem,100%)"
      |  items='[{"value":"compact","label":"Compact","description":"Less padding"},{"value":"comfortable","label":"Comfortable","description":"Default spacing"},{"value":"spacious","label":"Spacious","description":"Roomy layout"}]'>
      |</sc-radio-group>
      |"""),
    "range" -> entry("sc-range", "Volume")("""
      |<sc-range style="width:min(20rem,100%)">
      |  <label style="display:grid;gap:.35rem;font-size:.875rem">Volume
      |    <input type="range" min="0" max="100" value="40" aria-label="Volume">
      |  </label>
      |</sc-range>
      |"""),
    "scrollbar" -> entry("sc-scrollbar", "Scrollable content")("""
      |<sc-scrollbar style="width:min(20rem,100%);height:8rem;overflow:auto;border:1px solid var(--border);border-radius:.5rem;padding:.75rem">
      |  <p>Scrollable content line 1</p>
      |  <p>Scrollable content line 2</p>
      |  <p>Scrollable content line 3</p>
      |  <p>Scrollable content line 4</p>
      |  <p>Scrollable content line 5</p>
      |</sc-scrollbar>
      |"""),
    "select" -> entry("sc-select", "Choose a plan")("""
      |<sc-select style="width:min(18rem,100%)" placeholder="Choose a plan" value="pro"
      |  options='[{"value":"starter","label":"Starter"},{"value":"pro","label":"Pro"},{"value":"enterprise","label":"Enterprise"}]'>
      |</sc-select>
      |"""),
    "separator" -> entry("sc-separator", "separator")("""
      |<div style="width:min(20rem,100%);display:grid;gap:.75rem">
      |  <div>Above</div>
      |  <sc-separator style="width:100%;height:1px;display:block" orientation="horizontal" data-marker="separator"></sc-separator>
      |  <div>Below</div>
      |</div>
      |"""),
    "sidebar" -> entry("sc-sidebar", "Overview")("""
      |<sc-sidebar style="width:min(18rem,100%);height:16rem;max-height:16rem;border:1px solid var(--border);border-radius:.75rem;overflow:auto;display:block"
      |  menus='[{"label":"Platform","items":[{"label":"Overview","active":true},{"label":"Projects"},{"label":"Settings"}]}]'> 
      |</sc-sidebar>
      |"""),
    "skeleton" -> entry("sc-skeleton", "skeleton")("""
      |<sc-skeleton style="width:min(20rem,100%);height:3rem;border-radius:.5rem;display:block;background:var(--muted)" data-marker="skeleton"></sc-skeleton>
      |"""),
    "slider" -> entry("sc-slider", "65")("""
      |<sc-slider style="width:min(20rem,100%);height:1.5rem" value="65" min="0" max="100" step="1" aria-label="Volume"></sc-slider>
      |"""),
    "spinner" -> entry("sc-spinner", "spinner")("""
      |<sc-spinner style="width:2rem;height:2rem;display:inline-block" data-marker="spinner" aria-label="Loading"></sc-spinner>
      |"""),
    "switch" -> entry("sc-switch", "Enable alerts")("""
      |<label style="display:flex;align-items:center;gap:.75rem;font-size:.875rem">
      |  <sc-switch style="width:2.5rem;height:1.5rem" checked aria-label="Enable alerts"></sc-switch>
      |  Enable alerts
      |</label>
      |"""),
    "table" -> entry("sc-table", "Alice")("""
      |<sc-table style="width:min(28rem,100%)">
      |  <table>
      |    <thead is="sc-table-header">
      |      <tr is="sc-table-row">
      |        <th is="sc-table-head">Name</th>
      |        <th is="sc-table-head">Role</th>
      |      </tr>
      |    </thead>
      |    <tbody is="sc-table-body">
      |      <tr is="sc-table-row">
      |        <td is="sc-table-cell">Alice</td>
      |        <td is="sc-table-cell">Admin</td>
      |      </tr>
      |      <tr is="sc-table-row">
      |        <td is="sc-table-cell">Bob</td>
      |        <td is="sc-table-cell">Editor</td>
      |      </tr>
      |    </tbody>
      |  </table>
      |</sc-table>
      |"""),
    "tabs" -> entry("sc-tabs", "Overview")("""
      |<sc-tabs style="width:min(24rem,100%)" value="overview"
      |  items='[{"value":"overview","label":"Overview"},{"value":"analytics","label":"Analytics"},{"value":"reports","label":"Reports"}]'>
      |  <div slot="panel-overview" style="padding:.75rem 0">Your account overview.</div>
      |  <div slot="panel-analytics" style="padding:.75rem 0">Analytics charts live here.</div>
      |  <div slot="panel-reports" style="padding:.75rem 0">Exported reports.</div>
      |</sc-tabs>
      |"""),
    "textarea" -> entry("sc-textarea", "Write a short note")("""
      |<sc-textarea style="width:min(24rem,100%);min-height:6rem" placeholder="Write a short note" value="Ship the docs tonight." name="notes"></sc-textarea>
      |"""),
    "toast" -> entry("sc-toast", "Saved")("""
      |<sc-toast class="block w-full max-w-[22rem] rounded-xl border border-border px-4 py-3">
      |  <strong>Saved</strong>
      |  <div class="text-muted-foreground">Your changes are live.</div>
      |</sc-toast>
      |"""),
    "toggle-group" -> entry("sc-toggle-group", "Center")("""
      |<sc-toggle-group type="single" value="center" variant="outline" size="sm"
      |  items='[{"value":"left","label":"Left"},{"value":"center","label":"Center"},{"value":"right","label":"Right"}]'>
      |</sc-toggle-group>
      |"""),
    "tooltip" -> entry("sc-tooltip", "Hover me")("""
      |<sc-tooltip text="Rendered inside a Shadow Root">
      |  <sc-button variant="secondary">Hover me</sc-button>
      |</sc-tooltip>
      |""")
  )

  val supportedSlugs: Set[String] = entries.keySet
  val catalog: Seq[(String, WebComponentExample)] = entries.toSeq.sortBy(_._1)

  def assertComplete(componentSlugs: Seq[String]): Unit =
    require(
      componentSlugs.forall(slug => apply(slug).forall(example => example.source.contains(example.tag))),
      "WebComponentExamples sources must include their root tag"
    )
    require(catalog.nonEmpty, "WebComponentExamples catalog must not be empty")

  def apply(slug: String): Option[WebComponentExample] = entries.get(slug)

  def unsupportedMessage(slug: String): String =
    if slug == "typography" then "Not applicable: Typography is a Tailwind recipe, not a component wrapper."
    else s"Web Component wrapper for ${slug.replace('-', ' ')} is planned for a future phase."
