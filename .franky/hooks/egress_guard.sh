#!/usr/bin/env bash
# franky hook: egress guard — block shell commands to hosts outside scope
set -euo pipefail

INPUT="$(cat)"
CMD="$(echo "$INPUT" | jq -r '.command // .tool_input.command // empty' 2>/dev/null || true)"
[[ -z "$CMD" ]] && exit 0

# Anchor on this script's own location, not $PWD or a bare "." fallback —
# hooks are not guaranteed to run with CWD at the repo root. FRANKY_ROOT still
# wins when set.
ROOT="${FRANKY_ROOT:-$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)}"
export FRANKY_ROOT="$ROOT"

if command -v franky >/dev/null 2>&1; then
  if ! franky -C "$ROOT" scope check-shell "$CMD" >/dev/null 2>&1; then
    franky -C "$ROOT" scope check-shell "$CMD" 2>&1 || true
    echo "BLOCKED by franky egress_guard" >&2
    exit 2
  fi
  exit 0
fi

# Fallback: block obvious curl/wget to non-localhost when FRANKY_ENV=sandbox
if [[ "${FRANKY_ENV:-sandbox}" == "sandbox" ]]; then
  if echo "$CMD" | grep -qE '(curl|wget|fetch).*(https?://[^/ ]+)'; then
    URL="$(echo "$CMD" | grep -oE 'https?://[^/ ]+' | head -1)"
    HOST="$(echo "$URL" | sed -E 's|https?://||; s|/.*||; s|:.*||')"
    [[ "$HOST" == "localhost" || "$HOST" == "127.0.0.1" ]] && exit 0
    echo "BLOCKED by franky egress_guard (sandbox): remote host $HOST — install franky CLI for full scope checks" >&2
    exit 2
  fi
fi
exit 0
