package com.gierza_molases.molases_app.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.gierza_molases.molases_app.dao.BranchDao;
import com.gierza_molases.molases_app.dao.BranchDeliveryDao;
import com.gierza_molases.molases_app.dao.CustomerDao;
import com.gierza_molases.molases_app.dao.CustomerDeliveryDao;
import com.gierza_molases.molases_app.dao.DeliveryDao;
import com.gierza_molases.molases_app.dao.ProductDao;
import com.gierza_molases.molases_app.model.Branch;
import com.gierza_molases.molases_app.model.BranchDelivery;
import com.gierza_molases.molases_app.model.Customer;
import com.gierza_molases.molases_app.model.CustomerDelivery;
import com.gierza_molases.molases_app.model.Delivery;
import com.gierza_molases.molases_app.model.Product;
import com.gierza_molases.molases_app.model.ProductWithQuantity;
import com.gierza_molases.molases_app.model.response.DeliveryViewResponse;
import com.gierza_molases.molases_app.util.TransactionHelper;

public class DeliveryService {

	private final DeliveryDao deliveryDao;
	private final CustomerDeliveryDao customerDeliveryDao;
	private final BranchDeliveryDao branchDeliveryDao;

	// transaction fetcher (Mapper) Dao
	private final CustomerDao customerDao;
	private final BranchDao branchDao;
	private final ProductDao productDao;

	public DeliveryService(DeliveryDao deliveryDao, CustomerDeliveryDao customerDeliverDao,
			BranchDeliveryDao branchDeliveryDao, CustomerDao customerDao, BranchDao branchDao, ProductDao productDao) {
		this.deliveryDao = deliveryDao;
		this.customerDeliveryDao = customerDeliverDao;
		this.branchDeliveryDao = branchDeliveryDao;

		this.customerDao = customerDao;
		this.branchDao = branchDao;
		this.productDao = productDao;
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

	public List<Delivery> fetchNextPage(Long lastSeenId, String search, Date startAt, Date endAt) {
		return deliveryDao.fetchNextPageNewest(lastSeenId, search, startAt, endAt, 15); // page size fixed to 15
	}

	public DeliveryViewResponse getDeliveryDetials(int deliveryId) {
		DeliveryViewResponse deliveryDetialsResponse = new DeliveryViewResponse();

		TransactionHelper.executeInTransaction(conn -> {
			Delivery delivery = this.deliveryDao.findById(conn, deliveryId);

			List<CustomerDelivery> customerDeliveries = customerDeliveryDao.findAllByDeliveryId(conn, deliveryId);

			deliveryDetialsResponse.setDelivery(delivery);
			// mapped
			deliveryDetialsResponse.setMappedCustomerDelivery(this.mapCustomerDeliveries(customerDeliveries, conn));
			// raww

		});

		return deliveryDetialsResponse;
	}

	public Map<Customer, Map<Branch, List<ProductWithQuantity>>> mapCustomerDeliveries(
			List<CustomerDelivery> customerDeliveries, Connection conn) throws SQLException {

		Map<Customer, Map<Branch, List<ProductWithQuantity>>> mappedCustomerDeliveries = new HashMap<>();

		for (CustomerDelivery cd : customerDeliveries) {
			Customer customer = this.customerDao.findById(cd.getCustomerId(), conn);

			// customer customer branches for this delivery
			Map<Branch, List<ProductWithQuantity>> customerBranchesDeliveries = this
					.getCustomerCustomerBranchDeliveries(cd.getId(), conn);

			mappedCustomerDeliveries.put(customer, customerBranchesDeliveries);
		}

		return mappedCustomerDeliveries;

	}

	public Map<Branch, List<ProductWithQuantity>> getCustomerCustomerBranchDeliveries(int customerDeliveryId,
			Connection conn) {
		// fetch customer branches for this delivery
		List<BranchDelivery> customerBranchesDelivery = this.branchDeliveryDao.findAllByCustomerDelivery(conn,
				customerDeliveryId);

		// mappedCutomer deliveries
		return this.mapCustomerBranchesDelivery(customerBranchesDelivery, conn);

	}

	public Map<Branch, List<ProductWithQuantity>> mapCustomerBranchesDelivery(List<BranchDelivery> branchesDelivery,
			Connection conn) {

		Map<Branch, List<ProductWithQuantity>> customerBranchesDelivery = new HashMap<>();

		for (BranchDelivery bd : branchesDelivery) {
			Branch branch = branchDao.findById(bd.getBranchId(), conn);
			Product product = productDao.findById(bd.getProductId(), conn);

			ProductWithQuantity pwq = new ProductWithQuantity(product, bd.getQuantity());

			// get existing list or create a new one
			customerBranchesDelivery.computeIfAbsent(branch, b -> new ArrayList<>()).add(pwq);
		}

		return customerBranchesDelivery;
	}

	public void deleteDelivery(int deliveryId) {
		deliveryDao.deleteDelivery(deliveryId);
	}

}
