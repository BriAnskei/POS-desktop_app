package com.gierza_molases.molases_app.service;

import java.util.List;
import java.util.Map;

import com.gierza_molases.molases_app.dao.BranchDeliveryDao;
import com.gierza_molases.molases_app.dao.CustomerDeliveryDao;
import com.gierza_molases.molases_app.dao.DeliveryDao;
import com.gierza_molases.molases_app.model.BranchDelivery;
import com.gierza_molases.molases_app.model.CustomerDelivery;
import com.gierza_molases.molases_app.model.Delivery;

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

	public void addNewDelivery(Delivery delivery, Map<CustomerDelivery, List<BranchDelivery>> customerDelivery) {
//		TransactionHelper.executeInTransaction(conn -> {
//			// 1. Insert the main delivery record
//			int deliveryId = this.deliveryDao.insert(conn, delivery);
//
//			// 2. Insert each customer delivery and their branch deliveries
//			for (Map.Entry<CustomerDelivery, List<BranchDelivery>> entry : customerDeliveryMap.entrySet()) {
//				CustomerDelivery customerDelivery = entry.getKey();
//				List<BranchDelivery> branchDeliveries = entry.getValue();
//
//				// Set the delivery ID
//				CustomerDelivery customerDeliveryWithId = new CustomerDelivery(customerDelivery.getCustomerId(),
//						deliveryId);
//
//				// Insert customer_delivery record
//				int customerDeliveryId = this.customerDeliveryDao.insert(conn, customerDeliveryWithId);
//
//				// Insert all branch_delivery records for this customer
//				for (BranchDelivery branchDelivery : branchDeliveries) {
//					BranchDelivery branchDeliveryWithId = new BranchDelivery(customerDeliveryId,
//							branchDelivery.getBranchId(), branchDelivery.getProductId(), branchDelivery.getQuantity(),
//							branchDelivery.getStatus());
//
//					this.branchDeliveryDao.insert(conn, branchDeliveryWithId);
//				}
//			}
//		});

	}

}
