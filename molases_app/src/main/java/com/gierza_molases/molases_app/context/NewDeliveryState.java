package com.gierza_molases.molases_app.context;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.gierza_molases.molases_app.model.Branch;
import com.gierza_molases.molases_app.model.Customer;
import com.gierza_molases.molases_app.model.ProductWithQuantity;

public class NewDeliveryState {
	// Delivery details (Step 1)
	private String deliveryName;
	private Date deliveryDate;
	private Map<String, Double> expenses;

	// Customer deliveries (Step 2)
	// Structure: Customer -> Branch -> List of Products with Quantities
	private Map<Customer, Map<Branch, List<ProductWithQuantity>>> customerDeliveries;

	/**
	 * Constructor - Initialize collections
	 */
	public NewDeliveryState() {
		this.expenses = new HashMap<>();
		this.customerDeliveries = new LinkedHashMap<>();
	}

	/*
	 * ====================== Getters ======================
	 */

	public String getDeliveryName() {
		return deliveryName;
	}

	public Date getDeliveryDate() {
		return deliveryDate;
	}

	public Map<String, Double> getDeliveryExpenses() {
		return expenses;
	}

	public Map<Customer, Map<Branch, List<ProductWithQuantity>>> getCustomerDeliveries() {
		return customerDeliveries;
	}

	/*
	 * ====================== Customer Delivery Operations ======================
	 */

	/**
	 * Add or update a customer delivery with their branches and products
	 */
	public void newCustomerDelivery(Customer customer, Map<Branch, List<ProductWithQuantity>> branchDeliveries) {
		if (customer == null) {
			throw new IllegalArgumentException("Customer cannot be null");
		}
		if (branchDeliveries == null || branchDeliveries.isEmpty()) {
			throw new IllegalArgumentException("Branch deliveries cannot be null or empty");
		}
		customerDeliveries.put(customer, new LinkedHashMap<>(branchDeliveries));
	}

	/**
	 * Remove a customer and all their deliveries
	 */
	public void deleteCustomerDelivery(Customer customer) {
		if (customer == null) {
			return;
		}
		customerDeliveries.remove(customer);
	}

	/**
	 * Check if a customer already exists in the delivery
	 */
	public boolean hasCustomer(Customer customer) {
		return customerDeliveries.containsKey(customer);
	}

	/**
	 * Get branches and products for a specific customer
	 */
	public Map<Branch, List<ProductWithQuantity>> getCustomerBranches(Customer customer) {
		return customerDeliveries.get(customer);
	}

	/**
	 * Add a branch with products to an existing customer
	 */
	public void addBranchToCustomer(Customer customer, Branch branch, List<ProductWithQuantity> products) {
		if (customer == null || branch == null) {
			throw new IllegalArgumentException("Customer and Branch cannot be null");
		}
		if (products == null || products.isEmpty()) {
			throw new IllegalArgumentException("Products cannot be null or empty");
		}

		Map<Branch, List<ProductWithQuantity>> branches = customerDeliveries.get(customer);
		if (branches == null) {
			branches = new LinkedHashMap<>();
			customerDeliveries.put(customer, branches);
		}

		branches.put(branch, products);
	}

	/**
	 * Remove a branch from a customer's delivery
	 */
	public void removeBranchFromCustomer(Customer customer, Branch branch) {
		if (customer == null || branch == null) {
			return;
		}

		Map<Branch, List<ProductWithQuantity>> branches = customerDeliveries.get(customer);
		if (branches != null) {
			branches.remove(branch);

			// If customer has no more branches, remove the customer
			if (branches.isEmpty()) {
				customerDeliveries.remove(customer);
			}
		}
	}

	/*
	 * ====================== Setters ======================
	 */

	/**
	 * Set Step 1 form data (delivery name, date, and expenses)
	 */
	public void setStepOneForm(String deliveryName, Date deliveryDate, Map<String, Double> expenses) {
		this.deliveryName = deliveryName;
		this.deliveryDate = deliveryDate;
		this.expenses = expenses != null ? new HashMap<>(expenses) : new HashMap<>();
	}

	/**
	 * Set Step 2 form data (customer deliveries)
	 */
	public void setStepTwoForm(Map<Customer, Map<Branch, List<ProductWithQuantity>>> customerDeliveries) {
		this.customerDeliveries = customerDeliveries != null ? new LinkedHashMap<>(customerDeliveries)
				: new LinkedHashMap<>();
	}

	/*
	 * ====================== State Management ======================
	 */

	/**
	 * Reset entire state - call when creating new delivery or canceling
	 */
	public void resetState() {
		this.deliveryName = null;
		this.deliveryDate = null;
		this.expenses = new HashMap<>();
		this.customerDeliveries = new LinkedHashMap<>();
	}

	/**
	 * Check if state has any data
	 */
	public boolean isEmpty() {
		return (deliveryName == null || deliveryName.trim().isEmpty()) && deliveryDate == null
				&& (expenses == null || expenses.isEmpty())
				&& (customerDeliveries == null || customerDeliveries.isEmpty());
	}

	/**
	 * Check if Step 1 is complete
	 */
	public boolean isStep1Complete() {
		return deliveryName != null && !deliveryName.trim().isEmpty() && deliveryDate != null && expenses != null
				&& !expenses.isEmpty();
	}

	/**
	 * Check if Step 2 is complete
	 */
	public boolean isStep2Complete() {
		if (customerDeliveries == null || customerDeliveries.isEmpty()) {
			return false;
		}

		// Ensure each customer has at least one branch with products
		for (Map<Branch, List<ProductWithQuantity>> branches : customerDeliveries.values()) {
			if (branches == null || branches.isEmpty()) {
				return false;
			}
			for (List<ProductWithQuantity> products : branches.values()) {
				if (products == null || products.isEmpty()) {
					return false;
				}
			}
		}

		return true;
	}

	/**
	 * Check if entire delivery is complete and ready to save
	 */
	public boolean isComplete() {
		return isStep1Complete() && isStep2Complete();
	}

	/*
	 * ====================== Utility Methods ======================
	 */

	/**
	 * Get total number of customers
	 */
	public int getTotalCustomers() {
		return customerDeliveries != null ? customerDeliveries.size() : 0;
	}

	/**
	 * Get total number of branches across all customers
	 */
	public int getTotalBranches() {
		if (customerDeliveries == null) {
			return 0;
		}

		return customerDeliveries.values().stream().mapToInt(branches -> branches != null ? branches.size() : 0).sum();
	}

	/**
	 * Get total number of products across all branches
	 */
	public int getTotalProducts() {
		if (customerDeliveries == null) {
			return 0;
		}

		return customerDeliveries.values().stream().flatMap(branches -> branches.values().stream())
				.mapToInt(products -> products != null ? products.size() : 0).sum();
	}

	/**
	 * Get total expenses amount
	 */
	public double getTotalExpenses() {
		if (expenses == null) {
			return 0.0;
		}

		return expenses.values().stream().mapToDouble(Double::doubleValue).sum();
	}

	/**
	 * Calculate gross sales (total selling price of all products)
	 */
	public double calculateGrossSales() {
		if (customerDeliveries == null) {
			return 0.0;
		}

		return customerDeliveries.values().stream().flatMap(branches -> branches.values().stream())
				.flatMap(List::stream).mapToDouble(ProductWithQuantity::getTotalSellingPrice).sum();
	}

	/**
	 * Calculate total capital (total capital cost of all products)
	 */
	public double calculateTotalCapital() {
		if (customerDeliveries == null) {
			return 0.0;
		}

		return customerDeliveries.values().stream().flatMap(branches -> branches.values().stream())
				.flatMap(List::stream).mapToDouble(ProductWithQuantity::getTotalCapital).sum();
	}

	/**
	 * Calculate gross profit (gross sales - total capital)
	 */
	public double calculateGrossProfit() {
		return calculateGrossSales() - calculateTotalCapital();
	}

	/**
	 * Calculate net profit (gross profit - total expenses)
	 */
	public double calculateNetProfit() {
		return calculateGrossProfit() - getTotalExpenses();
	}

	@Override
	public String toString() {
		return "NewDeliveryState{" + "deliveryName='" + deliveryName + '\'' + ", deliveryDate=" + deliveryDate
				+ ", totalExpenses=" + getTotalExpenses() + ", totalCustomers=" + getTotalCustomers()
				+ ", totalBranches=" + getTotalBranches() + ", totalProducts=" + getTotalProducts() + ", grossSales="
				+ calculateGrossSales() + ", netProfit=" + calculateNetProfit() + ", isComplete=" + isComplete() + '}';
	}
}