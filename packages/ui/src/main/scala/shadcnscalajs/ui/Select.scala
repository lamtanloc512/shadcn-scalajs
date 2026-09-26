package shadcnscalajs.ui

import com.raquo.laminar.api.L.*
import org.scalajs.dom

import scala.scalajs.js

/** shadcn/ui Select — a listbox: a button trigger showing the current value and a portaled panel of options.
  *
  * This replaces a native `<select>`, which cannot carry the trigger chrome, the check indicator, grouped labels, or
  * the open/close animation. The native element is still available as [[NativeSelect]], which is the same split
  * upstream makes.
  *
  * Items register their own label text on mount, so [[value]] can render the selected option's label without the caller
  * repeating it — the same reason upstream reads `SelectValue` out of context rather than from a prop.
  */
object Select:

  enum Size derives CanEqual:
    case Default, Sm

  val triggerClass: String =
    "cn-select-trigger flex w-full items-center justify-between gap-2 rounded-md border border-input bg-background px-3 py-2 text-sm whitespace-nowrap shadow-xs transition-[color,box-shadow] outline-none focus-visible:border-ring focus-visible:ring-[3px] focus-visible:ring-ring/50 disabled:cursor-not-allowed disabled:opacity-50 data-placeholder:text-muted-foreground data-[size=default]:h-9 data-[size=sm]:h-8 *:data-[slot=select-value]:line-clamp-1 *:data-[slot=select-value]:flex *:data-[slot=select-value]:items-center *:data-[slot=select-value]:gap-2 [&_svg]:pointer-events-none [&_svg]:shrink-0 [&_svg:not([class*='size-'])]:size-4"

  val contentClass: String =
    "cn-select-content cn-select-content-logical z-50 max-h-72 min-w-32 overflow-y-auto rounded-md border bg-popover text-popover-foreground shadow-md data-open:animate-in data-open:fade-in-0 data-open:zoom-in-95 data-closed:animate-out data-closed:fade-out-0 data-closed:zoom-out-95 data-[side=bottom]:slide-in-from-top-2 data-[side=top]:slide-in-from-bottom-2"

  val itemClass: String =
    "cn-select-item relative flex w-full cursor-default items-center gap-2 rounded-sm py-1.5 pe-8 ps-2 text-sm outline-hidden select-none focus:bg-accent focus:text-accent-foreground data-disabled:pointer-events-none data-disabled:opacity-50 [&_svg]:pointer-events-none [&_svg]:shrink-0 [&_svg:not([class*='size-'])]:size-4"

  /** One select instance: the panel's open state plus the selection, which every part reads. */
  final class Ctx private[ui] (
      private[ui] val anchor: Floating.Anchor,
      val selected: Var[String],
      private val labels: Var[Map[String, String]]
  ):
    /** The selected option's label, or `None` while nothing is chosen and the placeholder should show. */
    private[ui] def selectedLabel: Signal[Option[String]] =
      selected.signal.combineWith(labels.signal).map((v, map) => map.get(v).filter(_ => v.nonEmpty))

    def item(itemValue: String, mods: Modifier[HtmlElement]*): HtmlElement =
      div(
        dataAttr("slot") := "select-item",
        role := "option",
        tabIndex := -1,
        cls := itemClass,
        aria.selected <-- selected.signal.map(_ == itemValue),
        dataAttr("state") <-- selected.signal.map(v => if v == itemValue then "checked" else "unchecked"),
        // The label is read back out of the DOM so the trigger can show it without the caller passing it twice.
        onMountCallback { ctx =>
          val text = ctx.thisNode.ref.textContent.trim
          labels.update(_.updated(itemValue, text))
        },
        onPointerEnter --> { (ev: dom.PointerEvent) => ev.currentTarget.asInstanceOf[dom.html.Element].focus() },
        onClick --> { _ =>
          selected.set(itemValue)
          anchor.close()
          anchor.focusTrigger()
        },
        span(dataAttr("slot") := "select-item-text", cls := "cn-select-item-text flex flex-1 gap-2", mods),
        span(
          dataAttr("slot") := "select-item-indicator",
          cls := "cn-select-item-indicator pointer-events-none absolute end-2 flex size-3.5 items-center justify-center",
          cls("invisible") <-- selected.signal.map(_ != itemValue),
          Icons.check(svg.cls := "size-4")
        )
      )

    def group(mods: Modifier[HtmlElement]*): HtmlElement =
      div(dataAttr("slot") := "select-group", role := "group", cls := "cn-select-group p-1", mods)

  def label(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "select-label",
      cls := "cn-select-label px-2 py-1.5 text-xs text-muted-foreground",
      mods
    )

  def separator(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "select-separator",
      role := "separator",
      cls := "cn-select-separator pointer-events-none -mx-1 my-1 h-px bg-border",
      mods
    )

  /** Composable form: `build` receives the select's context and returns the panel's rows. */
  def apply(selectedVar: Var[String], placeholder: String = "Select…", size: Size = Size.Default)(
      build: Ctx => Seq[Modifier[HtmlElement]]
  ): HtmlElement =
    render(selectedVar, placeholder, size, Nil, build)

  /** Flat form for a plain list of `value -> label` pairs. */
  def stateful(selectedVar: Var[String], options: List[(String, String)], mods: Modifier[HtmlElement]*): HtmlElement =
    render(
      selectedVar,
      placeholder = "Select…",
      size = Size.Default,
      triggerMods = mods,
      build = ctx => Seq(ctx.group(options.map((optValue, optLabel) => ctx.item(optValue, optLabel))))
    )

  private def render(
      selectedVar: Var[String],
      placeholder: String,
      size: Size,
      triggerMods: Seq[Modifier[HtmlElement]],
      build: Ctx => Seq[Modifier[HtmlElement]]
  ): HtmlElement =
    val anchor = Floating.anchor()
    val ctx = Ctx(anchor, selectedVar, Var(Map.empty))
    val sizeName = size.toString.toLowerCase

    div(
      dataAttr("slot") := "select",
      cls := "select relative w-full",
      button(
        typ := "button",
        dataAttr("slot") := "select-trigger",
        dataAttr("size") := sizeName,
        role := "combobox",
        cls := triggerClass,
        aria.hasPopup := true,
        // Upstream styles the placeholder through `data-placeholder`, which has to be absent once something is selected:
        // the CSS matches the attribute's presence, so any value at all — even "false" — would keep the muted styling.
        onMountBind { mountCtx =>
          ctx.selectedLabel --> { selected =>
            val el = mountCtx.thisNode.ref
            if selected.isEmpty then el.setAttribute("data-placeholder", "") else el.removeAttribute("data-placeholder")
          }
        },
        Floating.triggerBase(anchor),
        Floating.clickToToggle(anchor),
        triggerMods,
        span(
          dataAttr("slot") := "select-value",
          cls := "cn-select-value flex flex-1 text-start",
          child.text <-- ctx.selectedLabel.map(_.getOrElse(placeholder))
        ),
        Icons
          .chevronDown(Icons.svgSlot := "select-trigger-icon", svg.cls := "cn-select-trigger-icon size-4 opacity-50"),
        onKeyDown --> { (ev: dom.KeyboardEvent) =>
          if ev.key == "ArrowDown" || ev.key == "ArrowUp" then
            ev.preventDefault()
            anchor.open()
        }
      ),
      Floating.content(
        anchor,
        Floating.Placement(side = Floating.Side.Bottom, align = Floating.Align.Start, matchTriggerWidth = true),
        contentClass
      )(
        dataAttr("slot") := "select-content",
        role := "listbox",
        tabIndex := -1,
        Floating.keyboardNav(anchor, "[role=option]:not([data-disabled])"),
        typeahead,
        div(cls := "p-1", build(ctx))
      )
    )

  /** Typing letters jumps to the first option starting with them, as a native `<select>` does. The buffer resets after
    * a pause so a new prefix starts a fresh search.
    */
  private def typeahead: Modifier[HtmlElement] =
    var buffer = ""
    var lastKeyAt = 0.0
    onKeyDown --> { (ev: dom.KeyboardEvent) =>
      if ev.key.length == 1 && !ev.ctrlKey && !ev.metaKey && !ev.altKey && ev.key != " " then
        val now = js.Date.now()
        buffer = if now - lastKeyAt > 700 then ev.key else buffer + ev.key
        lastKeyAt = now
        val panel = ev.currentTarget.asInstanceOf[dom.html.Element]
        panel
          .querySelectorAll("[role=option]:not([data-disabled])")
          .toList
          .collect { case el: dom.html.Element => el }
          .find(_.textContent.trim.toLowerCase.startsWith(buffer.toLowerCase))
          .foreach(_.focus())
    }
