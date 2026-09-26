import { Command } from "commander";
import { existsSync } from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";
import { McpServer } from "@modelcontextprotocol/sdk/server/mcp.js";
import { StdioServerTransport } from "@modelcontextprotocol/sdk/server/stdio.js";
import { z } from "zod";
import { DEFAULT_REGISTRY, readConfig } from "../utils/config.js";
import { fetchIndex, fetchItem, type RegistryIndexEntry, type RegistryItem } from "../utils/registry.js";

type ItemRecord = RegistryItem & {
  description?: string;
  categories?: string[];
};

const itemTypes = ["scala:ui", "scala:block", "css:theme"] as const;

function repoRegistry(): string | undefined {
  const here = path.dirname(fileURLToPath(import.meta.url));
  const candidate = path.resolve(here, "../../../apps/web/public/registry");
  return existsSync(path.join(candidate, "index.json")) ? candidate : undefined;
}

async function resolveRegistry(explicit?: string): Promise<string> {
  if (explicit) return explicit;
  try {
    return (await readConfig(process.cwd())).registry;
  } catch {
    return repoRegistry() ?? DEFAULT_REGISTRY;
  }
}

function installCommand(names: string[]): string {
  return `npx shadcn-scalajs@latest add ${names.join(" ")}`;
}

function summarize(entry: RegistryIndexEntry): Record<string, unknown> {
  return {
    name: entry.name,
    title: entry.title,
    type: entry.type,
    registryDependencies: entry.registryDependencies
  };
}

function text(value: unknown) {
  return { content: [{ type: "text" as const, text: JSON.stringify(value, null, 2) }] };
}

function failure(message: string) {
  return { content: [{ type: "text" as const, text: message }], isError: true as const };
}

async function startServer(registry: string): Promise<void> {
  const server = new McpServer({ name: "shadcn-scalajs", version: "0.3.2" });

  server.registerTool(
    "list_items",
    {
      description:
        "List shadcn-scalajs registry items (Laminar components, blocks, and theme packs). Use this before installing.",
      inputSchema: {
        type: z.enum(itemTypes).optional().describe("Filter by registry item type. Omit to return every item.")
      }
    },
    async ({ type }) => {
      const index = await fetchIndex(registry);
      const items = type ? index.filter(entry => entry.type === type) : index;
      return text({ registry, count: items.length, items: items.map(summarize) });
    }
  );

  server.registerTool(
    "search_items",
    {
      description: "Find registry items whose name or title contains the query. Matching is case-insensitive.",
      inputSchema: {
        query: z.string().min(1).describe("Name or title fragment, for example button or login.")
      }
    },
    async ({ query }) => {
      const needle = query.toLowerCase();
      const items = (await fetchIndex(registry)).filter(entry =>
        entry.name.toLowerCase().includes(needle) || entry.title.toLowerCase().includes(needle)
      );
      return text({ registry, count: items.length, items: items.map(summarize) });
    }
  );

  server.registerTool(
    "get_item",
    {
      description:
        "Read one registry item: description, dependencies, file targets, and the add command. Set includeSource when you need the Scala source that would be copied into the project.",
      inputSchema: {
        name: z.string().describe("Registry item name, for example button or login-01."),
        includeSource: z
          .boolean()
          .optional()
          .describe("Include each file's Scala or CSS contents. Defaults to false.")
      }
    },
    async ({ name, includeSource }) => {
      let item: ItemRecord;
      try {
        item = (await fetchItem(registry, name)) as ItemRecord;
      } catch (error) {
        const message = error instanceof Error ? error.message : String(error);
        return failure(`No registry item named ${name}. ${message}`);
      }
      return text({
        name: item.name,
        title: item.title,
        type: item.type,
        description: item.description ?? "",
        categories: item.categories ?? [],
        registryDependencies: item.registryDependencies,
        scalaDependencies: item.scalaDependencies,
        install: installCommand([item.name]),
        files: item.files.map(file => ({
          target: file.target,
          type: file.type,
          ...(includeSource ? { content: file.content } : {})
        }))
      });
    }
  );

  server.registerTool(
    "install_command",
    {
      description:
        "Return the CLI command that copies the named components or blocks into a project, including transitive registry dependencies when add runs.",
      inputSchema: {
        names: z.array(z.string().min(1)).min(1).describe("Registry item names to pass to add.")
      }
    },
    async ({ names }) => text({ command: installCommand(names), registry })
  );

  const transport = new StdioServerTransport();
  await server.connect(transport);
  console.error(`shadcn-scalajs mcp listening on stdio (registry: ${registry})`);
}

export const mcpCommand = new Command("mcp")
  .description("Start the Model Context Protocol server on stdio for coding agents")
  .option("--registry <url-or-path>", "registry URL or local directory of registry JSON")
  .action(async (opts: { registry?: string }) => {
    const registry = await resolveRegistry(opts.registry);
    await startServer(registry);
  });
