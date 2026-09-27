import { copyFileSync, mkdirSync } from "node:fs";
import { dirname, join, resolve } from "node:path";
import { fileURLToPath } from "node:url";

// Keep the published CLI's adapters identical to the repository's Web Component layer.
const root = resolve(dirname(fileURLToPath(import.meta.url)), "../../..");
const source = join(root, "packages/webcomponents/src/main/scala/shadcnscalajs/webcomponents");
const dest = join(root, "packages/cli/assets/webcomponents");
const names = [
  "LightPrimitive", "ScElementBase", "ScElements", "ScStyles", "ScThemeApi", "ScThemeState",
  "ScAccordion", "ScAlertDialog", "ScAspectRatio", "ScBadge", "ScButton", "ScCalendar", "ScCarousel", "ScCard", "ScChart",
  "ScCheckbox", "ScCombobox", "ScContextMenu", "ScDataTable", "ScDatePicker", "ScDialog", "ScDrawer", "ScDropdownMenu", "ScHoverCard", "ScInput", "ScInputGroup", "ScInputOTP", "ScItem", "ScMenubar", "ScNavigationMenu",
  "ScPagination", "ScProgress", "ScRangeCalendar", "ScResizable", "ScScrollArea", "ScSelect", "ScSheet", "ScSidebar", "ScSlider", "ScSonner", "ScSwitch", "ScTabs", "ScTextarea", "ScThemeSwitcher", "ScToggle"
];
mkdirSync(dest, { recursive: true });
for (const name of names) copyFileSync(join(source, `${name}.scala`), join(dest, `${name}.scala`));
console.log(`Synced ${names.length} Web Component Scala sources into CLI assets`);
