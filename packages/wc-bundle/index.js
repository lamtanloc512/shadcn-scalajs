const html = document.documentElement;
html.setAttribute("data-sc-no-stored-theme", "");
html.setAttribute("data-sc-isolated", "");
const stylePack = html.getAttribute("data-style-pack");
if (stylePack && stylePack !== "nova") {
  throw new Error(`The default Web Component bundle only includes the Nova pack, not ${stylePack}`);
}
if (!stylePack) html.setAttribute("data-style-pack", "nova");
const assetsBase = new URL(".", import.meta.url).href.replace(/\/$/, "");

if (!html.hasAttribute("data-sc-assets-base")) {
  html.setAttribute("data-sc-assets-base", assetsBase);
}
if (!html.hasAttribute("data-sc-pack-base")) {
  html.setAttribute("data-sc-pack-base", new URL("styles", import.meta.url).href);
}

const documentCss = new URL("./sc-document.css", import.meta.url).href;
const existing = [...document.querySelectorAll('link[rel="stylesheet"]')].find(link => link.href === documentCss);
if (existing && !existing.sheet) {
  await new Promise((resolve, reject) => {
    existing.addEventListener("load", resolve, { once: true });
    existing.addEventListener("error", () => reject(new Error(`Failed to load ${documentCss}`)), { once: true });
  });
}
if (!existing) {
  const preload = document.createElement("link");
  preload.rel = "preload";
  preload.as = "style";
  preload.href = documentCss;
  document.head.appendChild(preload);
  const sheet = document.createElement("link");
  sheet.rel = "stylesheet";
  sheet.href = documentCss;
  const loaded = new Promise((resolve, reject) => {
    sheet.addEventListener("load", resolve, { once: true });
    sheet.addEventListener("error", () => reject(new Error(`Failed to load ${documentCss}`)), { once: true });
  });
  document.head.appendChild(sheet);
  await loaded;
}
await import("./sc-components.js");
