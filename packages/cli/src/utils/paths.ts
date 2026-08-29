import path from "node:path";
import type { Config } from "./config.js";
import type { RegistryItemFile } from "./registry.js";

/** Scala sources live under `config.sourceDir`. Theme CSS lives under the UI package root. */
export function resolveInstallPath(cwd: string, config: Config, file: RegistryItemFile): string {
  if (file.type.startsWith("css:")) {
    // sourceDir ends with .../src/main/scala/shadcnscalajs
    //   packages/ui/src/main/scala/shadcnscalajs -> packages/ui
    //   src/main/scala/shadcnscalajs -> project root
    const uiRoot = path.resolve(cwd, config.sourceDir, "..", "..", "..", "..");
    return path.join(uiRoot, file.target);
  }
  return path.join(cwd, config.sourceDir, file.target);
}
