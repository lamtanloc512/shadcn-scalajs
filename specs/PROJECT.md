# shadcn-scalajs

Project overview for agent context. Feature specs live in `specs/features/`.

## Stack

- **Scala 3.5.2 + Scala.js 1.20.1 + Laminar 17.2.1**, sbt build (`sbt-scalajs`, `sbt-scalafmt`). No cross-project/JVM side — this product has no server.
- **Vite 5 + `@scala-js/vite-plugin-scalajs`** for the `modules/site` dev loop.
- **Node/TypeScript + Commander** for `packages/cli`.
- **basecoat** (vendored, Tailwind-compiled CSS) for styling/theming — see `vendor/NOTICE.md`.
- No headless-primitives library (no Radix/bits-ui equivalent exists for Laminar) — interactive components (`DropdownMenu`) hand-roll behavior with Airstream `Var`/`EventBus`; everything else prefers native elements (`<dialog>`, `<details>`) to avoid needing one.

See `/Users/locgorilla/.claude/plans/let-create-for-effervescent-penguin.md` for full architecture rationale and `AGENTS.md`'s "Project rules" section for concrete gotchas and commands.

## Verify

```bash
export PATH="$PATH:/Users/locgorilla/Library/Application Support/Coursier/bin"
sbt core/compile ui/compile webcomponents/compile site/compile
```

`franky verify` itself is not yet wired to anything Scala-specific — `scripts/lint`/`scripts/test` are still franky's no-op stubs. Use the sbt/npm commands in `AGENTS.md` directly until those are filled in.
