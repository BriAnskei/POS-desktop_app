package com.gierza_molases.molases_app.dto.delivery;

import java.util.List;

import com.gierza_molases.molases_app.model.CustomerPayments;

public class DeliveryChanges {

	private CustomerDeliveryChanges customerDeliveryChanges;
	private BranchDeliveryChanges branchDeliveryChanges;
	private DeliveryStatusChanges deliveryStatusChanges;
	private ProductDeliveryChanges productDeliveryChanges;

	// Payments
	private List<CustomerPayments> customerPaymentTypes;

	/** No-args constructor (required for serializers / frameworks) */
	public DeliveryChanges() {
	}

	/** Full constructor */
	public DeliveryChanges(CustomerDeliveryChanges customerDeliveryChanges, BranchDeliveryChanges branchDeliveryChanges,
			DeliveryStatusChanges deliveryStatusChanges, ProductDeliveryChanges productDeliveryChanges,
			List<CustomerPayments> customerPaymentTypes) {
		this.customerDeliveryChanges = customerDeliveryChanges;
		this.branchDeliveryChanges = branchDeliveryChanges;
		this.deliveryStatusChanges = deliveryStatusChanges;
		this.customerPaymentTypes = customerPaymentTypes;
		this.productDeliveryChanges = productDeliveryChanges;
	}

	// ===== Getters & Setters =====
	public CustomerDeliveryChanges getCustomerDeliveryChanges() {
		return customerDeliveryChanges;
	}

	public void setCustomerDeliveryChanges(CustomerDeliveryChanges customerDeliveryChanges) {
		this.customerDeliveryChanges = customerDeliveryChanges;
	}

	public BranchDeliveryChanges getBranchDeliveryChanges() {
		return branchDeliveryChanges;
	}

	public void setBranchDeliveryChanges(BranchDeliveryChanges branchDeliveryChanges) {
		this.branchDeliveryChanges = branchDeliveryChanges;
	}

	public DeliveryStatusChanges getDeliveryStatusChanges() {
		return deliveryStatusChanges;
	}

	public void setDeliveryStatusChanges(DeliveryStatusChanges deliveryStatusChanges) {
		this.deliveryStatusChanges = deliveryStatusChanges;
	}

	public ProductDeliveryChanges getProductDeliveryChanges() {
		return productDeliveryChanges;
	}

	public void setProductDeliveryChanges(ProductDeliveryChanges productDeliveryChanges) {
		this.productDeliveryChanges = productDeliveryChanges;
	}

	public List<CustomerPayments> getCustomerPaymentTypes() {
		return customerPaymentTypes;
	}

	public void setCustomerPaymentTypes(List<CustomerPayments> customerPaymentTypes) {
		this.customerPaymentTypes = customerPaymentTypes;
	}
}
