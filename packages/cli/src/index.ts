#!/usr/bin/env node
import { Command } from "commander";
import { initCommand } from "./commands/init.js";
import { addCommand } from "./commands/add.js";

const program = new Command()
  .name("shadcn-scalajs")
  .description("Scaffold Scala.js + Laminar projects and add shadcn/ui-style components")
  .version("0.2.1");

program.addCommand(initCommand);
program.addCommand(addCommand);

program.parseAsync(process.argv);
