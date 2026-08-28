package shadcnscalajs.webcomponents

import org.scalajs.dom

import scala.collection.mutable
import scala.scalajs.js

/** The stylesheets every shadow root adopts, so the ~330 kB Tailwind output is parsed once rather than cloned per
  * instance. Sharing also lets the CSS arrive after the elements are defined — `use` restyles everything.
  *
  * Two sheets, not one: the base sheet holds the tokens, utilities and the style pack baked in at build time, and never
  * changes. The pack sheet holds a pack fetched at runtime, so switching packs re-parses only that pack.
  */
object ScStyles:

  private val propertyRule = """@property\s+(--[\w-]+)\s*\{[^}]*\}""".r
  private val packMarker = """--sc-pack:\s*"([\w-]+)"""".r

  private val baseSheet = construct()
  private val packSheet = construct()

  private var baseCss: Option[String] = None
  private var packCss: Option[String] = None
  private var baked: Option[String] = None
  private val registeredProperties = mutable.Set.empty[String]
  private val fallbackRoots = js.Array[dom.ShadowRoot]()

  def baseHref: String =
    val base = Option(dom.document.documentElement.getAttribute("data-sc-assets-base"))
      .filter(_.nonEmpty)
      .getOrElse(".")
    s"${base.stripSuffix("/")}/sc-components.css"

  // scalajs-dom 2.8.0 doesn't type the Constructable Stylesheets API.
  private def construct(): Option[js.Dynamic] =
    if js.isUndefined(js.Dynamic.global.CSSStyleSheet) then None
    else
      try Some(js.Dynamic.newInstance(js.Dynamic.global.CSSStyleSheet)())
      catch case _: Throwable => None

  def adopt(root: dom.ShadowRoot): Unit =
    (baseSheet, packSheet) match
      case (Some(base), Some(pack)) => root.asInstanceOf[js.Dynamic].adoptedStyleSheets = js.Array(base, pack)
      case _ =>
        fallbackRoots.push(root)
        baseCss.foreach(injectStyleTag(root, "data-sc-base", _))
        packCss.foreach(injectStyleTag(root, "data-sc-pack", _))

  /** Stops tracking a shadow root that has been disconnected, including the no-constructable-stylesheet fallback. */
  def release(root: dom.ShadowRoot): Unit =
    var i = fallbackRoots.length - 1
    while i >= 0 do
      if fallbackRoots(i) eq root then fallbackRoots.splice(i, 1)
      i -= 1

  def use(css: String): Unit =
    baseCss = Some(css)
    baked = packMarker.findFirstMatchIn(css).map(_.group(1))
    registerProperties(css)
    fill(baseSheet, "data-sc-base", css)
    installDocumentSheet(css)
    // Light-DOM compounds (sc-card-*) need pack hooks on the document, including the baked pack, before first paint
    // of upgraded hosts — not only when the user later switches packs.
    baked.foreach(ScPacks.linkDocumentPack)

  /** The pack the bundle was built with, which the base sheet already carries. */
  def bakedPack: Option[String] = baked

  def usePack(css: String): Unit =
    packCss = Some(css)
    registerProperties(css)
    fill(packSheet, "data-sc-pack", css)

  def clearPack(): Unit =
    packCss = Some("")
    fill(packSheet, "data-sc-pack", "")

  def isLoaded: Boolean = baseCss.isDefined

  private def fill(sheet: Option[js.Dynamic], marker: String, css: String): Unit =
    sheet match
      case Some(s) => s.replaceSync(css)
      case None    => fallbackRoots.foreach(injectStyleTag(_, marker, css))

  /** `@property` registrations are only honoured from document-level stylesheets — inside a shadow root they are
    * ignored. Tailwind resolves `border-style` (and 150-odd other declarations) through `var(--tw-border-style)`, so
    * without this every `border` utility computes to `border-style: none` and renders no border at all.
    */
  private def registerProperties(css: String): Unit =
    val rules = propertyRule
      .findAllMatchIn(css)
      .filter(m => registeredProperties.add(m.group(1)))
      .map(_.matched)
      .mkString("\n")
    if rules.nonEmpty then
      val head = dom.document.head
      val style = head.querySelector("style[data-sc-properties]") match
        case null =>
          val created = dom.document.createElement("style")
          created.setAttribute("data-sc-properties", "")
          head.appendChild(created)
          created
        case existing => existing
      style.textContent = style.textContent + "\n" + rules

  private def injectStyleTag(root: dom.ShadowRoot, marker: String, css: String): Unit =
    root.querySelector(s"style[$marker]") match
      case null =>
        val style = dom.document.createElement("style")
        style.setAttribute(marker, "")
        style.textContent = css
        root.appendChild(style)
      case existing => existing.textContent = css

  private def installDocumentSheet(css: String): Unit =
    val head = dom.document.head
    if head != null then
      head.querySelector("style[data-sc-document]") match
        case null =>
          val style = dom.document.createElement("style")
          style.setAttribute("data-sc-document", "")
          style.textContent = css
          head.appendChild(style)
        case existing => existing.textContent = css

/** Mirrors the document theme onto the top-level containers of each shadow root, since ancestor selectors cannot reach
  * across the boundary. One document observer feeds every root.
  */
private[webcomponents] object ScTheme:

  private val roots = mutable.ArrayBuffer.empty[dom.ShadowRoot]
  private val rootObservers = mutable.Map.empty[dom.ShadowRoot, dom.MutationObserver]
  private val hostObservers = mutable.Map.empty[dom.ShadowRoot, dom.MutationObserver]
  private val appliedTokens = mutable.Map.empty[dom.Element, Set[String]]
  private var documentObserver: Option[dom.MutationObserver] = None

  private val menuSelector = ".cn-menu-target, .cn-menu-translucent, [data-menu-translucent]"

  def mirror(root: dom.ShadowRoot): Unit =
    if roots.contains(root) then return
    roots += root
    refresh(root)
    observeRoot(root)
    observeDocument()
    // Pack sync is deferred: calling it from every element upgrade nested hundreds of DOM writes inside one
    // Airstream transaction and blew Transaction.maxDepth when the mosaic mounted all at once.
    if !packSyncScheduled then
      packSyncScheduled = true
      dom.window.setTimeout(
        () => {
          packSyncScheduled = false
          ScPacks.sync()
        },
        0
      )

  def unmirror(root: dom.ShadowRoot): Unit =
    roots -= root
    rootObservers.remove(root).foreach(_.disconnect())
    hostObservers.remove(root).foreach(_.disconnect())
    ScStyles.release(root)
    if roots.isEmpty then
      documentObserver.foreach(_.disconnect())
      documentObserver = None
    val children = root.childNodes
    var i = 0
    while i < children.length do
      children(i) match
        case el: dom.Element => appliedTokens.remove(el)
        case _               => ()
      i += 1

  /** Re-applies document-level token overrides after the public theme API changes them. */
  def refreshAll(): Unit = roots.foreach(refresh)

  private var packSyncScheduled = false
  private def refresh(root: dom.ShadowRoot): Unit =
    val children = root.childNodes
    var i = 0
    while i < children.length do
      children(i) match
        case el: dom.Element =>
          apply(root, el)
          treatMenus(el)
        case _ => ()
      i += 1

  // Only the `dark` class is mirrored, not the whole class attribute: portal containers carry classes of their own that
  // overwriting would drop, and `dark` is the only one the token blocks key off.
  private def apply(root: dom.ShadowRoot, el: dom.Element): Unit =
    val docEl = dom.document.documentElement
    if docEl.classList.contains("dark") then el.classList.add("dark") else el.classList.remove("dark")
    ScThemeState.cssAttributes.foreach { attr =>
      Option(docEl.getAttribute(attr)) match
        case Some(value) => el.setAttribute(attr, value)
        case None        => el.removeAttribute(attr)
    }
    applyTokenOverrides(root, el)

  /** CSS token presets are declared on the shadow wrapper, so inherited custom properties alone cannot override them.
    * Copy explicit inline tokens from `<html>` and then the component host onto that wrapper. This keeps the standard
    * Web Component theming contract (`--primary`, `--radius`, …), with host-level values taking precedence globally.
    */
  private def applyTokenOverrides(root: dom.ShadowRoot, el: dom.Element): Unit =
    val global = customProperties(dom.document.documentElement.asInstanceOf[dom.html.Element])
    val host = root.asInstanceOf[js.Dynamic].host.asInstanceOf[dom.html.Element]
    val local = customProperties(host)
    val tokens = global ++ local
    val previous = appliedTokens.getOrElse(el, Set.empty)
    (previous -- tokens.keySet).foreach(el.asInstanceOf[dom.html.Element].style.removeProperty)
    tokens.foreach((name, tokenValue) => el.asInstanceOf[dom.html.Element].style.setProperty(name, tokenValue))
    appliedTokens(el) = tokens.keySet

  private def customProperties(el: dom.html.Element): Map[String, String] =
    val styles = el.style
    (0 until styles.length)
      .map(styles.item)
      .filter(_.startsWith("--"))
      .map(name => name -> styles.getPropertyValue(name))
      .toMap

  /** Floating content — dropdown menus, select and combobox panels — is portaled into a container appended to the
    * shadow root, making it a *sibling* of the theme host rather than a descendant. Left alone it inherits the light
    * `:host` tokens and matches no pack rule, so a menu stayed white on a dark page. Menus are also built on open,
    * hence `subtree`: the container can appear before the panel inside it.
    */
  private def observeRoot(root: dom.ShadowRoot): Unit =
    val observer = new dom.MutationObserver((records, _) => {
      records.foreach { record =>
        val added = record.addedNodes
        var i = 0
        while i < added.length do
          added(i) match
            case el: dom.Element =>
              if el.parentNode eq root then apply(root, el)
              treatMenus(el)
            case _ => ()
          i += 1
      }
    })
    observer.observe(
      root,
      new dom.MutationObserverInit {
        childList = true
        subtree = true
      }
    )
    rootObservers(root) = observer
    // Host-level inline tokens (`<sc-button style="--primary: ...">`) can change dynamically after mount.
    val host = root.asInstanceOf[js.Dynamic].host.asInstanceOf[dom.Element]
    val hostObserver = new dom.MutationObserver((_, _) => refresh(root))
    hostObserver.observe(
      host,
      new dom.MutationObserverInit {
        attributes = true
        attributeFilter = js.Array("style")
      }
    )
    hostObservers(root) = hostObserver

  /** globals.css ships menus frosted; the site turns that off unless the chosen menu colour asks for translucency, and
    * substitutes `dark` for the inverted options. Without the same pass a plain HTML page gets translucent menus where
    * every page of the site has opaque ones.
    */
  private def treatMenus(el: dom.Element): Unit =
    treatMenu(el)
    val nodes = el.querySelectorAll(menuSelector)
    var i = 0
    while i < nodes.length do
      treatMenu(nodes(i))
      i += 1

  private def treatMenu(el: dom.Element): Unit =
    val menuColor = ScThemeState.menuColor()
    val inverted = menuColor == "inverted" || menuColor == "inverted-translucent"
    val translucent = menuColor == "default-translucent" || menuColor == "inverted-translucent"
    if el.classList.contains("cn-menu-target") then
      if inverted then el.classList.add("dark") else el.classList.remove("dark")
    if el.classList.contains("cn-menu-translucent") || el.hasAttribute("data-menu-translucent") then
      if translucent then
        el.classList.add("cn-menu-translucent")
        el.removeAttribute("data-menu-translucent")
      else
        el.classList.remove("cn-menu-translucent")
        el.setAttribute("data-menu-translucent", "")

  private def observeDocument(): Unit =
    if documentObserver.isEmpty then
      val observer = new dom.MutationObserver((_, _) => {
        roots.foreach(refresh)
        if !packSyncScheduled then
          packSyncScheduled = true
          dom.window.setTimeout(
            () => {
              packSyncScheduled = false
              ScPacks.sync()
            },
            0
          )
      })
      observer.observe(
        dom.document.documentElement,
        new dom.MutationObserverInit {
          attributes = true
          attributeFilter = js.Array("class", "style") ++ ScThemeState.cssAttributes
        }
      )
      documentObserver = Some(observer)

/** Fetches the stylesheet for a style pack the bundle wasn't built with.
  *
  * The site links exactly one pack at a time rather than bundling all eight, because Chromium buckets rules by their
  * rightmost simple selector and eight copies of every rule made full-document recalc an order of magnitude slower. The
  * same reasoning applies here, so a pack switch swaps the pack sheet instead of accumulating packs.
  */
private[webcomponents] object ScPacks:

  private var requested: Option[String] = None

  /** Where the pack stylesheets are served from, overridable for sites that don't host them at the site root. */
  private def base: String =
    Option(dom.document.documentElement.getAttribute("data-sc-pack-base")).getOrElse("/styles")

  def sync(): Unit =
    val pack = Option(dom.document.documentElement.getAttribute("data-style-pack"))
      .orElse(ScStyles.bakedPack)
    pack.foreach(linkDocumentPack)
    val wanted = pack.filter(p => !ScStyles.bakedPack.contains(p))
    if wanted != requested then
      requested = wanted
      wanted match
        case None => ScStyles.clearPack()
        case Some(name) =>
          dom
            .fetch(s"$base/pack-$name.css")
            .`then`[String](res => if res.ok then res.text() else js.Promise.resolve(""))
            .`then`[Unit] { (css: String) =>
              if requested.contains(name) && css.nonEmpty then ScStyles.usePack(css)
            }
            .`catch`(_ => ())

  /** Light-DOM mosaic markup (card shells, field rows) lives outside shadow roots, so it needs the pack on the document
    * as well as inside adopted sheets.
    */
  private[webcomponents] def linkDocumentPack(pack: String): Unit =
    val head = dom.document.head
    if head == null then return
    val href = s"$base/pack-$pack.css"
    val existing = head.querySelector(s"""link[data-sc-pack-doc="$pack"]""")
    if existing != null then return
    val link = dom.document.createElement("link")
    link.setAttribute("data-sc-pack-doc", pack)
    link.setAttribute("rel", "stylesheet")
    link.setAttribute("href", href)
    link.addEventListener(
      "load",
      (_: dom.Event) => {
        // A slower old request may finish after the newer pack. Never let that stale load remove the current sheet.
        val current = Option(dom.document.documentElement.getAttribute("data-style-pack"))
          .orElse(ScStyles.bakedPack)
        if current.contains(pack) then
          val links = head.querySelectorAll("link[data-sc-pack-doc]")
          var i = 0
          while i < links.length do
            val node = links.item(i)
            if node != link && node.parentNode != null then node.parentNode.removeChild(node)
            i += 1
      }
    )
    head.appendChild(link)
