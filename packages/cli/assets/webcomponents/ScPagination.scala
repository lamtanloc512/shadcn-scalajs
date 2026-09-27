package shadcnscalajs.webcomponents

import com.raquo.laminar.api.L.*
import shadcnscalajs.ui.Pagination

import scala.scalajs.js

class ScPagination extends ScElementBase:

  private val pageVar = Var(1)
  private val pageCountVar = Var(10)
  private val siblingCountVar = Var(1)
  private val echo = EchoGuard[Int]()

  private def clamp(page: Int, count: Int): Int = page.max(1).min(count)

  private def setPage(page: Int): Unit =
    val next = clamp(page, pageCountVar.now())
    if next != pageVar.now() then
      echo.wrote(next)
      pageVar.set(next)

  observeAttribute("page")(v => setPage(v.flatMap(_.toIntOption).getOrElse(1)))
  observeAttribute("page-count") { v =>
    val count = v.flatMap(_.toIntOption).getOrElse(10).max(1)
    pageCountVar.set(count)
    setPage(pageVar.now())
  }
  observeAttribute("sibling-count")(v => siblingCountVar.set(v.flatMap(_.toIntOption).getOrElse(1).max(0)))
  stringProperty("page")
  stringProperty("page-count")
  stringProperty("sibling-count")

  mount(
    div(
      child <-- pageCountVar.signal.combineWithFn(siblingCountVar.signal) { (count, siblings) =>
        Pagination.stateful(pageVar, count, siblings)
      },
      pageVar.signal.changes --> Observer[Int] { page =>
        val fromAttribute = echo.isEcho(page)
        if getAttribute("page") != page.toString then setAttribute("page", page.toString)
        if !fromAttribute then emit("sc-change", page)
      }
    )
  )

object ScPagination:
  def register(): Unit =
    ScElements.define("sc-pagination", js.constructorOf[ScPagination], "page", "page-count", "sibling-count")
