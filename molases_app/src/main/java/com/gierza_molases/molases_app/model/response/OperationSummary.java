package com.gierza_molases.molases_app.model.response;

public class OperationSummary {

	private int totalDeliveries;
	private int pendingDeliveries;
	private int totalCustomers;
	private int totalProducts;

	public OperationSummary(int totalDeliveries, int pendingDeliveries, int totalCustomers, int totalProducts) {
		this.totalDeliveries = totalDeliveries;
		this.pendingDeliveries = pendingDeliveries;
		this.totalCustomers = totalCustomers;
		this.totalProducts = totalProducts;
	}

	public int getTotalDeliveries() {
		return totalDeliveries;
	}

	public int getPendingDeliveries() {
		return pendingDeliveries;
	}

	public int getTotalCustomers() {
		return totalCustomers;
	}

	public int getTotalProducts() {
		return totalProducts;
	}
}
