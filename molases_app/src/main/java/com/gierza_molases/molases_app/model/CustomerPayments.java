package com.gierza_molases.molases_app.model;

import java.time.LocalDateTime;
import java.util.Date;

public class CustomerPayments {

	private Integer id;
	private int customerId;
	private int customerDeliveryId;
	private String paymentType;

	private String status;
	private String notes;

	private double total;
	private double totalPayment;

	private Date promiseToPay; // loan payment
	private LocalDateTime createdAt;

	// ===== Fetch-only / JOIN fields =====
	private int deliveryId;
	private String customerName;
	private String deliveryName;
	private Date deliveryDate;

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

	// raw data for delivery view module functionality
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

	// complete data for a page view
	public CustomerPayments(Integer id, int customerId, int customerDeliveryId, String paymentType, String status,
			String notes, double total, double totalPayment, Date promiseToPay, LocalDateTime createdAt, int deliveryId,
			String customerName, String deliveryName, Date deliveryDate) {

		this.id = id;
		this.customerId = customerId;
		this.customerDeliveryId = customerDeliveryId;
		this.paymentType = paymentType;

		this.status = status;
		this.notes = notes;

		this.total = total;
		this.totalPayment = totalPayment;

		this.promiseToPay = promiseToPay;
		this.createdAt = createdAt;

		this.deliveryId = deliveryId;
		this.customerName = customerName;
		this.deliveryName = deliveryName;
		this.deliveryDate = deliveryDate;
	}

	// ===== Getters =====

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

	public String getNotes() {
		return notes;
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

	public int getDeliveryId() {
		return deliveryId;
	}

	public String getCustomerName() {
		return customerName;
	}

	public String getDeliveryName() {
		return deliveryName;
	}

	public Date getDeliveryDate() {
		return deliveryDate;
	}

	// ===== Setters =====

	public void setId(Integer id) {
		this.id = id;
	}

	public void setCustomerId(int customerId) {
		this.customerId = customerId;
	}

	public void setCustomerDeliveryId(int customerDeliveryId) {
		this.customerDeliveryId = customerDeliveryId;
	}

	public void setPaymentType(String paymentType) {
		this.paymentType = paymentType;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public void setTotal(double total) {
		this.total = total;
	}

	public void setTotalPayment(double totalPayment) {
		this.totalPayment = totalPayment;
	}

	public void setPromiseToPay(Date promiseToPay) {
		this.promiseToPay = promiseToPay;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public void setDeliveryId(int deliveryId) {
		this.deliveryId = deliveryId;
	}

	public void setCustomerName(String customerName) {
		this.customerName = customerName;
	}

	public void setDeliveryName(String deliveryName) {
		this.deliveryName = deliveryName;
	}

	public void setDeliveryDate(Date deliveryDate) {
		this.deliveryDate = deliveryDate;
	}

	@Override
	public String toString() {
		return "CustomerPayments{" + "id=" + id + ", customerId=" + customerId + ", customerDeliveryId="
				+ customerDeliveryId + ", paymentType='" + paymentType + '\'' + ", status='" + status + '\''
				+ ", total=" + total + ", totalPayment=" + totalPayment + ", promiseToPay=" + promiseToPay
				+ ", createdAt=" + createdAt + '}';
	}
}
