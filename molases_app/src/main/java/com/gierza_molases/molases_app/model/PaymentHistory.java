package com.gierza_molases.molases_app.model;

import java.time.Instant;

public class PaymentHistory {

	private Integer id;
	private int customerPaymentId;
	private double amount;
	private Instant createdAt;

	public PaymentHistory() {
	}

	public PaymentHistory(Integer id, int customerPaymentId, double amount, Instant createdAt) {
		this.id = id;
		this.customerPaymentId = customerPaymentId;
		this.amount = amount;
		this.createdAt = createdAt;
	}

	public PaymentHistory(int customerPaymentId, double amount) {
		this.customerPaymentId = customerPaymentId;
		this.amount = amount;
	}

	// Getters & Setters

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public int getCustomerPaymentId() {
		return customerPaymentId;
	}

	public void setCustomerPaymentId(int customerPaymentId) {
		this.customerPaymentId = customerPaymentId;
	}

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}
}
