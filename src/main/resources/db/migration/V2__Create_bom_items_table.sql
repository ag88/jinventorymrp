-- Create bom_items table
CREATE TABLE bom_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    parent_product_id BIGINT NOT NULL,
    child_product_id BIGINT NOT NULL,
    quantity DECIMAL(15,4) NOT NULL,
    unit VARCHAR(20),
    sequence_number INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (parent_product_id) REFERENCES products(id),
    FOREIGN KEY (child_product_id) REFERENCES products(id)
);
