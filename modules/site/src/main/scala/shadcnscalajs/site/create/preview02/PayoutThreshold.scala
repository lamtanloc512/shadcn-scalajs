package shadcnscalajs.site.create.preview02

import com.raquo.laminar.api.L.*
import shadcnscalajs.ui.*

/** preview-02 mosaic card. STUB — port from
  * /Users/elam/Personal/shadcn-svelte/docs/src/lib/registry/examples/create/preview-02/cards/payout-threshold.svelte
  */
object PayoutThreshold:
  def apply(): HtmlElement =
    Card(dataAttr("card") := "payout-threshold", Card.header(Card.title("PayoutThreshold")))
