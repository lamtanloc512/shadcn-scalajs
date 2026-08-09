import { defineConfig } from "vite";
import scalaJSPlugin from "@scala-js/vite-plugin-scalajs";

export default defineConfig({
  base: "/",
  publicDir: "public",
  plugins: [
    scalaJSPlugin({
      cwd: "../..", // path to build.sbt (modules/site -> repo root)
      projectID: "site" // scala.js project name in build.sbt
    })
  ],
  server: {
    port: 4300,
    strictPort: true
  },
  build: {
    // Production: Vite plugin runs site/fullLinkJS, then esbuild minifies.
    // No source maps — Scala.js maps use absolute file:/https: URIs that Vite
    // cannot resolve (and we disable linker maps in build.sbt).
    sourcemap: false,
    target: "es2020",
    minify: "esbuild",
    cssMinify: true,
    reportCompressedSize: true,
    // Entry stays large (docs + ui); async chunks from LazyRoutes are much smaller.
    chunkSizeWarningLimit: 1200
  }
});
