#!/usr/bin/env bash
# Read-only oracle of the Gate-1 restore step for the UAT TESTER (Phase 28, plan 28-07).
#
# READ-ONLY: it only reads device state through `adb -s <usb serial>` getters. It never changes the
# device. The restore itself (`wm density 450`, NEVER `wm density reset`; the font scale, the
# accessibility settings and the harness uninstall) is the gate's job; this script proves it worked.
#
# Reads: wm density, system font_scale, secure enabled_accessibility_services, secure
# accessibility_enabled, secure touch_exploration_enabled, and `pm list packages <name>` for each
# --package.
#
# Usage: verify-tester-restored.sh [--serial <usb serial>] [--density <n>] [--font-scale <v>]
#          [--services <v>] [--a11y-enabled <v>] [--touch-exploration <v>] [--package <name>]...
#   defaults: serial R5CT10XNKQN, density 450, font scale 1.0, services null, a11y-enabled null,
#             touch-exploration null (the values recorded in the Phase 26 and 27 self-UAT logs).
#   env ADB: the adb executable (default `adb`).
#
# Exit: 0 every check OK (RESTORED=yes); 1 any check differs (RESTORED=no);
#       2 usage error (including a --package that is not a plain dotted package name), a wireless serial (host:port, refused before any adb call), or a device that
#         cannot be read. 2 is INFRA, never a pass.
set -euo pipefail

ADB="${ADB:-adb}"
SERIAL="R5CT10XNKQN"
WANT_DENSITY="450"
WANT_FONT="1.0"
WANT_SERVICES="null"
WANT_A11Y="null"
WANT_TOUCH="null"
PACKAGES=()

die(){ echo "ERROR: $*" >&2; exit 2; }
need_value(){ [ "$#" -ge 2 ] || die "option $1 needs a value"; }

while [ "$#" -gt 0 ]; do
  case "$1" in
    --serial) need_value "$@"; SERIAL="$2"; shift 2 ;;
    --density) need_value "$@"; WANT_DENSITY="$2"; shift 2 ;;
    --font-scale) need_value "$@"; WANT_FONT="$2"; shift 2 ;;
    --services) need_value "$@"; WANT_SERVICES="$2"; shift 2 ;;
    --a11y-enabled) need_value "$@"; WANT_A11Y="$2"; shift 2 ;;
    --touch-exploration) need_value "$@"; WANT_TOUCH="$2"; shift 2 ;;
    --package)
      need_value "$@"
      # `adb shell` joins its arguments and hands them to the device shell, so quoting here does not
      # protect the value: only a plain dotted package name may reach adb.
      [[ "$2" =~ ^[A-Za-z][A-Za-z0-9_]*(\.[A-Za-z0-9_]+)+$ ]] || die "invalid package name: $2"
      PACKAGES+=("$2"); shift 2 ;;
    *) die "unknown option: $1" ;;
  esac
done

# The wireless personal phone is addressed as host:port; the tester is USB only.
case "$SERIAL" in
  *:*) die "refusing a wireless serial ($SERIAL): the tester is addressed by its USB serial only" ;;
esac

adb_get(){ "$ADB" -s "$SERIAL" "$@"; }

STATE=$(adb_get get-state 2>/dev/null) || die "device $SERIAL is not readable (get-state failed)"
[ "$STATE" = "device" ] || die "device $SERIAL is not ready (state: $STATE)"

DENSITY_OUT=$(adb_get shell wm density 2>/dev/null) || die "cannot read wm density"
FONT_GOT=$(adb_get shell settings get system font_scale 2>/dev/null) || die "cannot read font_scale"
SERVICES_GOT=$(adb_get shell settings get secure enabled_accessibility_services 2>/dev/null) \
  || die "cannot read enabled_accessibility_services"
A11Y_GOT=$(adb_get shell settings get secure accessibility_enabled 2>/dev/null) \
  || die "cannot read accessibility_enabled"
TOUCH_GOT=$(adb_get shell settings get secure touch_exploration_enabled 2>/dev/null) \
  || die "cannot read touch_exploration_enabled"

trim(){ printf '%s' "$1" | tr -d '\r' | sed -e 's/^[[:space:]]*//' -e 's/[[:space:]]*$//'; }

# Effective density: the override when one exists, the physical density otherwise.
OVERRIDE=$(printf '%s\n' "$DENSITY_OUT" | tr -d '\r' | sed -n 's/^Override density:[[:space:]]*\([0-9][0-9]*\).*/\1/p' | tail -n 1)
PHYSICAL=$(printf '%s\n' "$DENSITY_OUT" | tr -d '\r' | sed -n 's/^Physical density:[[:space:]]*\([0-9][0-9]*\).*/\1/p' | tail -n 1)
if [ -n "$OVERRIDE" ]; then
  DENSITY_GOT="$OVERRIDE"
elif [ -n "$PHYSICAL" ]; then
  DENSITY_GOT="$PHYSICAL"
else
  die "cannot parse the density from: $DENSITY_OUT"
fi

fails=0
ok(){ echo "OK $1"; }
bad(){ echo "FAIL $1 got=$2 want=$3"; fails=$((fails+1)); }

# Normalised accessibility value: empty and the literal null are the same "unset".
norm_null(){ local v; v=$(trim "$1"); if [ -z "$v" ]; then echo null; else echo "$v"; fi; }
# Same, and for the two boolean settings a 0 is the same as unset when the expectation is unset.
norm_flag(){ local v; v=$(norm_null "$1"); if [ "$v" = "0" ]; then echo null; else echo "$v"; fi; }

if [ "$DENSITY_GOT" = "$WANT_DENSITY" ]; then ok density; else bad density "$DENSITY_GOT" "$WANT_DENSITY"; fi

FONT_N=$(norm_null "$FONT_GOT")
[ "$FONT_N" = "null" ] && FONT_N="1.0"
FONT_W=$(norm_null "$WANT_FONT")
[ "$FONT_W" = "null" ] && FONT_W="1.0"
if awk -v a="$FONT_N" -v b="$FONT_W" 'BEGIN { exit !(a + 0 == b + 0) }'; then
  ok font_scale
else
  bad font_scale "$(trim "$FONT_GOT")" "$WANT_FONT"
fi

SERVICES_N=$(norm_null "$SERVICES_GOT"); SERVICES_W=$(norm_null "$WANT_SERVICES")
if [ "$SERVICES_N" = "$SERVICES_W" ]; then ok enabled_accessibility_services; else bad enabled_accessibility_services "$SERVICES_N" "$SERVICES_W"; fi

A11Y_N=$(norm_flag "$A11Y_GOT"); A11Y_W=$(norm_flag "$WANT_A11Y")
if [ "$A11Y_N" = "$A11Y_W" ]; then ok accessibility_enabled; else bad accessibility_enabled "$(norm_null "$A11Y_GOT")" "$WANT_A11Y"; fi

TOUCH_N=$(norm_flag "$TOUCH_GOT"); TOUCH_W=$(norm_flag "$WANT_TOUCH")
if [ "$TOUCH_N" = "$TOUCH_W" ]; then ok touch_exploration_enabled; else bad touch_exploration_enabled "$(norm_null "$TOUCH_GOT")" "$WANT_TOUCH"; fi

for pkg in "${PACKAGES[@]}"; do
  LISTED=$(adb_get shell pm list packages "$pkg" 2>/dev/null | tr -d '\r') || die "cannot list packages for $pkg"
  # `pm list packages <filter>` is a substring filter; only the exact line counts as installed.
  if grep -Fxq "package:$pkg" <<<"$LISTED"; then
    bad "package:$pkg" installed absent
  else
    ok "package:$pkg"
  fi
done

if [ "$fails" -eq 0 ]; then echo "RESTORED=yes"; exit 0; fi
echo "RESTORED=no"
exit 1
