package shadcnscalajs.webcomponents

import com.raquo.laminar.api.L.*
import shadcnscalajs.ui.{DropdownMenu, Menubar}

import scala.scalajs.js

/** A menubar driven by a JSON `menus` attribute or an array-valued `menus` property. */
class ScMenubar extends ScElementBase:

  import ScMenubar.MenuData

  private val menusVar = Var(List.empty[MenuData])

  observeAttribute("menus")(v => menusVar.set(parseMenus(v.orNull)))
  jsonProperty("menus")(v => menusVar.set(parseMenus(v)))

  mount(ScMenubar.view(menusVar.signal))

  private def parseMenus(value: js.Any): List[MenuData] =
    ScElements
      .toArray(value)
      .fold(List.empty[MenuData])(_.toList.zipWithIndex.map { case (rawMenu, menuIndex) =>
        val items = ScElements
          .toArray(rawMenu.items.asInstanceOf[js.Any])
          .fold(List.empty[DropdownMenu.Item])(
            _.toList.zipWithIndex.map { case (rawItem, itemIndex) =>
              DropdownMenu.Item(
                label = rawItem.label.asInstanceOf[String],
                onSelect = () => emit("sc-select", js.Dynamic.literal(menu = menuIndex, item = itemIndex)),
                disabled = rawItem.disabled.asInstanceOf[js.UndefOr[Boolean]].getOrElse(false)
              )
            }
          )
        MenuData(rawMenu.label.asInstanceOf[String], items)
      })

object ScMenubar:
  private case class MenuData(label: String, items: List[DropdownMenu.Item])

  def register(): Unit =
    ScElements.define("sc-menubar", js.constructorOf[ScMenubar], "menus")

  private def view(menus: Signal[List[MenuData]]): HtmlElement =
    div(
      child <-- menus.map { entries =>
        Menubar.bar() { bar =>
          entries.map(entry => bar.menu(entry.label)(entry.items*))
        }
      }
    )
