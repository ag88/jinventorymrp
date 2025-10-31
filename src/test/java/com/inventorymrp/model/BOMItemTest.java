package com.inventorymrp.model;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for BOMItem model.
 */
class BOMItemTest {

    @Test
    void testBOMItemCreation() {
        BOMItem bomItem = new BOMItem(1L, 2L, new BigDecimal("2.5"));
        
        assertNotNull(bomItem);
        assertEquals(1L, bomItem.getParentProductId());
        assertEquals(2L, bomItem.getChildProductId());
        assertEquals(new BigDecimal("2.5"), bomItem.getQuantity());
        assertEquals(0, bomItem.getSequenceNumber());
    }

    @Test
    void testBOMItemSetters() {
        BOMItem bomItem = new BOMItem();
        bomItem.setParentProductId(10L);
        bomItem.setChildProductId(20L);
        bomItem.setQuantity(new BigDecimal("3.0"));
        bomItem.setUnit("kg");
        bomItem.setSequenceNumber(1);
        
        assertEquals(10L, bomItem.getParentProductId());
        assertEquals(20L, bomItem.getChildProductId());
        assertEquals(new BigDecimal("3.0"), bomItem.getQuantity());
        assertEquals("kg", bomItem.getUnit());
        assertEquals(1, bomItem.getSequenceNumber());
    }

    @Test
    void testBOMItemWithProducts() {
        BOMItem bomItem = new BOMItem();
        Product parent = new Product("PARENT", "Parent Product");
        parent.setId(1L);
        Product child = new Product("CHILD", "Child Product");
        child.setId(2L);
        
        bomItem.setParentProduct(parent);
        bomItem.setChildProduct(child);
        
        assertNotNull(bomItem.getParentProduct());
        assertNotNull(bomItem.getChildProduct());
        assertEquals("PARENT", bomItem.getParentProduct().getCode());
        assertEquals("CHILD", bomItem.getChildProduct().getCode());
    }

    @Test
    void testBOMItemToString() {
        BOMItem bomItem = new BOMItem(1L, 2L, new BigDecimal("5"));
        bomItem.setId(100L);
        
        String result = bomItem.toString();
        assertTrue(result.contains("100"));
        assertTrue(result.contains("1"));
        assertTrue(result.contains("2"));
    }
}
