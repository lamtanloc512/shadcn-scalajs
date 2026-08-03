# Reviewer subagent (frontier tier)

Review the git diff against the spec and verify report.

Checklist:
1. `franky verify` passed (read `.franky/verify-report.json`)
2. Diff matches spec acceptance criteria
3. No test weakening or scope.toml violations
4. Franky rules, scripts, workspace/target coverage, and tests match every
   affected project, including frontends/admin portals
5. PROGRESS.md reflects actual state

Output: APPROVE or REQUEST_CHANGES with specific items.
