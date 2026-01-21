package com.gierza_molases.molases_app.model;

public class Payment {
	private int id;
	private String customerName;
	private String deliveryName;
	private String deliveryDate;
	private double totalPayments;
	private String createdAt;
	private String status;

	public Payment() {
	}

	public Payment(int id, String customerName, String deliveryName, String deliveryDate, double totalPayments,
			String createdAt, String status) {
		this.id = id;
		this.customerName = customerName;
		this.deliveryName = deliveryName;
		this.deliveryDate = deliveryDate;
		this.totalPayments = totalPayments;
		this.createdAt = createdAt;
		this.status = status;
	}

	// Getters and Setters
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getCustomerName() {
		return customerName;
	}

	public void setCustomerName(String customerName) {
		this.customerName = customerName;
	}

	public String getDeliveryName() {
		return deliveryName;
	}

	public void setDeliveryName(String deliveryName) {
		this.deliveryName = deliveryName;
	}

	public String getDeliveryDate() {
		return deliveryDate;
	}

	public void setDeliveryDate(String deliveryDate) {
		this.deliveryDate = deliveryDate;
	}

	public double getTotalPayments() {
		return totalPayments;
	}

	public void setTotalPayments(double totalPayments) {
		this.totalPayments = totalPayments;
	}

	public String getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(String createdAt) {
		this.createdAt = createdAt;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
}