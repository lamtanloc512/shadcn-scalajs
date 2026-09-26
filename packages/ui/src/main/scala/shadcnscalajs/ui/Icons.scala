package shadcnscalajs.ui

import com.raquo.laminar.api.L.*
import com.raquo.laminar.api.L.svg.{path as svgPath, svg as svgTag}
import org.scalajs.dom

import scala.scalajs.js

import shadcnscalajs.ui.icons.{HugeiconsIconData, LucideIconData, PhosphorIconData, RemixIconData, TablerIconData}

/** Per-library SVG path catalog backing the `iconLibrary` field of `shadcnscalajs.site.ThemeConfig`. */
trait IconDataSource:
  def viewBox: String
  def strokeBased: Boolean
  def paths: Map[String, Seq[String]]

/** Swappable icon registry — resolves each named concept via per-library [[IconDataSource]] objects based on `<html
  * data-icon-library="...">`, which `ThemeConfig.applyToDocument` sets.
  */
object Icons:

  val Concepts: List[String] = List(
    "activity",
    "alert-circle",
    "arrow-left-right",
    "arrow-right",
    "audio-lines",
    "badge-check",
    "bell",
    "bold",
    "book-open",
    "bookmark",
    "bot",
    "building-2",
    "calendar",
    "camera",
    "car",
    "check",
    "chevron-down",
    "chevron-right",
    "chevrons-up-down",
    "circle-help",
    "circle-plus",
    "cloud",
    "coffee",
    "command",
    "copy",
    "credit-card",
    "dice-faces",
    "file-bar-chart",
    "file-text",
    "folder",
    "forward",
    "frame",
    "gauge",
    "gallery-vertical-end",
    "globe",
    "heart",
    "image",
    "italic",
    "layout-dashboard",
    "lock",
    "lock-keyhole",
    "log-out",
    "map",
    "maximize",
    "menu",
    "message-square",
    "minimize",
    "minus",
    "moon",
    "more-horizontal",
    "paintbrush",
    "pie-chart",
    "plus",
    "refresh-cw",
    "repeat",
    "search",
    "settings-2",
    "shield",
    "shopping-cart",
    "sparkles",
    "square-lock",
    "square-terminal",
    "square-unlock",
    "star",
    "sun",
    "target",
    "thermometer",
    "timer",
    "trash-2",
    "trending-up",
    "tv",
    "underline",
    "undo",
    "user",
    "volume-2",
    "wallet",
    "x"
  )

  private val sources: Map[String, IconDataSource] = Map(
    "lucide" -> LucideIconData,
    "tabler" -> TablerIconData,
    "hugeicons" -> HugeiconsIconData,
    "phosphor" -> PhosphorIconData,
    "remixicon" -> RemixIconData
  )

  /** Mirrors the `data-icon-library` attribute on `<html>` reactively, the same MutationObserver technique
    * `ScElementBase.observeAttribute` already uses for custom-element attributes.
    */
  val activeLibrary: Signal[String] =
    val initial = Option(dom.document.documentElement.getAttribute("data-icon-library")).getOrElse("lucide")
    val libraryVar = Var(initial)
    val observer = new dom.MutationObserver((records, _) =>
      records.foreach { record =>
        if record.attributeName == "data-icon-library" then
          libraryVar.set(Option(dom.document.documentElement.getAttribute("data-icon-library")).getOrElse("lucide"))
      }
    )
    observer.observe(
      dom.document.documentElement,
      new dom.MutationObserverInit { attributes = true; attributeFilter = js.Array("data-icon-library") }
    )
    libraryVar.signal

  private def sourceFor(library: String): IconDataSource =
    sources.getOrElse(library, LucideIconData)

  private def resolve(concept: String, library: String): (IconDataSource, Seq[String]) =
    val active = sourceFor(library)
    active.paths.get(concept) match
      case Some(pathData) => (active, pathData)
      case None =>
        (LucideIconData, LucideIconData.paths.getOrElse(concept, Seq.empty))

  /** `data-slot` for icons. The HTML `dataAttr` does not type-check against `SvgElement`, and upstream puts the slot on
    * the `<svg>` itself rather than on a wrapper, which the style packs' `[data-slot=...] svg` rules rely on.
    */
  val svgSlot: com.raquo.laminar.keys.SvgAttr[String] =
    svg.svgAttr("data-slot", com.raquo.laminar.codecs.StringAsIsCodec, None)

  def icon(concept: String)(mods: Modifier[SvgElement]*): SvgElement =
    svgTag(
      svg.viewBox <-- activeLibrary.map { lib => resolve(concept, lib)._1.viewBox },
      svg.fill <-- activeLibrary.map { lib =>
        if resolve(concept, lib)._1.strokeBased then "none" else "currentColor"
      },
      svg.stroke <-- activeLibrary.map { lib =>
        if resolve(concept, lib)._1.strokeBased then "currentColor" else "none"
      },
      svg.strokeWidth <-- activeLibrary.map { lib =>
        if resolve(concept, lib)._1.strokeBased then "2" else "0"
      },
      svg.strokeLineCap <-- activeLibrary.map { lib =>
        if resolve(concept, lib)._1.strokeBased then "round" else "butt"
      },
      svg.strokeLineJoin <-- activeLibrary.map { lib =>
        if resolve(concept, lib)._1.strokeBased then "round" else "miter"
      },
      svg.cls := "size-4",
      aria.hidden := true,
      children <-- activeLibrary.map { lib =>
        val (_, pathData) = resolve(concept, lib)
        pathData.map(d => svgPath(svg.d := d)).toList
      },
      mods
    )

  def activity(mods: Modifier[SvgElement]*): SvgElement = icon("activity")(mods*)
  def alertCircle(mods: Modifier[SvgElement]*): SvgElement = icon("alert-circle")(mods*)
  def arrowLeftRight(mods: Modifier[SvgElement]*): SvgElement = icon("arrow-left-right")(mods*)
  def arrowRight(mods: Modifier[SvgElement]*): SvgElement = icon("arrow-right")(mods*)
  def audioLines(mods: Modifier[SvgElement]*): SvgElement = icon("audio-lines")(mods*)
  def badgeCheck(mods: Modifier[SvgElement]*): SvgElement = icon("badge-check")(mods*)
  def bell(mods: Modifier[SvgElement]*): SvgElement = icon("bell")(mods*)
  def bold(mods: Modifier[SvgElement]*): SvgElement = icon("bold")(mods*)
  def bookOpen(mods: Modifier[SvgElement]*): SvgElement = icon("book-open")(mods*)
  def bookmark(mods: Modifier[SvgElement]*): SvgElement = icon("bookmark")(mods*)
  def bot(mods: Modifier[SvgElement]*): SvgElement = icon("bot")(mods*)
  def building2(mods: Modifier[SvgElement]*): SvgElement = icon("building-2")(mods*)
  def calendar(mods: Modifier[SvgElement]*): SvgElement = icon("calendar")(mods*)
  def camera(mods: Modifier[SvgElement]*): SvgElement = icon("camera")(mods*)
  def car(mods: Modifier[SvgElement]*): SvgElement = icon("car")(mods*)
  def check(mods: Modifier[SvgElement]*): SvgElement = icon("check")(mods*)
  def chevronDown(mods: Modifier[SvgElement]*): SvgElement = icon("chevron-down")(mods*)
  def chevronRight(mods: Modifier[SvgElement]*): SvgElement = icon("chevron-right")(mods*)
  def chevronsUpDown(mods: Modifier[SvgElement]*): SvgElement = icon("chevrons-up-down")(mods*)
  def circleHelp(mods: Modifier[SvgElement]*): SvgElement = icon("circle-help")(mods*)
  def circlePlus(mods: Modifier[SvgElement]*): SvgElement = icon("circle-plus")(mods*)
  def cloud(mods: Modifier[SvgElement]*): SvgElement = icon("cloud")(mods*)
  def coffee(mods: Modifier[SvgElement]*): SvgElement = icon("coffee")(mods*)
  def command(mods: Modifier[SvgElement]*): SvgElement = icon("command")(mods*)
  def copy(mods: Modifier[SvgElement]*): SvgElement = icon("copy")(mods*)
  def creditCard(mods: Modifier[SvgElement]*): SvgElement = icon("credit-card")(mods*)
  def diceFaces(mods: Modifier[SvgElement]*): SvgElement = icon("dice-faces")(mods*)
  def fileBarChart(mods: Modifier[SvgElement]*): SvgElement = icon("file-bar-chart")(mods*)
  def fileText(mods: Modifier[SvgElement]*): SvgElement = icon("file-text")(mods*)
  def folder(mods: Modifier[SvgElement]*): SvgElement = icon("folder")(mods*)
  def forward(mods: Modifier[SvgElement]*): SvgElement = icon("forward")(mods*)
  def frame(mods: Modifier[SvgElement]*): SvgElement = icon("frame")(mods*)
  def gauge(mods: Modifier[SvgElement]*): SvgElement = icon("gauge")(mods*)
  def galleryVerticalEnd(mods: Modifier[SvgElement]*): SvgElement = icon("gallery-vertical-end")(mods*)
  def globe(mods: Modifier[SvgElement]*): SvgElement = icon("globe")(mods*)
  def heart(mods: Modifier[SvgElement]*): SvgElement = icon("heart")(mods*)
  def image(mods: Modifier[SvgElement]*): SvgElement = icon("image")(mods*)
  def italic(mods: Modifier[SvgElement]*): SvgElement = icon("italic")(mods*)
  def layoutDashboard(mods: Modifier[SvgElement]*): SvgElement = icon("layout-dashboard")(mods*)
  def lock(mods: Modifier[SvgElement]*): SvgElement = icon("lock")(mods*)
  def lockKeyhole(mods: Modifier[SvgElement]*): SvgElement = icon("lock-keyhole")(mods*)
  def logOut(mods: Modifier[SvgElement]*): SvgElement = icon("log-out")(mods*)
  def map(mods: Modifier[SvgElement]*): SvgElement = icon("map")(mods*)
  def maximize(mods: Modifier[SvgElement]*): SvgElement = icon("maximize")(mods*)
  def menu(mods: Modifier[SvgElement]*): SvgElement = icon("menu")(mods*)
  def messageSquare(mods: Modifier[SvgElement]*): SvgElement = icon("message-square")(mods*)
  def minimize(mods: Modifier[SvgElement]*): SvgElement = icon("minimize")(mods*)
  def minus(mods: Modifier[SvgElement]*): SvgElement = icon("minus")(mods*)
  def moon(mods: Modifier[SvgElement]*): SvgElement = icon("moon")(mods*)
  def moreHorizontal(mods: Modifier[SvgElement]*): SvgElement = icon("more-horizontal")(mods*)
  def paintbrush(mods: Modifier[SvgElement]*): SvgElement = icon("paintbrush")(mods*)
  def pieChart(mods: Modifier[SvgElement]*): SvgElement = icon("pie-chart")(mods*)
  def plus(mods: Modifier[SvgElement]*): SvgElement = icon("plus")(mods*)
  def refreshCw(mods: Modifier[SvgElement]*): SvgElement = icon("refresh-cw")(mods*)
  def repeat(mods: Modifier[SvgElement]*): SvgElement = icon("repeat")(mods*)
  def search(mods: Modifier[SvgElement]*): SvgElement = icon("search")(mods*)
  def settings2(mods: Modifier[SvgElement]*): SvgElement = icon("settings-2")(mods*)
  def shield(mods: Modifier[SvgElement]*): SvgElement = icon("shield")(mods*)
  def shoppingCart(mods: Modifier[SvgElement]*): SvgElement = icon("shopping-cart")(mods*)
  def sparkles(mods: Modifier[SvgElement]*): SvgElement = icon("sparkles")(mods*)
  def squareLock(mods: Modifier[SvgElement]*): SvgElement = icon("square-lock")(mods*)
  def squareTerminal(mods: Modifier[SvgElement]*): SvgElement = icon("square-terminal")(mods*)
  def squareUnlock(mods: Modifier[SvgElement]*): SvgElement = icon("square-unlock")(mods*)
  def star(mods: Modifier[SvgElement]*): SvgElement = icon("star")(mods*)
  def sun(mods: Modifier[SvgElement]*): SvgElement = icon("sun")(mods*)
  def target(mods: Modifier[SvgElement]*): SvgElement = icon("target")(mods*)
  def thermometer(mods: Modifier[SvgElement]*): SvgElement = icon("thermometer")(mods*)
  def timer(mods: Modifier[SvgElement]*): SvgElement = icon("timer")(mods*)
  def trash2(mods: Modifier[SvgElement]*): SvgElement = icon("trash-2")(mods*)
  def trendingUp(mods: Modifier[SvgElement]*): SvgElement = icon("trending-up")(mods*)
  def tv(mods: Modifier[SvgElement]*): SvgElement = icon("tv")(mods*)
  def underline(mods: Modifier[SvgElement]*): SvgElement = icon("underline")(mods*)
  def undo(mods: Modifier[SvgElement]*): SvgElement = icon("undo")(mods*)
  def user(mods: Modifier[SvgElement]*): SvgElement = icon("user")(mods*)
  def volume2(mods: Modifier[SvgElement]*): SvgElement = icon("volume-2")(mods*)
  def wallet(mods: Modifier[SvgElement]*): SvgElement = icon("wallet")(mods*)
  def x(mods: Modifier[SvgElement]*): SvgElement = icon("x")(mods*)
