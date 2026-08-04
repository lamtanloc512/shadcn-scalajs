package shadcnscalajs.ui

import com.raquo.laminar.api.L.*
import com.raquo.laminar.codecs.StringAsIsCodec

object Avatar:
  private val imageTag = htmlTag("img")
  private val srcAttr = htmlAttr("src", StringAsIsCodec)
  private val altAttr = htmlAttr("alt", StringAsIsCodec)

  def apply(mods: Modifier[HtmlElement]*): HtmlElement =
    div(cls := "relative flex size-10 shrink-0 overflow-hidden rounded-full", mods)
  def image(src: String, alt: String = "", mods: Modifier[HtmlElement]*): HtmlElement =
    imageTag(srcAttr := src, altAttr := alt, cls := "aspect-square size-full", mods)
  def fallback(mods: Modifier[HtmlElement]*): HtmlElement =
    div(cls := "flex size-full items-center justify-center rounded-full bg-muted", mods)
