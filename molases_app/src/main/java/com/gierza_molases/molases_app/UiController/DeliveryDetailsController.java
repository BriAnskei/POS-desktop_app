package com.gierza_molases.molases_app.UiController;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.swing.SwingWorker;

import com.gierza_molases.molases_app.context.DeliveryDetailsState;
import com.gierza_molases.molases_app.model.Branch;
import com.gierza_molases.molases_app.model.Customer;
import com.gierza_molases.molases_app.model.ProductWithQuantity;
import com.gierza_molases.molases_app.model.response.DeliveryViewResponse;
import com.gierza_molases.molases_app.service.DeliveryService;

public class DeliveryDetailsController {

	private final DeliveryDetailsState state;
	private final DeliveryService deliveryService;

	public DeliveryDetailsController(DeliveryDetailsState state, DeliveryService deliveryService) {
		this.state = state;
		this.deliveryService = deliveryService;
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
						error = new IllegalStateException("Customer already exists in the delivery.");
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
	 * Remove a customer from the delivery
	 */
	public void removeCustomerDelivery(Customer customer) {
		// TODO: Implement database deletion
		// For now, just remove from state
		state.removeCustomerDelivery(customer);
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
					// TODO: Call service method to update delivery expenses
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
		// TODO: Implement database update for expenses
		// For now, just update state
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
}