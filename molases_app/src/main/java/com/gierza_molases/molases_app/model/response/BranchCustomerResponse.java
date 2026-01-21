package com.gierza_molases.molases_app.model.response;

import com.gierza_molases.molases_app.model.Branch;
import com.gierza_molases.molases_app.model.Customer;

public class BranchCustomerResponse {
	private Branch branch;
	private Customer customer;

	// Constructors
	public BranchCustomerResponse() {
	}

	public BranchCustomerResponse(Branch branch, Customer customer) {
		this.branch = branch;
		this.customer = customer;
	}

	// Getters and setters
	public Branch getBranch() {
		return branch;
	}

	public void setBranch(Branch branch) {
		this.branch = branch;
	}

	public Customer getCustomer() {
		return customer;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
	}
}