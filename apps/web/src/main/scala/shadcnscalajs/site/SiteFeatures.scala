package shadcnscalajs.site

import scala.scalajs.js

/** Build-time site capabilities exposed by `index.js` from Vite environment variables. */
object SiteFeatures:
  /** Keep in lockstep with `packages/cli/package.json`. Copied pages name this version so an assistant does not guess. */
  val cliVersion = "0.3.2"

  val webComponents: Boolean =
    val value = js.Dynamic.global.selectDynamic("__SHADCN_SCALAJS_ENABLE_WEB_COMPONENTS__")
    !js.isUndefined(value) && value.asInstanceOf[Boolean]
