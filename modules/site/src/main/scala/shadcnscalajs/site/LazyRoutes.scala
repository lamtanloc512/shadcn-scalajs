package shadcnscalajs.site

import com.raquo.laminar.api.L.*
import shadcnscalajs.site.create.{CreatePageEntry, PreviewOnlyPage}

import scala.scalajs.js

/** Route bodies that are not needed for the first paint of the docs shell.
  *
  * Each method owns its own `js.dynamicImport` so the Scala.js linker can put the page object (and everything only it
  * reaches — the block catalog, dashboard, create shell, …) into an async module. Call sites in [[Main]] must go
  * through these helpers and must not also call the page objects directly, or the split collapses.
  */
object LazyRoutes:

  def blocksIndex(): HtmlElement =
    LazyMount(js.dynamicImport(BlocksIndexPage()))

  def blockDocs(name: String): HtmlElement =
    LazyMount(js.dynamicImport(BlockDocsPage(name)))

  def blockPreview(name: String): HtmlElement =
    LazyMount(js.dynamicImport(BlockPreviewPage(name)))

  def create(): HtmlElement =
    LazyMount(js.dynamicImport(CreatePageEntry()))

  def createPreview(): HtmlElement =
    LazyMount(js.dynamicImport(PreviewOnlyPage()))

  /** Landing mosaic — heavy cards (charts, QR, forms). Kept off the entry chunk so component-docs cold loads stay lean. */
  def landingMosaic(): HtmlElement =
    LazyMount(js.dynamicImport(shadcnscalajs.site.create.preview02.Preview02()))
