# Changelog

All notable changes to this project will be documented in this file.

## [Unreleased]

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
