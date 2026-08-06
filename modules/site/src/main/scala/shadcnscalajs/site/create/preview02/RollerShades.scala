package shadcnscalajs.site.create.preview02

import com.raquo.laminar.api.L.*
import shadcnscalajs.ui.*

/** preview-02 mosaic card — port from roller-shades.svelte */
object RollerShades:

  private def positionToPreset(position: Double): String =
    if position <= 10 then "open"
    else if position >= 90 then "closed"
    else "half"

  def apply(): HtmlElement =
    val position = Var(List(50.0))
    val preset = Var[Option[String]](Some("half"))

    position.signal --> { values =>
      val p = values.headOption.getOrElse(50.0)
      preset.set(Some(positionToPreset(p)))
    }

    preset.signal --> { selected =>
      selected.foreach { value =>
        val p = position.now().headOption.getOrElse(50.0)
        val derived = positionToPreset(p)
        if value != derived then
          val snapped = value match
            case "open"   => 0.0
            case "half"   => 50.0
            case "closed" => 100.0
            case _        => p
          position.set(List(snapped))
      }
    }

    Card(
      dataAttr("card") := "roller-shades",
      Card.header(
        Card.title("Living Room"),
        Card.description("Roller Shades")
      ),
      Card.content(
        cls := "flex flex-col gap-4",
        div(
          cls := "flex h-32 flex-col overflow-hidden rounded-lg border bg-muted",
          div(
            cls := "bg-muted-foreground transition-all duration-300",
            styleAttr <-- position.signal.map { values =>
              s"height:${values.headOption.getOrElse(50.0)}%"
            }
          )
        ),
        div(
          cls := "flex items-center gap-3",
          span(
            cls := "text-xs font-medium tracking-wider text-muted-foreground uppercase",
            "Open"
          ),
          Slider.multiple(position, 0.0, 100.0, 1.0, cls := "flex-1"),
          span(
            cls := "text-xs font-medium tracking-wider text-muted-foreground uppercase",
            "Close"
          )
        )
      ),
      Card.footer(
        cls := "w-full",
        div(
          cls := "w-full",
          ToggleGroup.single(
            preset,
            Toggle.Variant.Outline,
            Toggle.Size.Default,
            ToggleGroup.Item("open", "Open"),
            ToggleGroup.Item("half", "Half"),
            ToggleGroup.Item("closed", "Closed")
          )
        )
      )
    )
