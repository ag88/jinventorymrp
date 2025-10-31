package com.inventorymrp.util;

import org.sql2o.Sql2o;

/**
 * Database utility class for managing H2 database connection.
 */
public class DatabaseUtil {
    private static Sql2o sql2o;
    private static final String DB_URL = "jdbc:h2:./data/inventorydb;AUTO_SERVER=TRUE";
    private static final String DB_USER = "sa";
    private static final String DB_PASSWORD = "";

    private DatabaseUtil() {
        // Private constructor to prevent instantiation
    }

    public static Sql2o getSql2o() {
        if (sql2o == null) {
            sql2o = new Sql2o(DB_URL, DB_USER, DB_PASSWORD);
            sql2o.setDefaultCaseSensitive(false);
        }
        return sql2o;
    }

    public static Sql2o getSql2o(String url, String user, String password) {
        sql2o = new Sql2o(url, user, password);
        sql2o.setDefaultCaseSensitive(false);
        return sql2o;
    }

    public static void initializeDatabase() {
        try (org.sql2o.Connection con = getSql2o().open()) {
            // Create Products table
            con.createQuery(
                "CREATE TABLE IF NOT EXISTS products (" +
                "  id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                "  code VARCHAR(50) UNIQUE NOT NULL," +
                "  name VARCHAR(200) NOT NULL," +
                "  description VARCHAR(1000)," +
                "  unit VARCHAR(20)," +
                "  unit_cost DECIMAL(15,2)," +
                "  stock_quantity INT DEFAULT 0," +
                "  reorder_level INT DEFAULT 0," +
                "  lead_time_days INT DEFAULT 0," +
                "  is_assembly BOOLEAN DEFAULT FALSE," +
                "  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")"
            ).executeUpdate();

            // Create BOM Items table
            con.createQuery(
                "CREATE TABLE IF NOT EXISTS bom_items (" +
                "  id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                "  parent_product_id BIGINT NOT NULL," +
                "  child_product_id BIGINT NOT NULL," +
                "  quantity DECIMAL(15,4) NOT NULL," +
                "  unit VARCHAR(20)," +
                "  sequence_number INT DEFAULT 0," +
                "  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "  FOREIGN KEY (parent_product_id) REFERENCES products(id)," +
                "  FOREIGN KEY (child_product_id) REFERENCES products(id)" +
                ")"
            ).executeUpdate();

            // Create Inventory Transactions table
            con.createQuery(
                "CREATE TABLE IF NOT EXISTS inventory_transactions (" +
                "  id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                "  product_id BIGINT NOT NULL," +
                "  transaction_type VARCHAR(20) NOT NULL," +
                "  quantity INT NOT NULL," +
                "  reference VARCHAR(200)," +
                "  transaction_date TIMESTAMP NOT NULL," +
                "  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "  FOREIGN KEY (product_id) REFERENCES products(id)" +
                ")"
            ).executeUpdate();

            // Create Purchase Orders table
            con.createQuery(
                "CREATE TABLE IF NOT EXISTS purchase_orders (" +
                "  id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                "  product_id BIGINT NOT NULL," +
                "  quantity INT NOT NULL," +
                "  status VARCHAR(20) DEFAULT 'PENDING'," +
                "  order_date DATE NOT NULL," +
                "  expected_delivery_date DATE," +
                "  supplier VARCHAR(200)," +
                "  reference VARCHAR(200)," +
                "  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "  FOREIGN KEY (product_id) REFERENCES products(id)" +
                ")"
            ).executeUpdate();

            System.out.println("Database initialized successfully");
        } catch (Exception e) {
            System.err.println("Error initializing database: " + e.getMessage());
            throw new RuntimeException("Failed to initialize database", e);
        }
    }
}
