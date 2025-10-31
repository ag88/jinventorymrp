package com.inventorymrp.dao;

import com.inventorymrp.model.InventoryTransaction;
import com.inventorymrp.util.DatabaseUtil;
import org.sql2o.Connection;
import org.sql2o.Sql2o;

import java.util.List;

/**
 * Data Access Object for InventoryTransaction entity.
 */
public class InventoryTransactionDAO {
    private final Sql2o sql2o;

    public InventoryTransactionDAO() {
        this.sql2o = DatabaseUtil.getSql2o();
    }

    public InventoryTransactionDAO(Sql2o sql2o) {
        this.sql2o = sql2o;
    }

    public InventoryTransaction create(InventoryTransaction transaction) {
        String sql = "INSERT INTO inventory_transactions (product_id, transaction_type, quantity, " +
                     "reference, transaction_date, created_at) " +
                     "VALUES (:productId, :transactionType, :quantity, :reference, " +
                     ":transactionDate, :createdAt)";
        
        try (Connection con = sql2o.open()) {
            long id = con.createQuery(sql, true)
                .bind(transaction)
                .executeUpdate()
                .getKey(Long.class);
            transaction.setId(id);
            return transaction;
        }
    }

    public InventoryTransaction findById(Long id) {
        String sql = "SELECT id, product_id as productId, transaction_type as transactionType, " +
                     "quantity, reference, transaction_date as transactionDate, " +
                     "created_at as createdAt " +
                     "FROM inventory_transactions WHERE id = :id";
        try (Connection con = sql2o.open()) {
            return con.createQuery(sql)
                .addParameter("id", id)
                .executeAndFetchFirst(InventoryTransaction.class);
        }
    }

    public List<InventoryTransaction> findByProductId(Long productId) {
        String sql = "SELECT id, product_id as productId, transaction_type as transactionType, " +
                     "quantity, reference, transaction_date as transactionDate, " +
                     "created_at as createdAt " +
                     "FROM inventory_transactions WHERE product_id = :productId " +
                     "ORDER BY transaction_date DESC";
        try (Connection con = sql2o.open()) {
            return con.createQuery(sql)
                .addParameter("productId", productId)
                .executeAndFetch(InventoryTransaction.class);
        }
    }

    public List<InventoryTransaction> findAll() {
        String sql = "SELECT id, product_id as productId, transaction_type as transactionType, " +
                     "quantity, reference, transaction_date as transactionDate, " +
                     "created_at as createdAt " +
                     "FROM inventory_transactions ORDER BY transaction_date DESC";
        try (Connection con = sql2o.open()) {
            return con.createQuery(sql)
                .executeAndFetch(InventoryTransaction.class);
        }
    }

    public List<InventoryTransaction> findByType(String transactionType) {
        String sql = "SELECT id, product_id as productId, transaction_type as transactionType, " +
                     "quantity, reference, transaction_date as transactionDate, " +
                     "created_at as createdAt " +
                     "FROM inventory_transactions WHERE transaction_type = :type " +
                     "ORDER BY transaction_date DESC";
        try (Connection con = sql2o.open()) {
            return con.createQuery(sql)
                .addParameter("type", transactionType)
                .executeAndFetch(InventoryTransaction.class);
        }
    }

    public void delete(Long id) {
        String sql = "DELETE FROM inventory_transactions WHERE id = :id";
        try (Connection con = sql2o.open()) {
            con.createQuery(sql)
                .addParameter("id", id)
                .executeUpdate();
        }
    }

    public int count() {
        String sql = "SELECT COUNT(*) FROM inventory_transactions";
        try (Connection con = sql2o.open()) {
            return con.createQuery(sql)
                .executeScalar(Integer.class);
        }
    }
}
