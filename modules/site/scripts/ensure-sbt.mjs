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
    const candidate = directory && join(directory, "sbt");
    // Rewrite our managed launcher on every build so a cached older wrapper
    // cannot survive an update to this bootstrap script.
    if (candidate && candidate !== sbtPath && (await isExecutable(candidate))) return true;
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
JAVA_ARGS=""
NO_COLORS=""

# These are sbt runner-script options used by the Scala.js Vite plugin. The
# launcher jar itself does not understand them, so handle them before exec.
while [ "$#" -gt 0 ]; do
  case "$1" in
    --batch) shift ;;
    -no-colors) NO_COLORS="-Dsbt.log.noformat=true"; shift ;;
    -D*) JAVA_ARGS="$JAVA_ARGS $1"; shift ;;
    *) break ;;
  esac
done

# JAVA_ARGS contains only JVM -D properties supplied by the trusted build tool.
# shellcheck disable=SC2086
exec java -Xms512m -Xmx2048m $JAVA_ARGS $NO_COLORS -jar "$SCRIPT_DIR/../.cache/sbt-launch-${sbtVersion}.jar" "$@"
`;
await writeFile(sbtPath, wrapper);
await chmod(sbtPath, 0o755);
console.log(`Installed temporary sbt launcher at ${sbtPath}`);
