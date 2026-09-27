package shadcnscalajs.webcomponents

import com.raquo.laminar.api.L.*
import shadcnscalajs.ui.NavigationMenu

import scala.scalajs.js

class ScNavigationMenu extends ScElementBase:

  private val itemsVar = Var(List.empty[ScNavigationMenu.Item])

  observeAttribute("items")(value => itemsVar.set(ScNavigationMenu.parseItems(value.orNull)))
  jsonProperty("items")(value => itemsVar.set(ScNavigationMenu.parseItems(value)))

  mount(ScNavigationMenu.view(itemsVar))

object ScNavigationMenu:

  private case class Link(label: String, href: String)
  private case class Item(label: String, links: List[Link])

  def register(): Unit =
    ScElements.define("sc-navigation-menu", js.constructorOf[ScNavigationMenu], "items")

  private def parseItems(value: js.Any): List[Item] =
    ScElements.toArray(value).toList.flatMap(_.toList).flatMap { raw =>
      val label = raw.label
      if js.typeOf(label) != "string" then None
      else
        val links = ScElements.toArray(raw.links).toList.flatMap(_.toList).flatMap { link =>
          val linkLabel = link.label
          val href = link.href
          if js.typeOf(linkLabel) == "string" && js.typeOf(href) == "string" && safeHref(href.asInstanceOf[String]) then
            Some(Link(linkLabel.asInstanceOf[String], href.asInstanceOf[String]))
          else None
        }
        Some(Item(label.asInstanceOf[String], links))
    }

  private def safeHref(href: String): Boolean =
    val trimmed = href.trim
    trimmed.startsWith("/") || trimmed.startsWith("#") ||
    trimmed.startsWith("https://") || trimmed.startsWith("http://")

  private def view(itemsVar: Var[List[Item]]): HtmlElement =
    div(
      children <-- itemsVar.signal.map { items =>
        List(
          NavigationMenu.root(viewport = true)(
            NavigationMenu.list(
              items.zipWithIndex.map { case (item, index) =>
                NavigationMenu.item(
                  s"item-$index",
                  NavigationMenu.trigger(item.label),
                  NavigationMenu.content(
                    ul(
                      cls := "grid w-[400px] gap-2 p-4 md:w-[500px] md:grid-cols-2",
                      item.links.map { link =>
                        li(NavigationMenu.link(href := link.href, link.label))
                      }
                    )
                  )
                )
              }
            )
          )
        )
      }
    )
