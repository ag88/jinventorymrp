package com.inventorymrp.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * PurchaseOrder represents an order to procure materials.
 */
public class PurchaseOrder {
    private Long id;
    private Long productId;
    private Integer quantity;
    private String status; // "PENDING", "ORDERED", "RECEIVED", "CANCELLED"
    private LocalDate orderDate;
    private LocalDate expectedDeliveryDate;
    private String supplier;
    private String reference;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public PurchaseOrder() {
        this.status = "PENDING";
        this.orderDate = LocalDate.now();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public PurchaseOrder(Long productId, Integer quantity, LocalDate expectedDeliveryDate) {
        this();
        this.productId = productId;
        this.quantity = quantity;
        this.expectedDeliveryDate = expectedDeliveryDate;
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

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDate getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDate orderDate) {
        this.orderDate = orderDate;
    }

    public LocalDate getExpectedDeliveryDate() {
        return expectedDeliveryDate;
    }

    public void setExpectedDeliveryDate(LocalDate expectedDeliveryDate) {
        this.expectedDeliveryDate = expectedDeliveryDate;
    }

    public String getSupplier() {
        return supplier;
    }

    public void setSupplier(String supplier) {
        this.supplier = supplier;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "PurchaseOrder{" +
                "id=" + id +
                ", productId=" + productId +
                ", quantity=" + quantity +
                ", status='" + status + '\'' +
                ", expectedDeliveryDate=" + expectedDeliveryDate +
                '}';
    }
}
