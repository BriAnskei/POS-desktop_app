package com.gierza_molases.molases_app.service;

import com.gierza_molases.molases_app.dao.BranchDeliveryDao;
import com.gierza_molases.molases_app.dao.CustomerDeliveryDao;

public class CustomerDeliveryService {
	private final CustomerDeliveryDao customerDeliveryDao;
	private final BranchDeliveryDao branchDeliveryDao;

	public CustomerDeliveryService(CustomerDeliveryDao customerDeliveryDao, BranchDeliveryDao branchDeliveryDao) {
		this.customerDeliveryDao = customerDeliveryDao;
		this.branchDeliveryDao = branchDeliveryDao;

	}
//
//	public void insert(int deliveryId, Map<CustomerDelivery, List<BranchDelivery>> customerDeliveries) {
//		TransactionHelper.executeInTransaction(conn -> {
//			for (Map.Entry<CustomerDelivery, List<BranchDelivery>> entry : customerDeliveries.entrySet()) {
//				CustomerDelivery cd = entry.getKey();
//				List<BranchDelivery> branchList = entry.getValue();
//
//				int customerDeliveryId = customerDeliveryDao.insert(conn, cd.getCustomerId(), deliveryId);
//
//				branchDeliveryDao.insertAll(conn, customerDeliveryId, branchList);
//
//			}
//		});
//	}
}
