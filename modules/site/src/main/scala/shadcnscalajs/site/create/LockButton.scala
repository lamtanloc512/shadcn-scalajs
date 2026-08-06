package shadcnscalajs.site.create

import com.raquo.laminar.api.L.*
import com.raquo.laminar.codecs.StringAsIsCodec
import shadcnscalajs.ui.Icons

/** Per-field lock toggle for create-page pickers. Ported from shadcn-svelte's `lock-button.svelte`. */
object LockButton:

  private val titleAttr = htmlAttr("title", StringAsIsCodec)

  private val lockLabels: Map[String, String] = Map(
    "style" -> "style",
    "baseColor" -> "base color",
    "theme" -> "theme",
    "chartColor" -> "chart color",
    "iconLibrary" -> "icon library",
    "font" -> "font",
    "fontHeading" -> "heading font",
    "menuAccent" -> "menu accent",
    "menuColor" -> "menu color",
    "radius" -> "radius"
  )

  def apply(state: CreateState, lockKey: String, mods: Modifier[HtmlElement]*): HtmlElement =
    val fieldLabel = lockLabels.getOrElse(lockKey, lockKey)
    button(
      typ := "button",
      cls := "cn-tooltip flex size-4 cursor-pointer items-center justify-center rounded opacity-0 transition-opacity group-focus-within/picker:opacity-100 group-hover/picker:opacity-100 focus-visible:opacity-100 data-[locked=true]:opacity-100 pointer-coarse:hidden",
      dataAttr("locked") <-- state.isLocked(lockKey).map(locked => if locked then "true" else "false"),
      titleAttr <-- state.isLocked(lockKey).map(locked => if locked then "Locked" else "Unlocked"),
      aria.label <-- state
        .isLocked(lockKey)
        .map(locked => if locked then s"Unlock $fieldLabel" else s"Lock $fieldLabel"),
      mods,
      child <-- state
        .isLocked(lockKey)
        .map(locked =>
          if locked then Icons.squareLock(svg.cls := "size-5 text-foreground")
          else Icons.squareUnlock(svg.cls := "size-5 text-foreground")
        ),
      onClick --> { _ => state.toggleLock(lockKey) }
    )
