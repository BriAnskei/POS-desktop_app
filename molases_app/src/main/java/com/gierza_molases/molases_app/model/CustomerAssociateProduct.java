package com.gierza_molases.molases_app.model;

public class CustomerAssociateProduct {
	private int id;
	private int customerId;
	private int productId;

	public CustomerAssociateProduct(int id, int customerId, int productId) {
		this.id = id;
		this.customerId = customerId;
		this.productId = productId;
	}

	public int getId() {
		return id;
	}

	public int getCustomerId() {
		return customerId;
	}

	public int getProductId() {
		return productId;
	}
}
