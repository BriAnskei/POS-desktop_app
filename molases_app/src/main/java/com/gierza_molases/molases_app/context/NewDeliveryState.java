package com.gierza_molases.molases_app.context;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import com.gierza_molases.molases_app.model.Branch;
import com.gierza_molases.molases_app.model.Customer;
import com.gierza_molases.molases_app.model.Product;

public class NewDeliveryState {
	// delivery detials
	private String deliveryName;
	private LocalDateTime deliveryDate;
	private Map<String, Double> expenses;

	// custome, branches delivery
	private Map<Customer, Map<Branch, List<Product>>> customerDeliveries;

	/*
	 * ====================== Getters ======================
	 */
	public String getDeliveryName() {
		return deliveryName;
	}

	public LocalDateTime getDeliveryDate() {
		return deliveryDate;
	}

	public Map<String, Double> getDeliveryExpenses() {
		return expenses;
	}

	public Map<Customer, Map<Branch, List<Product>>> getCustomerDeliveries() {
		return customerDeliveries;
	}

	/*
	 * ====================== ======================
	 */
	public void newCustomerDelivery(Customer customer, Map<Branch, List<Product>> branchDeliveries) {
		customerDeliveries.putIfAbsent(customer, branchDeliveries);
	}

	public void deleteCustomerDelivery(Customer customer) {
		customerDeliveries.remove(customer);
	}

	/*
	 * ====================== Setters ======================
	 */
	public void setStepOneForm(String deliveryName, LocalDateTime deliveryDate, Map<String, Double> expenses) {
		this.deliveryName = deliveryName;
		this.deliveryDate = deliveryDate;
		this.expenses = expenses;
	}

	public void setStepTwoForm(Map<Customer, Map<Branch, List<Product>>> customerDeliveries) {
		this.customerDeliveries = customerDeliveries;
	}

	public void resetState() {
		this.deliveryName = null;
		this.deliveryDate = null;

		this.expenses.clear();
		this.customerDeliveries.clear();

	}

}
