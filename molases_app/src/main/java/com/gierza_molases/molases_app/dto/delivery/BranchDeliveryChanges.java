package com.gierza_molases.molases_app.dto.delivery;

import java.util.List;
import java.util.Map;

import com.gierza_molases.molases_app.model.BranchDelivery;
import com.gierza_molases.molases_app.model.ProductDelivery;

public class BranchDeliveryChanges {

	private Map<BranchDelivery, List<ProductDelivery>> newBranchesDelivery;

	/** No-args constructor */
	public BranchDeliveryChanges() {
	}

	/** Full constructor */
	public BranchDeliveryChanges(Map<BranchDelivery, List<ProductDelivery>> newBranchesDelivery) {
		this.newBranchesDelivery = newBranchesDelivery;

	}

	// ===== Getters & Setters =====

	public Map<BranchDelivery, List<ProductDelivery>> getNewBranchesDelivery() {
		return newBranchesDelivery;
	}

	public void setNewBranchesDelivery(Map<BranchDelivery, List<ProductDelivery>> newBranchesDelivery) {
		this.newBranchesDelivery = newBranchesDelivery;
	}

}
