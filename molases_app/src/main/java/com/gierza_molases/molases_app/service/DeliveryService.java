package com.gierza_molases.molases_app.service;

import java.util.List;
import java.util.Map;

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

	public void addNewDelivery(Delivery delivery, Map<CustomerDelivery, List<BranchDelivery>> customerDeliveries) {
		TransactionHelper.executeInTransaction(conn -> {

			int deliveryId = deliveryDao.insert(conn, delivery);

			for (Map.Entry<CustomerDelivery, List<BranchDelivery>> entry : customerDeliveries.entrySet()) {

				CustomerDelivery cd = entry.getKey();
				List<BranchDelivery> branchList = entry.getValue();

				int customerDeliveryId = customerDeliveryDao.insert(conn, cd.getCustomerId(), deliveryId);

				branchDeliveryDao.insertAll(conn, customerDeliveryId, branchList);
			}
		});
	}

}
