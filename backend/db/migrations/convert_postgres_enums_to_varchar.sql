-- Migration: convert Postgres enum columns to varchar
-- Converts all enum-backed columns used by the application to varchar
BEGIN;

ALTER TABLE users ALTER COLUMN role TYPE varchar USING role::text;

ALTER TABLE devices ALTER COLUMN device_status TYPE varchar USING device_status::text;
ALTER TABLE devices ALTER COLUMN device_type TYPE varchar USING device_type::text;

ALTER TABLE rental_contracts ALTER COLUMN contract_status TYPE varchar USING contract_status::text;

ALTER TABLE invoices ALTER COLUMN payment_status TYPE varchar USING payment_status::text;

ALTER TABLE device_maintenance_logs ALTER COLUMN maintenance_priority TYPE varchar USING maintenance_priority::text;

COMMIT;

-- Note: run this with psql as the DB user (see application.properties) e.g.:
-- PGPASSWORD=postgres psql -h localhost -U postgres -d o2_medical_db -f ./db/migrations/convert_postgres_enums_to_varchar.sql
