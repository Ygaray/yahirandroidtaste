# Hub additive guards

## Declaring non-additive (lane-2/3) changes

The pre-commit hook (`tools/hooks/pre-commit`) runs the lane classifier to detect whether a commit is additive (lane 1, fast path) or non-additive (lane 2: behavior change, lane 3: API break).

Lane 2/3 changes are blocked by default because they require coordination (hub mutex, semantic versioning bump). To land a deliberately non-additive change, re-run your commit with the `HUB_LANE_OVERRIDE` environment variable set to the detected lane:

```bash
HUB_LANE_OVERRIDE=2 git commit …   # Behavior change (source-only modification, API append-only)
HUB_LANE_OVERRIDE=3 git commit …   # API break (public symbol removed/renamed)
```

The hook will then allow the commit with the explicit declaration. This ensures that non-additive changes are intentional and coordinated, not accidental.

## API dump discipline

On every additive change, run `./gradlew apiDump` and commit the updated `$API_FILE` in the same commit — otherwise the `.api` is stale and the pre-commit guard sees no new symbols.

Before any release, run the full guard test suite:

```bash
bash tools/test/run-all.sh
```

This ensures all additive-detection guards pass (verify-api-additive, verify-additive-diff, classify-hub-change, and precommit-hook).

## Installation

Run `bash tools/hooks/install.sh` to symlink the pre-commit hook into your `.git/hooks/` directory.
