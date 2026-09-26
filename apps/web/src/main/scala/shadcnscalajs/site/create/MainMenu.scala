package shadcnscalajs.site.create

import com.raquo.laminar.api.L.*
import org.scalajs.dom
import shadcnscalajs.ui.{Button, Icons, Kbd}

/** Header menu picker — navigate, shuffle, theme toggle, undo/redo, reset. Ported from `main-menu.svelte`. */
object MainMenu:

  private def isMacPlatform: Boolean =
    dom.window.navigator.platform.toLowerCase.contains("mac")

  def apply(state: CreateState, mods: Modifier[HtmlElement]*): HtmlElement =
    val undoShortcut = if isMacPlatform then "⌘Z" else "Ctrl+Z"
    val redoShortcut = if isMacPlatform then "⇧⌘Z" else "Ctrl+Shift+Z"
    val navigateShortcut = if isMacPlatform then "⌘P" else "Ctrl+P"
    val resetShortcut = if isMacPlatform then "⇧R" else "Shift+R"

    div(
      cls := "group/picker relative w-full",
      mods,
      Picker.root(cls := "relative w-full") { ctx =>
        div(
          Picker.trigger(ctx, Picker.TriggerVariant.Menu)(
            span(cls := "font-medium", "Menu"),
            Icons.menu(svg.cls := "size-5")
          ),
          Picker.content(ctx, cls := "right-0 md:w-56")(
            Picker.group()(
              Picker.item(ctx, () => (), isItemDisabled = Val(true))(
                "Navigate...",
                Picker.shortcut(navigateShortcut)
              ),
              Picker.item(ctx, () => state.randomize())(
                "Shuffle",
                Picker.shortcut("R")
              ),
              Picker.item(ctx, () => state.toggleDark())(
                "Light/Dark",
                Picker.shortcut("D")
              )
            ),
            Picker.separator(),
            Picker.group()(
              Picker.item(ctx, () => state.undo(), isItemDisabled = state.canUndo.map(!_))(
                "Undo",
                Picker.shortcut(undoShortcut)
              ),
              Picker.item(ctx, () => state.redo(), isItemDisabled = state.canRedo.map(!_))(
                "Redo",
                Picker.shortcut(redoShortcut)
              ),
              Picker.separator(),
              Picker.item(ctx, () => state.reset())(
                "Reset",
                Picker.shortcut(resetShortcut)
              )
            )
          )
        )
      }
    )

  /** Footer shuffle control — port of `random-button.svelte` (non-submenu variant). */
  def shuffleButton(state: CreateState): HtmlElement =
    Button.of(
      _.variant(Button.Variant.Outline),
      _.size(Button.Size.Default),
      _ =>
        cls := "flex-1 touch-manipulation bg-transparent! px-2! py-0! text-sm! transition-none select-none hover:bg-muted! md:flex-none pointer-coarse:h-10!",
      _ => onClick --> { (_: dom.MouseEvent) => state.randomize() },
      _ => span(cls := "w-full text-center font-medium", "Shuffle")
    )

  /** Footer reset control — port of `reset-button.svelte` (non-submenu variant). */
  def resetButton(state: CreateState): HtmlElement =
    Button.of(
      _.variant(Button.Variant.Ghost),
      _.size(Button.Size.Sm),
      _ =>
        cls := "h-[calc(--spacing(13.5))] w-[140px] touch-manipulation justify-between rounded-xl border border-foreground/10 bg-muted/50 select-none focus-visible:border-transparent focus-visible:ring-1 sm:rounded-lg md:w-full md:rounded-lg md:border-transparent md:bg-transparent md:pr-3.5! md:pl-2!",
      _ => onClick --> { (_: dom.MouseEvent) => state.reset() },
      _ =>
        div(
          cls := "flex flex-col justify-start text-left",
          div(cls := "text-xs text-muted-foreground", "Reset"),
          div(cls := "text-sm font-medium text-foreground", "Start Over")
        ),
      _ => Icons.undo(svg.cls := "-translate-x-0.5")
    )
