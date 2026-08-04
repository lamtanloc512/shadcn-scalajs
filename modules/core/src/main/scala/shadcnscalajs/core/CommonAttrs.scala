package shadcnscalajs.core

import com.raquo.laminar.api.L.*
import com.raquo.laminar.codecs.BooleanAsAttrPresenceCodec

/** Attributes shared across components that wrap native `<details>`/`<dialog>` elements. (DataAttrs.scala removed —
  * styling is now in Tailwind classes.)
  */
object CommonAttrs:

  /** Presence-based `open` attribute for `<details>` and `<dialog>`. */
  val openAttr: HtmlAttr[Boolean] = htmlAttr("open", BooleanAsAttrPresenceCodec)
