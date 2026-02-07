package com.gierza_molases.molases_app.UiController;

import java.util.List;
import java.util.function.Consumer;

import javax.swing.SwingWorker;

import com.gierza_molases.molases_app.context.CustomerPaymentState;
import com.gierza_molases.molases_app.context.CustomerPaymentState.PageState;
import com.gierza_molases.molases_app.model.CustomerPayments;
import com.gierza_molases.molases_app.service.CustomerPaymentsService;

public class CustomerPaymentController {
	private final CustomerPaymentState state;
	private final CustomerPaymentsService service;

	public CustomerPaymentController(CustomerPaymentState customerPaymentState,
			CustomerPaymentsService customerPaymentsService) {
		this.service = customerPaymentsService;
		this.state = customerPaymentState;
	}

	// Getter for state
	public CustomerPaymentState getState() {
		return state;
	}

	/**
	 * Load payments from database
	 * 
	 * @param isNextPage true if navigating to next page, false for first page or
	 *                   refresh
	 */
	public void loadPayments(boolean isNextPage, Runnable onDone, Runnable onError) {
		new SwingWorker<List<CustomerPayments>, Void>() {
			private List<CustomerPayments> loaded;
			private Exception error;

			@Override
			protected List<CustomerPayments> doInBackground() {
				try {
					String searchFilter = state.getSearch().isEmpty() ? null : state.getSearch();
					String paymentTypeFilter = "All".equals(state.getPaymentType()) ? null : state.getPaymentType();
					String statusFilter = "All".equals(state.getStatus()) ? null : state.getStatus();

					loaded = service.fetchPayamentsCursor(state.getLastSeenPaymentId(), searchFilter, paymentTypeFilter,
							statusFilter, state.getFromDate(), state.getToDate(), state.getPageSize());

				} catch (Exception e) {
					error = e;
				}
				return loaded;
			}

			@Override
			protected void done() {
				if (error != null) {
					error.printStackTrace();
					if (onError != null)
						onError.run();
				} else {
					// If this is a next page navigation, save current page to history
					if (isNextPage && !state.getPayments().isEmpty()) {
						List<CustomerPayments> currentPayments = state.getPayments();
						Long historyLastSeenId = currentPayments.isEmpty() ? null
								: (long) currentPayments.get(0).getId() - 1;

						state.pushPageToHistory(new PageState(historyLastSeenId, currentPayments, state.getSearch(),
								state.getPaymentType(), state.getStatus(), state.getFromDate(), state.getToDate()));
					}

					boolean hasNext = loaded.size() > state.getPageSize();

					if (hasNext) {
						// remove the extra item
						loaded.remove(loaded.size() - 1);
					}

					state.setHasNextPage(hasNext);
					state.setPayments(loaded);

					// Update last seen ID for next page
					if (!loaded.isEmpty()) {
						state.setLastSeenPaymentId((long) loaded.get(loaded.size() - 1).getId());
					}

					if (onDone != null)
						onDone.run();
				}
			}
		}.execute();
	}

	/**
	 * Navigate to previous page using cached data
	 */
	public void goToPreviousPage(Runnable onDone) {
		PageState previousState = state.popPageFromHistory();

		if (previousState == null) {
			return;
		}

		// Restore the previous page data
		state.setPayments(previousState.getPayments());
		state.setSearch(previousState.getSearchFilter());
		state.setPaymentType(previousState.getPaymentTypeFilter());
		state.setStatus(previousState.getStatusFilter());
		state.setFromDate(previousState.getFromDate());
		state.setToDate(previousState.getToDate());
		state.setLastSeenPaymentId(previousState.getLastSeenPaymentId());

		// Since we're going back, there's definitely a next page
		state.setHasNextPage(true);

		if (onDone != null)
			onDone.run();
	}

	/**
	 * Delete payment by ID
	 */
	public void deletePayment(int paymentId, Runnable onSuccess, Consumer<String> onError) {
		new SwingWorker<Void, Void>() {
			private Exception error;

			@Override
			protected Void doInBackground() {
				try {
//					service.deletePayment(paymentId);
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
					if (onSuccess != null)
						onSuccess.run();
				}
			}
		}.execute();
	}

	/**
	 * Perform search and reload from first page
	 */
	public void search(String searchText, String paymentType, String status, java.util.Date fromDate,
			java.util.Date toDate, Runnable onDone, Runnable onError) {
		state.setSearch(searchText);
		state.setPaymentType(paymentType);
		state.setStatus(status);
		state.setFromDate(fromDate);
		state.setToDate(toDate);
		state.resetPagination();
		loadPayments(false, onDone, onError);
	}

	/**
	 * Clear search and reload
	 */
	public void clearSearch(Runnable onDone, Runnable onError) {
		state.setSearch("");
		state.setPaymentType("All");
		state.setStatus("All");
		state.setFromDate(null);
		state.setToDate(null);
		state.resetPagination();
		loadPayments(false, onDone, onError);
	}

	/**
	 * Refresh current page
	 */
	public void refreshCurrentPage(Runnable onDone, Runnable onError) {
		state.resetPagination();
		loadPayments(false, onDone, onError);
	}

	/**
	 * Reset state (useful when navigating away from page)
	 */
	public void resetState() {
		state.reset();
	}
}