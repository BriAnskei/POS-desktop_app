package com.gierza_molases.molases_app.service;

import java.util.List;

import com.gierza_molases.molases_app.dao.BranchDao;
import com.gierza_molases.molases_app.model.Branch;

public class BranchService {

	private final BranchDao branchDao = new BranchDao();

//	/**
//	 * Add multiple branches for one customer
//	 */
//	public void addBranches(int customerId, List<String> addresses, List<String> notes) {
//
//		if (addresses == null || addresses.isEmpty()) {
//			throw new IllegalArgumentException("At least one branch address is required");
//		}
//
//		List<Branch> branches = new ArrayList<>();
//
//		for (int i = 0; i < addresses.size(); i++) {
//			String address = addresses.get(i);
//			String note = (notes != null && notes.size() > i) ? notes.get(i) : null;
//
//			branches.add(new Branch(customerId, address, note));
//		}
//
//		branchDao.insertBranches(branches);
//	}

	/**
	 * Get all branches for a customer
	 */
	public List<Branch> getBranchesByCustomerId(int customerId) {
		return branchDao.findAllByCustomerId(customerId);
	}
}
