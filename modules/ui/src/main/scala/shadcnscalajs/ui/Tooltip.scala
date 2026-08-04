package shadcnscalajs.ui

import com.raquo.laminar.api.L.*
import com.raquo.laminar.codecs.StringAsIsCodec

object Tooltip:
  private val titleAttr = htmlAttr("title", StringAsIsCodec)
  def apply(text: String, mods: Modifier[HtmlElement]*): HtmlElement =
    span(titleAttr := text, dataAttr("tooltip") := text, cls := "cn-tooltip cursor-help", mods)
