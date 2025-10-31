-- Refactor product lead time columns
-- Rename lead_time_days to order_lead_time and change type to DOUBLE
-- Add item_lead_time column as DOUBLE

-- Add new columns first
ALTER TABLE products ADD COLUMN order_lead_time DOUBLE DEFAULT 0.0;
ALTER TABLE products ADD COLUMN item_lead_time DOUBLE DEFAULT 0.0;

-- Copy existing data from lead_time_days to order_lead_time
UPDATE products SET order_lead_time = CAST(lead_time_days AS DOUBLE);

-- Drop old column
ALTER TABLE products DROP COLUMN lead_time_days;
