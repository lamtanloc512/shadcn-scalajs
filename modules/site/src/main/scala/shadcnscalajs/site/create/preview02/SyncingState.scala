package shadcnscalajs.site.create.preview02

import com.raquo.laminar.api.L.*
import shadcnscalajs.ui.*

/** preview-02 mosaic card — port from syncing-state.svelte */
object SyncingState:
  def apply(): HtmlElement =
    Card(
      dataAttr("card") := "syncing-state",
      Card.content(
        cls := "p-0",
        Empty(
          cls := "p-4",
          Empty.header(
            Empty.media(Empty.MediaVariant.Icon, Spinner()),
            Empty.title("Syncing your accounts"),
            Empty.description(
              "We're pulling in your latest transactions. This usually takes a few seconds."
            )
          ),
          Empty.content(
            Button.of(
              _.variant(Button.Variant.Outline),
              _.size(Button.Size.Default),
              _ => "Cancel"
            )
          )
        )
      )
    )
