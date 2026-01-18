package com.gierza_molases.molases_app.UiController;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.swing.SwingWorker;

import com.gierza_molases.molases_app.context.BranchState;
import com.gierza_molases.molases_app.context.CustomerState;
import com.gierza_molases.molases_app.context.NewDeliveryState;
import com.gierza_molases.molases_app.context.ProductState;
import com.gierza_molases.molases_app.model.Branch;
import com.gierza_molases.molases_app.model.BranchDelivery;
import com.gierza_molases.molases_app.model.Customer;
import com.gierza_molases.molases_app.model.CustomerDelivery;
import com.gierza_molases.molases_app.model.Delivery;
import com.gierza_molases.molases_app.model.ProductWithQuantity;
import com.gierza_molases.molases_app.service.DeliveryService;

public class NewDeliveryController {
	// Inject the existing controllers
	private final NewDeliveryState state;
	private final CustomersController customersController;
	private final BranchesController branchesController;
	private final ProductsController productsController;

	// Delivery Service
	private final DeliveryService deliveryService;

	public NewDeliveryController(NewDeliveryState state, CustomersController customersController,
			BranchesController branchesController, ProductsController productsController,
			DeliveryService deliveryService) {
		this.state = state;
		this.customersController = customersController;
		this.branchesController = branchesController;
		this.productsController = productsController;

		this.deliveryService = deliveryService;
	}

	public NewDeliveryState getState() {
		return state;
	}

	/*
	 * ====================== Step 1 Operations ======================
	 */

	// (Already handled in DeliveryFormStep1)

	/*
	 * ====================== Step 2 - Customer Selection Operations
	 * ======================
	 */

	/**
	 * Search for customers in the delivery form (Customer selection)
	 */
	public void searchCustomers(String searchText, Runnable onSuccess, Consumer<String> onError) {
		customersController.search20(searchText, onSuccess, onError);
	}

	/**
	 * Load top 20 customers for initial selection
	 */
	public void load20CustomerData(Runnable onSuccess, Consumer<String> onError) {
		this.customersController.loadTop20Customers(onSuccess, onError);
	}

	/**
	 * Get current customer results
	 */
	public CustomerState getCustomerState() {
		return customersController.getState();
	}

	/*
	 * ====================== Step 2 - Branch Selection Operations
	 * ======================
	 */

	/**
	 * Load branches for a specific customer
	 */
	public void loadCustomerBranches(int customerId, Runnable onSuccess, Consumer<String> onError) {
		this.branchesController.loadBranchByCustomerId(customerId, onSuccess, onError);
	}

	/**
	 * Get current branches results
	 */
	public BranchState getBranchesState() {
		return branchesController.getState();
	}

	/*
	 * ====================== Step 2 - Product Selection Operations
	 * ======================
	 */

	/**
	 * Load products for selection (all associated products for the customer)
	 */
	public void loadProductSelection(Runnable onSuccess, Consumer<String> onError, int customerId) {
		this.productsController.loadProductsSelection(onSuccess, onError, customerId);
	}

	/**
	 * Search products for a specific customer
	 */
	public void searchProducts(String search, int customerId, Runnable onSuccess, Consumer<String> onError) {
		this.productsController.searchSelection(search, customerId, onSuccess, onError);
	}

	/**
	 * Get current product results
	 */
	public ProductState getProductState() {
		return productsController.getState();
	}

	/*
	 * ====================== Step 2 - Delivery Data Management
	 * ======================
	 */

	/**
	 * Add a customer with their branches and products to the delivery This is
	 * called when AddCustomerBranchDialog saves
	 */
	public void addCustomerDelivery(Customer customer, Map<Branch, List<ProductWithQuantity>> branchProducts) {
		state.newCustomerDelivery(customer, branchProducts);
	}

	/**
	 * Remove a customer from the delivery
	 */
	public void removeCustomerDelivery(Customer customer) {
		state.deleteCustomerDelivery(customer);
	}

	/**
	 * Check if a customer is already in the delivery
	 */
	public boolean hasCustomer(Customer customer) {
		return state.hasCustomer(customer);
	}

	/**
	 * Get all customer deliveries (for displaying in Step 2 table)
	 */
	public Map<Customer, Map<Branch, List<ProductWithQuantity>>> getCustomerDeliveries() {
		return state.getCustomerDeliveries();
	}

	/**
	 * Get branches and products for a specific customer
	 */
	public Map<Branch, List<ProductWithQuantity>> getCustomerBranches(Customer customer) {
		return state.getCustomerBranches(customer);
	}

	/*
	 * ====================== Save Delivery ======================
	 */
	public void saveDelivery(Runnable onSuccess, Consumer<String> onError) {

		// Validate state is complete
		if (!state.isComplete()) {
			onError.accept("Delivery data is incomplete. Please complete all steps.");
			return;
		}

		new SwingWorker<Void, Void>() {
			private Exception error;

			@Override
			protected Void doInBackground() {
				try {
					Delivery delivery = buildDeliveryFromState();
					Map<CustomerDelivery, List<BranchDelivery>> customerDeliveryMap = buildCustomerDeliveryMap();

					deliveryService.addNewDelivery(delivery, customerDeliveryMap);
				} catch (Exception e) {
					error = e;
				}
				return null;
			}

			@Override
			protected void done() {
				if (error == null) {
					if (onSuccess != null) {
						onSuccess.run();
					}
					state.resetState();
				} else {
					error.printStackTrace();
					if (onError != null) {
						onError.accept(error.getMessage());
					}
				}
			}
		}.execute();
	}

	/**
	 * Build Delivery object from state (Step 1 data + calculated financials)
	 */
	private Delivery buildDeliveryFromState() {
		// Convert java.util.Date to LocalDateTime
		LocalDateTime scheduleDate = state.getDeliveryDate().toInstant().atZone(ZoneId.systemDefault())
				.toLocalDateTime();
		String deliveryName = state.getDeliveryName();
		Map<String, Double> expenses = state.getDeliveryExpenses();

		// Get financial calculations from state
		double totalGross = state.calculateGrossSales();
		double totalCapital = state.calculateTotalCapital();
		double grossProfit = state.calculateGrossProfit();
		double netProfit = state.calculateNetProfit();

		// Get customer and branch counts from state
		int totalCustomers = state.getTotalCustomers();
		int totalBranches = state.getTotalBranches();

		Delivery delivery = new Delivery(scheduleDate, deliveryName, expenses, totalCustomers, totalBranches,
				totalGross, totalCapital, grossProfit, netProfit);

		return delivery;
	}

	/**
	 * Transform state's customer deliveries into the DB structure. Returns:
	 * Map<CustomerDelivery, List<BranchDelivery>>
	 */
	private Map<CustomerDelivery, List<BranchDelivery>> buildCustomerDeliveryMap() {
		Map<CustomerDelivery, List<BranchDelivery>> result = new HashMap<>();

		// Get customer deliveries from state
		Map<Customer, Map<Branch, List<ProductWithQuantity>>> customerDeliveries = state.getCustomerDeliveries();

		// Loop through each customer
		for (Map.Entry<Customer, Map<Branch, List<ProductWithQuantity>>> customerEntry : customerDeliveries
				.entrySet()) {

			Customer customer = customerEntry.getKey();
			Map<Branch, List<ProductWithQuantity>> branches = customerEntry.getValue();

			// Create CustomerDelivery object (without deliveryId for now, will be set by
			// service)
			// Note: deliveryId will be filled after Delivery is saved and we get the ID
			CustomerDelivery customerDelivery = new CustomerDelivery(customer.getId(), 0 // deliveryId - will be set by
																							// service after Delivery is
																							// inserted
			);

			// List to hold all BranchDelivery records for this customer
			List<BranchDelivery> branchDeliveries = new ArrayList<>();

			// Loop through each branch for this customer
			for (Map.Entry<Branch, List<ProductWithQuantity>> branchEntry : branches.entrySet()) {
				Branch branch = branchEntry.getKey();
				List<ProductWithQuantity> products = branchEntry.getValue();

				// For each product in this branch, create a BranchDelivery record
				for (ProductWithQuantity productWithQty : products) {
					BranchDelivery branchDelivery = new BranchDelivery(0, // customerDeliveryId - will be set by service
																			// after CustomerDelivery is inserted
							branch.getId(), productWithQty.getProduct().getId(), productWithQty.getQuantity(),
							"scheduled" // status - default for new delivery
					);

					branchDeliveries.add(branchDelivery);
				}
			}

			// Add to result map
			result.put(customerDelivery, branchDeliveries);
		}

		return result;
	}

	/*
	 * ====================== State Management ======================
	 */

	/**
	 * Reset all filters and state when form is closed/reset
	 */
	public void resetFormFilters() {
		customersController.resetState();
		branchesController.resetState();
		productsController.resetState();
		state.resetState();
	}

	/**
	 * Check if delivery is complete and ready to save
	 */
	public boolean isDeliveryComplete() {
		return state.isComplete();
	}
}