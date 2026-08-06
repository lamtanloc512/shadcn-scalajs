package shadcnscalajs.site.create.preview02

import com.raquo.laminar.api.L.*
import com.raquo.laminar.codecs.StringAsIsCodec
import shadcnscalajs.ui.*

import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js.Thenable.Implicits.*

/** preview-02 mosaic card — ported from
  * /Users/elam/Personal/shadcn-svelte/docs/src/lib/registry/examples/create/preview-02/cards/qr-connect.svelte
  */
object QrConnect:

  private val connectUrl = "https://www.youtube.com/watch?v=dQw4w9WgXcQ"
  private val imgTag = htmlTag("img")
  private val srcAttr = htmlAttr("src", StringAsIsCodec)
  private val altAttr = htmlAttr("alt", StringAsIsCodec)
  private val widthAttr = htmlAttr("width", StringAsIsCodec)
  private val heightAttr = htmlAttr("height", StringAsIsCodec)

  def apply(): HtmlElement =
    val dataUrlVar = Var("")

    Card(
      dataAttr("card") := "qr-connect",
      onMountCallback { _ =>
        val _ = QrCode.toDataUrl(connectUrl, 160, 1).toFuture.foreach(dataUrlVar.set)
      },
      Card.content(cls := "flex justify-center pt-6", qrFrame(dataUrlVar)),
      Card.header(
        cls := "text-center",
        Card.title("Scan to connect your mobile device"),
        Card.description("Open the Ledger mobile app and scan this code to link your device.")
      ),
      Card.footer(
        Button.of(
          _.variant(Button.Variant.Secondary),
          _ => cls := "w-full",
          _ => "Got it"
        )
      )
    )

  private def qrFrame(dataUrlVar: Var[String]): HtmlElement =
    div(
      cls := "rounded-xl border bg-white p-4",
      child <-- dataUrlVar.signal.map { url =>
        if url.nonEmpty then
          imgTag(
            srcAttr := url,
            altAttr := "",
            widthAttr := "160",
            heightAttr := "160",
            cls := "block"
          )
        else div(cls := "size-[160px] animate-pulse rounded bg-muted")
      }
    )
