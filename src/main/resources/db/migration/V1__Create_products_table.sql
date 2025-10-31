-- Create products table
CREATE TABLE products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(200) NOT NULL,
    description VARCHAR(1000),
    unit VARCHAR(20),
    unit_cost DECIMAL(15,2),
    stock_quantity INT DEFAULT 0,
    reorder_level INT DEFAULT 0,
    lead_time_days INT DEFAULT 0,
    is_assembly BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
