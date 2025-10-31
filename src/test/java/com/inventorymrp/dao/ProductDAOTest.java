package com.inventorymrp.dao;

import com.inventorymrp.model.Product;
import com.inventorymrp.util.DatabaseUtil;
import org.junit.jupiter.api.*;
import org.sql2o.Sql2o;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ProductDAO.
 */
class ProductDAOTest {
    private static Sql2o sql2o;
    private ProductDAO productDAO;

    @BeforeAll
    static void setupDatabase() {
        // Use in-memory H2 database for testing
        sql2o = DatabaseUtil.getSql2o("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1", "sa", "");
        DatabaseUtil.initializeDatabase();
    }

    @BeforeEach
    void setUp() {
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
    void testCreateProduct() {
        Product product = new Product("P001", "Test Product");
        product.setDescription("Test description");
        product.setUnit("pcs");
        product.setUnitCost(new BigDecimal("10.50"));
        product.setStockQuantity(100);
        product.setReorderLevel(20);
        product.setLeadTimeDays(5);
        product.setIsAssembly(false);

        Product created = productDAO.create(product);

        assertNotNull(created.getId());
        assertEquals("P001", created.getCode());
        assertEquals("Test Product", created.getName());
    }

    @Test
    void testFindById() {
        Product product = new Product("P002", "Find Test");
        Product created = productDAO.create(product);

        Product found = productDAO.findById(created.getId());

        assertNotNull(found);
        assertEquals(created.getId(), found.getId());
        assertEquals("P002", found.getCode());
    }

    @Test
    void testFindByCode() {
        Product product = new Product("P003", "Code Test");
        productDAO.create(product);

        Product found = productDAO.findByCode("P003");

        assertNotNull(found);
        assertEquals("P003", found.getCode());
        assertEquals("Code Test", found.getName());
    }

    @Test
    void testFindAll() {
        productDAO.create(new Product("P001", "Product 1"));
        productDAO.create(new Product("P002", "Product 2"));
        productDAO.create(new Product("P003", "Product 3"));

        List<Product> products = productDAO.findAll();

        assertEquals(3, products.size());
    }

    @Test
    void testFindAssemblies() {
        Product assembly1 = new Product("A001", "Assembly 1");
        assembly1.setIsAssembly(true);
        productDAO.create(assembly1);

        Product component = new Product("C001", "Component 1");
        component.setIsAssembly(false);
        productDAO.create(component);

        Product assembly2 = new Product("A002", "Assembly 2");
        assembly2.setIsAssembly(true);
        productDAO.create(assembly2);

        List<Product> assemblies = productDAO.findAssemblies();

        assertEquals(2, assemblies.size());
        assertTrue(assemblies.stream().allMatch(Product::getIsAssembly));
    }

    @Test
    void testFindComponents() {
        Product assembly = new Product("A001", "Assembly 1");
        assembly.setIsAssembly(true);
        productDAO.create(assembly);

        Product component1 = new Product("C001", "Component 1");
        component1.setIsAssembly(false);
        productDAO.create(component1);

        Product component2 = new Product("C002", "Component 2");
        component2.setIsAssembly(false);
        productDAO.create(component2);

        List<Product> components = productDAO.findComponents();

        assertEquals(2, components.size());
        assertTrue(components.stream().noneMatch(Product::getIsAssembly));
    }

    @Test
    void testUpdateProduct() {
        Product product = new Product("P004", "Update Test");
        Product created = productDAO.create(product);

        created.setName("Updated Name");
        created.setStockQuantity(50);
        productDAO.update(created);

        Product updated = productDAO.findById(created.getId());

        assertEquals("Updated Name", updated.getName());
        assertEquals(50, updated.getStockQuantity());
    }

    @Test
    void testUpdateStockQuantity() {
        Product product = new Product("P005", "Stock Test");
        product.setStockQuantity(100);
        Product created = productDAO.create(product);

        productDAO.updateStockQuantity(created.getId(), 75);

        Product updated = productDAO.findById(created.getId());
        assertEquals(75, updated.getStockQuantity());
    }

    @Test
    void testDeleteProduct() {
        Product product = new Product("P006", "Delete Test");
        Product created = productDAO.create(product);

        productDAO.delete(created.getId());

        Product deleted = productDAO.findById(created.getId());
        assertNull(deleted);
    }

    @Test
    void testCount() {
        assertEquals(0, productDAO.count());

        productDAO.create(new Product("P001", "Product 1"));
        productDAO.create(new Product("P002", "Product 2"));

        assertEquals(2, productDAO.count());
    }
}
