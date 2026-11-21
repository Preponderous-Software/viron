# Releasing Viron

Releases are made by the fleet's release automation.

## Versions and tags

- The version lives in `pom.xml` (the project `<version>`).
- Tags are the bare version, `X.Y.Z` (for example `0.5.0`), annotated, on a commit of `main` whose
  `pom.xml` carries that version.
- The automation opens and merges a pull request that sets the version, checks the merge commit
  (CI green, a dry run of `release.yml`, the database contract below), then pushes the tag.

## What a tag builds

`.github/workflows/release.yml` runs on a pushed `X.Y.Z` tag. It checks that the `pom.xml` version
equals the tag, builds and tests with JDK 21 (`./mvnw -B package`), and attaches to the GitHub
release `Viron X.Y.Z` (creating it if it does not exist yet):

- `viron-X.Y.Z.jar` and `viron-X.Y.Z.jar.sha256`

The `.sha256` is in `sha256sum` format (`<hex>  <filename>`); check a download with
`sha256sum -c viron-X.Y.Z.jar.sha256`.

Run it by hand (Actions, Release, Run workflow) with an existing tag to rebuild that release's
download, or with no tag for a dry run of the selected branch: everything is built and checked and
the jar is kept as a workflow artifact, but nothing is attached. Pull requests that change
`release.yml` or `pom.xml` run the dry run too.

## Database

The schema files are `db-scripts/setup/*.sql`; changes to an existing database are
`db-scripts/migrations/*.sql`. A released migration is never edited or deleted, and any change to
the setup scripts after a release ships with a new migration. See
[db-scripts/README.md](../db-scripts/README.md).
