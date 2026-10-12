#!/usr/bin/env python3
"""Source-additivity review of a Metalava api.txt against a baseline api.txt (API-03).

Driven by tools/verify-api-source-additive.sh; run with `python3 -I` (standard library only).

usage: verify-api-source-additive.py <baseline-api.txt> <head-api.txt> <baseline-label>

Rules (see the shell wrapper for the rationale and the limits):
  R1  every public/protected class of the baseline exists at head
  R2  every (class, kind, member name) triple of the baseline exists at head
      (kind is ctor, method, property or field; a constructor's name is the class simple name)
  R3  every baseline enum constant (class, constant name) exists at head
  R4  for every baseline method or constructor signature, head has a signature with the same
      (class, kind, name) and an identical ORDERED list of REQUIRED (non-optional) parameters
      (type and name)
  R5  ... whose full parameter list (type and name, optional or not) starts with the baseline's
      parameter list, so no parameter is inserted before an existing one (a positional call keeps binding)
  R6  ... and whose return type is unchanged; a baseline property or field keeps its type
  R7  ... and, when the baseline's last parameter is a function type (a trailing lambda), adds no
      parameter after it
  R8  no enum constant is added to a baseline enum (it breaks an exhaustive `when`)

Reviewed exceptions: ACCEPTED_EXCEPTIONS below waives named rules for exact signatures, only for the
baseline label it is keyed on. Every waiver is printed as an ACCEPTED line.

Exit codes: 0 pass, 1 rule violated, 2 usage or tooling error.
"""
import re
import sys

MEMBER_KINDS = ("ctor", "method", "property", "field", "enum_constant")
# Class headers may carry leading annotations, e.g. `@androidx.compose.runtime.Immutable public final class X {`.
HEADER_RE = re.compile(
    r"^(?:@[\w.$]+(?:\([^)]*\))?\s+)*(?:public|protected)\b.*?\b(class|interface|enum|@interface)\s+([\w.$]+)"
)


# Reviewed, documented source breaks of v2.6.0 (API.md section 11 and "Adding / changing components",
# INTEGRATION.md 5b). Keyed by baseline label, then (class, kind, member name) -> (waived rules, why).
# An entry here means "the owner accepted this break and the docs say so"; add nothing without both.
_COMPONENT = "io.github.ygaray.yahirandroidtaste.component"
_FEEDBACK = "io.github.ygaray.yahirandroidtaste.feedback"
_NEEDS = "io.github.ygaray.yahirandroidtaste.model.VoiceOutcomeUiState.NeedsConfirmation"
ACCEPTED_EXCEPTIONS = {
    "v2.5.1": {
        (_COMPONENT + ".SheetScaffoldKt", "method", "SheetScaffold"): (
            {"R5"}, "shape inserted before the trailing content lambda; positional content breaks (API.md)"),
        (_FEEDBACK + ".UndoCenterScreenKt", "method", "UndoCenterScreen"): (
            {"R5"}, "groups and onUndoGroup inserted before onClear; four positional arguments break (API.md)"),
        (_NEEDS, "ctor", "NeedsConfirmation"): (
            {"R5"}, "confirmEnabled and titleActions inserted before onConfirm and onCancel (API.md)"),
        (_NEEDS, "method", "copy"): (
            {"R5"}, "data-class copy of the same inserted fields (API.md)"),
        (_NEEDS, "method", "component10"): (
            {"R6"}, "destructuring shifts after the inserted fields (API.md)"),
        (_NEEDS, "method", "component11"): (
            {"R6"}, "destructuring shifts after the inserted fields (API.md)"),
    },
}

MODIFIERS = frozenset({
    "public", "protected", "static", "final", "abstract", "suspend", "inline", "default",
    "synchronized", "native", "open", "operator", "infix", "override", "internal",
})
NAME_RE = re.compile(r"[A-Za-z_$][\w$]*")


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


def split_type_name(text):
    """Split `type name` at the last top-level space (outside <>). An unnamed parameter, such as the
    synthetic `Continuation<? super X>` of a suspend function or an extension receiver, has no name."""
    depth = 0
    for i in range(len(text) - 1, -1, -1):
        ch = text[i]
        if ch == ">":
            depth += 1
        elif ch == "<":
            depth -= 1
        elif ch == " " and depth == 0:
            tail = text[i + 1:]
            if NAME_RE.fullmatch(tail):
                return text[:i].strip(), tail
            return text, ""
    return text, ""


def strip_modifiers(text):
    """Drop the leading modifier keywords (public static final ...) and return the rest."""
    tokens = text.split(" ")
    while tokens and tokens[0] in MODIFIERS:
        tokens.pop(0)
    return " ".join(tokens)


def parse_signature(rest):
    """Return (name, return_type, params) for the text after `ctor` or `method`.

    params is an ordered tuple of (optional, type, name); name is "" for an unnamed parameter."""
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
    return_type, name = split_type_name(strip_modifiers(head)) if head else ("", "")
    if not name:
        # a constructor head is `public Name`: nothing is left to hold a return type
        name, return_type = return_type, ""
    params = []
    for raw in split_top_level(rest[open_at + 1:close]):
        param = strip_annotations(raw)
        optional = param.startswith("optional ")
        if optional:
            param = param[len("optional "):]
        ptype, pname = split_type_name(param)
        params.append((optional, ptype, pname))
    return name, return_type, tuple(params)


def parse_typed_member(rest):
    """Return (name, type) for the text after `property` or `field` (modifiers and `= value` dropped)."""
    body = strip_annotations(rest.rstrip(";").split(" = ", 1)[0].strip())
    mtype, name = split_type_name(strip_modifiers(body))
    return name, mtype


def simple_name(class_name):
    return class_name.split(".")[-1]


def parse_api(path):
    """Return (classes, triples, signatures, enum_constants, member_types).

    signatures maps (class, kind, name) to a list of (return_type, params); member_types maps
    (class, kind, name) to the set of declared types of a property or field."""
    classes = set()
    triples = set()
    signatures = {}
    enum_constants = set()
    member_types = {}
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
                        name, return_type, params = parse_signature(rest)
                        if kind == "ctor":
                            name = simple_name(current)
                        triples.add((current, kind, name))
                        signatures.setdefault((current, kind, name), []).append((return_type, params))
                    else:
                        name, member_type = parse_typed_member(rest)
                        if kind == "enum_constant":
                            enum_constants.add((current, name))
                        else:
                            triples.add((current, kind, name))
                            member_types.setdefault((current, kind, name), set()).add(member_type)
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
    return classes, triples, signatures, enum_constants, member_types


def render(params):
    return "(" + ", ".join(("optional " if o else "") + (t + (" " + n if n else "")) for o, t, n in params) + ")"


def is_function_type(type_name):
    return type_name.startswith("kotlin.jvm.functions.Function") or type_name.startswith("kotlin.Function")


def review_steps(base_sig, head_sigs):
    """Return the ordered (rule, predicate, message) checks for one baseline signature.

    Each predicate selects the head signatures that satisfy the rule; a rule fails when none does."""
    base_return, base_params = base_sig
    offers = " | ".join(sorted(render(params) for _, params in head_sigs))
    base_full = [(t, n) for _, t, n in base_params]
    base_required = [(t, n) for o, t, n in base_params if not o]
    steps = [
        ("R4", lambda sig: [(t, n) for o, t, n in sig[1] if not o] == base_required,
         "no head signature has the baseline's required parameters (%s); head offers: %s"
         % (", ".join(("%s %s" % (t, n)).strip() for t, n in base_required), offers)),
        ("R5", lambda sig: [(t, n) for _, t, n in sig[1]][:len(base_full)] == base_full,
         "baseline parameters %s are not a positional prefix of any head signature; head offers: %s"
         % (render(base_params), offers)),
        ("R6", lambda sig: sig[0] == base_return,
         "return type %r changed; head returns: %s" % (base_return, " | ".join(sorted({r for r, _ in head_sigs})))),
    ]
    if base_params and is_function_type(base_params[-1][1]):
        steps.append(
            ("R7", lambda sig: len(sig[1]) == len(base_params),
             "a parameter is added after the trailing lambda %s; head offers: %s"
             % (base_params[-1][2] or base_params[-1][1], offers)))
    return steps


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
    base_classes, base_triples, base_sigs, base_enums, base_types = base
    head_classes, head_triples, head_sigs, head_enums, head_types = head
    if not base_classes or not head_classes:
        sys.stderr.write("API-ADDITIVE ERROR: an api file holds no classes (wrong file?)\n")
        return 2

    exceptions = ACCEPTED_EXCEPTIONS.get(label, {})
    failures = []
    accepted = []
    removed_classes = 0
    removed_members = 0
    signature_changes = 0

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
        waived, why = exceptions.get(triple, (set(), ""))
        for base_sig in sorted(base_sigs[triple], key=lambda sig: render(sig[1])):
            candidates = head_sigs[triple]
            for rule, predicate, message in review_steps(base_sig, candidates):
                survivors = [sig for sig in candidates if predicate(sig)]
                if survivors:
                    candidates = survivors
                    continue
                if rule == "R7" and "R5" in waived:
                    continue  # R7 reads the prefix-matched candidates; an accepted R5 break leaves none
                if rule in waived:
                    # reviewed exception: keep checking the later rules against the unnarrowed candidates
                    accepted.append("ACCEPTED %s: %s %s %s (%s)" % (rule, triple[0], triple[1], triple[2], why))
                    continue
                failures.append("FAIL %s: %s %s %s: %s" % (rule, triple[0], triple[1], triple[2], message))
                signature_changes += 1
                break
    for triple in sorted(base_types):
        if triple not in head_types:
            continue
        waived, why = exceptions.get(triple, (set(), ""))
        for member_type in sorted(base_types[triple] - head_types[triple]):
            if "R6" in waived:
                accepted.append("ACCEPTED R6: %s %s %s (%s)" % (triple[0], triple[1], triple[2], why))
            else:
                failures.append("FAIL R6: %s %s %s: type %r changed; head declares: %s" % (
                    triple[0], triple[1], triple[2], member_type, " | ".join(sorted(head_types[triple]))))
                signature_changes += 1
    base_enum_classes = {cls for cls, _ in base_enums}
    for cls, name in sorted(head_enums - base_enums):
        if cls in base_enum_classes:
            failures.append("FAIL R8: enum constant added to an existing enum (breaks exhaustive when): %s.%s" % (cls, name))
            signature_changes += 1

    for line in accepted:
        print(line)
    if failures:
        for failure in failures:
            print(failure)
        print(
            "API SOURCE-ADDITIVE FAILED baseline=%s removed_classes=%d removed_members=%d signature_changes=%d "
            "accepted_exceptions=%d" % (label, removed_classes, removed_members, signature_changes, len(accepted))
        )
        return 1

    print(
        "API SOURCE-ADDITIVE OK baseline=%s removed_classes=0 removed_members=0 enum_constants_intact=%d "
        "required_param_changes=0 added_classes=%d added_members=%d signature_changes=0 accepted_exceptions=%d"
        % (
            label,
            len(base_enums),
            len(head_classes - base_classes),
            len(head_triples - base_triples) + len(head_enums - base_enums),
            len(accepted),
        )
    )
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv))
