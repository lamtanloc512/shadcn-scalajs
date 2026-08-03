import { readFile } from "node:fs/promises";
import path from "node:path";

export interface RegistryItemFile {
  content: string;
  type: string;
  target: string;
}

export interface RegistryItem {
  name: string;
  title: string;
  type: string;
  registryDependencies: string[];
  scalaDependencies: string[];
  files: RegistryItemFile[];
}

export interface RegistryIndexEntry {
  name: string;
  title: string;
  type: string;
  registryDependencies: string[];
}

/** `registry` is either an http(s) base URL or a local directory path
 * (the latter mainly for testing against `modules/site/public/registry`
 * without needing a deployed registry host). */
async function fetchJson<T>(registry: string, relativePath: string): Promise<T> {
  if (registry.startsWith("http://") || registry.startsWith("https://")) {
    const res = await fetch(`${registry.replace(/\/$/, "")}/${relativePath}`);
    if (!res.ok) throw new Error(`Registry fetch failed (${res.status}): ${relativePath}`);
    return (await res.json()) as T;
  }
  const filePath = path.join(registry, relativePath);
  return JSON.parse(await readFile(filePath, "utf-8")) as T;
}

export async function fetchIndex(registry: string): Promise<RegistryIndexEntry[]> {
  return fetchJson<RegistryIndexEntry[]>(registry, "index.json");
}

export async function fetchItem(registry: string, name: string): Promise<RegistryItem> {
  return fetchJson<RegistryItem>(registry, `${name}.json`);
}

/** Recursively resolves `registryDependencies`, matching shadcn-svelte's
 * `resolveRegistryItems`/`fetchRegistryItems` split — walks the dependency
 * graph first, dedupes by name, then fetches every item's full content. */
export async function resolveItems(registry: string, names: string[]): Promise<RegistryItem[]> {
  const resolved = new Map<string, RegistryItem>();

  async function visit(name: string): Promise<void> {
    if (resolved.has(name)) return;
    const item = await fetchItem(registry, name);
    resolved.set(name, item);
    for (const dep of item.registryDependencies) {
      await visit(dep);
    }
  }

  for (const name of names) {
    await visit(name);
  }

  return [...resolved.values()];
}
