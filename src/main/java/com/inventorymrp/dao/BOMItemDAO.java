package com.inventorymrp.dao;

import com.inventorymrp.model.BOMItem;
import com.inventorymrp.model.Product;
import com.inventorymrp.util.DatabaseUtil;
import org.sql2o.Connection;
import org.sql2o.Sql2o;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Data Access Object for BOMItem entity.
 */
public class BOMItemDAO {
    private final Sql2o sql2o;
    private final ProductDAO productDAO;

    public BOMItemDAO() {
        this.sql2o = DatabaseUtil.getSql2o();
        this.productDAO = new ProductDAO(sql2o);
    }

    public BOMItemDAO(Sql2o sql2o) {
        this.sql2o = sql2o;
        this.productDAO = new ProductDAO(sql2o);
    }

    public BOMItem create(BOMItem bomItem) {
        String sql = "INSERT INTO bom_items (parent_product_id, child_product_id, quantity, " +
                     "unit, sequence_number, created_at, updated_at) " +
                     "VALUES (:parentProductId, :childProductId, :quantity, " +
                     ":unit, :sequenceNumber, :createdAt, :updatedAt)";
        
        try (Connection con = sql2o.open()) {
            long id = con.createQuery(sql, true)
                .bind(bomItem)
                .executeUpdate()
                .getKey(Long.class);
            bomItem.setId(id);
            return bomItem;
        }
    }

    public BOMItem findById(Long id) {
        String sql = "SELECT id, parent_product_id as parentProductId, child_product_id as childProductId, " +
                     "quantity, unit, sequence_number as sequenceNumber " +
                     "FROM bom_items WHERE id = :id";
        try (Connection con = sql2o.open()) {
            BOMItem bomItem = con.createQuery(sql)
                .addParameter("id", id)
                .executeAndFetchFirst(BOMItem.class);
            
            if (bomItem != null) {
                enrichWithProducts(bomItem);
            }
            return bomItem;
        }
    }

    public List<BOMItem> findByParentProductId(Long parentProductId) {
        String sql = "SELECT id, parent_product_id as parentProductId, child_product_id as childProductId, " +
                     "quantity, unit, sequence_number as sequenceNumber " +
                     "FROM bom_items WHERE parent_product_id = :parentProductId ORDER BY sequence_number";
        try (Connection con = sql2o.open()) {
            List<BOMItem> bomItems = con.createQuery(sql)
                .addParameter("parentProductId", parentProductId)
                .executeAndFetch(BOMItem.class);
            
            for (BOMItem bomItem : bomItems) {
                enrichWithProducts(bomItem);
            }
            return bomItems;
        }
    }

    public List<BOMItem> findByChildProductId(Long childProductId) {
        String sql = "SELECT id, parent_product_id as parentProductId, child_product_id as childProductId, " +
                     "quantity, unit, sequence_number as sequenceNumber " +
                     "FROM bom_items WHERE child_product_id = :childProductId ORDER BY sequence_number";
        try (Connection con = sql2o.open()) {
            List<BOMItem> bomItems = con.createQuery(sql)
                .addParameter("childProductId", childProductId)
                .executeAndFetch(BOMItem.class);
            
            for (BOMItem bomItem : bomItems) {
                enrichWithProducts(bomItem);
            }
            return bomItems;
        }
    }

    public List<BOMItem> findAll() {
        String sql = "SELECT id, parent_product_id as parentProductId, child_product_id as childProductId, " +
                     "quantity, unit, sequence_number as sequenceNumber " +
                     "FROM bom_items ORDER BY parent_product_id, sequence_number";
        try (Connection con = sql2o.open()) {
            List<BOMItem> bomItems = con.createQuery(sql)
                .executeAndFetch(BOMItem.class);
            
            for (BOMItem bomItem : bomItems) {
                enrichWithProducts(bomItem);
            }
            return bomItems;
        }
    }

    public void update(BOMItem bomItem) {
        bomItem.setUpdatedAt(LocalDateTime.now());
        String sql = "UPDATE bom_items SET parent_product_id = :parentProductId, " +
                     "child_product_id = :childProductId, quantity = :quantity, " +
                     "unit = :unit, sequence_number = :sequenceNumber, updated_at = :updatedAt " +
                     "WHERE id = :id";
        
        try (Connection con = sql2o.open()) {
            con.createQuery(sql)
                .bind(bomItem)
                .executeUpdate();
        }
    }

    public void delete(Long id) {
        String sql = "DELETE FROM bom_items WHERE id = :id";
        try (Connection con = sql2o.open()) {
            con.createQuery(sql)
                .addParameter("id", id)
                .executeUpdate();
        }
    }

    public void deleteByParentProductId(Long parentProductId) {
        String sql = "DELETE FROM bom_items WHERE parent_product_id = :parentProductId";
        try (Connection con = sql2o.open()) {
            con.createQuery(sql)
                .addParameter("parentProductId", parentProductId)
                .executeUpdate();
        }
    }

    private void enrichWithProducts(BOMItem bomItem) {
        if (bomItem != null) {
            if (bomItem.getParentProductId() != null) {
                bomItem.setParentProduct(productDAO.findById(bomItem.getParentProductId()));
            }
            if (bomItem.getChildProductId() != null) {
                bomItem.setChildProduct(productDAO.findById(bomItem.getChildProductId()));
            }
        }
    }

    public int count() {
        String sql = "SELECT COUNT(*) FROM bom_items";
        try (Connection con = sql2o.open()) {
            return con.createQuery(sql)
                .executeScalar(Integer.class);
        }
    }
}
