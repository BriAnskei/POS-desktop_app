package com.gierza_molases.molases_app.model;

public class BranchDelivery {

	private Integer id;
	private int customerDeliveryId;
	private int branchId;
	private int productId;

	private int quantity;
	private String status; // scheduled, cancelled, delivered;

	// create
	public BranchDelivery(int customerDeliveryId, int branchId, int productId, int quantity, String status) {
		this.customerDeliveryId = customerDeliveryId;
		this.branchId = branchId;
		this.productId = productId;

		this.quantity = quantity;
		this.status = status;
	}

	public BranchDelivery(Integer id, int branchId, int customerDeliveryId, int productId, int quantity,
			String status) {
		this.id = id;
		this.customerDeliveryId = customerDeliveryId;
		this.branchId = branchId;
		this.productId = productId;

		this.quantity = quantity;
		this.status = status;
	}

	public Integer getId() {
		return id;
	}

	public int getCustomerDeliveryId() {
		return customerDeliveryId;
	}

	public int getBranchId() {
		return branchId;
	}

	public int getProductId() {
		return productId;
	}

	public int getQuantity() {
		return quantity;
	}

	public String getStatus() {
		return status;
	}

}
