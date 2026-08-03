#!/usr/bin/env bash
# franky hook: secret scan — block content that looks like secrets
set -euo pipefail

INPUT="$(cat)"
CONTENT="$(echo "$INPUT" | jq -r '.tool_input.content // .tool_input.new_string // empty' 2>/dev/null || true)"
[[ -z "$CONTENT" ]] && exit 0

PATTERNS=(
  'AKIA[0-9A-Z]{16}'
  'sk-[a-zA-Z0-9]{20,}'
  '-----BEGIN (RSA |OPENSSH |EC )?PRIVATE KEY-----'
  'xox[baprs]-[0-9A-Za-z-]{10,}'
)

for pat in "${PATTERNS[@]}"; do
  if echo "$CONTENT" | grep -qE -- "$pat"; then
    echo "BLOCKED by franky secret_scan: suspected secret pattern in tool input" >&2
    exit 2
  fi
done
exit 0
