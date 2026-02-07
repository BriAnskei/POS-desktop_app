package com.gierza_molases.molases_app.context;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Stack;

import com.gierza_molases.molases_app.model.CustomerPayments;

public class CustomerPaymentState {
	private String search = "";
	private String paymentType = "All";
	private String status = "All";
	private Date fromDate = null;
	private Date toDate = null;
	private Long lastSeenPaymentId = null;
	private boolean hasNextPage = true;
	private int pageSize = 15;
	private List<CustomerPayments> payments = new ArrayList<>();
	private Stack<PageState> pageHistory = new Stack<>();

	/**
	 * Inner class to store page state for backward navigation
	 */
	public static class PageState {
		private Long lastSeenPaymentId;
		private List<CustomerPayments> payments;
		private String searchFilter;
		private String paymentTypeFilter;
		private String statusFilter;
		private Date fromDate;
		private Date toDate;

		public PageState(Long lastSeenPaymentId, List<CustomerPayments> payments, String searchFilter,
				String paymentTypeFilter, String statusFilter, Date fromDate, Date toDate) {
			this.lastSeenPaymentId = lastSeenPaymentId;
			this.payments = new ArrayList<>(payments); // Create a copy
			this.searchFilter = searchFilter;
			this.paymentTypeFilter = paymentTypeFilter;
			this.statusFilter = statusFilter;
			this.fromDate = fromDate;
			this.toDate = toDate;
		}

		public Long getLastSeenPaymentId() {
			return lastSeenPaymentId;
		}

		public List<CustomerPayments> getPayments() {
			return payments;
		}

		public String getSearchFilter() {
			return searchFilter;
		}

		public String getPaymentTypeFilter() {
			return paymentTypeFilter;
		}

		public String getStatusFilter() {
			return statusFilter;
		}

		public Date getFromDate() {
			return fromDate;
		}

		public Date getToDate() {
			return toDate;
		}
	}

	// Getters
	public String getSearch() {
		return search;
	}

	public String getPaymentType() {
		return paymentType;
	}

	public String getStatus() {
		return status;
	}

	public Date getFromDate() {
		return fromDate;
	}

	public Date getToDate() {
		return toDate;
	}

	public Long getLastSeenPaymentId() {
		return lastSeenPaymentId;
	}

	public boolean hasNextPage() {
		return hasNextPage;
	}

	public int getPageSize() {
		return pageSize;
	}

	public List<CustomerPayments> getPayments() {
		return payments;
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

	public void setPaymentType(String paymentType) {
		this.paymentType = paymentType;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public void setFromDate(Date fromDate) {
		this.fromDate = fromDate;
	}

	public void setToDate(Date toDate) {
		this.toDate = toDate;
	}

	public void setLastSeenPaymentId(Long lastSeenPaymentId) {
		this.lastSeenPaymentId = lastSeenPaymentId;
	}

	public void setHasNextPage(boolean hasNextPage) {
		this.hasNextPage = hasNextPage;
	}

	public void setPayments(List<CustomerPayments> payments) {
		this.payments = payments;
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
		paymentType = "All";
		status = "All";
		fromDate = null;
		toDate = null;
		lastSeenPaymentId = null;
		hasNextPage = true;
		payments.clear();
		pageHistory.clear();
	}

	public void resetPagination() {
		lastSeenPaymentId = null;
		hasNextPage = true;
		pageHistory.clear();
	}
}