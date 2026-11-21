# Database scripts

Viron persists to Postgres. The schema comes from these scripts:

- `setup/create_sequences.sql`, `setup/create_tables.sql`: create the schema of a new database.
  Docker Compose mounts `setup/` as `/docker-entrypoint-initdb.d`, so they run only when Postgres
  initialises an empty volume.
- `migrations/*.sql`: bring an existing database, created by an earlier version's setup scripts,
  up to the current schema. They are run by hand, once per database, in file-name order (each file
  is named `YYYY-MM-DD_<what-it-does>.sql` and says in its header how to run it).
- `teardown/delete_tables.sql`: drops the tables and ID sequences.

## The contract

Databases made by a released version must keep working with every later version. The fleet's
release automation checks these two rules before it tags a release:

1. A migration file that has been released is never edited or deleted. A fix to a released
   migration is a new migration file.
2. Any change to `setup/*.sql` after a release ships with a new file in `migrations/` that makes
   the same change to an existing database, so that a database created from the old setup scripts
   plus every migration has the same schema as one created from the new setup scripts.

See `docs/RELEASING.md` for how releases are made.
