package com.inventorymrp.service;

import com.inventorymrp.dao.InventoryTransactionDAO;
import com.inventorymrp.dao.ProductDAO;
import com.inventorymrp.model.InventoryTransaction;
import com.inventorymrp.model.Product;

import java.time.LocalDateTime;

/**
 * Service for managing inventory operations.
 */
public class InventoryService {
    private final ProductDAO productDAO;
    private final InventoryTransactionDAO transactionDAO;

    public InventoryService() {
        this.productDAO = new ProductDAO();
        this.transactionDAO = new InventoryTransactionDAO();
    }

    public InventoryService(ProductDAO productDAO, InventoryTransactionDAO transactionDAO) {
        this.productDAO = productDAO;
        this.transactionDAO = transactionDAO;
    }

    /**
     * Add stock to inventory (e.g., after receiving goods).
     */
    public void addStock(Long productId, Integer quantity, String reference) {
        Product product = productDAO.findById(productId);
        if (product == null) {
            throw new IllegalArgumentException("Product not found: " + productId);
        }

        // Update stock quantity
        Integer newQuantity = product.getStockQuantity() + quantity;
        productDAO.updateStockQuantity(productId, newQuantity);

        // Record transaction
        InventoryTransaction transaction = new InventoryTransaction(productId, "IN", quantity);
        transaction.setReference(reference);
        transactionDAO.create(transaction);
    }

    /**
     * Remove stock from inventory (e.g., after production consumption).
     */
    public void removeStock(Long productId, Integer quantity, String reference) {
        Product product = productDAO.findById(productId);
        if (product == null) {
            throw new IllegalArgumentException("Product not found: " + productId);
        }

        if (product.getStockQuantity() < quantity) {
            throw new IllegalStateException("Insufficient stock for product: " + product.getCode());
        }

        // Update stock quantity
        Integer newQuantity = product.getStockQuantity() - quantity;
        productDAO.updateStockQuantity(productId, newQuantity);

        // Record transaction
        InventoryTransaction transaction = new InventoryTransaction(productId, "OUT", quantity);
        transaction.setReference(reference);
        transactionDAO.create(transaction);
    }

    /**
     * Adjust stock (e.g., for corrections or cycle counts).
     */
    public void adjustStock(Long productId, Integer newQuantity, String reference) {
        Product product = productDAO.findById(productId);
        if (product == null) {
            throw new IllegalArgumentException("Product not found: " + productId);
        }

        Integer difference = newQuantity - product.getStockQuantity();
        
        // Update stock quantity
        productDAO.updateStockQuantity(productId, newQuantity);

        // Record transaction
        InventoryTransaction transaction = new InventoryTransaction(productId, "ADJUSTMENT", difference);
        transaction.setReference(reference);
        transactionDAO.create(transaction);
    }

    /**
     * Check if product is below reorder level.
     */
    public boolean isBelowReorderLevel(Long productId) {
        Product product = productDAO.findById(productId);
        if (product == null) {
            return false;
        }
        return product.getStockQuantity() < product.getReorderLevel();
    }
}
