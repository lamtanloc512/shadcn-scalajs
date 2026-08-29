import { Command } from "commander";
import { defaultConfig, writeConfig, type Config } from "../utils/config.js";

export const initCommand = new Command("init")
  .description("Create a shadcn-scalajs.json config file in the current project")
  .option("--registry <url>", "registry URL or local path", defaultConfig.registry)
  .option("--source-dir <path>", "directory to write component source into", defaultConfig.sourceDir)
  .option("--preset <code>", "design-system preset code from the customizer")
  .action(async (opts: { registry: string; sourceDir: string; preset?: string }) => {
    if (opts.preset && !/^[ab][0-9A-Za-z]{1,9}$/.test(opts.preset)) {
      throw new Error(`Invalid preset code: ${opts.preset}`);
    }

    const config: Config = {
      registry: opts.registry,
      sourceDir: opts.sourceDir,
      ...(opts.preset ? { preset: opts.preset } : {})
    };
    await writeConfig(process.cwd(), config);
    console.log(`Wrote shadcn-scalajs.json:\n${JSON.stringify(config, null, 2)}`);
  });
