package shadcnscalajs.ui

import com.raquo.laminar.api.L.*

/** shadcn/ui Card primitives.
  *
  * The helpers intentionally accept Laminar modifiers so they can be composed with application-specific content without
  * introducing a React-style wrapper API.
  *
  * The utility classes must stand alone: `modules/ui` is what the CLI copies into consumer projects, which have neither
  * the vendored basecoat CSS nor `shadcn-presets.generated.css`. The `cn-card*` hook classes and `data-slot` attributes
  * are additionally what the style-pack presets target — packs are unlayered, so where a pack defines a rule it
  * overrides the utilities below by design.
  */
object Card:

  def apply(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "card",
      dataAttr("size") := "default",
      cls := "cn-card group/card flex flex-col gap-6 overflow-hidden rounded-xl bg-card py-6 text-sm text-card-foreground shadow-xs ring-1 ring-foreground/10",
      mods
    )

  def header(mods: Modifier[HtmlElement]*): HtmlElement =
    headerTag(
      dataAttr("slot") := "card-header",
      cls := "cn-card-header group/card-header @container/card-header grid auto-rows-min items-start gap-1.5 px-6 has-data-[slot=card-action]:grid-cols-[1fr_auto] has-data-[slot=card-description]:grid-rows-[auto_auto]",
      mods
    )

  def title(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "card-title",
      cls := "cn-font-heading cn-card-title leading-none font-semibold",
      mods
    )

  def description(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "card-description",
      cls := "cn-card-description text-sm text-muted-foreground",
      mods
    )

  def action(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "card-action",
      cls := "cn-card-action col-start-2 row-span-2 row-start-1 self-start justify-self-end",
      mods
    )

  def content(mods: Modifier[HtmlElement]*): HtmlElement =
    sectionTag(
      dataAttr("slot") := "card-content",
      cls := "cn-card-content px-6",
      mods
    )

  def footer(mods: Modifier[HtmlElement]*): HtmlElement =
    footerTag(
      dataAttr("slot") := "card-footer",
      cls := "cn-card-footer flex items-center px-6",
      mods
    )
