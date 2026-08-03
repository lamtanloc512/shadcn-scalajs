#!/usr/bin/env bash
# franky hook: PreToolUse path guard — block writes outside allowed paths
set -euo pipefail

INPUT="$(cat)"
TOOL="$(echo "$INPUT" | jq -r '.tool_name // .tool // empty' 2>/dev/null || true)"
[[ "$TOOL" != "Write" && "$TOOL" != "Edit" ]] && exit 0

PATH_ARG="$(echo "$INPUT" | jq -r '.tool_input.file_path // .tool_input.path // empty' 2>/dev/null || true)"
[[ -z "$PATH_ARG" ]] && exit 0

# Anchor on this script's own location, not $PWD or a bare "." fallback:
# hooks are not guaranteed to run with CWD at the repo root (e.g. invoked from
# a subdirectory), and agent harnesses pass absolute file_paths, so a "."
# fallback silently degrades to comparing REL against PATH_ARG unchanged —
# every write then looks "outside scope" and gets blocked. FRANKY_ROOT still
# wins when set.
ROOT="${FRANKY_ROOT:-$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)}"
REL="${PATH_ARG#"$ROOT"/}"
REL="${REL#./}"

if command -v franky >/dev/null 2>&1; then
  if ! franky -C "$ROOT" scope check-write "$REL" >/dev/null 2>&1; then
    franky -C "$ROOT" scope check-write "$REL" 2>&1 || true
    echo "BLOCKED by franky path_guard" >&2
    exit 2
  fi
  exit 0
fi

# Fallback when franky CLI not on PATH
SCOPE="$ROOT/.franky/scope.toml"
allowed() {
  local p="$1"
  [[ "$p" == .franky/* ]] && return 0
  [[ "$p" == specs/* || "$p" == scripts/* || "$p" == src/* || "$p" == tests/* ]] && return 0
  [[ "$p" == AGENTS.md || "$p" == CLAUDE.md ]] && return 0
  [[ "$p" == .cursor/skills/franky/* || "$p" == .cursor/hooks* ]] && return 0
  return 1
}

if ! allowed "$REL"; then
  echo "BLOCKED by franky path_guard: write outside allowed scope: $REL" >&2
  exit 2
fi
exit 0
