#!/usr/bin/env python3
"""Source-additivity review of a Metalava api.txt against a baseline api.txt (API-03).

Driven by tools/verify-api-source-additive.sh; run with `python3 -I` (standard library only).

usage: verify-api-source-additive.py <baseline-api.txt> <head-api.txt> <baseline-label>

Rules (see the shell wrapper for the rationale):
  R1  every public/protected class of the baseline exists at head
  R2  every (class, kind, member name) triple of the baseline exists at head
      (kind is ctor, method, property or field; a constructor's name is the class simple name)
  R3  every baseline enum constant (class, constant name) exists at head
  R4  for every baseline method or constructor signature, head has a signature with the same
      (class, kind, name) and an identical ORDERED list of REQUIRED (non-optional) parameter types

Exit codes: 0 pass, 1 rule violated, 2 usage or tooling error.
"""
import re
import sys

MEMBER_KINDS = ("ctor", "method", "property", "field", "enum_constant")
# Class headers may carry leading annotations, e.g. `@androidx.compose.runtime.Immutable public final class X {`.
HEADER_RE = re.compile(
    r"^(?:@[\w.$]+(?:\([^)]*\))?\s+)*(?:public|protected)\b.*?\b(class|interface|enum|@interface)\s+([\w.$]+)"
)


def strip_annotations(text):
    """Remove `@Name` and `@Name(...)` (balanced parentheses) from a signature fragment."""
    out = []
    i = 0
    n = len(text)
    while i < n:
        if text[i] == "@":
            j = i + 1
            while j < n and (text[j].isalnum() or text[j] in "._$"):
                j += 1
            if j < n and text[j] == "(":
                depth = 0
                while j < n:
                    if text[j] == "(":
                        depth += 1
                    elif text[j] == ")":
                        depth -= 1
                        if depth == 0:
                            j += 1
                            break
                    j += 1
            i = j
            continue
        out.append(text[i])
        i += 1
    return re.sub(r"\s+", " ", "".join(out)).strip()


def split_top_level(text):
    """Split a parameter list at top-level commas (ignoring commas inside <> and ())."""
    parts = []
    cur = []
    depth = 0
    for ch in text:
        if ch in "<(":
            depth += 1
        elif ch in ">)":
            depth -= 1
        if ch == "," and depth == 0:
            parts.append("".join(cur))
            cur = []
        else:
            cur.append(ch)
    parts.append("".join(cur))
    return [p.strip() for p in parts if p.strip()]


def parse_signature(rest):
    """Return (name, required_types) for the text after `ctor` or `method`."""
    rest = rest.strip()
    if rest.endswith(";"):
        rest = rest[:-1].rstrip()
    close = rest.rfind(")")
    if close < 0:
        raise ValueError("no parameter list in: " + rest)
    depth = 0
    open_at = -1
    for i in range(close, -1, -1):
        if rest[i] == ")":
            depth += 1
        elif rest[i] == "(":
            depth -= 1
            if depth == 0:
                open_at = i
                break
    if open_at < 0:
        raise ValueError("unbalanced parameter list in: " + rest)
    head = strip_annotations(rest[:open_at])
    name = head.split(" ")[-1] if head else ""
    required = []
    for raw in split_top_level(rest[open_at + 1:close]):
        param = strip_annotations(raw)
        if param.startswith("optional "):
            continue
        required.append(param.rsplit(" ", 1)[0] if " " in param else param)
    return name, tuple(required)


def simple_name(class_name):
    return class_name.split(".")[-1]


def parse_api(path):
    classes = set()
    triples = set()
    signatures = {}
    enum_constants = set()
    package = ""
    current = None
    with open(path, encoding="utf-8") as handle:
        for number, line in enumerate(handle, 1):
            text = line.strip()
            if not text or text.startswith("//"):
                continue
            if text.startswith("package ") and text.endswith("{"):
                package = text[len("package "):-1].strip()
                current = None
                continue
            if text == "}":
                if current is not None and not line.startswith("    "):
                    current = None
                continue
            kind = text.split(" ", 1)[0]
            if kind in MEMBER_KINDS and current is not None:
                rest = text[len(kind):].strip()
                try:
                    if kind in ("ctor", "method"):
                        name, required = parse_signature(rest)
                        if kind == "ctor":
                            name = simple_name(current)
                        triples.add((current, kind, name))
                        signatures.setdefault((current, kind, name), set()).add(required)
                    else:
                        body = rest.rstrip(";").split(" = ", 1)[0].strip()
                        name = strip_annotations(body).split(" ")[-1]
                        if kind == "enum_constant":
                            enum_constants.add((current, name))
                        else:
                            triples.add((current, kind, name))
                except ValueError as err:
                    sys.stderr.write("API-ADDITIVE ERROR: %s:%d: %s\n" % (path, number, err))
                    sys.exit(2)
                continue
            match = HEADER_RE.match(text)
            if match and text.endswith("{"):
                current = package + "." + match.group(2) if package else match.group(2)
                classes.add(current)
            elif text.endswith("{"):
                # A block opener that is neither a package nor a member nor a recognised class header:
                # silently skipping it would attribute the next members to the wrong class.
                sys.stderr.write("API-ADDITIVE ERROR: %s:%d: unrecognised class header: %s\n" % (path, number, text))
                sys.exit(2)
    return classes, triples, signatures, enum_constants


def main(argv):
    if len(argv) != 4:
        sys.stderr.write("usage: verify-api-source-additive.py <baseline> <head> <label>\n")
        return 2
    try:
        base = parse_api(argv[1])
        head = parse_api(argv[2])
    except OSError as err:
        sys.stderr.write("API-ADDITIVE ERROR: cannot read api file: %s\n" % err)
        return 2
    label = argv[3]
    base_classes, base_triples, base_sigs, base_enums = base
    head_classes, head_triples, head_sigs, head_enums = head
    if not base_classes or not head_classes:
        sys.stderr.write("API-ADDITIVE ERROR: an api file holds no classes (wrong file?)\n")
        return 2

    failures = []
    removed_classes = 0
    removed_members = 0
    required_changes = 0

    for cls in sorted(base_classes - head_classes):
        failures.append("FAIL R1: class removed: %s" % cls)
        removed_classes += 1
    for cls, kind, name in sorted(base_triples - head_triples):
        if cls not in head_classes:
            continue  # already reported once as a removed class
        failures.append("FAIL R2: member removed: %s %s %s" % (cls, kind, name))
        removed_members += 1
    for cls, name in sorted(base_enums - head_enums):
        if cls not in head_classes:
            continue
        failures.append("FAIL R3: enum constant removed: %s.%s" % (cls, name))
        removed_members += 1
    for triple in sorted(base_sigs):
        if triple not in head_sigs:
            continue  # the whole name is gone: R2 already says so
        for required in sorted(base_sigs[triple] - head_sigs[triple]):
            failures.append(
                "FAIL R4: no head signature of %s %s %s has required parameters (%s); head offers: %s"
                % (
                    triple[0],
                    triple[1],
                    triple[2],
                    ", ".join(required),
                    " | ".join("(" + ", ".join(sig) + ")" for sig in sorted(head_sigs[triple])),
                )
            )
            required_changes += 1

    if failures:
        for failure in failures:
            print(failure)
        print(
            "API SOURCE-ADDITIVE FAILED baseline=%s removed_classes=%d removed_members=%d required_param_changes=%d"
            % (label, removed_classes, removed_members, required_changes)
        )
        return 1

    print(
        "API SOURCE-ADDITIVE OK baseline=%s removed_classes=0 removed_members=0 enum_constants_intact=%d "
        "required_param_changes=0 added_classes=%d added_members=%d"
        % (
            label,
            len(base_enums),
            len(head_classes - base_classes),
            len(head_triples - base_triples) + len(head_enums - base_enums),
        )
    )
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv))
