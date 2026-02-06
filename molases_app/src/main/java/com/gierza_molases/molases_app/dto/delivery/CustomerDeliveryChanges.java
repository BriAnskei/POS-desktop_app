package com.gierza_molases.molases_app.dto.delivery;

import java.util.List;

public class CustomerDeliveryChanges {
	private List<CustomerBranchDelivery> addedDelivery;

	public CustomerDeliveryChanges() {

	}

	public CustomerDeliveryChanges(List<CustomerBranchDelivery> addedDelivery) {
		this.addedDelivery = addedDelivery;

	}

	public List<CustomerBranchDelivery> getAddedDelivery() {
		return addedDelivery;
	}

	public void setAddedDelivery(List<CustomerBranchDelivery> addedDelivery) {
		this.addedDelivery = addedDelivery;
	}

}
