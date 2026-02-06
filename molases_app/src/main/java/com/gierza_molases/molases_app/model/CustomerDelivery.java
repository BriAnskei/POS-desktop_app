package com.gierza_molases.molases_app.model;

public class CustomerDelivery {
	private Integer id;
	private int customerId;
	private int deliveryId;

	private String status;

	// INPUT
	public CustomerDelivery(int customerId, int deliveryId) {
		this.customerId = customerId;
		this.deliveryId = deliveryId;
	}

	// FETCH
	public CustomerDelivery(Integer id, int customerId, int deliveryId, String status) {
		this.id = id;
		this.customerId = customerId;
		this.deliveryId = deliveryId;
		this.status = status;
	}

	public void setDeliveryId(int deliveryId) {
		this.deliveryId = deliveryId;
	}

	public void setStatus(String status) {
		this.status = status;
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

	public String getStatus() {
		return status;
	}

	@Override
	public String toString() {
		return "CustomerDelivery{" + "id=" + id + ", customerId=" + customerId + ", deliveryId=" + deliveryId
				+ ", status='" + status + '\'' + '}';
	}

}
