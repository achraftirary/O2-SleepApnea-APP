-- Migration: fix rental duration trigger function to use integer date subtraction
CREATE OR REPLACE FUNCTION calculate_rental_duration()
RETURNS TRIGGER AS $$
BEGIN
    NEW.rental_duration_days := COALESCE(NEW.expected_return_date - NEW.rental_start_date, 0);
    IF NEW.actual_return_date IS NOT NULL THEN
        NEW.actual_rental_duration_days := COALESCE(NEW.actual_return_date - NEW.deployment_date, 0);
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Apply with:
-- PGPASSWORD=postgres psql -h localhost -U postgres -d o2_medical_db -f backend/db/migrations/fix_rental_duration_function.sql
