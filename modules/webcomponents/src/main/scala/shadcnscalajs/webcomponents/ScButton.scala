package shadcnscalajs.webcomponents

import com.raquo.laminar.api.L.*
import org.scalajs.dom
import shadcnscalajs.core.Tags.slotTag
import shadcnscalajs.ui.Button

import scala.scalajs.js

class ScButton extends ScElementBase:

  private val variantVar = Var(Button.Variant.Primary)
  private val sizeVar = Var(Button.Size.Default)

  observeAttribute("variant")(v => ScButton.parseVariant(v).foreach(variantVar.set))
  observeAttribute("size")(v => ScButton.parseSize(v).foreach(sizeVar.set))
  stringProperty("variant")
  stringProperty("size")

  mount(
    button(
      typ := "button",
      dataAttr("slot") := "button",
      cls := ButtonStyles.base,
      cls := "btn cn-button group/button",
      cls <-- variantVar.signal.map(ButtonStyles.variantClass),
      cls <-- sizeVar.signal.map(ButtonStyles.sizeClass),
      dataAttr("variant") <-- variantVar.signal.map(_.toString.toLowerCase),
      dataAttr("size") <-- sizeVar.signal.map(_.toString.toLowerCase),
      slotTag()
    )
  )

object ScButton:

  def register(): Unit =
    ScElements.define("sc-button", js.constructorOf[ScButton], "variant", "size")

  private def parseVariant(v: Option[String]): Option[Button.Variant] = v.collect {
    case "primary"     => Button.Variant.Primary; case "secondary" => Button.Variant.Secondary
    case "outline"     => Button.Variant.Outline; case "ghost"     => Button.Variant.Ghost
    case "destructive" => Button.Variant.Destructive; case "link"  => Button.Variant.Link
  }

  private def parseSize(v: Option[String]): Option[Button.Size] = v.collect {
    case "default" => Button.Size.Default; case "xs"     => Button.Size.Xs; case "sm"          => Button.Size.Sm
    case "lg"      => Button.Size.Lg; case "icon"        => Button.Size.Icon
    case "icon-xs" => Button.Size.IconXs; case "icon-sm" => Button.Size.IconSm; case "icon-lg" => Button.Size.IconLg
  }

private object ButtonStyles:
  val base =
    "inline-flex shrink-0 items-center justify-center gap-2 rounded-md text-sm font-medium whitespace-nowrap transition-all outline-none focus-visible:border-ring focus-visible:ring-[3px] focus-visible:ring-ring/50 disabled:pointer-events-none disabled:opacity-50 aria-invalid:border-destructive aria-invalid:ring-destructive/20 dark:aria-invalid:ring-destructive/40 [&_svg]:pointer-events-none [&_svg]:shrink-0 [&_svg:not([class*='size-'])]:size-4"

  val variantClass: Button.Variant => String =
    case Button.Variant.Primary => "bg-primary text-primary-foreground hover:bg-primary/90"
    case Button.Variant.Destructive =>
      "bg-destructive text-white hover:bg-destructive/90 focus-visible:ring-destructive/20 dark:bg-destructive/60 dark:focus-visible:ring-destructive/40"
    case Button.Variant.Outline =>
      "border bg-background shadow-xs hover:bg-accent hover:text-accent-foreground dark:border-input dark:bg-input/30 dark:hover:bg-input/50"
    case Button.Variant.Secondary => "bg-secondary text-secondary-foreground hover:bg-secondary/80"
    case Button.Variant.Ghost     => "hover:bg-accent hover:text-accent-foreground dark:hover:bg-accent/50"
    case Button.Variant.Link      => "text-primary underline-offset-4 hover:underline"

  val sizeClass: Button.Size => String =
    case Button.Size.Default => "h-9 px-4 py-2 has-[>svg]:px-3"
    case Button.Size.Xs   => "h-6 gap-1 rounded-md px-2 text-xs has-[>svg]:px-1.5 [&_svg:not([class*='size-'])]:size-3"
    case Button.Size.Sm   => "h-8 gap-1.5 rounded-md px-3 has-[>svg]:px-2.5"
    case Button.Size.Lg   => "h-10 rounded-md px-6 has-[>svg]:px-4"
    case Button.Size.Icon => "size-9"
    case Button.Size.IconXs => "size-6 rounded-md [&_svg:not([class*='size-'])]:size-3"
    case Button.Size.IconSm => "size-8"
    case Button.Size.IconLg => "size-10"
