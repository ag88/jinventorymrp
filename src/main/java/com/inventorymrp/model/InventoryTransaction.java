package com.inventorymrp.model;

import java.time.LocalDateTime;

/**
 * InventoryTransaction records stock movements.
 */
public class InventoryTransaction {
    private Long id;
    private Long productId;
    private String transactionType; // "IN", "OUT", "ADJUSTMENT"
    private Integer quantity;
    private String reference; // Reference number or note
    private LocalDateTime transactionDate;
    private LocalDateTime createdAt;

    public InventoryTransaction() {
        this.transactionDate = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
    }

    public InventoryTransaction(Long productId, String transactionType, Integer quantity) {
        this();
        this.productId = productId;
        this.transactionType = transactionType;
        this.quantity = quantity;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDateTime transactionDate) {
        this.transactionDate = transactionDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "InventoryTransaction{" +
                "id=" + id +
                ", productId=" + productId +
                ", transactionType='" + transactionType + '\'' +
                ", quantity=" + quantity +
                ", transactionDate=" + transactionDate +
                '}';
    }
}
