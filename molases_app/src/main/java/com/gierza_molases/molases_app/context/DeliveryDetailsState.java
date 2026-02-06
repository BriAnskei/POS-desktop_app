package com.gierza_molases.molases_app.context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.gierza_molases.molases_app.dto.delivery.CustomerBranchDelivery;
import com.gierza_molases.molases_app.model.Branch;
import com.gierza_molases.molases_app.model.Customer;
import com.gierza_molases.molases_app.model.Delivery;
import com.gierza_molases.molases_app.model.Product;
import com.gierza_molases.molases_app.model.ProductWithQuantity;

public class DeliveryDetailsState {

	// Delivery information
	private Delivery delivery;

	// raw data
	private List<CustomerBranchDelivery> rawCustomerBranchDeliveries;

	// Mapped customer deliveries data (Customer -> Branch -> Products)
	private Map<Customer, Map<Branch, List<ProductWithQuantity>>> mappedCustomerDeliveries;

	// Newly added customer deliveries
	private Map<Customer, Map<Branch, List<ProductWithQuantity>>> additionalCustomerDelivery;

	// Temporary payment types tracked in UI (not yet saved to DB)
	// This will be populated when user sets payment type before marking as
	// delivered
	private Map<Customer, String> temporaryPaymentTypes;

	// Customer delivery statuses (Delivered/Cancelled) - tracked locally until
	// saved to DB
	private Map<Customer, String> customerDeliveryStatuses;

	// Branch delivery statuses (Delivered/Cancelled) - tracked locally until saved
	// to DB
	private Map<Branch, String> branchDeliveryStatuses;

	// ----------------New data holder

	// NEW: Track removed products from branches
	private Map<Branch, List<ProductWithQuantity>> removedProducts;

	// NEW: Track newly added products to existing branches
	private Map<Branch, List<ProductWithQuantity>> addedProducts;

	// NEW: Track edited product quantities (Branch -> Product -> QuantityChange)
	private Map<Branch, Map<Product, QuantityChange>> editedProductQuantities;

	// NEW: Track newly added branches to existing customers
	private Map<Customer, Map<Branch, List<ProductWithQuantity>>> addedBranches;

	// NEW: Track original expenses at load time (for calculating new expenses)
	private Map<String, Double> originalExpenses;

	// Inner class to track quantity changes
	public static class QuantityChange {
		private final int originalQuantity;
		private int newQuantity;

		public QuantityChange(int originalQuantity, int newQuantity) {
			this.originalQuantity = originalQuantity;
			this.newQuantity = newQuantity;
		}

		public int getOriginalQuantity() {
			return originalQuantity;
		}

		public int getNewQuantity() {
			return newQuantity;
		}

		public void setNewQuantity(int newQuantity) {
			this.newQuantity = newQuantity;
		}

		public int getQuantityDifference() {
			return newQuantity - originalQuantity;
		}
	}

	public DeliveryDetailsState() {
		this.mappedCustomerDeliveries = new HashMap<>();
		this.additionalCustomerDelivery = new HashMap<>();
		this.temporaryPaymentTypes = new HashMap<>();
		this.customerDeliveryStatuses = new HashMap<>();
		this.branchDeliveryStatuses = new HashMap<>();
		this.removedProducts = new HashMap<>();
		this.addedProducts = new HashMap<>();
		this.editedProductQuantities = new HashMap<>();
		this.addedBranches = new HashMap<>();
		this.originalExpenses = new HashMap<>();
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
	 * Get customer delivery statuses map
	 */
	public Map<Customer, String> getCustomerDeliveryStatuses() {
		return customerDeliveryStatuses;
	}

	/**
	 * Get delivery status for a specific customer Returns "Delivered" if no status
	 * has been set
	 */
	public String getCustomerDeliveryStatus(Customer customer) {
		return customerDeliveryStatuses.getOrDefault(customer, "Delivered");
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

	/**
	 * Get removed products map
	 */
	public Map<Branch, List<ProductWithQuantity>> getRemovedProducts() {
		return removedProducts;
	}

	/**
	 * Get added products map
	 */
	public Map<Branch, List<ProductWithQuantity>> getAddedProducts() {
		return addedProducts;
	}

	/**
	 * Get edited product quantities map
	 */
	public Map<Branch, Map<Product, QuantityChange>> getEditedProductQuantities() {
		return editedProductQuantities;
	}

	/**
	 * Get added branches map
	 */
	public Map<Customer, Map<Branch, List<ProductWithQuantity>>> getAddedBranches() {
		return addedBranches;
	}

	/**
	 * Get original expenses (at load time)
	 */
	public Map<String, Double> getOriginalExpenses() {
		return originalExpenses;
	}

	public List<CustomerBranchDelivery> getRawCustomerBranchDeliveries() {
		return rawCustomerBranchDeliveries;
	}

	/*
	 * ====================== Setters ======================
	 */

	public void setDelivery(Delivery delivery) {
		this.delivery = delivery;
	}

	public void setRawCustomerBranchDeliveries(List<CustomerBranchDelivery> rawData) {
		this.rawCustomerBranchDeliveries = rawData != null ? new ArrayList<>(rawData) : new ArrayList<>();
	}

	public void setMappedCustomerDeliveries(
			Map<Customer, Map<Branch, List<ProductWithQuantity>>> mappedCustomerDeliveries) {
		this.mappedCustomerDeliveries = mappedCustomerDeliveries != null ? new HashMap<>(mappedCustomerDeliveries)
				: new HashMap<>();

		// Store original expenses when delivery is loaded
		if (delivery != null && delivery.getExpenses() != null) {
			this.originalExpenses = new HashMap<>(delivery.getExpenses());
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
	 * Set delivery status for a specific customer
	 */
	public void setCustomerDeliveryStatus(Customer customer, String status) {
		if (customer != null && status != null) {
			this.customerDeliveryStatuses.put(customer, status);
		}

		if ("Cancelled".equals(status)) {
			setPaymentType(customer, "N/A");
		} else if ("Delivered".equals(status) && "N/A".equals(getPaymentType(customer))) {
			setPaymentType(customer, "Not Set");
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
		this.rawCustomerBranchDeliveries = new ArrayList<>();
		this.temporaryPaymentTypes = new HashMap<>();
		this.customerDeliveryStatuses = new HashMap<>();
		this.branchDeliveryStatuses = new HashMap<>();
		this.additionalCustomerDelivery = new HashMap<>();
		this.removedProducts = new HashMap<>();
		this.addedProducts = new HashMap<>();
		this.editedProductQuantities = new HashMap<>();
		this.addedBranches = new HashMap<>();
		this.originalExpenses = new HashMap<>();
	}

	/**
	 * Check if state has delivery data loaded
	 */
	public boolean isLoaded() {
		return delivery != null && mappedCustomerDeliveries != null && !mappedCustomerDeliveries.isEmpty();
	}

	public boolean wasCancelledCustomerNewlyAdded(Customer customer) {
		return this.getAdditionalCustomerDelivery().containsKey(customer);
	}

	/*
	 * ====================== Utility Methods ======================
	 */

	/**
	 * Recalculate all financial metrics based on current customer deliveries
	 * Excludes customers with "Cancelled" status and branches with "Cancelled"
	 * status from calculations
	 */
	public void recalculateAllFinancials() {
		if (delivery == null || mappedCustomerDeliveries == null) {
			return;
		}

		double totalGross = 0.0;
		double totalCapital = 0.0;

		// Calculate totals from all customer deliveries, excluding cancelled customers
		// and cancelled branches
		for (Map.Entry<Customer, Map<Branch, List<ProductWithQuantity>>> entry : mappedCustomerDeliveries.entrySet()) {
			Customer customer = entry.getKey();
			Map<Branch, List<ProductWithQuantity>> branches = entry.getValue();

			// Skip cancelled customers
			String customerStatus = getCustomerDeliveryStatus(customer);
			if ("Cancelled".equalsIgnoreCase(customerStatus)) {
				continue;
			}

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

			// Initialize customer status as "Delivered"
			this.customerDeliveryStatuses.putIfAbsent(customer, "Delivered");

			// Initialize branch statuses as "Delivered" for new branches
			for (Branch branch : branchProducts.keySet()) {
				if (branch != null) {
					this.branchDeliveryStatuses.putIfAbsent(branch, "Delivered");
				}

			}

			// update customer and branch count
			delivery.setTotalCustomers(delivery.getTotalCustomers() + 1); // increament count
			delivery.setTotalBranches(delivery.getTotalBranches() + branchProducts.size());

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
			this.customerDeliveryStatuses.remove(customer);
		}
	}

	/**
	 * Cancel a customer's entire delivery (all branches) Sets the customer status
	 * to "Cancelled" instead of removing them
	 */
	public void cancelCustomerDelivery(Customer customer) {
		if (customer == null) {
			return;
		}

		// Get the customer's current branches and products
		Map<Branch, List<ProductWithQuantity>> customerBranches = this.mappedCustomerDeliveries.get(customer);

		if (customerBranches == null || customerBranches.isEmpty()) {
			return; // Customer doesn't exist or has no branches
		}

		// Set customer status to "Cancelled" instead of removing
		this.customerDeliveryStatuses.put(customer, "Cancelled");

		// Set all branch statuses to "Cancelled" as well
		for (Branch branch : customerBranches.keySet()) {
			this.branchDeliveryStatuses.put(branch, "Cancelled");
		}

		// Recalculate financials (cancelled customers will be excluded automatically)
		recalculateAllFinancials();
	}

	/*
	 * ====================== Product/Branch Management Methods (NEW)
	 * ======================
	 */

	/**
	 * Track a newly added branch for an existing customer
	 */
	public void trackAddedBranch(Customer customer, Branch branch, List<ProductWithQuantity> products) {
		if (customer == null || branch == null || products == null) {
			return;
		}

		// Initialize customer entry if needed
		addedBranches.putIfAbsent(customer, new HashMap<>());

		// Add the branch with its products
		addedBranches.get(customer).put(branch, new ArrayList<>(products));
	}

	/**
	 * Add a product to an existing branch Tracks in addedProducts and updates
	 * mappedCustomerDeliveries
	 */
	public void addProductToBranch(Customer customer, Branch branch, ProductWithQuantity productWithQty) {
		if (customer == null || branch == null || productWithQty == null) {
			return;
		}

		// Get customer's branches
		Map<Branch, List<ProductWithQuantity>> customerBranches = mappedCustomerDeliveries.get(customer);
		if (customerBranches == null) {
			throw new IllegalStateException("Customer does not exist in the collection");
		}

		// Get branch's products
		List<ProductWithQuantity> branchProducts = customerBranches.get(branch);
		if (branchProducts == null) {
			throw new IllegalStateException("Branch does not exist in the collection");
		}

		// Check if product already exists in this branch
		for (ProductWithQuantity existing : branchProducts) {
			if (existing.getProduct().getId() == productWithQty.getProduct().getId()) {
				// Product already exists, don't add duplicate
				return;
			}
		}

		// Add to mappedCustomerDeliveries
		branchProducts.add(productWithQty);

		// Track in addedProducts
		addedProducts.putIfAbsent(branch, new ArrayList<>());
		addedProducts.get(branch).add(productWithQty);

		// Recalculate financials
		recalculateAllFinancials();
	}

	/**
	 * Remove a product from a branch Tracks in removedProducts and updates
	 * mappedCustomerDeliveries Returns false if it's the last product (not allowed
	 * to remove)
	 */
	public boolean removeProductFromBranch(Customer customer, Branch branch, ProductWithQuantity productToRemove) {
		if (customer == null || branch == null || productToRemove == null) {
			return false;
		}

		// Get customer's branches
		Map<Branch, List<ProductWithQuantity>> customerBranches = mappedCustomerDeliveries.get(customer);
		if (customerBranches == null) {
			return false;
		}

		// Get branch's products
		List<ProductWithQuantity> branchProducts = customerBranches.get(branch);
		if (branchProducts == null || branchProducts.isEmpty()) {
			return false;
		}

		// Check if this is the last product
		if (branchProducts.size() <= 1) {
			return false; // Cannot remove last product
		}

		// Check if this product was newly added (in addedProducts)
		boolean wasRecentlyAdded = false;
		if (addedProducts.containsKey(branch)) {
			List<ProductWithQuantity> addedList = addedProducts.get(branch);
			for (int i = 0; i < addedList.size(); i++) {
				ProductWithQuantity added = addedList.get(i);
				if (added.getProduct().getId() == productToRemove.getProduct().getId()) {
					// Remove from addedProducts instead of tracking in removedProducts
					addedList.remove(i);
					if (addedList.isEmpty()) {
						addedProducts.remove(branch);
					}
					wasRecentlyAdded = true;
					break;
				}
			}
		}

		// Remove from mappedCustomerDeliveries
		branchProducts.remove(productToRemove);

		// If it wasn't recently added, track in removedProducts
		if (!wasRecentlyAdded) {
			removedProducts.putIfAbsent(branch, new ArrayList<>());
			removedProducts.get(branch).add(productToRemove);
		}

		// Remove from editedProductQuantities if it exists there
		if (editedProductQuantities.containsKey(branch)) {
			editedProductQuantities.get(branch).remove(productToRemove.getProduct());
			if (editedProductQuantities.get(branch).isEmpty()) {
				editedProductQuantities.remove(branch);
			}
		}

		// Recalculate financials
		recalculateAllFinancials();

		return true;
	}

	/**
	 * Edit product quantity in a branch Tracks in editedProductQuantities and
	 * updates mappedCustomerDeliveries
	 */
	public void editProductQuantity(Customer customer, Branch branch, ProductWithQuantity productWithQty,
			int newQuantity) {
		if (customer == null || branch == null || productWithQty == null || newQuantity <= 0) {
			return;
		}

		// Get customer's branches
		Map<Branch, List<ProductWithQuantity>> customerBranches = mappedCustomerDeliveries.get(customer);
		if (customerBranches == null) {
			throw new IllegalStateException("Customer does not exist in the collection");

		}

		// Get branch's products
		List<ProductWithQuantity> branchProducts = customerBranches.get(branch);
		if (branchProducts == null) {
			throw new IllegalStateException("branch does not exist in the collection");
		}

		// Find the product and update its quantity
		int originalQuantity = productWithQty.getQuantity();

		for (ProductWithQuantity product : branchProducts) {
			if (product.getProduct().getId() == productWithQty.getProduct().getId()) {

				// Check if this product was recently added
				boolean wasRecentlyAdded = false;
				if (addedProducts.containsKey(branch)) {
					for (ProductWithQuantity added : addedProducts.get(branch)) {
						if (added.getProduct().getId() == product.getProduct().getId()) {
							// Update quantity in addedProducts
							added.setQuantity(newQuantity);
							wasRecentlyAdded = true;
							break;
						}
					}
				}

				// Update quantity in mappedCustomerDeliveries
				product.setQuantity(newQuantity);

				// Track in editedProductQuantities only if it wasn't recently added
				if (!wasRecentlyAdded) {
					editedProductQuantities.putIfAbsent(branch, new HashMap<>());
					Map<Product, QuantityChange> branchEdits = editedProductQuantities.get(branch);

					if (branchEdits.containsKey(product.getProduct())) {
						// Update existing quantity change
						branchEdits.get(product.getProduct()).setNewQuantity(newQuantity);
					} else {
						// Create new quantity change record
						branchEdits.put(product.getProduct(), new QuantityChange(originalQuantity, newQuantity));
					}
				}

				break;
			}
		}

		// Recalculate financials
		recalculateAllFinancials();
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
				+ ", removedProducts=" + removedProducts.size() + ", addedProducts=" + addedProducts.size()
				+ ", editedProducts=" + editedProductQuantities.size() + ", addedBranches=" + addedBranches.size()
				+ ", originalExpenses=" + originalExpenses.size() + '}';
	}
}