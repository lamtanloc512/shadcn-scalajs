package shadcnscalajs.core

import com.raquo.laminar.api.L.*
import com.raquo.laminar.codecs.BooleanAsAttrPresenceCodec

/** Attributes not predefined in Laminar/Scala DOM Types, shared across
  * components that wrap native `<details>`/`<dialog>` elements.
  */
object CommonAttrs:

  /** The boolean `open` attribute shared by `<details>` and `<dialog>`
    * (presence-based, like `disabled` — MDN: this is not the same as
    * `HTMLDialogElement.open` reflected property, but setting the attribute
    * is sufficient for `<details>`; `<dialog>` should be opened/closed via
    * `showModal()`/`close()` instead, see Dialog.scala).
    */
  val openAttr: HtmlAttr[Boolean] = htmlAttr("open", BooleanAsAttrPresenceCodec)

  /** Presence-only marker basecoat's popover/dropdown-menu CSS hooks off of
    * (`[data-popover]` selectors in basecoat/src/css/components/popover.css),
    * distinct from the typed `data-variant`/`data-size` attrs in DataAttrs.
    */
  val dataPopover: HtmlAttr[Boolean] = htmlAttr("data-popover", BooleanAsAttrPresenceCodec)
