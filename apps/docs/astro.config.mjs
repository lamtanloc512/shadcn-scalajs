// @ts-check
import { defineConfig } from "astro/config";
import starlight from "@astrojs/starlight";

export default defineConfig({
  integrations: [
    starlight({
      title: "shadcn-scalajs",
      description: "Copy-and-own shadcn/ui components for Scala.js and Laminar.",
      social: [
        {
          icon: "github",
          label: "GitHub",
          href: "https://github.com/lamtanloc512/shadcn-scalajs"
        }
      ],
      sidebar: [
        {
          label: "Getting started",
          items: [
            { label: "Introduction", link: "/" },
            { label: "Installation", link: "/installation/" },
            { label: "Existing project", link: "/existing-project/" },
            { label: "Project layout", link: "/project-layout/" },
            { label: "Add components", link: "/add-components/" },
            { label: "Build Web Components", link: "/web-components/" }
          ]
        },
        {
          label: "Agents",
          items: [{ label: "MCP server", link: "/mcp/" }]
        }
      ],
      editLink: {
        baseUrl: "https://github.com/lamtanloc512/shadcn-scalajs/edit/main/apps/docs/"
      }
    })
  ]
});
