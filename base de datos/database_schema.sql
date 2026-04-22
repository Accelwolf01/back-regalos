-- ==========================================================
-- SCRIPT DE CREACIÓN DE BASE DE DATOS - GIFT STORE (PostgreSQL)
-- ==========================================================

-- 1. Roles y Permisos (Seguridad)
CREATE TABLE roles (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    description VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE permissions (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    module VARCHAR(50) NOT NULL,
    description VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT true
);

CREATE TABLE role_permissions (
    role_id BIGINT REFERENCES roles(id) ON DELETE CASCADE,
    permission_id BIGINT REFERENCES permissions(id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id)
);

CREATE TABLE app_users (
    id SERIAL PRIMARY KEY,
    email VARCHAR(150) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    role_id BIGINT REFERENCES roles(id),
    is_active BOOLEAN NOT NULL DEFAULT true,
    last_login TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Catálogo y Productos
CREATE TABLE categories (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    description TEXT,
    image_url VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE suppliers (
    id SERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    contact_name VARCHAR(100),
    phone VARCHAR(50),
    email VARCHAR(150),
    address TEXT,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE products (
    id SERIAL PRIMARY KEY,
    category_id BIGINT REFERENCES categories(id),
    supplier_id BIGINT REFERENCES suppliers(id),
    name VARCHAR(150) NOT NULL,
    description TEXT NOT NULL,
    price DECIMAL(12,0) NOT NULL,
    cost_price DECIMAL(12,0) NOT NULL DEFAULT 0,
    stock INTEGER NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE product_images (
    id SERIAL PRIMARY KEY,
    product_id BIGINT REFERENCES products(id) ON DELETE CASCADE,
    image_base64 TEXT NOT NULL,
    is_main BOOLEAN DEFAULT false,
    display_order INTEGER DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE product_promotions (
    id SERIAL PRIMARY KEY,
    product_id BIGINT REFERENCES products(id) ON DELETE CASCADE,
    discount_percentage DECIMAL(5,2),
    discount_price DECIMAL(12,0),
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 3. Ventas y Pedidos
CREATE TABLE customers (
    id SERIAL PRIMARY KEY,
    document_type VARCHAR(20) NOT NULL DEFAULT 'CC',
    document_number VARCHAR(50) UNIQUE NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE delivery_cities (
    id SERIAL PRIMARY KEY,
    department VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    delivery_cost DECIMAL(12,0) NOT NULL,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE order_statuses (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    display_order INTEGER DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE orders (
    id SERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL REFERENCES customers(id),
    subtotal_amount DECIMAL(12,0) NOT NULL,
    delivery_cost DECIMAL(12,0) NOT NULL DEFAULT 0,
    total_amount DECIMAL(12,0) NOT NULL,
    total_cost_amount DECIMAL(12,0) NOT NULL DEFAULT 0,
    currency VARCHAR(10) DEFAULT 'COP',
    delivery_city_id BIGINT REFERENCES delivery_cities(id),
    delivery_neighborhood VARCHAR(150),
    delivery_address TEXT NOT NULL,
    delivery_instructions TEXT,
    delivery_date DATE,
    delivery_time_range VARCHAR(50),
    gift_sender_name VARCHAR(100),
    gift_receiver_name VARCHAR(100),
    gift_message TEXT,
    tracking_code VARCHAR(50) UNIQUE NOT NULL,
    order_status_id BIGINT REFERENCES order_statuses(id),
    payment_status VARCHAR(50) DEFAULT 'PENDING',
    bold_transaction_id VARCHAR(100) UNIQUE,
    payment_method VARCHAR(50),
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE order_items (
    id SERIAL PRIMARY KEY,
    order_id BIGINT REFERENCES orders(id) ON DELETE CASCADE,
    product_id BIGINT REFERENCES products(id),
    product_name VARCHAR(150) NOT NULL,
    unit_price DECIMAL(12,0) NOT NULL,
    unit_cost_price DECIMAL(12,0) NOT NULL DEFAULT 0,
    quantity INTEGER NOT NULL,
    subtotal DECIMAL(12,0) NOT NULL,
    subtotal_cost DECIMAL(12,0) NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE order_tracking_history (
    id SERIAL PRIMARY KEY,
    order_id BIGINT REFERENCES orders(id) ON DELETE CASCADE,
    previous_status_id BIGINT REFERENCES order_statuses(id),
    new_status_id BIGINT REFERENCES order_statuses(id) NOT NULL,
    comments TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 4. Ajustes y Personalización de la Tienda
CREATE TABLE store_banners (
    id SERIAL PRIMARY KEY,
    image_base64 TEXT NOT NULL,
    title VARCHAR(255) NOT NULL,
    subtitle VARCHAR(255),
    display_order INTEGER DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE store_carousel_images (
    id SERIAL PRIMARY KEY,
    image_base64 TEXT NOT NULL,
    display_order INTEGER DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE store_settings (
    id SERIAL PRIMARY KEY,
    config_group VARCHAR(50) NOT NULL,
    config_key VARCHAR(100) UNIQUE NOT NULL,
    config_value TEXT NOT NULL,
    description VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ==========================================================
-- DATOS INICIALES SEMILLA
-- ==========================================================

-- Roles base
INSERT INTO roles (name, description) VALUES 
('SUPER_ADMIN', 'Administrador total con acceso a todo'),
('DUEÑO_TIENDA', 'Dueño de la tienda con permisos administrativos'),
('EMPLEADO', 'Personal operativo'),
('CLIENTE', 'Cliente del frontend');

-- Estados de pedido
INSERT INTO order_statuses (name, description, display_order) VALUES 
('PENDIENTE_PAGO', 'El pedido espera confirmación de pago', 1),
('PAGADO', 'Pago confirmado, listo para procesar', 2),
('EN_PREPARACION', 'El regalo está siendo armado', 3),
('EN_CAMINO', 'El pedido ha salido a entrega', 4),
('ENTREGADO', 'El pedido fue entregado con éxito', 5),
('CANCELADO', 'El pedido fue anulado', 6);

-- Configuración básica
INSERT INTO store_settings (config_group, config_key, config_value, description) VALUES 
('GENERAL', 'store_name', 'GiftMagic', 'Nombre comercial de la tienda'),
('SOCIAL', 'social_whatsapp', '573000000000', 'Número de WhatsApp para contacto');

-- Usuarios administradores de prueba (Contraseña: admin123)
-- El hash corresponde a BCrypt, que es el que usa tu backend.
INSERT INTO app_users (email, password_hash, first_name, last_name, role_id, is_active) VALUES 
('admin1@regalos.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.TVuHOnu', 'Admin', 'Uno', 1, true),
('admin2@regalos.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.TVuHOnu', 'Admin', 'Dos', 1, true);
