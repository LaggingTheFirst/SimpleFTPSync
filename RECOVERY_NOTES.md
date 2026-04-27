# SimpleFTPSync recovery notes

This source tree was reconstructed from `SimpleFTPSync-1.1.jar`.

What was recovered:
- Original Maven coordinates and dependencies from the embedded `pom.xml`.
- Original `plugin.yml` and `config.yml`.
- Java source recreated from the compiled bytecode for the plugin package `org.cptgum.simpleftpsync`.

What to know before continuing development:
- The jar is a shaded build that bundles `commons-net` and `jsch`.
- The first maintenance pass has already fixed the config mismatch by supporting both a `sync-folders` list and the old section-style structure.
- The original "folder sync" logic only uploaded a single file path. The current source now supports recursive directory uploads for FTP, FTPS, and SFTP.
- `/sfs reload` now restarts the scheduled sync task so interval changes apply immediately.
- Maven is still needed on the machine before the project can be built locally from source.
