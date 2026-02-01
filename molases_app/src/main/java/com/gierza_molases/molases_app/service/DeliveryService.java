package com.gierza_molases.molases_app.service;

import java.sql.Connection;
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
import com.gierza_molases.molases_app.dao.ProductDeliveryDao;
import com.gierza_molases.molases_app.dto.delivery.CustomerBranchDelivery;
import com.gierza_molases.molases_app.dto.delivery.NewDelivery;
import com.gierza_molases.molases_app.model.Branch;
import com.gierza_molases.molases_app.model.BranchDelivery;
import com.gierza_molases.molases_app.model.Customer;
import com.gierza_molases.molases_app.model.CustomerDelivery;
import com.gierza_molases.molases_app.model.Delivery;
import com.gierza_molases.molases_app.model.Product;
import com.gierza_molases.molases_app.model.ProductDelivery;
import com.gierza_molases.molases_app.model.ProductWithQuantity;
import com.gierza_molases.molases_app.model.response.DeliveryViewResponse;
import com.gierza_molases.molases_app.util.TransactionHelper;

public class DeliveryService {

	private final DeliveryDao deliveryDao;
	private final CustomerDeliveryDao customerDeliveryDao;
	private final BranchDeliveryDao branchDeliveryDao;
	private final ProductDeliveryDao productDeliveryDao;

	// transaction fetcher (Mapper) Dao
	private final CustomerDao customerDao;
	private final BranchDao branchDao;
	private final ProductDao productDao;

	public DeliveryService(DeliveryDao deliveryDao, CustomerDeliveryDao customerDeliverDao,
			BranchDeliveryDao branchDeliveryDao, ProductDeliveryDao productDeliveryDao, CustomerDao customerDao,
			BranchDao branchDao, ProductDao productDao) {
		this.deliveryDao = deliveryDao;
		this.customerDeliveryDao = customerDeliverDao;
		this.branchDeliveryDao = branchDeliveryDao;
		this.productDeliveryDao = productDeliveryDao;

		this.customerDao = customerDao;
		this.branchDao = branchDao;
		this.productDao = productDao;
	}

	public void addNewDelivery(NewDelivery newDelivery) {
		TransactionHelper.executeInTransaction(conn -> {
			int deliveryId = deliveryDao.insert(conn, newDelivery.getDelivery());

			System.out.println("Inserting new Dleivery");
			insertCustomerDeliveries(deliveryId, newDelivery.getCustomerBranchDeliveries(), conn);

		});
	}

	private void insertCustomerDeliveries(int deliveryId, List<CustomerBranchDelivery> customerBranchDeliveries,
			Connection conn) throws Exception {

		System.out.println("Inserting new insertCustomerDeliveries");
		for (CustomerBranchDelivery cbd : customerBranchDeliveries) {

			CustomerDelivery cd = cbd.getCustomerDelivery();
			cd.setDeliveryId(deliveryId);

			System.out.println("Inserting customerDelivery");
			int customerDeliveryId = customerDeliveryDao.insert(conn, cd);
			System.out.println("customerDelivery done");
			insertAllBranchAndProducts(customerDeliveryId, cbd.getBranches(), conn);
		}
	}

	private void insertAllBranchAndProducts(int customerDeliveryId, Map<BranchDelivery, List<ProductDelivery>> branches,
			Connection conn) throws Exception {
		for (Map.Entry<BranchDelivery, List<ProductDelivery>> entry : branches.entrySet()) {

			BranchDelivery bd = entry.getKey();
			List<ProductDelivery> pd = entry.getValue();

			System.out.println("Inserting branchDeliveryDao");
			int branchDeliveryId = branchDeliveryDao.insert(conn, customerDeliveryId, bd);
			System.out.println("branchDeliveryDao done");
			System.out.println("Inserting productDeliveryDao");
			productDeliveryDao.insertAll(branchDeliveryId, pd, conn);
			System.out.println("productDeliveryDao done");
		}
	}

	public List<Delivery> fetchNextPage(Long lastSeenId, String search, Date startAt, Date endAt) {
		return deliveryDao.fetchNextPageNewest(lastSeenId, search, startAt, endAt, 15); // page size fixed to 15
	}

	public DeliveryViewResponse getDeliveryDetials(int deliveryId) {
		DeliveryViewResponse response = new DeliveryViewResponse();

		TransactionHelper.executeInTransaction(conn -> {
			assembleDeliveryViewResponse(response, deliveryId, conn);
		});

		return response;
	}

	private void assembleDeliveryViewResponse(DeliveryViewResponse response, int deliveryId, Connection conn)
			throws Exception {
		Delivery delivery = deliveryDao.findById(conn, deliveryId);

		List<CustomerBranchDelivery> customerDeliveries = fetchCustomerDeliveries(deliveryId, conn);
		Map<Customer, Map<Branch, List<ProductWithQuantity>>> mappedCustomerDeliveries = fetchMappedCustomerDeliveries(
				customerDeliveries, conn);

		response.setCustomerDeliveries(customerDeliveries);
		response.setMappedCustomerDelivery(mappedCustomerDeliveries);
		response.setDelivery(delivery);

	}

	/**
	 * fetches the raw data of the customer deliveries.
	 */
	private List<CustomerBranchDelivery> fetchCustomerDeliveries(int deliveryId, Connection conn) throws Exception {
		List<CustomerBranchDelivery> customerBranchDeliveries = new ArrayList<>();

		List<CustomerDelivery> customerDeliveryList = customerDeliveryDao.findAllByDeliveryId(conn, deliveryId);

		for (CustomerDelivery cd : customerDeliveryList) {
			CustomerBranchDelivery cbd = new CustomerBranchDelivery();
			cbd.setCustomerDelivery(cd);

			Map<BranchDelivery, List<ProductDelivery>> branchesAndProductsOrders = fetchBranchesAndProductsOrders(
					deliveryId, conn);
			cbd.setBranches(branchesAndProductsOrders);

			customerBranchDeliveries.add(cbd);

		}
		return customerBranchDeliveries;
	}

	private Map<BranchDelivery, List<ProductDelivery>> fetchBranchesAndProductsOrders(int customerDeliveryId,
			Connection conn) throws Exception {
		Map<BranchDelivery, List<ProductDelivery>> customerBranchesProductsDelivery = new HashMap<>();

		List<BranchDelivery> customerBranchDelivery = fetchCustomerBranches(customerDeliveryId, conn);

		for (BranchDelivery branchDelivery : customerBranchDelivery) {
			List<ProductDelivery> customerBranchProductsOrders = fetchCustomerProductsOrders(branchDelivery.getId(),
					conn);
			customerBranchesProductsDelivery.put(branchDelivery, customerBranchProductsOrders);

		}

		return customerBranchesProductsDelivery;
	}

	private List<BranchDelivery> fetchCustomerBranches(int customerDeliveryId, Connection conn) {
		return branchDeliveryDao.findAllByCustomerDelivery(conn, customerDeliveryId);
	}

	private List<ProductDelivery> fetchCustomerProductsOrders(int branchDeliveryId, Connection conn) throws Exception {
		return productDeliveryDao.fetchProductDeliverisByBranchDeliveryId(branchDeliveryId, conn);
	}

	/**
	 * fetches the mapped data used for ui.
	 */
	private Map<Customer, Map<Branch, List<ProductWithQuantity>>> fetchMappedCustomerDeliveries(
			List<CustomerBranchDelivery> customerBranchDeliveries, Connection conn) {
		Map<Customer, Map<Branch, List<ProductWithQuantity>>> mappedCustomerDelivery = new HashMap<>();

		for (CustomerBranchDelivery cbd : customerBranchDeliveries) {
			Customer customer = customerDao.findById(cbd.getCustomerDelivery().getCustomerId());

			Map<Branch, List<ProductWithQuantity>> customerBranchOrders = fetchMappedBranchProductsDeliveries(
					cbd.getBranches(), conn);

			mappedCustomerDelivery.put(customer, customerBranchOrders);
		}

		return mappedCustomerDelivery;

	}

	private Map<Branch, List<ProductWithQuantity>> fetchMappedBranchProductsDeliveries(
			Map<BranchDelivery, List<ProductDelivery>> branchesOrders, Connection conn) {
		Map<Branch, List<ProductWithQuantity>> customerOrders = new HashMap<>();

		for (Map.Entry<BranchDelivery, List<ProductDelivery>> entry : branchesOrders.entrySet()) {
			BranchDelivery bd = entry.getKey();
			List<ProductDelivery> productDeliveryList = entry.getValue();

			Branch branch = branchDao.findById(bd.getBranchId(), conn);

			// fetch products
			List<ProductWithQuantity> productWithQuantityList = fetchBranchProductsOrders(productDeliveryList, conn);

			customerOrders.put(branch, productWithQuantityList);

		}

		return customerOrders;

	}

	private List<ProductWithQuantity> fetchBranchProductsOrders(List<ProductDelivery> productDeliveryList,
			Connection conn) {
		List<ProductWithQuantity> productWithQuantityList = new ArrayList<>();
		for (ProductDelivery pd : productDeliveryList) {
			Product product = productDao.findById(pd.getProductId(), conn);

			ProductWithQuantity pwq = new ProductWithQuantity(product, pd.getQuantity());

			productWithQuantityList.add(pwq);

		}

		return productWithQuantityList;
	}

	public void markasDelivered(int deliveryId) {

	}

	public void deleteDelivery(int deliveryId) {
		deliveryDao.deleteDelivery(deliveryId);
	}

}
