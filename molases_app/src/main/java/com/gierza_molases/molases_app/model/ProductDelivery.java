package com.gierza_molases.molases_app.model;

public class ProductDelivery {

	private Integer id;
	private int branchDeliveryId;
	private int productId;

	private int quantity;

	public ProductDelivery(Integer id, int branchDeliveryId, int productId, int quantity) {
		this.id = id;
		this.branchDeliveryId = branchDeliveryId;
		this.productId = productId;

		this.quantity = quantity;
	}

	public ProductDelivery(int branchDeliveryId, int productId, int quantity) {
		this.branchDeliveryId = branchDeliveryId;
		this.productId = productId;

		this.quantity = quantity;
	}

	public Integer getId() {
		return id;
	}

	public int getBranchDeliveryId() {
		return branchDeliveryId;
	}

	public int getProductId() {
		return productId;
	}

	public int getQuantity() {
		return quantity;
	}

	@Override
	public String toString() {
		return "ProductDelivery{" + "id=" + id + ", productId=" + productId + ", branchDeliveryId=" + branchDeliveryId
				+ ", quantity=" + quantity + '}';
	}

}
