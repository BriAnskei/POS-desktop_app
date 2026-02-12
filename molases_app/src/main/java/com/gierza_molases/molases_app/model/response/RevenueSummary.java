package com.gierza_molases.molases_app.model.response;

public class RevenueSummary {
	private double totalRevenue;
	private double totalExpenses;
	private double netProfit;

	public RevenueSummary(double totalRevenue, double totalExpenses, double netProfit) {
		this.totalRevenue = totalRevenue;
		this.totalExpenses = totalExpenses;
		this.netProfit = netProfit;
	}

	// Getters
	public double getTotalRevenue() {
		return totalRevenue;
	}

	public double getTotalExpenses() {
		return totalExpenses;
	}

	public double getNetProfit() {
		return netProfit;
	}
}
