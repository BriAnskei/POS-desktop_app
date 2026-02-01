package com.gierza_molases.molases_app.model;

import java.time.LocalDateTime;

public class CustomerPayments {
	private Integer id;
	private int customerId;
	private int customerDeliveryId;

	private String paymentType;

	private double total;
	private double totalPayment;
	private double balance;

	private String note;

	private LocalDateTime createdAt;

	public CustomerPayments(int customerId, int customerDeliveryId, String paymentType, double total,
			double totalPayment, double balance, String note, LocalDateTime createdAt) {
		this.customerId = customerId;
		this.customerDeliveryId = customerDeliveryId;
		this.paymentType = paymentType;
		this.total = total;
		this.totalPayment = totalPayment;
		this.balance = balance;
		this.createdAt = createdAt;
	}

	public CustomerPayments(Integer id, int customerId, int customerDeliveryId, String paymentType, double totalPayment,
			double balance, String note, LocalDateTime createdAt) {
		this.customerDeliveryId = customerDeliveryId;
		this.customerId = customerId;
		this.customerDeliveryId = customerDeliveryId;
		this.paymentType = paymentType;
		this.totalPayment = totalPayment;
		this.balance = balance;
		this.createdAt = createdAt;
	}

	public Integer getId() {
		return id;
	}

	public int getCustomerId() {
		return customerId;
	}

	public int getCustomerDeliveryId() {
		return customerDeliveryId;
	}

	public String getPaymentType() {
		return paymentType;
	}

	public double getTotal() {
		return total;
	}

	public double getTotalPayment() {
		return totalPayment;
	}

	public double getBalance() {
		return balance;
	}

	public String getNote() {
		return note;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

}
