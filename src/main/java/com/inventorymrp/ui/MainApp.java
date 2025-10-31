package com.inventorymrp.ui;

import com.inventorymrp.util.DatabaseUtil;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import java.awt.*;
import java.nio.MappedByteBuffer;

/**
 * Main application entry point and window.
 */
public class MainApp extends JFrame {
	
	MRPPanel mrppanel;
    
    public MainApp() {
        setTitle("Inventory Management with MRP");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1024, 768);
        setLocationRelativeTo(null);
        
        // Initialize database
        try {
            DatabaseUtil.initializeDatabase();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Failed to initialize database: " + e.getMessage(),
                "Database Error", 
                JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
        
        // Create menu bar
        createMenuBar();
        
        // Create tabbed pane
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // Add tabs
        tabbedPane.addTab("Products", new ProductPanel());
        tabbedPane.addTab("BOM", new BOMPanel());
        tabbedPane.addTab("Inventory", new InventoryPanel());
        mrppanel = new MRPPanel();
        tabbedPane.addTab("MRP", mrppanel);
        tabbedPane.addTab("Purchase Orders", new PurchaseOrderPanel());
        
        tabbedPane.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				JTabbedPane tabbedpane = (JTabbedPane) e.getSource();
				//System.out.println(tabbedpane.getSelectedIndex());
				if (tabbedpane.getSelectedIndex() == 3) { // MRP
					mrppanel.loadProducts();
				}
			}
		});
        
        add(tabbedPane, BorderLayout.CENTER);
        
        // Status bar
        JLabel statusBar = new JLabel("Ready");
        statusBar.setBorder(BorderFactory.createEtchedBorder());
        add(statusBar, BorderLayout.SOUTH);
    }
    
    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        
        // File menu
        JMenu fileMenu = new JMenu("File");
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exitItem);
        
        // Help menu
        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e -> 
            JOptionPane.showMessageDialog(this, 
                "Inventory Management with MRP\nVersion 1.0\n\nManages inventory and material requirements planning.",
                "About", 
                JOptionPane.INFORMATION_MESSAGE));
        helpMenu.add(aboutItem);
        
        menuBar.add(fileMenu);
        menuBar.add(helpMenu);
        
        setJMenuBar(menuBar);
    }
    
    public static void main(String[] args) {
        // Set look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Create and show the application
        SwingUtilities.invokeLater(() -> {
            MainApp app = new MainApp();
            app.setVisible(true);
        });
    }
}
