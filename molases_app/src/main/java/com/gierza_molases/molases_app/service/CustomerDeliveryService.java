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

}
