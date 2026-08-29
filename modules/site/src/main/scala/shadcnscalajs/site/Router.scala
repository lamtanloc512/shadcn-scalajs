package shadcnscalajs.site

import com.raquo.laminar.api.L.*
import org.scalajs.dom

import scala.scalajs.js

/** Client-side navigation for what used to be a plain multi-page app.
  *
  * Every internal link was a real page load, which meant re-parsing and re-executing the whole bundle (~1.2 MB of
  * JavaScript) plus both stylesheets (~283 kB of base CSS and a ~400 kB style pack) to redraw a page whose header and
  * sidebar had not changed. Measured on the preview build at 4x CPU throttle, that left the viewport blank from ~26 ms
  * after the click until ~211 ms. Keeping the document alive and swapping only the routed subtree removes all of it.
  *
  * The server still serves `index.html` for every path, so deep links, refresh, and opening a link in a new tab keep
  * working exactly as before — this only takes over the in-page case.
  */
object Router:

  enum Route derives CanEqual:
    case Landing
    case ComponentsIndex
    case Component(slug: String)
    case BlocksIndex
    case Block(name: String)
    case BlockPreview(name: String)
    case Create
    case CreatePreview
    case WebComponents

  /** `/create` has no page of its own; it is the customizer's canonical entry and normalises to `/create/preview-02`.
    */
  private val CreatePath = "/create/preview-02"

  def parse(pathname: String): Route =
    if pathname == "/web-components" || pathname == "/web-components/" then
      if SiteFeatures.webComponents then Route.WebComponents else Route.ComponentsIndex
    else if pathname == "/components" || pathname == "/components/" then Route.ComponentsIndex
    else if pathname.startsWith("/components/") then
      Route.Component(pathname.stripPrefix("/components/").stripSuffix("/"))
    else if pathname.startsWith("/blocks/") && pathname.endsWith("/preview") then
      Route.BlockPreview(pathname.stripPrefix("/blocks/").stripSuffix("/preview"))
    else if pathname == "/blocks" || pathname == "/blocks/" then Route.BlocksIndex
    else if pathname.startsWith("/blocks/") then Route.Block(pathname.stripPrefix("/blocks/").stripSuffix("/"))
    else if pathname == "/create" || pathname == "/create/" || pathname == CreatePath then Route.Create
    else if pathname == "/preview/preview-02" then Route.CreatePreview
    else Route.Landing

  private val initialRoute = parse(dom.window.location.pathname)
  if !SiteFeatures.webComponents && dom.window.location.pathname.startsWith("/web-components") then
    dom.window.history.replaceState(null, "", "/components")

  private val currentVar = Var(initialRoute)

  /** Distinct because a hash jump or a repeated link click writes the same route back, and an undeduplicated `Var`
    * would emit anyway — rebuilding the whole page to land on an anchor already on screen.
    */
  val current: Signal[Route] = currentVar.signal.distinct

  def now: Route = currentVar.now()

  private var installed = false

  /** Takes over internal link clicks and the back/forward buttons. Safe to call more than once. */
  def install(): Unit =
    if installed then return
    installed = true

    // Chrome restores the previous scroll offset on its own schedule, which fights the restore below.
    dom.window.history.asInstanceOf[js.Dynamic].scrollRestoration = "manual"

    if dom.window.location.pathname == "/create" || dom.window.location.pathname == "/create/" then
      replaceUrl(CreatePath + dom.window.location.search + dom.window.location.hash)

    dom.window.addEventListener(
      "popstate",
      (ev: dom.PopStateEvent) => {
        currentVar.set(parse(dom.window.location.pathname))
        val restored = scrollOf(ev.state)
        afterRender(() => dom.window.scrollTo(0, restored))
      }
    )

    // Capture phase, so a handler that stops propagation on the way up cannot strand a link on the old page.
    dom.document.addEventListener(
      "click",
      (ev: dom.MouseEvent) => handleClick(ev),
      useCapture = true
    )

  def navigate(href: String): Unit =
    val url = new dom.URL(href, dom.window.location.href)
    if url.pathname == dom.window.location.pathname && url.hash.nonEmpty then
      jumpToHash(url.hash)
      return

    rememberScroll()
    val target = if url.pathname == "/create" || url.pathname == "/create/" then CreatePath else url.pathname
    dom.window.history.pushState(
      js.Dynamic.literal(scrollY = 0),
      "",
      target + url.search + url.hash
    )
    currentVar.set(parse(target))
    afterRender(() => if url.hash.nonEmpty then scrollToHash(url.hash) else dom.window.scrollTo(0, 0))

  /** Chrome stops scrolling to fragments itself once `scrollRestoration` is `manual`, so the table of contents and
    * every other in-page anchor has to be moved here by hand.
    */
  private def jumpToHash(hash: String): Unit =
    rememberScroll()
    scrollToHash(hash)
    dom.window.history.pushState(js.Dynamic.literal(scrollY = dom.window.scrollY.toInt), "", hash)

  private def replaceUrl(to: String): Unit =
    dom.window.history.replaceState(js.Dynamic.literal(scrollY = dom.window.scrollY.toInt), "", to)

  /** Parks the offset on the entry being left, so back returns to where the reader actually was. */
  private def rememberScroll(): Unit =
    dom.window.history.replaceState(
      js.Dynamic.literal(scrollY = dom.window.scrollY.toInt),
      "",
      dom.window.location.href
    )

  private def scrollOf(state: js.Any): Int =
    if state == null then 0
    else state.asInstanceOf[js.Dynamic].selectDynamic("scrollY").asInstanceOf[js.UndefOr[Int]].getOrElse(0)

  private def scrollToHash(hash: String): Unit =
    val target = dom.document.querySelector(hash)
    if target != null then target.scrollIntoView() else dom.window.scrollTo(0, 0)

  /** Laminar renders synchronously when the route Var changes, but layout has not settled until the next frame. */
  private def afterRender(f: () => Unit): Unit =
    dom.window.requestAnimationFrame(_ => f())
    ()

  private def handleClick(ev: dom.MouseEvent): Unit =
    if ev.defaultPrevented || ev.button != 0 then return
    if ev.metaKey || ev.ctrlKey || ev.shiftKey || ev.altKey then return

    val anchor = anchorFor(ev)
    if anchor == null then return
    if anchor.hasAttribute("download") then return
    val linkTarget = anchor.getAttribute("target")
    if linkTarget != null && linkTarget.nonEmpty && linkTarget != "_self" then return

    val raw = anchor.getAttribute("href")
    if raw == null || raw.isEmpty then return

    val url = new dom.URL(anchor.href, dom.window.location.href)
    if url.origin != dom.window.location.origin then return

    if url.pathname == dom.window.location.pathname && url.hash.nonEmpty then
      ev.preventDefault()
      jumpToHash(url.hash)
      return

    // Anything this router does not recognise is left to the server: it may well be a real file.
    if !owns(url.pathname) then return

    ev.preventDefault()
    navigate(url.pathname + url.search + url.hash)

  /** Paths the app renders itself. Unknown paths fall through to a real navigation rather than silently landing. */
  private def owns(pathname: String): Boolean =
    pathname == "/" ||
      pathname.startsWith("/components") ||
      pathname.startsWith("/blocks") ||
      pathname.startsWith("/create") ||
      pathname == "/preview/preview-02" ||
      pathname.startsWith("/web-components")

  private def anchorFor(ev: dom.MouseEvent): dom.html.Anchor =
    val path = ev.asInstanceOf[js.Dynamic].composedPath().asInstanceOf[js.Array[dom.EventTarget]]
    var i = 0
    var found: dom.html.Anchor = null
    while i < path.length && found == null do
      path(i) match
        case el: dom.html.Anchor => found = el
        case _                   => ()
      i += 1
    found
