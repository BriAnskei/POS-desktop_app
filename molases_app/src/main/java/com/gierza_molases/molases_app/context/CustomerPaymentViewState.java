package com.gierza_molases.molases_app.context;

import java.util.ArrayList;
import java.util.List;

import com.gierza_molases.molases_app.model.CustomerPayments;
import com.gierza_molases.molases_app.model.PaymentHistory;

public class CustomerPaymentViewState {

	private CustomerPayments customerPayment;
	private List<PaymentHistory> paymentHistory;

	public CustomerPaymentViewState() {
		this.paymentHistory = new ArrayList<>();
	}

	public CustomerPayments getCustomerPayment() {
		return customerPayment;
	}

	public void setCustomerPayment(CustomerPayments customerPayment) {
		this.customerPayment = customerPayment;
	}

	public List<PaymentHistory> getPaymentHistory() {
		return paymentHistory;
	}

	public void setPaymentHistory(List<PaymentHistory> paymentHistory) {
		this.paymentHistory = paymentHistory;
	}

	public void addPaymentHistory(PaymentHistory payment) {
		this.paymentHistory.add(payment);
	}

	public void clearPaymentHistory() {
		this.paymentHistory.clear();
	}
}