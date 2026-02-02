package com.gierza_molases.molases_app.UiController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.swing.SwingWorker;

import com.gierza_molases.molases_app.context.DeliveryDetailsState;
import com.gierza_molases.molases_app.dto.delivery.DeliveryChanges;
import com.gierza_molases.molases_app.model.Branch;
import com.gierza_molases.molases_app.model.Customer;
import com.gierza_molases.molases_app.model.Delivery;
import com.gierza_molases.molases_app.model.ProductWithQuantity;
import com.gierza_molases.molases_app.model.response.DeliveryViewResponse;
import com.gierza_molases.molases_app.service.BranchDeliveryService;
import com.gierza_molases.molases_app.service.CustomerDeliveryService;
import com.gierza_molases.molases_app.service.DeliveryService;

public class DeliveryDetailsController {

	private final DeliveryDetailsState state;

	// service
	private final DeliveryService deliveryService;
	private final CustomerDeliveryService customerDeliveryService;
	private final BranchDeliveryService branchDeliveryService;

	public DeliveryDetailsController(DeliveryDetailsState state, DeliveryService deliveryService,
			CustomerDeliveryService customerDeliveryService, BranchDeliveryService branchDeliveryService) {
		this.state = state;
		this.deliveryService = deliveryService;
		this.customerDeliveryService = customerDeliveryService;
		this.branchDeliveryService = branchDeliveryService;
	}

	public DeliveryDetailsState getState() {
		return state;
	}

	/**
	 * Load delivery data from database by ID Fetches: Delivery, Customer
	 * Deliveries, Branch Deliveries, Products
	 * 
	 * @param deliveryId - ID of the delivery to load
	 * @param onSuccess  - Callback to run on successful load
	 * @param onError    - Callback to run on error (receives error message)
	 */
	public void loadDeliveryData(int deliveryId, Runnable onSuccess, Consumer<String> onError) {
		new SwingWorker<DeliveryViewResponse, Void>() {
			private Exception error;

			@Override
			protected DeliveryViewResponse doInBackground() {
				try {
					return deliveryService.getDeliveryDetials(deliveryId);
				} catch (Exception e) {
					error = e;
					return null;
				}
			}

			@Override
			protected void done() {
				if (error != null) {
					error.printStackTrace();
					if (onError != null) {
						onError.accept("Failed to load delivery: " + error.getMessage());
					}
				} else {
					try {
						DeliveryViewResponse response = get();

						if (response != null) {
							// Store data in state
							state.setDelivery(response.getDeliveryDetials());
							state.setMappedCustomerDeliveries(response.getMappedCustomerDeliveries());
							state.setRawCustomerBranchDeliveries(response.getCustomerDeliveries()); // ADD THIS LINE

							// Initialize payment types as "Not Set" for all customers
							if (response.getMappedCustomerDeliveries() != null) {
								for (Customer customer : response.getMappedCustomerDeliveries().keySet()) {
									state.setPaymentType(customer, "Not Set");
								}
							}

							if (onSuccess != null) {
								onSuccess.run();
							}
						} else {
							if (onError != null) {
								onError.accept("No delivery data found");
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
						if (onError != null) {
							onError.accept("Error processing delivery data: " + e.getMessage());
						}
					}
				}
			}
		}.execute();
	}

	/**
	 * Mark delivery as delivered and save all changes
	 */
	public void markDeliveryAsDelivered(Runnable onSuccess, Consumer<String> onError) {
		new SwingWorker<Void, Void>() {
			private Exception error;

			@Override
			protected Void doInBackground() {
				try {
					// Build DeliveryChanges DTO from state
					DeliveryChangesBuilder builder = new DeliveryChangesBuilder(state);
					DeliveryChanges deliveryChanges = builder.build();

					// Get updated delivery object
					Delivery delivery = state.getDelivery();

					// Call service to save all changes in transaction
					deliveryService.markAsDelivered(delivery, deliveryChanges);

				} catch (Exception e) {
					error = e;
				}
				return null;
			}

			@Override
			protected void done() {
				if (error != null) {
					error.printStackTrace();
					if (onError != null) {
						onError.accept("Failed to mark delivery as delivered: " + error.getMessage());
					}
				} else {
					if (onSuccess != null) {
						onSuccess.run();
					}
				}
			}
		}.execute();
	}

	/*
	 * ====================== Customer Delivery Operations ======================
	 */

	public void addAdditionalCustomer(Customer customer, Map<Branch, List<ProductWithQuantity>> branchProducts,
			Runnable onSuccess, Consumer<String> onError) {

		new SwingWorker<Void, Void>() {
			private Exception error;

			@Override
			protected Void doInBackground() {
				try {
					// check if the customer already exist
					if (state.getMappedCustomerDeliveries().containsKey(customer)) {
						error = new IllegalStateException(
								"Customer already exists in the delivery. You can add a new delivery on the existing customer delivery");
						return null;
					}

					state.addCustomerDelivery(customer, branchProducts);

				} catch (Exception e) {
					error = e;
				}
				return null;
			}

			@Override
			protected void done() {
				if (error != null) {
					error.printStackTrace();
					if (onError != null) {
						onError.accept("Failed to add customer: " + error.getMessage());
					}
				} else {
					if (onSuccess != null) {
						onSuccess.run();
					}
				}
			}
		}.execute();
	}

	/**
	 * Cancel a customer's entire delivery (all branches) This sets the customer
	 * status to "Cancelled"
	 */
	public void cancelCustomerDelivery(Customer customer, Runnable onSuccess, Consumer<String> onError) {
		new SwingWorker<Void, Void>() {
			private Exception error;

			@Override
			protected Void doInBackground() {
				try {
					// Validate input
					if (customer == null) {
						error = new IllegalArgumentException("Customer cannot be null");
						return null;
					}

					// Check if customer exists in delivery
					if (!state.getMappedCustomerDeliveries().containsKey(customer)) {
						error = new IllegalStateException("Customer does not exist in this delivery");
						return null;
					}

					// Cancel via state (this will set status to "Cancelled")
					state.cancelCustomerDelivery(customer);

				} catch (Exception e) {
					error = e;
				}
				return null;
			}

			@Override
			protected void done() {
				if (error != null) {
					error.printStackTrace();
					if (onError != null) {
						onError.accept("Failed to cancel customer delivery: " + error.getMessage());
					}
				} else {
					if (onSuccess != null) {
						onSuccess.run();
					}
				}
			}
		}.execute();
	}

	/**
	 * Set delivery status for a customer (Delivered/Cancelled) This triggers
	 * financial recalculation and is stored locally until saved to DB
	 */
	public void setCustomerDeliveryStatus(Customer customer, String status, Runnable onSuccess,
			Consumer<String> onError) {
		new SwingWorker<Void, Void>() {
			private Exception error;

			@Override
			protected Void doInBackground() {
				try {
					// Validate input
					if (customer == null) {
						error = new IllegalArgumentException("Customer cannot be null");
						return null;
					}

					// Check if customer exists in delivery
					if (!state.getMappedCustomerDeliveries().containsKey(customer)) {
						error = new IllegalStateException("Customer does not exist in this delivery");
						return null;
					}

					// Validate status
					if (status == null || (!status.equals("Delivered") && !status.equals("Cancelled"))) {
						error = new IllegalArgumentException("Status must be either 'Delivered' or 'Cancelled'");
						return null;
					}

					// Set customer status
					state.setCustomerDeliveryStatus(customer, status);

					// Set all branch statuses to match customer status
					Map<Branch, List<ProductWithQuantity>> customerBranches = state.getMappedCustomerDeliveries()
							.get(customer);
					if (customerBranches != null) {
						for (Branch branch : customerBranches.keySet()) {
							state.setBranchDeliveryStatus(branch, status);
						}
					}

					// Recalculate financials
					state.recalculateAllFinancials();

				} catch (Exception e) {
					error = e;
				}
				return null;
			}

			@Override
			protected void done() {
				if (error != null) {
					error.printStackTrace();
					if (onError != null) {
						onError.accept("Failed to update customer status: " + error.getMessage());
					}
				} else {
					if (onSuccess != null) {
						onSuccess.run();
					}
				}
			}
		}.execute();
	}

	/**
	 * Get delivery status for a specific customer
	 */
	public String getCustomerDeliveryStatus(Customer customer) {
		return state.getCustomerDeliveryStatus(customer);
	}

	/*
	 * ====================== Product Management Operations (NEW)
	 * ======================
	 */

	/**
	 * Add a product to an existing branch
	 */
	public void addProductToBranch(Customer customer, Branch branch, ProductWithQuantity productWithQty,
			Runnable onSuccess, Consumer<String> onError) {
		new SwingWorker<Void, Void>() {
			private Exception error;

			@Override
			protected Void doInBackground() {
				try {
					// Validate inputs
					if (customer == null) {
						error = new IllegalArgumentException("Customer cannot be null");
						return null;
					}
					if (branch == null) {
						error = new IllegalArgumentException("Branch cannot be null");
						return null;
					}
					if (productWithQty == null) {
						error = new IllegalArgumentException("Product cannot be null");
						return null;
					}

					// Check if customer exists
					if (!state.getMappedCustomerDeliveries().containsKey(customer)) {
						error = new IllegalStateException("Customer does not exist in this delivery");
						return null;
					}

					// Check if branch exists for this customer
					Map<Branch, List<ProductWithQuantity>> customerBranches = state.getMappedCustomerDeliveries()
							.get(customer);
					if (!customerBranches.containsKey(branch)) {
						error = new IllegalStateException("Branch does not exist for this customer");
						return null;
					}

					// Check if product already exists in this branch
					List<ProductWithQuantity> branchProducts = customerBranches.get(branch);
					for (ProductWithQuantity existing : branchProducts) {
						if (existing.getProduct().getId() == productWithQty.getProduct().getId()) {
							error = new IllegalStateException(
									"Product already exists in this branch. Use edit quantity instead.");
							return null;
						}
					}

					// Add product via state
					state.addProductToBranch(customer, branch, productWithQty);

				} catch (Exception e) {
					error = e;
				}
				return null;
			}

			@Override
			protected void done() {
				if (error != null) {
					error.printStackTrace();
					if (onError != null) {
						onError.accept("Failed to add product: " + error.getMessage());
					}
				} else {
					if (onSuccess != null) {
						onSuccess.run();
					}
				}
			}
		}.execute();
	}

	/**
	 * Remove a product from a branch
	 */
	public void removeProductFromBranch(Customer customer, Branch branch, ProductWithQuantity productToRemove,
			Runnable onSuccess, Consumer<String> onError) {
		new SwingWorker<Boolean, Void>() {
			private Exception error;
			private boolean result = false;

			@Override
			protected Boolean doInBackground() {
				try {
					// Validate inputs
					if (customer == null) {
						error = new IllegalArgumentException("Customer cannot be null");
						return false;
					}
					if (branch == null) {
						error = new IllegalArgumentException("Branch cannot be null");
						return false;
					}
					if (productToRemove == null) {
						error = new IllegalArgumentException("Product cannot be null");
						return false;
					}

					// Remove product via state
					result = state.removeProductFromBranch(customer, branch, productToRemove);

					if (!result) {
						error = new IllegalStateException(
								"Cannot remove product. It may be the last product in the branch.");
						return false;
					}

					return true;

				} catch (Exception e) {
					error = e;
					return false;
				}
			}

			@Override
			protected void done() {
				if (error != null) {
					error.printStackTrace();
					if (onError != null) {
						onError.accept("Failed to remove product: " + error.getMessage());
					}
				} else if (result) {
					if (onSuccess != null) {
						onSuccess.run();
					}
				} else {
					if (onError != null) {
						onError.accept("Failed to remove product");
					}
				}
			}
		}.execute();
	}

	/**
	 * Edit product quantity in a branch
	 */
	public void editProductQuantity(Customer customer, Branch branch, ProductWithQuantity productWithQty,
			int newQuantity, Runnable onSuccess, Consumer<String> onError) {
		new SwingWorker<Void, Void>() {
			private Exception error;

			@Override
			protected Void doInBackground() {
				try {
					// Validate inputs
					if (customer == null) {
						error = new IllegalArgumentException("Customer cannot be null");
						return null;
					}
					if (branch == null) {
						error = new IllegalArgumentException("Branch cannot be null");
						return null;
					}
					if (productWithQty == null) {
						error = new IllegalArgumentException("Product cannot be null");
						return null;
					}
					if (newQuantity <= 0) {
						error = new IllegalArgumentException("Quantity must be greater than 0");
						return null;
					}

					// Edit product quantity via state
					state.editProductQuantity(customer, branch, productWithQty, newQuantity);

				} catch (Exception e) {
					error = e;
				}
				return null;
			}

			@Override
			protected void done() {
				if (error != null) {
					error.printStackTrace();
					if (onError != null) {
						onError.accept("Failed to edit quantity: " + error.getMessage());
					}
				} else {
					if (onSuccess != null) {
						onSuccess.run();
					}
				}
			}
		}.execute();
	}

	/*
	 * ====================== Expense Operations ======================
	 */

	/**
	 * Add an expense to the delivery
	 */
	public void addExpense(String name, Double amount, Runnable onSuccess, Consumer<String> onError) {
		// TODO: Implement database update for expenses
		// For now, just update state
		new SwingWorker<Void, Void>() {
			private Exception error;

			@Override
			protected Void doInBackground() {
				try {

					state.addExpense(name, amount);
				} catch (Exception e) {
					error = e;
				}
				return null;
			}

			@Override
			protected void done() {
				if (error != null) {
					error.printStackTrace();
					if (onError != null) {
						onError.accept("Failed to add expense: " + error.getMessage());
					}
				} else {
					if (onSuccess != null) {
						onSuccess.run();
					}
				}
			}
		}.execute();
	}

	/**
	 * Remove an expense from the delivery
	 */
	public void removeExpense(String name, Runnable onSuccess, Consumer<String> onError) {

		new SwingWorker<Void, Void>() {
			private Exception error;

			@Override
			protected Void doInBackground() {
				try {

					state.removeExpense(name);
				} catch (Exception e) {
					error = e;
				}
				return null;
			}

			@Override
			protected void done() {
				if (error != null) {
					error.printStackTrace();
					if (onError != null) {
						onError.accept("Failed to remove expense: " + error.getMessage());
					}
				} else {
					if (onSuccess != null) {
						onSuccess.run();
					}
				}
			}
		}.execute();
	}

	/*
	 * ====================== Payment Type Operations ======================
	 */

	/**
	 * Set payment type for a customer (temporary, not saved until delivery is
	 * marked as delivered)
	 */
	public void setTemporaryPaymentType(Customer customer, String paymentType) {
		state.setPaymentType(customer, paymentType);
	}

	/*
	 * ====================== Branch Delivery Status Operations
	 * ======================
	 */

	/**
	 * Add a new branch with products to an existing customer delivery
	 */
	public void addBranchToCustomer(Customer customer, Branch branch, List<ProductWithQuantity> products,
			Runnable onSuccess, Consumer<String> onError) {
		new SwingWorker<Void, Void>() {
			private Exception error;

			@Override
			protected Void doInBackground() {
				try {
					// Validate inputs
					if (customer == null) {
						error = new IllegalArgumentException("Customer cannot be null");
						return null;
					}
					if (branch == null) {
						error = new IllegalArgumentException("Branch cannot be null");
						return null;
					}
					if (products == null || products.isEmpty()) {
						error = new IllegalArgumentException("Branch must have at least one product");
						return null;
					}

					// Check if customer exists in delivery
					if (!state.getMappedCustomerDeliveries().containsKey(customer)) {
						error = new IllegalStateException("Customer does not exist in this delivery");
						return null;
					}

					// Check if branch already exists for this customer
					Map<Branch, List<ProductWithQuantity>> customerBranches = state.getMappedCustomerDeliveries()
							.get(customer);
					for (Branch existingBranch : customerBranches.keySet()) {
						if (existingBranch.getId() == branch.getId()) {
							error = new IllegalStateException("Branch already exists for this customer");
							return null;
						}
					}

					// Add branch to customer's deliveries
					customerBranches.put(branch, new ArrayList<>(products));

					// Initialize branch status as "Delivered"
					state.setBranchDeliveryStatus(branch, "Delivered");

					// Track this branch as newly added
					state.trackAddedBranch(customer, branch, products); // ADD THIS LINE

					// Recalculate financials
					state.recalculateAllFinancials();

				} catch (Exception e) {
					error = e;
				}
				return null;
			}

			@Override
			protected void done() {
				if (error != null) {
					error.printStackTrace();
					if (onError != null) {
						onError.accept("Failed to add branch: " + error.getMessage());
					}
				} else {
					if (onSuccess != null) {
						onSuccess.run();
					}
				}
			}
		}.execute();
	}

	/**
	 * Set delivery status for a branch (Delivered/Cancelled) This triggers
	 * financial recalculation and is stored locally until saved to DB
	 */
	public void setBranchDeliveryStatus(Branch branch, String status, Runnable onSuccess, Consumer<String> onError) {
		new SwingWorker<Void, Void>() {
			private Exception error;

			@Override
			protected Void doInBackground() {
				try {
					state.setBranchDeliveryStatus(branch, status);
					state.recalculateAllFinancials();
				} catch (Exception e) {
					error = e;
				}
				return null;
			}

			@Override
			protected void done() {
				if (error != null) {
					error.printStackTrace();
					if (onError != null) {
						onError.accept("Failed to update branch status: " + error.getMessage());
					}
				} else {
					if (onSuccess != null) {
						onSuccess.run();
					}
				}
			}
		}.execute();
	}

	/**
	 * Get delivery status for a specific branch
	 */
	public String getBranchDeliveryStatus(Branch branch) {
		return state.getBranchDeliveryStatus(branch);
	}

	/*
	 * ====================== State Management ======================
	 */

	/**
	 * Reset state - called when navigating away from details page
	 */
	public void resetState() {
		state.resetState();
	}

	/**
	 * Check if delivery data is loaded
	 */
	public boolean isDataLoaded() {
		return state.isLoaded();
	}

	/*
	 * ====================== Save Operations (TODO) ======================
	 */

	/**
	 * Save all branch delivery statuses to database Called when marking delivery as
	 * delivered
	 */
	public void saveBranchDeliveryStatuses(Runnable onSuccess, Consumer<String> onError) {
		// TODO: Implement database save for branch delivery statuses
		// This should be called when "Mark as Delivered" is clicked
		// Save the branchDeliveryStatuses map to the database
		new SwingWorker<Void, Void>() {
			private Exception error;

			@Override
			protected Void doInBackground() {
				try {
					// TODO: Call service method to save branch statuses to DB
					// deliveryService.saveBranchStatuses(deliveryId,
					// state.getBranchDeliveryStatuses());

					// TODO: Save customer statuses to DB
					// deliveryService.saveCustomerStatuses(deliveryId,
					// state.getCustomerDeliveryStatuses());

					// TODO: Save removed products to DB
					// deliveryService.saveRemovedProducts(deliveryId, state.getRemovedProducts());

					// TODO: Save added products to DB
					// deliveryService.saveAddedProducts(deliveryId, state.getAddedProducts());

					// TODO: Save edited product quantities to DB
					// deliveryService.saveEditedProductQuantities(deliveryId,
					// state.getEditedProductQuantities());

					throw new UnsupportedOperationException("Database save not yet implemented");
				} catch (Exception e) {
					error = e;
				}
				return null;
			}

			@Override
			protected void done() {
				if (error != null) {
					error.printStackTrace();
					if (onError != null) {
						onError.accept("Failed to save statuses: " + error.getMessage());
					}
				} else {
					if (onSuccess != null) {
						onSuccess.run();
					}
				}
			}
		}.execute();
	}
}