package com.gierza_molases.molases_app.model.response;

public class DeletionPreview {
	public int deliveryCount;
	public int customerDeliveryCount;
	public int branchDeliveryCount;
	public int productDeliveryCount;
	public int paymentCount;
	public int paymentHistoryCount;

	@Override
	public String toString() {
		return String.format(
				"Deletion Preview:\n" + "- Deliveries: %d\n" + "- Customer Deliveries: %d\n"
						+ "- Branch Deliveries: %d\n" + "- Product Deliveries: %d\n" + "- Payments: %d\n"
						+ "- Payment History: %d",
				deliveryCount, customerDeliveryCount, branchDeliveryCount, productDeliveryCount, paymentCount,
				paymentHistoryCount);
	}
}