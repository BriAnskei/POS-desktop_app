package com.gierza_molases.molases_app.UiController;

import java.util.List;
import java.util.function.Consumer;

import javax.swing.SwingWorker;

import com.gierza_molases.molases_app.Context.ProductState;
import com.gierza_molases.molases_app.model.Product;
import com.gierza_molases.molases_app.service.ProductAssociationService;
import com.gierza_molases.molases_app.service.ProductService;

public class ProductsController {

	private final ProductState state;
	private final ProductService service;
	private final ProductAssociationService productAssociationService;

	public ProductsController(ProductState state, ProductService service,
			ProductAssociationService productAssociationService) {
		this.state = state;
		this.service = service;

		this.productAssociationService = productAssociationService;
	}

	public ProductState getState() {
		return state;
	}

	/*
	 * ========================= Load Products =========================
	 */

	public void loadProducts(Runnable onDone, Consumer<String> onError) {
		state.setLoading(true);

		new SwingWorker<List<Product>, Void>() {
			private Exception error;

			@Override
			protected List<Product> doInBackground() {
				try {
					String search = state.getSearch().isBlank() ? null : state.getSearch();
					String sortOrder = state.getSortOrder();

					// Example service call
					return service.getAllProducts(search, sortOrder);

				} catch (Exception e) {
					error = e;
					return null;
				}
			}

			@Override
			protected void done() {
				state.setLoading(false);

				if (error != null) {
					error.printStackTrace();

					if (onError != null)
						onError.accept(error.getMessage());
					return;
				}

				try {
					state.setProducts(get());
					if (onDone != null)
						onDone.run();
				} catch (Exception e) {
					e.printStackTrace();

					if (onError != null)
						onError.accept(e.getMessage());
				}
			}
		}.execute();
	}

	/*
	 * ========================= CRUD Operations =========================
	 */

	public void addProduct(Product product, List<Integer> associatedCustomerIds, Runnable onSuccess,
			Consumer<String> onError) {
		new SwingWorker<Void, Void>() {
			private Exception error;

			@Override
			protected Void doInBackground() {
				try {
					service.addProduct(product, associatedCustomerIds);
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
					loadProducts(onSuccess, onError);
				}
			}
		}.execute();
	}

	public void updateProduct(Product product, Runnable onSuccess, Consumer<String> onError) {
		new SwingWorker<Void, Void>() {
			private Exception error;

			@Override
			protected Void doInBackground() {
				try {
					service.updateProduct(product);
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
					loadProducts(onSuccess, onError);
				}
			}
		}.execute();
	}

	public void deleteProduct(int productId, Runnable onSuccess, Consumer<String> onError) {
		new SwingWorker<Void, Void>() {
			private Exception error;

			@Override
			protected Void doInBackground() {
				try {
					service.deleteProduct(productId);
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
					loadProducts(onSuccess, onError);
				}
			}
		}.execute();
	}

	public void addCustomerAssociations(int productId, List<Integer> customerIds, Runnable onSuccess,
			Consumer<String> onError) {
		new SwingWorker<Void, Void>() {
			private Exception error;

			@Override
			protected Void doInBackground() {
				try {
					productAssociationService.insertAll(productId, customerIds);
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
					// Reload products to reflect updated association counts
					loadProducts(onSuccess, onError);
				}
			}
		}.execute();
	}

	/**
	 * Remove a customer association from a product
	 */
	public void removeCustomerAssociation(int productId, int customerId, Runnable onSuccess, Consumer<String> onError) {
		new SwingWorker<Void, Void>() {
			private Exception error;

			@Override
			protected Void doInBackground() {
				try {
					productAssociationService.removeAssociation(productId, customerId);
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
					// Reload products to reflect updated association counts
					loadProducts(onSuccess, onError);
				}
			}
		}.execute();
	}

	/*
	 * ========================= Filters / Sorting =========================
	 */

	public void search(String searchText, Runnable onDone, Consumer<String> onError) {
		state.setSearch(searchText);
		loadProducts(onDone, onError);
	}

	public void changeSortOrder(String sortOrder, Runnable onDone, Consumer<String> onError) {
		state.setSortOrder(sortOrder);
		loadProducts(onDone, onError);
	}

	public void clearFilters(Runnable onDone, Consumer<String> onError) {
		state.resetFilters();
		loadProducts(onDone, onError);
	}

	public void refresh(Runnable onDone, Consumer<String> onError) {
		loadProducts(onDone, onError);
	}

	public void resetState() {
		state.reset();
	}
}
