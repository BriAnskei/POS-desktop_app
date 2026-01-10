package com.gierza_molases.molases_app.model;

import java.time.LocalDateTime;

public class Product {
	private int id;
	private String name;
	private double sellingPrice;
	private double capital;

	private LocalDateTime createdAt;
	private boolean isProductAssociateWithCustomer;
	private int associatedCount;

	// Validation
	public static final int MIN_NAME_LENGTH = 3;
	public static final int MAX_NAME_LENGTH = 100;

	// creation
	public Product(String name, double sellingPrice, double capital) {
		this.name = name;
		this.sellingPrice = sellingPrice;
		this.capital = capital;
	}

	// Constructor without associate customer
	public Product(int id, String name, double sellingPrice, double capital, LocalDateTime createdAt) {
		this.id = id;
		this.name = name;
		this.sellingPrice = sellingPrice;
		this.capital = capital;
		this.createdAt = createdAt;
		this.isProductAssociateWithCustomer = false;
	}

	// Constructor with association indicator
	public Product(int id, String name, double sellingPrice, double capital, LocalDateTime createdAt,
			boolean isProductAssociateWithCustomer, int associatedCount) {
		this.id = id;
		this.name = name;
		this.sellingPrice = sellingPrice;
		this.capital = capital;
		this.createdAt = createdAt;
		this.isProductAssociateWithCustomer = isProductAssociateWithCustomer;
		this.associatedCount = associatedCount;
	}

	// validation
	public void validate() {

		// Name validation
		if (name == null || name.trim().isEmpty()) {
			throw new IllegalArgumentException("Product name is required.");
		}

		String trimmedName = name.trim();

		if (trimmedName.length() < MIN_NAME_LENGTH) {
			throw new IllegalArgumentException("Product name must be at least " + MIN_NAME_LENGTH + " characters.");
		}

		if (trimmedName.length() > MAX_NAME_LENGTH) {
			throw new IllegalArgumentException("Product name must not exceed " + MAX_NAME_LENGTH + " characters.");
		}

		// Price validation
		if (sellingPrice < 0) {
			throw new IllegalArgumentException("Selling price cannot be negative.");
		}

		if (capital < 0) {
			throw new IllegalArgumentException("Capital cannot be negative.");
		}

		// Round prices to 2 decimal places
		sellingPrice = Math.round(sellingPrice * 100.0) / 100.0;
		capital = Math.round(capital * 100.0) / 100.0;

		// Profit validation
		if (sellingPrice <= capital) {
			throw new IllegalArgumentException("Selling price must be greater than capital to ensure profit.");
		}
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public double getSellingPrice() {
		return sellingPrice;
	}

	public double getCapital() {
		return capital;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public boolean isProductAssociateWithCustomer() {
		return isProductAssociateWithCustomer;
	}

	public int getAssociatedCount() {
		return associatedCount;
	}

	public void setProductAssociateWithCustomer(boolean isProductAssociateWithCustomer) {
		this.isProductAssociateWithCustomer = isProductAssociateWithCustomer;
	}

	public double getProfit() {
		return sellingPrice - capital;
	}

	public double getProfitMargin() {
		if (capital == 0)
			return 0;
		return ((sellingPrice - capital) / capital) * 100;
	}
}
