package com.gierza_molases.molases_app.dto.delivery;

import java.util.List;
import java.util.Map;

import com.gierza_molases.molases_app.model.BranchDelivery;
import com.gierza_molases.molases_app.model.ProductDelivery;

public class BranchDeliveryChanges {

	private Map<BranchDelivery, List<ProductDelivery>> newBranchesDelivery;
	private List<Integer> removedBranchDeliveryIds;

	/** No-args constructor */
	public BranchDeliveryChanges() {
	}

	/** Full constructor */
	public BranchDeliveryChanges(Map<BranchDelivery, List<ProductDelivery>> newBranchesDelivery,
			List<Integer> removedBranchDeliveryIds) {
		this.newBranchesDelivery = newBranchesDelivery;
		this.removedBranchDeliveryIds = removedBranchDeliveryIds;
	}

	// ===== Getters & Setters =====

	public Map<BranchDelivery, List<ProductDelivery>> getNewBranchesDelivery() {
		return newBranchesDelivery;
	}

	public void setNewBranchesDelivery(Map<BranchDelivery, List<ProductDelivery>> newBranchesDelivery) {
		this.newBranchesDelivery = newBranchesDelivery;
	}

	public List<Integer> getRemovedBranchDelivery() {
		return removedBranchDeliveryIds;
	}

	public void setRemovedBranchDelivery(List<Integer> removedBranchDeliveryIds) {
		this.removedBranchDeliveryIds = removedBranchDeliveryIds;
	}
}
