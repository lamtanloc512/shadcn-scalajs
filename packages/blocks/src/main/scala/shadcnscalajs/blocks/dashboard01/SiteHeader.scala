package shadcnscalajs.blocks.dashboard01

import com.raquo.laminar.api.L.*
import shadcnscalajs.ui.*

/** Port of shadcn-svelte `blocks/dashboard-01/components/site-header.svelte` — the sticky header inside
  * `Sidebar.inset`, sized off the `--header-height` custom property the page sets on `Sidebar.provider`.
  */
object SiteHeader:

  def apply(openVar: Var[Boolean]): HtmlElement =
    headerTag(
      cls := "flex h-(--header-height) shrink-0 items-center gap-2 border-b transition-[width,height] ease-linear group-has-data-[collapsible=icon]/sidebar-wrapper:h-(--header-height)",
      div(
        cls := "flex w-full items-center gap-1 px-4 lg:gap-2 lg:px-6",
        Sidebar.trigger(openVar)(cls := "-ms-1"),
        Separator(Separator.Orientation.Vertical, cls := "mx-2 data-[orientation=vertical]:h-4"),
        h1(cls := "text-base font-medium", "Documents"),
        div(
          cls := "ms-auto flex items-center gap-2",
          Button.anchor(
            "https://github.com/shadcn-ui/ui/tree/main/apps/v4/app/(examples)/dashboard",
            Button.appearance(Button.Variant.Ghost, Button.Size.Sm),
            cls := "hidden sm:flex dark:text-foreground",
            target := "_blank",
            rel := "noopener noreferrer",
            "GitHub"
          )
        )
      )
    )
