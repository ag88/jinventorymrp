package com.inventorymrp.dao;

import com.inventorymrp.model.PurchaseOrder;
import com.inventorymrp.util.DatabaseUtil;
import org.sql2o.Connection;
import org.sql2o.Sql2o;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Data Access Object for PurchaseOrder entity.
 */
public class PurchaseOrderDAO {
    private final Sql2o sql2o;

    public PurchaseOrderDAO() {
        this.sql2o = DatabaseUtil.getSql2o();
    }

    public PurchaseOrderDAO(Sql2o sql2o) {
        this.sql2o = sql2o;
    }

    public PurchaseOrder create(PurchaseOrder purchaseOrder) {
        String sql = "INSERT INTO purchase_orders (product_id, quantity, status, order_date, " +
                     "expected_delivery_date, supplier, reference, created_at, updated_at) " +
                     "VALUES (:productId, :quantity, :status, :orderDate, " +
                     ":expectedDeliveryDate, :supplier, :reference, :createdAt, :updatedAt)";
        
        try (Connection con = sql2o.open()) {
            long id = con.createQuery(sql, true)
                .bind(purchaseOrder)
                .executeUpdate()
                .getKey(Long.class);
            purchaseOrder.setId(id);
            return purchaseOrder;
        }
    }

    public PurchaseOrder findById(Long id) {
        String sql = "SELECT * FROM purchase_orders WHERE id = :id";
        try (Connection con = sql2o.open()) {
            return con.createQuery(sql)
                .addParameter("id", id)
                .executeAndFetchFirst(PurchaseOrder.class);
        }
    }

    public List<PurchaseOrder> findByProductId(Long productId) {
        String sql = "SELECT * FROM purchase_orders WHERE product_id = :productId " +
                     "ORDER BY order_date DESC";
        try (Connection con = sql2o.open()) {
            return con.createQuery(sql)
                .addParameter("productId", productId)
                .executeAndFetch(PurchaseOrder.class);
        }
    }

    public List<PurchaseOrder> findByStatus(String status) {
        String sql = "SELECT * FROM purchase_orders WHERE status = :status " +
                     "ORDER BY order_date DESC";
        try (Connection con = sql2o.open()) {
            return con.createQuery(sql)
                .addParameter("status", status)
                .executeAndFetch(PurchaseOrder.class);
        }
    }

    public List<PurchaseOrder> findAll() {
        String sql = "SELECT product_id as productId, " 
        		+ "quantity, "
        		+ "status, "
        		+ "order_date as orderDate, "
        		+ "expected_delivery_date as expectedDeliveryDate, "
        		+ "supplier, "
        		+ "reference, "
        		+ "created_at as createdAt, "
        		+ "updated_at as updatedAt "
        		+ "FROM purchase_orders ORDER BY order_date DESC";
        try (Connection con = sql2o.open()) {
            return con.createQuery(sql)
                .executeAndFetch(PurchaseOrder.class);
        }
    }

    public void update(PurchaseOrder purchaseOrder) {
        purchaseOrder.setUpdatedAt(LocalDateTime.now());
        String sql = "UPDATE purchase_orders SET product_id = :productId, quantity = :quantity, " +
                     "status = :status, order_date = :orderDate, " +
                     "expected_delivery_date = :expectedDeliveryDate, supplier = :supplier, " +
                     "reference = :reference, updated_at = :updatedAt " +
                     "WHERE id = :id";
        
        try (Connection con = sql2o.open()) {
            con.createQuery(sql)
                .bind(purchaseOrder)
                .executeUpdate();
        }
    }

    public void delete(Long id) {
        String sql = "DELETE FROM purchase_orders WHERE id = :id";
        try (Connection con = sql2o.open()) {
            con.createQuery(sql)
                .addParameter("id", id)
                .executeUpdate();
        }
    }

    public int count() {
        String sql = "SELECT COUNT(*) FROM purchase_orders";
        try (Connection con = sql2o.open()) {
            return con.createQuery(sql)
                .executeScalar(Integer.class);
        }
    }
}
