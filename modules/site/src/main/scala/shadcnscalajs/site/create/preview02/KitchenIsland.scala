package shadcnscalajs.site.create.preview02

import com.raquo.laminar.api.L.*
import shadcnscalajs.ui.*

/** preview-02 mosaic card — port from kitchen-island.svelte */
object KitchenIsland:

  private final case class ScenePreset(
      brightness: List[Double],
      colorTemp: List[Double],
      volume: List[Double],
      fade: List[Double]
  )

  private val scenes: Map[String, ScenePreset] = Map(
    "cooking" -> ScenePreset(List(90), List(70), List(30), List(0)),
    "dining" -> ScenePreset(List(50), List(40), List(20), List(60)),
    "nightlight" -> ScenePreset(List(15), List(20), List(0), List(80)),
    "focus" -> ScenePreset(List(100), List(85), List(0), List(0))
  )

  private def sliderRow(
      enabled: Signal[Boolean],
      titleText: String,
      icon: SvgElement,
      valuesVar: Var[List[Double]]
  ): HtmlElement =
    Item.of(
      _.size(Item.Size.Sm),
      _.variant(Item.Variant.Outline),
      _ =>
        Seq(
          Item.media(Item.MediaVariant.Icon, icon),
          Item.content(
            cls := "flex-row items-center gap-3",
            Item.title(cls := "shrink-0", titleText)
          ),
          Item.actions(
            cls := "flex-1",
            Slider.multiple(
              valuesVar,
              0.0,
              100.0,
              1.0,
              cls := "w-full",
              onMountBind { ctx =>
                enabled --> { en =>
                  if en then ctx.thisNode.ref.removeAttribute("data-disabled")
                  else ctx.thisNode.ref.setAttribute("data-disabled", "")
                }
              }
            )
          )
        )
    )

  def apply(): HtmlElement =
    val enabled = Var(true)
    val scene = Var[Option[String]](Some("cooking"))
    val brightness = Var(List(90.0))
    val colorTemp = Var(List(70.0))
    val volume = Var(List(30.0))
    val fade = Var(List(0.0))

    scene.signal --> { selected =>
      selected.foreach { name =>
        scenes.get(name).foreach { preset =>
          brightness.set(preset.brightness)
          colorTemp.set(preset.colorTemp)
          volume.set(preset.volume)
          fade.set(preset.fade)
        }
      }
    }

    Card(
      dataAttr("card") := "kitchen-island",
      Card.header(
        Card.title("Kitchen Island"),
        Card.description("Hue Color Ambient"),
        Card.action(Switch(enabled))
      ),
      Card.content(
        cls := "flex flex-col gap-4",
        div(
          cls := "flex flex-col gap-2",
          span(cls := "sr-only", "Scenes"),
          ToggleGroup.single(
            scene,
            Toggle.Variant.Outline,
            Toggle.Size.Default,
            ToggleGroup.Item("cooking", "Cooking", disabled = enabled.signal.map(!_)),
            ToggleGroup.Item("dining", "Dining", disabled = enabled.signal.map(!_)),
            ToggleGroup.Item("nightlight", "Nightlight", disabled = enabled.signal.map(!_)),
            ToggleGroup.Item("focus", "Focus", disabled = enabled.signal.map(!_))
          )
        ),
        Item.group(
          sliderRow(enabled.signal, "Brightness", Icons.sun(), brightness),
          sliderRow(enabled.signal, "Color Temp", Icons.thermometer(), colorTemp),
          sliderRow(enabled.signal, "Volume", Icons.volume2(), volume),
          sliderRow(enabled.signal, "Fade", Icons.timer(), fade)
        )
      )
    )
