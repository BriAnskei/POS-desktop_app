package com.gierza_molases.molases_app.dto.delivery;

import java.util.List;
import java.util.Map;

import com.gierza_molases.molases_app.model.ProductDelivery;

public class ProductDeliveryChanges {

	// Newly added product deliveries
	private List<ProductDelivery> addedProductDelivery;

	// Updated quantities: (productDeliveryId -> newQuantity)
	private Map<Integer, Integer> updatedProductDeliveryQuantity;

	// Removed product delivery IDs
	private List<Integer> removedProductDeliveryIds;

	/** No-args constructor */
	public ProductDeliveryChanges() {
	}

	/** Full constructor */
	public ProductDeliveryChanges(
			List<ProductDelivery> addedProductDelivery,
			Map<Integer, Integer> updatedProductDeliveryQuantity,
			List<Integer> removedProductDeliveryIds
	) {
		this.addedProductDelivery = addedProductDelivery;
		this.updatedProductDeliveryQuantity = updatedProductDeliveryQuantity;
		this.removedProductDeliveryIds = removedProductDeliveryIds;
	}

	// ===== Getters & Setters =====

	public List<ProductDelivery> getAddedProductDelivery() {
		return addedProductDelivery;
	}

	public void setAddedProductDelivery(List<ProductDelivery> addedProductDelivery) {
		this.addedProductDelivery = addedProductDelivery;
	}

	public Map<Integer, Integer> getUpdatedProductDeliveryQuantity() {
		return updatedProductDeliveryQuantity;
	}

	public void setUpdatedProductDeliveryQuantity(
			Map<Integer, Integer> updatedProductDeliveryQuantity) {
		this.updatedProductDeliveryQuantity = updatedProductDeliveryQuantity;
	}

	public List<Integer> getRemovedProductDeliveryIds() {
		return removedProductDeliveryIds;
	}

	public void setRemovedProductDeliveryIds(List<Integer> removedProductDeliveryIds) {
		this.removedProductDeliveryIds = removedProductDeliveryIds;
	}
}
