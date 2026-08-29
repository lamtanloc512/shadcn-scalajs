import { access, chmod, mkdir, writeFile } from "node:fs/promises";
import { constants } from "node:fs";
import { delimiter, dirname, join } from "node:path";
import { fileURLToPath } from "node:url";

const sbtVersion = "1.10.5";
const scriptDir = dirname(fileURLToPath(import.meta.url));
const siteDir = dirname(scriptDir);
const binDir = join(siteDir, "node_modules", ".bin");
const cacheDir = join(siteDir, "node_modules", ".cache");
const launcherPath = join(cacheDir, `sbt-launch-${sbtVersion}.jar`);
const sbtPath = join(binDir, "sbt");

async function isExecutable(path) {
  try {
    await access(path, constants.X_OK);
    return true;
  } catch {
    return false;
  }
}

async function hasSbtOnPath() {
  for (const directory of (process.env.PATH ?? "").split(delimiter)) {
    if (directory && (await isExecutable(join(directory, "sbt")))) return true;
  }
  return false;
}

if (await hasSbtOnPath()) {
  console.log("sbt is already available.");
  process.exit(0);
}

console.log(`sbt was not found; installing the ${sbtVersion} launcher for this build...`);
await mkdir(binDir, { recursive: true });
await mkdir(cacheDir, { recursive: true });

try {
  await access(launcherPath, constants.R_OK);
} catch {
  const url = `https://repo.maven.apache.org/maven2/org/scala-sbt/sbt-launch/${sbtVersion}/sbt-launch-${sbtVersion}.jar`;
  const response = await fetch(url);
  if (!response.ok) throw new Error(`Could not download sbt launcher: ${response.status} ${response.statusText}`);
  await writeFile(launcherPath, Buffer.from(await response.arrayBuffer()));
}

const wrapper = `#!/bin/sh
SCRIPT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
exec java -Xms512m -Xmx2048m -jar "$SCRIPT_DIR/../.cache/sbt-launch-${sbtVersion}.jar" "$@"
`;
await writeFile(sbtPath, wrapper);
await chmod(sbtPath, 0o755);
console.log(`Installed temporary sbt launcher at ${sbtPath}`);
