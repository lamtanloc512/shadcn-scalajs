package shadcnscalajs.webcomponents

import com.raquo.laminar.api.L.*
import org.scalajs.dom
import shadcnscalajs.core.Tags.slotTag
import shadcnscalajs.ui.Carousel

import scala.scalajs.js

/** A carousel whose light-DOM children use slot="slide-0", slot="slide-1", and so on. */
class ScCarousel extends ScElementBase:

  private val countVar = Var(3)
  private val slidesVar = Var(List.empty[String])
  private val orientationVar = Var(Carousel.Orientation.Horizontal)

  observeAttribute("count")(raw => countVar.set(raw.flatMap(_.toIntOption).filter(_ >= 0).map(_.min(100)).getOrElse(3)))
  observeAttribute("orientation") { raw =>
    orientationVar.set(
      if raw.contains("vertical") then Carousel.Orientation.Vertical else Carousel.Orientation.Horizontal
    )
  }
  observeAttribute("slides")(raw => slidesVar.set(ScCarousel.parseSlides(raw.orNull)))
  stringProperty("count")
  stringProperty("orientation")
  jsonProperty("slides")(raw => slidesVar.set(ScCarousel.parseSlides(raw)))

  mount(ScCarousel.view(countVar, slidesVar, orientationVar, (index: Int) => emit("sc-change", index)))

object ScCarousel:
  def register(): Unit =
    ScElements.define("sc-carousel", js.constructorOf[ScCarousel], "count", "slides", "orientation")

  private def parseSlides(raw: js.Any): List[String] =
    ScElements.toArray(raw).map(_.toList.map(_.toString)).getOrElse(Nil)

  private def view(
      countVar: Var[Int],
      slidesVar: Var[List[String]],
      orientationVar: Var[Carousel.Orientation],
      onChange: Int => Unit
  ): HtmlElement =
    div(
      child <-- orientationVar.signal.map { orientation =>
        val carousel = Carousel.ctx(orientation)
        Carousel.root(
          onMountBind { _ =>
            countVar.signal.combineWith(slidesVar.signal) --> { _ =>
              dom.window.requestAnimationFrame(_ => carousel.refresh())
            }
          },
          carousel.content(
            children <-- countVar.signal.combineWithFn(slidesVar.signal) { (count, slides) =>
              (0 until count).map { index =>
                carousel.item(slotTag(nameAttr := s"slide-$index", slides.lift(index).getOrElse("")))
              }
            }
          ),
          carousel.previous(),
          carousel.next(),
          carousel.selectedIndex.changes --> Observer[Int](onChange)
        )
      }
    )
