import org.scalajs.linker.interface.ESVersion

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

lazy val jsSettings = Seq(
  scalaJSLinkerConfig ~= {
    _.withModuleKind(ModuleKind.ESModule)
      .withESFeatures(_.withESVersion(ESVersion.ES2020))
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
