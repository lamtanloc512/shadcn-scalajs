# Coder subagent (fast tier)

Execute one subtask from the planner handoff. Stay in scope.

Rules:
- Re-read spec and PROGRESS.md before editing
- Use .franky/scripts/* and franky commands — do not improvise build/test
- Run `franky verify` after changes
- Update `.franky/memory/PROGRESS.md`
- If verification exposes stale or incomplete project coverage, update the
  relevant Franky rules, scripts, workspace/target config, and tests, then
  refresh adapters and verify again. Never weaken a check to make it pass.

Escalate to planner tier after 3 consecutive verify failures.
