package com.inventorymrp.ui;

import com.inventorymrp.dao.BOMItemDAO;
import com.inventorymrp.dao.ProductDAO;
import com.inventorymrp.model.BOMItem;
import com.inventorymrp.model.Product;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * Panel for managing Bill of Materials.
 */
public class BOMPanel extends JPanel {
    private final BOMItemDAO bomItemDAO;
    private final ProductDAO productDAO;
    private JTable bomTable;
    private DefaultTableModel tableModel;
    
    public BOMPanel() {
        this.bomItemDAO = new BOMItemDAO();
        this.productDAO = new ProductDAO();
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Create table
        String[] columns = {"ID", "Parent Product", "Child Product", "Quantity", "Unit", "Sequence"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        bomTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(bomTable);
        
        // Create button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addButton = new JButton("Add BOM Item");
        JButton deleteButton = new JButton("Delete BOM Item");
        JButton refreshButton = new JButton("Refresh");
        JButton viewByProductButton = new JButton("View by Product");
        
        addButton.addActionListener(e -> addBOMItem());
        deleteButton.addActionListener(e -> deleteBOMItem());
        refreshButton.addActionListener(e -> loadBOMItems());
        viewByProductButton.addActionListener(e -> viewByProduct());
        
        buttonPanel.add(addButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(viewByProductButton);
        buttonPanel.add(refreshButton);
        
        add(buttonPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        
        loadBOMItems();
    }
    
    private void loadBOMItems() {
        tableModel.setRowCount(0);
        List<BOMItem> bomItems = bomItemDAO.findAll();
        for (BOMItem item : bomItems) {
            Object[] row = {
                item.getId(),
                item.getParentProduct() != null ? item.getParentProduct().getCode() + " - " + item.getParentProduct().getName() : "",
                item.getChildProduct() != null ? item.getChildProduct().getCode() + " - " + item.getChildProduct().getName() : "",
                item.getQuantity(),
                item.getUnit(),
                item.getSequenceNumber()
            };
            tableModel.addRow(row);
        }
    }
    
    private void addBOMItem() {
        List<Product> assemblies = productDAO.findAssemblies();
        List<Product> allProducts = productDAO.findAll();
        
        if (assemblies.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No assembly products found. Please create an assembly product first.");
            return;
        }
        
        JComboBox<String> parentCombo = new JComboBox<>();
        for (Product p : assemblies) {
            parentCombo.addItem(p.getId() + " - " + p.getCode() + " - " + p.getName());
        }
        
        JComboBox<String> childCombo = new JComboBox<>();
        for (Product p : allProducts) {
            childCombo.addItem(p.getId() + " - " + p.getCode() + " - " + p.getName());
        }
        
        JTextField quantityField = new JTextField("1.0", 10);
        JTextField unitField = new JTextField(10);
        JTextField sequenceField = new JTextField("0", 10);
        
        JPanel panel = new JPanel(new GridLayout(5, 2, 5, 5));
        panel.add(new JLabel("Parent Product (Assembly):"));
        panel.add(parentCombo);
        panel.add(new JLabel("Child Product (Component):"));
        panel.add(childCombo);
        panel.add(new JLabel("Quantity:"));
        panel.add(quantityField);
        panel.add(new JLabel("Unit:"));
        panel.add(unitField);
        panel.add(new JLabel("Sequence:"));
        panel.add(sequenceField);
        
        int result = JOptionPane.showConfirmDialog(this, panel, "Add BOM Item", 
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            try {
                String parentStr = (String) parentCombo.getSelectedItem();
                String childStr = (String) childCombo.getSelectedItem();
                
                Long parentId = Long.parseLong(parentStr.split(" - ")[0]);
                Long childId = Long.parseLong(childStr.split(" - ")[0]);
                
                BOMItem bomItem = new BOMItem();
                bomItem.setParentProductId(parentId);
                bomItem.setChildProductId(childId);
                bomItem.setQuantity(new BigDecimal(quantityField.getText()));
                bomItem.setUnit(unitField.getText());
                bomItem.setSequenceNumber(Integer.parseInt(sequenceField.getText()));
                
                bomItemDAO.create(bomItem);
                loadBOMItems();
                JOptionPane.showMessageDialog(this, "BOM Item added successfully!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void deleteBOMItem() {
        int selectedRow = bomTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a BOM item to delete.");
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to delete this BOM item?", 
            "Confirm Delete", 
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                Long bomItemId = (Long) tableModel.getValueAt(selectedRow, 0);
                bomItemDAO.delete(bomItemId);
                loadBOMItems();
                JOptionPane.showMessageDialog(this, "BOM Item deleted successfully!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void viewByProduct() {
        List<Product> assemblies = productDAO.findAssemblies();
        if (assemblies.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No assembly products found.");
            return;
        }
        
        JComboBox<String> productCombo = new JComboBox<>();
        for (Product p : assemblies) {
            productCombo.addItem(p.getId() + " - " + p.getCode() + " - " + p.getName());
        }
        
        int result = JOptionPane.showConfirmDialog(this, productCombo, "Select Product", 
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            try {
                String selectedStr = (String) productCombo.getSelectedItem();
                Long productId = Long.parseLong(selectedStr.split(" - ")[0]);
                
                tableModel.setRowCount(0);
                List<BOMItem> bomItems = bomItemDAO.findByParentProductId(productId);
                for (BOMItem item : bomItems) {
                    Object[] row = {
                        item.getId(),
                        item.getParentProduct() != null ? item.getParentProduct().getCode() + " - " + item.getParentProduct().getName() : "",
                        item.getChildProduct() != null ? item.getChildProduct().getCode() + " - " + item.getChildProduct().getName() : "",
                        item.getQuantity(),
                        item.getUnit(),
                        item.getSequenceNumber()
                    };
                    tableModel.addRow(row);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
