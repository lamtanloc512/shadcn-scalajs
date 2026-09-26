package shadcnscalajs.site.create.preview02

import com.raquo.laminar.api.L.*
import org.scalajs.dom
import shadcnscalajs.ui.*

/** Port of shadcn-svelte preview-02 `cards/cover-art.svelte`. */
object CoverArt:
  def apply(): HtmlElement =
    Card(
      dataAttr("card") := "cover-art",
      Card.content(
        cls := "flex flex-col gap-3",
        Label(
          forId := "cover-art",
          cls := "text-center text-xs font-normal tracking-wider text-muted-foreground uppercase",
          "Cover Art"
        ),
        Item.of(
          _ => cls := "aspect-square",
          _.variant(Item.Variant.Outline),
          _ =>
            label(
              forId := "cover-art",
              cls := "flex size-full cursor-pointer items-center justify-center",
              Icons.image(svg.cls := "size-10 text-muted-foreground/50")
            )
        ),
        input(
          typ := "file",
          idAttr := "cover-art",
          accept := "image/jpeg,image/png",
          cls := "sr-only"
        )
      ),
      Card.footer(
        cls := "flex-col gap-2",
        Button.of(
          _.variant(Button.Variant.Secondary),
          _ => cls := "w-full",
          _ => typ := "button",
          _ =>
            onClick --> { _ =>
              dom.document.getElementById("cover-art").asInstanceOf[dom.html.Input].click()
            },
          _ => "Upload Artwork"
        ),
        Card.description(
          cls := "text-center text-xs",
          "Minimum 3000 × 3000px",
          br(),
          "JPEG or PNG only"
        )
      )
    )
