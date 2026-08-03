# Add to .claude/settings.json hooks section (or run: franky hooks install --claude):
{
  "hooks": {
    "PreToolUse": [
      { "matcher": "Write|Edit", "hooks": [{ "type": "command", "command": "/Users/locgorilla/StartUp/shadcn-scalajs/.franky/hooks/path_guard.sh" }] },
      { "matcher": "Write|Edit", "hooks": [{ "type": "command", "command": "/Users/locgorilla/StartUp/shadcn-scalajs/.franky/hooks/secret_scan.sh" }] },
      { "matcher": "Write|Edit", "hooks": [{ "type": "command", "command": "/Users/locgorilla/StartUp/shadcn-scalajs/.franky/hooks/lint_gate.sh" }] },
    ]
  }
}