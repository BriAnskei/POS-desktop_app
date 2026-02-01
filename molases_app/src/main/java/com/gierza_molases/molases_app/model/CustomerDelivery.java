package com.gierza_molases.molases_app.model;

public class CustomerDelivery {
	private Integer id;
	private int customerId;
	private int deliveryId;

	// INPUT
	public CustomerDelivery(int customerId, int deliveryId) {
		this.customerId = customerId;
		this.deliveryId = deliveryId;
	}

	// FETCH
	public CustomerDelivery(Integer id, int customerId, int deliveryId) {
		this.id = id;
		this.customerId = customerId;
		this.deliveryId = deliveryId;
	}

	public void setDeliveryId(int deliveryId) {
		this.deliveryId = deliveryId;
	}

	public Integer getId() {
		return id;
	}

	public int getCustomerId() {
		return customerId;
	}

	public int getDeliveryId() {
		return deliveryId;
	}
}
