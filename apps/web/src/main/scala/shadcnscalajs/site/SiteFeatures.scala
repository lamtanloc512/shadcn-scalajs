package shadcnscalajs.site

import scala.scalajs.js

/** Build-time site capabilities exposed by `index.js` from Vite environment variables. */
object SiteFeatures:
  val webComponents: Boolean =
    val value = js.Dynamic.global.selectDynamic("__SHADCN_SCALAJS_ENABLE_WEB_COMPONENTS__")
    !js.isUndefined(value) && value.asInstanceOf[Boolean]
