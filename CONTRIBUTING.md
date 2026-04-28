# Contributing

Thanks for helping continue SimpleFTPSync.

## Development notes

- The original project was recovered from a released jar, so some early commits intentionally preserve original behavior before later cleanup.
- The main source lives in `src/main/java`.
- Plugin resources live in `src/main/resources`.
- `build.ps1` exists as a local fallback build path when Maven access is unreliable.

## Before opening a pull request

1. Keep changes focused.
2. Update docs when behavior changes.
3. Run a local build.
4. If you change sync behavior, include a short manual test note in the PR description.
