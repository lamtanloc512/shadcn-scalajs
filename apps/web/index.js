import "./src/styles/globals.css"
import "./src/docs-highlight.js"
import "./src/editor.js"
import { inject } from "@vercel/analytics"

inject()

// Speed Insights' script (web-vitals `reportAllChanges`) throws
// `Cannot read properties of undefined (reading 'startTime')` on every
// client-side navigation. The gallery is an SPA, so leave it unloaded.

const webComponentsEnabled = import.meta.env.VITE_ENABLE_WEB_COMPONENTS === "true"
globalThis.__SHADCN_SCALAJS_ENABLE_WEB_COMPONENTS__ = webComponentsEnabled

if (webComponentsEnabled) {
  void import("./src/webcomponents-runtime.js")
}

void import('scalajs:main.js')
