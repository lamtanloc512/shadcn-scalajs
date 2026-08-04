import { readdirSync, readFileSync, writeFileSync } from "node:fs";
import { dirname, join, resolve } from "node:path";
import { fileURLToPath } from "node:url";

const here = dirname(fileURLToPath(import.meta.url));
const projectRoot = resolve(here, "../../..");
const sourceRoot = join(projectRoot, "vendor/basecoat-source");
const output = join(projectRoot, "modules/site/src/styles/basecoat.generated.css");
const packs = ["lyra", "vega", "nova", "maia", "mira", "luma", "sera", "rhea"];

function layerBody(source, file) {
  const marker = "@layer components";
  const markerIndex = source.indexOf(marker);
  if (markerIndex < 0) throw new Error(`Missing ${marker} in ${file}`);
  const start = source.indexOf("{", markerIndex);
  let depth = 0;
  let end = -1;
  for (let index = start; index < source.length; index += 1) {
    if (source[index] === "{") depth += 1;
    if (source[index] === "}") depth -= 1;
    if (depth === 0) {
      end = index;
      break;
    }
  }
  if (start < 0 || end <= start) throw new Error(`Invalid component layer in ${file}`);
  return source.slice(start + 1, end).trim();
}

const componentDir = join(sourceRoot, "components");
const componentCss = readdirSync(componentDir)
  .filter((name) => name.endsWith(".css"))
  .sort()
  .map((name) => [name, readFileSync(join(componentDir, name), "utf8")])
  .filter(([, source]) => source.includes("@layer components"))
  .map(([name, source]) => layerBody(source, name))
  .join("\n\n");

const packCss = packs
  .map((pack) => {
    const file = join(sourceRoot, "styles", `${pack}.css`);
    const body = layerBody(readFileSync(file, "utf8"), file).replaceAll("has-[.kbd]:pe-1.5 ", "");
    return `[data-style-pack="${pack}"] {\n${body}\n}`;
  })
  .join("\n\n");

writeFileSync(
  output,
  `/* Generated from vendor/basecoat-source. Run npm run build:basecoat-styles after updating the vendor snapshot. */\n@layer components {\n${componentCss}\n\n${packCss}\n}\n\n@keyframes toast-up {\n  from { opacity: 0; transform: translateY(100%); }\n}\n`,
);
