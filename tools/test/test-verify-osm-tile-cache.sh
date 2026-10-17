#!/usr/bin/env bash
# Fixture test for tools/verify-osm-tile-cache.py (Phase 28, POL-03). Fully offline: the fixture
# databases are built with python3's sqlite3 module using the exact osmdroid DDL and the real
# blocked placeholder bytes committed at src/test/resources/osm-blocked-placeholder.png.
set -euo pipefail
DIR="$(cd "$(dirname "$0")/.." && pwd)"
SCRIPT="$DIR/verify-osm-tile-cache.py"
[ -f "$SCRIPT" ] || { echo "FAIL: script under test is missing: $SCRIPT"; exit 1; }
command -v python3 >/dev/null || { echo "FAIL: python3 not found"; exit 1; }
python3 -I -c 'import sqlite3' || { echo "FAIL: python3 has no sqlite3 support"; exit 1; }
ROOT="$(cd "$DIR/.." && pwd)"
PLACEHOLDER="$ROOT/src/test/resources/osm-blocked-placeholder.png"
[ -f "$PLACEHOLDER" ] || { echo "FAIL: placeholder fixture missing: $PLACEHOLDER"; exit 1; }

TMP="$(mktemp -d)"; trap 'rm -rf "$TMP"' EXIT

pass=0; fail=0
check(){ if [ "$1" = "$2" ]; then pass=$((pass+1)); echo "ok: $3"; else echo "FAIL: $3 (got $1 want $2)"; fail=$((fail+1)); fi; }

# mkdb <file> <spec>...   spec: real | placeholder | sizeonly | placeholder@Provider | empty ; "none" = no tiles table
mkdb(){
  local f="$1"; shift
  python3 -I - "$f" "$PLACEHOLDER" "$@" <<'PY'
import sqlite3, sys
f, ph, *specs = sys.argv[1:]
conn = sqlite3.connect(f)
if specs == ["none"]:
    conn.execute("CREATE TABLE other (x INTEGER)")
else:
    conn.execute("CREATE TABLE IF NOT EXISTS tiles (key INTEGER , provider TEXT, tile BLOB, expires INTEGER, PRIMARY KEY (key, provider))")
    pb = open(ph, "rb").read()
    for i, spec in enumerate(specs):
        name, _, prov = spec.partition("@")
        prov = prov or "Mapnik"
        blob = {"real": bytes((j * 31 + 7) % 256 for j in range(4472)),
                "placeholder": pb,
                "sizeonly": b"\x00" * 6987,
                "empty": b""}[name]
        conn.execute("INSERT INTO tiles VALUES (?,?,?,?)", (i + 1, prov, blob, 4102444800000))
conn.commit(); conn.close()
PY
}

run(){ # prints "<rc>|<stdout>"
  local rc=0 out
  out="$(python3 -I "$SCRIPT" "$@" 2>/dev/null)" || rc=$?
  echo "$rc|$out"
}

mkdb "$TMP/real.db" real
check "$(run "$TMP/real.db")" "0|PLACEHOLDER_ROWS=0 REAL_ROWS=1 TOTAL_ROWS=1" "one real tile, no placeholder passes"

mkdb "$TMP/ph.db" placeholder real
check "$(run "$TMP/ph.db" | cut -d'|' -f1)" "1" "placeholder present fails the default assertion"
check "$(run "$TMP/ph.db" --expect-placeholder)" "0|PLACEHOLDER_ROWS=1 REAL_ROWS=1 TOTAL_ROWS=2" "placeholder present passes --expect-placeholder"

mkdb "$TMP/phonly.db" placeholder
check "$(run "$TMP/phonly.db" | cut -d'|' -f1)" "1" "placeholder only fails the default assertion"

mkdb "$TMP/emptytable.db" empty
check "$(run "$TMP/emptytable.db" | cut -d'|' -f1)" "1" "no real tile cannot prove the wire (default)"
check "$(run "$TMP/emptytable.db" --expect-placeholder | cut -d'|' -f1)" "1" "no placeholder fails --expect-placeholder"

mkdb "$TMP/realonly.db" real real
check "$(run "$TMP/realonly.db" --expect-placeholder | cut -d'|' -f1)" "1" "--expect-placeholder with a real-only database fails"

mkdb "$TMP/sizeonly.db" sizeonly
check "$(run "$TMP/sizeonly.db")" "0|PLACEHOLDER_ROWS=0 REAL_ROWS=1 TOTAL_ROWS=1" "a 6987-byte row of other bytes counts as real"

mkdb "$TMP/otherprov.db" placeholder@OpenTopo real
check "$(run "$TMP/otherprov.db" | cut -d'|' -f1)" "1" "placeholder under a different provider still counts"

printf 'this is not a database, just text padding padding padding padding\n' > "$TMP/garbage.db"
check "$(run "$TMP/garbage.db" | cut -d'|' -f1)" "2" "non-database file exits 2"
check "$(run "$TMP/missing.db" | cut -d'|' -f1)" "2" "missing file exits 2"

mkdb "$TMP/notiles.db" none
check "$(run "$TMP/notiles.db" | cut -d'|' -f1)" "2" "database without a tiles table exits 2"

check "$(run "$TMP/real.db" --bogus | cut -d'|' -f1)" "2" "unknown flag exits 2"
check "$(run | cut -d'|' -f1)" "2" "no path exits 2"

before="$(md5sum < "$TMP/real.db")"
run "$TMP/real.db" >/dev/null
check "$(md5sum < "$TMP/real.db")" "$before" "the input database is not modified"

echo "PASS=$pass FAIL=$fail"
[ "$fail" -eq 0 ]
