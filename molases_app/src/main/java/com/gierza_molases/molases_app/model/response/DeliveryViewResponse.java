package com.gierza_molases.molases_app.model.response;

import java.util.List;
import java.util.Map;

import com.gierza_molases.molases_app.model.Branch;
import com.gierza_molases.molases_app.model.BranchDelivery;
import com.gierza_molases.molases_app.model.Customer;
import com.gierza_molases.molases_app.model.CustomerDelivery;
import com.gierza_molases.molases_app.model.Delivery;
import com.gierza_molases.molases_app.model.ProductWithQuantity;

public class DeliveryViewResponse {

	private Delivery delivery;

	// exact DB model; on fetch
	private Map<CustomerDelivery, List<BranchDelivery>> customerDeliveries;

	// mapped customer deliveries data
	private Map<Customer, Map<Branch, List<ProductWithQuantity>>> mappedCustomerDeliveries;

	public DeliveryViewResponse() {

	}

	public DeliveryViewResponse(Delivery delivery, Map<CustomerDelivery, List<BranchDelivery>> customerDeliveries) {
		this.delivery = delivery;
		this.customerDeliveries = customerDeliveries;
	}

	public Delivery getDeliveryDetials() {
		return delivery;
	}

	public Map<CustomerDelivery, List<BranchDelivery>> getCustomerDeliveries() {
		return customerDeliveries;
	}

	public Map<Customer, Map<Branch, List<ProductWithQuantity>>> getMappedCustomerDeliveries() {
		return mappedCustomerDeliveries;
	}

	public void setDelivery(Delivery delivery) {
		this.delivery = delivery;
	}

	public void setCustomerDeliveries(Map<CustomerDelivery, List<BranchDelivery>> customerDeliveries) {
		this.customerDeliveries = customerDeliveries;
	}

	public void setMappedCustomerDelivery(
			Map<Customer, Map<Branch, List<ProductWithQuantity>>> mappedCustomerDeliveries) {
		this.mappedCustomerDeliveries = mappedCustomerDeliveries;
	}

}
