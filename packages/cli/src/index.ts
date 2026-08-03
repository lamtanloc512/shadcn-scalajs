#!/usr/bin/env node
import { Command } from "commander";
import { initCommand } from "./commands/init.js";
import { addCommand } from "./commands/add.js";

const program = new Command()
  .name("shadcn-scalajs")
  .description("Add shadcn/ui-style Scala.js + Laminar components to your project")
  .version("0.1.0");

program.addCommand(initCommand);
program.addCommand(addCommand);

program.parseAsync(process.argv);
