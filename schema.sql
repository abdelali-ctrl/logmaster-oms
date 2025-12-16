-- LogMaster Complete Database Schema v2
-- With multi-product order support
-- Run as: psql -U postgres -d logmaster_db -f schema.sql

-- Drop existing tables if any (in correct order due to foreign keys)
DROP TABLE IF EXISTS order_items CASCADE;
DROP TABLE IF EXISTS orders CASCADE;
DROP TABLE IF EXISTS products CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- Create users table (with role for authentication)
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'USER'
);

-- Create products table
CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    price DOUBLE PRECISION NOT NULL,
    stock INTEGER NOT NULL,
    category VARCHAR(255) NOT NULL
);

-- Create orders table (updated for multi-product)
CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    product_id BIGINT REFERENCES products(id),  -- Legacy, nullable
    quantity INTEGER,  -- Legacy, nullable
    total_amount DOUBLE PRECISION NOT NULL,
    order_date TIMESTAMP NOT NULL,
    status VARCHAR(50) NOT NULL,
    shipping_address VARCHAR(255) NOT NULL,
    notes TEXT
);

-- Create order_items table (for multi-product orders)
CREATE TABLE order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    product_id BIGINT NOT NULL REFERENCES products(id),
    quantity INTEGER NOT NULL,
    unit_price DOUBLE PRECISION NOT NULL
);

-- Create indexes
CREATE INDEX idx_orders_user_id ON orders(user_id);
CREATE INDEX idx_orders_product_id ON orders(product_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_date ON orders(order_date DESC);
CREATE INDEX idx_order_items_order_id ON order_items(order_id);
CREATE INDEX idx_order_items_product_id ON order_items(product_id);
CREATE INDEX idx_products_category ON products(category);
CREATE INDEX idx_users_email ON users(email);

-- Grant permissions to application user
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO logmaster_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO logmaster_user;
GRANT USAGE ON SCHEMA public TO logmaster_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO logmaster_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO logmaster_user;

-- Insert test data

-- Users (with roles)
INSERT INTO users (name, email, password, role) VALUES 
    ('Admin User', 'admin@example.com', 'admin123', 'ADMIN'),
    ('Jean Dupont', 'jean@example.com', 'password123', 'USER'),
    ('Marie Martin', 'marie@example.com', 'password123', 'USER'),
    ('Pierre Durand', 'pierre@example.com', 'password123', 'USER');

-- Products
INSERT INTO products (name, price, stock, category) VALUES 
    ('iPhone 15 Pro', 1199.00, 50, 'Électronique'),
    ('MacBook Air M3', 1299.00, 30, 'Électronique'),
    ('AirPods Pro', 279.00, 100, 'Électronique'),
    ('Nike Air Max', 149.00, 75, 'Sport'),
    ('Adidas Ultraboost', 189.00, 60, 'Sport'),
    ('Samsung Galaxy S24', 999.00, 40, 'Électronique'),
    ('Sony WH-1000XM5', 349.00, 45, 'Électronique'),
    ('Levi''s 501 Jeans', 89.00, 120, 'Vêtements'),
    ('PS5 Console', 499.00, 25, 'Gaming'),
    ('Nintendo Switch', 299.00, 55, 'Gaming');

-- Sample orders (legacy format for backward compatibility)
INSERT INTO orders (user_id, product_id, quantity, total_amount, order_date, status, shipping_address, notes) VALUES
    (2, 1, 1, 1199.00, NOW() - INTERVAL '5 days', 'DELIVERED', '123 Rue de Paris, 75001 Paris', 'Livraison express'),
    (2, 3, 2, 558.00, NOW() - INTERVAL '3 days', 'SHIPPED', '123 Rue de Paris, 75001 Paris', NULL),
    (3, 2, 1, 1299.00, NOW() - INTERVAL '2 days', 'PROCESSING', '45 Avenue Lyon, 69001 Lyon', 'Couleur: Gris'),
    (3, 4, 1, 149.00, NOW() - INTERVAL '1 day', 'CONFIRMED', '45 Avenue Lyon, 69001 Lyon', NULL),
    (4, 6, 1, 999.00, NOW(), 'PENDING', '78 Boulevard Nice, 06000 Nice', 'Urgent');

-- Verify
SELECT 'Schema v2 created successfully!' AS result;
SELECT 'Users:' AS info, COUNT(*) AS count FROM users;
SELECT 'Products:' AS info, COUNT(*) AS count FROM products;
SELECT 'Orders:' AS info, COUNT(*) AS count FROM orders;
