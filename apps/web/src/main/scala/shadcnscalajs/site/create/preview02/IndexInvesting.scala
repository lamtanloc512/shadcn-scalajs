package shadcnscalajs.site.create.preview02

import com.raquo.laminar.api.L.*
import shadcnscalajs.ui.*

/** preview-02 mosaic card — port of index-investing.svelte */
object IndexInvesting:

  def apply(): HtmlElement =
    Card(
      dataAttr("card") := "index-investing",
      Card.header(
        Card.title("Dollar-Cost Averaging"),
        Card.description("A strategy for building wealth over time.")
      ),
      Card.content(
        Card.description(
          cls := "mt-3 text-sm leading-relaxed",
          a(
            href := "#/",
            cls := "underline underline-offset-4 hover:text-primary",
            "Over time"
          ),
          ", this smooths out the average cost of your investments. When prices drop, your fixed amount buys ",
          "more shares. When prices rise, you buy fewer. The result is a lower average cost per share ",
          "compared to lump-sum investing during volatile periods."
        )
      )
    )
