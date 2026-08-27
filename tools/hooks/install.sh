#!/usr/bin/env bash
set -euo pipefail
ROOT="$(git rev-parse --show-toplevel)"
ln -sf ../../tools/hooks/pre-commit "$ROOT/.git/hooks/pre-commit"
chmod +x "$ROOT/tools/hooks/pre-commit"
echo "installed hub pre-commit additive guard"
