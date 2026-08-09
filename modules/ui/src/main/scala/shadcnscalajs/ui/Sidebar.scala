package shadcnscalajs.ui

import com.raquo.laminar.api.L.*
import com.raquo.laminar.api.L.svg.{path as svgPath, svg as svgTag}
import org.scalajs.dom

import scala.collection.mutable
import scala.scalajs.js

/** shadcn/ui Sidebar — layout primitives matching the canonical new-york-v4 sidebar components.
  *
  * Use [[provider]] + [[root]] for composed sidebars (see preview-02 `sidebar-nav`). The root carries `cn-menu-target`
  * so `ThemeConfig.applyToDocument` can toggle menu color / translucent hooks.
  */
object Sidebar:

  enum Collapsible derives CanEqual:
    case None, Offcanvas, Icon

  enum Side derives CanEqual:
    case Left, Right

  enum Variant derives CanEqual:
    case Sidebar, Floating, Inset

  enum MenuButtonVariant derives CanEqual:
    case Default, Outline

  enum MenuButtonSize derives CanEqual:
    case Default, Sm, Lg

  enum MenuSubButtonSize derives CanEqual:
    case Sm, Md

  private val CookieName = "sidebar_state"
  private val CookieMaxAge = 60 * 60 * 24 * 7
  private val WidthMobile = "18rem"

  // Rail drag bounds, in pixels. `CollapsedWidthThreshold` sits above the icon rail width so a measurement can tell a
  // collapsed sidebar from an expanded one; `DragThreshold` is the slop that separates a click from a drag.
  private val MinWidth = 160.0
  private val MaxWidth = 480.0
  private val CollapsedWidthThreshold = 64.0
  private val DragThreshold = 3.0

  private val menuButtonBase: String =
    "cn-sidebar-menu-button peer/menu-button group/menu-button flex w-full items-center overflow-hidden outline-hidden disabled:pointer-events-none disabled:opacity-50 aria-disabled:pointer-events-none aria-disabled:opacity-50 [&_svg]:size-4 [&_svg]:shrink-0 [&>span:last-child]:truncate"

  private val menuButtonVariantClasses: Map[MenuButtonVariant, String] = Map(
    MenuButtonVariant.Default -> "cn-sidebar-menu-button-variant-default",
    MenuButtonVariant.Outline -> "cn-sidebar-menu-button-variant-outline"
  )

  private val menuButtonSizeClasses: Map[MenuButtonSize, String] = Map(
    MenuButtonSize.Default -> "cn-sidebar-menu-button-size-default",
    MenuButtonSize.Sm -> "cn-sidebar-menu-button-size-sm",
    MenuButtonSize.Lg -> "cn-sidebar-menu-button-size-lg"
  )

  private val contexts = mutable.Map.empty[Var[Boolean], SidebarContext]

  private val isMobileVar = Var(dom.window.matchMedia("(max-width: 767px)").matches)
  private val isMobile: Signal[Boolean] =
    val mq = dom.window.matchMedia("(max-width: 767px)")
    val listener: js.Function1[dom.Event, Unit] = (_: dom.Event) => isMobileVar.set(mq.matches)
    mq.addEventListener("change", listener)
    isMobileVar.signal

  /** Shared sidebar state — created by [[provider]] and keyed by `openVar` for [[root]] / [[trigger]] / [[rail]]. */
  final class SidebarContext private[Sidebar] (
      val openVar: Var[Boolean],
      val openMobileVar: Var[Boolean]
  ):
    def state: Signal[String] = openVar.signal.map(if _ then "expanded" else "collapsed")

    def setOpen(open: Boolean): Unit =
      openVar.set(open)
      Sidebar.writeCookieOpen(open)

    def setOpenMobile(open: Boolean): Unit =
      openMobileVar.set(open)

    def toggle(): Unit =
      if isMobileVar.now() then openMobileVar.update(!_)
      else setOpen(!openVar.now())

  private def readCookieOpen(default: Boolean = true): Boolean =
    val prefix = s"$CookieName="
    dom.document.cookie
      .split(";")
      .iterator
      .map(_.trim)
      .find(_.startsWith(prefix))
      .fold(default)(entry => entry.substring(prefix.length) == "true")

  private def writeCookieOpen(open: Boolean): Unit =
    dom.document.cookie = s"$CookieName=$open; path=/; max-age=$CookieMaxAge"

  private def contextFor(openVar: Var[Boolean]): SidebarContext =
    contexts.getOrElseUpdate(
      openVar,
      SidebarContext(openVar, Var(false))
    )

  private def collapsibleName(value: Collapsible): String = value match
    case Collapsible.None      => "none"
    case Collapsible.Offcanvas => "offcanvas"
    case Collapsible.Icon      => "icon"

  private def sideName(value: Side): String = value match
    case Side.Left  => "left"
    case Side.Right => "right"

  private def variantName(value: Variant): String = value match
    case Variant.Sidebar  => "sidebar"
    case Variant.Floating => "floating"
    case Variant.Inset    => "inset"

  private def sheetSideName(value: Side): String = sideName(value)

  private def panelLeftIcon: SvgElement =
    svgTag(
      svg.cls := "size-4",
      svg.viewBox := "0 0 24 24",
      svg.fill := "none",
      svg.stroke := "currentColor",
      svg.strokeWidth := "2",
      svg.strokeLineCap := "round",
      svg.strokeLineJoin := "round",
      svgPath(svg.d := "M19 3H5a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V5a2 2 0 0 0-2-2z"),
      svgPath(svg.d := "M9 3v18")
    )

  /** Sidebar context wrapper — sets `--sidebar-width` / `--sidebar-width-icon`, persists open state to cookie, and
    * handles Cmd/Ctrl+B toggle.
    */
  def provider(openVar: Var[Boolean] = Var(readCookieOpen()))(mods: Modifier[HtmlElement]*): HtmlElement =
    val ctx = contextFor(openVar)
    div(
      dataAttr("slot") := "sidebar-wrapper",
      styleAttr := "--sidebar-width: 16rem; --sidebar-width-icon: 3rem;",
      cls := "group/sidebar-wrapper flex min-h-svh w-full has-data-[variant=inset]:bg-sidebar",
      windowEvents(_.onKeyDown)
        .filter(ev => ev.key == "b" && (ev.metaKey || ev.ctrlKey))
        .--> { (ev: dom.KeyboardEvent) =>
          ev.preventDefault()
          ctx.toggle()
        },
      mods
    )

  /** Root sidebar container. Defaults to `collapsible = None` for simple embedded sidebars. */
  def apply(mods: Modifier[HtmlElement]*): HtmlElement = root()(mods*)

  def root(
      collapsible: Collapsible = Collapsible.None,
      side: Side = Side.Left,
      variant: Variant = Variant.Sidebar,
      openVar: Var[Boolean] = Var(true)
  )(mods: Modifier[HtmlElement]*): HtmlElement =
    if collapsible == Collapsible.None then
      div(
        dataAttr("slot") := "sidebar",
        cls := "cn-menu-target flex h-full w-(--sidebar-width) flex-col bg-sidebar text-sidebar-foreground",
        mods
      )
    else
      val ctx = contextFor(openVar)
      div(
        child <-- isMobile.map { mobile =>
          if mobile then mobileRoot(side, ctx)(mods*)
          else desktopRoot(collapsible, side, variant, ctx)(mods*)
        }
      )

  /** On mobile the sidebar is a [[Drawer]] sliding in from its own side, rather than an inline panel. */
  private def mobileRoot(side: Side, ctx: SidebarContext)(mods: Modifier[HtmlElement]*): HtmlElement =
    val direction = if side == Side.Left then Drawer.Direction.Left else Drawer.Direction.Right
    Drawer(ctx.openMobileVar, direction)(
      styleAttr := s"--sidebar-width: $WidthMobile;",
      // Drawer sizes its side panels `w-3/4 sm:max-w-sm` in the popover palette; the sidebar has its own width token
      // and surface colors, and `!` is what wins against the component's own utilities.
      cls := "w-(--sidebar-width)! max-w-(--sidebar-width)! bg-sidebar! p-0 text-sidebar-foreground",
      div(cls := "sr-only", h2("Sidebar"), p("Displays the mobile sidebar.")),
      // The sidebar markers go here, not on the drawer's content div: `Dialog.element` finds that div by its
      // `data-slot="drawer-content"` to drive the enter/exit animation, so overwriting the slot would break it.
      div(
        dataAttr("sidebar") := "sidebar",
        dataAttr("slot") := "sidebar",
        dataAttr("mobile") := "true",
        dataAttr("side") := sheetSideName(side),
        cls := "flex h-full w-full flex-col",
        mods
      )
    )

  private def desktopRoot(
      collapsible: Collapsible,
      side: Side,
      variant: Variant,
      ctx: SidebarContext
  )(mods: Modifier[HtmlElement]*): HtmlElement =
    val isFloatingOrInset = variant == Variant.Floating || variant == Variant.Inset
    val gapIconWidth =
      if isFloatingOrInset then "group-data-[collapsible=icon]:w-[calc(var(--sidebar-width-icon)+(--spacing(4)))]"
      else "group-data-[collapsible=icon]:w-(--sidebar-width-icon)"
    val containerVariantClasses =
      if isFloatingOrInset then
        "p-2 group-data-[collapsible=icon]:w-[calc(var(--sidebar-width-icon)+(--spacing(4))+2px)]"
      else
        "group-data-[collapsible=icon]:w-(--sidebar-width-icon) group-data-[side=left]:border-e group-data-[side=right]:border-s"
    val containerSideClasses =
      if side == Side.Left then "start-0 group-data-[collapsible=offcanvas]:start-[calc(var(--sidebar-width)*-1)]"
      else "end-0 group-data-[collapsible=offcanvas]:end-[calc(var(--sidebar-width)*-1)]"

    div(
      cls := "group peer hidden text-sidebar-foreground md:block",
      dataAttr("state") <-- ctx.state,
      dataAttr("collapsible") <-- ctx.openVar.signal.map(open => if open then "" else collapsibleName(collapsible)),
      dataAttr("variant") := variantName(variant),
      dataAttr("side") := sideName(side),
      dataAttr("slot") := "sidebar",
      div(
        dataAttr("slot") := "sidebar-gap",
        cls := s"cn-sidebar-gap relative w-(--sidebar-width) bg-transparent group-data-[collapsible=offcanvas]:w-0 group-data-[side=right]:rotate-180 $gapIconWidth"
      ),
      div(
        dataAttr("slot") := "sidebar-container",
        cls := s"fixed inset-y-0 z-10 hidden h-svh w-(--sidebar-width) transition-[left,right,width] duration-200 ease-linear md:flex $containerSideClasses $containerVariantClasses",
        div(
          dataAttr("sidebar") := "sidebar",
          dataAttr("slot") := "sidebar-inner",
          cls := "cn-sidebar-inner cn-menu-target flex size-full flex-col",
          mods
        )
      )
    )

  def inset(mods: Modifier[HtmlElement]*): HtmlElement =
    mainTag(
      dataAttr("slot") := "sidebar-inset",
      cls := "cn-sidebar-inset relative flex w-full flex-1 flex-col",
      mods
    )

  def header(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "sidebar-header",
      dataAttr("sidebar") := "header",
      cls := "cn-sidebar-header flex flex-col",
      mods
    )

  def content(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "sidebar-content",
      dataAttr("sidebar") := "content",
      cls := "cn-sidebar-content flex min-h-0 flex-1 flex-col overflow-auto group-data-[collapsible=icon]:overflow-hidden",
      mods
    )

  def footer(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "sidebar-footer",
      dataAttr("sidebar") := "footer",
      cls := "cn-sidebar-footer flex flex-col",
      mods
    )

  def group(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "sidebar-group",
      dataAttr("sidebar") := "group",
      cls := "cn-sidebar-group relative flex w-full min-w-0 flex-col",
      mods
    )

  def groupLabel(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "sidebar-group-label",
      dataAttr("sidebar") := "group-label",
      cls := "cn-sidebar-group-label flex shrink-0 items-center outline-hidden [&>svg]:shrink-0",
      mods
    )

  def groupAction(mods: Modifier[HtmlElement]*): HtmlElement =
    button(
      typ := "button",
      dataAttr("slot") := "sidebar-group-action",
      dataAttr("sidebar") := "group-action",
      cls := "cn-sidebar-group-action flex aspect-square items-center justify-center outline-hidden transition-transform group-data-[collapsible=icon]:hidden after:absolute after:-inset-2 md:after:hidden [&>svg]:shrink-0",
      mods
    )

  def groupContent(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "sidebar-group-content",
      dataAttr("sidebar") := "group-content",
      cls := "cn-sidebar-group-content w-full",
      mods
    )

  def input(mods: Modifier[HtmlElement]*): HtmlElement =
    Input(
      dataAttr("slot") := "sidebar-input",
      dataAttr("sidebar") := "input",
      cls := "cn-sidebar-input",
      mods
    )

  def menu(mods: Modifier[HtmlElement]*): HtmlElement =
    ul(
      dataAttr("slot") := "sidebar-menu",
      dataAttr("sidebar") := "menu",
      cls := "cn-sidebar-menu flex w-full min-w-0 flex-col",
      mods
    )

  /** Back-compat helper: wraps content in a menu button inside a list item. */
  def menuItem(mods: Modifier[HtmlElement]*): HtmlElement =
    li(
      dataAttr("slot") := "sidebar-menu-item",
      dataAttr("sidebar") := "menu-item",
      cls := "group/menu-item relative",
      menuButton()(mods*)
    )

  def menuButton(isActive: Boolean = false)(mods: Modifier[HtmlElement]*): HtmlElement =
    button(
      typ := "button",
      dataAttr("slot") := "sidebar-menu-button",
      dataAttr("sidebar") := "menu-button",
      dataAttr("variant") := "default",
      dataAttr("size") := "default",
      cls := s"$menuButtonBase ${menuButtonVariantClasses(MenuButtonVariant.Default)} ${menuButtonSizeClasses(MenuButtonSize.Default)}",
      if isActive then dataAttr("active") := "true" else emptyMod,
      mods
    )

  def menuButtonOf(isActive: Boolean = false)(mods: (MenuButtonApi.type => Modifier[HtmlElement])*): HtmlElement =
    button(
      typ := "button",
      dataAttr("slot") := "sidebar-menu-button",
      dataAttr("sidebar") := "menu-button",
      cls := menuButtonBase,
      if isActive then dataAttr("active") := "true" else emptyMod,
      mods.map(_(MenuButtonApi))
    )

  /** Anchor-rendered menu button, for rows that navigate. Upstream spreads the menu-button props onto an `<a>` via a
    * child snippet; nesting an `<a>` inside [[menuButton]]'s `<button>` instead is invalid markup and drops the anchor
    * out of the button's flex row.
    */
  def menuButtonLink(
      isActive: Boolean = false,
      variant: MenuButtonVariant = MenuButtonVariant.Default,
      size: MenuButtonSize = MenuButtonSize.Default
  )(mods: Modifier[HtmlElement]*): HtmlElement =
    a(
      dataAttr("slot") := "sidebar-menu-button",
      dataAttr("sidebar") := "menu-button",
      dataAttr("variant") := variant.toString.toLowerCase,
      dataAttr("size") := size.toString.toLowerCase,
      cls := s"$menuButtonBase ${menuButtonVariantClasses(variant)} ${menuButtonSizeClasses(size)}",
      if isActive then dataAttr("active") := "true" else emptyMod,
      mods
    )

  object MenuButtonApi:
    def variant(value: MenuButtonVariant): Modifier[HtmlElement] =
      val name = value.toString.toLowerCase
      Seq(dataAttr("variant") := name, cls(menuButtonVariantClasses(value)))

    def size(value: MenuButtonSize): Modifier[HtmlElement] =
      val name = value.toString.toLowerCase
      Seq(dataAttr("size") := name, cls(menuButtonSizeClasses(value)))

  def menuAction(showOnHover: Boolean = false)(mods: Modifier[HtmlElement]*): HtmlElement =
    button(
      typ := "button",
      dataAttr("slot") := "sidebar-menu-action",
      dataAttr("sidebar") := "menu-action",
      cls := s"cn-sidebar-menu-action flex items-center justify-center outline-hidden transition-transform group-data-[collapsible=icon]:hidden after:absolute after:-inset-2 md:after:hidden [&>svg]:shrink-0${if showOnHover then " group-focus-within/menu-item:opacity-100 group-hover/menu-item:opacity-100 peer-data-active/menu-button:text-sidebar-accent-foreground md:opacity-0 data-open:opacity-100" else ""}",
      mods
    )

  def menuBadge(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "sidebar-menu-badge",
      dataAttr("sidebar") := "menu-badge",
      cls := "cn-sidebar-menu-badge flex items-center justify-center tabular-nums select-none group-data-[collapsible=icon]:hidden",
      mods
    )

  def menuSkeleton(showIcon: Boolean = false)(mods: Modifier[HtmlElement]*): HtmlElement =
    val width = s"${50 + scala.util.Random.nextInt(40)}%"
    div(
      dataAttr("slot") := "sidebar-menu-skeleton",
      dataAttr("sidebar") := "menu-skeleton",
      cls := "cn-sidebar-menu-skeleton flex items-center",
      if showIcon then Skeleton(dataAttr("sidebar") := "menu-skeleton-icon", cls := "cn-sidebar-menu-skeleton-icon")
      else emptyMod,
      Skeleton(
        dataAttr("sidebar") := "menu-skeleton-text",
        cls := "cn-sidebar-menu-skeleton-text max-w-(--skeleton-width) flex-1",
        styleAttr := s"--skeleton-width: $width;"
      ),
      mods
    )

  def menuSub(mods: Modifier[HtmlElement]*): HtmlElement =
    ul(
      dataAttr("slot") := "sidebar-menu-sub",
      dataAttr("sidebar") := "menu-sub",
      cls := "cn-sidebar-menu-sub flex min-w-0 flex-col",
      mods
    )

  def menuSubItem(mods: Modifier[HtmlElement]*): HtmlElement =
    li(
      dataAttr("slot") := "sidebar-menu-sub-item",
      dataAttr("sidebar") := "menu-sub-item",
      cls := "group/menu-sub-item relative",
      mods
    )

  def menuSubButton(isActive: Boolean = false, size: MenuSubButtonSize = MenuSubButtonSize.Md)(
      mods: Modifier[HtmlElement]*
  ): HtmlElement =
    a(
      href := "#",
      dataAttr("slot") := "sidebar-menu-sub-button",
      dataAttr("sidebar") := "menu-sub-button",
      dataAttr("size") := size.toString.toLowerCase,
      if isActive then dataAttr("active") := "true" else emptyMod,
      cls := "cn-sidebar-menu-sub-button flex min-w-0 -translate-x-px items-center overflow-hidden outline-hidden group-data-[collapsible=icon]:hidden disabled:pointer-events-none disabled:opacity-50 aria-disabled:pointer-events-none aria-disabled:opacity-50 [&>span:last-child]:truncate [&>svg]:shrink-0",
      mods
    )

  def separator(mods: Modifier[HtmlElement]*): HtmlElement =
    Separator(
      Separator.Orientation.Horizontal,
      dataAttr("slot") := "sidebar-separator",
      dataAttr("sidebar") := "separator",
      cls := "cn-sidebar-separator w-auto",
      mods
    )

  /** Drag the rail to resize the sidebar, or click it to toggle.
    *
    * Upstream's `sidebar-rail` is click-only despite showing a resize cursor; dragging is an addition here. Width is
    * written to the wrapper's `--sidebar-width`, so it survives collapse/expand and is inherited by the gap and the
    * fixed container alike.
    */
  def rail(openVar: Var[Boolean])(mods: Modifier[HtmlElement]*): HtmlElement =
    val ctx = contextFor(openVar)
    var dragging = false
    var didDrag = false
    var startX = 0.0
    var startWidth = 256.0
    var lastExpandedWidth = 256.0
    var resizeDirection = 1.0
    var wrapper = Option.empty[dom.html.Element]
    var pendingWidth = 0.0
    var frameId = 0
    var frozen = List.empty[dom.html.Element]

    // Pointer moves arrive faster than the browser paints, and each width write invalidates the layout of the whole
    // page. Coalescing to one write per frame keeps the drag from queueing up redundant reflows.
    def scheduleWidth(width: Double): Unit =
      pendingWidth = width
      if frameId == 0 then
        frameId = dom.window.requestAnimationFrame { _ =>
          frameId = 0
          wrapper.foreach(_.style.setProperty("--sidebar-width", s"${pendingWidth}px"))
        }

    // The gap and the container ease `width` over 200ms, which is right for a toggle but wrong for a drag: every frame
    // would restart the animation, so the panel trails the pointer. Suspend the easing until the drag ends.
    def freezeTransitions(root: dom.Element): Unit =
      val nodes = root.querySelectorAll("[data-slot='sidebar-gap'],[data-slot='sidebar-container']")
      var i = 0
      var collected = List.empty[dom.html.Element]
      while i < nodes.length do
        val element = nodes.item(i).asInstanceOf[dom.html.Element]
        element.style.setProperty("transition", "none")
        collected = element :: collected
        i += 1
      frozen = collected

    def endDrag(): Unit =
      dragging = false
      frozen.foreach(_.style.removeProperty("transition"))
      frozen = Nil
      dom.document.body.style.removeProperty("user-select")
      wrapper = None

    button(
      typ := "button",
      dataAttr("sidebar") := "rail",
      dataAttr("slot") := "sidebar-rail",
      aria.label := "Resize or toggle Sidebar",
      tabIndex := -1,
      title := "Drag to resize or click to toggle Sidebar",
      cls := "cn-sidebar-rail absolute inset-y-0 z-20 hidden w-4 -translate-x-1/2 transition-all ease-linear group-data-[side=left]:-right-4 group-data-[side=right]:left-0 after:absolute after:inset-y-0 after:left-1/2 after:w-[2px] sm:flex in-data-[side=left]:cursor-w-resize in-data-[side=right]:cursor-e-resize [[data-side=left][data-state=collapsed]_&]:cursor-e-resize [[data-side=right][data-state=collapsed]_&]:cursor-w-resize group-data-[collapsible=offcanvas]:translate-x-0 group-data-[collapsible=offcanvas]:after:left-full hover:group-data-[collapsible=offcanvas]:bg-sidebar [[data-side=left][data-collapsible=offcanvas]_&]:-right-2 [[data-side=right][data-collapsible=offcanvas]_&]:-left-2",
      onMountCallback { mountCtx =>
        val sidebar = mountCtx.thisNode.ref.closest("[data-slot='sidebar']")
        if sidebar != null then
          val measured = sidebar.getBoundingClientRect().width
          if measured > CollapsedWidthThreshold then lastExpandedWidth = measured
      },
      onMouseDown --> { (ev: dom.MouseEvent) =>
        if ev.button == 0 then
          val railElement = ev.currentTarget.asInstanceOf[dom.html.Element]
          val sidebar = railElement.closest("[data-slot='sidebar']")
          wrapper = Option(railElement.closest("[data-slot='sidebar-wrapper']")).map(_.asInstanceOf[dom.html.Element])
          val measured = if sidebar == null then 0.0 else sidebar.getBoundingClientRect().width
          startX = ev.clientX
          // A collapsed sidebar measures the icon width, which would make the drag jump; resume from the last
          // expanded width instead.
          startWidth = if openVar.now() && measured > CollapsedWidthThreshold then measured else lastExpandedWidth
          resizeDirection = if sidebar != null && sidebar.getAttribute("data-side") == "right" then -1.0 else 1.0
          dragging = true
          didDrag = false
          ev.preventDefault()
      },
      documentEvents(_.onMouseMove) --> { (ev: dom.MouseEvent) =>
        if dragging then
          val delta = (ev.clientX - startX) * resizeDirection
          if math.abs(delta) >= DragThreshold then
            if !didDrag then
              didDrag = true
              wrapper.foreach(freezeTransitions)
              dom.document.body.style.setProperty("user-select", "none")
            if !openVar.now() then ctx.setOpen(true)
            val nextWidth = (startWidth + delta).max(MinWidth).min(MaxWidth)
            lastExpandedWidth = nextWidth
            scheduleWidth(nextWidth)
            ev.preventDefault()
      },
      documentEvents(_.onMouseUp) --> { _ => if dragging then endDrag() },
      onClick --> { (ev: dom.MouseEvent) =>
        // A drag ends with a click on the rail; swallow it so resizing doesn't also collapse the sidebar.
        if didDrag then
          didDrag = false
          ev.preventDefault()
          ev.stopPropagation()
        else ctx.toggle()
      },
      mods
    )

  def trigger(openVar: Var[Boolean])(mods: Modifier[HtmlElement]*): HtmlElement =
    val ctx = contextFor(openVar)
    Button.of(
      _.variant(Button.Variant.Ghost),
      _.size(Button.Size.IconSm),
      _ => dataAttr("sidebar") := "trigger",
      _ => dataAttr("slot") := "sidebar-trigger",
      _ => cls := "cn-sidebar-trigger",
      _ => onClick --> { _ => ctx.toggle() },
      _ => panelLeftIcon,
      _ => span(cls := "sr-only", "Toggle Sidebar"),
      _ => mods
    )
