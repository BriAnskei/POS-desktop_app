
package com.gierza_molases.molases_app.UiController;

import java.util.List;
import java.util.function.Consumer;

import javax.swing.SwingWorker;

import com.gierza_molases.molases_app.context.BranchState;
import com.gierza_molases.molases_app.context.BranchState.PageState;
import com.gierza_molases.molases_app.model.Branch;
import com.gierza_molases.molases_app.service.BranchService;

public class BranchesController {
	private final BranchState state;
	private final BranchService service;

	public BranchesController(BranchState state, BranchService service) {
		this.state = state;
		this.service = service;
	}

	// Getter for state
	public BranchState getState() {
		return state;
	}

	/**
	 * Load branches from database
	 * 
	 * @param isNextPage true if navigating to next page, false for first page or
	 *                   refresh
	 */
	public void loadBranches(boolean isNextPage, Runnable onDone, Runnable onError) {
		new SwingWorker<List<Branch>, Void>() {
			private List<Branch> loaded;

			private Exception error;

			@Override
			protected List<Branch> doInBackground() {
				try {
					String filter = state.getSearch().isEmpty() ? null : state.getSearch();
					loaded = service.fetchBranchCursor(state.getLastSeenBranchId(), filter, state.getPageSize());

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
					if (isNextPage && !state.getBranches().isEmpty()) {
						List<Branch> currentBranches = state.getBranches();
						Long historyLastSeenId = currentBranches.isEmpty() ? null
								: (long) currentBranches.get(0).getId() - 1;

						state.pushPageToHistory(new PageState(historyLastSeenId, currentBranches, state.getSearch()));
					}

					boolean hasNext = loaded.size() > state.getPageSize();

					if (hasNext) {
						// remove the extra item
						loaded.remove(loaded.size() - 1);
					}

					state.setHasNextPage(hasNext);
					state.setBranches(loaded);

					// Update last seen ID for next page
					if (!loaded.isEmpty()) {
						state.setLastSeenBranchId((long) loaded.get(loaded.size() - 1).getId());
					}

					if (onDone != null)
						onDone.run();
				}
			}
		}.execute();
	}

	/**
	 * Load branch by customer id
	 * 
	 */

	public void loadBranchByCustomerId(int customerId, Runnable onSuccess, Consumer<String> onError) {
		new SwingWorker<List<Branch>, Void>() {
			private List<Branch> customerBranches;
			private Exception error;

			@Override
			protected List<Branch> doInBackground() {
				try {
					customerBranches = service.getBranchesByCustomerId(customerId);

				} catch (Exception e) {
					error = e;
				}

				return customerBranches;

			}

			@Override
			protected void done() {

				if (error != null) {
					error.printStackTrace();
					if (onError != null)
						onError.accept(error.getMessage());
				} else {
					state.reset();
					state.setBranches(customerBranches);
					if (onSuccess != null) {
						onSuccess.run();
					}

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
		state.setBranches(previousState.getBranches());
		state.setSearch(previousState.getSearchFilter());
		state.setLastSeenBranchId(previousState.getLastSeenBranchId());

		// Since we're going back, there's definitely a next page
		state.setHasNextPage(true);

		if (onDone != null)
			onDone.run();
	}

	/**
	 * Delete branch by ID
	 */
	public void deleteBranch(int branchId, Runnable onSuccess, Consumer<String> onError) {
		new SwingWorker<Void, Void>() {
			private Exception error;

			@Override
			protected Void doInBackground() {
				try {
					service.delete(branchId);
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
	 * Add new branch
	 */
	public void addBranch(int customerId, Branch branch, Runnable onSuccess, Runnable onError) {
		new SwingWorker<Void, Void>() {
			private Exception error;

			@Override
			protected Void doInBackground() {
				try {
					service.addBranche(customerId, branch);
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
	 * Update branch
	 */
	public void updateBranch(int branchId, Branch branch, Runnable onSuccess, Runnable onError) {
		new SwingWorker<Void, Void>() {
			private Exception error;

			@Override
			protected Void doInBackground() {
				try {
					service.update(branchId, branch);
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
		loadBranches(false, onDone, onError);
	}

	/**
	 * Clear search and reload
	 */
	public void clearSearch(Runnable onDone, Runnable onError) {
		state.setSearch("");
		state.resetPagination();
		loadBranches(false, onDone, onError);
	}

	/**
	 * Refresh current page
	 */
	public void refreshCurrentPage(Runnable onDone, Runnable onError) {
		state.resetPagination();
		loadBranches(false, onDone, onError);
	}

	/**
	 * Reset state (useful when navigating away from page)
	 */
	public void resetState() {
		state.reset();
	}
}