package com.gierza_molases.molases_app.dto.delivery;

import java.util.List;

import com.gierza_molases.molases_app.model.Delivery;

public class NewDelivery {
	private Delivery delivery;
	private List<CustomerBranchDelivery> customerBranchDeliveries;

	public NewDelivery(Delivery delivery, List<CustomerBranchDelivery> customerBranchDeliveries) {
		this.delivery = delivery;
		this.customerBranchDeliveries = customerBranchDeliveries;
	}

	public Delivery getDelivery() {
		return delivery;
	}

	public List<CustomerBranchDelivery> getCustomerBranchDeliveries() {
		return customerBranchDeliveries;
	}

}
