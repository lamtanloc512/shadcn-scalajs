package shadcnscalajs.site.create

import com.raquo.laminar.api.L.*
import org.scalajs.dom

/** Floating 01/02 preview switcher anchored to the preview frame. Only `preview-02` is active; `02` is a disabled
  * affordance until the legacy preview route exists.
  */
object PreviewSwitcher:

  def apply(activeItem: String): HtmlElement =
    val visible = activeItem == "preview" || activeItem.startsWith("preview-0")
    if !visible then div()
    else
      div(
        cls := "dark absolute right-3 bottom-3 z-20 flex items-center gap-1 rounded-xl bg-card/90 p-1 shadow-xl backdrop-blur-xl",
        switcherButton(
          label = "01",
          isActive = activeItem == "preview-02",
          isDisabled = false,
          titleText = None,
          clickObserver = Observer.empty
        ),
        switcherButton(
          label = "02",
          isActive = false,
          isDisabled = true,
          titleText = Some("Coming soon"),
          clickObserver = Observer.empty
        )
      )

  private def switcherButton(
      label: String,
      isActive: Boolean,
      isDisabled: Boolean,
      titleText: Option[String],
      clickObserver: Observer[dom.Event]
  ): HtmlElement =
    button(
      typ := "button",
      cls := "h-7 min-w-8 rounded-lg px-2.5 text-xs font-medium text-muted-foreground transition-colors hover:text-foreground data-[active=true]:bg-accent data-[active=true]:text-accent-foreground",
      cls := (if isDisabled then "cursor-not-allowed opacity-60" else "cursor-pointer"),
      dataAttr("active") := isActive.toString,
      disabled := isDisabled,
      titleText.fold[Modifier[HtmlElement]](emptyMod)(t => title := t),
      aria.disabled := isDisabled,
      onClick --> clickObserver,
      label
    )
