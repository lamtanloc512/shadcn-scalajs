# Franky bootstrap — customize to THIS repo (once)

> **Agents:** `.franky/` is a **placeholder**. Research the repo root, then **update franky** so rules match reality.

## Workflow

1. Read this file + run `franky bootstrap`
2. Review `.franky/vaults/Index.md` — the knowledge base `franky bootstrap` just (re)generated for this repo (or one entry per repo if this is a `workspace.toml` multi-repo setup). Open `.franky/vaults/` in Obsidian to browse it.
3. Research: README, CLAUDE.md, docs/, Cargo.toml / pom.xml / package.json
4. Use the orchestrator: configured frontier tier for planning/review, configured fast-tier workers for bounded implementation/verification slices, with final integration kept local
5. Research every affected project, including frontends/admin portals; update `.franky/scripts.toml`, `AGENTS.md`, `scope.toml`, `workspace.toml`, `targets.toml`, and tests to match reality
6. `franky init --refresh-adapters` after scripts.toml changes
7. `franky verify` must pass; fix Franky configuration gaps found after implementation before claiming done
8. `franky bootstrap --complete "what you changed"`

## Files to customize

| File | Purpose |
|---|---|
| `.franky/scripts.toml` | Real build/test commands |
| `AGENTS.md` | Project-specific agent rules |
| `.franky/scope.toml` | Allowed write paths |
| `.franky/workspace.toml` | Multi-repo layout |
| `.franky/targets.toml` | Web/visual targets |

## Knowledge vault

`.franky/vaults/` is a generated Obsidian vault: `Index.md` plus one `repos/<repo>/Overview.md`
per repo (grouped per entry when `workspace.toml` defines multiple projects). It's fully
regenerated on every `franky bootstrap` run — treat it as derived output, not a place to
hand-write notes.

Then follow `franky help` every session.