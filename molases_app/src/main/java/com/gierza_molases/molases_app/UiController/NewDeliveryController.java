package com.gierza_molases.molases_app.UiController;

import java.util.function.Consumer;

import com.gierza_molases.molases_app.context.CustomerState; // Assuming you have this
import com.gierza_molases.molases_app.context.NewDeliveryState;
import com.gierza_molases.molases_app.context.ProductState;

public class NewDeliveryController {

	// Inject the existing controllers
	private final NewDeliveryState state;
	private final CustomersController customersController;
	private final ProductsController productsController;

	public NewDeliveryController(NewDeliveryState state, CustomersController customersController,
			ProductsController productsController) {
		this.state = state;
		this.customersController = customersController;
		this.productsController = productsController;
	}

	public NewDeliveryState getState() {
		return state;
	}

	/**
	 * Search for customers in the delivery form (Customer selection)
	 */
	public void searchCustomers(String searchText, Runnable onSuccess, Consumer<String> onError) {
		customersController.search(searchText, onSuccess, onSuccess);
	}

	public void load20CustomerData(Runnable onSuccess, Consumer<String> onError) {
		this.customersController.loadTop20Customers(onSuccess, onError);
	}

	/**
	 * Search for products in the delivery form
	 */
	public void searchProducts(String searchText, Runnable onSuccess, Consumer<String> onError) {
		productsController.search(searchText, onSuccess, onError);
	}

	/**
	 * Get current customer results
	 */
	public CustomerState getCustomerState() {
		return customersController.getState();
	}

	/**
	 * Get current product results
	 */
	public ProductState getProductState() {
		return productsController.getState();
	}

	/**
	 * Reset all filters when form is closed/reset
	 */
	public void resetFormFilters() {
		customersController.resetState();
		productsController.resetState();
	}
}