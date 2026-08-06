package shadcnscalajs.ui

import com.raquo.laminar.api.L.*
import com.raquo.laminar.api.L.svg.{path as svgPath, svg as svgTag}
import org.scalajs.dom

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

  /** Sidebar context wrapper — sets `--sidebar-width` / `--sidebar-width-icon` and handles Cmd/Ctrl+B toggle. */
  def provider(openVar: Var[Boolean] = Var(true))(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "sidebar-wrapper",
      styleAttr := "--sidebar-width: 16rem; --sidebar-width-icon: 3rem;",
      cls := "group/sidebar-wrapper flex min-h-svh w-full has-data-[variant=inset]:bg-sidebar",
      windowEvents(_.onKeyDown)
        .filter(ev => ev.key == "b" && (ev.metaKey || ev.ctrlKey))
        .--> { (ev: dom.KeyboardEvent) =>
          ev.preventDefault()
          openVar.update(!_)
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
        dataAttr("state") <-- openVar.signal.map(if _ then "expanded" else "collapsed"),
        dataAttr("collapsible") <-- openVar.signal.map(open => if open then "" else collapsibleName(collapsible)),
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

  def groupContent(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "sidebar-group-content",
      dataAttr("sidebar") := "group-content",
      cls := "cn-sidebar-group-content w-full",
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

  object MenuButtonApi:
    def variant(value: MenuButtonVariant): Modifier[HtmlElement] =
      val name = value.toString.toLowerCase
      Seq(dataAttr("variant") := name, cls(menuButtonVariantClasses(value)))

    def size(value: MenuButtonSize): Modifier[HtmlElement] =
      val name = value.toString.toLowerCase
      Seq(dataAttr("size") := name, cls(menuButtonSizeClasses(value)))

  def menuBadge(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "sidebar-menu-badge",
      dataAttr("sidebar") := "menu-badge",
      cls := "cn-sidebar-menu-badge flex items-center justify-center tabular-nums select-none group-data-[collapsible=icon]:hidden",
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

  def rail(openVar: Var[Boolean])(mods: Modifier[HtmlElement]*): HtmlElement =
    button(
      typ := "button",
      dataAttr("sidebar") := "rail",
      dataAttr("slot") := "sidebar-rail",
      aria.label := "Toggle Sidebar",
      tabIndex := -1,
      title := "Toggle Sidebar",
      cls := "cn-sidebar-rail absolute inset-y-0 z-20 hidden w-4 -translate-x-1/2 transition-all ease-linear group-data-[side=left]:-right-4 group-data-[side=right]:left-0 after:absolute after:inset-y-0 after:left-1/2 after:w-[2px] sm:flex in-data-[side=left]:cursor-w-resize in-data-[side=right]:cursor-e-resize [[data-side=left][data-state=collapsed]_&]:cursor-e-resize [[data-side=right][data-state=collapsed]_&]:cursor-w-resize group-data-[collapsible=offcanvas]:translate-x-0 group-data-[collapsible=offcanvas]:after:left-full hover:group-data-[collapsible=offcanvas]:bg-sidebar [[data-side=left][data-collapsible=offcanvas]_&]:-right-2 [[data-side=right][data-collapsible=offcanvas]_&]:-left-2",
      onClick --> { _ => openVar.update(!_) },
      mods
    )

  def trigger(openVar: Var[Boolean])(mods: Modifier[HtmlElement]*): HtmlElement =
    Button.of(
      _.variant(Button.Variant.Ghost),
      _.size(Button.Size.IconSm),
      _ => dataAttr("sidebar") := "trigger",
      _ => dataAttr("slot") := "sidebar-trigger",
      _ => cls := "cn-sidebar-trigger",
      _ => onClick --> { _ => openVar.update(!_) },
      _ => panelLeftIcon,
      _ => span(cls := "sr-only", "Toggle Sidebar"),
      _ => mods
    )
