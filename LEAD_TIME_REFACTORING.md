# Lead Time Refactoring Documentation

## Overview
This document describes the lead time computation refactoring implemented in the jinventorymrp application.

## Changes Summary

### 1. Product Model Changes
**Before:**
- Single field: `leadTimeDays` (Integer)

**After:**
- Two fields:
  - `orderLeadTime` (Double) - Fixed time for order processing
  - `itemLeadTime` (Double) - Variable time per item produced

### 2. Database Schema Changes
**Migration:** V5__Refactor_product_lead_time.sql

The migration performs the following:
1. Adds `order_lead_time` DOUBLE column (default 0.0)
2. Adds `item_lead_time` DOUBLE column (default 0.0)
3. Migrates existing `lead_time_days` data to `order_lead_time`
4. Drops the old `lead_time_days` column

### 3. Lead Time Calculation Formula
```
Total Lead Time = orderLeadTime + (quantity × itemLeadTime)
```

**Examples:**
- Product with orderLeadTime=5.0, itemLeadTime=0.5
  - Quantity 1: 5.0 + (1 × 0.5) = 5.5 days
  - Quantity 10: 5.0 + (10 × 0.5) = 10.0 days
  - Quantity 20: 5.0 + (20 × 0.5) = 15.0 days

### 4. GUI Changes

#### ProductPanel
- **Table Columns:** Added separate columns for "Order Lead Time" and "Item Lead Time"
- **Add/Edit Dialog:** Two separate input fields instead of one
  - "Order Lead Time (days):" - accepts decimal values
  - "Item Lead Time (days/item):" - accepts decimal values

#### MRPPanel
- **New Feature:** Lead time display below the results table
- **Label:** Shows "Lead Time: X.XX days"
- **Calculation:** Triggered when "Calculate Requirements" button is clicked
- **Display:** Formatted to 2 decimal places

### 5. API Changes

#### New Method in MRPService
```java
public double calculateLeadTime(Long productId, Integer demandQuantity)
```

**Parameters:**
- `productId`: The ID of the product
- `demandQuantity`: The quantity to produce

**Returns:** The calculated lead time in days (as a double)

### 6. Test Coverage
All existing tests have been updated and new tests added:
- ProductTest: Updated to test new fields
- ProductDAOTest: Updated to use new field names
- MRPServiceTest: Added tests for calculateLeadTime method
- **Total Tests:** 53 tests (all passing)

### 7. Backward Compatibility
The migration ensures backward compatibility:
- Existing `lead_time_days` values are automatically migrated to `order_lead_time`
- New `item_lead_time` defaults to 0.0, maintaining previous behavior if not set
- Purchase orders continue to work with rounded lead time values

## Usage Guide

### Creating a Product with Lead Times
```java
Product product = new Product("PROD-001", "Example Product");
product.setOrderLeadTime(5.0);      // 5 days for order processing
product.setItemLeadTime(0.5);       // 0.5 days per item produced
productDAO.create(product);
```

### Calculating Lead Time
```java
MRPService mrpService = new MRPService();
double leadTime = mrpService.calculateLeadTime(productId, 10);
// Returns: 5.0 + (10 * 0.5) = 10.0 days
```

### UI Interaction
1. Navigate to the **Products** tab
2. Click **Add Product** or **Edit Product**
3. Enter values for both lead time fields
4. Navigate to the **MRP** tab
5. Select a product and enter demand quantity
6. Click **Calculate Requirements**
7. The lead time will be displayed below the table

## Benefits
1. **More Accurate Modeling:** Separates fixed and variable lead time components
2. **Flexibility:** Supports different production scenarios (e.g., setup time + per-unit time)
3. **Better Planning:** Enables more precise delivery date predictions
4. **Scalability:** Lead time scales appropriately with quantity

## Migration Notes
- The migration is idempotent and safe to run multiple times
- Existing data is preserved during the upgrade
- No manual intervention required for existing installations
- Database version updated from V4 to V5

## Security
✅ CodeQL security scan completed with 0 vulnerabilities
