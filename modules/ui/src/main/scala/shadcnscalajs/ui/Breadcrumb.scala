package shadcnscalajs.ui

import com.raquo.laminar.api.L.*

object Breadcrumb:
  def apply(mods: Modifier[HtmlElement]*): HtmlElement = navTag(cls := "breadcrumb", aria.label := "breadcrumb", mods)
  def list(mods: Modifier[HtmlElement]*): HtmlElement =
    ol(cls := "cn-breadcrumb-list flex flex-wrap items-center gap-1.5 text-sm text-muted-foreground sm:gap-2.5", mods)
  def item(mods: Modifier[HtmlElement]*): HtmlElement = li(cls := "cn-breadcrumb-item inline-flex items-center gap-1.5", mods)
  def link(hrefValue: String, mods: Modifier[HtmlElement]*): HtmlElement =
    a(href := hrefValue, cls := "cn-breadcrumb-link transition-colors hover:text-foreground", mods)
  def separator(mods: Modifier[HtmlElement]*): HtmlElement =
    li(aria.hidden := true, cls := "cn-breadcrumb-separator [&>svg]:size-3.5", "/", mods)
