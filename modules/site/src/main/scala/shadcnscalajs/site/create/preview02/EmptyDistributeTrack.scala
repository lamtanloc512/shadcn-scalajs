package shadcnscalajs.site.create.preview02

import com.raquo.laminar.api.L.*
import shadcnscalajs.ui.*

/** preview-02 mosaic card — port from empty-distribute-track.svelte */
object EmptyDistributeTrack:
  def apply(): HtmlElement =
    Card(
      dataAttr("card") := "empty-distribute-track",
      Card.content(
        Empty(
          cls := "p-4",
          Empty.media(Empty.MediaVariant.Icon, Icons.plus()),
          Empty.header(
            Empty.title("Distribute Track"),
            Empty.description(
              "Upload your first master to start reaching listeners on Spotify, Apple Music, and more."
            )
          ),
          Empty.content(
            Button.of(
              _.variant(Button.Variant.Primary),
              _.size(Button.Size.Default),
              _ => "Create Release"
            )
          )
        )
      )
    )
