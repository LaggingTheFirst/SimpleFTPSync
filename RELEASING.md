# Releasing SimpleFTPSync

## Checklist

1. Update `CHANGELOG.md`.
2. Confirm `pom.xml` version is correct.
3. Run a local build:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\build.ps1
```

4. If Maven is available, also verify:

```bash
mvn package
```

5. Smoke-test on a server with at least one real sync target.
6. Commit release notes or version bumps.
7. Tag the release in git.
8. Push the tag to GitHub.
9. Create the GitHub release and attach the built jar if desired.

## Suggested first public revived release

- Version: `1.2.0`
- Title: `SimpleFTPSync 1.2.0 - community revival`

## Suggested release summary

- Recovered the original plugin source from the released jar.
- Restored a clean Maven project.
- Added recursive sync support.
- Added manual sync and reload support.
- Added changed-file-only sync and checksum detection.
- Added safer SFTP host verification.
