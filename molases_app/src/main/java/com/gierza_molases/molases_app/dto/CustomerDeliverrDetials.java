package com.gierza_molases.molases_app.dto;

import java.util.List;
import java.util.Map;

import com.gierza_molases.molases_app.model.BranchDelivery;
import com.gierza_molases.molases_app.model.CustomerPayments;
import com.gierza_molases.molases_app.model.Delivery;

public class CustomerDeliverrDetials {
	public Delivery delivery;

	private Map<BranchDelivery, String> branchDeliveryStatuses;

	// added branch from customer delivery
	private List<BranchDelivery> addedBranchDelivery;

	// removed branch from customer deliver
	private List<BranchDelivery> removedBrancheDelivery;

	// edited product quantity on branch
	private List<BranchDelivery> editedBranchProductQuantity;

	// removed products form branches
	private List<BranchDelivery> removedProductsFromBranches;

	// payments
	private List<CustomerPayments> customerPayments;

}
