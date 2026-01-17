package com.gierza_molases.molases_app.UiController;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.SwingWorker;

import com.gierza_molases.molases_app.context.CustomerState;
import com.gierza_molases.molases_app.model.Branch;
import com.gierza_molases.molases_app.model.Customer;
import com.gierza_molases.molases_app.service.CustomerService;

public class CustomersController {
	private final CustomerState state;
	private final CustomerService service;

	public CustomersController(CustomerState state, CustomerService service) {
		this.state = state;
		this.service = service;
	}

	// Getter for state (so UI can read current values)
	public CustomerState getState() {
		return state;
	}

	/**
	 * Load customers from database
	 */
	public void loadCustomers(Runnable onDone, Runnable onError) {
		new SwingWorker<Void, Void>() {
			private List<Customer> loaded;
			private int total;
			private Exception error;

			@Override
			protected Void doInBackground() {
				try {
					total = service.getTotalCustomerCount(state.getSearch());
					loaded = service.fetchAll(state.getCurrentPage(), state.getItemsPerPage(), state.getSearch(),
							state.getSortOrder());
				} catch (Exception e) {
					error = e;
				}
				return null;
			}

			@Override
			protected void done() {
				if (error != null) {
					error.printStackTrace();
					if (onError != null)
						onError.run();
				} else {
					state.setCustomers(loaded);
					state.setTotalCustomers(total);
					if (onDone != null)
						onDone.run();
				}
			}
		}.execute();
	}

	public void loadTop20Customers(Runnable onDone, Consumer<String> onError) {

		new SwingWorker<Void, Void>() {
			private List<Customer> loaded;
			private Exception error;

			@Override
			protected Void doInBackground() {
				try {
					loaded = service.fetch20Customer(state.getSearch());

				} catch (Exception e) {
					error = e;
				}
				return null;
			}

			@Override
			protected void done() {
				if (error != null) {
					error.printStackTrace();
					if (onError != null)
						onError.accept(error.getMessage());

				} else {
					state.setCustomers(loaded);

					if (onDone != null)
						onDone.run();
				}
			}

		}.execute();
	}

	/**
	 * Delete customer by ID
	 */
	public void deleteCustomer(int customerId, Runnable onSuccess, Consumer<Exception> onError) {
		new SwingWorker<Void, Void>() {
			private Exception error;

			@Override
			protected Void doInBackground() {
				try {
					service.delete(customerId);
				} catch (Exception e) {
					error = e;
				}
				return null;
			}

			@Override
			protected void done() {
				if (error != null) {
					error.printStackTrace();
					if (onError != null)
						onError.accept(error);
				} else {
					if (onSuccess != null)
						onSuccess.run();
				}
			}
		}.execute();
	}

	/**
	 * Add new customer (Individual type)
	 */
	public void addCustomerIndividual(String firstName, String middleName, String lastName, String contactNumber,
			String address, List<Branch> branches, Runnable onSuccess, Consumer<Exception> onError) {
		new SwingWorker<Void, Void>() {
			private Exception error;

			@Override
			protected Void doInBackground() {
				try {
					service.addCustomerAsIndividualType(firstName, middleName, lastName, contactNumber, address,
							branches);
				} catch (Exception e) {
					error = e;
				}
				return null;
			}

			@Override
			protected void done() {
				if (error != null) {
					error.printStackTrace();
					if (onError != null)
						onError.accept(error);
				} else {
					if (onSuccess != null)
						onSuccess.run();
				}
			}
		}.execute();
	}

	/**
	 * Add new customer (Company type)
	 */
	public void addCustomerCompany(String companyName, String contactNumber, String address, List<Branch> branches,
			Runnable onSuccess, Consumer<Exception> onError) {
		new SwingWorker<Void, Void>() {
			private Exception error;

			@Override
			protected Void doInBackground() {
				try {
					service.addCustomerAsCompanyType(companyName, contactNumber, address, branches);
				} catch (Exception e) {
					error = e;
				}
				return null;
			}

			@Override
			protected void done() {
				if (error != null) {
					error.printStackTrace();
					if (onError != null)
						onError.accept(error);
				} else {
					if (onSuccess != null)
						onSuccess.run();
				}
			}
		}.execute();
	}

	/**
	 * Update customer (Individual type)
	 */
	public void updateCustomerIndividual(int customerId, String firstName, String middleName, String lastName,
			String contactNumber, String address, Runnable onSuccess, Consumer<Exception> onError) {
		new SwingWorker<Void, Void>() {
			private Exception error;

			@Override
			protected Void doInBackground() {
				try {
					service.updateIndividual(customerId, firstName, middleName, lastName, contactNumber, address);
				} catch (Exception e) {
					error = e;
				}
				return null;
			}

			@Override
			protected void done() {
				if (error != null) {
					error.printStackTrace();
					if (onError != null)
						onError.accept(error);
				} else {
					if (onSuccess != null)
						onSuccess.run();
				}
			}
		}.execute();
	}

	/**
	 * Update customer (Company type)
	 */
	public void updateCustomerCompany(int customerId, String companyName, String contactNumber, String address,
			Runnable onSuccess, Consumer<Exception> onError) {
		new SwingWorker<Void, Void>() {
			private Exception error;

			@Override
			protected Void doInBackground() {
				try {
					service.updateCompany(customerId, companyName, contactNumber, address);
				} catch (Exception e) {
					error = e;
				}
				return null;
			}

			@Override
			protected void done() {
				if (error != null) {
					error.printStackTrace();
					if (onError != null)
						onError.accept(error);
				} else {
					if (onSuccess != null)
						onSuccess.run();
				}
			}
		}.execute();
	}

	/**
	 * Update pagination and reload
	 */
	public void goToPage(int page, Runnable onDone, Runnable onError) {
		state.setCurrentPage(page);
		loadCustomers(onDone, onError);
	}

	/**
	 * Update search and reload load 20 customer
	 */
	public void search20(String searchText, Runnable onDone, Consumer<String> onError) {
		state.setSearch(searchText);
		this.loadTop20Customers(onDone, onError);
	}

	/**
	 * Update search and reload from page 1
	 */
	public void search(String searchText, Runnable onDone, Runnable onError) {
		state.setSearch(searchText);
		state.setCurrentPage(1);
		loadCustomers(onDone, onError);
	}

	/**
	 * Update sort order and reload from page 1
	 */
	public void updateSortOrder(String sortOrder, Runnable onDone, Runnable onError) {
		state.setSortOrder(sortOrder);
		state.setCurrentPage(1);
		loadCustomers(onDone, onError);
	}

	/**
	 * Clear all filters and reload
	 */
	public void clearFilters(Runnable onDone, Runnable onError) {
		state.setSearch("");
		state.setSortOrder("DESC");
		state.setCurrentPage(1);
		loadCustomers(onDone, onError);
	}

	/**
	 * Reset state (useful when navigating away from page)
	 */
	public void resetState() {
		state.setCurrentPage(1);
		state.setSearch("");
		state.setSortOrder("DESC");
		state.setCustomers(new ArrayList<>());
		state.setTotalCustomers(0);
	}
}