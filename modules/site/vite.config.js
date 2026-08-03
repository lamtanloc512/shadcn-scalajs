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
  }
});
