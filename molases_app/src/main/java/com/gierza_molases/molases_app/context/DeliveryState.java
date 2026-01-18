package com.gierza_molases.molases_app.context;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Stack;

import com.gierza_molases.molases_app.model.Delivery;

public class DeliveryState {
	private String search = "";
	private Date startAt = null;
	private Date endAt = null;
	private Long lastSeenDeliveryId = null;
	private boolean hasNextPage = true;
	private int pageSize = 15;
	private List<Delivery> deliveries = new ArrayList<>();
	private Stack<PageState> pageHistory = new Stack<>();

	/**
	 * Inner class to store page state for backward navigation
	 */
	public static class PageState {
		private Long lastSeenDeliveryId;
		private List<Delivery> deliveries;
		private String searchFilter;
		private Date startAt;
		private Date endAt;

		public PageState(Long lastSeenDeliveryId, List<Delivery> deliveries, String searchFilter, Date startAt,
				Date endAt) {
			this.lastSeenDeliveryId = lastSeenDeliveryId;
			this.deliveries = new ArrayList<>(deliveries); // Create a copy
			this.searchFilter = searchFilter;
			this.startAt = startAt;
			this.endAt = endAt;
		}

		public Long getLastSeenDeliveryId() {
			return lastSeenDeliveryId;
		}

		public List<Delivery> getDeliveries() {
			return deliveries;
		}

		public String getSearchFilter() {
			return searchFilter;
		}

		public Date getStartAt() {
			return startAt;
		}

		public Date getEndAt() {
			return endAt;
		}
	}

	// Getters
	public String getSearch() {
		return search;
	}

	public Date getStartAt() {
		return startAt;
	}

	public Date getEndAt() {
		return endAt;
	}

	public Long getLastSeenDeliveryId() {
		return lastSeenDeliveryId;
	}

	public boolean hasNextPage() {
		return hasNextPage;
	}

	public int getPageSize() {
		return pageSize;
	}

	public List<Delivery> getDeliveries() {
		return deliveries;
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

	public void setStartAt(Date startAt) {
		this.startAt = startAt;
	}

	public void setEndAt(Date endAt) {
		this.endAt = endAt;
	}

	public void setLastSeenDeliveryId(Long lastSeenDeliveryId) {
		this.lastSeenDeliveryId = lastSeenDeliveryId;
	}

	public void setHasNextPage(boolean hasNextPage) {
		this.hasNextPage = hasNextPage;
	}

	public void setDeliveries(List<Delivery> deliveries) {
		this.deliveries = deliveries;
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
		startAt = null;
		endAt = null;
		lastSeenDeliveryId = null;
		hasNextPage = true;
		deliveries.clear();
		pageHistory.clear();
	}

	public void resetPagination() {
		lastSeenDeliveryId = null;
		hasNextPage = true;
		pageHistory.clear();
	}
}