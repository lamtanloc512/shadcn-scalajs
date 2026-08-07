import org.scalajs.linker.interface.{ESVersion, ModuleSplitStyle}

ThisBuild / organization := "dev.shadcn-scalajs"
ThisBuild / version := "0.1.0"
ThisBuild / scalaVersion := Versions.Scala_3
ThisBuild / scalacOptions ++= Seq(
  "-deprecation",
  "-language:implicitConversions"
)

lazy val root = project
  .in(file("."))
  .aggregate(core, ui, blocks, webcomponents, site)
  .settings(
    name := "shadcn-scalajs"
  )
  .settings(noPublish)

// Packages whose classes change often — emit small ES modules so Vite can
// hot-reload only the touched files. Libraries stay in fewer larger modules.
lazy val appPackages = List(
  "shadcnscalajs.site",
  "shadcnscalajs.ui",
  "shadcnscalajs.blocks",
  "shadcnscalajs.core",
  "shadcnscalajs.webcomponents"
)

lazy val jsSettings = Seq(
  scalaJSLinkerConfig ~= { cfg =>
    cfg
      .withModuleKind(ModuleKind.ESModule)
      .withESFeatures(_.withESVersion(ESVersion.ES2020))
      .withModuleSplitStyle(ModuleSplitStyle.SmallModulesFor(appPackages))
      // Vite prepends the *-fastopt dir onto Scala.js absolute file:/https:
      // sourcemap URIs and then warns "points to missing source files".
      // Disable linker maps; browser + Vite own maps are not usable for Scala
      // sources through this pipeline anyway.
      .withSourceMap(false)
  },
  // Production link (fullLinkJS / vite build): one module, size-oriented ES
  // features. sbt-scalajs already enables minify + Semantics.optimized on
  // fullOpt; Closure stays off for ESModule (unsupported) — Vite minifies.
  Compile / fullOptJS / scalaJSLinkerConfig ~= { cfg =>
    cfg
      .withModuleSplitStyle(ModuleSplitStyle.FewestModules)
      .withSourceMap(false)
      .withBatchMode(true)
      .withESFeatures(
        _.withESVersion(ESVersion.ES2020)
          .withAvoidClasses(false) // smaller output vs. Firefox-tuned default
      )
  }
)

// Design tokens, Variant/Size ADTs shared by every component.
lazy val core = project
  .in(file("modules/core"))
  .enablePlugins(ScalaJSPlugin)
  .settings(jsSettings)
  .settings(
    libraryDependencies ++= Seq(
      "com.raquo" %%% "laminar" % Versions.Laminar
    )
  )

// Laminar component source of truth — this is what the CLI/registry serves
// to consumers as copy-paste-owned .scala files.
lazy val ui = project
  .in(file("modules/ui"))
  .enablePlugins(ScalaJSPlugin)
  .settings(jsSettings)
  .settings(noPublish)
  .dependsOn(core)

// Blocks — multi-file page/section compositions built from `ui`, served to
// consumers as copy-paste-owned .scala files exactly like components.
lazy val blocks = project
  .in(file("modules/blocks"))
  .enablePlugins(ScalaJSPlugin)
  .settings(jsSettings)
  .settings(noPublish)
  .dependsOn(ui)

// Custom-element (Web Component) export layer, so any JS framework or plain
// HTML page can consume the same components without a Scala toolchain.
lazy val webcomponents = project
  .in(file("modules/webcomponents"))
  .enablePlugins(ScalaJSPlugin)
  .settings(jsSettings)
  .settings(noPublish)
  .settings(
    scalaJSUseMainModuleInitializer := true
  )
  .dependsOn(ui)

// Docs/demo app: dogfoods `ui` directly via Laminar, and hosts a plain-HTML
// page that only loads the compiled Web Component bundle (no Scala.js
// runtime), plus the static registry JSON built by scripts/build-registry.ts.
lazy val site = project
  .in(file("modules/site"))
  .enablePlugins(ScalaJSPlugin)
  .settings(jsSettings)
  .settings(noPublish)
  .settings(
    scalaJSUseMainModuleInitializer := true
  )
  .dependsOn(ui, blocks, webcomponents)

lazy val noPublish = Seq(
  publishLocal / skip := true,
  publish / skip := true
)

addCommandAlias("uiw", ";~ui/fastLinkJS")
addCommandAlias("wcw", ";~webcomponents/fastLinkJS")
addCommandAlias("sitew", ";~site/fastLinkJS")
// Size-optimized production link (Scala.js minify + FewestModules). Prefer
// `cd modules/site && npm run build` which runs site/fullLinkJS via the Vite
// plugin and then minifies/bundles with esbuild.
addCommandAlias("siteOpt", ";site/fullLinkJS")
addCommandAlias(
  "opt",
  ";ui/fullLinkJS;webcomponents/fullLinkJS;site/fullLinkJS"
)
