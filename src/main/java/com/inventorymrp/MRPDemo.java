package com.inventorymrp;

import com.inventorymrp.dao.BOMItemDAO;
import com.inventorymrp.dao.ProductDAO;
import com.inventorymrp.model.BOMItem;
import com.inventorymrp.model.Product;
import com.inventorymrp.service.InventoryService;
import com.inventorymrp.service.MRPService;
import com.inventorymrp.util.DatabaseUtil;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Demo application showcasing the MRP functionality.
 */
public class MRPDemo {
    public static void main(String[] args) {
        System.out.println("=== Inventory Management with MRP Demo ===\n");
        
        // Initialize database
        DatabaseUtil.initializeDatabase();
        
        // Initialize services
        ProductDAO productDAO = new ProductDAO();
        BOMItemDAO bomItemDAO = new BOMItemDAO();
        InventoryService inventoryService = new InventoryService();
        MRPService mrpService = new MRPService();
        
        // Create a bicycle assembly example
        System.out.println("Creating products...");
        
        // Assembly product
        Product bike = new Product("BIKE-001", "Mountain Bike");
        bike.setIsAssembly(true);
        bike.setUnit("pcs");
        bike.setStockQuantity(0);
        bike = productDAO.create(bike);
        System.out.println("  Created: " + bike);
        
        // Component products
        Product frame = new Product("FRAME-001", "Bike Frame");
        frame.setUnit("pcs");
        frame.setUnitCost(new BigDecimal("150.00"));
        frame.setStockQuantity(5);
        frame.setOrderLeadTime(10.0);
        frame.setItemLeadTime(0.0);
        frame = productDAO.create(frame);
        System.out.println("  Created: " + frame);
        
        Product wheel = new Product("WHEEL-001", "Bike Wheel");
        wheel.setUnit("pcs");
        wheel.setUnitCost(new BigDecimal("45.00"));
        wheel.setStockQuantity(15);
        wheel.setOrderLeadTime(7.0);
        wheel.setItemLeadTime(0.0);
        wheel = productDAO.create(wheel);
        System.out.println("  Created: " + wheel);
        
        Product chain = new Product("CHAIN-001", "Bike Chain");
        chain.setUnit("pcs");
        chain.setUnitCost(new BigDecimal("25.00"));
        chain.setStockQuantity(8);
        chain.setOrderLeadTime(5.0);
        chain.setItemLeadTime(0.0);
        chain = productDAO.create(chain);
        System.out.println("  Created: " + chain);
        
        // Create BOM for bike
        System.out.println("\nCreating Bill of Materials...");
        BOMItem bom1 = new BOMItem(bike.getId(), frame.getId(), new BigDecimal("1"));
        bom1.setSequenceNumber(1);
        bomItemDAO.create(bom1);
        System.out.println("  BOM: BIKE requires 1 x FRAME");
        
        BOMItem bom2 = new BOMItem(bike.getId(), wheel.getId(), new BigDecimal("2"));
        bom2.setSequenceNumber(2);
        bomItemDAO.create(bom2);
        System.out.println("  BOM: BIKE requires 2 x WHEEL");
        
        BOMItem bom3 = new BOMItem(bike.getId(), chain.getId(), new BigDecimal("1"));
        bom3.setSequenceNumber(3);
        bomItemDAO.create(bom3);
        System.out.println("  BOM: BIKE requires 1 x CHAIN");
        
        // MRP Calculation
        int demandQuantity = 10;
        System.out.println("\n=== MRP Analysis ===");
        System.out.println("Demand: " + demandQuantity + " x BIKE");
        
        // Calculate requirements
        Map<Long, Integer> requirements = mrpService.calculateMaterialRequirements(bike.getId(), demandQuantity);
        System.out.println("\nMaterial Requirements:");
        for (Map.Entry<Long, Integer> entry : requirements.entrySet()) {
            Product product = productDAO.findById(entry.getKey());
            System.out.println("  " + product.getCode() + ": " + entry.getValue() + " " + product.getUnit());
        }
        
        // Check availability
        System.out.println("\nAvailability Check:");
        Map<Long, MRPService.MaterialAvailability> availability = 
            mrpService.checkMaterialAvailability(bike.getId(), demandQuantity);
        
        boolean allAvailable = true;
        for (MRPService.MaterialAvailability avail : availability.values()) {
            String status = avail.getSufficient() ? "OK" : "SHORTAGE";
            System.out.println("  " + avail.getProductCode() + ": ");
            System.out.println("    Required: " + avail.getRequiredQuantity());
            System.out.println("    Available: " + avail.getAvailableQuantity());
            System.out.println("    Shortage: " + avail.getShortage());
            System.out.println("    Status: " + status);
            
            if (!avail.getSufficient()) {
                allAvailable = false;
            }
        }
        
        // Generate purchase orders if needed
        if (!allAvailable) {
            System.out.println("\n=== Purchase Orders ===");
            var purchaseOrders = mrpService.generatePurchaseOrders(bike.getId(), demandQuantity);
            System.out.println("Generated " + purchaseOrders.size() + " purchase order(s):");
            for (var po : purchaseOrders) {
                Product product = productDAO.findById(po.getProductId());
                System.out.println("  PO for " + product.getCode() + ": " + po.getQuantity() + " " + product.getUnit());
                System.out.println("    Expected delivery: " + po.getExpectedDeliveryDate());
            }
        } else {
            System.out.println("\nAll materials are available! Production can proceed.");
        }
        
        System.out.println("\n=== Demo Complete ===");
    }
}
