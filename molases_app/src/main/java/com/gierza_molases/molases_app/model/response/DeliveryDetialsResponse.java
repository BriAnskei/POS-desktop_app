package com.gierza_molases.molases_app.model.response;

import java.util.List;
import java.util.Map;

import com.gierza_molases.molases_app.model.Branch;
import com.gierza_molases.molases_app.model.Customer;
import com.gierza_molases.molases_app.model.Delivery;
import com.gierza_molases.molases_app.model.ProductWithQuantity;

public class DeliveryDetialsResponse {

	private Delivery delivery;
	private Map<Customer, Map<Branch, List<ProductWithQuantity>>> customerDeliveries;

	public DeliveryDetialsResponse(Delivery delivery,
			Map<Customer, Map<Branch, List<ProductWithQuantity>>> customerDeliverries) {
		this.delivery = delivery;
		this.customerDeliveries = customerDeliverries;
	}

	public Delivery getDeliveryDetials() {
		return delivery;
	}

	public Map<Customer, Map<Branch, List<ProductWithQuantity>>> getCustomerDeliveries() {
		return customerDeliveries;
	}

}
