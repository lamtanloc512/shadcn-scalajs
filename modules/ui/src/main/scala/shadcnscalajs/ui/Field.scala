package shadcnscalajs.ui

import com.raquo.laminar.api.L.*

/** shadcn/ui Field primitives — Tailwind utilities copied from the canonical new-york-v4 field.tsx. */
object Field:
  enum Orientation derives CanEqual:
    case Vertical, Horizontal, Responsive

    def attrValue: String = this match
      case Vertical   => "vertical"
      case Horizontal => "horizontal"
      case Responsive => "responsive"

  enum LegendVariant derives CanEqual:
    case Legend, Label

    def attrValue: String = this match
      case Legend => "legend"
      case Label  => "label"

  private val labelTag = htmlTag("label")
  private val fieldSetTag = htmlTag("fieldset")
  private val legendTag = htmlTag("legend")

  private val fieldBaseClasses =
    "group/field flex w-full gap-3 data-[invalid=true]:text-destructive"

  private def orientationClasses(orientation: Orientation): String =
    orientation match
      case Orientation.Vertical =>
        "flex-col [&>*]:w-full [&>.sr-only]:w-auto"
      case Orientation.Horizontal =>
        "flex-row items-center [&>[data-slot=field-label]]:flex-auto has-[>[data-slot=field-content]]:items-start has-[>[data-slot=field-content]]:[&>[role=checkbox],[role=radio]]:mt-px"
      case Orientation.Responsive =>
        "flex-col @md/field-group:flex-row @md/field-group:items-center [&>*]:w-full @md/field-group:[&>*]:w-auto [&>.sr-only]:w-auto @md/field-group:[&>[data-slot=field-label]]:flex-auto @md/field-group:has-[>[data-slot=field-content]]:items-start @md/field-group:has-[>[data-slot=field-content]]:[&>[role=checkbox],[role=radio]]:mt-px"

  /** field.tsx `Field` / `fieldVariants` — vertical orientation by default. */
  def apply(mods: Modifier[HtmlElement]*): HtmlElement =
    apply(Orientation.Vertical, mods*)

  def apply(orientation: Orientation, mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      role := "group",
      dataAttr("slot") := "field",
      dataAttr("orientation") := orientation.attrValue,
      cls := s"$fieldBaseClasses ${orientationClasses(orientation)}",
      mods
    )

  /** field.tsx `FieldSet`. */
  def set(mods: Modifier[HtmlElement]*): HtmlElement =
    fieldSetTag(
      dataAttr("slot") := "field-set",
      cls := "flex flex-col gap-6 has-[>[data-slot=checkbox-group]]:gap-3 has-[>[data-slot=radio-group]]:gap-3",
      mods
    )

  /** field.tsx `FieldLegend`. */
  def legend(mods: Modifier[HtmlElement]*): HtmlElement =
    legend(LegendVariant.Legend, mods*)

  def legend(text: String, mods: Modifier[HtmlElement]*): HtmlElement =
    legend(LegendVariant.Legend, text, mods*)

  def legend(variant: LegendVariant, mods: Modifier[HtmlElement]*): HtmlElement =
    legendTag(
      dataAttr("slot") := "field-legend",
      dataAttr("variant") := variant.attrValue,
      cls := "mb-3 font-medium data-[variant=legend]:text-base data-[variant=label]:text-sm",
      mods
    )

  def legend(variant: LegendVariant, text: String, mods: Modifier[HtmlElement]*): HtmlElement =
    legendTag(
      dataAttr("slot") := "field-legend",
      dataAttr("variant") := variant.attrValue,
      cls := "mb-3 font-medium data-[variant=legend]:text-base data-[variant=label]:text-sm",
      mods,
      text
    )

  /** field.tsx `FieldGroup`. */
  def group(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "field-group",
      cls := "group/field-group @container/field-group flex w-full flex-col gap-7 data-[slot=checkbox-group]:gap-3 [&>[data-slot=field-group]]:gap-4",
      mods
    )

  /** field.tsx `FieldContent`. */
  def content(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "field-content",
      cls := "group/field-content flex flex-1 flex-col gap-1.5 leading-snug",
      mods
    )

  private val labelClasses =
    "cn-field-label group/field-label peer/field-label flex w-fit items-center gap-2 text-sm leading-snug font-medium select-none group-data-[disabled=true]/field:opacity-50 has-[>[data-slot=field]]:w-full has-[>[data-slot=field]]:flex-col has-[>[data-slot=field]]:rounded-md has-[>[data-slot=field]]:border [&>*]:data-[slot=field]:p-4 has-data-[state=checked]:bg-primary/5 has-data-[state=checked]:border-primary dark:has-data-[state=checked]:bg-primary/10"

  /** field.tsx `FieldLabel` — wraps Label's classes plus the field-label additions. */
  def label(text: String, mods: Modifier[HtmlElement]*): HtmlElement =
    labelTag(
      dataAttr("slot") := "field-label",
      cls := labelClasses,
      mods,
      text
    )

  /** field.tsx `FieldTitle`. */
  def title(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "field-label",
      cls := "flex w-fit items-center gap-2 text-sm leading-snug font-medium group-data-[disabled=true]/field:opacity-50",
      mods
    )

  def title(text: String, mods: Modifier[HtmlElement]*): HtmlElement =
    title((mods :+ (text: Modifier[HtmlElement]))*)

  /** field.tsx `FieldDescription`. */
  def description(mods: Modifier[HtmlElement]*): HtmlElement =
    p(
      dataAttr("slot") := "field-description",
      cls := "text-sm leading-normal font-normal text-muted-foreground last:mt-0 [&>a]:underline [&>a]:underline-offset-4 [&>a:hover]:text-primary",
      mods
    )

  /** field.tsx `FieldSeparator`. */
  def separator(mods: Modifier[HtmlElement]*)(content: HtmlElement*): HtmlElement =
    div(
      dataAttr("slot") := "field-separator",
      dataAttr("content") := content.nonEmpty.toString,
      cls := "relative -my-2 h-5 text-sm group-data-[variant=outline]/field-group:-mb-2",
      mods,
      Separator(Separator.Orientation.Horizontal, cls := "absolute inset-0 top-1/2"),
      content.map { node =>
        span(
          dataAttr("slot") := "field-separator-content",
          cls := "relative mx-auto block w-fit bg-background px-2 text-muted-foreground",
          node
        )
      }
    )

  /** field-error.svelte `FieldError` — optional `errors` list and/or child content; renders nothing when empty. */
  def error(mods: Modifier[HtmlElement]*): Node =
    error(Nil, mods*)

  def error(errors: Seq[String], mods: Modifier[HtmlElement]*): Node =
    val messages = errors.flatMap(msg => Option(msg).filter(_.nonEmpty))
    if mods.isEmpty && messages.isEmpty then emptyNode
    else
      div(
        role := "alert",
        dataAttr("slot") := "field-error",
        cls := "cn-field-error font-normal text-sm text-destructive",
        if mods.nonEmpty then mods
        else if messages.length == 1 then messages.head
        else
          ul(
            cls := "ml-4 flex list-disc flex-col gap-1",
            messages.map(msg => li(msg))
          )
      )
