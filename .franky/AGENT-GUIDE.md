# Franky — agent instructions

The model proposes; deterministic systems dispose. Follow this guide in every
franky-enabled project.

## First steps (every session)

1. Run `franky help` — or read `.franky/AGENT-GUIDE.md` or `.cursor/skills/franky/SKILL.md` (same content).
2. Read `AGENTS.md`, `REVIEW.md` if present, the active spec in `specs/features/`, and `.franky/memory/PROGRESS.md`.
3. Run `franky doctor`. If it flags `.franky/vaults/` missing, this repo adopted franky before a newer architecture shipped — run `franky bootstrap` once to sync it (regenerates the knowledge vault and any other newly-added scaffolding; safe to run on an already-bootstrapped repo).
4. For non-trivial work, use the orchestrator: keep coordination/integration local, use the configured frontier tier for planning/review, and spawn focused fast-tier sub-agents for bounded implementation or verification slices when available.
5. Work on a feature branch with a clean git state.
6. Before handing back: run `franky verify` and read `.franky/verify-report.json`.

## Commands

| Command | Purpose |
|---|---|
| `franky help` | This guide — read first |
| `franky hooks list` | Installed deterministic hooks |
| `franky hooks install --all` | Wire hooks into `.cursor/hooks.json` + `.claude/settings.json` |
| `franky scope` | Permission scope banner |
| `franky scope check-write PATH` | Enforce write path (used by path_guard hook) |
| `franky scope check-shell CMD` | Enforce egress on shell commands |
| `franky env` | Active environment manifest |
| `franky container up` | Build + run isolated Docker workspace |
| `franky mcp` | Merged MCP server registry |
| `franky visual` | Playwright capture + baseline diff |
| `franky visual --update-baseline` | Accept new screenshots as baseline |
| `franky run` | Orchestrator — verify + run-report (dry-run) |
| `franky run --container` | Orchestrator with verification scripts in Docker |
| `franky run --execute` | Invoke planner/coder/reviewer via agent CLI |
| `franky commands` | List slash commands from `.franky/commands.toml` |
| `franky verify` | Run setup → build → lint → test; ground truth for done |
| `franky verify --container` | Run setup → build → lint → test inside Docker sandbox |
| `franky verify --step test` | Tests only |
| `franky doctor` | Health-check franky setup |
| `franky doctor --adopt` | After adopting an existing repo |
| `franky init --dry-run` | Preview init without writing files |

## Orchestrator architecture

Franky uses a provider-neutral three-role pipeline. The planner and reviewer are
the frontier brain tier (`gpt-5.4` by default); the coder is the configured fast
worker tier (`gpt-5.4-mini` by default). The planner handoff is passed to the coder, then
deterministic verification runs before the reviewer sees the result. Configure
`[agent].command`, `model_flag`, and `[agent.model_map]` for the installed AI CLI.

## Script interface (do not improvise)

Use these instead of ad-hoc shell:

```bash
scripts/setup   # idempotent deps
scripts/build   # compile/package
scripts/lint    # static checks
scripts/test    # test suite — must pass
scripts/serve   # dev server (when configured)
```

Or use slash commands from `franky commands` (e.g. run `franky verify` for `/verify`).

## Definition of done

A task is done ONLY when ALL of these hold:

1. `franky verify` exits 0 (`passed: true` in `.franky/verify-report.json`).
2. Changes match the active spec acceptance criteria.
3. `.franky/memory/PROGRESS.md` is updated (done / next / blockers).
4. If implementation exposes missing project coverage or stale Franky setup (for
   example an omitted `admin-portal`), update the relevant rules, scripts,
   workspace/target config, and tests, then re-run verification.
5. Output is a reviewable git diff on a branch — never push to default branches.

## On verify failure

1. Read `.franky/verify-report.json` — check `steps[].excerpt` for the failing step.
2. Fix the code, or update Franky rules/scripts/tests when the report shows that
   project coverage, adapters, workspace scope, or targets do not match reality.
   Do NOT weaken tests or edit checks only to force a pass.
3. Run `franky init --refresh-adapters` after changing `.franky/scripts.toml`.
4. Re-run `franky verify` until pass.

## Memory (anti-drift)

- `.franky/memory/PROGRESS.md` — source of truth for task state.
- Keep `.franky/memory/PROGRESS.md` under ~5,000 characters; consolidate completed, no-longer-relevant Done entries before appending if it would exceed that.
- `.franky/memory/decisions.log` — append decisions with rationale.
- Re-read spec and current files from disk before consequential edits.

## Init / adopt (when adding franky to a repo)

```bash
franky init --dry-run --path .    # preview
franky init --path .              # adopt (non-destructive)
franky doctor --adopt
```

Existing `AGENTS.md` and native `scripts/` are preserved. Missing scripts get adapters.

## Never

- Claim done without passing `franky verify`.
- Delete, weaken, or hardcode around failing tests.
- Auto-push or write to production without human approval.
- Paste raw secret values into context — use handles from env only.

## Project layout

```
AGENTS.md
.fanky/config.toml       # model tiers, hooks
.fanky/scope.toml        # default-deny permissions
.fanky/commands.toml     # slash command registry
.fanky/memory/PROGRESS.md
specs/features/*.spec.md
scripts/setup|build|lint|test
```

## More

- Slash commands detail: `docs/slash-commands.md` (in franky repo)
- Framework vs frank service: franky = cross-project CLI; frank = Quarkus orchestration


## Post-init bootstrap (required once)

After `franky init`, `.franky/` is a **placeholder**. Before feature work:

1. Read `.franky/BOOTSTRAP.md`
2. Run `franky bootstrap` — insights on what to customize
3. Decide where sub-agents/orchestrator can help: split independent research, implementation, review, and verification tasks while keeping final integration local
4. Research every affected project, including frontends/admin portals; update scripts.toml, AGENTS.md, scope.toml, workspace.toml, targets.toml, and tests to match the repo
5. `franky verify` must pass; fix Franky configuration gaps discovered after implementation before claiming done
6. `franky bootstrap --complete "summary"` — or `franky bootstrap --execute` to run via agent CLI

Until bootstrap is complete, `franky doctor` reports bootstrap pending.
