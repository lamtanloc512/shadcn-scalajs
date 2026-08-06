package shadcnscalajs.site.create.preview02

import com.raquo.laminar.api.L.*
import shadcnscalajs.ui.*

/** preview-02 mosaic card. STUB — port from
  * /Users/elam/Personal/shadcn-svelte/docs/src/lib/registry/examples/create/preview-02/cards/kitchen-island.svelte
  */
object KitchenIsland:
  def apply(): HtmlElement =
    Card(dataAttr("card") := "kitchen-island", Card.header(Card.title("KitchenIsland")))
