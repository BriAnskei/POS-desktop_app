package com.gierza_molases.molases_app.model;

import java.time.LocalDateTime;
import java.util.Date;

public class CustomerPayments {
	private Integer id;
	private int customerId;
	private int customerDeliveryId;
	private String paymentType;
	private String status;
	private double total;
	private double totalPayment;
	private Date promiseToPay; // loan payment
	private LocalDateTime createdAt;

	// Constructor without ID (for creating new payments)
	public CustomerPayments(int customerId, int customerDeliveryId, String paymentType, String status, double total,
			double totalPayment, Date promiseToPay, LocalDateTime createdAt) {
		this.customerId = customerId;
		this.customerDeliveryId = customerDeliveryId;
		this.paymentType = paymentType;
		this.status = status;
		this.total = total;
		this.totalPayment = totalPayment;
		this.promiseToPay = promiseToPay;
		this.createdAt = createdAt;
	}

	// Constructor with ID (for loading from database)
	public CustomerPayments(Integer id, int customerId, int customerDeliveryId, String paymentType, String status,
			double total, double totalPayment, Date promiseToPay, LocalDateTime createdAt) {
		this.id = id;
		this.customerId = customerId;
		this.customerDeliveryId = customerDeliveryId;
		this.paymentType = paymentType;
		this.status = status;
		this.total = total;
		this.totalPayment = totalPayment;
		this.promiseToPay = promiseToPay;
		this.createdAt = createdAt;
	}

	public void setCustomerDeliveryId(int customerDeliverId) {
		this.customerDeliveryId = customerDeliverId;
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

	public String getStatus() {
		return status;
	}

	public double getTotal() {
		return total;
	}

	public double getTotalPayment() {
		return totalPayment;
	}

	public Date getPromiseToPay() {
		return promiseToPay;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	@Override
	public String toString() {
		return "CustomerPayments{" + "id=" + id + ", customerId=" + customerId + ", customerDeliveryId="
				+ customerDeliveryId + ", paymentType='" + paymentType + '\'' + ", status='" + status + '\''
				+ ", total=" + total + ", totalPayment=" + totalPayment + ", promiseToPay=" + promiseToPay
				+ ", createdAt=" + createdAt + '}';
	}

}