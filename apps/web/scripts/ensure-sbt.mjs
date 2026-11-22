import { access, chmod, mkdir, readdir, rm, writeFile } from "node:fs/promises";
import { execFileSync, spawnSync } from "node:child_process";
import { constants } from "node:fs";
import { delimiter, dirname, join } from "node:path";
import { fileURLToPath } from "node:url";

const sbtVersion = "1.13.1";
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

// Scala 3.8+ runs only on JDK 17 or newer. Vercel's build image ships Java 11, so when the JVM on PATH is older this
// fetches a Temurin JDK into node_modules/.cache (kept between builds by the dependency cache) and the wrapper uses it.
const minimumJava = 17;
const jdkFeature = 21;
const jdkDir = join(cacheDir, `jdk-${jdkFeature}`);

function parseMajor(text) {
  const match = text.match(/version "(\d+)(?:\.(\d+))?/);
  if (!match) return 0;
  const first = Number(match[1]);
  return first === 1 ? Number(match[2] ?? 0) : first;
}

function javaVersionOutput(javaBin) {
  const result = spawnSync(javaBin, ["-version"], { encoding: "utf8" });
  return `${result.stdout ?? ""}${result.stderr ?? ""}`;
}

async function findJdkJava() {
  try {
    for (const entry of await readdir(jdkDir)) {
      const candidate = join(jdkDir, entry, "bin", "java");
      if (await isExecutable(candidate)) return candidate;
    }
  } catch {}
  return null;
}

async function ensureJava() {
  if (parseMajor(javaVersionOutput("java")) >= minimumJava) return "java";
  const cached = await findJdkJava();
  if (cached && parseMajor(javaVersionOutput(cached)) >= minimumJava) return cached;

  const arch = { x64: "x64", arm64: "aarch64" }[process.arch];
  const os = { linux: "linux", darwin: "mac" }[process.platform];
  if (!arch || !os) throw new Error(`Java ${minimumJava}+ is required; install a JDK for ${process.platform}/${process.arch}.`);
  console.log(`Java ${minimumJava}+ was not found; downloading Temurin JDK ${jdkFeature} for this build...`);
  const url = `https://api.adoptium.net/v3/binary/latest/${jdkFeature}/ga/${os}/${arch}/jdk/hotspot/normal/eclipse`;
  const response = await fetch(url);
  if (!response.ok) throw new Error(`Could not download a JDK: ${response.status} ${response.statusText}`);
  await rm(jdkDir, { recursive: true, force: true });
  await mkdir(jdkDir, { recursive: true });
  const archive = join(cacheDir, `jdk-${jdkFeature}.tar.gz`);
  await writeFile(archive, Buffer.from(await response.arrayBuffer()));
  execFileSync("tar", ["-xzf", archive, "-C", jdkDir]);
  await rm(archive, { force: true });
  // macOS archives nest the runtime under Contents/Home.
  const found = (await findJdkJava()) ?? (await findMacJava());
  if (!found) throw new Error("Downloaded JDK has no bin/java");
  return found;
}

async function findMacJava() {
  try {
    for (const entry of await readdir(jdkDir)) {
      const candidate = join(jdkDir, entry, "Contents", "Home", "bin", "java");
      if (await isExecutable(candidate)) return candidate;
    }
  } catch {}
  return null;
}

const javaBin = await ensureJava();

const wrapper = `#!/bin/sh
SCRIPT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
JAVA_ARGS=""
NO_COLORS=""

# These are sbt runner-script options used by the Scala.js Vite plugin. The
# launcher jar itself does not understand them, so handle them before exec.
while [ "$#" -gt 0 ]; do
  case "$1" in
    --batch|-batch) shift ;;
    -no-colors) NO_COLORS="-Dsbt.log.noformat=true"; shift ;;
    -D*) JAVA_ARGS="$JAVA_ARGS $1"; shift ;;
    *) break ;;
  esac
done

# JAVA_ARGS contains only JVM -D properties supplied by the trusted build tool.
# shellcheck disable=SC2086
exec "${javaBin}" -Xms512m -Xmx2048m $JAVA_ARGS $NO_COLORS -jar "$SCRIPT_DIR/../.cache/sbt-launch-${sbtVersion}.jar" "$@"
`;
await writeFile(sbtPath, wrapper);
await chmod(sbtPath, 0o755);
console.log(`Installed temporary sbt launcher at ${sbtPath}`);
