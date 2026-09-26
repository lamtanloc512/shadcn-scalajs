package shadcnscalajs.site.create.preview02

import com.raquo.laminar.api.L.*
import shadcnscalajs.ui.*

/** Port of shadcn-svelte preview-02 `cards/front-door.svelte`. */
object FrontDoor:
  def apply(): HtmlElement =
    Card(
      dataAttr("card") := "front-door",
      Card.header(
        Card.title("Front Door"),
        Card.description("Smart Lock Pro"),
        Card.action(
          div(
            cls := "flex items-center gap-1.5 text-sm text-muted-foreground",
            "Locked",
            Icons.lock(svg.cls := "size-4")
          )
        )
      ),
      Card.content(
        div(
          cls := "relative flex aspect-video items-center justify-center overflow-hidden rounded-lg bg-muted bg-[repeating-linear-gradient(45deg,transparent,transparent_10px,var(--border)_10px,var(--border)_11px)]",
          Badge.of(
            _.variant(Badge.Variant.Destructive),
            _ => cls := "absolute top-2 right-2",
            _ => "Live"
          )
        )
      )
    )
