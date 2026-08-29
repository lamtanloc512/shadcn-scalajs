import { readFile, writeFile } from "node:fs/promises";
import path from "node:path";

/**
 * Project-local config, analogous to shadcn-svelte's `components.json`.
 *
 * v1 scope: written files keep their fixed `shadcnscalajs.ui`/`shadcnscalajs.core`
 * package declarations verbatim (no import-path rewriting, unlike shadcn-svelte's
 * transformImports) — so `sourceDir` must point at a directory literally named
 * `.../shadcnscalajs`, matching Scala's directory-matches-package convention;
 * `add` then writes e.g. `<sourceDir>/ui/Button.scala`. Consumers add
 * `shadcn-scalajs-core` as a normal sbt library dependency for the shared
 * `core` package the copied `ui` files import from (printed as a hint by
 * `add`) — package-rewriting so consumers can fully own every line under
 * their own namespace is a documented v2 follow-up, not implemented here.
 */
export interface Config {
  registry: string;
  sourceDir: string;
  preset?: string;
}

export const CONFIG_FILE_NAME = "shadcn-scalajs.json";

/** Local Vite registry (modules/site `npm run dev` → http://localhost:4300/registry).
 * Override with `init --registry` when using a deployed host. */
export const DEFAULT_REGISTRY = "https://shadcn-scalajs.vercel.app/registry";

export const defaultConfig: Config = {
  registry: DEFAULT_REGISTRY,
  sourceDir: "src/main/scala/shadcnscalajs"
};

export async function readConfig(cwd: string): Promise<Config> {
  const configPath = path.join(cwd, CONFIG_FILE_NAME);
  const raw = await readFile(configPath, "utf-8").catch(() => {
    throw new Error(`No ${CONFIG_FILE_NAME} found in ${cwd}. Run \`shadcn-scalajs init\` first.`);
  });
  return { ...defaultConfig, ...JSON.parse(raw) };
}

export async function writeConfig(cwd: string, config: Config): Promise<void> {
  const configPath = path.join(cwd, CONFIG_FILE_NAME);
  await writeFile(configPath, JSON.stringify(config, null, 2) + "\n");
}
