import { Command } from "commander";
import { access } from "node:fs/promises";
import path from "node:path";
import { defaultConfig, writeConfig, type Config } from "../utils/config.js";
import { isEmptyDirectory, scaffold } from "../utils/scaffold.js";

const validProjectName = /^[A-Za-z0-9][A-Za-z0-9._-]{0,99}$/;
const validPackage = /^[a-z][A-Za-z0-9]*(?:\.[a-z][A-Za-z0-9]*)*$/;

export const initCommand = new Command("init")
  .description("Initialize shadcn-scalajs config and scaffold a Scala.js + Laminar project")
  .option("--registry <url>", "registry URL or local path", defaultConfig.registry)
  .option("--source-dir <path>", "directory to write component source into")
  .option("--preset <code>", "design-system preset code from the customizer")
  .option("--project-name <name>", "project name used in generated files", "my-scalajs-app")
  .option("--package <name>", "Scala package name for shared/services code", "app")
  .option("--force", "overwrite generated files and an existing config")
  .option("--no-scaffold", "only write shadcn-scalajs.json (for an existing project)")
  .action(async (opts: { registry: string; sourceDir: string; preset?: string; projectName: string; package: string; force?: boolean; scaffold: boolean }) => {
    if (opts.preset && !/^[ab][0-9A-Za-z]{1,9}$/.test(opts.preset)) {
      throw new Error(`Invalid preset code: ${opts.preset}`);
    }
    if (!validProjectName.test(opts.projectName)) {
      throw new Error(`Invalid project name: ${opts.projectName}`);
    }
    if (!validPackage.test(opts.package)) {
      throw new Error(`Invalid Scala package: ${opts.package}`);
    }
    if (opts.sourceDir && (path.isAbsolute(opts.sourceDir) || opts.sourceDir.split(/[\\\\/]+/).includes(".."))) {
      throw new Error(`Invalid source directory: ${opts.sourceDir}`);
    }

    const cwd = process.cwd();
    const configPath = path.join(cwd, "shadcn-scalajs.json");
    const configExists = await access(configPath).then(() => true).catch(() => false);
    if (configExists && !opts.force) {
      throw new Error("shadcn-scalajs.json already exists; use --force to replace it.");
    }
    const empty = await isEmptyDirectory(cwd);
    if (opts.scaffold && !empty && !opts.force) {
      throw new Error("Refusing to scaffold in a non-empty directory; use --no-scaffold for config-only initialization or --force.");
    }

    // Components intentionally retain their canonical shadcnscalajs package so they
    // can be copied verbatim; this path is the source root expected by `add`.
    const config: Config = {
      registry: opts.registry,
      sourceDir: opts.sourceDir ?? (opts.scaffold ? "packages/ui/src/main/scala/shadcnscalajs" : defaultConfig.sourceDir),
      ...(opts.preset ? { preset: opts.preset } : {})
    };
    if (opts.scaffold) {
      const written = await scaffold(
        cwd,
        { projectName: opts.projectName, scalaPackage: opts.package, preset: opts.preset },
        Boolean(opts.force)
      );
      console.log(`Scaffolded ${written.length} files.`);
    }

    await writeConfig(cwd, config);
    console.log(`Wrote shadcn-scalajs.json:\n${JSON.stringify(config, null, 2)}`);
  });
