# SimpleFTPSync

[![Build](https://github.com/LaggingTheFirst/SimpleFTPSync/actions/workflows/build.yml/badge.svg)](https://github.com/LaggingTheFirst/SimpleFTPSync/actions/workflows/build.yml)

SimpleFTPSync is a community continuation of the original plugin for synchronizing local files or folders to remote FTP, FTPS, or SFTP servers.

Current public revival release: `1.2.0`

## Current state

This project was reconstructed from the released `SimpleFTPSync-1.1.jar` after the original repository disappeared.

The recovered source has already been cleaned up to:
- restore a normal Maven project structure
- support `sync-folders` as a proper YAML list
- keep compatibility with the older section-style `sync-folders` layout
- upload directories recursively instead of treating everything like a single file
- restart the scheduler when `/sfs reload` is used
- allow a manual sync run with `/sfs run`
- skip unchanged files between sync runs using a local state file
- support per-entry include and exclude patterns
- use configurable checksum-based change detection
- default SFTP behavior to strict known-host verification

## Commands

- `/sfs reload` reloads the config and restarts the scheduled sync task
- `/sfs run` starts an immediate asynchronous sync

## Step-by-step setup

1. Build or take the ready-made plugin jar and place it in your server's `plugins` folder.
2. Start the server once so SimpleFTPSync creates its config folder.
3. Stop the server and open `plugins/SimpleFTPSync/config.yml`.
4. Notice that the default config is intentionally inactive until you fill it in.
5. Set `sync-type` to `FTP`, `FTPS`, or `SFTP`.
6. Fill in the matching server section with your host, port, username, password, and base `remote-path`.
7. Add one or more entries under `sync-folders` for the files or folders you want to upload. Entry `remote-path` values may be relative to the selected protocol base path, which is usually the easiest setup.
8. Set `sync-interval` to the number of seconds between sync runs.
9. Leave `sync-only-changed: true` unless you intentionally want to re-upload everything every cycle.
10. Start the server again.
11. Run `/sfs run` in game or in the console to test immediately.
12. If you change the config later, run `/sfs reload` to apply it without restarting the server.

## SFTP host keys

For SFTP, strict host verification is enabled by default.

1. Connect to your SFTP server from a trusted machine and capture its host key into a `known_hosts` file.
2. Put that file at the path configured by `sftp.known-hosts-file`.
3. Keep `sftp.strict-host-key-checking: true`.

If you disable strict host key checking, the plugin will still work, but it is less secure because it will trust any host that answers at that address.

## Config

Use `src/main/resources/config.yml` as the template. The preferred structure is:

```yml
sync-interval: 300
sync-type: "SFTP"
sync-on-startup: false
sync-only-changed: true
change-detection: "CHECKSUM"

sftp:
  remote-path: "/backups"

sync-folders:
  - local-path: "world"
    remote-path: "world"
    changed-only: true
    exclude:
      - "**/session.lock"
  - local-path: "plugins/ExamplePlugin/data"
    remote-path: "example-plugin"
    include:
      - "**/*.yml"
```

`local-path` plus the selected protocol's `remote-path` is still supported as a legacy single-entry fallback.

If you prefer, `sync-folders[].remote-path` can still be a fully absolute remote destination.

## Build

This project uses Maven and targets Java 8 source compatibility.

The project metadata is prepared for Spigot `1.21.11-R0.2-SNAPSHOT`.
If you are building offline with `build.ps1`, it will use the newest cached Spigot API snapshot jar available in your local `.m2` repository.

Dependencies:
- Spigot API `1.21.11-R0.2-SNAPSHOT`
- `commons-net`
- `jsch`

Once Maven is installed, a typical build command is:

```bash
mvn package
```

## Local fallback build

This workspace also includes [build.ps1](C:\Users\temit\Desktop\SimpleFTPSync-1.1\build.ps1), which builds a fresh shaded jar without Maven by:
- compiling the recovered source against the locally cached Spigot API jar
- reusing the bundled FTP/SFTP libraries from the original `SimpleFTPSync-1.1.jar`
- packaging the result as `build/SimpleFTPSync-1.2.0.jar`

Run it with:

```powershell
.\build.ps1
```

## Publishing note

This repository starts with a recovered `1.1` snapshot commit and then continues with maintenance and feature work on top of that recovered base.

## Project links

- Changelog: [CHANGELOG.md](C:\Users\temit\Desktop\SimpleFTPSync-1.1\CHANGELOG.md)
- Contributing: [CONTRIBUTING.md](C:\Users\temit\Desktop\SimpleFTPSync-1.1\CONTRIBUTING.md)
- Release checklist: [RELEASING.md](C:\Users\temit\Desktop\SimpleFTPSync-1.1\RELEASING.md)
- Windows SFTP setup: [WINDOWS_SFTP_SETUP.md](C:\Users\temit\Desktop\SimpleFTPSync-1.1\WINDOWS_SFTP_SETUP.md)
