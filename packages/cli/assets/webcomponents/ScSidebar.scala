package shadcnscalajs.webcomponents

import com.raquo.laminar.api.L.*
import shadcnscalajs.ui.Sidebar

import scala.scalajs.js

class ScSidebar extends ScElementBase:

  private val menusVar = Var(List.empty[ScSidebar.Group])
  private val revision = Var(0)

  observeAttribute("menus")(v => { menusVar.set(parseMenus(v.orNull)); revision.update(_ + 1) })
  jsonProperty("menus")(v => { menusVar.set(parseMenus(v)); revision.update(_ + 1) })

  mount(ScSidebar.view(menusVar, revision))

  private def parseMenus(value: js.Any): List[ScSidebar.Group] =
    ScElements
      .toArray(value)
      .map(_.toList.map { raw =>
        val items = ScElements
          .toArray(raw.items.asInstanceOf[js.Any])
          .map(_.toList.map { item =>
            ScSidebar.Entry(
              label = item.label.asInstanceOf[String],
              active = item.active.asInstanceOf[js.UndefOr[Boolean]].getOrElse(false)
            )
          })
          .getOrElse(Nil)
        ScSidebar.Group(raw.label.asInstanceOf[String], items)
      })
      .getOrElse(Nil)

object ScSidebar:
  final case class Entry(label: String, active: Boolean)
  final case class Group(label: String, items: List[Entry])

  def register(): Unit =
    ScElements.define("sc-sidebar", js.constructorOf[ScSidebar], "menus")

  private def view(menusVar: Var[List[Group]], revision: Var[Int]): HtmlElement =
    val openVar = Var(true)
    // Playground/docs hosts cap height (e.g. 16rem). Upstream Sidebar.provider defaults to
    // `min-h-svh`, which would blow past that — force a contained, scrollable shell instead.
    div(
      cls := "h-full min-h-0 max-h-full overflow-hidden",
      children <-- revision.signal.map { _ =>
        val groups = menusVar.now()
        List(
          Sidebar.provider(openVar)(
            cls := "min-h-0! h-full max-h-full overflow-hidden",
            Sidebar.root(collapsible = Sidebar.Collapsible.None, openVar = openVar)(
              cls := "h-full max-h-full w-full overflow-hidden bg-transparent", {
                val contentMods: List[Modifier[HtmlElement]] =
                  (cls := "gap-0") :: groups.zipWithIndex.flatMap { (group, idx) =>
                    val entries = group.items.map { entry =>
                      li(
                        dataAttr("slot") := "sidebar-menu-item",
                        dataAttr("sidebar") := "menu-item",
                        cls := "group/menu-item relative",
                        Sidebar.menuButton(isActive = entry.active)(entry.label)
                      )
                    }
                    val block = Sidebar.group(
                      cls := (if idx == 0 then "pb-1" else "pt-1"),
                      Sidebar.groupLabel(group.label),
                      Sidebar.groupContent(Sidebar.menu(entries*))
                    )
                    if idx < groups.length - 1 then List(block, Sidebar.separator(cls := "w-auto!"))
                    else List(block)
                  }
                Sidebar.content(contentMods*)
              }
            )
          )
        )
      }
    )
