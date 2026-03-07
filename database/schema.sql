CREATE TABLE IF NOT EXISTS customers (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    license_number VARCHAR(40) NOT NULL UNIQUE,
    email VARCHAR(255),
    password_hash VARCHAR(64),
    role VARCHAR(20) NOT NULL DEFAULT 'user' CHECK (role IN ('admin', 'user')),
    is_blocked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

ALTER TABLE customers ADD COLUMN IF NOT EXISTS email VARCHAR(255);
ALTER TABLE customers ADD COLUMN IF NOT EXISTS password_hash VARCHAR(64);
ALTER TABLE customers ADD COLUMN IF NOT EXISTS role VARCHAR(20) NOT NULL DEFAULT 'user';

ALTER TABLE customers DROP CONSTRAINT IF EXISTS customers_role_check;
ALTER TABLE customers
    ADD CONSTRAINT customers_role_check CHECK (role IN ('admin', 'user'));

CREATE UNIQUE INDEX IF NOT EXISTS uq_customers_email_lower
ON customers ((LOWER(email)));

INSERT INTO customers (name, phone, license_number, email, password_hash, role, is_blocked)
VALUES (
    'System Admin',
    '0000000000',
    'ADMIN-LICENSE',
    'admin@letmedrive.com',
    'e86f78a8a3caf0b60d8e74e5942aa6d86dc150cd3c03338aef25b7d2d7e3acc7',
    'admin',
    FALSE
)
ON CONFLICT ((LOWER(email))) DO UPDATE
SET
    password_hash = EXCLUDED.password_hash,
    role = 'admin',
    is_blocked = FALSE;

CREATE TABLE IF NOT EXISTS cars (
    id BIGSERIAL PRIMARY KEY,
    car_name VARCHAR(120) NOT NULL,
    brand VARCHAR(80) NOT NULL,
    seat_count INT NOT NULL CHECK (seat_count > 0),
    premium_level VARCHAR(20) NOT NULL DEFAULT 'PREMIUM' CHECK (premium_level = 'PREMIUM'),
    price_per_hour NUMERIC(10,2) NOT NULL CHECK (price_per_hour >= 0),
    late_fee_per_hour NUMERIC(10,2) NOT NULL CHECK (late_fee_per_hour >= 0),
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS rentals (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL REFERENCES customers(id),
    car_id BIGINT NOT NULL REFERENCES cars(id),
    start_time TIMESTAMP NOT NULL,
    expected_return_time TIMESTAMP NOT NULL,
    actual_return_time TIMESTAMP NULL,
    base_amount NUMERIC(10,2) NOT NULL,
    fine_amount NUMERIC(10,2) NOT NULL DEFAULT 0,
    total_amount NUMERIC(10,2) NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('BOOKED', 'RETURNED')),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CHECK (start_time < expected_return_time)
);

CREATE INDEX IF NOT EXISTS idx_rentals_car_status_time
ON rentals (car_id, status, start_time, expected_return_time);
