#!/usr/bin/env python3
"""Offline wire-level check over a pulled osmdroid tile cache (Phase 28, POL-03).

Usage:  python3 -I tools/verify-osm-tile-cache.py <cache.db> [--expect-placeholder]

A blocked OSM tile fetch caches a 6987-byte placeholder PNG. This reads a cache.db pulled off a
device and counts placeholder rows (size AND md5) and real tile rows (any other non-empty tile).

Exit codes:
  0  default: zero placeholder rows and at least one real tile row
     --expect-placeholder: at least one placeholder row (the control run over a broken baseline)
  1  the assertion failed
  2  usage error, missing file, unreadable database or missing tiles table

Read-only (mode=ro URI), parameterized statements, no network, no writes. The input is treated as
untrusted: a malformed file exits 2.
"""
import hashlib
import sqlite3
import sys
from pathlib import Path
from urllib.parse import quote

# A content fingerprint of a public placeholder image, not a security control.
PLACEHOLDER_SIZE = 6987
PLACEHOLDER_MD5 = "c069a15b2cc2d6b6f527ad09eb93c61a"


def usage(msg=None):
    if msg:
        print(msg, file=sys.stderr)
    print("usage: verify-osm-tile-cache.py <cache.db> [--expect-placeholder]", file=sys.stderr)
    return 2


def count_rows(path):
    uri = "file:" + quote(str(path.resolve())) + "?mode=ro"
    conn = sqlite3.connect(uri, uri=True)
    try:
        total = conn.execute("SELECT count(*) FROM tiles").fetchone()[0]
        placeholder = 0
        real = 0
        for (tile,) in conn.execute("SELECT tile FROM tiles WHERE tile IS NOT NULL AND length(tile) > 0"):
            blob = bytes(tile) if not isinstance(tile, bytes) else tile
            if len(blob) == PLACEHOLDER_SIZE and hashlib.md5(blob).hexdigest() == PLACEHOLDER_MD5:
                placeholder += 1
            else:
                real += 1
        return placeholder, real, total
    finally:
        conn.close()


def main(argv):
    expect_placeholder = False
    paths = []
    for arg in argv:
        if arg == "--expect-placeholder":
            expect_placeholder = True
        elif arg.startswith("-"):
            return usage("unknown option: " + arg)
        else:
            paths.append(arg)
    if len(paths) != 1:
        return usage("expected exactly one cache.db path")
    path = Path(paths[0])
    if not path.is_file():
        return usage("not a file: " + str(path))
    try:
        placeholder, real, total = count_rows(path)
    except (sqlite3.Error, OSError, ValueError) as exc:
        print("cannot read tiles table: %s" % exc, file=sys.stderr)
        return 2
    print("PLACEHOLDER_ROWS=%d REAL_ROWS=%d TOTAL_ROWS=%d" % (placeholder, real, total))
    if expect_placeholder:
        return 0 if placeholder >= 1 else 1
    return 0 if placeholder == 0 and real >= 1 else 1


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
