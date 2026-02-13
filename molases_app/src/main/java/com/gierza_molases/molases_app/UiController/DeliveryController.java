package com.gierza_molases.molases_app.UiController;

import java.util.Date;
import java.util.List;

import javax.swing.SwingWorker;

import com.gierza_molases.molases_app.context.DeliveryState;
import com.gierza_molases.molases_app.context.DeliveryState.PageState;
import com.gierza_molases.molases_app.model.Delivery;
import com.gierza_molases.molases_app.service.DeliveryService;

public class DeliveryController {
	private final DeliveryState state;
	private final DeliveryService service;

	public DeliveryController(DeliveryState state, DeliveryService service) {
		this.state = state;
		this.service = service;
	}

	// Getter for state
	public DeliveryState getState() {
		return state;
	}

	/**
	 * Load deliveries from database
	 * 
	 * @param isNextPage true if navigating to next page, false for first page or
	 *                   refresh
	 */
	public void loadDeliveries(boolean isNextPage, Runnable onDone, Runnable onError) {
		new SwingWorker<List<Delivery>, Void>() {
			private List<Delivery> loaded;
			private Exception error;

			@Override
			protected List<Delivery> doInBackground() {
				try {

					String filter = state.getSearch().isEmpty() ? null : state.getSearch();
					loaded = service.fetchNextPage(state.getLastSeenDeliveryId(), filter, state.getStartAt(),
							state.getEndAt(), state.getPageSize());
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
						System.err.print(error);
					onError.run();
				} else {
					// If this is a next page navigation, save current page to history
					if (isNextPage && !state.getDeliveries().isEmpty()) {
						List<Delivery> currentDeliveries = state.getDeliveries();
						Long historyLastSeenId = currentDeliveries.isEmpty() ? null
								: (long) currentDeliveries.get(0).getId() - 1;

						state.pushPageToHistory(new PageState(historyLastSeenId, currentDeliveries, state.getSearch(),
								state.getStartAt(), state.getEndAt()));
					}

					boolean hasMore = loaded.size() > state.getPageSize();

					if (hasMore) {
						loaded.remove(loaded.size() - 1);
					}

					// Update state with loaded data
					state.setDeliveries(loaded);
					state.setHasNextPage(hasMore);

					// Update last seen ID for next page
					if (!loaded.isEmpty()) {
						state.setLastSeenDeliveryId((long) loaded.get(loaded.size() - 1).getId());
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
		state.setDeliveries(previousState.getDeliveries());
		state.setSearch(previousState.getSearchFilter());
		state.setStartAt(previousState.getStartAt());
		state.setEndAt(previousState.getEndAt());
		state.setLastSeenDeliveryId(previousState.getLastSeenDeliveryId());

		// Since we're going back, there's definitely a next page
		state.setHasNextPage(true);

		if (onDone != null)
			onDone.run();
	}

	/**
	 * Delete delivery by ID
	 */
	public void deleteDelivery(int deliveryId, Runnable onSuccess, Runnable onError) {
		new SwingWorker<Void, Void>() {
			private Exception error;

			@Override
			protected Void doInBackground() {
				try {
					service.deleteDelivery(deliveryId);
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
					if (onSuccess != null)
						onSuccess.run();
				}
			}
		}.execute();
	}

	/**
	 * Perform search and reload from first page
	 */
	public void search(String searchText, Runnable onDone, Runnable onError) {
		state.setSearch(searchText);
		state.resetPagination();
		loadDeliveries(false, onDone, onError);
	}

	/**
	 * Set date filter and reload from first page
	 */
	public void filterByDate(Date startAt, Date endAt, Runnable onDone, Runnable onError) {
		state.setStartAt(startAt);
		state.setEndAt(endAt);
		state.resetPagination();
		loadDeliveries(false, onDone, onError);
	}

	/**
	 * Clear all filters and reload
	 */
	public void clearFilters(Runnable onDone, Runnable onError) {
		state.setSearch("");
		state.setStartAt(null);
		state.setEndAt(null);
		state.resetPagination();
		loadDeliveries(false, onDone, onError);
	}

	/**
	 * Refresh current page
	 */
	public void refreshCurrentPage(Runnable onDone, Runnable onError) {
		state.resetPagination();
		loadDeliveries(false, onDone, onError);
	}

	/**
	 * Reset state (useful when navigating away from page)
	 */
	public void resetState() {
		state.reset();
	}
}