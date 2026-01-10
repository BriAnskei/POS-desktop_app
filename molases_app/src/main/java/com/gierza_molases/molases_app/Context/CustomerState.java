package com.gierza_molases.molases_app.Context;

import java.util.ArrayList;
import java.util.List;

import com.gierza_molases.molases_app.model.Customer;

public class CustomerState {

	private int currentPage = 1;
	private int itemsPerPage = 20;
	private int totalCustomers = 0;

	private String search = "";
	private String sortOrder = "DESC";

	private List<Customer> customers = new ArrayList<>();

	// getters & setters
	public int getCurrentPage() {

		return currentPage;
	}

	public void setCurrentPage(int currentPage) {
		this.currentPage = currentPage;
	}

	public int getItemsPerPage() {
		return itemsPerPage;
	}

	public int getTotalCustomers() {
		return totalCustomers;
	}

	public void setTotalCustomers(int totalCustomers) {
		this.totalCustomers = totalCustomers;
	}

	public String getSearch() {
		return search;
	}

	public void setSearch(String search) {
		this.search = search;
	}

	public String getSortOrder() {
		return sortOrder;
	}

	public void setSortOrder(String sortOrder) {
		this.sortOrder = sortOrder;
	}

	public List<Customer> getCustomers() {
		return customers;
	}

	public void setCustomers(List<Customer> customers) {
		this.customers = customers;
	}
}
