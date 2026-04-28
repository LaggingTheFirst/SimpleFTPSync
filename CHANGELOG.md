# Changelog

All notable changes to this project will be documented in this file.

## [Unreleased]

### Fixed
- Updated `/sfs run` to use the Paper-compatible async scheduling path on modern servers.
- Fixed `**/session.lock` excludes so they also match a root-level `session.lock`.

### Changed
- Made the default config inactive until real sync settings are entered.
- Added sync target preflight validation to avoid placeholder-host error spam on fresh installs.
- Added easier relative `sync-folders[].remote-path` support so entries can live under the selected protocol base `remote-path`.
- Added a dedicated Windows SFTP setup guide for the simpler `C:\MinecraftSync` layout.

## [1.2.0] - 2026-04-28

### Added
- Public repository scaffolding for the revived project.
- GitHub Actions build workflow.
- Offline local build script for environments where Maven dependency access is unreliable.
- Step-by-step setup and SFTP host key guidance in the README.

### Changed
- Rebuilt the original `1.1` release into a maintainable source tree.
- Updated project metadata toward modern Spigot `1.21.11` compatibility.
- Improved sync behavior with:
  - recursive directory uploads
  - manual `/sfs run`
  - reloadable scheduler
  - changed-file-only sync
  - checksum-based change detection
  - include/exclude path patterns
  - stricter SFTP host verification

## [1.1] - Recovered

### Notes
- This tag represents the recovered upstream source snapshot reconstructed from the released `SimpleFTPSync-1.1.jar`.
