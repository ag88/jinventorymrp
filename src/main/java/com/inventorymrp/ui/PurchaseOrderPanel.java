package com.inventorymrp.ui;

import com.inventorymrp.dao.ProductDAO;
import com.inventorymrp.dao.PurchaseOrderDAO;
import com.inventorymrp.model.Product;
import com.inventorymrp.model.PurchaseOrder;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Panel for managing purchase orders.
 */
public class PurchaseOrderPanel extends JPanel {
    private final PurchaseOrderDAO purchaseOrderDAO;
    private final ProductDAO productDAO;
    private JTable poTable;
    private DefaultTableModel tableModel;
    
    public PurchaseOrderPanel() {
        this.purchaseOrderDAO = new PurchaseOrderDAO();
        this.productDAO = new ProductDAO();
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Create table
        String[] columns = {"ID", "Product", "Quantity", "Status", "Order Date", "Expected Delivery", "Supplier"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        poTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(poTable);
        
        // Create button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addButton = new JButton("Add PO");
        JButton updateStatusButton = new JButton("Update Status");
        JButton deleteButton = new JButton("Delete PO");
        JButton refreshButton = new JButton("Refresh");
        
        addButton.addActionListener(e -> addPurchaseOrder());
        updateStatusButton.addActionListener(e -> updateStatus());
        deleteButton.addActionListener(e -> deletePurchaseOrder());
        refreshButton.addActionListener(e -> loadPurchaseOrders());
        
        buttonPanel.add(addButton);
        buttonPanel.add(updateStatusButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(refreshButton);
        
        add(buttonPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        
        loadPurchaseOrders();
    }
    
    private void loadPurchaseOrders() {
        tableModel.setRowCount(0);
        List<PurchaseOrder> purchaseOrders = purchaseOrderDAO.findAll();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        for (PurchaseOrder po : purchaseOrders) {
            Product product = productDAO.findById(po.getProductId());
            String productInfo = product != null ? product.getCode() + " - " + product.getName() : "Unknown";
            
            Object[] row = {
                po.getId(),
                productInfo,
                po.getQuantity(),
                po.getStatus(),
                po.getOrderDate().format(formatter),
                po.getExpectedDeliveryDate() != null ? po.getExpectedDeliveryDate().format(formatter) : "",
                po.getSupplier() != null ? po.getSupplier() : ""
            };
            tableModel.addRow(row);
        }
    }
    
    private void addPurchaseOrder() {
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
        JTextField supplierField = new JTextField(20);
        JTextField deliveryDateField = new JTextField(LocalDate.now().plusDays(7).toString(), 15);
        JTextField referenceField = new JTextField(20);
        
        JPanel panel = new JPanel(new GridLayout(5, 2, 5, 5));
        panel.add(new JLabel("Product:"));
        panel.add(productCombo);
        panel.add(new JLabel("Quantity:"));
        panel.add(quantityField);
        panel.add(new JLabel("Supplier:"));
        panel.add(supplierField);
        panel.add(new JLabel("Expected Delivery (YYYY-MM-DD):"));
        panel.add(deliveryDateField);
        panel.add(new JLabel("Reference:"));
        panel.add(referenceField);
        
        int result = JOptionPane.showConfirmDialog(this, panel, "Add Purchase Order", 
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            try {
                String productStr = (String) productCombo.getSelectedItem();
                Long productId = Long.parseLong(productStr.split(" - ")[0]);
                Integer quantity = Integer.parseInt(quantityField.getText());
                LocalDate deliveryDate = LocalDate.parse(deliveryDateField.getText());
                
                PurchaseOrder po = new PurchaseOrder(productId, quantity, deliveryDate);
                po.setSupplier(supplierField.getText());
                po.setReference(referenceField.getText());
                
                purchaseOrderDAO.create(po);
                loadPurchaseOrders();
                JOptionPane.showMessageDialog(this, "Purchase Order added successfully!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void updateStatus() {
        int selectedRow = poTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a purchase order to update.");
            return;
        }
        
        Long poId = (Long) tableModel.getValueAt(selectedRow, 0);
        PurchaseOrder po = purchaseOrderDAO.findById(poId);
        
        if (po == null) {
            JOptionPane.showMessageDialog(this, "Purchase order not found.");
            return;
        }
        
        String[] statuses = {"PENDING", "ORDERED", "RECEIVED", "CANCELLED"};
        String newStatus = (String) JOptionPane.showInputDialog(this, 
            "Select new status:", 
            "Update Status",
            JOptionPane.QUESTION_MESSAGE, 
            null, 
            statuses, 
            po.getStatus());
        
        if (newStatus != null) {
            try {
                po.setStatus(newStatus);
                purchaseOrderDAO.update(po);
                loadPurchaseOrders();
                JOptionPane.showMessageDialog(this, "Status updated successfully!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void deletePurchaseOrder() {
        int selectedRow = poTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a purchase order to delete.");
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to delete this purchase order?", 
            "Confirm Delete", 
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                Long poId = (Long) tableModel.getValueAt(selectedRow, 0);
                purchaseOrderDAO.delete(poId);
                loadPurchaseOrders();
                JOptionPane.showMessageDialog(this, "Purchase Order deleted successfully!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
