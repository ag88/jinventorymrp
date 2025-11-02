package com.inventorymrp.service;

import com.inventorymrp.dao.BOMItemDAO;
import com.inventorymrp.dao.ProductDAO;
import com.inventorymrp.dao.PurchaseOrderDAO;
import com.inventorymrp.model.BOMItem;
import com.inventorymrp.model.Product;
import com.inventorymrp.model.PurchaseOrder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

/**
 * MRP (Material Requirements Planning) Service. Calculates material
 * requirements based on production demand and BOM.
 */
public class MRPService {
	private final ProductDAO productDAO;
	private final BOMItemDAO bomItemDAO;
	private final PurchaseOrderDAO purchaseOrderDAO;

	public MRPService() {
		this.productDAO = new ProductDAO();
		this.bomItemDAO = new BOMItemDAO();
		this.purchaseOrderDAO = new PurchaseOrderDAO();
	}

	public MRPService(ProductDAO productDAO, BOMItemDAO bomItemDAO, PurchaseOrderDAO purchaseOrderDAO) {
		this.productDAO = productDAO;
		this.bomItemDAO = bomItemDAO;
		this.purchaseOrderDAO = purchaseOrderDAO;
	}

	/**
	 * Calculate material requirements for producing a given quantity of a product.
	 * This is the main MRP calculation that determines dependent demand.
	 */
	public Map<Long, Integer> calculateMaterialRequirements(Long productId, Integer demandQuantity) {
		Map<Long, Integer> requirements = new HashMap<>();
		calculateRequirementsRecursive(productId, demandQuantity, requirements);
		return requirements;
	}

	/**
	 * Recursive method to calculate requirements through the BOM hierarchy.
	 */
	private void calculateRequirementsRecursive(Long productId, Integer quantity, Map<Long, Integer> requirements) {
		Product product = productDAO.findById(productId);
		if (product == null) {
			return;
		}

		// If this product is an assembly, calculate requirements for its components
		if (product.getIsAssembly()) {
			List<BOMItem> bomItems = bomItemDAO.findByParentProductId(productId);
			for (BOMItem bomItem : bomItems) {
				// Calculate required quantity of this component
				int requiredQty = bomItem.getQuantity().multiply(new BigDecimal(quantity)).intValue();

				Long childId = bomItem.getChildProductId();

				// Recursively calculate for sub-assemblies or add to requirements for
				// components
				calculateRequirementsRecursive(childId, requiredQty, requirements);
			}
		} //else {
		
		// For non-assembly items (leaf components), add to requirements
		// edit: add original product assembly itself into bom items 
		requirements.put(productId, requirements.getOrDefault(productId, 0) + quantity);

	}

	/**
	 * Generate purchase orders based on material requirements and current stock.
	 */
	public List<PurchaseOrder> generatePurchaseOrders(Long productId, Integer demandQuantity) {
		List<PurchaseOrder> purchaseOrders = new ArrayList<>();
		Map<Long, Integer> requirements = calculateMaterialRequirements(productId, demandQuantity);

		for (Map.Entry<Long, Integer> entry : requirements.entrySet()) {
			Long materialId = entry.getKey();
			Integer requiredQty = entry.getValue();

			Product material = productDAO.findById(materialId);
			if (material == null) {
				continue;
			}

			if (material.getNonPurchase())
				continue;
			
			// Calculate net requirement (required - available stock)
			Integer netRequirement = requiredQty - material.getStockQuantity();

			if (netRequirement > 0) {
				// Create purchase order
				//long leadTimeDays = material.getOrderLeadTime() != null ? Math.round(material.getOrderLeadTime()) : 0;
				long leadTimeDays = (long) calculateLeadTimeRecursive(material.getId(), netRequirement, true);
				LocalDate expectedDelivery = LocalDate.now().plusDays(leadTimeDays);
				PurchaseOrder po = new PurchaseOrder(materialId, netRequirement, expectedDelivery);
				po.setReference("MRP-" + productId + "-" + System.currentTimeMillis());
				purchaseOrders.add(po);
			}
		}

		return purchaseOrders;
	}

	/**
	 * Check if sufficient materials are available to produce the demanded quantity.
	 */
	public Map<Long, MaterialAvailability> checkMaterialAvailability(Long productId, Integer demandQuantity) {
		Map<Long, MaterialAvailability> availability = new HashMap<>();
		Map<Long, Integer> requirements = calculateMaterialRequirements(productId, demandQuantity);

		for (Map.Entry<Long, Integer> entry : requirements.entrySet()) {
			Long materialId = entry.getKey();
			Integer requiredQty = entry.getValue();

			Product material = productDAO.findById(materialId);
			if (material == null) {
				continue;
			}

			MaterialAvailability avail = new MaterialAvailability();
			avail.setProductId(materialId);
			avail.setProductCode(material.getCode());
			avail.setProductName(material.getName());
			avail.setRequiredQuantity(requiredQty);
			avail.setAvailableQuantity(material.getStockQuantity());
			avail.setShortage(Math.max(0, requiredQty - material.getStockQuantity()));
			avail.setSufficient(material.getStockQuantity() >= requiredQty);

			availability.put(materialId, avail);
		}

		return availability;
	}

	/**
	 * Calculate total lead time for a product based on demand quantity. Lead time =
	 * orderLeadTime + (number of items * itemLeadTime) This applies to all
	 * products, providing flexibility for different production scenarios.
	 * @param exclstock exclude existing stocks while calculating the lead time
	 */
	public double calculateLeadTime(Long productId, Integer quantity, boolean exclstock) {
		Product product = productDAO.findById(productId);
		if (product == null) {
			return 0.0;
		}

		double orderLeadTime = product.getOrderLeadTime() != null ? product.getOrderLeadTime() : 0.0;
		double itemLeadTime = product.getItemLeadTime() != null ? product.getItemLeadTime() : 0.0;
		
		if (exclstock) {
			// BOM item lead time = orderLeadTime + number of items x itemLeadTime
			return orderLeadTime + (quantity * itemLeadTime);
		} else {
			if (product.getStockQuantity() >= quantity )
				return 0.0;
			else
				return orderLeadTime + ((quantity - product.getStockQuantity()) * itemLeadTime);
		}
	}

	/**
	 * Calculate total lead time for a product by recursively considering the longest 
	 * lead time of the parts, calls {@link calculateLeadTime} for each part
	 * and summing up the longest lead time in addition to itself
	 * @param exclstock exclude existing stocks while calculating the lead time
	 */
	public double calculateLeadTimeRecursive(Long productId, Integer quantity, boolean exclstock) { 

		Product product = productDAO.findById(productId);
		if (product == null) {
			return 0.0;
		}

		// If this product is an assembly, calculate lead time for its components
		double longestChildLeadTime = 0.0;
		if (product.getIsAssembly()) {
			List<BOMItem> bomItems = bomItemDAO.findByParentProductId(productId);			
			for (BOMItem bomItem : bomItems) {
				// Calculate required quantity of this component
				int requiredQty = bomItem.getQuantity().multiply(new BigDecimal(quantity))
						.intValue();

				Long childId = bomItem.getChildProductId();

				// Recursively calculate for sub-assemblies or add to requirements for components
				double childLeadTime = calculateLeadTimeRecursive(childId, requiredQty, exclstock);
				if (childLeadTime > longestChildLeadTime)
					longestChildLeadTime = childLeadTime;
			}
		}
		
		double leadtime = calculateLeadTime(productId, quantity, exclstock) + longestChildLeadTime;
		return leadtime;
		
	}


    /**
     * Inner class to represent material availability status.
     */
    public static class MaterialAvailability {
        private Long productId;
        private String productCode;
        private String productName;
        private Integer requiredQuantity;
        private Integer availableQuantity;
        private Integer shortage;
        private Boolean sufficient;

        // Getters and Setters
        public Long getProductId() {
            return productId;
        }

        public void setProductId(Long productId) {
            this.productId = productId;
        }

        public String getProductCode() {
            return productCode;
        }

        public void setProductCode(String productCode) {
            this.productCode = productCode;
        }

        public String getProductName() {
            return productName;
        }

        public void setProductName(String productName) {
            this.productName = productName;
        }

        public Integer getRequiredQuantity() {
            return requiredQuantity;
        }

        public void setRequiredQuantity(Integer requiredQuantity) {
            this.requiredQuantity = requiredQuantity;
        }

        public Integer getAvailableQuantity() {
            return availableQuantity;
        }

        public void setAvailableQuantity(Integer availableQuantity) {
            this.availableQuantity = availableQuantity;
        }

        public Integer getShortage() {
            return shortage;
        }

        public void setShortage(Integer shortage) {
            this.shortage = shortage;
        }

        public Boolean getSufficient() {
            return sufficient;
        }

        public void setSufficient(Boolean sufficient) {
            this.sufficient = sufficient;
        }

        @Override
        public String toString() {
            return "MaterialAvailability{" +
                    "productCode='" + productCode + '\'' +
                    ", productName='" + productName + '\'' +
                    ", requiredQuantity=" + requiredQuantity +
                    ", availableQuantity=" + availableQuantity +
                    ", shortage=" + shortage +
                    ", sufficient=" + sufficient +
                    '}';
        }
    }
}
