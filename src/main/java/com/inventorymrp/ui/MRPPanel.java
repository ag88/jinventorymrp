package com.inventorymrp.ui;

import com.inventorymrp.dao.ProductDAO;
import com.inventorymrp.model.Product;
import com.inventorymrp.service.MRPService;
import com.inventorymrp.service.MRPService.MaterialAvailability;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;

/**
 * Panel for MRP (Material Requirements Planning).
 */
public class MRPPanel extends JPanel {
    private final MRPService mrpService;
    private final ProductDAO productDAO;
    private JTable resultsTable;
    private DefaultTableModel tableModel;
    private JLabel leadTimeValueLabel;
    
    public MRPPanel() {
        this.mrpService = new MRPService();
        this.productDAO = new ProductDAO();
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Create top panel for input
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        inputPanel.add(new JLabel("Select Product:"));
        
        JComboBox<String> productCombo = new JComboBox<>();
        inputPanel.add(productCombo);
        
        inputPanel.add(new JLabel("Demand Quantity:"));
        JTextField quantityField = new JTextField(10);
        inputPanel.add(quantityField);
        
        JButton calculateButton = new JButton("Calculate Requirements");
        inputPanel.add(calculateButton);
        
        JButton checkAvailabilityButton = new JButton("Check Availability");
        inputPanel.add(checkAvailabilityButton);
        
        JButton generatePOButton = new JButton("Generate Purchase Orders");
        inputPanel.add(generatePOButton);
        
        // Create table for results
        String[] columns = {"Product Code", "Product Name", "Required Qty", "Available Qty", "Shortage", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        resultsTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(resultsTable);
        
        // Create bottom panel for lead time display
        JPanel leadTimePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leadTimePanel.add(new JLabel("Lead Time:"));
        leadTimeValueLabel = new JLabel("N/A");
        leadTimePanel.add(leadTimeValueLabel);
        
        add(inputPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(leadTimePanel, BorderLayout.SOUTH);
        
        // Load products
        loadProducts(productCombo);
        
        // Button actions
        calculateButton.addActionListener(e -> calculateRequirements(productCombo, quantityField));
        checkAvailabilityButton.addActionListener(e -> checkAvailability(productCombo, quantityField));
        generatePOButton.addActionListener(e -> generatePurchaseOrders(productCombo, quantityField));
    }
    
    private void loadProducts(JComboBox<String> productCombo) {
        productCombo.removeAllItems();
        List<Product> products = productDAO.findAll();
        for (Product p : products) {
            productCombo.addItem(p.getId() + " - " + p.getCode() + " - " + p.getName());
        }
    }
    
    private void calculateRequirements(JComboBox<String> productCombo, JTextField quantityField) {
        try {
            String selectedStr = (String) productCombo.getSelectedItem();
            if (selectedStr == null) {
                JOptionPane.showMessageDialog(this, "Please select a product.");
                return;
            }
            
            Long productId = Long.parseLong(selectedStr.split(" - ")[0]);
            Integer quantity = Integer.parseInt(quantityField.getText());
            
            Map<Long, Integer> requirements = mrpService.calculateMaterialRequirements(productId, quantity);
            
            // Calculate and display lead time
            double leadTime = mrpService.calculateLeadTime(productId, quantity);
            leadTimeValueLabel.setText(String.format("%.2f days", leadTime));
            
            tableModel.setRowCount(0);
            for (Map.Entry<Long, Integer> entry : requirements.entrySet()) {
                Product product = productDAO.findById(entry.getKey());
                if (product != null) {
                    Object[] row = {
                        product.getCode(),
                        product.getName(),
                        entry.getValue(),
                        product.getStockQuantity(),
                        Math.max(0, entry.getValue() - product.getStockQuantity()),
                        product.getStockQuantity() >= entry.getValue() ? "OK" : "SHORTAGE"
                    };
                    tableModel.addRow(row);
                }
            }
            
            if (requirements.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No material requirements found.");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void checkAvailability(JComboBox<String> productCombo, JTextField quantityField) {
        try {
            String selectedStr = (String) productCombo.getSelectedItem();
            if (selectedStr == null) {
                JOptionPane.showMessageDialog(this, "Please select a product.");
                return;
            }
            
            Long productId = Long.parseLong(selectedStr.split(" - ")[0]);
            Integer quantity = Integer.parseInt(quantityField.getText());
            
            Map<Long, MaterialAvailability> availability = mrpService.checkMaterialAvailability(productId, quantity);
            
            tableModel.setRowCount(0);
            boolean allAvailable = true;
            for (MaterialAvailability avail : availability.values()) {
                Object[] row = {
                    avail.getProductCode(),
                    avail.getProductName(),
                    avail.getRequiredQuantity(),
                    avail.getAvailableQuantity(),
                    avail.getShortage(),
                    avail.getSufficient() ? "SUFFICIENT" : "INSUFFICIENT"
                };
                tableModel.addRow(row);
                if (!avail.getSufficient()) {
                    allAvailable = false;
                }
            }
            
            if (allAvailable) {
                JOptionPane.showMessageDialog(this, "All materials are available!", 
                    "Availability Check", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Some materials are insufficient. Check the table for details.", 
                    "Availability Check", JOptionPane.WARNING_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void generatePurchaseOrders(JComboBox<String> productCombo, JTextField quantityField) {
        try {
            String selectedStr = (String) productCombo.getSelectedItem();
            if (selectedStr == null) {
                JOptionPane.showMessageDialog(this, "Please select a product.");
                return;
            }
            
            Long productId = Long.parseLong(selectedStr.split(" - ")[0]);
            Integer quantity = Integer.parseInt(quantityField.getText());
            
            var purchaseOrders = mrpService.generatePurchaseOrders(productId, quantity);
            
            if (purchaseOrders.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No purchase orders needed. All materials are in stock!");
                return;
            }
            
            // Save purchase orders
            for (var po : purchaseOrders) {
                // Note: PO is created in the service but we could save it here
            }
            
            StringBuilder message = new StringBuilder("Generated " + purchaseOrders.size() + " purchase order(s):\n\n");
            for (var po : purchaseOrders) {
                Product product = productDAO.findById(po.getProductId());
                message.append("Product: ").append(product.getCode()).append(" - ")
                       .append(product.getName()).append("\n");
                message.append("Quantity: ").append(po.getQuantity()).append("\n");
                message.append("Expected Delivery: ").append(po.getExpectedDeliveryDate()).append("\n\n");
            }
            
            JOptionPane.showMessageDialog(this, message.toString(), 
                "Purchase Orders Generated", JOptionPane.INFORMATION_MESSAGE);
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
