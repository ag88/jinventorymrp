package com.inventorymrp.model;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Product model.
 */
class ProductTest {

    @Test
    void testProductCreation() {
        Product product = new Product("P001", "Test Product");
        
        assertNotNull(product);
        assertEquals("P001", product.getCode());
        assertEquals("Test Product", product.getName());
        assertEquals(0, product.getStockQuantity());
        assertEquals(0, product.getReorderLevel());
        assertEquals(0.0, product.getOrderLeadTime());
        assertEquals(0.0, product.getItemLeadTime());
        assertFalse(product.getIsAssembly());
    }

    @Test
    void testProductSetters() {
        Product product = new Product();
        product.setCode("P002");
        product.setName("Assembly Product");
        product.setDescription("Test assembly");
        product.setUnit("pcs");
        product.setUnitCost(new BigDecimal("100.50"));
        product.setStockQuantity(50);
        product.setReorderLevel(10);
        product.setOrderLeadTime(5.0);
        product.setItemLeadTime(0.5);
        product.setIsAssembly(true);
        
        assertEquals("P002", product.getCode());
        assertEquals("Assembly Product", product.getName());
        assertEquals("Test assembly", product.getDescription());
        assertEquals("pcs", product.getUnit());
        assertEquals(new BigDecimal("100.50"), product.getUnitCost());
        assertEquals(50, product.getStockQuantity());
        assertEquals(10, product.getReorderLevel());
        assertEquals(5.0, product.getOrderLeadTime());
        assertEquals(0.5, product.getItemLeadTime());
        assertTrue(product.getIsAssembly());
    }

    @Test
    void testProductToString() {
        Product product = new Product("P003", "Component");
        product.setId(1L);
        product.setStockQuantity(100);
        
        String result = product.toString();
        assertTrue(result.contains("P003"));
        assertTrue(result.contains("Component"));
        assertTrue(result.contains("100"));
    }
}
