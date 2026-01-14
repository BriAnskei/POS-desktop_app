package com.gierza_molases.molases_app.service;

import java.util.List;

import com.gierza_molases.molases_app.dao.BranchDeliveryDao;
import com.gierza_molases.molases_app.dao.CustomerDeliveryDao;
import com.gierza_molases.molases_app.dao.DeliveryDao;
import com.gierza_molases.molases_app.model.BranchDelivery;
import com.gierza_molases.molases_app.model.CustomerDelivery;
import com.gierza_molases.molases_app.model.Delivery;
import com.gierza_molases.molases_app.util.TransactionHelper;

public class DeliveryService {

	private final DeliveryDao deliveryDao;
	private final CustomerDeliveryDao customerDeliveryDao;
	private final BranchDeliveryDao branchDeliveryDao;

	public DeliveryService(DeliveryDao deliveryDao, CustomerDeliveryDao customerDeliverDao,
			BranchDeliveryDao branchDeliveryDao) {
		this.deliveryDao = deliveryDao;
		this.customerDeliveryDao = customerDeliverDao;
		this.branchDeliveryDao = branchDeliveryDao;
	}

	public void addNewDelivery(Delivery delivery, List<CustomerDelivery> customerDelivery,
			List<BranchDelivery> branchDelivery) {
		TransactionHelper.executeInTransaction(conn -> {

			// insert delivery
			int deliveryId = this.deliveryDao.insert(conn, delivery);

		});

	}

}
