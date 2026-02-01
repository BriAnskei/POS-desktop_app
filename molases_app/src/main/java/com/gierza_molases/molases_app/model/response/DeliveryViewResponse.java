package com.gierza_molases.molases_app.model.response;

import java.util.List;
import java.util.Map;

import com.gierza_molases.molases_app.dto.delivery.CustomerBranchDelivery;
import com.gierza_molases.molases_app.model.Branch;
import com.gierza_molases.molases_app.model.Customer;
import com.gierza_molases.molases_app.model.Delivery;
import com.gierza_molases.molases_app.model.ProductWithQuantity;

public class DeliveryViewResponse {

	private Delivery delivery;

	private List<CustomerBranchDelivery> customerBranchDeliveries;

	// mapped customer deliveries data
	private Map<Customer, Map<Branch, List<ProductWithQuantity>>> mappedCustomerDeliveries;

	public DeliveryViewResponse() {

	}

	public DeliveryViewResponse(Delivery delivery, List<CustomerBranchDelivery> customerBranchDelivery,
			List<CustomerBranchDelivery> customerBranchDeliveries) {
		this.delivery = delivery;
		this.customerBranchDeliveries = customerBranchDeliveries;
		this.customerBranchDeliveries = customerBranchDelivery;
	}

	public Delivery getDeliveryDetials() {
		return delivery;
	}

	public List<CustomerBranchDelivery> getCustomerDeliveries() {
		return customerBranchDeliveries;
	}

	public Map<Customer, Map<Branch, List<ProductWithQuantity>>> getMappedCustomerDeliveries() {
		return mappedCustomerDeliveries;
	}

	public void setDelivery(Delivery delivery) {
		this.delivery = delivery;
	}

	public void setCustomerDeliveries(List<CustomerBranchDelivery> customerBranchDeliveries) {
		this.customerBranchDeliveries = customerBranchDeliveries;
	}

	public void setMappedCustomerDelivery(
			Map<Customer, Map<Branch, List<ProductWithQuantity>>> mappedCustomerDeliveries) {
		this.mappedCustomerDeliveries = mappedCustomerDeliveries;
	}

}
