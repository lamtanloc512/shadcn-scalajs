import { Command } from "commander";
import { access } from "node:fs/promises";
import path from "node:path";
import { createInterface } from "node:readline/promises";
import { stdin as input, stdout as output } from "node:process";
import { defaultConfig, writeConfig, type Config } from "../utils/config.js";
import { stylePackFromPreset } from "../utils/preset.js";
import { isEmptyDirectory, scaffold } from "../utils/scaffold.js";

const validProjectName = /^[A-Za-z0-9][A-Za-z0-9._-]{0,99}$/;
const validPackage = /^[a-z][A-Za-z0-9]*(?:\.[a-z][A-Za-z0-9]*)*$/;

async function resolveScaffoldMetadata(
  projectNameOption?: string,
  groupOption?: string,
  packageOption?: string
): Promise<{ projectName: string; artifactGroup: string }> {
  if (projectNameOption && (groupOption || packageOption)) {
    return { projectName: projectNameOption, artifactGroup: groupOption ?? packageOption! };
  }

  const prompts = createInterface({ input, output });
  try {
    const projectName = projectNameOption ?? (await prompts.question("Project name: ")).trim();
    const artifactGroup = groupOption ?? packageOption
      ?? (await prompts.question("Artifact group (for example org.ethan.app): ")).trim();
    return { projectName, artifactGroup };
  } finally {
    prompts.close();
  }
}

export const initCommand = new Command("init")
  .description("Initialize shadcn-scalajs config and scaffold a Scala.js + Laminar project")
  .option("--registry <url>", "registry URL or local path", defaultConfig.registry)
  .option("--source-dir <path>", "directory to write component source into")
  .option("--preset <code>", "design-system preset code from the customizer")
  .option("--style <pack>", "style pack (nova, vega, maia, lyra, mira, luma, sera, rhea)")
  .option("--project-name <name>", "project name used in generated files (skips the prompt)")
  .option("--group <group>", "sbt artifact group, e.g. org.ethan.app (skips the prompt)")
  .option("--package <name>", "Scala package prefix (defaults to the artifact group)")
  .option("--force", "overwrite generated files and an existing config")
  .option("--no-scaffold", "only write shadcn-scalajs.json (for an existing project)")
  .action(async (opts: { registry: string; sourceDir: string; preset?: string; style?: string; projectName?: string; group?: string; package?: string; force?: boolean; scaffold: boolean }) => {
    if (opts.preset && !/^[ab][0-9A-Za-z]{1,9}$/.test(opts.preset)) {
      throw new Error(`Invalid preset code: ${opts.preset}`);
    }

    // `--no-scaffold` initializes an existing project and needs no generated project metadata.
    const metadata = opts.scaffold
      ? await resolveScaffoldMetadata(opts.projectName, opts.group, opts.package)
      : undefined;
    const projectName = metadata?.projectName;
    if (projectName && !validProjectName.test(projectName)) {
      throw new Error(`Invalid project name: ${projectName}`);
    }
    if (opts.scaffold && !projectName) {
      throw new Error("Project name is required.");
    }
    const artifactGroup = metadata?.artifactGroup ?? opts.group;
    const scalaPackage = opts.package ?? artifactGroup;
    if (artifactGroup && !validPackage.test(artifactGroup)) {
      throw new Error(`Invalid artifact group: ${artifactGroup}`);
    }
    if (scalaPackage && !validPackage.test(scalaPackage)) {
      throw new Error(`Invalid Scala package: ${scalaPackage}`);
    }
    if (opts.scaffold && (!artifactGroup || !scalaPackage)) {
      throw new Error("Artifact group is required.");
    }
    if (opts.sourceDir && (path.isAbsolute(opts.sourceDir) || opts.sourceDir.split(/[\\\\/]+/).includes(".."))) {
      throw new Error(`Invalid source directory: ${opts.sourceDir}`);
    }

    const invocationCwd = process.cwd();
    const createsChildDirectory = opts.scaffold && ["examples", ".examples"].includes(path.basename(invocationCwd));
    const cwd = createsChildDirectory ? path.join(invocationCwd, projectName!) : invocationCwd;
    const configPath = path.join(cwd, "shadcn-scalajs.json");
    const configExists = await access(configPath).then(() => true).catch(() => false);
    if (configExists && !opts.force) {
      throw new Error("shadcn-scalajs.json already exists; use --force to replace it.");
    }
    const empty = await isEmptyDirectory(cwd);
    if (opts.scaffold && !empty && !opts.force) {
      throw new Error("Refusing to scaffold in a non-empty directory; use --no-scaffold for config-only initialization or --force.");
    }

    const stylePack = opts.style ?? stylePackFromPreset(opts.preset) ?? "nova";

    // Components intentionally retain their canonical shadcnscalajs package so they
    // can be copied verbatim; this path is the source root expected by `add`.
    const config: Config = {
      registry: opts.registry,
      sourceDir: opts.sourceDir ?? (opts.scaffold ? "packages/ui/src/main/scala/shadcnscalajs" : defaultConfig.sourceDir),
      stylePack,
      ...(opts.preset ? { preset: opts.preset } : {})
    };
    if (opts.scaffold) {
      const written = await scaffold(
        cwd,
        {
          projectName: projectName!,
          artifactGroup: artifactGroup!,
          scalaPackage: scalaPackage!,
          preset: opts.preset,
          stylePack
        },
        Boolean(opts.force)
      );
      const destination = path.relative(invocationCwd, cwd) || ".";
      console.log(`Scaffolded ${written.length} files in ${destination}`);
      if (createsChildDirectory) console.log(`Next: cd ${destination}`);
    }

    await writeConfig(cwd, config);
    console.log(`Wrote shadcn-scalajs.json:\n${JSON.stringify(config, null, 2)}`);
  });
