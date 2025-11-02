package com.inventorymrp.service;

import com.inventorymrp.dao.BOMItemDAO;
import com.inventorymrp.dao.ProductDAO;
import com.inventorymrp.dao.PurchaseOrderDAO;
import com.inventorymrp.model.BOMItem;
import com.inventorymrp.model.Product;
import com.inventorymrp.model.PurchaseOrder;
import com.inventorymrp.service.MRPService.MaterialAvailability;
import com.inventorymrp.util.DatabaseUtil;
import org.junit.jupiter.api.*;
import org.sql2o.Sql2o;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for MRPService - testing Material Requirements Planning logic.
 */
class MRPServiceTest {
    private static Sql2o sql2o;
    private MRPService mrpService;
    private ProductDAO productDAO;
    private BOMItemDAO bomItemDAO;
    private PurchaseOrderDAO purchaseOrderDAO;

    @BeforeAll
    static void setupDatabase() {
        sql2o = DatabaseUtil.getSql2o("jdbc:h2:mem:testdb3;DB_CLOSE_DELAY=-1", "sa", "");
        DatabaseUtil.initializeDatabase("jdbc:h2:mem:testdb3;DB_CLOSE_DELAY=-1", "sa", "");
    }

    @BeforeEach
    void setUp() {
        productDAO = new ProductDAO(sql2o);
        bomItemDAO = new BOMItemDAO(sql2o);
        purchaseOrderDAO = new PurchaseOrderDAO(sql2o);
        mrpService = new MRPService(productDAO, bomItemDAO, purchaseOrderDAO);

        // Clean up before each test
        try (org.sql2o.Connection con = sql2o.open()) {
            con.createQuery("DELETE FROM bom_items").executeUpdate();
            con.createQuery("DELETE FROM inventory_transactions").executeUpdate();
            con.createQuery("DELETE FROM purchase_orders").executeUpdate();
            con.createQuery("DELETE FROM products").executeUpdate();
        }
    }

    @Test
    void testCalculateMaterialRequirements_SimpleComponent() {
        // Create a simple component (no BOM)
        Product component = new Product("COMP1", "Simple Component");
        component.setIsAssembly(false);        
        component = productDAO.create(component);

        Map<Long, Integer> requirements = mrpService.calculateMaterialRequirements(component.getId(), 10);

        assertEquals(1, requirements.size());
        assertEquals(10, requirements.get(component.getId()));
    }

    @Test
    void testCalculateMaterialRequirements_SimpleAssembly() {
        // Create assembly with 2 components
        Product assembly = new Product("ASSY1", "Assembly 1");
        assembly.setIsAssembly(true);
        assembly = productDAO.create(assembly);

        Product comp1 = new Product("COMP1", "Component 1");
        comp1 = productDAO.create(comp1);

        Product comp2 = new Product("COMP2", "Component 2");
        comp2 = productDAO.create(comp2);

        // Assembly requires 2 of comp1 and 3 of comp2
        bomItemDAO.create(new BOMItem(assembly.getId(), comp1.getId(), new BigDecimal("2")));
        bomItemDAO.create(new BOMItem(assembly.getId(), comp2.getId(), new BigDecimal("3")));

        // Calculate requirements to build 5 assemblies
        Map<Long, Integer> requirements = mrpService.calculateMaterialRequirements(assembly.getId(), 5);

        assertEquals(3, requirements.size());
        assertEquals(10, requirements.get(comp1.getId())); // 5 * 2 = 10
        assertEquals(15, requirements.get(comp2.getId())); // 5 * 3 = 15
    }

    @Test
    void testCalculateMaterialRequirements_MultiLevelBOM() {
        // Create a multi-level BOM structure
        // TopAssembly -> SubAssembly -> Component
        Product topAssembly = new Product("TOP", "Top Assembly");
        topAssembly.setIsAssembly(true);
        topAssembly = productDAO.create(topAssembly);

        Product subAssembly = new Product("SUB", "Sub Assembly");
        subAssembly.setIsAssembly(true);
        subAssembly = productDAO.create(subAssembly);

        Product component = new Product("COMP", "Component");
        component = productDAO.create(component);

        // TopAssembly requires 2 SubAssemblies
        bomItemDAO.create(new BOMItem(topAssembly.getId(), subAssembly.getId(), new BigDecimal("2")));
        
        // Each SubAssembly requires 3 Components
        bomItemDAO.create(new BOMItem(subAssembly.getId(), component.getId(), new BigDecimal("3")));

        // Calculate requirements to build 4 TopAssemblies
        Map<Long, Integer> requirements = mrpService.calculateMaterialRequirements(topAssembly.getId(), 4);

        // Should need 4 * 2 * 3 = 24 components
        assertEquals(24, requirements.get(component.getId()));
    }

    @Test
    void testCheckMaterialAvailability_SufficientStock() {
        Product assembly = new Product("ASSY", "Assembly");
        assembly.setIsAssembly(true);
        assembly = productDAO.create(assembly);

        Product comp = new Product("COMP", "Component");
        comp.setStockQuantity(100); // Plenty in stock
        comp = productDAO.create(comp);

        bomItemDAO.create(new BOMItem(assembly.getId(), comp.getId(), new BigDecimal("5")));

        // Check if we can build 10 assemblies (requires 50 components)
        Map<Long, MaterialAvailability> availability = mrpService.checkMaterialAvailability(assembly.getId(), 10);

        MaterialAvailability compAvail = availability.get(comp.getId());
        assertNotNull(compAvail);
        assertEquals(50, compAvail.getRequiredQuantity());
        assertEquals(100, compAvail.getAvailableQuantity());
        assertEquals(0, compAvail.getShortage());
        assertTrue(compAvail.getSufficient());
    }

    @Test
    void testCheckMaterialAvailability_InsufficientStock() {
        Product assembly = new Product("ASSY", "Assembly");
        assembly.setIsAssembly(true);
        assembly = productDAO.create(assembly);

        Product comp = new Product("COMP", "Component");
        comp.setStockQuantity(20); // Only 20 in stock
        comp = productDAO.create(comp);

        bomItemDAO.create(new BOMItem(assembly.getId(), comp.getId(), new BigDecimal("5")));

        // Try to build 10 assemblies (requires 50 components, but only 20 available)
        Map<Long, MaterialAvailability> availability = mrpService.checkMaterialAvailability(assembly.getId(), 10);

        MaterialAvailability compAvail = availability.get(comp.getId());
        assertNotNull(compAvail);
        assertEquals(50, compAvail.getRequiredQuantity());
        assertEquals(20, compAvail.getAvailableQuantity());
        assertEquals(30, compAvail.getShortage()); // Need 30 more
        assertFalse(compAvail.getSufficient());
    }

    @Test
    void testGeneratePurchaseOrders_NoShortage() {
        Product assembly = new Product("ASSY", "Assembly");
        assembly.setIsAssembly(true);
        assembly.setNonPurchase(true);
        assembly = productDAO.create(assembly);

        Product comp = new Product("COMP", "Component");
        comp.setStockQuantity(100); // Sufficient stock
        comp.setOrderLeadTime(5.0);
        comp = productDAO.create(comp);

        bomItemDAO.create(new BOMItem(assembly.getId(), comp.getId(), new BigDecimal("2")));

        // Build 10 assemblies (requires 20 components, have 100)
        List<PurchaseOrder> purchaseOrders = mrpService.generatePurchaseOrders(assembly.getId(), 10);

        // Should not generate any purchase orders since we have enough stock
        assertEquals(0, purchaseOrders.size());
    }

    @Test
    void testGeneratePurchaseOrders_WithShortage() {
        Product assembly = new Product("ASSY", "Assembly");
        assembly.setIsAssembly(true);
        assembly.setNonPurchase(true);
        assembly = productDAO.create(assembly);

        Product comp = new Product("COMP", "Component");
        comp.setStockQuantity(10); // Only 10 in stock
        comp.setOrderLeadTime(7.0);
        comp = productDAO.create(comp);

        bomItemDAO.create(new BOMItem(assembly.getId(), comp.getId(), new BigDecimal("5")));

        // Build 10 assemblies (requires 50 components, have only 10)
        List<PurchaseOrder> purchaseOrders = mrpService.generatePurchaseOrders(assembly.getId(), 10);

        // Should generate a purchase order for 40 components (50 - 10)
        assertEquals(1, purchaseOrders.size());
        PurchaseOrder po = purchaseOrders.get(0);
        assertEquals(comp.getId(), po.getProductId());
        assertEquals(40, po.getQuantity()); // Net requirement
        assertNotNull(po.getExpectedDeliveryDate());
    }

    @Test
    void testGeneratePurchaseOrders_MultipleComponents() {
        Product assembly = new Product("ASSY", "Assembly");
        assembly.setIsAssembly(true);
        assembly.setNonPurchase(true);
        assembly = productDAO.create(assembly);

        Product comp1 = new Product("COMP1", "Component 1");
        comp1.setStockQuantity(5);
        comp1.setOrderLeadTime(5.0);
        comp1 = productDAO.create(comp1);

        Product comp2 = new Product("COMP2", "Component 2");
        comp2.setStockQuantity(100); // Sufficient
        comp2.setOrderLeadTime(3.0);
        comp2 = productDAO.create(comp2);

        bomItemDAO.create(new BOMItem(assembly.getId(), comp1.getId(), new BigDecimal("2")));
        bomItemDAO.create(new BOMItem(assembly.getId(), comp2.getId(), new BigDecimal("1")));

        // Build 10 assemblies
        List<PurchaseOrder> purchaseOrders = mrpService.generatePurchaseOrders(assembly.getId(), 10);

        // Should generate PO only for comp1 (needs 20, has 5, shortage 15)
        assertEquals(1, purchaseOrders.size());
        assertEquals(comp1.getId(), purchaseOrders.get(0).getProductId());
        assertEquals(15, purchaseOrders.get(0).getQuantity());
    }

    @Test
    void testCalculateLeadTime() {
        Product product = new Product("PROD1", "Product 1");
        product.setOrderLeadTime(5.0);
        product.setItemLeadTime(0.5);
        product = productDAO.create(product);

        // Lead time = orderLeadTime + (quantity * itemLeadTime)
        // = 5.0 + (10 * 0.5) = 10.0
        double leadTime = mrpService.calculateLeadTime(product.getId(), 10, false);
        assertEquals(10.0, leadTime, 0.001);
    }

    @Test
    void testCalculateLeadTime_ZeroItemLeadTime() {
        Product product = new Product("PROD2", "Product 2");
        product.setOrderLeadTime(3.0);
        product.setItemLeadTime(0.0);
        product = productDAO.create(product);

        // Lead time = orderLeadTime + (quantity * itemLeadTime)
        // = 3.0 + (20 * 0.0) = 3.0
        double leadTime = mrpService.calculateLeadTime(product.getId(), 20, false);
        assertEquals(3.0, leadTime, 0.001);
    }
}
