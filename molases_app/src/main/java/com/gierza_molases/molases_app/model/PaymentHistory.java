package com.gierza_molases.molases_app.model;

import java.util.Date;

public class PaymentHistory {
	private double amount;
	private Date paidAt;

	public PaymentHistory() {
	}

	public PaymentHistory(double amount, Date paidAt) {
		this.amount = amount;
		this.paidAt = paidAt;
	}

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	public Date getPaidAt() {
		return paidAt;
	}

	public void setPaidAt(Date paidAt) {
		this.paidAt = paidAt;
	}
}