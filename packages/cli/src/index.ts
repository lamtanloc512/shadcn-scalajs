#!/usr/bin/env node
import { Command } from "commander";
import { initCommand } from "./commands/init.js";
import { addCommand } from "./commands/add.js";
import { mcpCommand } from "./commands/mcp.js";

const program = new Command()
  .name("shadcn-scalajs")
  .description("Scaffold Scala.js + Laminar projects and add shadcn/ui-style components")
  .version("0.3.2");

program.addCommand(initCommand);
program.addCommand(addCommand);
program.addCommand(mcpCommand);

program.parseAsync(process.argv);
