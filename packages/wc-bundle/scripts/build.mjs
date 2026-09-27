import { execFileSync } from "node:child_process";
import { copyFileSync, mkdirSync, readFileSync, writeFileSync } from "node:fs";
import postcss from "../../../apps/web/node_modules/postcss/lib/postcss.mjs";
import { dirname, join, resolve } from "node:path";
import { fileURLToPath } from "node:url";
import { isolateDocumentCss } from "../../cli/assets/webcomponents/isolated-css.mjs";

const packageRoot = resolve(dirname(fileURLToPath(import.meta.url)), "..");
const siteRoot = resolve(packageRoot, "../../apps/web");
const source = join(siteRoot, "public");
const dist = join(packageRoot, "dist");

execFileSync(process.execPath, [join(siteRoot, "scripts/build-style-packs.mjs")], {
  cwd: siteRoot,
  stdio: "inherit"
});
execFileSync(process.execPath, [join(siteRoot, "scripts/build-webcomponents.mjs")], {
  cwd: siteRoot,
  stdio: "inherit",
  env: { ...process.env, SC_WC_PACK: "nova" }
});

mkdirSync(join(dist, "styles"), { recursive: true });
copyFileSync(join(packageRoot, "index.js"), join(dist, "index.js"));
for (const file of ["sc-components.js", "sc-components.css", "styles/pack-nova.css"]) {
  copyFileSync(join(source, file), join(dist, file));
}
writeFileSync(
  join(dist, "sc-document.css"),
  isolateDocumentCss(readFileSync(join(dist, "sc-components.css"), "utf8"), postcss)
);
console.log(`Web Components package built in ${dist}`);
