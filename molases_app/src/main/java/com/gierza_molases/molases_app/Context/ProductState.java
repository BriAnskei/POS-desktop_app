package com.gierza_molases.molases_app.Context;

import java.util.ArrayList;
import java.util.List;

import com.gierza_molases.molases_app.model.Product;

public class ProductState {

	// Product data
	private List<Product> products = new ArrayList<>();

	// UI filters / controls
	private String search = "";
	private String sortOrder = "DESC"; // "ASC" or "DESC"

	// Optional UI flags
	private boolean loading = false;

	/*
	 * ====================== Getters ======================
	 */

	public List<Product> getProducts() {
		return products;
	}

	public String getSearch() {
		return search;
	}

	public String getSortOrder() {
		return sortOrder;
	}

	public boolean isLoading() {
		return loading;
	}

	/*
	 * ====================== Setters ======================
	 */

	public void setProducts(List<Product> products) {
		this.products = products;
	}

	public void setSearch(String search) {
		this.search = search;
	}

	public void setSortOrder(String sortOrder) {
		this.sortOrder = sortOrder;
	}

	public void setLoading(boolean loading) {
		this.loading = loading;
	}

	/*
	 * ====================== Helpers ======================
	 */

	public void clearProducts() {
		products.clear();
	}

	public void resetFilters() {
		search = "";
		sortOrder = "DESC";
	}

	public void reset() {
		clearProducts();
		resetFilters();
		loading = false;

	}
}
