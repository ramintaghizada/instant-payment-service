
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    phone_number VARCHAR(20) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    salt VARCHAR(255),
    full_name VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    email_verified BOOLEAN DEFAULT FALSE,
    phone_verified BOOLEAN DEFAULT FALSE,
    two_factor_enabled BOOLEAN DEFAULT FALSE,
    two_factor_secret VARCHAR(255),
    failed_login_attempts INT DEFAULT 0,
    last_login_at TIMESTAMP,
    last_login_ip VARCHAR(45),
    password_changed_at TIMESTAMP,
    device_fingerprint VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- V5__create_wallets_table.sql
CREATE TABLE IF NOT EXISTS wallets (
    wallet_id VARCHAR(50) PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    wallet_name VARCHAR(100) NOT NULL,
    balance DECIMAL(19,4) NOT NULL DEFAULT 0,
    currency VARCHAR(3) NOT NULL DEFAULT 'AZN',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    pin_hash VARCHAR(255),
    pin_salt VARCHAR(255),
    is_primary BOOLEAN DEFAULT FALSE,
    daily_limit DECIMAL(19,4) DEFAULT 10000,
    transaction_limit DECIMAL(19,4) DEFAULT 5000,
    daily_spent DECIMAL(19,4) DEFAULT 0,
    last_reset_date DATE,
    version BIGINT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- V6__create_roles_and_permissions.sql
CREATE TABLE IF NOT EXISTS roles (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    description TEXT
);

CREATE TABLE IF NOT EXISTS permissions (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    description TEXT
);

CREATE TABLE IF NOT EXISTS user_roles (
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    role_id INT REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE IF NOT EXISTS role_permissions (
    role_id INT REFERENCES roles(id) ON DELETE CASCADE,
    permission_id INT REFERENCES permissions(id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id)
);

-- V7__create_sessions_table.sql
CREATE TABLE IF NOT EXISTS user_sessions (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    access_token TEXT NOT NULL,
    refresh_token TEXT NOT NULL,
    ip_address VARCHAR(45),
    user_agent TEXT,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_sessions_access_token ON user_sessions(access_token);
CREATE INDEX idx_sessions_user_id ON user_sessions(user_id);

-- Insert default roles
INSERT INTO roles (name, description) VALUES 
('ROLE_USER', 'Regular user with basic permissions'),
('ROLE_MERCHANT', 'Merchant account with additional permissions'),
('ROLE_ADMIN', 'Administrator with full access'),
('ROLE_SUPPORT', 'Customer support with limited admin access');

-- Insert permissions
INSERT INTO permissions (name, description) VALUES 
('VIEW_BALANCE', 'View wallet balance'),
('SEND_MONEY', 'Send money to other wallets'),
('RECEIVE_MONEY', 'Receive money from other wallets'),
('VIEW_TRANSACTIONS', 'View transaction history'),
('MANAGE_WALLETS', 'Create and manage wallets'),
('MANAGE_USERS', 'Manage user accounts'),
('VIEW_ANALYTICS', 'View system analytics'),
('MANAGE_SYSTEM', 'Manage system configuration');

-- Assign permissions to roles (simplified example)
-- Admin gets all permissions
-- User gets basic permissions