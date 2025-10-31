package com.inventorymrp.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * BOMItem (Bill of Materials Item) represents a component in an assembly.
 * Each BOMItem links to a parent product (the assembly) and a child product (the component).
 */
public class BOMItem {
    private Long id;
    private Long parentProductId; // The assembly product
    private Long childProductId;  // The component product
    private BigDecimal quantity;  // Quantity of child needed per parent
    private String unit;          // Unit of measurement
    private Integer sequenceNumber; // Order in the BOM
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Transient fields for convenience (not stored in DB)
    private Product parentProduct;
    private Product childProduct;

    public BOMItem() {
        this.quantity = BigDecimal.ONE;
        this.sequenceNumber = 0;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public BOMItem(Long parentProductId, Long childProductId, BigDecimal quantity) {
        this();
        this.parentProductId = parentProductId;
        this.childProductId = childProductId;
        this.quantity = quantity;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getParentProductId() {
        return parentProductId;
    }

    public void setParentProductId(Long parentProductId) {
        this.parentProductId = parentProductId;
    }

    public Long getChildProductId() {
        return childProductId;
    }

    public void setChildProductId(Long childProductId) {
        this.childProductId = childProductId;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public Integer getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(Integer sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
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

    public Product getParentProduct() {
        return parentProduct;
    }

    public void setParentProduct(Product parentProduct) {
        this.parentProduct = parentProduct;
    }

    public Product getChildProduct() {
        return childProduct;
    }

    public void setChildProduct(Product childProduct) {
        this.childProduct = childProduct;
    }

    @Override
    public String toString() {
        return "BOMItem{" +
                "id=" + id +
                ", parentProductId=" + parentProductId +
                ", childProductId=" + childProductId +
                ", quantity=" + quantity +
                '}';
    }
}
