package shadcnscalajs.webcomponents

import org.scalajs.dom
import shadcnscalajs.ui.Trace

import scala.scalajs.js

/** `<sc-trace>` and its parts — light-DOM hosts, so a line wrapped in the page's own `<button>` or `<a>` picks up
  * upstream's `in-[a,button]` hover styles exactly as in Laminar.
  *
  * {{{
  * <sc-trace>
  *   <sc-trace-line><sc-trace-title>Searched the web</sc-trace-title><sc-trace-description>4 results</sc-trace-description></sc-trace-line>
  *   <sc-trace-collapsible open>
  *     <sc-trace-collapsible-trigger><sc-trace-line>…</sc-trace-line></sc-trace-collapsible-trigger>
  *     <sc-trace-collapsible-content>Details</sc-trace-collapsible-content>
  *   </sc-trace-collapsible>
  * </sc-trace>
  * }}}
  */
class ScTraceRoot extends LightPrimitive("trace", Trace.rootClass)

class ScTraceLine extends LightVariantPrimitive("trace-line"):
  protected def classes(attr: String => Option[String]): String = Trace.lineClass
  override protected def stamps(attr: String => Option[String]): Seq[(String, String)] =
    attr("status").filter(Set("running", "error", "pending")).map("data-status" -> _).toSeq

  // `stamps` only adds; dropping the attribute must clear the mirrored status too.
  override protected def sync(): Unit =
    if !Option(this.getAttribute("status")).exists(Set("running", "error", "pending")) then
      this.removeAttribute("data-status")
    super.sync()

class ScTraceIcon extends LightPrimitive("trace-icon", Trace.iconClass):
  this.setAttribute("aria-hidden", "true")

class ScTraceTitle extends LightPrimitive("trace-title", Trace.titleClass)
class ScTraceDescription extends LightPrimitive("trace-description", Trace.descriptionClass)

class ScTraceCollapsibleTrigger extends LightPrimitive("trace-collapsible-trigger", Trace.collapsibleTriggerClass)

class ScTraceCollapsibleContent
    extends LightPrimitive(
      "trace-collapsible-content",
      "block min-w-0 overflow-hidden transition-[max-height] duration-200 ease-out"
    )

/** Owns the open state. It finds its trigger and content among its direct children, makes the trigger a keyboard button
  * with `aria-expanded`, appends the rotating chevron, and animates the content's height — the same behaviour as
  * `Trace.collapsible`. User toggles emit `sc-change` with `{ open }`; setting `open` from outside stays silent.
  */
class ScTraceCollapsible extends LightVariantPrimitive("trace-collapsible"):
  protected def classes(attr: String => Option[String]): String = Trace.collapsibleClass
  override protected def stamps(attr: String => Option[String]): Seq[(String, String)] =
    Seq("data-state" -> (if this.hasAttribute("open") then "open" else "closed"))

  private var wired = false

  js.Dynamic.global.Object.defineProperty(
    this,
    "open",
    js.Dynamic.literal(
      configurable = true,
      get = (() => this.hasAttribute("open")): js.Function0[Boolean],
      set = (
          (v: js.Any) =>
            if js.DynamicImplicits.truthValue(v.asInstanceOf[js.Dynamic]) then this.setAttribute("open", "")
            else this.removeAttribute("open")
      ): js.Function1[js.Any, Unit]
    )
  )

  private def child(tag: String): Option[dom.HTMLElement] =
    Option(this.querySelector(s":scope > $tag")).map(_.asInstanceOf[dom.HTMLElement])

  private def trigger = child("sc-trace-collapsible-trigger")
  private def content = child("sc-trace-collapsible-content")

  private def toggle(): Unit =
    if this.hasAttribute("open") then this.removeAttribute("open") else this.setAttribute("open", "")
    this.dispatchEvent(
      new dom.CustomEvent(
        "sc-change",
        js.Dynamic
          .literal(detail = js.Dynamic.literal(open = this.hasAttribute("open")), bubbles = true, composed = true)
          .asInstanceOf[dom.CustomEventInit]
      )
    )

  private var pending: Option[dom.MutationObserver] = None

  private def wire(): Unit =
    if !wired && (trigger.isEmpty || content.isEmpty) && pending.isEmpty then
      // Upgraded while the parser is still streaming the children in: wire once both have arrived.
      val observer = new dom.MutationObserver((_, obs) =>
        if trigger.nonEmpty && content.nonEmpty then
          obs.disconnect()
          pending = None
          wire()
          sync()
      )
      observer.observe(this, new dom.MutationObserverInit { childList = true })
      pending = Some(observer)
    if !wired && trigger.nonEmpty && content.nonEmpty then
      wired = true
      trigger.foreach { t =>
        t.setAttribute("role", "button")
        if !t.hasAttribute("tabindex") then t.setAttribute("tabindex", "0")
        if t.querySelector(":scope > [data-sc-trace-chevron]") == null then
          val icon = dom.document.createElement("span")
          icon.setAttribute("data-slot", "trace-icon")
          icon.setAttribute("data-sc-trace-chevron", "")
          icon.setAttribute("aria-hidden", "true")
          icon.setAttribute("class", s"${Trace.iconClass} ${Trace.chevronClass}")
          icon.innerHTML =
            """<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="size-4"><path d="m9 18 6-6-6-6"/></svg>"""
          t.appendChild(icon)
        t.addEventListener("click", (_: dom.Event) => toggle())
        t.addEventListener(
          "keydown",
          (ev: dom.KeyboardEvent) =>
            if ev.key == "Enter" || ev.key == " " then
              ev.preventDefault()
              toggle()
        )
      }
      content.foreach { c =>
        // The inner wrapper carries the indent and rule; margins on the clipped host would show while closed.
        if c.querySelector(":scope > [data-slot=trace-collapsible-content-inner]") == null then
          val inner = dom.document.createElement("div")
          inner.setAttribute("data-slot", "trace-collapsible-content-inner")
          inner.setAttribute("class", Trace.collapsibleContentClass)
          while c.firstChild != null do inner.appendChild(c.firstChild)
          c.appendChild(inner)
      }

  override protected def sync(): Unit =
    super.sync()
    val open = this.hasAttribute("open")
    trigger.foreach(_.setAttribute("aria-expanded", open.toString))
    content.foreach { c =>
      if open then
        c.style.maxHeight = s"${c.scrollHeight}px"
        c.removeAttribute("aria-hidden")
        // Release the cap once expanded, so content that grows later is not clipped.
        js.timers.setTimeout(220)(if this.hasAttribute("open") then c.style.maxHeight = "none")
      else
        if c.style.maxHeight == "none" then
          c.style.maxHeight = s"${c.scrollHeight}px"
          val _ = c.offsetHeight // commit the start height so the collapse animates
        c.style.maxHeight = "0px"
        c.setAttribute("aria-hidden", "true")
    }

  override def connectedCallback(): Unit =
    wire()
    super.connectedCallback()

object ScTrace:
  def register(): Unit =
    ScElements.define("sc-trace", js.constructorOf[ScTraceRoot])
    ScElements.define("sc-trace-line", js.constructorOf[ScTraceLine], "status")
    ScElements.define("sc-trace-icon", js.constructorOf[ScTraceIcon])
    ScElements.define("sc-trace-title", js.constructorOf[ScTraceTitle])
    ScElements.define("sc-trace-description", js.constructorOf[ScTraceDescription])
    ScElements.define("sc-trace-collapsible-trigger", js.constructorOf[ScTraceCollapsibleTrigger])
    ScElements.define("sc-trace-collapsible-content", js.constructorOf[ScTraceCollapsibleContent])
    ScElements.define("sc-trace-collapsible", js.constructorOf[ScTraceCollapsible], "open")
