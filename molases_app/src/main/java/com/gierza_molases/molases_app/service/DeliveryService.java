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
		DeliveryViewResponse response = new DeliveryViewResponse();

		TransactionHelper.executeInTransaction(conn -> {
			Delivery delivery = deliveryDao.findById(conn, deliveryId);
			List<CustomerDelivery> cds = customerDeliveryDao.findAllByDeliveryId(conn, deliveryId);

			response.setDelivery(delivery);

			assembleCustomerDeliveries(response, cds, conn);
		});

		return response;
	}

	private void assembleCustomerDeliveries(DeliveryViewResponse response, List<CustomerDelivery> customerDeliveries,
			Connection conn) throws SQLException {
		Map<CustomerDelivery, List<BranchDelivery>> raw = new HashMap<>();
		Map<Customer, Map<Branch, List<ProductWithQuantity>>> mapped = new HashMap<>();

		for (CustomerDelivery cd : customerDeliveries) {
			List<BranchDelivery> bds = branchDeliveryDao.findAllByCustomerDelivery(conn, cd.getId());

			raw.put(cd, bds);

			Customer customer = customerDao.findById(cd.getCustomerId(), conn);

			Map<Branch, List<ProductWithQuantity>> branchMap = new HashMap<>();
			for (BranchDelivery bd : bds) {
				Branch branch = branchDao.findById(bd.getBranchId(), conn);
				Product product = productDao.findById(bd.getProductId(), conn);

				System.out.println("Branch of customer delivery: " + bd.getBranchId());

				branchMap.computeIfAbsent(branch, b -> new ArrayList<>())
						.add(new ProductWithQuantity(product, bd.getQuantity()));
			}

			mapped.put(customer, branchMap);
		}

		response.setCustomerDeliveries(raw);
		response.setMappedCustomerDelivery(mapped);
	}

	public void deleteDelivery(int deliveryId) {
		deliveryDao.deleteDelivery(deliveryId);
	}

}
