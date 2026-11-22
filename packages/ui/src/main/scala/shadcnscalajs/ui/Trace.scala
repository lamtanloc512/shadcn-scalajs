package shadcnscalajs.ui

import com.raquo.laminar.api.L.*

/** shadcn/ui Trace — a compact, collapsible log of an agent's steps ("Searched 4 sites", "Read package.json").
  *
  * A line is a `span`; wrap it in a `button` or `a` and it becomes interactive (hover color, focus ring) without extra
  * props, exactly as upstream. [[collapsible]] nests detail under a line with the same height animation as
  * [[Collapsible]].
  */
object Trace:

  // Class lists shared with the light-DOM `sc-*` web component hosts.
  val rootClass: String =
    "cn-trace flex w-full min-w-0 flex-col gap-2 *:has-[>[data-slot=trace-line]]:flex *:has-[>[data-slot=trace-line]]:w-fit *:has-[>[data-slot=trace-line]]:max-w-full *:has-[>[data-slot=trace-line]]:min-w-0 *:has-[>[data-slot=trace-line]]:rounded-md *:has-[>[data-slot=trace-line]]:text-left *:has-[>[data-slot=trace-line]]:outline-none *:has-[>[data-slot=trace-line]]:focus-visible:ring-3 *:has-[>[data-slot=trace-line]]:focus-visible:ring-ring/50"

  val iconClass: String =
    "cn-trace-icon flex size-4 shrink-0 items-center justify-center [&_svg]:pointer-events-none [&_svg:not([class*='size-'])]:size-4 [[data-slot=trace-title]~&]:-ml-1"

  val titleClass: String =
    "cn-trace-title min-w-0 truncate motion-reduce:shimmer-none"

  val descriptionClass: String =
    "cn-trace-description shrink-0 text-muted-foreground/70 tabular-nums"

  val collapsibleClass: String =
    "cn-trace-collapsible flex w-full min-w-0 flex-col"

  val collapsibleTriggerClass: String =
    "cn-trace-collapsible-trigger group/trace-collapsible-trigger flex w-fit max-w-full min-w-0 cursor-pointer items-center gap-1 rounded-md border-0 bg-transparent p-0 text-left text-muted-foreground transition-colors outline-none hover:text-foreground focus-visible:ring-3 focus-visible:ring-ring/50 has-[[data-status=error]]:text-destructive"

  enum Status derives CanEqual:
    case Running, Error, Pending

  val collapsibleContentClass: String =
    "mt-2 mb-1 ml-[calc(--spacing(2)-1px)] min-w-0 border-l pr-1 pl-4 leading-relaxed wrap-break-word text-muted-foreground"

  /** The chevron's rotation keys off the trigger's `aria-expanded`. */
  val chevronClass: String = "transition-transform group-aria-expanded/trace-collapsible-trigger:rotate-90"

  def apply(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "trace",
      cls := rootClass,
      mods
    )

  val lineClass: String =
    "cn-trace-line flex w-fit max-w-full min-w-0 items-center gap-2 text-sm text-muted-foreground transition-colors in-[a,button]:cursor-pointer in-[a:hover,button:hover]:text-foreground data-[status=error]:text-destructive data-[status=error]:in-[a:hover,button:hover]:text-destructive data-[status=pending]:opacity-60 data-[status=running]:*:data-[slot=trace-title]:shimmer"

  def line(mods: Modifier[HtmlElement]*): HtmlElement =
    span(dataAttr("slot") := "trace-line", cls := lineClass, mods)

  def line(status: Status)(mods: Modifier[HtmlElement]*): HtmlElement =
    span(dataAttr("slot") := "trace-line", dataAttr("status") := status.toString.toLowerCase, cls := lineClass, mods)

  def line(status: Signal[Option[Status]])(mods: Modifier[HtmlElement]*): HtmlElement =
    span(
      dataAttr("slot") := "trace-line",
      dataAttr("status") <-- status.map(_.map(_.toString.toLowerCase).getOrElse("")),
      cls := lineClass,
      mods
    )

  def icon(mods: Modifier[HtmlElement]*): HtmlElement =
    span(
      dataAttr("slot") := "trace-icon",
      aria.hidden := true,
      cls := iconClass,
      mods
    )

  def title(mods: Modifier[HtmlElement]*): HtmlElement =
    span(dataAttr("slot") := "trace-title", cls := titleClass, mods)

  def description(mods: Modifier[HtmlElement]*): HtmlElement =
    span(
      dataAttr("slot") := "trace-description",
      cls := descriptionClass,
      mods
    )

  /** A line that discloses detail. `trigger` holds the line parts; a chevron is appended and rotates when open. */
  def collapsible(openVar: Var[Boolean] = Var(false))(
      trigger: Seq[Modifier[HtmlElement]],
      content: Seq[Modifier[HtmlElement]]
  ): HtmlElement =
    div(
      dataAttr("slot") := "trace-collapsible",
      dataAttr("state") <-- openVar.signal.map(if _ then "open" else "closed"),
      cls := collapsibleClass,
      button(
        typ := "button",
        dataAttr("slot") := "trace-collapsible-trigger",
        aria.expanded <-- openVar.signal,
        cls := collapsibleTriggerClass,
        onClick --> { _ => openVar.update(!_) },
        trigger,
        icon(
          cls := chevronClass,
          Icons.chevronRight()
        )
      ),
      Collapsible.panel(openVar)(
        dataAttr("slot") := "trace-collapsible-content",
        cls := "min-w-0",
        div(
          dataAttr("slot") := "trace-collapsible-content-inner",
          cls := collapsibleContentClass,
          content
        )
      )
    )
