import { Command } from "commander";
import { defaultConfig, writeConfig, type Config } from "../utils/config.js";

export const initCommand = new Command("init")
  .description("Create a shadcn-scalajs.json config file in the current project")
  .option("--registry <url>", "registry URL or local path", defaultConfig.registry)
  .option("--source-dir <path>", "directory to write component source into", defaultConfig.sourceDir)
  .action(async (opts: { registry: string; sourceDir: string }) => {
    const config: Config = { registry: opts.registry, sourceDir: opts.sourceDir };
    await writeConfig(process.cwd(), config);
    console.log(`Wrote shadcn-scalajs.json:\n${JSON.stringify(config, null, 2)}`);
  });
