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
      // `contents` so the example root is the previewCanvas flex item (same centering as Laminar).
      cls := "contents",
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
        // Must be invoked as a method. Pulling `loadScComponents` off the object unbinds `this`,
        // and the runtime then throws: Cannot read properties of undefined (reading 'promise').
        val runtime = js.Dynamic.global.ScComponentsRuntime
        if (!js.isUndefined(runtime) && !js.isUndefined(runtime.loadScComponents)) {
          val _ = runtime.loadScComponents(js.Array(tags*))
        }
      }
    )

  private val entries: Map[String, WebComponentExample] = Map(
    "accordion" -> entry("sc-accordion", "What are your shipping options?")("""
      |<div class="w-full max-w-sm">
      |  <sc-accordion
      |    sections='[{"title":"What are your shipping options?","content":"We offer standard (5-7 days), express (2-3 days), and overnight shipping. Free shipping on international orders."},{"title":"What is your return policy?","content":"You can return items within 30 days of delivery. Items must be unused and in their original packaging."},{"title":"How can I contact customer support?","content":"Email support@example.com or use live chat during business hours."}]'>
      |  </sc-accordion>
      |</div>
      |"""),
    "alert" -> entry("sc-alert", "Success! Your changes have been saved")("""
      |<div class="grid w-full max-w-xl items-start gap-4">
      |  <sc-alert>
      |    <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21.801 10A10 10 0 1 1 17 3.335"/><path d="m9 11 3 3L22 4"/></svg>
      |    <sc-alert-title>Success! Your changes have been saved</sc-alert-title>
      |    <sc-alert-description>This is an alert with icon, title and description.</sc-alert-description>
      |  </sc-alert>
      |  <sc-alert>
      |    <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><path d="M12 16v-4M12 8h.01"/></svg>
      |    <sc-alert-title>This Alert has a title and an icon. No description.</sc-alert-title>
      |  </sc-alert>
      |  <sc-alert>
      |    <sc-alert-title>This Alert has no icon</sc-alert-title>
      |    <sc-alert-description>Title and description line up in the same column either way.</sc-alert-description>
      |  </sc-alert>
      |  <sc-alert variant="destructive">
      |    <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><path d="M12 8v4"/><path d="M12 16h.01"/></svg>
      |    <sc-alert-title>Unable to process your payment.</sc-alert-title>
      |    <sc-alert-description>
      |      <p>Please verify your billing information and try again.</p>
      |      <ul class="list-inside list-disc text-sm">
      |        <li>Check your card details</li>
      |        <li>Ensure sufficient funds</li>
      |        <li>Verify billing address</li>
      |      </ul>
      |    </sc-alert-description>
      |  </sc-alert>
      |</div>
      |"""),
    "avatar" -> entry("sc-avatar", "LS")("""
      |<sc-avatar>LS</sc-avatar>
      |"""),
    "badge" -> entry("sc-badge", "New")("""
      |<div class="flex items-center gap-3">
      |  <sc-badge>New</sc-badge>
      |  <sc-badge variant="secondary">Beta</sc-badge>
      |  <sc-badge variant="outline">Outline</sc-badge>
      |</div>
      |"""),
    "breadcrumb" -> entry("sc-breadcrumb", "Components")("""
      |<sc-breadcrumb>
      |  <a href="/">Home</a><span>/</span><span>Components</span>
      |</sc-breadcrumb>
      |"""),
    "button" -> entry("sc-button", "Primary")("""
      |<div class="flex flex-wrap items-center gap-3">
      |  <sc-button variant="primary">Primary</sc-button>
      |  <sc-button variant="outline">Outline</sc-button>
      |  <sc-button variant="destructive">Delete</sc-button>
      |</div>
      |"""),
    "button-group" -> entry("sc-button-group", "Back")("""
      |<sc-button-group>
      |  <sc-button variant="outline">Back</sc-button>
      |  <sc-button variant="outline">Next</sc-button>
      |</sc-button-group>
      |"""),
    "calendar" -> entry("sc-calendar", "sc-calendar")("""
      |<sc-calendar aria-label="Choose date"></sc-calendar>
      |"""),
    "card" -> entry("sc-card", "Project update")("""
      |<sc-card class="w-full max-w-sm">
      |  <sc-card-header>
      |    <sc-card-title>Project update</sc-card-title>
      |    <sc-card-description>A Card composed from Laminar primitives.</sc-card-description>
      |  </sc-card-header>
      |  <sc-card-content>Your latest deployment is ready.</sc-card-content>
      |</sc-card>
      |"""),
    "chart" -> entry("sc-chart", "Jan")("""
      |<sc-chart type="bar"
      |  data='[["Jan",40],["Feb",65],["Mar",48],["Apr",80]]'
      |  tooltip-label="Revenue" show-labels>
      |</sc-chart>
      |"""),
    "checkbox" -> entry("sc-checkbox", "Accept terms")("""
      |<label class="flex items-center gap-3 text-sm">
      |  <sc-checkbox id="terms" aria-label="Accept terms"></sc-checkbox>
      |  Accept terms
      |</label>
      |"""),
    "collapsible" -> entry("sc-collapsible", "Show details")("""
      |<sc-collapsible>
      |  <button slot="trigger" type="button">Show details</button>
      |  <div slot="content" class="pt-2 text-sm text-muted-foreground">This is native details content.</div>
      |</sc-collapsible>
      |"""),
    "combobox" -> entry("sc-combobox", "Select framework")("""
      |<div class="flex w-full max-w-sm flex-col gap-4">
      |  <sc-combobox placeholder="Select framework…"
      |    items='[{"value":"next.js","label":"Next.js"},{"value":"sveltekit","label":"SvelteKit"},{"value":"nuxt.js","label":"Nuxt.js"},{"value":"remix","label":"Remix"},{"value":"astro","label":"Astro"}]'>
      |  </sc-combobox>
      |  <sc-combobox placeholder="Select frameworks…"
      |    items='[{"value":"next.js","label":"Next.js"},{"value":"sveltekit","label":"SvelteKit"},{"value":"nuxt.js","label":"Nuxt.js"},{"value":"remix","label":"Remix"},{"value":"astro","label":"Astro"}]'>
      |  </sc-combobox>
      |</div>
      |"""),
    "command" -> entry("sc-command", "Type a command or search")("""
      |<sc-command class="w-full max-w-sm border">
      |  <input placeholder="Type a command or search…" />
      |  <div>Suggestions</div>
      |  <div>Calendar</div>
      |  <div>Search Emoji</div>
      |  <div>Launch</div>
      |  <div>Settings</div>
      |  <div>Profile</div>
      |  <div>Billing</div>
      |</sc-command>
      |"""),
    "dialog" -> entry("sc-dialog", "Open dialog")("""
      |<div data-sc-dialog-demo class="grid justify-items-start gap-3">
      |  <sc-button id="open-dialog">Open dialog</sc-button>
      |  <sc-dialog id="demo-dialog">
      |    <div class="grid min-w-72 gap-3">
      |      <strong>Edit profile</strong>
      |      <p class="m-0 text-muted-foreground">Make changes to your profile here. Click save when you're done.</p>
      |      <div class="flex gap-2">
      |        <sc-button id="cancel-dialog" variant="outline">Cancel</sc-button>
      |        <sc-button id="close-dialog" variant="primary">Save changes</sc-button>
      |      </div>
      |    </div>
      |  </sc-dialog>
      |</div>
      |<script>
      |  (() => {
      |    const root = document.currentScript?.previousElementSibling || document.querySelector('[data-sc-dialog-demo]')
      |    const dialog = root?.querySelector('#demo-dialog') || document.getElementById('demo-dialog')
      |    const openBtn = root?.querySelector('#open-dialog') || document.getElementById('open-dialog')
      |    const closeBtn = root?.querySelector('#close-dialog') || document.getElementById('close-dialog')
      |    const cancelBtn = root?.querySelector('#cancel-dialog') || document.getElementById('cancel-dialog')
      |    openBtn?.addEventListener('click', () => dialog?.setAttribute('open', ''))
      |    closeBtn?.addEventListener('click', () => dialog?.removeAttribute('open'))
      |    cancelBtn?.addEventListener('click', () => dialog?.removeAttribute('open'))
      |  })()
      |</script>
      |"""),
    "dropdown-menu" -> entry("sc-dropdown-menu", "Open menu")("""
      |<sc-dropdown-menu items='[{"label":"Profile"},{"label":"Billing"},{"label":"Status Bar"},{"label":"Log out"}]'>
      |  <sc-button slot="trigger" variant="outline">Open menu</sc-button>
      |</sc-dropdown-menu>
      |"""),
    "empty" -> entry("sc-empty", "No projects")("""
      |<sc-empty class="w-full max-w-sm">
      |  <strong>No projects</strong>
      |  <p class="mt-2 text-muted-foreground">Create your first project to get started.</p>
      |</sc-empty>
      |"""),
    "field" -> entry("sc-field", "Email")("""
      |<div class="flex w-full max-w-sm flex-col gap-6">
      |  <sc-field>
      |    <sc-label>Email</sc-label>
      |    <sc-input placeholder="you@example.com"></sc-input>
      |    <p class="text-sm text-muted-foreground">We will never share your email.</p>
      |  </sc-field>
      |  <sc-field>
      |    <sc-label>Password</sc-label>
      |    <sc-input type="password"></sc-input>
      |    <p role="alert" class="text-sm text-destructive">Password must be at least 8 characters.</p>
      |  </sc-field>
      |  <sc-field>
      |    <sc-label>Username</sc-label>
      |    <sc-input placeholder="jane"></sc-input>
      |    <ul role="alert" class="ml-4 flex list-disc flex-col gap-1 text-sm text-destructive">
      |      <li>Username is required.</li>
      |      <li>Username must be unique.</li>
      |    </ul>
      |  </sc-field>
      |</div>
      |"""),
    "form" -> entry("sc-form", "Submit")("""
      |<sc-form class="grid w-full max-w-sm gap-3">
      |  <sc-field>
      |    <sc-label>Email</sc-label>
      |    <sc-input type="email" name="email" placeholder="you@example.com"></sc-input>
      |    <p class="text-sm text-muted-foreground">We'll never share your email.</p>
      |  </sc-field>
      |  <sc-button variant="primary">Submit</sc-button>
      |</sc-form>
      |"""),
    "input" -> entry("sc-input", "Type something")("""
      |<sc-input class="max-w-sm" placeholder="Type something…"></sc-input>
      |"""),
    "input-group" -> entry("sc-input-group", "example.com")("""
      |<div class="flex w-full max-w-sm flex-col gap-4">
      |  <sc-input-group class="flex items-center">
      |    <span class="px-3 text-sm text-muted-foreground">https://</span>
      |    <sc-input placeholder="example.com"></sc-input>
      |  </sc-input-group>
      |  <sc-input-group class="flex flex-col">
      |    <span class="px-3 pt-2 text-sm text-muted-foreground">Description</span>
      |    <sc-textarea placeholder="Enter your message…"></sc-textarea>
      |    <span class="px-3 pb-2 text-xs text-muted-foreground">Markdown supported</span>
      |  </sc-input-group>
      |</div>
      |"""),
    "item" -> entry("sc-item", "Laminar")("""
      |<sc-item class="w-full max-w-sm border">
      |  <div>
      |    <div>Laminar</div>
      |    <div class="text-sm text-muted-foreground">Reactive Scala.js UI</div>
      |  </div>
      |  <sc-badge>Stable</sc-badge>
      |</sc-item>
      |"""),
    "kbd" -> entry("sc-kbd", "⌘K")("""
      |<div class="flex items-center gap-3">
      |  <sc-kbd>⌘K</sc-kbd>
      |  <span class="inline-flex items-center gap-1"><sc-kbd>⌘</sc-kbd><sc-kbd>P</sc-kbd></span>
      |</div>
      |"""),
    "label" -> entry("sc-label", "Email address")("""
      |<div class="flex w-full max-w-sm flex-col gap-2">
      |  <sc-label>Email address</sc-label>
      |  <sc-input placeholder="you@example.com"></sc-input>
      |</div>
      |"""),
    "native-select" -> entry("sc-native-select", "Choose a plan")("""
      |<sc-native-select class="max-w-sm">
      |  <select aria-label="Plan">
      |    <option>Choose a plan</option>
      |    <option>Pro</option>
      |    <option>Team</option>
      |  </select>
      |</sc-native-select>
      |"""),
    "popover" -> entry("sc-popover", "Open popover")("""
      |<sc-popover>
      |  <sc-button slot="trigger" variant="outline">Open popover</sc-button>
      |  <div slot="content" class="grid w-80 gap-4 p-4">
      |    <div>
      |      <div class="font-medium">Dimensions</div>
      |      <p class="text-sm text-muted-foreground">Set the dimensions for the layer.</p>
      |    </div>
      |    <label class="grid grid-cols-3 items-center gap-4 text-sm">Width
      |      <input class="col-span-2 h-8 rounded-md border px-2" value="100%">
      |    </label>
      |    <label class="grid grid-cols-3 items-center gap-4 text-sm">Max. width
      |      <input class="col-span-2 h-8 rounded-md border px-2" value="300px">
      |    </label>
      |    <label class="grid grid-cols-3 items-center gap-4 text-sm">Height
      |      <input class="col-span-2 h-8 rounded-md border px-2" value="25px">
      |    </label>
      |    <label class="grid grid-cols-3 items-center gap-4 text-sm">Max. height
      |      <input class="col-span-2 h-8 rounded-md border px-2" value="none">
      |    </label>
      |  </div>
      |</sc-popover>
      |"""),
    "progress" -> entry("sc-progress", "68")("""
      |<sc-progress class="w-full max-w-sm" value="68" aria-label="Upload progress"></sc-progress>
      |"""),
    "radio" -> entry("sc-radio", "Pro")("""
      |<div class="flex items-center gap-3">
      |  <label class="flex items-center gap-2 text-sm">
      |    <sc-radio><input type="radio" name="plan" value="pro" checked></sc-radio>
      |    Pro
      |  </label>
      |  <label class="flex items-center gap-2 text-sm">
      |    <sc-radio><input type="radio" name="plan" value="team"></sc-radio>
      |    Team
      |  </label>
      |</div>
      |"""),
    "radio-group" -> entry("sc-radio-group", "Pro")("""
      |<sc-radio-group name="plan" value="pro"
      |  items='[{"value":"pro","label":"Pro"},{"value":"team","label":"Team"}]'>
      |</sc-radio-group>
      |"""),
    "range" -> entry("sc-range", "50")("""
      |<sc-range class="max-w-sm">
      |  <input type="range" min="0" max="100" value="50" aria-label="Range">
      |</sc-range>
      |"""),
    "scrollbar" -> entry("sc-scrollbar", "Scrollable content")("""
      |<sc-scrollbar class="h-32 w-full max-w-sm overflow-auto rounded-md border p-3">
      |  <p>Scrollable content</p>
      |  <div style="height:12rem"></div>
      |  <p>End</p>
      |</sc-scrollbar>
      |"""),
    "select" -> entry("sc-select", "Choose a plan")("""
      |<div class="w-full max-w-sm">
      |  <sc-select placeholder="Choose a plan"
      |    options='[{"value":"free","label":"Free"},{"value":"pro","label":"Pro"},{"value":"team","label":"Team"},{"value":"enterprise","label":"Enterprise"}]'>
      |  </sc-select>
      |</div>
      |"""),
    "separator" -> entry("sc-separator", "Section one")("""
      |<div class="flex w-full max-w-sm flex-col gap-4">
      |  <p class="text-sm">Section one</p>
      |  <sc-separator orientation="horizontal" data-marker="separator"></sc-separator>
      |  <p class="text-sm">Section two</p>
      |</div>
      |"""),
    "sidebar" -> entry("sc-sidebar", "Overview")("""
      |<sc-sidebar class="h-48 w-full max-w-sm"
      |  menus='[{"label":"Navigation","items":[{"label":"Overview","active":true},{"label":"Settings"}]}]'>
      |</sc-sidebar>
      |"""),
    "skeleton" -> entry("sc-skeleton", "skeleton")("""
      |<sc-skeleton class="h-20 w-full max-w-sm" data-marker="skeleton"></sc-skeleton>
      |"""),
    "slider" -> entry("sc-slider", "sc-slider")("""
      |<sc-slider class="w-full max-w-sm" min="0" max="100" step="1" aria-label="Volume"></sc-slider>
      |"""),
    "spinner" -> entry("sc-spinner", "spinner")("""
      |<sc-spinner data-marker="spinner" aria-label="Loading"></sc-spinner>
      |"""),
    "switch" -> entry("sc-switch", "Enabled")("""
      |<div class="flex items-center gap-3">
      |  <sc-switch checked aria-label="Enabled"></sc-switch>
      |  <span class="text-sm text-muted-foreground">Enabled</span>
      |</div>
      |"""),
    "table" -> entry("sc-table", "Drawer")("""
      |<sc-table>
      |  <table>
      |    <thead is="sc-table-header">
      |      <tr is="sc-table-row">
      |        <th is="sc-table-head">Component</th>
      |        <th is="sc-table-head">Status</th>
      |      </tr>
      |    </thead>
      |    <tbody is="sc-table-body">
      |      <tr is="sc-table-row">
      |        <td is="sc-table-cell">Drawer</td>
      |        <td is="sc-table-cell"><sc-badge>Ready</sc-badge></td>
      |      </tr>
      |      <tr is="sc-table-row">
      |        <td is="sc-table-cell">Dialog</td>
      |        <td is="sc-table-cell"><sc-badge variant="secondary">Native</sc-badge></td>
      |      </tr>
      |    </tbody>
      |  </table>
      |</sc-table>
      |"""),
    "tabs" -> entry("sc-tabs", "Overview")("""
      |<div class="flex w-full max-w-md flex-col gap-6">
      |  <div class="flex flex-col gap-2">
      |    <p class="text-sm font-medium">Default</p>
      |    <sc-tabs value="overview"
      |      items='[{"value":"overview","label":"Overview"},{"value":"usage","label":"Usage"}]'>
      |      <div slot="panel-overview" class="text-sm text-muted-foreground">Overview panel content.</div>
      |      <div slot="panel-usage" class="text-sm text-muted-foreground">Usage panel content.</div>
      |    </sc-tabs>
      |  </div>
      |  <div class="flex flex-col gap-2">
      |    <p class="text-sm font-medium">Line</p>
      |    <sc-tabs value="overview"
      |      items='[{"value":"overview","label":"Overview"},{"value":"usage","label":"Usage"}]'>
      |      <div slot="panel-overview" class="text-sm text-muted-foreground">Overview panel content.</div>
      |      <div slot="panel-usage" class="text-sm text-muted-foreground">Usage panel content.</div>
      |    </sc-tabs>
      |  </div>
      |</div>
      |"""),
    "textarea" -> entry("sc-textarea", "Write a message")("""
      |<sc-textarea class="max-w-sm" placeholder="Write a message…"></sc-textarea>
      |"""),
    "toast" -> entry("sc-toast", "Saved")("""
      |<sc-toast>
      |  <strong>Saved</strong>
      |  <div class="text-sm opacity-90">Everything is up to date.</div>
      |</sc-toast>
      |"""),
    "toggle-group" -> entry("sc-toggle-group", "Star")("""
      |<sc-toggle-group type="multiple" variant="outline" size="sm"
      |  items='[{"value":"star","label":"Star"},{"value":"heart","label":"Heart"},{"value":"bookmark","label":"Bookmark"}]'>
      |</sc-toggle-group>
      |"""),
    "tooltip" -> entry("sc-tooltip", "Hover me")("""
      |<sc-tooltip text="Helpful context">
      |  <span>Hover me</span>
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
