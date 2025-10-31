package com.inventorymrp.service;

import com.inventorymrp.dao.InventoryTransactionDAO;
import com.inventorymrp.dao.ProductDAO;
import com.inventorymrp.model.Product;
import com.inventorymrp.util.DatabaseUtil;
import org.junit.jupiter.api.*;
import org.sql2o.Sql2o;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for InventoryService.
 */
class InventoryServiceTest {
    private static Sql2o sql2o;
    private InventoryService inventoryService;
    private ProductDAO productDAO;

    @BeforeAll
    static void setupDatabase() {
        sql2o = DatabaseUtil.getSql2o("jdbc:h2:mem:testdb4;DB_CLOSE_DELAY=-1", "sa", "");
        DatabaseUtil.initializeDatabase("jdbc:h2:mem:testdb4;DB_CLOSE_DELAY=-1", "sa", "");
    }

    @BeforeEach
    void setUp() {
        productDAO = new ProductDAO(sql2o);
        InventoryTransactionDAO transactionDAO = new InventoryTransactionDAO(sql2o);
        inventoryService = new InventoryService(productDAO, transactionDAO);

        // Clean up before each test
        try (org.sql2o.Connection con = sql2o.open()) {
            con.createQuery("DELETE FROM bom_items").executeUpdate();
            con.createQuery("DELETE FROM inventory_transactions").executeUpdate();
            con.createQuery("DELETE FROM purchase_orders").executeUpdate();
            con.createQuery("DELETE FROM products").executeUpdate();
        }
    }

    @Test
    void testAddStock() {
        Product product = new Product("P001", "Test Product");
        product.setStockQuantity(10);
        product = productDAO.create(product);

        inventoryService.addStock(product.getId(), 25, "Receive from supplier");

        Product updated = productDAO.findById(product.getId());
        assertEquals(35, updated.getStockQuantity()); // 10 + 25
    }

    @Test
    void testRemoveStock() {
        Product product = new Product("P002", "Test Product 2");
        product.setStockQuantity(50);
        product = productDAO.create(product);

        inventoryService.removeStock(product.getId(), 20, "Production consumption");

        Product updated = productDAO.findById(product.getId());
        assertEquals(30, updated.getStockQuantity()); // 50 - 20
    }

    @Test
    void testRemoveStock_InsufficientStock() {
        Product product = new Product("P003", "Test Product 3");
        product.setStockQuantity(10);
        product = productDAO.create(product);
        final Long productId = product.getId();

        assertThrows(IllegalStateException.class, () -> {
            inventoryService.removeStock(productId, 20, "Try to remove too much");
        });

        // Stock should remain unchanged
        Product unchanged = productDAO.findById(productId);
        assertEquals(10, unchanged.getStockQuantity());
    }

    @Test
    void testAdjustStock() {
        Product product = new Product("P004", "Test Product 4");
        product.setStockQuantity(50);
        product = productDAO.create(product);

        inventoryService.adjustStock(product.getId(), 75, "Cycle count adjustment");

        Product updated = productDAO.findById(product.getId());
        assertEquals(75, updated.getStockQuantity());
    }

    @Test
    void testAdjustStock_Decrease() {
        Product product = new Product("P005", "Test Product 5");
        product.setStockQuantity(100);
        product = productDAO.create(product);

        inventoryService.adjustStock(product.getId(), 60, "Inventory correction");

        Product updated = productDAO.findById(product.getId());
        assertEquals(60, updated.getStockQuantity());
    }

    @Test
    void testIsBelowReorderLevel_True() {
        Product product = new Product("P006", "Test Product 6");
        product.setStockQuantity(5);
        product.setReorderLevel(10);
        product = productDAO.create(product);

        assertTrue(inventoryService.isBelowReorderLevel(product.getId()));
    }

    @Test
    void testIsBelowReorderLevel_False() {
        Product product = new Product("P007", "Test Product 7");
        product.setStockQuantity(50);
        product.setReorderLevel(10);
        product = productDAO.create(product);

        assertFalse(inventoryService.isBelowReorderLevel(product.getId()));
    }

    @Test
    void testIsBelowReorderLevel_AtReorderLevel() {
        Product product = new Product("P008", "Test Product 8");
        product.setStockQuantity(10);
        product.setReorderLevel(10);
        product = productDAO.create(product);

        assertFalse(inventoryService.isBelowReorderLevel(product.getId()));
    }

    @Test
    void testAddStock_NonexistentProduct() {
        assertThrows(IllegalArgumentException.class, () -> {
            inventoryService.addStock(999L, 10, "Invalid product");
        });
    }

    @Test
    void testRemoveStock_NonexistentProduct() {
        assertThrows(IllegalArgumentException.class, () -> {
            inventoryService.removeStock(999L, 10, "Invalid product");
        });
    }

    @Test
    void testAdjustStock_NonexistentProduct() {
        assertThrows(IllegalArgumentException.class, () -> {
            inventoryService.adjustStock(999L, 10, "Invalid product");
        });
    }
}
