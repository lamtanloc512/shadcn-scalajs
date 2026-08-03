# Franky base instructions

These rules apply to every project that adopts franky. Project-specific rules live in `AGENTS.md`.

## Session start

Before making any changes, read in full (not just skim):

1. `AGENTS.md` (this file merges in — you're already reading it)
2. `.franky/memory/PROGRESS.md` — the source of truth for what's done, what's next, and known follow-ups. Do not re-derive this from `git log` if it's documented here already.
3. Any `REVIEW.md` at the repo root, if present — prior review findings that may still be open.
4. The active spec under `specs/features/*.spec.md`, if the task at hand matches one.

If `PROGRESS.md` claims something is done, verify it against current source before trusting it (run `franky verify`) — but don't re-derive context that's already written down correctly.

## Core principle

The model proposes; deterministic systems dispose. Specs constrain intent, hooks constrain actions, tests constrain correctness, isolation constrains reach, memory constrains drift, and diff-review constrains what lands.

## Git discipline

1. Start from a clean working tree on a feature branch or worktree.
2. Make focused changes aligned with the active spec.
3. Run `franky verify` before handing work back.
4. The unit of delivery is a **reviewable git diff** — never push to default branches without human approval.

## Verification

- Ground truth is `franky verify` / `scripts/test` exit status — not self-report.
- Read `.franky/verify-report.json` on failure and iterate until `passed: true`.
- Do not weaken, delete, or hardcode around failing tests.

## Memory

- Keep `.franky/memory/PROGRESS.md` updated: done / next / blockers.
- Cap `PROGRESS.md` around 5,000 characters. When an update would exceed that, consolidate first: collapse completed, no-longer-relevant "Done" entries into a single short summary line (or drop them if superseded), rather than appending indefinitely. The file should always be fast for a fresh agent to read in full — that's its entire purpose.
- Append decisions to `.franky/memory/decisions.log` with rationale.
- Re-read spec and current file state from disk before consequential edits.

## Scope & secrets

- Default environment is sandbox/test. Production is opt-in and gated.
- Reference secrets by handle only — never paste raw secret values into context.
- Do not commit credentials or write outside granted paths.

## Never

- Auto-push or write to external systems without human approval.
- Edit verifier-owned tests or graded files to force a pass.
- Skip `franky verify` and claim done based on reasoning alone.
