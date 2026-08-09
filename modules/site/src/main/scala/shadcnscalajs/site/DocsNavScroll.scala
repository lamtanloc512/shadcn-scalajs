package shadcnscalajs.site

import com.raquo.laminar.api.L.*
import org.scalajs.dom

/** Keeps the component docs sidebar scrolled where the reader left it.
  *
  * The site is router-less — every nav link is a real page load — so the sidebar list, which is roughly three viewports
  * tall, would otherwise come back scrolled to the top on each click and lose the reader's place in the alphabet.
  */
object DocsNavScroll:

  private val StorageKey = "shadcn-scalajs.docs-nav-scroll"

  /** Attach to the scrollable nav element. */
  def preserve: Modifier[HtmlElement] = Modifier { el =>
    var frame = 0
    el.amend(
      onMountCallback { ctx =>
        val node = ctx.thisNode.ref
        restore(node)
        // Fonts and late layout can still change the list height, which clamps an early scrollTop write.
        dom.window.requestAnimationFrame(_ => restore(node))
      },
      onScroll --> { ev =>
        val node = ev.target.asInstanceOf[dom.html.Element]
        if frame == 0 then
          frame = dom.window.requestAnimationFrame { _ =>
            frame = 0
            save(node)
          }
      },
      // A click navigates away before a pending frame runs, so flush the offset synchronously.
      onClick --> { ev => save(ev.currentTarget.asInstanceOf[dom.html.Element]) }
    )
  }

  private def storage: Option[dom.Storage] =
    try Option(dom.window.sessionStorage)
    catch case _: Throwable => None

  private def save(el: dom.html.Element): Unit =
    storage.foreach(_.setItem(StorageKey, el.scrollTop.toString))

  private def restore(el: dom.html.Element): Unit =
    storage
      .flatMap(s => Option(s.getItem(StorageKey)))
      .flatMap(_.toDoubleOption)
      .foreach(top => el.scrollTop = top)
    // Arriving from prev/next, search, or a pasted URL leaves the current page's link outside the restored window.
    if !activeInView(el) then centerActive(el)

  private def activeLink(el: dom.html.Element): Option[dom.html.Element] =
    Option(el.querySelector("[aria-current='page']")).map(_.asInstanceOf[dom.html.Element])

  private def activeInView(el: dom.html.Element): Boolean =
    activeLink(el).fold(true) { link =>
      val view = el.getBoundingClientRect()
      val rect = link.getBoundingClientRect()
      rect.top >= view.top && rect.bottom <= view.bottom
    }

  private def centerActive(el: dom.html.Element): Unit =
    activeLink(el).foreach { link =>
      val view = el.getBoundingClientRect()
      val rect = link.getBoundingClientRect()
      el.scrollTop = el.scrollTop + (rect.top - view.top) - (view.height - rect.height) / 2
    }
