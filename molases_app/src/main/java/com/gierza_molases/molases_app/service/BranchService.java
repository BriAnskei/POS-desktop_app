package com.gierza_molases.molases_app.service;

import java.util.List;

import com.gierza_molases.molases_app.dao.BranchDao;
import com.gierza_molases.molases_app.dao.BranchDeliveryDao;
import com.gierza_molases.molases_app.dao.CustomerDao;
import com.gierza_molases.molases_app.model.Branch;
import com.gierza_molases.molases_app.model.Customer;
import com.gierza_molases.molases_app.model.response.BranchCustomerResponse;

public class BranchService {

	private final BranchDao branchDao;
	private final CustomerDao customerDao;

	private final BranchDeliveryDao branchDeliveryDao;

	public BranchService(BranchDao branchDao, CustomerDao customerDao, BranchDeliveryDao branchDeliveryDao) {
		this.branchDao = branchDao;
		this.customerDao = customerDao;

		this.branchDeliveryDao = branchDeliveryDao;
	}

	/**
	 * Add multiple branches for one customer
	 */
	public void addBranche(int customerId, Branch branch) {

		branch.validate();

		branchDao.insertBranch(customerId, branch);
	}

	/**
	 * Get all branches with search
	 */
	public List<Branch> fetchBranchCursor(Long lastSeenBranchId, String customerFilterName, int pageSize) {

		// we add one to pageSize so that we can check if there is more page to fetch
		return branchDao.fetchNextPage(lastSeenBranchId, customerFilterName, pageSize + 1);
	}

	/**
	 * get branch details
	 */
	public BranchCustomerResponse getBanchDetails(int branchId) {
		Branch branch = branchDao.findById(branchId);
		Customer customer = customerDao.findById(branch.getCustomerId());

		return new BranchCustomerResponse(branch, customer);
	}

	/**
	 * Get all branches for a customer
	 */
	public List<Branch> getBranchesByCustomerId(int customerId) {

		return branchDao.findAllByCustomerId(customerId);
	}

	public void update(int branchId, Branch branch) {

		branch.validate();

		branchDao.updateBranch(branchId, branch);
	}

	public void delete(int branchId) {

		// check first for transaction
		if (branchDeliveryDao.hasDelivery(branchId)) {
			throw new IllegalStateException("Cannot delete branch because it has deliveries.");
		}

		branchDao.deleteBranch(branchId);
	}
}
