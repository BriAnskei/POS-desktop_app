package com.gierza_molases.molases_app.context;

import java.util.List;
import java.util.Map;

import com.gierza_molases.molases_app.model.Branch;
import com.gierza_molases.molases_app.model.Customer;
import com.gierza_molases.molases_app.model.Product;
import com.gierza_molases.molases_app.model.ProductWithQuantity;

/**
 * Utility class to calculate financial changes in a delivery Used before
 * marking a delivery as delivered to show user the impact of their changes
 */
public class DeliveryChangesCalculator {

	/**
	 * Container for all financial change calculations
	 */
	public static class FinancialChanges {
		// Summary totals
		public double moneyReturned;
		public double moneyAdded;
		public double newExpenses;
		public double netChange;

		// Detailed breakdowns (for future expansion/tooltips)
		public double removedProductsTotal;
		public double cancelledBranchesTotal;
		public double removedExpensesTotal;
		public double addedProductsTotal;
		public double increasedQuantitiesTotal;
		public double newBranchesTotal;
		public double newCustomersTotal;
		public double addedExpensesTotal;

		public FinancialChanges() {
			this.moneyReturned = 0.0;
			this.moneyAdded = 0.0;
			this.newExpenses = 0.0;
			this.netChange = 0.0;
			this.removedProductsTotal = 0.0;
			this.cancelledBranchesTotal = 0.0;
			this.removedExpensesTotal = 0.0;
			this.addedProductsTotal = 0.0;
			this.increasedQuantitiesTotal = 0.0;
			this.newBranchesTotal = 0.0;
			this.newCustomersTotal = 0.0;
			this.addedExpensesTotal = 0.0;
		}

		/**
		 * Calculate net change
		 */
		public void calculateNetChange() {
			this.netChange = this.moneyAdded - this.moneyReturned - this.newExpenses;
		}
	}

	/**
	 * Calculate all financial changes from the delivery state
	 * 
	 * @param state - The current delivery details state
	 * @return FinancialChanges object with all calculations
	 */
	public static FinancialChanges calculate(DeliveryDetailsState state) {
		FinancialChanges changes = new FinancialChanges();

		if (state == null || !state.isLoaded()) {
			return changes;
		}

		// Calculate money returned
		calculateMoneyReturned(state, changes);

		// Calculate money added
		calculateMoneyAdded(state, changes);

		// Calculate new expenses
		calculateNewExpenses(state, changes);

		// Calculate net change
		changes.calculateNetChange();

		return changes;
	}

	/**
	 * Calculate money returned from: 1. Removed products 2. Cancelled branches
	 * (only products still existing) 3. Decreased quantities 4. Cancelled customer
	 * deliveries
	 */
	private static void calculateMoneyReturned(DeliveryDetailsState state, FinancialChanges changes) {

		// Removed products
		calculateRemovedProducts(state, changes);
		// 2. Cancelled branches (only count products still existing at cancellation)
		calculteCancelledBranches(state, changes);

		// 3. Decreased quantities
		calculateDecreasedQuantities(state, changes);

		// 4. Cancelled customer deliveries
		calculateCancelledCustomerDeliveries(state, changes);
		// Calculate total money returned
		changes.moneyReturned = changes.removedProductsTotal + changes.cancelledBranchesTotal
				+ changes.removedExpensesTotal;
	}

	private static void calculateRemovedProducts(DeliveryDetailsState state, FinancialChanges changes) {
		Map<Branch, List<ProductWithQuantity>> removedProducts = state.getRemovedProducts();
		if (removedProducts != null) {
			for (Map.Entry<Branch, List<ProductWithQuantity>> entry : removedProducts.entrySet()) {
				List<ProductWithQuantity> products = entry.getValue();
				if (products != null) {
					for (ProductWithQuantity product : products) {
						double productTotal = product.getTotalSellingPrice();
						changes.removedProductsTotal += productTotal;
					}
				}
			}
		}

	}

	private static void calculteCancelledBranches(DeliveryDetailsState state, FinancialChanges changes) {
		Map<Branch, String> branchStatuses = state.getBranchDeliveryStatuses();
		Map<Customer, Map<Branch, List<ProductWithQuantity>>> customerDeliveries = state.getMappedCustomerDeliveries();

		if (branchStatuses != null && customerDeliveries != null) {
			for (Map.Entry<Branch, String> statusEntry : branchStatuses.entrySet()) {
				Branch branch = statusEntry.getKey();
				String status = statusEntry.getValue();

				if ("Cancelled".equalsIgnoreCase(status)) {
					// Find this branch in customer deliveries and sum its current products
					for (Map.Entry<Customer, Map<Branch, List<ProductWithQuantity>>> customerEntry : customerDeliveries
							.entrySet()) {
						Map<Branch, List<ProductWithQuantity>> branches = customerEntry.getValue();

						if (branches != null && branches.containsKey(branch)) {
							List<ProductWithQuantity> products = branches.get(branch);
							if (products != null) {
								for (ProductWithQuantity product : products) {
									double productTotal = product.getTotalSellingPrice();
									changes.cancelledBranchesTotal += productTotal;
								}
							}
							break; // Found the branch, no need to continue
						}
					}
				}
			}
		}
	}

	private static void calculateDecreasedQuantities(DeliveryDetailsState state, FinancialChanges changes) {
		Map<Branch, Map<Product, DeliveryDetailsState.QuantityChange>> editedQuantities = state
				.getEditedProductQuantities();
		if (editedQuantities != null) {
			for (Map.Entry<Branch, Map<Product, DeliveryDetailsState.QuantityChange>> branchEntry : editedQuantities
					.entrySet()) {
				Map<Product, DeliveryDetailsState.QuantityChange> productChanges = branchEntry.getValue();
				if (productChanges != null) {
					for (Map.Entry<Product, DeliveryDetailsState.QuantityChange> productEntry : productChanges
							.entrySet()) {
						Product product = productEntry.getKey();
						DeliveryDetailsState.QuantityChange change = productEntry.getValue();

						// Only count decreases (newQty < originalQty)
						if (change.getNewQuantity() < change.getOriginalQuantity()) {
							int qtyDiff = change.getOriginalQuantity() - change.getNewQuantity();
							double priceDiff = qtyDiff * product.getSellingPrice();
							// Add to removedProductsTotal since it's the same concept
							changes.removedProductsTotal += priceDiff;
						}
					}
				}
			}
		}
	}

	private static void calculateCancelledCustomerDeliveries(DeliveryDetailsState state, FinancialChanges changes) {
		Map<Customer, Map<Branch, List<ProductWithQuantity>>> cancelledCustomers = state
				.getCancelledCustomerDeliveries();
		if (cancelledCustomers != null) {

			for (Map.Entry<Customer, Map<Branch, List<ProductWithQuantity>>> customerEntry : cancelledCustomers
					.entrySet()) {
				Customer customer = customerEntry.getKey();
				Map<Branch, List<ProductWithQuantity>> branches = customerEntry.getValue();

				boolean wasNewlyAdded = state.wasCancelledCustomerNewlyAdded(customer);

				// Only count as "money returned" if it was an ORIGINAL customer
				// If it was newly added, it should NOT show in money returned
				if (!wasNewlyAdded && branches != null) {
					for (Map.Entry<Branch, List<ProductWithQuantity>> branchEntry : branches.entrySet()) {
						List<ProductWithQuantity> products = branchEntry.getValue();
						if (products != null) {
							for (ProductWithQuantity product : products) {
								double productTotal = product.getTotalSellingPrice();
								changes.cancelledBranchesTotal += productTotal;
							}
						}
					}
				}
			}
		}

	}

	/**
	 * Calculate money added from: 1. Added products 2. Increased quantities 3. New
	 * branches 4. New customers
	 */
	private static void calculateMoneyAdded(DeliveryDetailsState state, FinancialChanges changes) {
		// 1. Added products
		Map<Branch, List<ProductWithQuantity>> addedProducts = state.getAddedProducts();
		if (addedProducts != null) {
			for (Map.Entry<Branch, List<ProductWithQuantity>> entry : addedProducts.entrySet()) {
				List<ProductWithQuantity> products = entry.getValue();
				if (products != null) {
					for (ProductWithQuantity product : products) {
						double productTotal = product.getTotalSellingPrice();
						changes.addedProductsTotal += productTotal;
					}
				}
			}
		}

		// 2. Increased quantities
		Map<Branch, Map<Product, DeliveryDetailsState.QuantityChange>> editedQuantities = state
				.getEditedProductQuantities();
		if (editedQuantities != null) {
			for (Map.Entry<Branch, Map<Product, DeliveryDetailsState.QuantityChange>> branchEntry : editedQuantities
					.entrySet()) {
				Map<Product, DeliveryDetailsState.QuantityChange> productChanges = branchEntry.getValue();
				if (productChanges != null) {
					for (Map.Entry<Product, DeliveryDetailsState.QuantityChange> productEntry : productChanges
							.entrySet()) {
						Product product = productEntry.getKey();
						DeliveryDetailsState.QuantityChange change = productEntry.getValue();

						// Only count increases (newQty > originalQty)
						if (change.getNewQuantity() > change.getOriginalQuantity()) {
							int qtyDiff = change.getNewQuantity() - change.getOriginalQuantity();
							double priceDiff = qtyDiff * product.getSellingPrice();
							changes.increasedQuantitiesTotal += priceDiff;
						}
					}
				}
			}
		}

		// 3. New branches (added to existing customers)
		Map<Customer, Map<Branch, List<ProductWithQuantity>>> addedBranches = state.getAddedBranches();
		if (addedBranches != null) {
			for (Map.Entry<Customer, Map<Branch, List<ProductWithQuantity>>> customerEntry : addedBranches.entrySet()) {
				Map<Branch, List<ProductWithQuantity>> branches = customerEntry.getValue();
				if (branches != null) {
					for (Map.Entry<Branch, List<ProductWithQuantity>> branchEntry : branches.entrySet()) {
						List<ProductWithQuantity> products = branchEntry.getValue();
						if (products != null) {
							for (ProductWithQuantity product : products) {
								double productTotal = product.getTotalSellingPrice();
								changes.newBranchesTotal += productTotal;
							}
						}
					}
				}
			}
		}

		// 4. New customers (entirely new customer deliveries)
		Map<Customer, Map<Branch, List<ProductWithQuantity>>> additionalCustomers = state
				.getAdditionalCustomerDelivery();
		if (additionalCustomers != null) {
			for (Map.Entry<Customer, Map<Branch, List<ProductWithQuantity>>> customerEntry : additionalCustomers
					.entrySet()) {
				Map<Branch, List<ProductWithQuantity>> branches = customerEntry.getValue();
				if (branches != null) {
					for (Map.Entry<Branch, List<ProductWithQuantity>> branchEntry : branches.entrySet()) {
						List<ProductWithQuantity> products = branchEntry.getValue();
						if (products != null) {
							for (ProductWithQuantity product : products) {
								double productTotal = product.getTotalSellingPrice();
								changes.newCustomersTotal += productTotal;
							}
						}
					}
				}
			}
		}

		// Calculate total money added
		changes.moneyAdded = changes.addedProductsTotal + changes.increasedQuantitiesTotal + changes.newBranchesTotal
				+ changes.newCustomersTotal;
	}

	/**
	 * Calculate new expenses (expenses added after initial load)
	 */
	private static void calculateNewExpenses(DeliveryDetailsState state, FinancialChanges changes) {
		if (state.getDelivery() == null) {
			return;
		}

		Map<String, Double> currentExpenses = state.getDelivery().getExpenses();
		Map<String, Double> originalExpenses = state.getOriginalExpenses();

		if (currentExpenses == null) {
			return;
		}

		// Compare current expenses with original expenses
		for (Map.Entry<String, Double> entry : currentExpenses.entrySet()) {
			String expenseName = entry.getKey();
			Double currentAmount = entry.getValue();

			// If expense doesn't exist in original, it's newly added
			if (originalExpenses == null || !originalExpenses.containsKey(expenseName)) {
				changes.addedExpensesTotal += currentAmount;
			}
		}

		// Calculate removed expenses (for completeness, though not currently used in
		// UI)
		if (originalExpenses != null) {
			for (Map.Entry<String, Double> entry : originalExpenses.entrySet()) {
				String expenseName = entry.getKey();
				Double originalAmount = entry.getValue();

				// If expense existed originally but not now, it was removed
				if (!currentExpenses.containsKey(expenseName)) {
					changes.removedExpensesTotal += originalAmount;
				}
			}
		}

		changes.newExpenses = changes.addedExpensesTotal;
	}
}