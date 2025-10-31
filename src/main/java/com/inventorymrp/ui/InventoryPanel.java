package com.inventorymrp.ui;

import com.inventorymrp.dao.InventoryTransactionDAO;
import com.inventorymrp.dao.ProductDAO;
import com.inventorymrp.model.InventoryTransaction;
import com.inventorymrp.model.Product;
import com.inventorymrp.service.InventoryService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Panel for managing inventory transactions.
 */
public class InventoryPanel extends JPanel {
    private final InventoryTransactionDAO transactionDAO;
    private final ProductDAO productDAO;
    private final InventoryService inventoryService;
    private JTable transactionTable;
    private DefaultTableModel tableModel;
    
    public InventoryPanel() {
        this.transactionDAO = new InventoryTransactionDAO();
        this.productDAO = new ProductDAO();
        this.inventoryService = new InventoryService();
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Create table
        String[] columns = {"ID", "Product", "Type", "Quantity", "Reference", "Date"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        transactionTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(transactionTable);
        
        // Create button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addStockButton = new JButton("Add Stock");
        JButton removeStockButton = new JButton("Remove Stock");
        JButton adjustStockButton = new JButton("Adjust Stock");
        JButton refreshButton = new JButton("Refresh");
        
        addStockButton.addActionListener(e -> addStock());
        removeStockButton.addActionListener(e -> removeStock());
        adjustStockButton.addActionListener(e -> adjustStock());
        refreshButton.addActionListener(e -> loadTransactions());
        
        buttonPanel.add(addStockButton);
        buttonPanel.add(removeStockButton);
        buttonPanel.add(adjustStockButton);
        buttonPanel.add(refreshButton);
        
        add(buttonPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        
        loadTransactions();
    }
    
    private void loadTransactions() {
        tableModel.setRowCount(0);
        List<InventoryTransaction> transactions = transactionDAO.findAll();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        
        for (InventoryTransaction t : transactions) {
            Product product = productDAO.findById(t.getProductId());
            String productInfo = product != null ? product.getCode() + " - " + product.getName() : "Unknown";
            
            Object[] row = {
                t.getId(),
                productInfo,
                t.getTransactionType(),
                t.getQuantity(),
                t.getReference(),
                t.getTransactionDate().format(formatter)
            };
            tableModel.addRow(row);
        }
    }
    
    private void addStock() {
        List<Product> products = productDAO.findAll();
        if (products.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No products found. Please create a product first.");
            return;
        }
        
        JComboBox<String> productCombo = new JComboBox<>();
        for (Product p : products) {
            productCombo.addItem(p.getId() + " - " + p.getCode() + " - " + p.getName());
        }
        
        JTextField quantityField = new JTextField(10);
        JTextField referenceField = new JTextField(20);
        
        JPanel panel = new JPanel(new GridLayout(3, 2, 5, 5));
        panel.add(new JLabel("Product:"));
        panel.add(productCombo);
        panel.add(new JLabel("Quantity:"));
        panel.add(quantityField);
        panel.add(new JLabel("Reference:"));
        panel.add(referenceField);
        
        int result = JOptionPane.showConfirmDialog(this, panel, "Add Stock", 
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            try {
                String productStr = (String) productCombo.getSelectedItem();
                Long productId = Long.parseLong(productStr.split(" - ")[0]);
                Integer quantity = Integer.parseInt(quantityField.getText());
                String reference = referenceField.getText();
                
                inventoryService.addStock(productId, quantity, reference);
                loadTransactions();
                JOptionPane.showMessageDialog(this, "Stock added successfully!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void removeStock() {
        List<Product> products = productDAO.findAll();
        if (products.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No products found.");
            return;
        }
        
        JComboBox<String> productCombo = new JComboBox<>();
        for (Product p : products) {
            productCombo.addItem(p.getId() + " - " + p.getCode() + " - " + p.getName() + " (Stock: " + p.getStockQuantity() + ")");
        }
        
        JTextField quantityField = new JTextField(10);
        JTextField referenceField = new JTextField(20);
        
        JPanel panel = new JPanel(new GridLayout(3, 2, 5, 5));
        panel.add(new JLabel("Product:"));
        panel.add(productCombo);
        panel.add(new JLabel("Quantity:"));
        panel.add(quantityField);
        panel.add(new JLabel("Reference:"));
        panel.add(referenceField);
        
        int result = JOptionPane.showConfirmDialog(this, panel, "Remove Stock", 
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            try {
                String productStr = (String) productCombo.getSelectedItem();
                Long productId = Long.parseLong(productStr.split(" - ")[0]);
                Integer quantity = Integer.parseInt(quantityField.getText());
                String reference = referenceField.getText();
                
                inventoryService.removeStock(productId, quantity, reference);
                loadTransactions();
                JOptionPane.showMessageDialog(this, "Stock removed successfully!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void adjustStock() {
        List<Product> products = productDAO.findAll();
        if (products.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No products found.");
            return;
        }
        
        JComboBox<String> productCombo = new JComboBox<>();
        for (Product p : products) {
            productCombo.addItem(p.getId() + " - " + p.getCode() + " - " + p.getName() + " (Current: " + p.getStockQuantity() + ")");
        }
        
        JTextField newQuantityField = new JTextField(10);
        JTextField referenceField = new JTextField(20);
        
        JPanel panel = new JPanel(new GridLayout(3, 2, 5, 5));
        panel.add(new JLabel("Product:"));
        panel.add(productCombo);
        panel.add(new JLabel("New Quantity:"));
        panel.add(newQuantityField);
        panel.add(new JLabel("Reference:"));
        panel.add(referenceField);
        
        int result = JOptionPane.showConfirmDialog(this, panel, "Adjust Stock", 
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            try {
                String productStr = (String) productCombo.getSelectedItem();
                Long productId = Long.parseLong(productStr.split(" - ")[0]);
                Integer newQuantity = Integer.parseInt(newQuantityField.getText());
                String reference = referenceField.getText();
                
                inventoryService.adjustStock(productId, newQuantity, reference);
                loadTransactions();
                JOptionPane.showMessageDialog(this, "Stock adjusted successfully!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
