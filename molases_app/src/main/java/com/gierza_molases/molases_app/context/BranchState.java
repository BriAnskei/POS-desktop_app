package com.gierza_molases.molases_app.context;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import com.gierza_molases.molases_app.model.Branch;

public class BranchState {
	private String search = "";
	private Long lastSeenBranchId = null;
	private boolean hasNextPage = true;
	private int pageSize = 15;

	private List<Branch> branches = new ArrayList<>();
	private Stack<PageState> pageHistory = new Stack<>();

	/**
	 * Inner class to store page state for backward navigation
	 */
	public static class PageState {
		private Long lastSeenBranchId;
		private List<Branch> branches;
		private String searchFilter;

		public PageState(Long lastSeenBranchId, List<Branch> branches, String searchFilter) {
			this.lastSeenBranchId = lastSeenBranchId;
			this.branches = new ArrayList<>(branches); // Create a copy
			this.searchFilter = searchFilter;
		}

		public Long getLastSeenBranchId() {
			return lastSeenBranchId;
		}

		public List<Branch> getBranches() {
			return branches;
		}

		public String getSearchFilter() {
			return searchFilter;
		}
	}

	// Getters
	public String getSearch() {
		return search;
	}

	public Long getLastSeenBranchId() {
		return lastSeenBranchId;
	}

	public boolean hasNextPage() {
		return hasNextPage;
	}

	public int getPageSize() {
		return pageSize;
	}

	public List<Branch> getBranches() {
		return branches;
	}

	public Stack<PageState> getPageHistory() {
		return pageHistory;
	}

	public boolean hasPreviousPage() {
		return !pageHistory.isEmpty();
	}

	// Setters
	public void setSearch(String search) {
		this.search = search;
	}

	public void setLastSeenBranchId(Long lastSeenBranchId) {
		this.lastSeenBranchId = lastSeenBranchId;
	}

	public void setHasNextPage(boolean hasNextPage) {
		this.hasNextPage = hasNextPage;
	}

	public void setBranches(List<Branch> branches) {
		this.branches = branches;
	}

	// Helper methods
	public void pushPageToHistory(PageState pageState) {
		pageHistory.push(pageState);
	}

	public PageState popPageFromHistory() {
		return pageHistory.isEmpty() ? null : pageHistory.pop();
	}

	public void clearHistory() {
		pageHistory.clear();
	}

	public void reset() {
		search = "";
		lastSeenBranchId = null;
		hasNextPage = true;
		branches.clear();
		pageHistory.clear();
	}

	public void resetPagination() {
		lastSeenBranchId = null;
		hasNextPage = true;
		pageHistory.clear();
	}
}