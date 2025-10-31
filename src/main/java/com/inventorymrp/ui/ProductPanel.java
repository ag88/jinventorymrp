package com.inventorymrp.ui;

import com.inventorymrp.dao.ProductDAO;
import com.inventorymrp.model.Product;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * Panel for managing products.
 */
public class ProductPanel extends JPanel {
    private final ProductDAO productDAO;
    private JTable productTable;
    private DefaultTableModel tableModel;
    
    public ProductPanel() {
        this.productDAO = new ProductDAO();
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Create table
        String[] columns = {"ID", "Code", "Name", "Unit", "Cost", "Stock", "Reorder Level", "Lead Time", "Is Assembly"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        productTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(productTable);
        
        // Create button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addButton = new JButton("Add Product");
        JButton editButton = new JButton("Edit Product");
        JButton deleteButton = new JButton("Delete Product");
        JButton refreshButton = new JButton("Refresh");
        
        addButton.addActionListener(e -> addProduct());
        editButton.addActionListener(e -> editProduct());
        deleteButton.addActionListener(e -> deleteProduct());
        refreshButton.addActionListener(e -> loadProducts());
        
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(refreshButton);
        
        add(buttonPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        
        loadProducts();
    }
    
    private void loadProducts() {
        tableModel.setRowCount(0);
        List<Product> products = productDAO.findAll();
        for (Product p : products) {
            Object[] row = {
                p.getId(),
                p.getCode(),
                p.getName(),
                p.getUnit(),
                p.getUnitCost(),
                p.getStockQuantity(),
                p.getReorderLevel(),
                p.getLeadTimeDays(),
                p.getIsAssembly() ? "Yes" : "No"
            };
            tableModel.addRow(row);
        }
    }
    
    private void addProduct() {
        JTextField codeField = new JTextField(20);
        JTextField nameField = new JTextField(20);
        JTextField descField = new JTextField(20);
        JTextField unitField = new JTextField(10);
        JTextField costField = new JTextField(10);
        JTextField stockField = new JTextField(10);
        JTextField reorderField = new JTextField(10);
        JTextField leadTimeField = new JTextField(10);
        JCheckBox isAssemblyCheck = new JCheckBox();
        
        JPanel panel = new JPanel(new GridLayout(9, 2, 5, 5));
        panel.add(new JLabel("Code:"));
        panel.add(codeField);
        panel.add(new JLabel("Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Description:"));
        panel.add(descField);
        panel.add(new JLabel("Unit:"));
        panel.add(unitField);
        panel.add(new JLabel("Unit Cost:"));
        panel.add(costField);
        panel.add(new JLabel("Stock Quantity:"));
        panel.add(stockField);
        panel.add(new JLabel("Reorder Level:"));
        panel.add(reorderField);
        panel.add(new JLabel("Lead Time (days):"));
        panel.add(leadTimeField);
        panel.add(new JLabel("Is Assembly:"));
        panel.add(isAssemblyCheck);
        
        int result = JOptionPane.showConfirmDialog(this, panel, "Add Product", 
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            try {
                Product product = new Product();
                product.setCode(codeField.getText());
                product.setName(nameField.getText());
                product.setDescription(descField.getText());
                product.setUnit(unitField.getText());
                if (!costField.getText().isEmpty()) {
                    product.setUnitCost(new BigDecimal(costField.getText()));
                }
                product.setStockQuantity(stockField.getText().isEmpty() ? 0 : Integer.parseInt(stockField.getText()));
                product.setReorderLevel(reorderField.getText().isEmpty() ? 0 : Integer.parseInt(reorderField.getText()));
                product.setLeadTimeDays(leadTimeField.getText().isEmpty() ? 0 : Integer.parseInt(leadTimeField.getText()));
                product.setIsAssembly(isAssemblyCheck.isSelected());
                
                productDAO.create(product);
                loadProducts();
                JOptionPane.showMessageDialog(this, "Product added successfully!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void editProduct() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a product to edit.");
            return;
        }
        
        Long productId = (Long) tableModel.getValueAt(selectedRow, 0);
        Product product = productDAO.findById(productId);
        
        if (product == null) {
            JOptionPane.showMessageDialog(this, "Product not found.");
            return;
        }
        
        JTextField codeField = new JTextField(product.getCode(), 20);
        JTextField nameField = new JTextField(product.getName(), 20);
        JTextField descField = new JTextField(product.getDescription() != null ? product.getDescription() : "", 20);
        JTextField unitField = new JTextField(product.getUnit() != null ? product.getUnit() : "", 10);
        JTextField costField = new JTextField(product.getUnitCost() != null ? product.getUnitCost().toString() : "", 10);
        JTextField stockField = new JTextField(String.valueOf(product.getStockQuantity()), 10);
        JTextField reorderField = new JTextField(String.valueOf(product.getReorderLevel()), 10);
        JTextField leadTimeField = new JTextField(String.valueOf(product.getLeadTimeDays()), 10);
        JCheckBox isAssemblyCheck = new JCheckBox("", product.getIsAssembly());
        
        JPanel panel = new JPanel(new GridLayout(9, 2, 5, 5));
        panel.add(new JLabel("Code:"));
        panel.add(codeField);
        panel.add(new JLabel("Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Description:"));
        panel.add(descField);
        panel.add(new JLabel("Unit:"));
        panel.add(unitField);
        panel.add(new JLabel("Unit Cost:"));
        panel.add(costField);
        panel.add(new JLabel("Stock Quantity:"));
        panel.add(stockField);
        panel.add(new JLabel("Reorder Level:"));
        panel.add(reorderField);
        panel.add(new JLabel("Lead Time (days):"));
        panel.add(leadTimeField);
        panel.add(new JLabel("Is Assembly:"));
        panel.add(isAssemblyCheck);
        
        int result = JOptionPane.showConfirmDialog(this, panel, "Edit Product", 
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            try {
                product.setCode(codeField.getText());
                product.setName(nameField.getText());
                product.setDescription(descField.getText());
                product.setUnit(unitField.getText());
                if (!costField.getText().isEmpty()) {
                    product.setUnitCost(new BigDecimal(costField.getText()));
                }
                product.setStockQuantity(Integer.parseInt(stockField.getText()));
                product.setReorderLevel(Integer.parseInt(reorderField.getText()));
                product.setLeadTimeDays(Integer.parseInt(leadTimeField.getText()));
                product.setIsAssembly(isAssemblyCheck.isSelected());
                
                productDAO.update(product);
                loadProducts();
                JOptionPane.showMessageDialog(this, "Product updated successfully!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void deleteProduct() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a product to delete.");
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to delete this product?", 
            "Confirm Delete", 
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                Long productId = (Long) tableModel.getValueAt(selectedRow, 0);
                productDAO.delete(productId);
                loadProducts();
                JOptionPane.showMessageDialog(this, "Product deleted successfully!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
