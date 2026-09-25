---
title: MCP server
description: Let a coding agent list, read, and install shadcn-scalajs components.
---

`shadcn-scalajs mcp` is a Model Context Protocol server on stdio. It reads the same registry as `add`. The official shadcn MCP server expects React registry items, so it does not understand this registry's `scala:ui` and `scala:block` payloads.

## Connect a client

```json
{
  "mcpServers": {
    "shadcn-scalajs": {
      "command": "npx",
      "args": ["-y", "shadcn-scalajs@latest", "mcp"]
    }
  }
}
```

The server uses the registry in `shadcn-scalajs.json` when that file is in the working directory. Otherwise it uses `https://shadcn-scalajs.vercel.app/registry`.

Inside this repository, after `npm run build` in `packages/cli`:

```bash
node packages/cli/dist/index.js mcp \
  --registry modules/site/public/registry
```

The process writes protocol messages to stdout. Logs go to stderr. Do not wrap the command in a shell that prints a banner.

## Tools

| Tool | What the agent gets |
| --- | --- |
| `list_items` | Names, titles, types, and registry dependencies. Optional `type` filter: `scala:ui`, `scala:block`, or `css:theme`. |
| `search_items` | Items whose name or title contains the query. |
| `get_item` | Description, dependencies, file targets, and the `add` command. `includeSource` adds the file contents that would be copied. |
| `install_command` | `npx shadcn-scalajs@latest add …` for the names you pass. |

`get_item` leaves source out unless the agent asks for it. Registry files inline the full Scala source, and a block can be large.

The server does not write files. The agent runs the printed `add` command, or calls `add` itself, when it is ready to copy components into the project.
