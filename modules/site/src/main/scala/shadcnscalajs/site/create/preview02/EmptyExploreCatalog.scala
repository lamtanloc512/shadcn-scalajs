package shadcnscalajs.site.create.preview02

import com.raquo.laminar.api.L.*
import shadcnscalajs.ui.*

/** preview-02 mosaic card — port from empty-explore-catalog.svelte */
object EmptyExploreCatalog:
  def apply(): HtmlElement =
    Card(
      dataAttr("card") := "empty-explore-catalog",
      Card.content(
        Empty(
          cls := "p-4",
          Empty.media(Empty.MediaVariant.Icon, Icons.audioLines()),
          Empty.header(
            Empty.title("Explore Catalog"),
            Empty.description(
              "Check your ISRC codes, metadata, and visual assets before going live."
            )
          ),
          Empty.content(
            Button.of(
              _.variant(Button.Variant.Primary),
              _.size(Button.Size.Default),
              _ => "View Catalog"
            )
          )
        )
      )
    )
