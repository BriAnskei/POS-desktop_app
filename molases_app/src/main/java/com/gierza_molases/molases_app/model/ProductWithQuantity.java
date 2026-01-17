package com.gierza_molases.molases_app.model;

/**
 * Represents a product with its quantity for delivery purposes. This is used to
 * track how many units of a product are being delivered.
 */
public class ProductWithQuantity {
	private final Product product;
	private int quantity;

	public ProductWithQuantity(Product product, int quantity) {
		if (product == null) {
			throw new IllegalArgumentException("Product cannot be null");
		}
		if (quantity <= 0) {
			throw new IllegalArgumentException("Quantity must be greater than 0");
		}
		this.product = product;
		this.quantity = quantity;
	}

	public Product getProduct() {
		return product;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		if (quantity <= 0) {
			throw new IllegalArgumentException("Quantity must be greater than 0");
		}
		this.quantity = quantity;
	}

	/**
	 * Calculate total selling price for this product (quantity * selling price)
	 */
	public double getTotalSellingPrice() {
		return product.getSellingPrice() * quantity;
	}

	/**
	 * Calculate total capital cost for this product (quantity * capital)
	 */
	public double getTotalCapital() {
		return product.getCapital() * quantity;
	}

	/**
	 * Calculate total profit for this product (quantity * profit per unit)
	 */
	public double getTotalProfit() {
		return product.getProfit() * quantity;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		ProductWithQuantity that = (ProductWithQuantity) o;
		return product.getId() == that.product.getId();
	}

	@Override
	public int hashCode() {
		return Integer.hashCode(product.getId());
	}

	@Override
	public String toString() {
		return "ProductWithQuantity{" + "product=" + product.getName() + ", quantity=" + quantity + ", totalPrice="
				+ getTotalSellingPrice() + '}';
	}
}