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
  *
  * `*Slot` / `*Class` vals are the single source of truth for Laminar helpers and the light-DOM `sc-card*` web
  * components — keep them in sync by editing only these constants.
  */
object Card:

  val rootSlot = "card"
  val headerSlot = "card-header"
  val titleSlot = "card-title"
  val descriptionSlot = "card-description"
  val actionSlot = "card-action"
  val contentSlot = "card-content"
  val footerSlot = "card-footer"

  val rootClass =
    "cn-card group/card flex flex-col gap-6 overflow-hidden rounded-xl bg-card py-6 text-sm text-card-foreground shadow-xs ring-1 ring-foreground/10"
  val headerClass =
    "cn-card-header group/card-header @container/card-header grid auto-rows-min items-start gap-1.5 px-6 has-data-[slot=card-action]:grid-cols-[1fr_auto] has-data-[slot=card-description]:grid-rows-[auto_auto]"
  val titleClass = "cn-font-heading cn-card-title leading-none font-semibold"
  val descriptionClass = "cn-card-description text-sm text-muted-foreground"
  val actionClass = "cn-card-action col-start-2 row-span-2 row-start-1 self-start justify-self-end"
  val contentClass = "cn-card-content px-6"
  val footerClass = "cn-card-footer flex items-center px-6"

  def apply(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := rootSlot,
      dataAttr("size") := "default",
      cls := rootClass,
      mods
    )

  def header(mods: Modifier[HtmlElement]*): HtmlElement =
    headerTag(
      dataAttr("slot") := headerSlot,
      cls := headerClass,
      mods
    )

  def title(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := titleSlot,
      cls := titleClass,
      mods
    )

  def description(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := descriptionSlot,
      cls := descriptionClass,
      mods
    )

  def action(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := actionSlot,
      cls := actionClass,
      mods
    )

  def content(mods: Modifier[HtmlElement]*): HtmlElement =
    sectionTag(
      dataAttr("slot") := contentSlot,
      cls := contentClass,
      mods
    )

  def footer(mods: Modifier[HtmlElement]*): HtmlElement =
    footerTag(
      dataAttr("slot") := footerSlot,
      cls := footerClass,
      mods
    )
