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


### 2. Lead Time Calculation Formula

#### 2a. Per BOM item lead time calculation
```
Lead Time = orderLeadTime + (quantity × itemLeadTime)
```

**Examples:**
- Product with orderLeadTime=5.0, itemLeadTime=0.5
  - Quantity 1: 5.0 + (1 × 0.5) = 5.5 days
  - Quantity 10: 5.0 + (10 × 0.5) = 10.0 days
  - Quantity 20: 5.0 + (20 × 0.5) = 15.0 days

#### 2b. Total lead time for full BOM

Lead time calculation for the full BOM of a product is done by recursively evaluating the
longest lead time for each of the parts, then adding up to the total lead time 

### 3. GUI Changes

#### ProductPanel
- **Table Columns:** Added separate columns for "Order Lead Time" and "Item Lead Time"
- **Add/Edit Dialog:** Two separate input fields instead of one
  - "Order Lead Time (days):" - accepts decimal values
  - "Item Lead Time (days/item):" - accepts decimal values

#### MRPPanel
- **New Feature:** Lead time display below the results table
- **New Feature:** added exclude existing stocks checkbox
- **Label:** Shows "Lead Time: X.XX days"
- **Calculation:** Triggered when "Calculate Requirements" button is clicked
- **Display:** Formatted to 2 decimal places

### 4. API Changes

#### New Methods in MRPService
```java
/**
 * Calculate total lead time for a product based on demand quantity. Lead time =
 * orderLeadTime + (number of items * itemLeadTime) This applies to all
 * products, providing flexibility for different production scenarios.
 * @param exclstock exclude existing stocks while calculating the lead time
 */
public double calculateLeadTime(Long productId, Integer demandQuantity, boolean exclstock)

/**
 * Calculate total lead time for a product by recursively considering the longest 
 * lead time of the parts, calls {@link calculateLeadTime} for each part
 * and summing up the longest lead time in addition to itself
 * @param exclstock exclude existing stocks while calculating the lead time
 */
public double calculateLeadTimeRecursive(Long productId, Integer quantity, boolean exclstock)
```

**Parameters:**
- `productId`: The ID of the product
- `demandQuantity / quantity`: The quantity to produce
- `exclstock`: exclude existing stocks while calculating the lead time

**Returns:** The calculated lead time in days (as a double)

### 5. Test Coverage
All existing tests have been updated and new tests added:
- ProductTest: Updated to test new fields
- ProductDAOTest: Updated to use new field names
- MRPServiceTest: Added tests for calculateLeadTime method
- **Total Tests:** 53 tests (all passing)


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
double leadTime = mrpService.calculateLeadTime(productId, 10, false);
// Returns: 5.0 + (10 * 0.5) = 10.0 days
double leadTime = mrpService.calculateLeadTimeRecursive(productId, 10, false);
// calculate the lead time of the product by summing up all the parts / bom item 
// considering the longest lead time for parallel parts
```

### UI Interaction
1. Navigate to the **Products** tab
2. Click **Add Product** or **Edit Product**
3. Enter values for both lead time fields
4. Navigate to the **MRP** tab
5. Select a product and enter demand quantity
6. Check **Exclude existing stock** (optional)
7. Click **Calculate Requirements**
8. The lead time will be displayed below the table

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
