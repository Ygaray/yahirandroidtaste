#!/usr/bin/env bash
# Fixture test for tools/verify-tester-restored.sh (Phase 28, plan 28-07). Fully offline: a fake `adb`
# executable answers from environment variables and logs every invocation, so the test can also prove
# that the oracle never issues a state-changing command and never touches adb for a wireless serial.
set -euo pipefail
DIR="$(cd "$(dirname "$0")/.." && pwd)"
SCRIPT="$DIR/verify-tester-restored.sh"
[ -f "$SCRIPT" ] || { echo "FAIL: script under test is missing: $SCRIPT"; exit 1; }

TMP="$(mktemp -d)"; trap 'rm -rf "$TMP"' EXIT
FAKE="$TMP/adb"; LOG="$TMP/adb.log"

cat > "$FAKE" <<'EOF'
#!/usr/bin/env bash
echo "$*" >> "$FAKE_LOG"
if [ "${1:-}" = "-s" ]; then shift 2; fi
case "$*" in
  "get-state")
    if [ "${FAKE_STATE:-device}" = "device" ]; then echo device; else echo "${FAKE_STATE}"; exit 1; fi ;;
  "shell wm density") printf '%b\n' "${FAKE_WM_DENSITY}" ;;
  "shell settings get system font_scale") echo "${FAKE_FONT_SCALE}" ;;
  "shell settings get secure enabled_accessibility_services") echo "${FAKE_SERVICES}" ;;
  "shell settings get secure accessibility_enabled") echo "${FAKE_A11Y_ENABLED}" ;;
  "shell settings get secure touch_exploration_enabled") echo "${FAKE_TOUCH_EXPLORATION}" ;;
  "shell pm list packages "*)
    args="$*"; want="${args#shell pm list packages }"
    for p in ${FAKE_PACKAGES:-}; do
      case "$p" in *"$want"*) echo "package:$p" ;; esac
    done ;;
  *) echo "fake adb: unsupported command: $*" >&2; exit 9 ;;
esac
EOF
chmod 755 "$FAKE"

pass=0; fail=0
check(){ if [ "$1" = "$2" ]; then pass=$((pass+1)); else echo "FAIL: $3 (got $1 want $2)"; fail=$((fail+1)); fi; }
check_grep(){ # <text> <pattern> <label>
  if printf '%s\n' "$1" | grep -Eq -- "$2"; then pass=$((pass+1)); else echo "FAIL: $3 (pattern '$2' not in output)"; fail=$((fail+1)); fi
}

OUT=""; RC=0
run(){ # [VAR=val ...] -- [script args]
  : > "$LOG"
  local envs=()
  while [ "$1" != "--" ]; do envs+=("$1"); shift; done
  shift
  RC=0
  OUT=$(env ADB="$FAKE" FAKE_LOG="$LOG" FAKE_STATE=device \
    FAKE_WM_DENSITY='Physical density: 450' FAKE_FONT_SCALE=null FAKE_SERVICES=null \
    FAKE_A11Y_ENABLED=0 FAKE_TOUCH_EXPLORATION=0 FAKE_PACKAGES='' \
    "${envs[@]}" bash "$SCRIPT" "$@" 2>&1) || RC=$?
}
no_state_change(){ # <label>: no state-changing verb in the fake adb log of the last run
  local hits
  hits=$(grep -Ec '(^| )(settings put|install|uninstall|start|svc|input|clear|reboot|push|pull|kill-server)( |$)|wm density [0-9]|wm density reset|wm size|am |pm (grant|revoke|disable|enable)' "$LOG" || true)
  check "$hits" "0" "$1: no state-changing command was issued"
}

# 1. Everything restored (physical density only, font scale absent, nothing enabled, no harness).
run -- --package com.harness.fixture
check "$RC" "0" "restored exits 0"
check_grep "$OUT" '^RESTORED=yes$' "restored prints RESTORED=yes"
check_grep "$OUT" '^OK density' "restored reports density OK"
check_grep "$(cat "$LOG")" '^-s R5CT10XNKQN ' "every adb call carries the default USB serial"
no_state_change "restored"

# 2. Override density 480 is the effective density, so the tester is not restored.
run FAKE_WM_DENSITY='Physical density: 450\nOverride density: 480' --
check "$RC" "1" "override density 480 exits 1"
check_grep "$OUT" '^FAIL density got=480 want=450$' "override density names its check"
check_grep "$OUT" '^RESTORED=no$' "override density prints RESTORED=no"
no_state_change "override density"

# 3. An override back at 450 over a different physical density is restored.
run FAKE_WM_DENSITY='Physical density: 560\nOverride density: 450' --
check "$RC" "0" "override 450 over physical 560 exits 0"

# 4. A physical density other than 450 with no override line is not restored.
run FAKE_WM_DENSITY='Physical density: 560' --
check "$RC" "1" "physical 560 without override exits 1"

# 5. Font scale 2.0 differs; the setting recorded as 1.0 or absent is equal to 1.0.
run FAKE_FONT_SCALE=2.0 --
check "$RC" "1" "font scale 2.0 exits 1"
check_grep "$OUT" '^FAIL font_scale ' "font scale names its check"
run FAKE_FONT_SCALE=1.0 --
check "$RC" "0" "font scale 1.0 equals the default"
run FAKE_FONT_SCALE=1.15 -- --font-scale 1.15
check "$RC" "0" "font scale equals a recorded non-default expectation"

# 6. A non-null accessibility service list differs from the null expectation, and matches a recorded one.
run FAKE_SERVICES='com.harness/.Observer' --
check "$RC" "1" "enabled service exits 1"
check_grep "$OUT" '^FAIL enabled_accessibility_services ' "service names its check"
run FAKE_SERVICES='com.harness/.Observer' -- --services 'com.harness/.Observer'
check "$RC" "0" "service equals a recorded expectation"

# 7. accessibility_enabled 1 and touch_exploration_enabled 1.
run FAKE_A11Y_ENABLED=1 --
check "$RC" "1" "accessibility_enabled 1 exits 1"
check_grep "$OUT" '^FAIL accessibility_enabled ' "accessibility_enabled names its check"
run FAKE_TOUCH_EXPLORATION=1 --
check "$RC" "1" "touch_exploration_enabled 1 exits 1"
check_grep "$OUT" '^FAIL touch_exploration_enabled ' "touch_exploration_enabled names its check"
run FAKE_A11Y_ENABLED=null FAKE_TOUCH_EXPLORATION= --
check "$RC" "0" "null and empty accessibility values count as off"

# 8. A harness package that is still installed; a longer name that merely contains it is not a match.
run FAKE_PACKAGES='com.harness.fixture com.other.app' -- --package com.harness.fixture
check "$RC" "1" "installed harness package exits 1"
check_grep "$OUT" '^FAIL package:com.harness.fixture ' "package names its check"
run FAKE_PACKAGES='com.harness.fixture.extra' -- --package com.harness.fixture
check "$RC" "0" "only a longer package name is not a match"
no_state_change "package"

# 9. Device offline or unreadable is INFRA (exit 2), never a pass.
run FAKE_STATE=offline --
check "$RC" "2" "offline device exits 2"
run FAKE_WM_DENSITY='garbled' --
check "$RC" "2" "unparsable density exits 2"

# 10. A wireless serial is refused before any adb call.
run -- --serial 100.118.21.106:1496
check "$RC" "2" "wireless serial exits 2"
check "$(wc -c < "$LOG" | tr -d ' ')" "0" "wireless serial: the fake adb log is empty"

# 10b. A --package that is not a plain dotted package name never reaches the device shell.
for bad_pkg in 'a;b' 'com.x; reboot' 'com.x$(id)' 'com.x`id`' 'com.x|id' 'com.x&id' 'com.x id' '-a.b' 'single' 'com..x' '.com.x' 'com.x.'; do
  run -- --package "$bad_pkg"
  check "$RC" "2" "invalid package '$bad_pkg' exits 2"
  check "$(wc -c < "$LOG" | tr -d ' ')" "0" "invalid package '$bad_pkg': the fake adb log is empty"
done
run -- --package com.harness.fixture_2.Sub
check "$RC" "0" "a valid dotted package name with digits and underscores is accepted"

# 10c. Only a plain USB serial is accepted: mDNS wireless names and other shapes are refused too.
for bad_serial in 'adb-R5CT10XNKQN-abc123._adb-tls-connect._tcp' 'adb-XXXX-yyyy._adb-tls-connect._tcp.' \
    'emulator-5554' 'R5CT10XNKQN;reboot' 'R5CT.10' 'host.local' '-s' 'R5CT10XNKQN extra'; do
  run -- --serial "$bad_serial"
  check "$RC" "2" "serial '$bad_serial' exits 2"
  check "$(wc -c < "$LOG" | tr -d ' ')" "0" "serial '$bad_serial': the fake adb log is empty"
done
run -- --serial R5CT10XNKQN
check "$RC" "0" "the plain USB serial is accepted"
check_grep "$(cat "$LOG")" '^-s R5CT10XNKQN ' "a plain USB serial reaches adb"

# 11. Usage errors.
run -- --bogus
check "$RC" "2" "unknown option exits 2"
check "$(wc -c < "$LOG" | tr -d ' ')" "0" "unknown option: the fake adb log is empty"
run -- --density
check "$RC" "2" "option without a value exits 2"

echo "PASS=$pass FAIL=$fail"
[ "$fail" -eq 0 ]
