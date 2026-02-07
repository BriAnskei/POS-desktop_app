package com.gierza_molases.molases_app.service;

import com.gierza_molases.molases_app.dao.BranchDeliveryDao;

public class BranchDeliveryService {

	private final BranchDeliveryDao branchDeliveryDao;

	public BranchDeliveryService(BranchDeliveryDao branchDeliveryDao) {
		this.branchDeliveryDao = branchDeliveryDao;
	}

//	public void insertAllBranchDelivery(int customerDeliveryId, List<BranchDelivery> list) {
//		this.branchDeliveryDao.insertAll(customerDeliveryId, list);
//	}

}