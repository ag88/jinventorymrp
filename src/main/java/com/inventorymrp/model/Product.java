package com.inventorymrp.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Product model representing an item in the inventory.
 * Can be either a finished product or a component.
 */
public class Product {
    private Long id;
    private String code;
    private String name;
    private String description;
    private String unit; // e.g., "pcs", "kg", "m"
    private BigDecimal unitCost;
    private Integer stockQuantity;
    private Integer reorderLevel;
    private Double orderLeadTime;
    private Double itemLeadTime;
    private Boolean isAssembly; // true if this product has a BOM
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Product() {
        this.stockQuantity = 0;
        this.reorderLevel = 0;
        this.orderLeadTime = 0.0;
        this.itemLeadTime = 0.0;
        this.isAssembly = false;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Product(String code, String name) {
        this();
        this.code = code;
        this.name = name;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public BigDecimal getUnitCost() {
        return unitCost;
    }

    public void setUnitCost(BigDecimal unitCost) {
        this.unitCost = unitCost;
    }

    public Integer getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(Integer stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public Integer getReorderLevel() {
        return reorderLevel;
    }

    public void setReorderLevel(Integer reorderLevel) {
        this.reorderLevel = reorderLevel;
    }

    public Double getOrderLeadTime() {
        return orderLeadTime;
    }

    public void setOrderLeadTime(Double orderLeadTime) {
        this.orderLeadTime = orderLeadTime;
    }

    public Double getItemLeadTime() {
        return itemLeadTime;
    }

    public void setItemLeadTime(Double itemLeadTime) {
        this.itemLeadTime = itemLeadTime;
    }

    public Boolean getIsAssembly() {
        return isAssembly;
    }

    public void setIsAssembly(Boolean isAssembly) {
        this.isAssembly = isAssembly;
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
        return "Product{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", stockQuantity=" + stockQuantity +
                ", isAssembly=" + isAssembly +
                '}';
    }
}
