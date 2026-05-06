-- =====================================================================
-- O2 Medical Equipment Rental Management System
-- PostgreSQL Database Schema
-- =====================================================================

-- =====================================================================
-- ENUMS (Custom Types)
-- =====================================================================

CREATE TYPE user_role AS ENUM ('AGENT', 'DOCTOR');

CREATE TYPE device_type AS ENUM (
    'OXYGEN_CONCENTRATOR_5L',
    'OXYGEN_CONCENTRATOR_10L',
    'OXYGEN_CONCENTRATOR_15L',
    'SLEEP_APNEA_DEVICE',
    'OXYGEN_MASK'
);

CREATE TYPE device_status AS ENUM (
    'AVAILABLE',
    'DEPLOYED',
    'IN_MAINTENANCE',
    'RETIRED'
);

CREATE TYPE rental_contract_status AS ENUM (
    'PENDING_DEPLOYMENT',
    'ACTIVE_RENTAL',
    'PENDING_PICKUP',
    'COMPLETED',
    'CANCELLED'
);

CREATE TYPE payment_status AS ENUM (
    'UNPAID',
    'PARTIAL',
    'PAID'
);

CREATE TYPE maintenance_priority AS ENUM (
    'LOW',
    'MEDIUM',
    'HIGH',
    'CRITICAL'
);

-- =====================================================================
-- USERS & AUTHENTICATION
-- =====================================================================

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(100) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    role user_role NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================================
-- INVENTORY MANAGEMENT
-- =====================================================================

CREATE TABLE devices (
    id BIGSERIAL PRIMARY KEY,
    device_type device_type NOT NULL,
    serial_number VARCHAR(100) UNIQUE NOT NULL,
    manufacturer VARCHAR(100),
    model VARCHAR(100),
    device_status device_status DEFAULT 'AVAILABLE',
    purchase_date DATE,
    purchase_price DECIMAL(10, 2),
    acquisition_date DATE NOT NULL,
    decommission_date DATE,
    -- For consumables (masks): quantity instead of serial tracking
    quantity_in_stock INT DEFAULT 1,
    is_consumable BOOLEAN DEFAULT FALSE,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_devices_serial ON devices(serial_number);
CREATE INDEX idx_devices_status ON devices(device_status);
CREATE INDEX idx_devices_type ON devices(device_type);

-- Maintenance history for devices
CREATE TABLE device_maintenance_logs (
    id BIGSERIAL PRIMARY KEY,
    device_id BIGINT NOT NULL REFERENCES devices(id) ON DELETE CASCADE,
    maintenance_type VARCHAR(100) NOT NULL,
    description TEXT,
    start_date DATE NOT NULL,
    end_date DATE,
    maintenance_priority maintenance_priority DEFAULT 'MEDIUM',
    cost DECIMAL(10, 2),
    technician_name VARCHAR(100),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_maintenance_device ON device_maintenance_logs(device_id);
CREATE INDEX idx_maintenance_priority ON device_maintenance_logs(maintenance_priority);

-- Device usage tracking for maintenance scheduling
CREATE TABLE device_usage_hours (
    id BIGSERIAL PRIMARY KEY,
    device_id BIGINT NOT NULL REFERENCES devices(id) ON DELETE CASCADE,
    total_rental_hours INT DEFAULT 0,
    total_rental_days INT DEFAULT 0,
    last_maintenance_hours INT DEFAULT 0,
    last_maintenance_days INT DEFAULT 0,
    threshold_maintenance_hours INT DEFAULT 500,
    threshold_maintenance_days INT DEFAULT 180,
    requires_maintenance BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_usage_device ON device_usage_hours(device_id);

-- =====================================================================
-- CLIENTS & PATIENTS
-- =====================================================================

CREATE TABLE clients (
    id BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    date_of_birth DATE,
    gender VARCHAR(20),
    phone VARCHAR(20) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE,
    -- Address for deployment
    street_address VARCHAR(255) NOT NULL,
    city VARCHAR(100) NOT NULL,
    postal_code VARCHAR(20) NOT NULL,
    country VARCHAR(100) DEFAULT 'France',
    -- Medical assignment
    assigned_doctor_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    medical_history TEXT,
    allergies TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_clients_phone ON clients(phone);
CREATE INDEX idx_clients_email ON clients(email);
CREATE INDEX idx_clients_doctor ON clients(assigned_doctor_id);

-- Medical documents upload (sleep apnea results, prescriptions, etc.)
CREATE TABLE medical_documents (
    id BIGSERIAL PRIMARY KEY,
    client_id BIGINT NOT NULL REFERENCES clients(id) ON DELETE CASCADE,
    uploaded_by_user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    document_type VARCHAR(100) NOT NULL, -- e.g., 'SLEEP_APNEA_RESULT', 'PRESCRIPTION'
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_size INT,
    mime_type VARCHAR(100),
    description TEXT,
    upload_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_medical_docs_client ON medical_documents(client_id);
CREATE INDEX idx_medical_docs_type ON medical_documents(document_type);

-- =====================================================================
-- RENTAL CONTRACTS & LOGISTICS
-- =====================================================================

CREATE TABLE rental_contracts (
    id BIGSERIAL PRIMARY KEY,
    contract_number VARCHAR(100) UNIQUE NOT NULL, -- e.g., RC-2024-001
    client_id BIGINT NOT NULL REFERENCES clients(id) ON DELETE RESTRICT,
    device_id BIGINT NOT NULL REFERENCES devices(id) ON DELETE RESTRICT,
    assigned_agent_id BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    contract_status rental_contract_status DEFAULT 'PENDING_DEPLOYMENT',
    -- Core dates
    rental_start_date DATE NOT NULL,
    expected_return_date DATE NOT NULL,
    actual_return_date DATE,
    -- Field operations
    deployment_date DATE,
    deployment_notes TEXT,
    pickup_date DATE,
    pickup_notes TEXT,
    -- Duration tracking (auto-calculated)
    rental_duration_days INT,
    actual_rental_duration_days INT,
    -- Financial tracking
    daily_rental_rate DECIMAL(10, 2) NOT NULL,
    total_rental_cost DECIMAL(10, 2),
    deposit_amount DECIMAL(10, 2) DEFAULT 0,
    additional_fees DECIMAL(10, 2) DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_rental_client ON rental_contracts(client_id);
CREATE INDEX idx_rental_device ON rental_contracts(device_id);
CREATE INDEX idx_rental_status ON rental_contracts(contract_status);
CREATE INDEX idx_rental_dates ON rental_contracts(rental_start_date, expected_return_date);

-- =====================================================================
-- PAYMENTS & INVOICING
-- =====================================================================

CREATE TABLE invoices (
    id BIGSERIAL PRIMARY KEY,
    invoice_number VARCHAR(100) UNIQUE NOT NULL, -- e.g., INV-2024-001
    rental_contract_id BIGINT NOT NULL REFERENCES rental_contracts(id) ON DELETE RESTRICT,
    client_id BIGINT NOT NULL REFERENCES clients(id) ON DELETE RESTRICT,
    invoice_date DATE NOT NULL,
    due_date DATE NOT NULL,
    payment_status payment_status DEFAULT 'UNPAID',
    -- Amount breakdown
    subtotal DECIMAL(10, 2) NOT NULL,
    tax_amount DECIMAL(10, 2) DEFAULT 0,
    discount_amount DECIMAL(10, 2) DEFAULT 0,
    total_amount DECIMAL(10, 2) NOT NULL,
    -- Balance tracking
    paid_amount DECIMAL(10, 2) DEFAULT 0,
    balance_due DECIMAL(10, 2),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_invoices_contract ON invoices(rental_contract_id);
CREATE INDEX idx_invoices_client ON invoices(client_id);
CREATE INDEX idx_invoices_status ON invoices(payment_status);
CREATE INDEX idx_invoices_dates ON invoices(invoice_date, due_date);

-- Payment records
CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    invoice_id BIGINT NOT NULL REFERENCES invoices(id) ON DELETE RESTRICT,
    payment_amount DECIMAL(10, 2) NOT NULL,
    payment_date DATE NOT NULL,
    payment_method VARCHAR(50) NOT NULL, -- e.g., 'CASH', 'BANK_TRANSFER', 'CHECK'
    transaction_reference VARCHAR(100),
    notes TEXT,
    recorded_by_user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_payments_invoice ON payments(invoice_id);
CREATE INDEX idx_payments_date ON payments(payment_date);

-- =====================================================================
-- ALERTS & NOTIFICATIONS
-- =====================================================================

CREATE TABLE alerts (
    id BIGSERIAL PRIMARY KEY,
    alert_type VARCHAR(100) NOT NULL, -- e.g., 'OVERDUE_PICKUP', 'UNPAID_INVOICE', 'LOW_STOCK', 'MAINTENANCE_DUE'
    severity VARCHAR(50) NOT NULL, -- 'INFO', 'WARNING', 'CRITICAL'
    related_entity_type VARCHAR(50), -- 'RENTAL_CONTRACT', 'INVOICE', 'DEVICE', 'CLIENT'
    related_entity_id BIGINT,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    is_resolved BOOLEAN DEFAULT FALSE,
    resolved_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_alerts_type ON alerts(alert_type);
CREATE INDEX idx_alerts_severity ON alerts(severity);
CREATE INDEX idx_alerts_resolved ON alerts(is_resolved);
CREATE INDEX idx_alerts_entity ON alerts(related_entity_type, related_entity_id);

-- =====================================================================
-- ANALYTICS & REPORTING
-- =====================================================================

-- Daily revenue snapshot for dashboard
CREATE TABLE daily_revenue_snapshot (
    id BIGSERIAL PRIMARY KEY,
    snapshot_date DATE UNIQUE NOT NULL,
    total_rentals_active INT DEFAULT 0,
    total_revenue_expected DECIMAL(15, 2) DEFAULT 0,
    total_revenue_paid DECIMAL(15, 2) DEFAULT 0,
    total_unpaid DECIMAL(15, 2) DEFAULT 0,
    total_overdue_pickups INT DEFAULT 0,
    low_stock_alerts INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_revenue_date ON daily_revenue_snapshot(snapshot_date);

-- Monthly aggregated metrics
CREATE TABLE monthly_metrics (
    id BIGSERIAL PRIMARY KEY,
    metric_month DATE NOT NULL, -- First day of the month
    total_contracts INT DEFAULT 0,
    total_revenue DECIMAL(15, 2) DEFAULT 0,
    total_paid DECIMAL(15, 2) DEFAULT 0,
    average_contract_value DECIMAL(10, 2),
    device_utilization_rate DECIMAL(5, 2), -- percentage
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_metrics_month ON monthly_metrics(metric_month);

-- =====================================================================
-- AUDIT LOGS
-- =====================================================================

CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(100) NOT NULL,
    entity_id BIGINT,
    old_values JSONB,
    new_values JSONB,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_audit_user ON audit_logs(user_id);
CREATE INDEX idx_audit_entity ON audit_logs(entity_type, entity_id);
CREATE INDEX idx_audit_timestamp ON audit_logs(timestamp);

-- =====================================================================
-- STORED PROCEDURES & FUNCTIONS
-- =====================================================================

-- Calculate rental duration in days
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

CREATE TRIGGER trigger_calculate_rental_duration
BEFORE INSERT OR UPDATE ON rental_contracts
FOR EACH ROW
EXECUTE FUNCTION calculate_rental_duration();

-- Auto-calculate invoice balance
CREATE OR REPLACE FUNCTION calculate_invoice_balance()
RETURNS TRIGGER AS $$
BEGIN
    NEW.balance_due := NEW.total_amount - NEW.paid_amount;
    IF NEW.balance_due <= 0 THEN
        NEW.payment_status := 'PAID';
    ELSIF NEW.paid_amount > 0 THEN
        NEW.payment_status := 'PARTIAL';
    ELSE
        NEW.payment_status := 'UNPAID';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_calculate_invoice_balance
BEFORE INSERT OR UPDATE ON invoices
FOR EACH ROW
EXECUTE FUNCTION calculate_invoice_balance();

-- Update device usage hours when rental completes
CREATE OR REPLACE FUNCTION update_device_usage_hours()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.contract_status = 'COMPLETED' AND OLD.contract_status != 'COMPLETED' THEN
        UPDATE device_usage_hours
        SET 
            total_rental_days = total_rental_days + COALESCE(NEW.actual_rental_duration_days, 0),
            total_rental_hours = total_rental_hours + (COALESCE(NEW.actual_rental_duration_days, 0) * 24),
            requires_maintenance = CASE 
                WHEN (total_rental_hours + (COALESCE(NEW.actual_rental_duration_days, 0) * 24)) >= threshold_maintenance_hours
                    OR (total_rental_days + COALESCE(NEW.actual_rental_duration_days, 0)) >= threshold_maintenance_days
                THEN TRUE
                ELSE FALSE
            END,
            updated_at = CURRENT_TIMESTAMP
        WHERE device_id = NEW.device_id;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_device_usage
AFTER UPDATE ON rental_contracts
FOR EACH ROW
EXECUTE FUNCTION update_device_usage_hours();

-- =====================================================================
-- INITIAL DATA SEED
-- =====================================================================

-- Create default users
INSERT INTO users (username, email, password_hash, first_name, last_name, role)
VALUES
    ('agent_001', 'agent@o2medical.fr', 'hashed_password_agent', 'Jean', 'Agent', 'AGENT'),
    ('doctor_001', 'doctor@o2medical.fr', 'hashed_password_doctor', 'Dr. Marie', 'Medecin', 'DOCTOR')
ON CONFLICT (username) DO NOTHING;

-- Create sample devices
INSERT INTO devices (device_type, serial_number, manufacturer, model, device_status, acquisition_date, is_consumable)
VALUES
    ('OXYGEN_CONCENTRATOR_5L', 'SN-OXY5L-001', 'ResMed', 'SevenStar', 'AVAILABLE', CURRENT_DATE, FALSE),
    ('OXYGEN_CONCENTRATOR_10L', 'SN-OXY10L-001', 'Inogen', 'Inogen One G5', 'AVAILABLE', CURRENT_DATE, FALSE),
    ('OXYGEN_CONCENTRATOR_15L', 'SN-OXY15L-001', 'Philips', 'Respironics EverFlo', 'AVAILABLE', CURRENT_DATE, FALSE),
    ('SLEEP_APNEA_DEVICE', 'SN-CPAP-001', 'ResMed', 'AirSense 10', 'AVAILABLE', CURRENT_DATE, FALSE),
    ('OXYGEN_MASK', 'BULK-MASK-001', 'Generic', 'Standard Oxygen Mask', 'AVAILABLE', CURRENT_DATE, TRUE)
ON CONFLICT (serial_number) DO NOTHING;

-- Create device usage tracking
INSERT INTO device_usage_hours (device_id)
SELECT id FROM devices WHERE NOT is_consumable
ON CONFLICT DO NOTHING;

COMMIT;
