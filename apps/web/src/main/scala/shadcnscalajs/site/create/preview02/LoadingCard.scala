package shadcnscalajs.site.create.preview02

import com.raquo.laminar.api.L.*
import shadcnscalajs.ui.*

/** preview-02 mosaic card — port from loading-card.svelte */
object LoadingCard:
  def apply(): HtmlElement =
    Card(
      dataAttr("card") := "loading-card",
      Card.header(
        Skeleton(cls := "h-5 w-32"),
        Skeleton(cls := "h-4 w-48")
      ),
      Card.content(
        cls := "flex flex-col gap-4",
        Skeleton(cls := "h-32 w-full rounded-lg"),
        div(
          cls := "flex flex-col gap-2",
          Skeleton(cls := "h-4 w-full"),
          Skeleton(cls := "h-4 w-3/4"),
          Skeleton(cls := "h-4 w-1/2")
        ),
        div(
          cls := "flex gap-2",
          Skeleton(cls := "h-9 flex-1 rounded-md"),
          Skeleton(cls := "h-9 flex-1 rounded-md")
        )
      )
    )
