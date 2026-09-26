package shadcnscalajs.site.create.preview02

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

@js.native
@JSImport("qrcode", JSImport.Default)
private object QRCodeJS extends js.Object:
  def toDataURL(text: String, opts: js.Object): js.Promise[String] = js.native

/** Thin facade over the `qrcode` npm package, used by the `QrConnect` preview-02 card. */
object QrCode:

  def toDataUrl(url: String, width: Int = 160, margin: Int = 1): js.Promise[String] =
    QRCodeJS.toDataURL(url, js.Dynamic.literal(width = width, margin = margin))
