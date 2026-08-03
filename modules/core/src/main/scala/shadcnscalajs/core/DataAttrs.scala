package shadcnscalajs.core

import com.raquo.laminar.api.L.*
import com.raquo.laminar.codecs.StringAsIsCodec

/** basecoat styles components via `data-variant`/`data-size` attributes on
  * plain elements (see basecoat/src/css/styles/vega.css), not combinatorial
  * utility/BEM classes. shadcn-scalajs components bind Scala enum values to
  * these same attributes so the vendored basecoat CSS applies unmodified.
  */
object DataAttrs:

  val dataVariant: HtmlAttr[String] = htmlAttr("data-variant", StringAsIsCodec)

  val dataSize: HtmlAttr[String] = htmlAttr("data-size", StringAsIsCodec)

  /** "IconSm" -> "icon-sm", "Outline" -> "outline" — matches the attribute
    * values basecoat's CSS selectors expect (e.g. `[data-size='icon-sm']`).
    */
  def kebabCase(name: String): String =
    name.zipWithIndex.map { case (c, i) =>
      if c.isUpper then (if i == 0 then c.toLower.toString else s"-${c.toLower}")
      else c.toString
    }.mkString
