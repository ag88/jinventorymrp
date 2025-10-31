package com.inventorymrp.dao;

import com.inventorymrp.model.BOMItem;
import com.inventorymrp.model.Product;
import com.inventorymrp.util.DatabaseUtil;
import org.junit.jupiter.api.*;
import org.sql2o.Sql2o;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for BOMItemDAO.
 */
class BOMItemDAOTest {
    private static Sql2o sql2o;
    private BOMItemDAO bomItemDAO;
    private ProductDAO productDAO;

    @BeforeAll
    static void setupDatabase() {
        sql2o = DatabaseUtil.getSql2o("jdbc:h2:mem:testdb2;DB_CLOSE_DELAY=-1", "sa", "");
        DatabaseUtil.initializeDatabase("jdbc:h2:mem:testdb2;DB_CLOSE_DELAY=-1", "sa", "");
    }

    @BeforeEach
    void setUp() {
        bomItemDAO = new BOMItemDAO(sql2o);
        productDAO = new ProductDAO(sql2o);
        
        // Clean up before each test
        try (org.sql2o.Connection con = sql2o.open()) {
            con.createQuery("DELETE FROM bom_items").executeUpdate();
            con.createQuery("DELETE FROM inventory_transactions").executeUpdate();
            con.createQuery("DELETE FROM purchase_orders").executeUpdate();
            con.createQuery("DELETE FROM products").executeUpdate();
        }
    }

    @Test
    void testCreateBOMItem() {
        Product parent = new Product("ASSY", "Assembly");
        parent.setIsAssembly(true);
        parent = productDAO.create(parent);

        Product child = new Product("COMP", "Component");
        child = productDAO.create(child);

        BOMItem bomItem = new BOMItem(parent.getId(), child.getId(), new BigDecimal("2.5"));
        bomItem.setUnit("pcs");
        bomItem.setSequenceNumber(1);

        BOMItem created = bomItemDAO.create(bomItem);

        assertNotNull(created.getId());
        assertEquals(parent.getId(), created.getParentProductId());
        assertEquals(child.getId(), created.getChildProductId());
        assertEquals(new BigDecimal("2.5"), created.getQuantity());
    }

    @Test
    void testFindById() {
        Product parent = productDAO.create(new Product("ASSY1", "Assembly 1"));
        Product child = productDAO.create(new Product("COMP1", "Component 1"));

        BOMItem bomItem = new BOMItem(parent.getId(), child.getId(), new BigDecimal("3"));
        BOMItem created = bomItemDAO.create(bomItem);

        BOMItem found = bomItemDAO.findById(created.getId());

        assertNotNull(found);
        assertEquals(created.getId(), found.getId());
        assertNotNull(found.getParentProduct());
        assertNotNull(found.getChildProduct());
        assertEquals("ASSY1", found.getParentProduct().getCode());
        assertEquals("COMP1", found.getChildProduct().getCode());
    }

    @Test
    void testFindByParentProductId() {
        Product parent = productDAO.create(new Product("ASSY2", "Assembly 2"));
        Product child1 = productDAO.create(new Product("COMP2", "Component 2"));
        Product child2 = productDAO.create(new Product("COMP3", "Component 3"));

        bomItemDAO.create(new BOMItem(parent.getId(), child1.getId(), new BigDecimal("1")));
        bomItemDAO.create(new BOMItem(parent.getId(), child2.getId(), new BigDecimal("2")));

        List<BOMItem> bomItems = bomItemDAO.findByParentProductId(parent.getId());

        assertEquals(2, bomItems.size());
        assertTrue(bomItems.stream().allMatch(b -> b.getParentProductId().equals(parent.getId())));
    }

    @Test
    void testFindByChildProductId() {
        Product parent1 = productDAO.create(new Product("ASSY3", "Assembly 3"));
        Product parent2 = productDAO.create(new Product("ASSY4", "Assembly 4"));
        Product child = productDAO.create(new Product("COMP4", "Component 4"));

        bomItemDAO.create(new BOMItem(parent1.getId(), child.getId(), new BigDecimal("1")));
        bomItemDAO.create(new BOMItem(parent2.getId(), child.getId(), new BigDecimal("2")));

        List<BOMItem> bomItems = bomItemDAO.findByChildProductId(child.getId());

        assertEquals(2, bomItems.size());
        assertTrue(bomItems.stream().allMatch(b -> b.getChildProductId().equals(child.getId())));
    }

    @Test
    void testFindAll() {
        Product parent = productDAO.create(new Product("ASSY5", "Assembly 5"));
        Product child1 = productDAO.create(new Product("COMP5", "Component 5"));
        Product child2 = productDAO.create(new Product("COMP6", "Component 6"));

        bomItemDAO.create(new BOMItem(parent.getId(), child1.getId(), new BigDecimal("1")));
        bomItemDAO.create(new BOMItem(parent.getId(), child2.getId(), new BigDecimal("1")));

        List<BOMItem> all = bomItemDAO.findAll();

        assertEquals(2, all.size());
    }

    @Test
    void testUpdateBOMItem() {
        Product parent = productDAO.create(new Product("ASSY6", "Assembly 6"));
        Product child = productDAO.create(new Product("COMP7", "Component 7"));

        BOMItem bomItem = new BOMItem(parent.getId(), child.getId(), new BigDecimal("1"));
        BOMItem created = bomItemDAO.create(bomItem);

        created.setQuantity(new BigDecimal("5"));
        created.setSequenceNumber(10);
        bomItemDAO.update(created);

        BOMItem updated = bomItemDAO.findById(created.getId());
        assertEquals(0, new BigDecimal("5").compareTo(updated.getQuantity()));
        assertEquals(10, updated.getSequenceNumber());
    }

    @Test
    void testDeleteBOMItem() {
        Product parent = productDAO.create(new Product("ASSY7", "Assembly 7"));
        Product child = productDAO.create(new Product("COMP8", "Component 8"));

        BOMItem bomItem = bomItemDAO.create(new BOMItem(parent.getId(), child.getId(), new BigDecimal("1")));

        bomItemDAO.delete(bomItem.getId());

        BOMItem deleted = bomItemDAO.findById(bomItem.getId());
        assertNull(deleted);
    }

    @Test
    void testDeleteByParentProductId() {
        Product parent = productDAO.create(new Product("ASSY8", "Assembly 8"));
        Product child1 = productDAO.create(new Product("COMP9", "Component 9"));
        Product child2 = productDAO.create(new Product("COMP10", "Component 10"));

        bomItemDAO.create(new BOMItem(parent.getId(), child1.getId(), new BigDecimal("1")));
        bomItemDAO.create(new BOMItem(parent.getId(), child2.getId(), new BigDecimal("1")));

        bomItemDAO.deleteByParentProductId(parent.getId());

        List<BOMItem> remaining = bomItemDAO.findByParentProductId(parent.getId());
        assertEquals(0, remaining.size());
    }

    @Test
    void testCount() {
        assertEquals(0, bomItemDAO.count());

        Product parent = productDAO.create(new Product("ASSY9", "Assembly 9"));
        Product child1 = productDAO.create(new Product("COMP11", "Component 11"));
        Product child2 = productDAO.create(new Product("COMP12", "Component 12"));

        bomItemDAO.create(new BOMItem(parent.getId(), child1.getId(), new BigDecimal("1")));
        bomItemDAO.create(new BOMItem(parent.getId(), child2.getId(), new BigDecimal("1")));

        assertEquals(2, bomItemDAO.count());
    }
}
