package com.gierza_molases.molases_app.context;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.gierza_molases.molases_app.model.Branch;
import com.gierza_molases.molases_app.model.Customer;
import com.gierza_molases.molases_app.model.Delivery;
import com.gierza_molases.molases_app.model.ProductWithQuantity;

public class DeliveryDetailsState {

	// Delivery information
	private Delivery delivery;

	// Mapped customer deliveries data (Customer -> Branch -> Products)
	private Map<Customer, Map<Branch, List<ProductWithQuantity>>> mappedCustomerDeliveries;

	// Newly added customer delivey
	private Map<Customer, Map<Branch, List<ProductWithQuantity>>> additionalCustomerDelivery;

	// Temporary payment types tracked in UI (not yet saved to DB)
	// This will be populated when user sets payment type before marking as
	// delivered
	private Map<Customer, String> temporaryPaymentTypes;

	// Branch delivery statuses (Delivered/Cancelled) - tracked locally until saved
	// to DB
	private Map<Branch, String> branchDeliveryStatuses;

	public DeliveryDetailsState() {
		this.mappedCustomerDeliveries = new HashMap<>();
		this.additionalCustomerDelivery = new HashMap<>();
		this.temporaryPaymentTypes = new HashMap<>();
		this.branchDeliveryStatuses = new HashMap<>();
	}

	/*
	 * ====================== Getters ======================
	 */

	public Delivery getDelivery() {
		return delivery;
	}

	public Map<Customer, Map<Branch, List<ProductWithQuantity>>> getMappedCustomerDeliveries() {
		return mappedCustomerDeliveries;
	}

	public Map<Customer, String> getTemporaryPaymentTypes() {
		return temporaryPaymentTypes;
	}

	/**
	 * Get payment type for a specific customer Returns "Not Set" if no payment type
	 * has been set
	 */
	public String getPaymentType(Customer customer) {
		return temporaryPaymentTypes.getOrDefault(customer, "Not Set");
	}

	public Map<Customer, Map<Branch, List<ProductWithQuantity>>> getAdditionalCustomerDelivery() {
		return this.additionalCustomerDelivery;
	}

	/**
	 * Get branch delivery statuses map
	 */
	public Map<Branch, String> getBranchDeliveryStatuses() {
		return branchDeliveryStatuses;
	}

	/**
	 * Get delivery status for a specific branch Returns "Delivered" if no status
	 * has been set
	 */
	public String getBranchDeliveryStatus(Branch branch) {
		return branchDeliveryStatuses.getOrDefault(branch, "Delivered");
	}

	/*
	 * ====================== Setters ======================
	 */

	public void setDelivery(Delivery delivery) {
		this.delivery = delivery;
	}

	public void setMappedCustomerDeliveries(
			Map<Customer, Map<Branch, List<ProductWithQuantity>>> mappedCustomerDeliveries) {
		this.mappedCustomerDeliveries = mappedCustomerDeliveries != null ? new HashMap<>(mappedCustomerDeliveries)
				: new HashMap<>();

		// Initialize branch statuses for all branches as "Delivered"
		if (mappedCustomerDeliveries != null) {
			for (Map<Branch, List<ProductWithQuantity>> branches : mappedCustomerDeliveries.values()) {
				for (Branch branch : branches.keySet()) {
					if (branch != null) {
						branchDeliveryStatuses.putIfAbsent(branch, "Delivered");
					}
				}
			}
		}
	}

	/**
	 * Set payment type for a specific customer
	 */
	public void setPaymentType(Customer customer, String paymentType) {
		if (customer != null && paymentType != null) {
			this.temporaryPaymentTypes.put(customer, paymentType);
		}
	}

	/**
	 * Set delivery status for a specific branch
	 */
	public void setBranchDeliveryStatus(Branch branch, String status) {
		if (branch != null && status != null) {
			this.branchDeliveryStatuses.put(branch, status);
		}
	}

	/*
	 * ====================== State Management ======================
	 */

	/**
	 * Reset entire state
	 */
	public void resetState() {
		this.delivery = null;
		this.mappedCustomerDeliveries = new HashMap<>();
		this.temporaryPaymentTypes = new HashMap<>();
		this.branchDeliveryStatuses = new HashMap<>();
		this.additionalCustomerDelivery = new HashMap<>();
	}

	/**
	 * Check if state has delivery data loaded
	 */
	public boolean isLoaded() {
		return delivery != null && mappedCustomerDeliveries != null && !mappedCustomerDeliveries.isEmpty();
	}

	/*
	 * ====================== Utility Methods ======================
	 */

	/**
	 * Recalculate all financial metrics based on current customer deliveries
	 * Excludes branches with "Cancelled" status from calculations
	 */
	public void recalculateAllFinancials() {
		if (delivery == null || mappedCustomerDeliveries == null) {
			return;
		}

		double totalGross = 0.0;
		double totalCapital = 0.0;

		// Calculate totals from all customer deliveries, excluding cancelled branches
		for (Map.Entry<Customer, Map<Branch, List<ProductWithQuantity>>> entry : mappedCustomerDeliveries.entrySet()) {
			Map<Branch, List<ProductWithQuantity>> branches = entry.getValue();

			for (Map.Entry<Branch, List<ProductWithQuantity>> branchEntry : branches.entrySet()) {
				Branch branch = branchEntry.getKey();

				// Skip cancelled branches
				String branchStatus = getBranchDeliveryStatus(branch);
				if ("Cancelled".equalsIgnoreCase(branchStatus)) {
					continue;
				}

				List<ProductWithQuantity> products = branchEntry.getValue();

				for (ProductWithQuantity product : products) {
					totalGross += product.getTotalSellingPrice();
					totalCapital += product.getTotalCapital();
				}
			}
		}

		// Update delivery object
		delivery.setTotalGross(totalGross);
		delivery.setTotalCapital(totalCapital);

		// Calculate gross profit
		double grossProfit = totalGross - totalCapital;
		delivery.setGrossProfit(grossProfit);

		// Recalculate net profit (gross profit - expenses)
		double totalExpenses = delivery.getTotalExpenses();
		double netProfit = grossProfit - totalExpenses;
		delivery.setNetProfit(netProfit);
	}

	/**
	 * Add a customer with their branches and products
	 */
	public void addCustomerDelivery(Customer customer, Map<Branch, List<ProductWithQuantity>> branchProducts) {

		if (customer != null && branchProducts != null && !branchProducts.isEmpty()) {
			this.mappedCustomerDeliveries.put(customer, new HashMap<>(branchProducts));
			this.additionalCustomerDelivery.put(customer, new HashMap<>(branchProducts));

			// Initialize payment type as "Not Set" if not already set
			this.temporaryPaymentTypes.putIfAbsent(customer, "Not Set");

			// Initialize branch statuses as "Delivered" for new branches
			for (Branch branch : branchProducts.keySet()) {
				if (branch != null) {
					this.branchDeliveryStatuses.putIfAbsent(branch, "Delivered");
				}
			}

			// Recalculate all financial metrics
			recalculateAllFinancials();
		}
	}

	/**
	 * Remove a customer from delivery
	 */
	public void removeCustomerDelivery(Customer customer) {
		if (customer != null) {
			// Remove branch statuses for this customer's branches
			Map<Branch, List<ProductWithQuantity>> branches = this.mappedCustomerDeliveries.get(customer);
			if (branches != null) {
				for (Branch branch : branches.keySet()) {
					this.branchDeliveryStatuses.remove(branch);
				}
			}

			this.mappedCustomerDeliveries.remove(customer);
			this.temporaryPaymentTypes.remove(customer);
		}
	}

	/**
	 * Add or update an expense in the delivery Also recalculates net profit
	 */
	public void addExpense(String name, Double amount) {
		if (delivery != null && name != null && amount != null && amount > 0) {
			Map<String, Double> expenses = new HashMap<>(delivery.getExpenses());
			expenses.put(name, amount);
			delivery.setExpenses(expenses);

			// Recalculate financial metrics
			recalculateFinancials();
		}
	}

	/**
	 * Remove an expense from the delivery Also recalculates net profit
	 */
	public void removeExpense(String name) {
		if (delivery != null && name != null) {
			Map<String, Double> expenses = new HashMap<>(delivery.getExpenses());
			expenses.remove(name);
			delivery.setExpenses(expenses);

			// Recalculate financial metrics
			recalculateFinancials();
		}
	}

	/**
	 * Recalculate financial metrics when expenses change Net Profit = Gross Profit
	 * - Total Expenses
	 */
	private void recalculateFinancials() {
		if (delivery == null) {
			return;
		}

		// Get current values
		Double grossProfit = delivery.getGrossProfit();
		if (grossProfit == null) {
			grossProfit = 0.0;
		}

		// Calculate total expenses from the expenses map
		double totalExpenses = delivery.getTotalExpenses();

		// Calculate new net profit
		double newNetProfit = grossProfit - totalExpenses;

		// Update the delivery's net profit
		delivery.setNetProfit(newNetProfit);
	}

	/**
	 * Get recalculated net profit based on current expenses
	 */
	public double getRecalculatedNetProfit() {
		if (delivery == null) {
			return 0.0;
		}

		Double grossProfit = delivery.getGrossProfit();
		if (grossProfit == null) {
			grossProfit = 0.0;
		}

		double totalExpenses = delivery.getTotalExpenses();
		return grossProfit - totalExpenses;
	}

	@Override
	public String toString() {
		return "DeliveryDetailsState{" + "deliveryId=" + (delivery != null ? delivery.getId() : "null")
				+ ", deliveryName='" + (delivery != null ? delivery.getName() : "null") + '\'' + ", totalCustomers="
				+ (mappedCustomerDeliveries != null ? mappedCustomerDeliveries.size() : 0) + ", isLoaded=" + isLoaded()
				+ '}';
	}
}