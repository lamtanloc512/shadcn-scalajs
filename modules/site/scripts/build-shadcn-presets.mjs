import { readdirSync, readFileSync, writeFileSync } from "node:fs";
import { dirname, join, resolve } from "node:path";
import { fileURLToPath } from "node:url";

const here = dirname(fileURLToPath(import.meta.url));
const projectRoot = resolve(here, "../../..");
const sourceRoot = join(projectRoot, "vendor/shadcn-source/styles");
const output = join(projectRoot, "modules/site/src/styles/shadcn-presets.generated.css");

const files = readdirSync(sourceRoot).filter((file) => file.startsWith("style-") && file.endsWith(".css")).sort();
const blocks = files.map((file) => {
  const pack = file.slice("style-".length, -".css".length);
  const source = readFileSync(join(sourceRoot, file), "utf8");
  return source.replaceAll(`.style-${pack}`, `[data-style-pack="${pack}"]`);
});

writeFileSync(
  output,
  `/* Generated from the shadcn v4 preset sources in vendor/shadcn-source. */\n${blocks.join("\n\n")}\n`,
);

