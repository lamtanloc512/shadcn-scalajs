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

  private def reactive(lucide: Seq[String], hugeicons: Seq[String])(mods: Modifier[SvgElement]*): SvgElement =
    svgTag(
      svg.viewBox := "0 0 24 24",
      svg.fill := "none",
      svg.stroke := "currentColor",
      svg.strokeWidth := "2",
      svg.strokeLineCap := "round",
      svg.strokeLineJoin := "round",
      svg.cls := "size-4",
      aria.hidden := true,
      children <-- activeLibrary.map { lib =>
        (if lib == "hugeicons" then hugeicons else lucide).map(d => svgPath(svg.d := d)).toList
      },
      mods
    )

  def chevronDown(mods: Modifier[SvgElement]*): SvgElement =
    reactive(
      lucide = Seq("m6 9 6 6 6-6"),
      hugeicons = Seq("M18 9.00005C18 9.00005 13.5811 15 12 15C10.4188 15 6 9 6 9")
    )(mods*)

  def chevronsUpDown(mods: Modifier[SvgElement]*): SvgElement =
    reactive(
      lucide = Seq("m7 15 5 5 5-5", "m7 9 5-5 5 5"),
      hugeicons = Seq(
        "M18 14C18 14 13.5811 19 12 19C10.4188 19 6 14 6 14",
        "M18 9.99996C18 9.99996 13.5811 5.00001 12 5C10.4188 4.99999 6 10 6 10"
      )
    )(mods*)

  def check(mods: Modifier[SvgElement]*): SvgElement =
    reactive(lucide = Seq("M20 6 9 17l-5-5"), hugeicons = Seq("M5 14L8.5 17.5L19 6.5"))(mods*)

  def x(mods: Modifier[SvgElement]*): SvgElement =
    reactive(
      lucide = Seq("M18 6 6 18", "m6 6 12 12"),
      hugeicons = Seq("M18 6L6.00081 17.9992", "M17.9992 18L6 6.00085")
    )(mods*)
