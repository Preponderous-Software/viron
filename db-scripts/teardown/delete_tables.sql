-- Copyright (c) 2024 Preponderous Software
-- MIT License

-- drop all tables
DROP TABLE IF EXISTS viron.entity CASCADE;
DROP TABLE IF EXISTS viron.location CASCADE;
DROP TABLE IF EXISTS viron.grid CASCADE;
DROP TABLE IF EXISTS viron.environment CASCADE;
DROP TABLE IF EXISTS viron.entity_location CASCADE;

DROP TABLE IF EXISTS viron.location_grid CASCADE;
DROP TABLE IF EXISTS viron.grid_environment CASCADE;

-- drop the id sequences created by setup/create_sequences.sql; they are not OWNED BY a
-- column, so DROP TABLE ... CASCADE leaves them behind and a re-run setup would keep counting
DROP SEQUENCE IF EXISTS viron.entity_id_seq;
DROP SEQUENCE IF EXISTS viron.environment_id_seq;
DROP SEQUENCE IF EXISTS viron.grid_id_seq;
DROP SEQUENCE IF EXISTS viron.location_id_seq;
