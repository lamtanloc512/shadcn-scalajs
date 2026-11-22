#!/usr/bin/env node
// Writes public/sitemap.xml from the built registry, so every component and block page the site serves is listed
// without a second hand-kept list. Run after build-registry.

import { readFileSync, writeFileSync } from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";

const site = "https://scalajs.auvyx.org";
const here = path.dirname(fileURLToPath(import.meta.url));
const publicDir = path.resolve(here, "../public");
const index = JSON.parse(readFileSync(path.join(publicDir, "registry/index.json"), "utf8"));
const items = Array.isArray(index) ? index : index.items;

// Guide pages mirror DocsPage.pages in apps/web/src/main/scala/shadcnscalajs/site/DocsPage.scala.
const guide = ["installation", "existing-project", "project-layout", "components", "mcp"];
const pages = [
  "/",
  "/docs",
  ...guide.map((slug) => `/docs/${slug}`),
  "/components",
  "/blocks",
  "/create",
  "/web-components",
  "/components/typography", // a recipe page, not a registry item
  ...items.filter((item) => item.type === "scala:ui").map((item) => `/components/${item.name}`),
  ...items.filter((item) => item.type === "scala:block").map((item) => `/blocks/${item.name}`)
];
// Internal helpers (floating, menu, icons, …) are registry items without a page of their own.
const hidden = new Set(["/components/floating", "/components/menu", "/components/icons"]);

const xml = [
  '<?xml version="1.0" encoding="UTF-8"?>',
  '<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">',
  ...[...new Set(pages)].filter((p) => !hidden.has(p)).map((p) => `  <url><loc>${site}${p}</loc></url>`),
  "</urlset>",
  ""
].join("\n");
writeFileSync(path.join(publicDir, "sitemap.xml"), xml);
console.log(`Built sitemap: ${xml.split("<url>").length - 1} URLs -> public/sitemap.xml`);
