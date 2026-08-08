package shadcnscalajs.ui

import com.raquo.laminar.api.L.*

/** Alert dialog composition over the native Laminar Dialog primitive.
  *
  * The utility classes must stand alone: `modules/ui` is what the CLI copies into consumer projects, which have neither
  * the vendored basecoat CSS nor `shadcn-presets.generated.css`. The `cn-alert-dialog*` hook classes and `data-slot`
  * attributes are additionally what the style-pack presets target — packs are unlayered, so where a pack defines a rule
  * it overrides the utilities below by design.
  */
object AlertDialog:

  private val contentBase: String =
    "cn-alert-dialog-content group/alert-dialog-content fixed top-1/2 left-1/2 z-50 grid w-full -translate-x-1/2 -translate-y-1/2 gap-6 rounded-xl bg-popover p-6 text-popover-foreground outline-none ring-1 ring-foreground/10 duration-100 data-[size=default]:max-w-xs data-[size=sm]:max-w-xs data-[size=default]:sm:max-w-md data-open:animate-in data-open:fade-in-0 data-open:zoom-in-95 data-closed:animate-out data-closed:fade-out-0 data-closed:zoom-out-95"

  def apply(isOpenVar: Var[Boolean])(mods: Modifier[HtmlElement]*): HtmlElement =
    Dialog.element(isOpenVar, "alert-dialog", "alert-dialog-content", contentBase)(
      dataAttr("size") := "default",
      mods
    )

  def overlay(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "alert-dialog-overlay",
      cls := "cn-alert-dialog-overlay fixed inset-0 z-50 bg-black/10 duration-100 supports-backdrop-filter:backdrop-blur-xs data-open:animate-in data-open:fade-in-0 data-closed:animate-out data-closed:fade-out-0",
      mods
    )

  def header(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "alert-dialog-header",
      cls := "cn-alert-dialog-header grid grid-rows-[auto_1fr] place-items-center gap-1.5 text-center has-data-[slot=alert-dialog-media]:grid-rows-[auto_auto_1fr] has-data-[slot=alert-dialog-media]:gap-x-4 sm:group-data-[size=default]/alert-dialog-content:place-items-start sm:group-data-[size=default]/alert-dialog-content:text-left sm:group-data-[size=default]/alert-dialog-content:has-data-[slot=alert-dialog-media]:grid-rows-[auto_1fr]",
      mods
    )

  def media(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "alert-dialog-media",
      cls := "cn-alert-dialog-media bg-muted mb-2 inline-flex size-10 items-center justify-center rounded-none sm:group-data-[size=default]/alert-dialog-content:row-span-2 *:[svg:not([class*='size-'])]:size-6",
      mods
    )

  def footer(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "alert-dialog-footer",
      cls := "cn-alert-dialog-footer flex flex-col-reverse gap-2 group-data-[size=sm]/alert-dialog-content:grid group-data-[size=sm]/alert-dialog-content:grid-cols-2 sm:flex-row sm:justify-end",
      mods
    )

  def title(mods: Modifier[HtmlElement]*): HtmlElement =
    h2(
      dataAttr("slot") := "alert-dialog-title",
      cls := "cn-font-heading cn-alert-dialog-title text-sm font-medium sm:group-data-[size=default]/alert-dialog-content:group-has-data-[slot=alert-dialog-media]/alert-dialog-content:col-start-2",
      mods
    )

  def description(mods: Modifier[HtmlElement]*): HtmlElement =
    p(
      dataAttr("slot") := "alert-dialog-description",
      cls := "cn-alert-dialog-description text-muted-foreground text-xs/relaxed text-balance md:text-pretty *:[a]:underline *:[a]:underline-offset-3 *:[a]:hover:text-foreground",
      mods
    )

  def action(mods: Modifier[HtmlElement]*): HtmlElement =
    Button
      .of(
        _.variant(Button.Variant.Primary),
        _ => dataAttr("slot") := "alert-dialog-action",
        _ => cls := "cn-alert-dialog-action"
      )
      .amend(mods)

  def cancel(mods: Modifier[HtmlElement]*): HtmlElement =
    Button
      .of(
        _.variant(Button.Variant.Outline),
        _ => dataAttr("slot") := "alert-dialog-cancel",
        _ => cls := "cn-alert-dialog-cancel"
      )
      .amend(mods)
