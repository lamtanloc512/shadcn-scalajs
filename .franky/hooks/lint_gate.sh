#!/usr/bin/env bash
# franky hook: lint gate — run scripts/lint before Write/Edit batches (optional pre-commit style)
set -euo pipefail

# Anchor on this script's own location, not $PWD or a bare "." fallback —
# hooks are not guaranteed to run with CWD at the repo root. FRANKY_ROOT still
# wins when set.
ROOT="${FRANKY_ROOT:-$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)}"
LINT="$ROOT/scripts/lint"
[[ ! -x "$LINT" ]] && exit 0

if ! "$LINT" >/dev/null 2>&1; then
  echo "BLOCKED by franky lint_gate: scripts/lint failed — fix before editing" >&2
  exit 2
fi
exit 0
