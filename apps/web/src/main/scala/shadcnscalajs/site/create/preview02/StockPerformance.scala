package shadcnscalajs.site.create.preview02

import com.raquo.laminar.api.L.*
import org.scalajs.dom
import shadcnscalajs.ui.*

import scala.scalajs.js

/** preview-02 mosaic card — port of stock-performance.svelte */
object StockPerformance:

  private val Tickers = List("VOO", "VIG", "AAPL", "MSFT", "GOOGL", "AMZN", "TSLA")

  private val ChartData: Map[String, List[(String, Double)]] = Map(
    "VOO" -> List(
      ("Jan", 412.0),
      ("Feb", 438.0),
      ("Mar", 395.0),
      ("Apr", 450.0),
      ("May", 420.0),
      ("Jun", 462.0)
    ),
    "AAPL" -> List(
      ("Jan", 185.0),
      ("Feb", 210.0),
      ("Mar", 172.0),
      ("Apr", 198.0),
      ("May", 178.0),
      ("Jun", 215.0)
    )
  )

  private val DefaultData = List(
    ("Jan", 100.0),
    ("Feb", 118.0),
    ("Mar", 95.0),
    ("Apr", 125.0),
    ("May", 108.0),
    ("Jun", 130.0)
  )

  private def compPath(ev: dom.Event): js.Array[dom.EventTarget] =
    ev.asInstanceOf[js.Dynamic].composedPath().asInstanceOf[js.Array[dom.EventTarget]]

  def apply(): HtmlElement =
    val tickerVar = Var("VOO")
    val openVar = Var(false)
    val searchVar = Var("")
    val activeIndexVar = Var(-1)
    val triggerRef = Var(Option.empty[dom.html.Element])
    val hover = Chart.hoverVar()

    def visibleTickers(search: String): List[String] =
      val q = search.trim.toLowerCase
      if q.isEmpty then Tickers else Tickers.filter(_.toLowerCase.contains(q))

    def closeAndFocus(): Unit =
      openVar.set(false)
      activeIndexVar.set(-1)
      triggerRef.now().foreach(_.focus())

    def selectTicker(ticker: String): Unit =
      tickerVar.set(ticker)
      searchVar.set("")
      closeAndFocus()

    def seriesFor(ticker: String): List[(String, Double)] =
      ChartData.getOrElse(ticker, DefaultData)

    def commandPanel: HtmlElement =
      Popover.panel(
        cls := "w-full p-0!",
        Command(
          Command.input(
            placeholder := "Search ticker...",
            value <-- searchVar.signal,
            onInput --> { ev =>
              searchVar.set(ev.target.asInstanceOf[dom.html.Input].value)
              activeIndexVar.set(-1)
            },
            onKeyDown --> { (ev: dom.KeyboardEvent) =>
              val visible = visibleTickers(searchVar.now())
              ev.key match
                case "ArrowDown" if visible.nonEmpty =>
                  ev.preventDefault()
                  activeIndexVar.set((activeIndexVar.now() + 1) % visible.size)
                case "ArrowUp" if visible.nonEmpty =>
                  ev.preventDefault()
                  activeIndexVar.set(
                    ((activeIndexVar.now() - 1) % visible.size + visible.size) % visible.size
                  )
                case "Enter" =>
                  ev.preventDefault()
                  val idx = activeIndexVar.now()
                  if idx >= 0 && idx < visible.size then selectTicker(visible(idx))
                  else if visible.size == 1 then selectTicker(visible.head)
                case _ => ()
            }
          ),
          Command.list(
            children <-- searchVar.signal
              .combineWith(activeIndexVar.signal)
              .map { case (search, activeIndex) =>
                val visible = visibleTickers(search)
                if visible.isEmpty then List(Command.empty("No tickers found."))
                else
                  visible.zipWithIndex.map { case (ticker, idx) =>
                    Command.item(
                      cls := (if idx == activeIndex then "bg-accent text-accent-foreground" else ""),
                      onClick --> { _ => selectTicker(ticker) },
                      ticker
                    )
                  }.toList
              }
          )
        )
      )

    Card(
      dataAttr("card") := "stock-performance",
      Card.header(
        Card.title("Stock Performance"),
        Card.description("6-month price history.")
      ),
      Card.content(
        cls := "flex flex-col gap-4",
        Field.group(
          Field(
            Field.label("Ticker", forId := "ticker-select"),
            div(
              cls := "popover relative w-full",
              onMountBind { ctx =>
                documentEvents(_.onMouseDown) --> { (ev: dom.MouseEvent) =>
                  if openVar.now() && compPath(ev).indexOf(ctx.thisNode.ref) == -1 then closeAndFocus()
                }
              },
              onKeyDown --> { (ev: dom.KeyboardEvent) =>
                if ev.key == "Escape" then closeAndFocus()
              },
              Button.of(
                _.variant(Button.Variant.Outline),
                _ => idAttr := "ticker-select",
                _ => cls := "w-full justify-between bg-muted font-normal",
                _ => role := "combobox",
                _ => aria.hasPopup := true,
                _ => aria.expanded <-- openVar.signal,
                _ => onMountCallback { ctx => triggerRef.set(Some(ctx.thisNode.ref)) },
                _ => onClick --> { _ => openVar.update(!_) },
                _ => child.text <-- tickerVar.signal,
                _ =>
                  Icons.chevronDown(
                    svg.cls := "size-4 text-muted-foreground opacity-50"
                  )
              ),
              child.maybe <-- openVar.signal.map(open => if open then Some(commandPanel) else None)
            )
          )
        ),
        Separator(),
        Chart(
          cls := "relative h-[200px] w-full",
          child <-- tickerVar.signal.map { ticker =>
            Chart.area(
              seriesFor(ticker),
              hover,
              Chart.AreaStyle(
                color = "var(--chart-1)",
                labelFormat = _.take(3)
              )
            )
          },
          Chart.tooltip(
            hover,
            seriesLabel = "Price",
            hideLabel = true,
            indicator = Chart.TooltipIndicator.Line
          )
        )
      )
    )
