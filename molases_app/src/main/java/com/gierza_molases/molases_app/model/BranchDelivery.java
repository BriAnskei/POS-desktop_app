package com.gierza_molases.molases_app.model;

public class BranchDelivery {

	private Integer id;
	private int customerDeliveryId;
	private int branchId;

	private String status; // scheduled, cancelled, delivered;

	// create
	public BranchDelivery(int customerDeliveryId, int branchId, String status) {
		this.customerDeliveryId = customerDeliveryId;
		this.branchId = branchId;

		this.status = status;
	}

	public BranchDelivery(Integer id, int branchId, int customerDeliveryId, String status) {
		this.id = id;
		this.customerDeliveryId = customerDeliveryId;
		this.branchId = branchId;

		this.status = status;
	}

	public Integer getId() {
		return id;
	}

	public int getCustomerDeliveryId() {
		return customerDeliveryId;
	}

	public int getBranchId() {
		return branchId;
	}

	public String getStatus() {
		return status;
	}

	@Override
	public String toString() {
		return "BranchDelivery{" + "id=" + id + ", customerDeliveryId=" + customerDeliveryId + ", branchId=" + branchId
				+ ", status='" + status + '\'' + '}';
	}

}
