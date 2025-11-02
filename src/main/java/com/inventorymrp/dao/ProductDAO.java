package com.inventorymrp.dao;

import com.inventorymrp.model.Product;
import com.inventorymrp.util.DatabaseUtil;
import org.sql2o.Connection;
import org.sql2o.Sql2o;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Data Access Object for Product entity.
 */
public class ProductDAO {
    private final Sql2o sql2o;

    public ProductDAO() {
        this.sql2o = DatabaseUtil.getSql2o();
    }

    public ProductDAO(Sql2o sql2o) {
        this.sql2o = sql2o;
    }

    public Product create(Product product) {
        String sql = "INSERT INTO products (code, name, description, unit, unit_cost, " +
                     "stock_quantity, reorder_level, order_lead_time, item_lead_time, is_assembly, non_purchase, " +
                     " created_at, updated_at) " +
                     "VALUES (:code, :name, :description, :unit, :unitCost, " +
                     ":stockQuantity, :reorderLevel, :orderLeadTime, :itemLeadTime, :isAssembly, :nonPurchase, " +
                     ":createdAt, :updatedAt)";
        
        try (Connection con = sql2o.open()) {
            long id = con.createQuery(sql, true)
                .bind(product)
                .executeUpdate()
                .getKey(Long.class);
            product.setId(id);
            return product;
        }
    }

    public Product findById(Long id) {
        String sql = "SELECT id, code, name, description, unit, " +
                     "unit_cost as unitCost, stock_quantity as stockQuantity, " +
                     "reorder_level as reorderLevel, order_lead_time as orderLeadTime, " +
                     "item_lead_time as itemLeadTime, is_assembly as isAssembly, " +
                     "non_purchase as nonPurchase " +
                     "FROM products WHERE id = :id";
        try (Connection con = sql2o.open()) {
            return con.createQuery(sql)
                .addParameter("id", id)
                .executeAndFetchFirst(Product.class);
        }
    }

    public Product findByCode(String code) {
        String sql = "SELECT id, code, name, description, unit, " +
                     "unit_cost as unitCost, stock_quantity as stockQuantity, " +
                     "reorder_level as reorderLevel, order_lead_time as orderLeadTime, " +
                     "item_lead_time as itemLeadTime, is_assembly as isAssembly, " +
                     "non_purchase as nonPurchase " +
                     "FROM products WHERE code = :code";
        try (Connection con = sql2o.open()) {
            return con.createQuery(sql)
                .addParameter("code", code)
                .executeAndFetchFirst(Product.class);
        }
    }

    public List<Product> findAll() {
        String sql = "SELECT id, code, name, description, unit, " +
                     "unit_cost as unitCost, stock_quantity as stockQuantity, " +
                     "reorder_level as reorderLevel, order_lead_time as orderLeadTime, " +
                     "item_lead_time as itemLeadTime, is_assembly as isAssembly, " +
                     "non_purchase as nonPurchase, " +
                     "FROM products ORDER BY code";
        try (Connection con = sql2o.open()) {
            return con.createQuery(sql)
                .executeAndFetch(Product.class);
        }
    }

    public List<Product> findAssemblies() {
        String sql = "SELECT id, code, name, description, unit, " +
                     "unit_cost as unitCost, stock_quantity as stockQuantity, " +
                     "reorder_level as reorderLevel, order_lead_time as orderLeadTime, " +
                     "item_lead_time as itemLeadTime, is_assembly as isAssembly, " +
                     "non_purchase as nonPurchase " +
                     "FROM products WHERE is_assembly = true ORDER BY code";
        try (Connection con = sql2o.open()) {
            return con.createQuery(sql)
                .executeAndFetch(Product.class);
        }
    }

    public List<Product> findComponents() {
        String sql = "SELECT id, code, name, description, unit, " +
                     "unit_cost as unitCost, stock_quantity as stockQuantity, " +
                     "reorder_level as reorderLevel, order_lead_time as orderLeadTime, " +
                     "item_lead_time as itemLeadTime, is_assembly as isAssembly, " +
                     "non_purchase as nonPurchase " +
                     "FROM products WHERE is_assembly = false ORDER BY code";
        try (Connection con = sql2o.open()) {
            return con.createQuery(sql)
                .executeAndFetch(Product.class);
        }
    }

    public void update(Product product) {
        product.setUpdatedAt(LocalDateTime.now());
        String sql = "UPDATE products SET code = :code, name = :name, description = :description, " +
                     "unit = :unit, unit_cost = :unitCost, stock_quantity = :stockQuantity, " +
                     "reorder_level = :reorderLevel, order_lead_time = :orderLeadTime, " +
                     "item_lead_time = :itemLeadTime, is_assembly = :isAssembly," +
                     "non_purchase = :nonPurchase, updated_at = :updatedAt " +
                     "WHERE id = :id";
        
        try (Connection con = sql2o.open()) {
            con.createQuery(sql)
                .bind(product)
                .executeUpdate();
        }
    }

    public void updateStockQuantity(Long productId, Integer newQuantity) {
        String sql = "UPDATE products SET stock_quantity = :quantity, updated_at = :updatedAt WHERE id = :id";
        try (Connection con = sql2o.open()) {
            con.createQuery(sql)
                .addParameter("id", productId)
                .addParameter("quantity", newQuantity)
                .addParameter("updatedAt", LocalDateTime.now())
                .executeUpdate();
        }
    }

    public void delete(Long id) {
        String sql = "DELETE FROM products WHERE id = :id";
        try (Connection con = sql2o.open()) {
            con.createQuery(sql)
                .addParameter("id", id)
                .executeUpdate();
        }
    }

    public int count() {
        String sql = "SELECT COUNT(*) FROM products";
        try (Connection con = sql2o.open()) {
            return con.createQuery(sql)
                .executeScalar(Integer.class);
        }
    }
}
