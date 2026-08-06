package shadcnscalajs.ui

import com.raquo.laminar.api.L.*

/** shadcn/ui Field primitives — Tailwind utilities copied from the canonical new-york-v4 field.tsx.
  *
  * Only the parts the shipped blocks use are ported: Field, FieldGroup, FieldLabel, FieldDescription (plus the
  * pre-existing error helper). FieldSet/FieldLegend/FieldSeparator/FieldContent/FieldTitle are not ported yet.
  */
object Field:
  private val labelTag = htmlTag("label")

  /** field.tsx `fieldVariants`, vertical orientation (the default). */
  def apply(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      role := "group",
      dataAttr("slot") := "field",
      dataAttr("orientation") := "vertical",
      cls := "field group/field flex w-full gap-3 data-[invalid=true]:text-destructive flex-col [&>*]:w-full [&>.sr-only]:w-auto",
      mods
    )

  /** field.tsx `FieldGroup`. */
  def group(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "field-group",
      cls := "group/field-group @container/field-group flex w-full flex-col gap-7 data-[slot=checkbox-group]:gap-3 [&>[data-slot=field-group]]:gap-4",
      mods
    )

  /** field.tsx `FieldLabel` — wraps Label's classes plus the field-label additions. */
  def label(text: String, mods: Modifier[HtmlElement]*): HtmlElement =
    labelTag(
      dataAttr("slot") := "field-label",
      cls := "flex items-center gap-2 text-sm leading-none font-medium select-none group/field-label peer/field-label w-fit leading-snug group-data-[disabled=true]/field:opacity-50",
      mods,
      text
    )

  /** field.tsx `FieldDescription`. */
  def description(mods: Modifier[HtmlElement]*): HtmlElement =
    p(
      dataAttr("slot") := "field-description",
      cls := "text-sm leading-normal font-normal text-muted-foreground last:mt-0 [&>a]:underline [&>a]:underline-offset-4 [&>a:hover]:text-primary",
      mods
    )

  def error(mods: Modifier[HtmlElement]*): HtmlElement = p(cls := "text-sm font-medium text-destructive", mods)
