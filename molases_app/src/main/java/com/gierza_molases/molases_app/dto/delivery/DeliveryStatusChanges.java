package com.gierza_molases.molases_app.dto.delivery;

import java.util.Map;

public class DeliveryStatusChanges {
	// Id, status

	// Status per customer delivery (by branch delivery)
	private Map<Integer, String> customerDeliveryStatuses;

	// Status per branch delivery
	private Map<Integer, String> branchDeliveryStatuses;

	/** No-args constructor */
	public DeliveryStatusChanges() {
	}

	/** Full constructor */
	public DeliveryStatusChanges(Map<Integer, String> customerDeliveryStatuses,
			Map<Integer, String> branchDeliveryStatuses) {
		this.customerDeliveryStatuses = customerDeliveryStatuses;
		this.branchDeliveryStatuses = branchDeliveryStatuses;
	}

	// ===== Getters & Setters =====

	public Map<Integer, String> getCustomerDeliveryStatuses() {
		return customerDeliveryStatuses;
	}

	public void setCustomerDeliveryStatuses(Map<Integer, String> customerDeliveryStatuses) {
		this.customerDeliveryStatuses = customerDeliveryStatuses;
	}

	public Map<Integer, String> getBranchDeliveryStatuses() {
		return branchDeliveryStatuses;
	}

	public void setBranchDeliveryStatuses(Map<Integer, String> branchDeliveryStatuses) {
		this.branchDeliveryStatuses = branchDeliveryStatuses;
	}
}
