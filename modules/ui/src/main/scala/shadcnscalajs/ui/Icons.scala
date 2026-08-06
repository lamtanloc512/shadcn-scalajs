package shadcnscalajs.ui

import com.raquo.laminar.api.L.*
import com.raquo.laminar.api.L.svg.{path as svgPath, svg as svgTag}
import org.scalajs.dom

import scala.scalajs.js

/** Swappable icon registry backing the `iconLibrary` field of `shadcnscalajs.site.ThemeConfig` — resolves each named
  * icon to Lucide or Hugeicons path data based on `<html data-icon-library="...">`, which `ThemeConfig.applyToDocument`
  * sets. Only covers icons real `modules/ui` components and blocks actually use today (see
  * docs/superpowers/specs/2026-08-06-create-theme-customizer-design.md's Icon abstraction section for what's
  * deliberately out of scope).
  */
object Icons:

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

  private def base(mods: Modifier[SvgElement]*)(paths: (String => SvgElement)*)(pathData: Seq[String]): SvgElement =
    svgTag(
      svg.viewBox := "0 0 24 24",
      svg.fill := "none",
      svg.stroke := "currentColor",
      svg.strokeWidth := "2",
      svg.strokeLineCap := "round",
      svg.strokeLineJoin := "round",
      svg.cls := "size-4",
      aria.hidden := true,
      pathData.map(d => svgPath(svg.d := d)),
      mods
    )

  private def reactive(lucide: Seq[String], hugeicons: Seq[String])(mods: Modifier[SvgElement]*): HtmlElement =
    div(
      cls := "inline-contents",
      child <-- activeLibrary.map { lib =>
        val data = if lib == "hugeicons" then hugeicons else lucide
        base(mods*)()(data)
      }
    )

  def chevronDown(mods: Modifier[SvgElement]*): HtmlElement =
    reactive(lucide = Seq("m6 9 6 6 6-6"), hugeicons = Seq("m6 9 6 6 6-6"))(mods*)

  def chevronsUpDown(mods: Modifier[SvgElement]*): HtmlElement =
    reactive(lucide = Seq("m7 15 5 5 5-5", "m7 9 5-5 5 5"), hugeicons = Seq("m7 15 5 5 5-5", "m7 9 5-5 5 5"))(mods*)

  def check(mods: Modifier[SvgElement]*): HtmlElement =
    reactive(lucide = Seq("M20 6 9 17l-5-5"), hugeicons = Seq("M20 6 9 17l-5-5"))(mods*)

  def x(mods: Modifier[SvgElement]*): HtmlElement =
    reactive(lucide = Seq("M18 6 6 18", "m6 6 12 12"), hugeicons = Seq("M18 6 6 18", "m6 6 12 12"))(mods*)
