package com.gierza_molases.molases_app.dto.delivery;

import java.util.List;
import java.util.Map;

import com.gierza_molases.molases_app.model.BranchDelivery;
import com.gierza_molases.molases_app.model.CustomerDelivery;
import com.gierza_molases.molases_app.model.ProductDelivery;

public class CustomerBranchDelivery {
	private CustomerDelivery customerDelivery;
	private Map<BranchDelivery, List<ProductDelivery>> branches;

	public CustomerBranchDelivery() {

	}

	public CustomerBranchDelivery(CustomerDelivery customerDelivery,
			Map<BranchDelivery, List<ProductDelivery>> branches) {
		this.customerDelivery = customerDelivery;
		this.branches = branches;
	}

	public CustomerDelivery getCustomerDelivery() {
		return customerDelivery;
	}

	public Map<BranchDelivery, List<ProductDelivery>> getBranches() {
		return branches;
	}

	public void setCustomerDelivery(CustomerDelivery customerDelivery) {
		this.customerDelivery = customerDelivery;
	}

	public void setBranches(Map<BranchDelivery, List<ProductDelivery>> branches) {
		this.branches = branches;
	}

}
