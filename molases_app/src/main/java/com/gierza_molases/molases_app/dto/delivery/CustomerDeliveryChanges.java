package com.gierza_molases.molases_app.dto.delivery;

import java.util.List;
import java.util.Map;

public class CustomerDeliveryChanges {
	private List<CustomerBranchDelivery> addedDelivery;
	private Map<Integer, String> customerDeliveryStatus; // id, status(cancelled, delivered)

	public CustomerDeliveryChanges() {

	}

	public CustomerDeliveryChanges(List<CustomerBranchDelivery> addedDelivery,
			Map<Integer, String> customerDeliveryStatus) {
		this.addedDelivery = addedDelivery;
		this.customerDeliveryStatus = customerDeliveryStatus;
	}

	public List<CustomerBranchDelivery> getAddedDelivery() {
		return addedDelivery;
	}

	public void setAddedDelivery(List<CustomerBranchDelivery> addedDelivery) {
		this.addedDelivery = addedDelivery;
	}

	public Map<Integer, String> getCancelledCustomerDeliveries() {
		return customerDeliveryStatus;
	}

	public void setCancelledCustomerDeliveries(Map<Integer, String> customerDeliveryStatus) {
		this.customerDeliveryStatus = customerDeliveryStatus;
	}

}
