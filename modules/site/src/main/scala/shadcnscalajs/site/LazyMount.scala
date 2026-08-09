package shadcnscalajs.site

import com.raquo.laminar.api.L.*

import scala.scalajs.js

/** Mounts a page that lives behind a Scala.js `js.dynamicImport` boundary.
  *
  * The linker turns each `js.dynamicImport { ... }` into a real `import("./…")`, and Vite then emits that as an async
  * chunk. Without those boundaries the production `FewestModules` link collapses the whole app into one 1.2 MB file,
  * so every cold visit paid for blocks, create, and the landing mosaic even when opening `/components/button`.
  *
  * The page constructor must be called *inside* the `js.dynamicImport` block (see the [[LazyRoutes]] callers) — passing
  * an already-built element here would keep it in the entry chunk.
  */
object LazyMount:

  def apply(load: js.Promise[HtmlElement]): HtmlElement =
    val loaded = Var[Option[HtmlElement]](None)
    load.`then`[Unit](
      (el: HtmlElement) =>
        loaded.set(Some(el))
        ()
      ,
      (_: Any) =>
        loaded.set(Some(failed))
        ()
    )
    div(
      cls := "contents",
      child <-- loaded.signal.map {
        case Some(el) => el
        case None     => placeholder
      }
    )

  private val placeholder: HtmlElement =
    div(
      cls := "flex min-h-[50vh] w-full items-center justify-center text-sm text-muted-foreground",
      aria.busy := true,
      "Loading…"
    )

  private val failed: HtmlElement =
    div(
      cls := "flex min-h-[50vh] w-full items-center justify-center text-sm text-destructive",
      "Failed to load this page. Refresh and try again."
    )
