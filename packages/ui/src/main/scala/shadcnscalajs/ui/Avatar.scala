package shadcnscalajs.ui

import com.raquo.laminar.api.L.*
import com.raquo.laminar.codecs.StringAsIsCodec

/** shadcn/ui Avatar — root, image, fallback, badge, and the group wrapper, with upstream's `data-slot` and `data-size`
  * contract. Packs size the avatar off `data-size` (`size-8` default, `size-6` sm, `size-10` lg) and the badge and
  * group-count sizes derive from it, so the attribute has to be present even at the default size.
  */
object Avatar:
  private val imageTag = htmlTag("img")
  private val srcAttr = htmlAttr("src", StringAsIsCodec)
  private val altAttr = htmlAttr("alt", StringAsIsCodec)

  enum Size derives CanEqual:
    case Default, Sm, Lg

  private val rootBase: String =
    "avatar cn-avatar group/avatar relative flex shrink-0 overflow-hidden rounded-full select-none after:absolute after:inset-0 after:rounded-full after:border after:border-border after:mix-blend-darken dark:after:mix-blend-lighten"

  private def sizeClasses(size: Size): String = size match
    case Size.Default => "size-8"
    case Size.Sm      => "size-6"
    case Size.Lg      => "size-10"

  def apply(mods: Modifier[HtmlElement]*): HtmlElement =
    apply(Size.Default, mods*)

  def apply(size: Size, mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "avatar",
      dataAttr("size") := size.toString.toLowerCase,
      cls := s"$rootBase ${sizeClasses(size)}",
      mods
    )

  def image(src: String, alt: String = "", mods: Modifier[HtmlElement]*): HtmlElement =
    imageTag(
      srcAttr := src,
      altAttr := alt,
      dataAttr("slot") := "avatar-image",
      cls := "cn-avatar-image aspect-square size-full rounded-full object-cover",
      mods
    )

  def fallback(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "avatar-fallback",
      cls := "cn-avatar-fallback flex size-full items-center justify-center rounded-full bg-muted text-sm text-muted-foreground group-data-[size=sm]/avatar:text-xs",
      mods
    )

  /** Status dot overlaying the avatar's lower-right corner. Sized by the root's `data-size` via `group/avatar`. */
  def badge(mods: Modifier[HtmlElement]*): HtmlElement =
    span(
      dataAttr("slot") := "avatar-badge",
      cls := "cn-avatar-badge absolute right-0 bottom-0 z-10 inline-flex items-center justify-center rounded-full bg-primary text-primary-foreground bg-blend-color ring-2 ring-background select-none group-data-[size=sm]/avatar:size-2 group-data-[size=sm]/avatar:[&>svg]:hidden group-data-[size=default]/avatar:size-2.5 group-data-[size=default]/avatar:[&>svg]:size-2 group-data-[size=lg]/avatar:size-3 group-data-[size=lg]/avatar:[&>svg]:size-2",
      mods
    )

  def group(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "avatar-group",
      cls := "cn-avatar-group group/avatar-group flex -space-x-2 *:data-[slot=avatar]:ring-2 *:data-[slot=avatar]:ring-background",
      mods
    )

  /** The trailing "+N" chip in an avatar group. */
  def groupCount(mods: Modifier[HtmlElement]*): HtmlElement =
    div(
      dataAttr("slot") := "avatar-group-count",
      cls := "cn-avatar-group-count relative flex size-8 shrink-0 items-center justify-center rounded-full bg-muted text-sm text-muted-foreground ring-2 ring-background group-has-data-[size=lg]/avatar-group:size-10 group-has-data-[size=sm]/avatar-group:size-6 [&>svg]:size-4",
      mods
    )
