-- Migration: convert varchar columns back to Postgres enum types
BEGIN;

DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'user_role') THEN
    CREATE TYPE user_role AS ENUM ('AGENT','DOCTOR');
  END IF;
END
$$;

DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'device_status') THEN
    CREATE TYPE device_status AS ENUM ('AVAILABLE','DEPLOYED','IN_MAINTENANCE','RETIRED');
  END IF;
END
$$;

DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'device_type') THEN
    CREATE TYPE device_type AS ENUM (
      'OXYGEN_CONCENTRATOR_5L',
      'OXYGEN_CONCENTRATOR_10L',
      'OXYGEN_CONCENTRATOR_15L',
      'SLEEP_APNEA_DEVICE',
      'OXYGEN_MASK'
    );
  END IF;
END
$$;

DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'rental_contract_status') THEN
    CREATE TYPE rental_contract_status AS ENUM ('PENDING_DEPLOYMENT','ACTIVE_RENTAL','PENDING_PICKUP','COMPLETED','CANCELLED');
  END IF;
END
$$;

DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'payment_status') THEN
    CREATE TYPE payment_status AS ENUM ('UNPAID','PARTIAL','PAID');
  END IF;
END
$$;

DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'maintenance_priority') THEN
    CREATE TYPE maintenance_priority AS ENUM ('LOW','MEDIUM','HIGH','CRITICAL');
  END IF;
END
$$;

ALTER TABLE users ALTER COLUMN role TYPE user_role USING role::user_role;

ALTER TABLE devices ALTER COLUMN device_status TYPE device_status USING device_status::device_status;
ALTER TABLE devices ALTER COLUMN device_type TYPE device_type USING device_type::device_type;

ALTER TABLE rental_contracts ALTER COLUMN contract_status TYPE rental_contract_status USING contract_status::rental_contract_status;

ALTER TABLE invoices ALTER COLUMN payment_status TYPE payment_status USING payment_status::payment_status;

ALTER TABLE device_maintenance_logs ALTER COLUMN maintenance_priority TYPE maintenance_priority USING maintenance_priority::maintenance_priority;

COMMIT;

-- Run with:
-- PGPASSWORD=postgres psql -h localhost -U postgres -d o2_medical_db -f backend/db/migrations/convert_varchar_to_postgres_enums.sql
