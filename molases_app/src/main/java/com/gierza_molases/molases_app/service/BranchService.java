package com.gierza_molases.molases_app.service;

import java.util.List;

import com.gierza_molases.molases_app.dao.BranchDao;
import com.gierza_molases.molases_app.dao.CustomerDao;
import com.gierza_molases.molases_app.model.Branch;
import com.gierza_molases.molases_app.model.BranchCustomerResponse;
import com.gierza_molases.molases_app.model.Customer;

public class BranchService {

	private final BranchDao branchDao;
	private final CustomerDao customerDao;

	public BranchService(BranchDao branchDao, CustomerDao customerDao) {
		this.branchDao = branchDao;
		this.customerDao = customerDao;
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
	public List<Branch> getBranchesByCustomerId(Long lastSeenBranchId, String customerFilterName, int pageSize) {
		return branchDao.fetchNextPage(lastSeenBranchId, customerFilterName, pageSize);
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
		branchDao.deleteBranch(branchId);
	}
}
