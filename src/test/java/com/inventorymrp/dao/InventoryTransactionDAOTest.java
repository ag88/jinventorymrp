package com.inventorymrp.dao;

import com.inventorymrp.model.InventoryTransaction;
import com.inventorymrp.model.Product;
import com.inventorymrp.util.DatabaseUtil;
import org.junit.jupiter.api.*;
import org.sql2o.Sql2o;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for InventoryTransactionDAO.
 */
class InventoryTransactionDAOTest {
    private static Sql2o sql2o;
    private InventoryTransactionDAO transactionDAO;
    private ProductDAO productDAO;
    private Long testProductId;

    @BeforeAll
    static void setupDatabase() {
        sql2o = DatabaseUtil.getSql2o("jdbc:h2:mem:testdb5;DB_CLOSE_DELAY=-1", "sa", "");
        DatabaseUtil.initializeDatabase("jdbc:h2:mem:testdb5;DB_CLOSE_DELAY=-1", "sa", "");
    }

    @BeforeEach
    void setUp() {
        transactionDAO = new InventoryTransactionDAO(sql2o);
        productDAO = new ProductDAO(sql2o);

        // Clean up before each test
        try (org.sql2o.Connection con = sql2o.open()) {
            con.createQuery("DELETE FROM inventory_transactions").executeUpdate();
            con.createQuery("DELETE FROM products").executeUpdate();
        }

        // Create a test product
        Product product = new Product("TEST001", "Test Product");
        product.setStockQuantity(0);
        product = productDAO.create(product);
        testProductId = product.getId();
    }

    @Test
    void testCreateAndFindById() {
        InventoryTransaction transaction = new InventoryTransaction(testProductId, "IN", 100);
        transaction.setReference("Test Reference");

        InventoryTransaction created = transactionDAO.create(transaction);
        assertNotNull(created.getId());

        InventoryTransaction found = transactionDAO.findById(created.getId());
        assertNotNull(found);
        assertEquals(testProductId, found.getProductId());
        assertEquals("IN", found.getTransactionType());
        assertEquals(100, found.getQuantity());
        assertEquals("Test Reference", found.getReference());
        assertNotNull(found.getTransactionDate());
        assertNotNull(found.getCreatedAt());
    }

    @Test
    void testFindAll() {
        // Create multiple transactions
        transactionDAO.create(new InventoryTransaction(testProductId, "IN", 50));
        transactionDAO.create(new InventoryTransaction(testProductId, "OUT", 20));
        transactionDAO.create(new InventoryTransaction(testProductId, "ADJUSTMENT", 10));

        List<InventoryTransaction> all = transactionDAO.findAll();
        assertEquals(3, all.size());
        
        // Verify all transactions have LocalDateTime fields properly populated
        for (InventoryTransaction t : all) {
            assertNotNull(t.getTransactionDate(), "Transaction date should not be null");
            assertNotNull(t.getCreatedAt(), "Created at should not be null");
        }
    }

    @Test
    void testFindByProductId() {
        // Create a second product
        Product product2 = new Product("TEST002", "Test Product 2");
        product2 = productDAO.create(product2);

        // Create transactions for both products
        transactionDAO.create(new InventoryTransaction(testProductId, "IN", 50));
        transactionDAO.create(new InventoryTransaction(testProductId, "OUT", 20));
        transactionDAO.create(new InventoryTransaction(product2.getId(), "IN", 30));

        List<InventoryTransaction> forProduct1 = transactionDAO.findByProductId(testProductId);
        assertEquals(2, forProduct1.size());

        List<InventoryTransaction> forProduct2 = transactionDAO.findByProductId(product2.getId());
        assertEquals(1, forProduct2.size());
    }

    @Test
    void testFindByType() {
        transactionDAO.create(new InventoryTransaction(testProductId, "IN", 50));
        transactionDAO.create(new InventoryTransaction(testProductId, "IN", 30));
        transactionDAO.create(new InventoryTransaction(testProductId, "OUT", 20));

        List<InventoryTransaction> inTransactions = transactionDAO.findByType("IN");
        assertEquals(2, inTransactions.size());

        List<InventoryTransaction> outTransactions = transactionDAO.findByType("OUT");
        assertEquals(1, outTransactions.size());
    }

    @Test
    void testDelete() {
        InventoryTransaction transaction = transactionDAO.create(
            new InventoryTransaction(testProductId, "IN", 100)
        );
        Long id = transaction.getId();

        assertNotNull(transactionDAO.findById(id));

        transactionDAO.delete(id);

        assertNull(transactionDAO.findById(id));
    }

    @Test
    void testCount() {
        assertEquals(0, transactionDAO.count());

        transactionDAO.create(new InventoryTransaction(testProductId, "IN", 50));
        assertEquals(1, transactionDAO.count());

        transactionDAO.create(new InventoryTransaction(testProductId, "OUT", 20));
        assertEquals(2, transactionDAO.count());
    }
}
