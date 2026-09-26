package shadcnscalajs.ui

import com.raquo.laminar.api.L.*
import org.scalajs.dom

import scala.collection.mutable
import scala.scalajs.js

/** shadcn/ui NavigationMenu — pure Laminar open-state machine with Tailwind/`cn-*` styling matching the canonical
  * navigation-menu parts. Open item is tracked per root via `Var[Option[String]]`. With `viewport = true` (default),
  * panels render into a shared viewport under the root; with `viewport = false`, each [[content]] is absolutely
  * positioned under its item.
  */
object NavigationMenu:

  private final class RootState(
      val openVar: Var[Option[String]],
      val viewport: Boolean,
      val contents: mutable.Map[String, HtmlElement]
  ):
    def toggle(value: String): Unit =
      openVar.update(_.fold(Some(value))(cur => if cur == value then None else Some(value)))

  private val roots = mutable.Map.empty[dom.html.Element, RootState]

  private def findRootEl(from: dom.Node): Option[dom.html.Element] =
    var node: dom.Node | Null = from
    while node != null do
      node match
        case el: dom.html.Element if el.getAttribute("data-slot") == "navigation-menu" =>
          return Some(el)
        case _ => ()
      node = node.parentNode
    None

  private def findItemValue(from: dom.Node): Option[String] =
    var node: dom.Node | Null = from
    while node != null do
      node match
        case el: dom.html.Element if el.getAttribute("data-slot") == "navigation-menu-item" =>
          return Option(el.getAttribute("data-value")).filter(_.nonEmpty)
        case _ => ()
      node = node.parentNode
    None

  private def stateOf(from: dom.Node): Option[RootState] =
    findRootEl(from).flatMap(roots.get)

  private def compPath(ev: dom.Event): js.Array[dom.EventTarget] =
    ev.asInstanceOf[js.Dynamic].composedPath().asInstanceOf[js.Array[dom.EventTarget]]

  private def contentClasses(viewport: Boolean): String =
    val base =
      "cn-navigation-menu-content top-0 left-0 w-full md:absolute md:w-auto **:data-[slot=navigation-menu-link]:focus:ring-0 **:data-[slot=navigation-menu-link]:focus:outline-none"
    if viewport then base
    else
      s"$base top-full mt-1.5 overflow-hidden group-data-[viewport=false]/navigation-menu:bg-popover group-data-[viewport=false]/navigation-menu:text-popover-foreground group-data-[viewport=false]/navigation-menu:ring-foreground/5 group-data-[viewport=false]/navigation-menu:dark:ring-foreground/10 group-data-[viewport=false]/navigation-menu:rounded-md group-data-[viewport=false]/navigation-menu:shadow-md group-data-[viewport=false]/navigation-menu:ring-1 group-data-[viewport=false]/navigation-menu:border"

  private def viewportPanel(state: RootState): HtmlElement =
    div(
      cls := "absolute start-0 top-full isolate z-50 flex w-full justify-center",
      div(
        dataAttr("slot") := "navigation-menu-viewport",
        cls := "cn-navigation-menu-viewport origin-top-center relative mt-1.5 w-full overflow-hidden rounded-md border bg-popover text-popover-foreground shadow-md md:w-auto",
        display <-- state.openVar.signal.map(open => if open.nonEmpty then "block" else "none"),
        aria.hidden <-- state.openVar.signal.map(_.isEmpty),
        child <-- state.openVar.signal.map { open =>
          open.flatMap(state.contents.get).getOrElse(emptyNode)
        }
      )
    )

  def apply(mods: Modifier[HtmlElement]*): HtmlElement =
    root(viewport = true)(mods*)

  def root(
      openVar: Var[Option[String]] = Var(None),
      viewport: Boolean = true
  )(mods: Modifier[HtmlElement]*): HtmlElement =
    val state = RootState(openVar, viewport, mutable.Map.empty)
    navTag(
      dataAttr("slot") := "navigation-menu",
      dataAttr("viewport") := viewport.toString,
      cls := "cn-navigation-menu group/navigation-menu relative flex max-w-max flex-1 items-center justify-center",
      onMountUnmountCallback(
        mount = { mountCtx =>
          roots(mountCtx.thisNode.ref) = state
        },
        unmount = { el =>
          roots.remove(el.ref)
        }
      ),
      onMountBind { mountCtx =>
        documentEvents(_.onMouseDown) --> { (ev: dom.MouseEvent) =>
          if state.openVar.now().nonEmpty && compPath(ev).indexOf(mountCtx.thisNode.ref) == -1 then
            state.openVar.set(None)
        }
      },
      onKeyDown --> { (ev: dom.KeyboardEvent) =>
        if ev.key == "Escape" then state.openVar.set(None)
      },
      mods,
      if viewport then viewportPanel(state) else emptyNode
    )

  def list(mods: Modifier[HtmlElement]*): HtmlElement =
    ul(
      dataAttr("slot") := "navigation-menu-list",
      cls := "cn-navigation-menu-list flex flex-1 list-none items-center justify-center gap-1",
      mods
    )

  private var itemSeq = 0

  def item(mods: Modifier[HtmlElement]*): HtmlElement =
    itemSeq += 1
    val value = s"nav-item-$itemSeq"
    li(
      dataAttr("slot") := "navigation-menu-item",
      dataAttr("value") := value,
      cls := "cn-navigation-menu-item relative",
      mods
    )

  def item(value: String, mods: Modifier[HtmlElement]*): HtmlElement =
    li(
      dataAttr("slot") := "navigation-menu-item",
      dataAttr("value") := value,
      cls := "cn-navigation-menu-item relative",
      mods
    )

  def trigger(mods: Modifier[HtmlElement]*): HtmlElement =
    val openState = Var("closed")
    val expanded = Var(false)
    button(
      typ := "button",
      dataAttr("slot") := "navigation-menu-trigger",
      cls := "cn-navigation-menu-trigger group/navigation-menu-trigger inline-flex h-9 w-max items-center justify-center gap-1 rounded-md bg-background px-4 py-2 text-sm font-medium outline-none hover:bg-accent hover:text-accent-foreground focus-visible:ring-[3px] focus-visible:ring-ring/50 disabled:pointer-events-none disabled:opacity-50 data-[state=open]:bg-accent/50",
      dataAttr("state") <-- openState.signal,
      aria.expanded <-- expanded.signal,
      aria.hasPopup := true,
      onMountBind { mountCtx =>
        val value = findItemValue(mountCtx.thisNode.ref).getOrElse("")
        val openSignal = stateOf(mountCtx.thisNode.ref).map(_.openVar.signal).getOrElse(Val(Option.empty[String]))
        openSignal --> { open =>
          openState.set(if open.contains(value) then "open" else "closed")
          expanded.set(open.contains(value))
        }
      },
      Icons.chevronDown(
        svg.cls := "relative top-px size-3 transition duration-300 group-data-[state=open]/navigation-menu-trigger:rotate-180"
      ),
      onClick --> { (ev: dom.MouseEvent) =>
        val el = ev.currentTarget.asInstanceOf[dom.Node]
        for
          state <- stateOf(el)
          value <- findItemValue(el)
        do state.toggle(value)
      },
      onKeyDown --> { (ev: dom.KeyboardEvent) =>
        ev.key match
          case "Enter" | " " =>
            ev.preventDefault()
            val el = ev.currentTarget.asInstanceOf[dom.Node]
            for
              state <- stateOf(el)
              value <- findItemValue(el)
            do state.toggle(value)
          case "Escape" =>
            stateOf(ev.currentTarget.asInstanceOf[dom.Node]).foreach(_.openVar.set(None))
          case _ => ()
      },
      mods
    )

  def content(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      cls := "contents",
      onMountCallback { mountCtx =>
        val value = findItemValue(mountCtx.thisNode.ref).getOrElse("")
        stateOf(mountCtx.thisNode.ref) match
          case Some(state) if state.viewport =>
            val panel = div(
              dataAttr("slot") := "navigation-menu-content",
              cls := contentClasses(true),
              mods
            )
            state.contents(value) = panel
          case Some(state) =>
            val panel = div(
              dataAttr("slot") := "navigation-menu-content",
              cls := contentClasses(false),
              mods
            )
            render(
              mountCtx.thisNode.ref,
              div(
                cls := "absolute left-0 top-full z-50",
                display <-- state.openVar.signal.map(open => if open.contains(value) then "block" else "none"),
                aria.hidden <-- state.openVar.signal.map(!_.contains(value)),
                panel
              )
            )
          case None =>
            render(
              mountCtx.thisNode.ref,
              div(
                cls := "invisible absolute left-0 top-full z-50 mt-1.5 w-full min-w-64 rounded-md border bg-popover p-4 text-popover-foreground opacity-0 shadow-md transition-opacity group-hover/item:visible group-hover/item:opacity-100",
                mods
              )
            )
      }
    )

  def link(mods: Modifier[HtmlElement]*): HtmlElement =
    a(
      dataAttr("slot") := "navigation-menu-link",
      cls := "cn-navigation-menu-link block select-none space-y-1 rounded-md p-3 leading-none no-underline outline-none transition-colors hover:bg-accent hover:text-accent-foreground focus:bg-accent focus:text-accent-foreground",
      mods
    )

  def indicator(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "navigation-menu-indicator",
      cls := "cn-navigation-menu-indicator top-full z-[1] flex h-1.5 items-end justify-center overflow-hidden",
      mods,
      div(
        cls := "cn-navigation-menu-indicator-arrow relative top-[60%] h-2 w-2 rotate-45 rounded-tl-sm bg-border shadow-md"
      )
    )

  def viewport(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "navigation-menu-viewport",
      cls := "cn-navigation-menu-viewport origin-top-center relative mt-1.5 w-full overflow-hidden md:w-auto",
      mods
    )
