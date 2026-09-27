import { Command } from "commander";
import { access, copyFile, mkdir, readFile, writeFile } from "node:fs/promises";
import { execFileSync } from "node:child_process";
import path from "node:path";
import { fileURLToPath } from "node:url";
import { readConfig } from "../utils/config.js";

// Only wrappers with a single, independently installable UI component are listed here.
// More compound wrappers need their own dependency/markup contract before they can be copied.
const wrappers: Record<string, string> = {
  accordion: "ScAccordion",
  "alert-dialog": "ScAlertDialog",
  "aspect-ratio": "ScAspectRatio",
  badge: "ScBadge",
  button: "ScButton",
  calendar: "ScCalendar",
  carousel: "ScCarousel",
  card: "ScCard",
  chart: "ScChart",
  checkbox: "ScCheckbox",
  combobox: "ScCombobox",
  "context-menu": "ScContextMenu",
  "data-table": "ScDataTable",
  "date-picker": "ScDatePicker",
  dialog: "ScDialog",
  drawer: "ScDrawer",
  "dropdown-menu": "ScDropdownMenu",
  "hover-card": "ScHoverCard",
  input: "ScInput",
  "input-group": "ScInputGroup",
  "input-otp": "ScInputOTP",
  item: "ScItem",
  menubar: "ScMenubar",
  "navigation-menu": "ScNavigationMenu",
  pagination: "ScPagination",
  progress: "ScProgress",
  "range-calendar": "ScRangeCalendar",
  resizable: "ScResizable",
  "scroll-area": "ScScrollArea",
  select: "ScSelect",
  sidebar: "ScSidebar",
  sheet: "ScSheet",
  slider: "ScSlider",
  sonner: "ScSonner",
  switch: "ScSwitch",
  tabs: "ScTabs",
  "theme-switcher": "ScThemeSwitcher",
  textarea: "ScTextarea",
  toggle: "ScToggle"
};
const runtime = ["LightPrimitive", "ScElementBase", "ScElements", "ScStyles", "ScThemeApi", "ScThemeState"];
const uiName = (name: string): string => name.split("-").map(part => part[0].toUpperCase() + part.slice(1)).join("");
const exists = (file: string): Promise<boolean> => access(file).then(() => true, () => false);

function mainSource(selected: string[]): string {
  return `package shadcnscalajs.webcomponents

import org.scalajs.dom

/** Register the wrappers installed in this project, using the project's own UI source. */
object Main:
  def main(args: Array[String]): Unit =
    ScThemeApi.install()
    dom.fetch(ScStyles.baseHref)
      .\`then\`[String](response =>
        if response.ok then response.text()
        else throw new Exception("Cannot load Web Component CSS: " + ScStyles.baseHref)
      )
      .\`then\`[Unit] { (css: String) =>
        ScStyles.use(css)
${selected.map(name => `        ${wrappers[name]}.register()`).join("\n")}
      }
      .\`catch\` { error =>
        dom.console.error("Web Components were not registered because CSS failed to load", error)
        ()
      }
`;
}

export const buildWcCommand = new Command("build-wc")
  .description("Install Web Component wrapper source for copied UI components and build JS/CSS")
  .argument("[components...]", "installed components to expose; defaults to every supported installed component")
  .action(async (requested: string[]) => {
    const cwd = process.cwd();
    const config = await readConfig(cwd);
    const sbt = path.join(cwd, "build.sbt");
    const buildScript = path.join(cwd, "scripts/build-webcomponents.mjs");
    if (!(await exists(sbt)) || !(await exists(buildScript)) || !(await readFile(sbt, "utf8")).includes("lazy val webcomponents = project")) {
      throw new Error("build-wc requires a scaffold with a webcomponents sbt project and scripts/build-webcomponents.mjs. Run init in a new directory first.");
    }
    const ui = path.resolve(cwd, config.sourceDir, "ui");
    const selected = requested.length ? [...new Set(requested)] : Object.keys(wrappers);
    for (const name of selected) {
      if (!Object.hasOwn(wrappers, name)) throw new Error(`No Web Component wrapper for ${name}. Supported: ${Object.keys(wrappers).join(", ")}`);
    }
    const available = await Promise.all(selected.map(async name => ({ name, present: await exists(path.join(ui, `${uiName(name)}.scala`)) })));
    if (requested.length) {
      for (const { name, present } of available) {
        if (!present) throw new Error(`Missing ${name} UI source; run shadcn-scalajs add ${name} first.`);
      }
    }
    const chosen = available.filter(item => item.present).map(item => item.name);
    if (!chosen.length) throw new Error("No supported UI components installed. Run shadcn-scalajs add button first.");

    const dest = path.join(cwd, "packages/webcomponents/src/main/scala/shadcnscalajs/webcomponents");
    const assets = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "../../assets/webcomponents");
    for (const name of [...runtime, ...chosen.map(item => wrappers[item])]) {
      const target = path.join(dest, `${name}.scala`);
      // These are consumer-owned adapters: never overwrite local edits on a rebuild.
      if (!(await exists(target))) {
        await mkdir(dest, { recursive: true });
        await copyFile(path.join(assets, `${name}.scala`), target);
        console.log(`+ ${path.relative(cwd, target)}`);
      }
    }
    const registered = (await Promise.all(Object.keys(wrappers).map(async name => ({
      name,
      present: await exists(path.join(dest, `${wrappers[name]}.scala`)) && await exists(path.join(ui, `${uiName(name)}.scala`))
    })))).filter(item => item.present).map(item => item.name);
    const entry = path.join(dest, "Main.scala");
    const source = mainSource(registered);
    if (!await exists(entry) || await readFile(entry, "utf8") !== source) {
      await writeFile(entry, source);
      console.log(`~ ${path.relative(cwd, entry)}`);
    }
    execFileSync(process.platform === "win32" ? "npm.cmd" : "npm", ["run", "build:webcomponents"], { cwd, stdio: "inherit" });
  });
